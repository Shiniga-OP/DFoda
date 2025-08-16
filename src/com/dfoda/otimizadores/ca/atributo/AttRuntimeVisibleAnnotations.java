package com.dfoda.otimizadores.ca.atributo;

import com.dfoda.otimizadores.rop.anotacao.Annotations;

public final class AttRuntimeVisibleAnnotations extends BaseAnnotations {
    public static final String ATTRIBUTE_NAME = "RuntimeVisibleAnnotations";

    public AttRuntimeVisibleAnnotations(Annotations annotations, int byteLength) {
        super(ATTRIBUTE_NAME, annotations, byteLength);
    }
}

