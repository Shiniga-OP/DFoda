package com.dfoda.otimizadores.rop.codigo;

import com.dfoda.util.Bits;
import com.dfoda.util.IntList;

public final class LocalVariableExtractor {
    private final RopMethod method;
    private final BasicBlockList blocks;
    private final LocalVariableInfo resultInfo;
    private final int[] workSet;

    public static LocalVariableInfo extract(RopMethod method) {
        LocalVariableExtractor lve = new LocalVariableExtractor(method);
        return lve.doit();
    }

    private LocalVariableExtractor(RopMethod method) {
        if (method == null) {
            throw new NullPointerException("method == null");
        }
        BasicBlockList blocks = method.getBlocks();
        int maxLabel = blocks.getMaxLabel();
        this.method = method;
        this.blocks = blocks;
        this.resultInfo = new LocalVariableInfo(method);
        this.workSet = Bits.makeBitSet(maxLabel);
    }

    private LocalVariableInfo doit() {
        int label = this.method.getFirstLabel();
        while (label >= 0) {
            Bits.clear(this.workSet, label);
            this.processBlock(label);
            label = Bits.findFirst(this.workSet, 0);
        }
        this.resultInfo.setImmutable();
        return this.resultInfo;
    }

    private void processBlock(int label) {
        RegisterSpecSet primaryState = this.resultInfo.mutableCopyOfStarts(label);
        BasicBlock block = this.blocks.labelToBlock(label);
        InsnList insns = block.getInsns();
        int insnSz = insns.size();
        boolean canThrowDuringLastInsn = block.hasExceptionHandlers() && insns.getLast().getResult() != null;
        int freezeSecondaryStateAt = insnSz - 1;
        RegisterSpecSet secondaryState = primaryState;
        for (int i = 0; i < insnSz; ++i) {
            RegisterSpec already;
            Insn insn;
            RegisterSpec result;
            if (canThrowDuringLastInsn && i == freezeSecondaryStateAt) {
                primaryState.setImmutable();
                primaryState = primaryState.mutableCopy();
            }
            if ((result = (insn = insns.get(i)).getLocalAssignment()) == null) {
                result = insn.getResult();
                if (result == null || primaryState.get(result.getReg()) == null) continue;
                primaryState.remove(primaryState.get(result.getReg()));
                continue;
            }
            if ((result = result.withSimpleType()).equals(already = primaryState.get(result))) continue;
            RegisterSpec previous = primaryState.localItemToSpec(result.getLocalItem());
            if (previous != null && previous.getReg() != result.getReg()) {
                primaryState.remove(previous);
            }
            this.resultInfo.addAssignment(insn, result);
            primaryState.put(result);
        }
        primaryState.setImmutable();
        IntList successors = block.getSuccessors();
        int succSz = successors.size();
        int primarySuccessor = block.getPrimarySuccessor();
        for (int i = 0; i < succSz; ++i) {
            RegisterSpecSet state;
            int succ = successors.get(i);
            state = succ == primarySuccessor ? primaryState : secondaryState;
            if (!this.resultInfo.mergeStarts(succ, state)) continue;
            Bits.set(this.workSet, succ);
        }
    }
}

