package com.dfoda.otimizadores.rop.cst;

import com.dfoda.otimizadores.rop.tipo.Type;
import com.dfoda.util.Hex;

public final class CstFloat
extends CstLiteral32 {
    public static final CstFloat VALUE_0 = CstFloat.make(Float.floatToIntBits(0.0f));
    public static final CstFloat VALUE_1 = CstFloat.make(Float.floatToIntBits(1.0f));
    public static final CstFloat VALUE_2 = CstFloat.make(Float.floatToIntBits(2.0f));

    public static CstFloat make(int bits) {
        return new CstFloat(bits);
    }

    private CstFloat(int bits) {
        super(bits);
    }

    public String toString() {
        int bits = this.getIntBits();
        return "float{0x" + Hex.u4(bits) + " / " + Float.intBitsToFloat(bits) + '}';
    }

    @Override
    public Type getType() {
        return Type.FLOAT;
    }

    @Override
    public String typeName() {
        return "float";
    }

    @Override
    public String toHuman() {
        return Float.toString(Float.intBitsToFloat(this.getIntBits()));
    }

    public float getValue() {
        return Float.intBitsToFloat(this.getIntBits());
    }
}

