package com.dex.util;

public final class ByteArrayByteInput implements ByteInput {
    public final byte[] bytes;
    public int position;

    public ByteArrayByteInput(byte ... bytes) {
        this.bytes = bytes;
    }

    @Override
    public byte readByte() {
        return this.bytes[this.position++];
    }
}

