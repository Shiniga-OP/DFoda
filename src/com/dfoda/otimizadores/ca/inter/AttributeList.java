package com.dfoda.otimizadores.ca.inter;

public interface AttributeList {
    public boolean isMutable();
    public int size();
    public Attribute get(int var1);
    public int byteLength();
    public Attribute findFirst(String var1);
    public Attribute findNext(Attribute var1);
}

