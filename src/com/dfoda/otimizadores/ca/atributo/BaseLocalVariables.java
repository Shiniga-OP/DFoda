package com.dfoda.otimizadores.ca.atributo;

import com.dfoda.otimizadores.ca.codigo.LocalVariableList;
import com.dex.util.ErroCtx;

public abstract class BaseLocalVariables extends BaseAttribute {
    public final LocalVariableList variaveisLocais;

    public BaseLocalVariables(String nome, LocalVariableList variaveisLocais) {
        super(nome);
        try {
            if(variaveisLocais.isMutable()) throw new ErroCtx("localVariables.isMutable()");
        } catch(NullPointerException e) {
            throw new NullPointerException("variaveisLocais == null: " + e);
        }
        this.variaveisLocais = variaveisLocais;
    }

    @Override
    public final int byteLength() {
        return 8 + this.variaveisLocais.size() * 10;
    }

    public final LocalVariableList getLocalVariables() {
        return this.variaveisLocais;
    }
}

