package com.dfoda.otimizadores.rop.cst;

import com.dfoda.util.ToHuman;

public abstract class Constant implements ToHuman, Comparable<Constant> {
    public abstract boolean isCategory2();
    public abstract String typeName();

    @Override
    public final int compareTo(Constant outra) {
        Class<?> outraClasse;
        Class<?> classe = this.getClass();
        if(classe != (outraClasse = outra.getClass())) return classe.getName().compareTo(outraClasse.getName());
        return this.compareTo0(outra);
    }

    protected abstract int compareTo0(Constant var1);
}

