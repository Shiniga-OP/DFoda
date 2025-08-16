package com.dfoda.dex.codigo.forma;

import java.util.BitSet;
import com.dfoda.dex.codigo.InsnFormat;
import com.dfoda.otimizadores.rop.codigo.RegisterSpecList;
import com.dfoda.dex.codigo.DalvInsn;
import com.dfoda.otimizadores.rop.cst.CstLiteralBits;
import com.dfoda.dex.codigo.CstInsn;
import com.dfoda.otimizadores.rop.cst.Constant;
import com.dfoda.otimizadores.rop.cst.CstLiteral64;
import com.dfoda.util.AnnotatedOutput;

public final class Form51l extends InsnFormat {
    public static final InsnFormat THE_ONE = new Form51l();

    @Override
    public String insnArgString(DalvInsn insn) {
        RegisterSpecList regs = insn.getRegisters();
        CstLiteralBits value = (CstLiteralBits)((CstInsn)insn).getConstant();
        return regs.get(0).regString() + ", " + Form51l.literalBitsString(value);
    }

    @Override
    public String insnCommentString(DalvInsn insn, boolean noteIndices) {
        CstLiteralBits value = (CstLiteralBits)((CstInsn)insn).getConstant();
        return Form51l.literalBitsComment(value, 64);
    }

    @Override
    public int codeSize() {
        return 5;
    }

    @Override
    public boolean isCompatible(DalvInsn insn) {
        RegisterSpecList regs = insn.getRegisters();
        if (!(insn instanceof CstInsn) || regs.size() != 1 || !Form51l.unsignedFitsInByte(regs.get(0).getReg())) {
            return false;
        }
        CstInsn ci = (CstInsn)insn;
        Constant cst = ci.getConstant();
        return cst instanceof CstLiteral64;
    }

    @Override
    public BitSet compatibleRegs(DalvInsn insn) {
        RegisterSpecList regs = insn.getRegisters();
        BitSet bits = new BitSet(1);
        bits.set(0, Form51l.unsignedFitsInByte(regs.get(0).getReg()));
        return bits;
    }

    @Override
    public void writeTo(AnnotatedOutput out, DalvInsn insn) {
        RegisterSpecList regs = insn.getRegisters();
        long value = ((CstLiteral64)((CstInsn)insn).getConstant()).getLongBits();
        Form51l.write(out, Form51l.opcodeUnit(insn, regs.get(0).getReg()), value);
    }
}

