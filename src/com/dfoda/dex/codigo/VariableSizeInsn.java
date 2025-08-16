package com.dfoda.dex.codigo;

import com.dfoda.otimizadores.rop.codigo.RegisterSpecList;
import com.dfoda.otimizadores.rop.codigo.SourcePosition;

public abstract class VariableSizeInsn
extends DalvInsn {
    public VariableSizeInsn(SourcePosition position, RegisterSpecList registers) {
        super(Dops.SPECIAL_FORMAT, position, registers);
    }

    @Override
    public final DalvInsn withOpcode(Dop opcode) {
        throw new RuntimeException("unsupported");
    }

    @Override
    public final DalvInsn withRegisterOffset(int delta) {
        return this.withRegisters(this.getRegisters().withOffset(delta));
    }
}

