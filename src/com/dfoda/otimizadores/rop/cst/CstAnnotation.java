package com.dfoda.otimizadores.rop.cst;

import com.dfoda.otimizadores.rop.anotacao.Annotation;

public final class CstAnnotation extends Constant {
    private final Annotation annotation;

    public CstAnnotation(Annotation annotation) {
        if (annotation == null) {
            throw new NullPointerException("annotation == null");
        }
        annotation.throwIfMutable();
        this.annotation = annotation;
    }

    public boolean equals(Object other) {
        if (!(other instanceof CstAnnotation)) {
            return false;
        }
        return this.annotation.equals(((CstAnnotation)other).annotation);
    }

    public int hashCode() {
        return this.annotation.hashCode();
    }

    @Override
    protected int compareTo0(Constant other) {
        return this.annotation.compareTo(((CstAnnotation)other).annotation);
    }

    public String toString() {
        return this.annotation.toString();
    }

    @Override
    public String typeName() {
        return "annotation";
    }

    @Override
    public boolean isCategory2() {
        return false;
    }

    @Override
    public String toHuman() {
        return this.annotation.toString();
    }

    public Annotation getAnnotation() {
        return this.annotation;
    }
}

