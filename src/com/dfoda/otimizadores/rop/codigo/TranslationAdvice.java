package com.dfoda.otimizadores.rop.codigo;

public interface TranslationAdvice {
    public boolean hasConstantOperation(Rop var1, RegisterSpec var2, RegisterSpec var3);
    public boolean requiresSourcesInOrder(Rop var1, RegisterSpecList var2);
    public int getMaxOptimalRegisterCount();
}

