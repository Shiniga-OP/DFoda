package com.dfoda.otimizadores.ca.atributo;

import com.dfoda.otimizadores.rop.tipo.TypeList;
import com.dex.util.ErroCtx;

public final class AttExceptions extends BaseAttribute {
    public static final String ATTRIBUTE_NAME = "Exceptions";
    public final TypeList erros;

    public AttExceptions(TypeList exceptions) {
        super(ATTRIBUTE_NAME);
        try {
            if(exceptions.isMutable()) throw new ErroCtx("exceptions.isMutable()");
        } catch(NullPointerException e) {
            throw new NullPointerException("erros == null: " + e);
        }
        this.erros = exceptions;
    }

    @Override
    public int byteLength() {
        return 8 + this.erros.size() * 2;
    }

    public TypeList getExceptions() {
        return this.erros;
    }
}

