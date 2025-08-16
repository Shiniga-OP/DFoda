package com.dfoda.otimizadores.ca.atributo;

import com.dfoda.otimizadores.rop.cst.CstString;

public final class AttSourceFile extends BaseAttribute {
    public static final String ATTRIBUTE_NAME = "SourceFile";
    public final CstString sourceFile;

    public AttSourceFile(CstString sourceFile) {
        super(ATTRIBUTE_NAME);
        if(sourceFile == null) throw new NullPointerException("sourceFile == null");
        this.sourceFile = sourceFile;
    }

    @Override
    public int byteLength() {
        return 8;
    }

    public CstString getSourceFile() {
        return this.sourceFile;
    }
}

