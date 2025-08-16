package com.dfoda.util.instrucoes;

import com.dfoda.util.IndexType;

public final class TwoRegisterDecodedInstruction extends DecodedInstruction {
    public final int a;
    public final int b;

    public TwoRegisterDecodedInstruction(InstructionCodec format, int opcode, int index, IndexType indexType, int target, long literal, int a, int b) {
        super(format, opcode, index, indexType, target, literal);
        this.a = a;
        this.b = b;
    }

    @Override
    public int getRegisterCount() {
        return 2;
    }

    @Override
    public int getA() {
        return this.a;
    }

    @Override
    public int getB() {
        return this.b;
    }

    @Override
    public DecodedInstruction withIndex(int newIndex) {
        return new TwoRegisterDecodedInstruction(this.getFormat(), this.getOpcode(), newIndex, this.getIndexType(), this.getTarget(), this.getLiteral(), this.a, this.b);
    }
}

