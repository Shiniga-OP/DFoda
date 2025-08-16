package com.dfoda.otimizadores.rop.cst;

import com.dfoda.otimizadores.rop.tipo.Type;

public final class CstKnownNull extends CstLiteralBits {
    public static final CstKnownNull THE_ONE = new CstKnownNull();

    public boolean equals(Object other) {
        return other instanceof CstKnownNull;
    }

    public int hashCode() {
        return 1147565434;
    }

    @Override
    protected int compareTo0(Constant other) {
        return 0;
    }

    public String toString() {
        return "known-null";
    }

    @Override
    public Type getType() {
        return Type.KNOWN_NULL;
    }

    @Override
    public String typeName() {
        return "known-null";
    }

    @Override
    public boolean isCategory2() {
        return false;
    }

    @Override
    public String toHuman() {
        return "null";
    }

    @Override
    public boolean fitsInInt() {
        return true;
    }

    @Override
    public int getIntBits() {
        return 0;
    }

    @Override
    public long getLongBits() {
        return 0L;
    }
}

