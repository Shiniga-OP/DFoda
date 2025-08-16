package com.dfoda.otimizadores.rop.cst;

import com.dfoda.otimizadores.rop.tipo.Type;
import com.dfoda.util.Hex;

public final class CstChar extends CstLiteral32 {
    public static final CstChar VALUE_0 = CstChar.make('\u0000');

    public static CstChar make(char value) {
        return new CstChar(value);
    }

    public static CstChar make(int value) {
        char cast = (char)value;
        if (cast != value) {
            throw new IllegalArgumentException("bogus char value: " + value);
        }
        return CstChar.make(cast);
    }

    private CstChar(char value) {
        super(value);
    }

    public String toString() {
        int value = this.getIntBits();
        return "char{0x" + Hex.u2(value) + " / " + value + '}';
    }

    @Override
    public Type getType() {
        return Type.CHAR;
    }

    @Override
    public String typeName() {
        return "char";
    }

    @Override
    public String toHuman() {
        return Integer.toString(this.getIntBits());
    }

    public char getValue() {
        return (char)this.getIntBits();
    }
}

