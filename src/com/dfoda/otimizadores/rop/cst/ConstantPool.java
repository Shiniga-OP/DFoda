package com.dfoda.otimizadores.rop.cst;

public interface ConstantPool {
    public int size();
    public Constant get(int var1);
    public Constant get0Ok(int var1);
    public Constant getOrNull(int var1);
    public Constant[] getEntries();
}

