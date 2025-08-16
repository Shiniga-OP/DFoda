package com.dfoda.otimizadores.ca.atributo;

import com.dfoda.otimizadores.ca.inter.Attribute;

public abstract class BaseAttribute implements Attribute {
    public final String name;

    public BaseAttribute(String name) {
        if(name == null) throw new NullPointerException("name == null");
        this.name = name;
    }

    @Override
    public String getName() {
        return this.name;
    }
}

