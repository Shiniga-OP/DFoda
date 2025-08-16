package com.dfoda.otimizadores.rop.cst;

import com.dfoda.otimizadores.rop.tipo.Prototype;
import com.dfoda.otimizadores.rop.tipo.Type;

public class CstCallSiteRef extends Constant {
    private final CstInvokeDynamic invokeDynamic;
    private final int id;

    CstCallSiteRef(CstInvokeDynamic invokeDynamic, int id) {
        if (invokeDynamic == null) {
            throw new NullPointerException("invokeDynamic == null");
        }
        this.invokeDynamic = invokeDynamic;
        this.id = id;
    }

    @Override
    public boolean isCategory2() {
        return false;
    }

    @Override
    public String typeName() {
        return "CallSiteRef";
    }

    @Override
    protected int compareTo0(Constant other) {
        CstCallSiteRef o = (CstCallSiteRef)other;
        int result = this.invokeDynamic.compareTo(o.invokeDynamic);
        if (result != 0) {
            return result;
        }
        return Integer.compare(this.id, o.id);
    }

    @Override
    public String toHuman() {
        return this.getCallSite().toHuman();
    }

    public String toString() {
        return this.getCallSite().toString();
    }

    public Prototype getPrototype() {
        return this.invokeDynamic.getPrototype();
    }

    public Type getReturnType() {
        return this.invokeDynamic.getReturnType();
    }

    public CstCallSite getCallSite() {
        return this.invokeDynamic.getCallSite();
    }
}

