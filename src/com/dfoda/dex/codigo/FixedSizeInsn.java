package com.dfoda.dex.codigo;

import com.dfoda.otimizadores.rop.codigo.RegisterSpecList;
import com.dfoda.otimizadores.rop.codigo.SourcePosition;
import com.dfoda.util.AnnotatedOutput;

public abstract class FixedSizeInsn extends DalvInsn {
    public FixedSizeInsn(Dop opcode, SourcePosition position, RegisterSpecList registers) {
        super(opcode, position, registers);
    }

    @Override
    public final int codeSize() {
        return this.getOpcode().getFormat().codeSize();
    }

    @Override
    public final void writeTo(AnnotatedOutput out) {
        this.getOpcode().getFormat().writeTo(out, this);
    }

    @Override
    public final DalvInsn withRegisterOffset(int delta) {
        return this.withRegisters(this.getRegisters().withOffset(delta));
    }

    @Override
    protected final String listingString0(boolean noteIndices) {
        return this.getOpcode().getFormat().listingString(this, noteIndices);
    }
}

