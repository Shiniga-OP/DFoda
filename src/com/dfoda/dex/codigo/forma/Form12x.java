package com.dfoda.dex.codigo.forma;

import java.util.BitSet;
import com.dfoda.dex.codigo.InsnFormat;
import com.dfoda.dex.codigo.DalvInsn;
import com.dfoda.otimizadores.rop.codigo.RegisterSpecList;
import com.dfoda.otimizadores.rop.codigo.RegisterSpec;
import com.dfoda.dex.codigo.SimpleInsn;
import com.dfoda.util.AnnotatedOutput;

public final class Form12x extends InsnFormat {
    public static final InsnFormat THE_ONE = new Form12x();

    @Override
    public String insnArgString(DalvInsn insn) {
        RegisterSpecList regs = insn.getRegisters();
        int sz = regs.size();
        return regs.get(sz - 2).regString() + ", " + regs.get(sz - 1).regString();
    }

    @Override
    public String insnCommentString(DalvInsn insn, boolean noteIndices) {
        return "";
    }

    @Override
    public int codeSize() {
        return 1;
    }

    @Override
    public boolean isCompatible(DalvInsn insn) {
        RegisterSpec rs2;
        RegisterSpec rs1;
        if (!(insn instanceof SimpleInsn)) {
            return false;
        }
        RegisterSpecList regs = insn.getRegisters();
        switch (regs.size()) {
            case 2: {
                rs1 = regs.get(0);
                rs2 = regs.get(1);
                break;
            }
            case 3: {
                rs1 = regs.get(1);
                rs2 = regs.get(2);
                if (rs1.getReg() == regs.get(0).getReg()) break;
                return false;
            }
            default: {
                return false;
            }
        }
        return Form12x.unsignedFitsInNibble(rs1.getReg()) && Form12x.unsignedFitsInNibble(rs2.getReg());
    }

    @Override
    public BitSet compatibleRegs(DalvInsn insn) {
        RegisterSpecList regs = insn.getRegisters();
        BitSet bits = new BitSet(2);
        int r0 = regs.get(0).getReg();
        int r1 = regs.get(1).getReg();
        switch (regs.size()) {
            case 2: {
                bits.set(0, Form12x.unsignedFitsInNibble(r0));
                bits.set(1, Form12x.unsignedFitsInNibble(r1));
                break;
            }
            case 3: {
                if (r0 != r1) {
                    bits.set(0, false);
                    bits.set(1, false);
                } else {
                    boolean dstRegComp = Form12x.unsignedFitsInNibble(r1);
                    bits.set(0, dstRegComp);
                    bits.set(1, dstRegComp);
                }
                bits.set(2, Form12x.unsignedFitsInNibble(regs.get(2).getReg()));
                break;
            }
            default: {
                throw new AssertionError();
            }
        }
        return bits;
    }

    @Override
    public void writeTo(AnnotatedOutput out, DalvInsn insn) {
        RegisterSpecList regs = insn.getRegisters();
        int sz = regs.size();
        Form12x.write(out, Form12x.opcodeUnit(insn, Form12x.makeByte(regs.get(sz - 2).getReg(), regs.get(sz - 1).getReg())));
    }
}

