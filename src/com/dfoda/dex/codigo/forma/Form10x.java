package com.dfoda.dex.codigo.forma;

import com.dfoda.dex.codigo.InsnFormat;
import com.dfoda.dex.codigo.DalvInsn;
import com.dfoda.dex.codigo.SimpleInsn;
import com.dfoda.util.AnnotatedOutput;

public final class Form10x extends InsnFormat {
    public static final InsnFormat THE_ONE = new Form10x();

    @Override
    public String insnArgString(DalvInsn insn) {
        return "";
    }

    @Override
    public String insnCommentString(DalvInsn insn, boolean noteIndices) {
        return "";
    }

    @Override
    public int codeSize() {
        return 1;
    }

    @Override
    public boolean isCompatible(DalvInsn insn) {
        return insn instanceof SimpleInsn && insn.getRegisters().size() == 0;
    }

    @Override
    public void writeTo(AnnotatedOutput out, DalvInsn insn) {
        Form10x.write(out, Form10x.opcodeUnit(insn, 0));
    }
}

