package com.dfoda.dex.codigo.forma;
import com.dfoda.dex.codigo.InsnFormat;
import com.dfoda.dex.codigo.DalvInsn;
import com.dfoda.dex.codigo.TargetInsn;
import com.dfoda.util.AnnotatedOutput;

public final class Form20t extends InsnFormat {
    public static final InsnFormat THE_ONE = new Form20t();

    private Form20t() {
    }

    @Override
    public String insnArgString(DalvInsn insn) {
        return Form20t.branchString(insn);
    }

    @Override
    public String insnCommentString(DalvInsn insn, boolean noteIndices) {
        return Form20t.branchComment(insn);
    }

    @Override
    public int codeSize() {
        return 2;
    }

    @Override
    public boolean isCompatible(DalvInsn insn) {
        if (!(insn instanceof TargetInsn) || insn.getRegisters().size() != 0) {
            return false;
        }
        TargetInsn ti = (TargetInsn)insn;
        return ti.hasTargetOffset() ? this.branchFits(ti) : true;
    }

    @Override
    public boolean branchFits(TargetInsn insn) {
        int offset = insn.getTargetOffset();
        return offset != 0 && Form20t.signedFitsInShort(offset);
    }

    @Override
    public void writeTo(AnnotatedOutput out, DalvInsn insn) {
        int offset = ((TargetInsn)insn).getTargetOffset();
        Form20t.write(out, Form20t.opcodeUnit(insn, 0), (short)offset);
    }
}

