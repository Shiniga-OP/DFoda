package com.dfoda.dex.codigo.forma;

import com.dfoda.dex.codigo.InsnFormat;
import com.dfoda.dex.codigo.DalvInsn;
import com.dfoda.dex.codigo.TargetInsn;
import com.dfoda.util.AnnotatedOutput;

public final class Form30t extends InsnFormat {
    public static final InsnFormat THE_ONE = new Form30t();

    @Override
    public String insnArgString(DalvInsn insn) {
        return Form30t.branchString(insn);
    }

    @Override
    public String insnCommentString(DalvInsn insn, boolean noteIndices) {
        return Form30t.branchComment(insn);
    }

    @Override
    public int codeSize() {
        return 3;
    }

    @Override
    public boolean isCompatible(DalvInsn insn) {
        return insn instanceof TargetInsn && insn.getRegisters().size() == 0;
    }

    @Override
    public boolean branchFits(TargetInsn insn) {
        return true;
    }

    @Override
    public void writeTo(AnnotatedOutput out, DalvInsn insn) {
        int offset = ((TargetInsn)insn).getTargetOffset();
        Form30t.write(out, Form30t.opcodeUnit(insn, 0), offset);
    }
}

