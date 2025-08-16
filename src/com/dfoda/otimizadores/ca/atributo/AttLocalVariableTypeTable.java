package com.dfoda.otimizadores.ca.atributo;

import com.dfoda.otimizadores.ca.codigo.LocalVariableList;

public final class AttLocalVariableTypeTable extends BaseLocalVariables {
    public static final String ATTRIBUTE_NAME = "LocalVariableTypeTable";

    public AttLocalVariableTypeTable(LocalVariableList localVariables) {
        super(ATTRIBUTE_NAME, localVariables);
    }
}

