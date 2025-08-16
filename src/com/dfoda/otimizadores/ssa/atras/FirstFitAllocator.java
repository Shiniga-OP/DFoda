package com.dfoda.otimizadores.ssa.atras;

import java.util.BitSet;
import com.dfoda.otimizadores.ssa.RegisterMapper;
import com.dfoda.util.BitIntSet;
import com.dfoda.otimizadores.rop.codigo.CstInsn;
import com.dfoda.otimizadores.rop.cst.CstInteger;
import com.dfoda.otimizadores.ssa.BasicRegisterMapper;
import com.dfoda.otimizadores.ssa.SsaMethod;
import com.dfoda.otimizadores.ssa.NormalSsaInsn;

public class FirstFitAllocator extends RegisterAllocator {
    private static final boolean PRESLOT_PARAMS = true;
    private final BitSet mapped;

    public FirstFitAllocator(SsaMethod ssaMeth, InterferenceGraph interference) {
        super(ssaMeth, interference);
        this.mapped = new BitSet(ssaMeth.getRegCount());
    }

    @Override
    public boolean wantsParamsMovedHigh() {
        return true;
    }

    @Override
    public RegisterMapper allocateRegisters() {
        int oldRegCount = this.ssaMeth.getRegCount();
        BasicRegisterMapper mapper = new BasicRegisterMapper(oldRegCount);
        int nextNewRegister = 0;
        nextNewRegister = this.ssaMeth.getParamWidth();
        for (int i = 0; i < oldRegCount; ++i) {
            if (this.mapped.get(i)) continue;
            int maxCategory = this.getCategoryForSsaReg(i);
            BitIntSet current = new BitIntSet(oldRegCount);
            this.interference.mergeInterferenceSet(i, current);
            boolean isPreslotted = false;
            int newReg = 0;
            if (this.isDefinitionMoveParam(i)) {
                NormalSsaInsn defInsn = (NormalSsaInsn)this.ssaMeth.getDefinitionForRegister(i);
                newReg = this.paramNumberFromMoveParam(defInsn);
                mapper.addMapping(i, newReg, maxCategory);
                isPreslotted = true;
            } else {
                mapper.addMapping(i, nextNewRegister, maxCategory);
                newReg = nextNewRegister;
            }
            for (int j = i + 1; j < oldRegCount; ++j) {
                if (this.mapped.get(j) || this.isDefinitionMoveParam(j) || current.has(j) || isPreslotted && maxCategory < this.getCategoryForSsaReg(j)) continue;
                this.interference.mergeInterferenceSet(j, current);
                maxCategory = Math.max(maxCategory, this.getCategoryForSsaReg(j));
                mapper.addMapping(j, newReg, maxCategory);
                this.mapped.set(j);
            }
            this.mapped.set(i);
            if (isPreslotted) continue;
            nextNewRegister += maxCategory;
        }
        return mapper;
    }

    private int paramNumberFromMoveParam(NormalSsaInsn ndefInsn) {
        CstInsn origInsn = (CstInsn)ndefInsn.getOriginalRopInsn();
        return ((CstInteger)origInsn.getConstant()).getValue();
    }
}

