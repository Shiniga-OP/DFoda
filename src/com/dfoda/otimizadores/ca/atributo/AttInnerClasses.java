package com.dfoda.otimizadores.ca.atributo;

import com.dex.util.ErroCtx;

public final class AttInnerClasses extends BaseAttribute {
    public static final String ATTRIBUTE_NAME = "InnerClasses";
    private final InnerClassList classesInters;

    public AttInnerClasses(InnerClassList classesInters) {
        super(ATTRIBUTE_NAME);
        try {
            if(classesInters.isMutable()) throw new ErroCtx("innerClasses.isMutable()");
        } catch(NullPointerException e) {
            throw new NullPointerException("innerClasses == null");
        }
        this.classesInters = classesInters;
    }

    @Override
    public int byteLength() {
        return 8 + this.classesInters.size() * 8;
    }

    public InnerClassList getInnerClasses() {
        return this.classesInters;
    }
}

