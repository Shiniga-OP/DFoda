package com.dfoda.otimizadores.ca.atributo;

import com.dfoda.otimizadores.rop.anotacao.AnnotationsList;
import com.dex.util.ErroCtx;

public abstract class BaseParameterAnnotations extends BaseAttribute {
    private final AnnotationsList parameterAnnotations;
    private final int byteLength;

    public BaseParameterAnnotations(String attributeName, AnnotationsList parameterAnnotations, int byteLength) {
        super(attributeName);
        try {
            if (parameterAnnotations.isMutable()) {
                throw new ErroCtx("parameterAnnotations.isMutable()");
            }
        }
        catch (NullPointerException ex) {
            throw new NullPointerException("parameterAnnotations == null");
        }
        this.parameterAnnotations = parameterAnnotations;
        this.byteLength = byteLength;
    }

    @Override
    public final int byteLength() {
        return this.byteLength + 6;
    }

    public final AnnotationsList getParameterAnnotations() {
        return this.parameterAnnotations;
    }
}

