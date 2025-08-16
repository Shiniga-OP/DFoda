package com.dfoda.otimizadores.ssa.atras;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Comparator;
import com.dfoda.otimizadores.rop.codigo.RopMethod;
import com.dfoda.otimizadores.ssa.RegisterMapper;
import com.dfoda.otimizadores.rop.codigo.BasicBlockList;
import com.dfoda.otimizadores.rop.codigo.BasicBlock;
import com.dfoda.otimizadores.rop.codigo.InsnList;
import com.dfoda.otimizadores.rop.codigo.RegisterSpecList;
import com.dfoda.otimizadores.rop.codigo.RegisterSpec;
import com.dfoda.otimizadores.ssa.BasicRegisterMapper;
import com.dfoda.otimizadores.ssa.SsaMethod;
import com.dfoda.otimizadores.ssa.SsaBasicBlock;
import com.dfoda.otimizadores.ssa.SsaInsn;
import com.dfoda.otimizadores.rop.codigo.Rops;
import com.dfoda.otimizadores.rop.codigo.Rop;
import com.dfoda.util.IntList;
import com.dfoda.otimizadores.ssa.PhiInsn;
import com.dfoda.util.Hex;

public class SsaToRop {
    private static final boolean DEBUG = false;
    private final SsaMethod ssaMeth;
    private final boolean minimizeRegisters;
    private final InterferenceGraph interference;

    public static RopMethod convertToRopMethod(SsaMethod ssaMeth, boolean minimizeRegisters) {
        return new SsaToRop(ssaMeth, minimizeRegisters).convert();
    }

    private SsaToRop(SsaMethod ssaMethod, boolean minimizeRegisters) {
        this.minimizeRegisters = minimizeRegisters;
        this.ssaMeth = ssaMethod;
        this.interference = LivenessAnalyzer.constructInterferenceGraph(ssaMethod);
    }

    private RopMethod convert() {
        FirstFitLocalCombiningAllocator allocator = new FirstFitLocalCombiningAllocator(this.ssaMeth, this.interference, this.minimizeRegisters);
        RegisterMapper mapper = ((RegisterAllocator)allocator).allocateRegisters();
        this.ssaMeth.setBackMode();
        this.ssaMeth.mapRegisters(mapper);
        this.removePhiFunctions();
        if (((RegisterAllocator)allocator).wantsParamsMovedHigh()) {
            this.moveParametersToHighRegisters();
        }
        this.removeEmptyGotos();
        RopMethod ropMethod = new RopMethod(this.convertBasicBlocks(), this.ssaMeth.blockIndexToRopLabel(this.ssaMeth.getEntryBlockIndex()));
        ropMethod = new IdenticalBlockCombiner(ropMethod).process();
        return ropMethod;
    }

    private void removeEmptyGotos() {
        final ArrayList<SsaBasicBlock> blocks = this.ssaMeth.getBlocks();
        this.ssaMeth.forEachBlockDepthFirst(false, new SsaBasicBlock.Visitor(){

            @Override
            public void visitBlock(SsaBasicBlock b, SsaBasicBlock parent) {
                ArrayList<SsaInsn> insns = b.getInsns();
                if (insns.size() == 1 && insns.get(0).getOpcode() == Rops.GOTO) {
                    BitSet preds = (BitSet)b.getPredecessors().clone();
                    int i = preds.nextSetBit(0);
                    while (i >= 0) {
                        SsaBasicBlock pb = (SsaBasicBlock)blocks.get(i);
                        pb.replaceSuccessor(b.getIndex(), b.getPrimarySuccessorIndex());
                        i = preds.nextSetBit(i + 1);
                    }
                }
            }
        });
    }

    private void removePhiFunctions() {
        ArrayList<SsaBasicBlock> blocks = this.ssaMeth.getBlocks();
        for (SsaBasicBlock block : blocks) {
            block.forEachPhiInsn(new PhiVisitor(blocks));
            block.removeAllPhiInsns();
        }
        for (SsaBasicBlock block : blocks) {
            block.scheduleMovesFromPhis();
        }
    }

    private void moveParametersToHighRegisters() {
        int paramWidth = this.ssaMeth.getParamWidth();
        BasicRegisterMapper mapper = new BasicRegisterMapper(this.ssaMeth.getRegCount());
        int regCount = this.ssaMeth.getRegCount();
        for (int i = 0; i < regCount; ++i) {
            if (i < paramWidth) {
                mapper.addMapping(i, regCount - paramWidth + i, 1);
                continue;
            }
            mapper.addMapping(i, i - paramWidth, 1);
        }
        this.ssaMeth.mapRegisters(mapper);
    }

