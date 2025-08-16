package com.dfoda.dex.codigo.forma;

import java.util.BitSet;
import com.dfoda.dex.codigo.InsnFormat;
import com.dfoda.dex.codigo.DalvInsn;
import com.dfoda.otimizadores.rop.codigo.RegisterSpecList;
import com.dfoda.dex.codigo.TargetInsn;
import com.dfoda.util.AnnotatedOutput;

public final class Form22t extends InsnFormat {
    public static final InsnFormat THE_ONE = new Form22t();

    @Override
    public String insnArgString(DalvInsn insn) {
        RegisterSpecList regs = insn.getRegisters();
        return regs.get(0).regString() + ", " + regs.get(1).regString() + ", " + Form22t.branchString(insn);
    }

    @Override
    public String insnCommentString(DalvInsn insn, boolean noteIndices) {
        return Form22t.branchComment(insn);
    }

    @Override
    public int codeSize() {
        return 2;
    }

    @Override
    public boolean isCompatible(DalvInsn insn) {
        RegisterSpecList regs = insn.getRegisters();
        if (!(insn instanceof TargetInsn && regs.size() == 2 && Form22t.unsignedFitsInNibble(regs.get(0).getReg()) && Form22t.unsignedFitsInNibble(regs.get(1).getReg()))) {
            return false;
        }
        TargetInsn ti = (TargetInsn)insn;
        return ti.hasTargetOffset() ? this.branchFits(ti) : true;
    }

    @Override
    public BitSet compatibleRegs(DalvInsn insn) {
        RegisterSpecList regs = insn.getRegisters();
        BitSet bits = new BitSet(2);
        bits.set(0, Form22t.unsignedFitsInNibble(regs.get(0).getReg()));
        bits.set(1, Form22t.unsignedFitsInNibble(regs.get(1).getReg()));
        return bits;
    }

    @Override
    public boolean branchFits(TargetInsn insn) {
        int offset = insn.getTargetOffset();
        return offset != 0 && Form22t.signedFitsInShort(offset);
    }

    @Override
    public void writeTo(AnnotatedOutput out, DalvInsn insn) {
        RegisterSpecList regs = insn.getRegisters();
        int offset = ((TargetInsn)insn).getTargetOffset();
        Form22t.write(out, Form22t.opcodeUnit(insn, Form22t.makeByte(regs.get(0).getReg(), regs.get(1).getReg())), (short)offset);
    }
}

