package com.dfoda.otimizadores.rop.tipo;

import com.dfoda.otimizadores.rop.tipo.Type;
import com.dfoda.util.ToHuman;

public interface TypeBearer extends ToHuman {
    public Type getType();
    public TypeBearer getFrameType();
    public int getBasicType();
    public int getBasicFrameType();
    public boolean isConstant();
}