    private BasicBlockList convertBasicBlocks() {
        ArrayList<SsaBasicBlock> blocks = this.ssaMeth.getBlocks();
        SsaBasicBlock exitBlock = this.ssaMeth.getExitBlock();
        BitSet reachable = this.ssaMeth.computeReachability();
        int ropBlockCount = reachable.cardinality();
        if (exitBlock != null && reachable.get(exitBlock.getIndex())) {
            --ropBlockCount;
        }
        BasicBlockList result = new BasicBlockList(ropBlockCount);
        int ropBlockIndex = 0;
        for (SsaBasicBlock b : blocks) {
            if (!reachable.get(b.getIndex()) || b == exitBlock) continue;
            result.set(ropBlockIndex++, this.convertBasicBlock(b));
        }
        if (exitBlock != null && !exitBlock.getInsns().isEmpty()) {
            throw new RuntimeException("Exit block must have no insns when leaving SSA form");
        }
        return result;
    }

    private void verifyValidExitPredecessor(SsaBasicBlock b) {
        ArrayList<SsaInsn> insns = b.getInsns();
        SsaInsn lastInsn = insns.get(insns.size() - 1);
        Rop opcode = lastInsn.getOpcode();
        if (opcode.getBranchingness() != 2 && opcode != Rops.THROW) {
            throw new RuntimeException("Exit predecessor must end in valid exit statement.");
        }
    }

    private BasicBlock convertBasicBlock(SsaBasicBlock block) {
        int exitRopLabel;
        IntList successorList = block.getRopLabelSuccessorList();
        int primarySuccessorLabel = block.getPrimarySuccessorRopLabel();
        SsaBasicBlock exitBlock = this.ssaMeth.getExitBlock();
        int n = exitRopLabel = exitBlock == null ? -1 : exitBlock.getRopLabel();
        if (successorList.contains(exitRopLabel)) {
            if (successorList.size() > 1) {
                throw new RuntimeException("Exit predecessor must have no other successors" + Hex.u2(block.getRopLabel()));
            }
            successorList = IntList.EMPTY;
            primarySuccessorLabel = -1;
            this.verifyValidExitPredecessor(block);
        }
        successorList.setImmutable();
        BasicBlock result = new BasicBlock(block.getRopLabel(), this.convertInsns(block.getInsns()), successorList, primarySuccessorLabel);
        return result;
    }

    private InsnList convertInsns(ArrayList<SsaInsn> ssaInsns) {
        int insnCount = ssaInsns.size();
        InsnList result = new InsnList(insnCount);
        for (int i = 0; i < insnCount; ++i) {
            result.set(i, ssaInsns.get(i).toRopInsn());
        }
        result.setImmutable();
        return result;
    }

    public int[] getRegistersByFrequency() {
        int regCount = this.ssaMeth.getRegCount();
        Integer[] ret = new Integer[regCount];
        for (int i = 0; i < regCount; ++i) {
            ret[i] = i;
        }
        Arrays.sort(ret, new Comparator<Integer>(){

            @Override
            public int compare(Integer o1, Integer o2) {
                return SsaToRop.this.ssaMeth.getUseListForRegister(o2).size() - SsaToRop.this.ssaMeth.getUseListForRegister(o1).size();
            }
        });
        int[] result = new int[regCount];
        for (int i = 0; i < regCount; ++i) {
            result[i] = ret[i];
        }
        return result;
    }

    public static class PhiVisitor implements PhiInsn.Visitor {
        private final ArrayList<SsaBasicBlock> blocks;

        public PhiVisitor(ArrayList<SsaBasicBlock> blocks) {
            this.blocks = blocks;
        }

        @Override
        public void visitPhiInsn(PhiInsn insn) {
            RegisterSpecList sources = insn.getSources();
            RegisterSpec result = insn.getResult();
            int sz = sources.size();
            for (int i = 0; i < sz; ++i) {
                RegisterSpec source = sources.get(i);
                SsaBasicBlock predBlock = this.blocks.get(insn.predBlockIndexForSourcesIndex(i));
                predBlock.addMoveToEnd(result, source);
            }
        }
    }
}

