package com.dfoda.otimizadores.rop.tipo;

import com.dfoda.otimizadores.rop.tipo.Type;

public interface TypeList {
    public boolean isMutable();
    public int size();
    public Type getType(int var1);
    public int getWordCount();
    public TypeList withAddedType(Type var1);
}

