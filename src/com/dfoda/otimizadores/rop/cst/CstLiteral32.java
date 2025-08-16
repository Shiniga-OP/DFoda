package com.dfoda.otimizadores.rop.cst;

public abstract class CstLiteral32 extends CstLiteralBits {
    public final int bits;

    CstLiteral32(int bits) {
        this.bits = bits;
    }

    public final boolean equals(Object other) {
        return other != null && this.getClass() == other.getClass() && this.bits == ((CstLiteral32)other).bits;
    }

    public final int hashCode() {
        return this.bits;
    }

    @Override
    protected int compareTo0(Constant other) {
        int otherBits = ((CstLiteral32)other).bits;
        if(this.bits < otherBits) return -1;
        if(this.bits > otherBits) return 1;
        return 0;
    }

    @Override
    public final boolean isCategory2() {
        return false;
    }

    @Override
    public final boolean fitsInInt() {
        return true;
    }

    @Override
    public final int getIntBits() {
        return this.bits;
    }

    @Override
    public final long getLongBits() {
        return this.bits;
    }
}

