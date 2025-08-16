package com.dfoda.otimizadores.ca.atributo;

import com.dfoda.otimizadores.rop.anotacao.Annotations;

public final class AttRuntimeInvisibleAnnotations extends BaseAnnotations {
    public static final String ATTRIBUTE_NAME = "RuntimeInvisibleAnnotations";

    public AttRuntimeInvisibleAnnotations(Annotations annotations, int byteLength) {
        super(ATTRIBUTE_NAME, annotations, byteLength);
    }
}

