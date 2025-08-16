package com.dfoda.otimizadores.rop.cst;

import com.dfoda.otimizadores.rop.tipo.Type;

public final class CstBoolean extends CstLiteral32 {
    public static final CstBoolean VALUE_FALSE = new CstBoolean(false);
    public static final CstBoolean VALUE_TRUE = new CstBoolean(true);

    public static CstBoolean make(boolean value) {
        return value ? VALUE_TRUE : VALUE_FALSE;
    }

    public static CstBoolean make(int valor) {
        if(valor == 0) return VALUE_FALSE;
        if(valor == 1) return VALUE_TRUE;
        throw new IllegalArgumentException("bogus value: " + valor);
    }

    private CstBoolean(boolean value) {
        super(value ? 1 : 0);
    }

    public String toString() {
        return this.getValue() ? "boolean{true}" : "boolean{false}";
    }

    @Override
    public Type getType() {
        return Type.BOOLEAN;
    }

    @Override
    public String typeName() {
        return "boolean";
    }

    @Override
    public String toHuman() {
        return this.getValue() ? "true" : "false";
    }

    public boolean getValue() {
        return this.getIntBits() != 0;
    }
}

