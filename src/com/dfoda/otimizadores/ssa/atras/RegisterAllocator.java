package com.dfoda.otimizadores.ssa.atras;

import java.util.ArrayList;
import com.dfoda.otimizadores.ssa.RegisterMapper;
import com.dfoda.otimizadores.rop.codigo.RegisterSpec;
import com.dfoda.util.IntSet;
import com.dfoda.util.IntIterator;
import com.dfoda.otimizadores.rop.codigo.RegisterSpecList;
import com.dfoda.otimizadores.ssa.SsaMethod;
import com.dfoda.otimizadores.ssa.SsaInsn;
import com.dfoda.otimizadores.ssa.NormalSsaInsn;
import com.dfoda.otimizadores.ssa.SsaBasicBlock;
import com.dfoda.otimizadores.rop.codigo.Rops;
import com.dfoda.otimizadores.rop.codigo.SourcePosition;
import com.dfoda.otimizadores.rop.codigo.PlainInsn;

public abstract class RegisterAllocator {
    protected final SsaMethod ssaMeth;
    protected final InterferenceGraph interference;

    public RegisterAllocator(SsaMethod ssaMeth, InterferenceGraph interference) {
        this.ssaMeth = ssaMeth;
        this.interference = interference;
    }

    public abstract boolean wantsParamsMovedHigh();

    public abstract RegisterMapper allocateRegisters();

    protected final int getCategoryForSsaReg(int reg) {
        SsaInsn definition = this.ssaMeth.getDefinitionForRegister(reg);
        if (definition == null) {
            return 1;
        }
        return definition.getResult().getCategory();
    }

    protected final RegisterSpec getDefinitionSpecForSsaReg(int reg) {
        SsaInsn definition = this.ssaMeth.getDefinitionForRegister(reg);
        return definition == null ? null : definition.getResult();
    }

    protected boolean isDefinitionMoveParam(int reg) {
        SsaInsn defInsn = this.ssaMeth.getDefinitionForRegister(reg);
        if (defInsn instanceof NormalSsaInsn) {
            NormalSsaInsn ndefInsn = (NormalSsaInsn)defInsn;
            return ndefInsn.getOpcode().getOpcode() == 3;
        }
        return false;
    }

    protected final RegisterSpec insertMoveBefore(SsaInsn insn, RegisterSpec reg) {
        SsaBasicBlock block = insn.getBlock();
        ArrayList<SsaInsn> insns = block.getInsns();
        int insnIndex = insns.indexOf(insn);
        if (insnIndex < 0) {
            throw new IllegalArgumentException("specified insn is not in this block");
        }
        if (insnIndex != insns.size() - 1) {
            throw new IllegalArgumentException("Adding move here not supported:" + insn.toHuman());
        }
        RegisterSpec newRegSpec = RegisterSpec.make(this.ssaMeth.makeNewSsaReg(), reg.getTypeBearer());
        SsaInsn toAdd = SsaInsn.makeFromRop(new PlainInsn(Rops.opMove(newRegSpec.getType()), SourcePosition.NO_INFO, newRegSpec, RegisterSpecList.make(reg)), block);
        insns.add(insnIndex, toAdd);
        int newReg = newRegSpec.getReg();
        IntSet liveOut = block.getLiveOutRegs();
        IntIterator liveOutIter = liveOut.iterator();
        while (liveOutIter.hasNext()) {
            this.interference.add(newReg, liveOutIter.next());
        }
        RegisterSpecList sources = insn.getSources();
        int szSources = sources.size();
        for (int i = 0; i < szSources; ++i) {
            this.interference.add(newReg, sources.get(i).getReg());
        }
        this.ssaMeth.onInsnsChanged();
        return newRegSpec;
    }
}

