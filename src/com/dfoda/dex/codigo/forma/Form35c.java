package com.dfoda.dex.codigo.forma;

import java.util.BitSet;
import com.dfoda.dex.codigo.InsnFormat;
import com.dfoda.dex.codigo.DalvInsn;
import com.dfoda.otimizadores.rop.codigo.RegisterSpecList;
import com.dfoda.dex.codigo.CstInsn;
import com.dfoda.otimizadores.rop.cst.Constant;
import com.dfoda.otimizadores.rop.cst.CstType;
import com.dfoda.otimizadores.rop.cst.CstMethodRef;
import com.dfoda.otimizadores.rop.cst.CstCallSiteRef;
import com.dfoda.otimizadores.rop.codigo.RegisterSpec;
import com.dfoda.util.AnnotatedOutput;
import com.dfoda.otimizadores.rop.tipo.Type;

public final class Form35c extends InsnFormat {
    public static final InsnFormat THE_ONE = new Form35c();

    @Override
    public String insnArgString(DalvInsn insn) {
        RegisterSpecList regs = Form35c.explicitize(insn.getRegisters());
        return Form35c.regListString(regs) + ", " + insn.cstString();
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
        if (!Form35c.unsignedFitsInShort(cpi)) {
            return false;
        }
        Constant cst = ci.getConstant();
        if (!(cst instanceof CstMethodRef || cst instanceof CstType || cst instanceof CstCallSiteRef)) {
            return false;
        }
        RegisterSpecList regs = ci.getRegisters();
        return Form35c.wordCount(regs) >= 0;
    }

    @Override
    public BitSet compatibleRegs(DalvInsn insn) {
        RegisterSpecList regs = insn.getRegisters();
        int sz = regs.size();
        BitSet bits = new BitSet(sz);
        for (int i = 0; i < sz; ++i) {
            RegisterSpec reg = regs.get(i);
            bits.set(i, Form35c.unsignedFitsInNibble(reg.getReg() + reg.getCategory() - 1));
        }
        return bits;
    }

    @Override
    public void writeTo(AnnotatedOutput out, DalvInsn insn) {
        int cpi = ((CstInsn)insn).getIndex();
        RegisterSpecList regs = Form35c.explicitize(insn.getRegisters());
        int sz = regs.size();
        int r0 = sz > 0 ? regs.get(0).getReg() : 0;
        int r1 = sz > 1 ? regs.get(1).getReg() : 0;
        int r2 = sz > 2 ? regs.get(2).getReg() : 0;
        int r3 = sz > 3 ? regs.get(3).getReg() : 0;
        int r4 = sz > 4 ? regs.get(4).getReg() : 0;
        Form35c.write(out, Form35c.opcodeUnit(insn, Form35c.makeByte(r4, sz)), (short)cpi, Form35c.codeUnit(r0, r1, r2, r3));
    }

    private static int wordCount(RegisterSpecList regs) {
        int sz = regs.size();
        if (sz > 5) {
            return -1;
        }
        int result = 0;
        for (int i = 0; i < sz; ++i) {
            RegisterSpec one = regs.get(i);
            result += one.getCategory();
            if (Form35c.unsignedFitsInNibble(one.getReg() + one.getCategory() - 1)) continue;
            return -1;
        }
        return result <= 5 ? result : -1;
    }

    private static RegisterSpecList explicitize(RegisterSpecList orig) {
        int sz;
        int wordCount = Form35c.wordCount(orig);
        if (wordCount == (sz = orig.size())) {
            return orig;
        }
        RegisterSpecList result = new RegisterSpecList(wordCount);
        int wordAt = 0;
        for (int i = 0; i < sz; ++i) {
            RegisterSpec one = orig.get(i);
            result.set(wordAt, one);
            if (one.getCategory() == 2) {
                result.set(wordAt + 1, RegisterSpec.make(one.getReg() + 1, Type.VOID));
                wordAt += 2;
                continue;
            }
            ++wordAt;
        }
        result.setImmutable();
        return result;
    }
}

