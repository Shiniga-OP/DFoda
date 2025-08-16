package com.dfoda.otimizadores.ca.atributo;

import com.dfoda.otimizadores.ca.codigo.LocalVariableList;

public final class AttLocalVariableTable extends BaseLocalVariables {
    public static final String ATTRIBUTE_NAME = "LocalVariableTable";

    public AttLocalVariableTable(LocalVariableList localVariables) {
        super(ATTRIBUTE_NAME, localVariables);
    }
}

