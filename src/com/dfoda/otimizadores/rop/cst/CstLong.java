package com.dfoda.otimizadores.rop.cst;

import com.dfoda.otimizadores.rop.tipo.Type;
import com.dfoda.util.Hex;

public final class CstLong extends CstLiteral64 {
    public static final CstLong VALUE_0 = CstLong.make(0L);
    public static final CstLong VALUE_1 = CstLong.make(1L);

    public static CstLong make(long value) {
        return new CstLong(value);
    }

    public CstLong(long value) {
        super(value);
    }

    public String toString() {
        long value = this.getLongBits();
        return "long{0x" + Hex.u8(value) + " / " + value + '}';
    }

    @Override
    public Type getType() {
        return Type.LONG;
    }

    @Override
    public String typeName() {
        return "long";
    }

    @Override
    public String toHuman() {
        return Long.toString(this.getLongBits());
    }

    public long getValue() {
        return this.getLongBits();
    }
}

