package com.dfoda.dex.codigo;

import com.dfoda.otimizadores.rop.codigo.SourcePosition;
import com.dfoda.otimizadores.rop.codigo.RegisterSpecList;

public final class SimpleInsn extends FixedSizeInsn {
    public SimpleInsn(Dop opcode, SourcePosition position, RegisterSpecList registers) {
        super(opcode, position, registers);
    }

    @Override
    public DalvInsn withOpcode(Dop opcode) {
        return new SimpleInsn(opcode, this.getPosition(), this.getRegisters());
    }

    @Override
    public DalvInsn withRegisters(RegisterSpecList registers) {
        return new SimpleInsn(this.getOpcode(), this.getPosition(), registers);
    }

    @Override
    protected String argString() {
        return null;
    }
}

