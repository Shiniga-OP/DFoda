package com.dfoda.dex.codigo.forma;

import java.util.BitSet;
import com.dfoda.dex.codigo.InsnFormat;
import com.dfoda.dex.codigo.DalvInsn;
import com.dfoda.otimizadores.rop.codigo.RegisterSpecList;
import com.dfoda.dex.codigo.CstInsn;
import com.dfoda.otimizadores.rop.cst.CstFieldRef;
import com.dfoda.otimizadores.rop.cst.Constant;
import com.dfoda.otimizadores.rop.cst.CstType;
import com.dfoda.util.AnnotatedOutput;

public final class Form22c extends InsnFormat {
    public static final InsnFormat THE_ONE = new Form22c();

    @Override
    public String insnArgString(DalvInsn insn) {
        RegisterSpecList regs = insn.getRegisters();
        return regs.get(0).regString() + ", " + regs.get(1).regString() + ", " + insn.cstString();
    }

    @Override
    public String insnCommentString(DalvInsn insn, boolean noteIndices) {
        if (noteIndices) {
            return insn.cstComment();
        }
        return "";
    }

    @Override
    public int codeSize() {
        return 2;
    }

    @Override
    public boolean isCompatible(DalvInsn insn) {
        RegisterSpecList regs = insn.getRegisters();
        if (!(insn instanceof CstInsn && regs.size() == 2 && Form22c.unsignedFitsInNibble(regs.get(0).getReg()) && Form22c.unsignedFitsInNibble(regs.get(1).getReg()))) {
            return false;
        }
        CstInsn ci = (CstInsn)insn;
        int cpi = ci.getIndex();
        if (!Form22c.unsignedFitsInShort(cpi)) {
            return false;
        }
        Constant cst = ci.getConstant();
        return cst instanceof CstType || cst instanceof CstFieldRef;
    }

    @Override
    public BitSet compatibleRegs(DalvInsn insn) {
        RegisterSpecList regs = insn.getRegisters();
        BitSet bits = new BitSet(2);
        bits.set(0, Form22c.unsignedFitsInNibble(regs.get(0).getReg()));
        bits.set(1, Form22c.unsignedFitsInNibble(regs.get(1).getReg()));
        return bits;
    }

    @Override
    public void writeTo(AnnotatedOutput out, DalvInsn insn) {
        RegisterSpecList regs = insn.getRegisters();
        int cpi = ((CstInsn)insn).getIndex();
        Form22c.write(out, Form22c.opcodeUnit(insn, Form22c.makeByte(regs.get(0).getReg(), regs.get(1).getReg())), (short)cpi);
    }
}

