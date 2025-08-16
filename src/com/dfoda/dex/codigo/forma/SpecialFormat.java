package com.dfoda.dex.codigo.forma;

import com.dfoda.dex.codigo.InsnFormat;
import com.dfoda.dex.codigo.DalvInsn;
import com.dfoda.util.AnnotatedOutput;

public final class SpecialFormat extends InsnFormat {
    public static final InsnFormat THE_ONE = new SpecialFormat();

    @Override
    public String insnArgString(DalvInsn insn) {
        throw new RuntimeException("unsupported");
    }

    @Override
    public String insnCommentString(DalvInsn insn, boolean noteIndices) {
        throw new RuntimeException("unsupported");
    }

    @Override
    public int codeSize() {
        throw new RuntimeException("unsupported");
    }

    @Override
    public boolean isCompatible(DalvInsn insn) {
        return true;
    }

    @Override
    public void writeTo(AnnotatedOutput out, DalvInsn insn) {
        throw new RuntimeException("unsupported");
    }
}

