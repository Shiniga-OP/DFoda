package com.dfoda.dex.codigo.forma;

import java.util.BitSet;
import com.dfoda.dex.codigo.InsnFormat;
import com.dfoda.dex.codigo.DalvInsn;
import com.dfoda.otimizadores.rop.codigo.RegisterSpecList;
import com.dfoda.dex.codigo.CstInsn;
import com.dfoda.otimizadores.rop.cst.CstLiteralBits;
import com.dfoda.otimizadores.rop.cst.Constant;
import com.dfoda.util.AnnotatedOutput;

public final class Form22b extends InsnFormat {
    public static final InsnFormat THE_ONE = new Form22b();

    @Override
    public String insnArgString(DalvInsn insn) {
        RegisterSpecList regs = insn.getRegisters();
        CstLiteralBits value = (CstLiteralBits)((CstInsn)insn).getConstant();
        return regs.get(0).regString() + ", " + regs.get(1).regString() + ", " + Form22b.literalBitsString(value);
    }

    @Override
    public String insnCommentString(DalvInsn insn, boolean noteIndices) {
        CstLiteralBits value = (CstLiteralBits)((CstInsn)insn).getConstant();
        return Form22b.literalBitsComment(value, 8);
    }

    @Override
    public int codeSize() {
        return 2;
    }

    @Override
    public boolean isCompatible(DalvInsn insn) {
        RegisterSpecList regs = insn.getRegisters();
        if (!(insn instanceof CstInsn && regs.size() == 2 && Form22b.unsignedFitsInByte(regs.get(0).getReg()) && Form22b.unsignedFitsInByte(regs.get(1).getReg()))) {
            return false;
        }
        CstInsn ci = (CstInsn)insn;
        Constant cst = ci.getConstant();
        if (!(cst instanceof CstLiteralBits)) {
            return false;
        }
        CstLiteralBits cb = (CstLiteralBits)cst;
        return cb.fitsInInt() && Form22b.signedFitsInByte(cb.getIntBits());
    }

    @Override
    public BitSet compatibleRegs(DalvInsn insn) {
        RegisterSpecList regs = insn.getRegisters();
        BitSet bits = new BitSet(2);
        bits.set(0, Form22b.unsignedFitsInByte(regs.get(0).getReg()));
        bits.set(1, Form22b.unsignedFitsInByte(regs.get(1).getReg()));
        return bits;
    }

    @Override
    public void writeTo(AnnotatedOutput out, DalvInsn insn) {
        RegisterSpecList regs = insn.getRegisters();
        int value = ((CstLiteralBits)((CstInsn)insn).getConstant()).getIntBits();
        Form22b.write(out, Form22b.opcodeUnit(insn, regs.get(0).getReg()), Form22b.codeUnit(regs.get(1).getReg(), value & 0xFF));
    }
}

