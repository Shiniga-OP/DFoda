package com.dfoda.dex.codigo.forma;

import com.dfoda.dex.codigo.InsnFormat;
import com.dfoda.dex.codigo.DalvInsn;
import com.dfoda.dex.codigo.CstInsn;
import com.dfoda.otimizadores.rop.cst.Constant;
import com.dfoda.otimizadores.rop.cst.CstMethodRef;
import com.dfoda.otimizadores.rop.cst.CstType;
import com.dfoda.otimizadores.rop.codigo.RegisterSpecList;
import com.dfoda.util.AnnotatedOutput;
import com.dfoda.otimizadores.rop.cst.CstCallSiteRef;

public final class Form3rc extends InsnFormat {
    public static final InsnFormat THE_ONE = new Form3rc();

    @Override
    public String insnArgString(DalvInsn insn) {
        return Form3rc.regRangeString(insn.getRegisters()) + ", " + insn.cstString();
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
        return 3;
    }

    @Override
    public boolean isCompatible(DalvInsn insn) {
        if (!(insn instanceof CstInsn)) {
            return false;
        }
        CstInsn ci = (CstInsn)insn;
        int cpi = ci.getIndex();
        Constant cst = ci.getConstant();
        if (!Form3rc.unsignedFitsInShort(cpi)) {
            return false;
        }
        if (!(cst instanceof CstMethodRef || cst instanceof CstType || cst instanceof CstCallSiteRef)) {
            return false;
        }
        RegisterSpecList regs = ci.getRegisters();
        int sz = regs.size();
        return regs.size() == 0 || Form3rc.isRegListSequential(regs) && Form3rc.unsignedFitsInShort(regs.get(0).getReg()) && Form3rc.unsignedFitsInByte(regs.getWordCount());
    }

    @Override
    public void writeTo(AnnotatedOutput out, DalvInsn insn) {
        RegisterSpecList regs = insn.getRegisters();
        int cpi = ((CstInsn)insn).getIndex();
        int firstReg = regs.size() == 0 ? 0 : regs.get(0).getReg();
        int count = regs.getWordCount();
        Form3rc.write(out, Form3rc.opcodeUnit(insn, count), (short)cpi, (short)firstReg);
    }
}

