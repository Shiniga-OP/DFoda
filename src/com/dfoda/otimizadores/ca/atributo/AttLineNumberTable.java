package com.dfoda.otimizadores.ca.atributo;

import com.dfoda.otimizadores.ca.codigo.LineNumberList;
import com.dex.util.ErroCtx;

public final class AttLineNumberTable extends BaseAttribute {
    public static final String ATTRIBUTE_NAME = "LineNumberTable";
    private final LineNumberList linhaNums;

    public AttLineNumberTable(LineNumberList linhaNums) {
        super(ATTRIBUTE_NAME);
        try {
            if(linhaNums.isMutable()) throw new ErroCtx("linhaNums.isMutable()");
        } catch(NullPointerException e) {
            throw new NullPointerException("linhaNums == null: "+e);
        }
        this.linhaNums = linhaNums;
    }

    @Override
    public int byteLength() {
        return 8 + 4 * this.linhaNums.size();
    }

    public LineNumberList getLineNumbers() {
        return this.linhaNums;
    }
}

