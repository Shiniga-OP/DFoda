package com.dfoda.otimizadores.rop.cst;

import com.dfoda.otimizadores.rop.tipo.TypeBearer;

public abstract class TypedConstant extends Constant implements TypeBearer {
    @Override
    public final TypeBearer getFrameType() {
        return this;
    }

    @Override
    public final int getBasicType() {
        return this.getType().getBasicType();
    }

    @Override
    public final int getBasicFrameType() {
        return this.getType().getBasicFrameType();
    }

    @Override
    public final boolean isConstant() {
        return true;
    }
}

