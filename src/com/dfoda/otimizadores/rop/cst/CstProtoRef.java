package com.dfoda.otimizadores.rop.cst;

import com.dfoda.otimizadores.rop.tipo.Prototype;
import com.dfoda.otimizadores.rop.tipo.Type;

public final class CstProtoRef extends TypedConstant {
    private final Prototype prototype;

    public CstProtoRef(Prototype prototype) {
        this.prototype = prototype;
    }

    public static CstProtoRef make(CstString descriptor) {
        Prototype prototype = Prototype.fromDescriptor(descriptor.getString());
        return new CstProtoRef(prototype);
    }

    public boolean equals(Object other) {
        if (!(other instanceof CstProtoRef)) {
            return false;
        }
        CstProtoRef otherCstProtoRef = (CstProtoRef)other;
        return this.getPrototype().equals(otherCstProtoRef.getPrototype());
    }

    public int hashCode() {
        return this.prototype.hashCode();
    }

    @Override
    public boolean isCategory2() {
        return false;
    }

    @Override
    public String typeName() {
        return "proto";
    }

    @Override
    protected int compareTo0(Constant other) {
        CstProtoRef otherCstProtoRef = (CstProtoRef)other;
        return this.prototype.compareTo(otherCstProtoRef.getPrototype());
    }

    @Override
    public String toHuman() {
        return this.prototype.getDescriptor();
    }

    public final String toString() {
        return this.typeName() + "{" + this.toHuman() + '}';
    }

    public Prototype getPrototype() {
        return this.prototype;
    }

    @Override
    public Type getType() {
        return Type.METHOD_TYPE;
    }
}

