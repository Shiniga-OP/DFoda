package com.dfoda.dexer.mostrar;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.EnumSet;
import com.dfoda.util.ByteArray;
import com.dfoda.otimizadores.ssa.Optimizer;
import com.dfoda.otimizadores.ssa.SsaBasicBlock;
import com.dfoda.util.Hex;
import com.dfoda.util.IntList;
import com.dfoda.otimizadores.rop.codigo.AccessFlags;
import com.dfoda.otimizadores.rop.codigo.RopMethod;
import com.dfoda.otimizadores.ssa.SsaMethod;
import com.dfoda.otimizadores.rop.codigo.DexTranslationAdvice;
import com.dfoda.otimizadores.ca.inter.Member;
import com.dfoda.otimizadores.ca.inter.Method;
import com.dfoda.otimizadores.ca.codigo.ConcreteMethod;
import com.dfoda.otimizadores.ssa.SsaInsn;
import com.dfoda.otimizadores.ca.codigo.Ropper;

public class SsaDumper extends BlockDumper {
    public static void dump(byte[] bytes, PrintStream out, String filePath, Args args) {
        SsaDumper sd = new SsaDumper(bytes, out, filePath, args);
        sd.dump();
    }

    private SsaDumper(byte[] bytes, PrintStream out, String filePath, Args args) {
        super(bytes, out, filePath, true, args);
    }

    @Override
    public void endParsingMember(ByteArray bytes, int offset, String name, String descriptor, Member member) {
        if (!(member instanceof Method)) {
            return;
        }
        if (!this.shouldDumpMethod(name)) {
            return;
        }
        if ((member.getAccessFlags() & 0x500) != 0) {
            return;
        }
        ConcreteMethod meth = new ConcreteMethod((Method)member, this.classFile, true, true);
        DexTranslationAdvice advice = DexTranslationAdvice.THE_ONE;
        RopMethod rmeth = Ropper.convert(meth, advice, this.classFile.getMethods(), this.dexOptions);
        SsaMethod ssaMeth = null;
        boolean isStatic = AccessFlags.isStatic(meth.getAccessFlags());
        int paramWidth = SsaDumper.computeParamWidth(meth, isStatic);
        if (this.args.passoSsa == null) {
            ssaMeth = Optimizer.debugNoRegisterAllocation(rmeth, paramWidth, isStatic, true, advice, EnumSet.allOf(Optimizer.OptionalStep.class));
        } else if ("edge-split".equals(this.args.passoSsa)) {
            ssaMeth = Optimizer.debugEdgeSplit(rmeth, paramWidth, isStatic, true, advice);
        } else if ("phi-placement".equals(this.args.passoSsa)) {
            ssaMeth = Optimizer.debugPhiPlacement(rmeth, paramWidth, isStatic, true, advice);
        } else if ("renaming".equals(this.args.passoSsa)) {
            ssaMeth = Optimizer.debugRenaming(rmeth, paramWidth, isStatic, true, advice);
        } else if ("dead-code".equals(this.args.passoSsa)) {
            ssaMeth = Optimizer.debugDeadCodeRemover(rmeth, paramWidth, isStatic, true, advice);
        }
        StringBuilder sb = new StringBuilder(2000);
        sb.append("first ");
        sb.append(Hex.u2(ssaMeth.blockIndexToRopLabel(ssaMeth.getEntryBlockIndex())));
        sb.append('\n');
        ArrayList<SsaBasicBlock> blocks = ssaMeth.getBlocks();
        ArrayList<SsaBasicBlock> sortedBlocks = (ArrayList<SsaBasicBlock>) blocks.clone();
        Collections.sort(sortedBlocks, SsaBasicBlock.LABEL_COMPARATOR);
        for (SsaBasicBlock block : sortedBlocks) {
            sb.append("block ").append(Hex.u2(block.getRopLabel())).append('\n');
            BitSet preds = block.getPredecessors();
            int i = preds.nextSetBit(0);
            while (i >= 0) {
                sb.append("  pred ");
                sb.append(Hex.u2(ssaMeth.blockIndexToRopLabel(i)));
                sb.append('\n');
                i = preds.nextSetBit(i + 1);
            }
            sb.append("  live in:" + block.getLiveInRegs());
            sb.append("\n");
            for (SsaInsn insn : block.getInsns()) {
                sb.append("  ");
                sb.append(insn.toHuman());
                sb.append('\n');
            }
            if (block.getSuccessors().cardinality() == 0) {
                sb.append("  returns\n");
            } else {
                int primary = block.getPrimarySuccessorRopLabel();
                IntList succLabelList = block.getRopLabelSuccessorList();
                int szSuccLabels = succLabelList.size();
                for (int i2 = 0; i2 < szSuccLabels; ++i2) {
                    sb.append("  next ");
                    sb.append(Hex.u2(succLabelList.get(i2)));
                    if (szSuccLabels != 1 && primary == succLabelList.get(i2)) {
                        sb.append(" *");
                    }
                    sb.append('\n');
                }
            }
            sb.append("  live out:" + block.getLiveOutRegs());
            sb.append("\n");
        }
        this.suppressDump = false;
        this.parsed(bytes, 0, bytes.size(), sb.toString());
        this.suppressDump = true;
    }
}

