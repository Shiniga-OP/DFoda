package com.dfoda.otimizadores.rop.cst;

import com.dfoda.otimizadores.rop.tipo.Type;
import com.dfoda.util.Hex;

public final class CstShort extends CstLiteral32 {
    public static final CstShort VALUE_0 = CstShort.make((short)0);

    public static CstShort make(short value) {
        return new CstShort(value);
    }

    public static CstShort make(int valor) {
        short cast = (short)valor;
        if(cast != valor) throw new IllegalArgumentException("bogus short value: " + valor);
        return CstShort.make(cast);
    }

    private CstShort(short value) {
        super(value);
    }

    public String toString() {
        int value = this.getIntBits();
        return "short{0x" + Hex.u2(value) + " / " + value + '}';
    }

    @Override
    public Type getType() {
        return Type.SHORT;
    }

    @Override
    public String typeName() {
        return "short";
    }

    @Override
    public String toHuman() {
        return Integer.toString(this.getIntBits());
    }

    public short getValue() {
        return (short)this.getIntBits();
    }
}

