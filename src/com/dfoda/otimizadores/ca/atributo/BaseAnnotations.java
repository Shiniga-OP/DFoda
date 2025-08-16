package com.dfoda.otimizadores.ca.atributo;

import com.dfoda.otimizadores.rop.anotacao.Annotations;
import com.dex.util.ErroCtx;

public abstract class BaseAnnotations extends BaseAttribute {
    public final Annotations anotacoes;
    public final int byteTam;

    public BaseAnnotations(String nomeAtributo, Annotations anotacoes, int byteTam) {
        super(nomeAtributo);
        try {
            if(anotacoes.isMutable()) throw new ErroCtx("annotations.isMutable()");
        } catch(NullPointerException e) {
            throw new NullPointerException("anotacoes == null: "+e);
        }
        this.anotacoes = anotacoes;
        this.byteTam = byteTam;
    }

    @Override
    public final int byteLength() {
        return this.byteTam + 6;
    }

    public final Annotations getAnnotations() {
        return this.anotacoes;
    }
}

