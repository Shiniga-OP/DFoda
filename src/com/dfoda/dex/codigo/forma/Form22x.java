package com.dfoda.dex.codigo.forma;

import com.dfoda.dex.codigo.InsnFormat;
import com.dfoda.dex.codigo.DalvInsn;
import com.dfoda.otimizadores.rop.codigo.RegisterSpecList;
import com.dfoda.dex.codigo.SimpleInsn;
import java.util.BitSet;
import com.dfoda.util.AnnotatedOutput;

public final class Form22x extends InsnFormat {
    public static final InsnFormat THE_ONE = new Form22x();

    @Override
    public String insnArgString(DalvInsn insn) {
        RegisterSpecList regs = insn.getRegisters();
        return regs.get(0).regString() + ", " + regs.get(1).regString();
    }

    @Override
    public String insnCommentString(DalvInsn insn, boolean noteIndices) {
        return "";
    }

    @Override
    public int codeSize() {
        return 2;
    }

    @Override
    public boolean isCompatible(DalvInsn insn) {
        RegisterSpecList regs = insn.getRegisters();
        return insn instanceof SimpleInsn && regs.size() == 2 && Form22x.unsignedFitsInByte(regs.get(0).getReg()) && Form22x.unsignedFitsInShort(regs.get(1).getReg());
    }

    @Override
    public BitSet compatibleRegs(DalvInsn insn) {
        RegisterSpecList regs = insn.getRegisters();
        BitSet bits = new BitSet(2);
        bits.set(0, Form22x.unsignedFitsInByte(regs.get(0).getReg()));
        bits.set(1, Form22x.unsignedFitsInShort(regs.get(1).getReg()));
        return bits;
    }

    @Override
    public void writeTo(AnnotatedOutput out, DalvInsn insn) {
        RegisterSpecList regs = insn.getRegisters();
        Form22x.write(out, Form22x.opcodeUnit(insn, regs.get(0).getReg()), (short)regs.get(1).getReg());
    }
}

