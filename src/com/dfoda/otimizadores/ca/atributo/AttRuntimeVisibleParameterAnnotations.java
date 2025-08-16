package com.dfoda.otimizadores.ca.atributo;

import com.dfoda.otimizadores.rop.anotacao.AnnotationsList;

public final class AttRuntimeVisibleParameterAnnotations extends BaseParameterAnnotations {
    public static final String ATTRIBUTE_NAME = "RuntimeVisibleParameterAnnotations";

    public AttRuntimeVisibleParameterAnnotations(AnnotationsList annotations, int byteLength) {
        super(ATTRIBUTE_NAME, annotations, byteLength);
    }
}

