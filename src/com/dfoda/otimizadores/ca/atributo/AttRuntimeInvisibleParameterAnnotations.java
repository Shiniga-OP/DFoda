package com.dfoda.otimizadores.ca.atributo;

import com.dfoda.otimizadores.rop.anotacao.AnnotationsList;

public final class AttRuntimeInvisibleParameterAnnotations extends BaseParameterAnnotations {
    public static final String ATTRIBUTE_NAME = "RuntimeInvisibleParameterAnnotations";

    public AttRuntimeInvisibleParameterAnnotations(AnnotationsList parameterAnnotations, int byteLength) {
        super(ATTRIBUTE_NAME, parameterAnnotations, byteLength);
    }
}

