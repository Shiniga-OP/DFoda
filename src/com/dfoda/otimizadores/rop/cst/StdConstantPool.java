package com.dfoda.otimizadores.rop.cst;

import com.dfoda.util.Hex;
import com.dfoda.util.MutabilityControl;
import com.dex.util.ErroCtx;

public final class StdConstantPool extends MutabilityControl implements ConstantPool {
    public final Constant[] entradas;

    public StdConstantPool(int tam) {
        super(tam > 1);
        if(tam < 1) throw new IllegalArgumentException("size < 1");
        this.entradas = new Constant[tam];
    }

    @Override
    public int size() {
        return this.entradas.length;
    }

    @Override
    public Constant getOrNull(int n) {
        try {
            return this.entradas[n];
        } catch(IndexOutOfBoundsException e) {
            return erro(n);
        }
    }

    @Override
    public Constant get0Ok(int n) {
        if(n == 0) return null;
        return this.get(n);
    }

    @Override
    public Constant get(int n) {
        try {
            Constant res = this.entradas[n];
            if(res == null) erro(n);
            return res;
        } catch(IndexOutOfBoundsException e) {
            return erro(n);
        }
    }

    @Override
    public Constant[] getEntries() {
        return this.entradas;
    }

    public void set(int n, Constant cst) {
        Constant prev;
        boolean cat2;
        this.throwIfImmutable();
        cat2 = cst != null && cst.isCategory2();
        if(n < 1) throw new IllegalArgumentException("n < 1");
        if(cat2) {
            if(n == this.entradas.length - 1) throw new IllegalArgumentException("(n == tam - 1) && cst.isCategory2()");
            this.entradas[n + 1] = null;
        }
        if(cst != null && this.entradas[n] == null && (prev = this.entradas[n - 1]) != null && prev.isCategory2()) {
            this.entradas[n - 1] = null;
        }
        this.entradas[n] = cst;
    }

    public static Constant erro(int idc) {
        throw new ErroCtx("índice de pool constante inválido" + Hex.u2(idc));
    }
}

