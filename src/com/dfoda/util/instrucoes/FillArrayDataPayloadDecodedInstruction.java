package com.dfoda.util.instrucoes;

public final class FillArrayDataPayloadDecodedInstruction extends DecodedInstruction {
    private final Object data;
    private final int size;
    private final int elementWidth;

    private FillArrayDataPayloadDecodedInstruction(InstructionCodec format, int opcode, Object data, int size, int elementWidth) {
        super(format, opcode, 0, null, 0, 0L);
        this.data = data;
        this.size = size;
        this.elementWidth = elementWidth;
    }

    public FillArrayDataPayloadDecodedInstruction(InstructionCodec format, int opcode, byte[] data) {
        this(format, opcode, data, data.length, 1);
    }

    public FillArrayDataPayloadDecodedInstruction(InstructionCodec format, int opcode, short[] data) {
        this(format, opcode, data, data.length, 2);
    }

    public FillArrayDataPayloadDecodedInstruction(InstructionCodec format, int opcode, int[] data) {
        this(format, opcode, data, data.length, 4);
    }

    public FillArrayDataPayloadDecodedInstruction(InstructionCodec format, int opcode, long[] data) {
        this(format, opcode, data, data.length, 8);
    }

    @Override
    public int getRegisterCount() {
        return 0;
    }

    public short getElementWidthUnit() {
        return (short)this.elementWidth;
    }

    public int getSize() {
        return this.size;
    }

    public Object getData() {
        return this.data;
    }

    @Override
    public DecodedInstruction withIndex(int newIndex) {
        throw new UnsupportedOperationException("no index in instruction");
    }
}

