package com.dfoda.dex.codigo.forma;

import com.dfoda.dex.codigo.InsnFormat;
import com.dfoda.dex.codigo.DalvInsn;
import com.dfoda.dex.codigo.MultiCstInsn;
import com.dfoda.otimizadores.rop.cst.CstMethodRef;
import com.dfoda.otimizadores.rop.cst.Constant;
import com.dfoda.otimizadores.rop.cst.CstProtoRef;
import com.dfoda.otimizadores.rop.codigo.RegisterSpecList;
import com.dfoda.util.AnnotatedOutput;

public final class Form4rcc extends InsnFormat {
    public static final InsnFormat THE_ONE = new Form4rcc();

    @Override
    public String insnArgString(DalvInsn insn) {
        return Form4rcc.regRangeString(insn.getRegisters()) + ", " + insn.cstString();
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
        return 4;
    }

    @Override
    public boolean isCompatible(DalvInsn insn) {
        if (!(insn instanceof MultiCstInsn)) {
            return false;
        }
        MultiCstInsn mci = (MultiCstInsn)insn;
        int methodIdx = mci.getIndex(0);
        int protoIdx = mci.getIndex(1);
        if (!Form4rcc.unsignedFitsInShort(methodIdx) || !Form4rcc.unsignedFitsInShort(protoIdx)) {
            return false;
        }
        Constant methodRef = mci.getConstant(0);
        if (!(methodRef instanceof CstMethodRef)) {
            return false;
        }
        Constant protoRef = mci.getConstant(1);
        if (!(protoRef instanceof CstProtoRef)) {
            return false;
        }
        RegisterSpecList regs = mci.getRegisters();
        int sz = regs.size();
        if (sz == 0) {
            return true;
        }
        return Form4rcc.unsignedFitsInByte(regs.getWordCount()) && Form4rcc.unsignedFitsInShort(sz) && Form4rcc.unsignedFitsInShort(regs.get(0).getReg()) && Form4rcc.isRegListSequential(regs);
    }

    @Override
    public void writeTo(AnnotatedOutput out, DalvInsn insn) {
        MultiCstInsn mci = (MultiCstInsn)insn;
        short regB = (short)mci.getIndex(0);
        short regH = (short)mci.getIndex(1);
        RegisterSpecList regs = insn.getRegisters();
        short regC = 0;
        if (regs.size() > 0) {
            regC = (short)regs.get(0).getReg();
        }
        int regA = regs.getWordCount();
        Form4rcc.write(out, Form4rcc.opcodeUnit(insn, regA), regB, regC, regH);
    }
}

