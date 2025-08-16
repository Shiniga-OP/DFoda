package com.dfoda.otimizadores.ca.atributo;

public final class AttDeprecated extends BaseAttribute {
    public static final String ATTRIBUTE_NAME = "Deprecated";

    public AttDeprecated() {
        super(ATTRIBUTE_NAME);
    }

    @Override
    public int byteLength() {
        return 6;
    }
}

