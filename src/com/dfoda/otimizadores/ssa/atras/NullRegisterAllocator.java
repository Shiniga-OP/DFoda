package com.dfoda.otimizadores.ssa.atras;

import com.dfoda.otimizadores.ssa.RegisterMapper;
import com.dfoda.otimizadores.ssa.SsaMethod;
import com.dfoda.otimizadores.ssa.BasicRegisterMapper;

public class NullRegisterAllocator extends RegisterAllocator {
    public NullRegisterAllocator(SsaMethod ssaMeth, InterferenceGraph interference) {
        super(ssaMeth, interference);
    }

    @Override
    public boolean wantsParamsMovedHigh() {
        return false;
    }

    @Override
    public RegisterMapper allocateRegisters() {
        int oldRegCount = this.ssaMeth.getRegCount();
        BasicRegisterMapper mapper = new BasicRegisterMapper(oldRegCount);
        for (int i = 0; i < oldRegCount; ++i) {
            mapper.addMapping(i, i * 2, 2);
        }
        return mapper;
    }
}

