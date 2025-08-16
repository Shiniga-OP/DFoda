package com.dfoda.dex.codigo.forma;

import java.util.BitSet;
import com.dfoda.dex.codigo.InsnFormat;
import com.dfoda.dex.codigo.DalvInsn;
import com.dfoda.otimizadores.rop.codigo.RegisterSpecList;
import com.dfoda.dex.codigo.TargetInsn;
import com.dfoda.util.AnnotatedOutput;

public final class Form31t extends InsnFormat {
    public static final InsnFormat THE_ONE = new Form31t();

    @Override
    public String insnArgString(DalvInsn insn) {
        RegisterSpecList regs = insn.getRegisters();
        return regs.get(0).regString() + ", " + Form31t.branchString(insn);
    }

    @Override
    public String insnCommentString(DalvInsn insn, boolean noteIndices) {
        return Form31t.branchComment(insn);
    }

    @Override
    public int codeSize() {
        return 3;
    }

    @Override
    public boolean isCompatible(DalvInsn insn) {
        RegisterSpecList regs = insn.getRegisters();
        return insn instanceof TargetInsn && regs.size() == 1 && Form31t.unsignedFitsInByte(regs.get(0).getReg());
    }

    @Override
    public BitSet compatibleRegs(DalvInsn insn) {
        RegisterSpecList regs = insn.getRegisters();
        BitSet bits = new BitSet(1);
        bits.set(0, Form31t.unsignedFitsInByte(regs.get(0).getReg()));
        return bits;
    }

    @Override
    public boolean branchFits(TargetInsn insn) {
        return true;
    }

    @Override
    public void writeTo(AnnotatedOutput out, DalvInsn insn) {
        RegisterSpecList regs = insn.getRegisters();
        int offset = ((TargetInsn)insn).getTargetOffset();
        Form31t.write(out, Form31t.opcodeUnit(insn, regs.get(0).getReg()), offset);
    }
}

