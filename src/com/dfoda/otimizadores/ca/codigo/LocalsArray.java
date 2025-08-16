package com.dfoda.otimizadores.ca.codigo;

import com.dfoda.util.MutabilityControl;
import com.dfoda.util.ToHuman;
import com.dex.util.ErroCtx;
import com.dfoda.otimizadores.rop.tipo.Type;
import com.dfoda.otimizadores.rop.tipo.TypeBearer;
import com.dfoda.otimizadores.rop.codigo.RegisterSpec;

public abstract class LocalsArray extends MutabilityControl implements ToHuman {
    public LocalsArray(boolean mutavel) {
        super(mutavel);
    }
    public abstract LocalsArray copy();
    public abstract void annotate(ErroCtx var1);
    public abstract void makeInitialized(Type var1);
    public abstract int getMaxLocals();
    public abstract void set(int var1, TypeBearer var2);
    public abstract void set(RegisterSpec var1);
    public abstract void invalidate(int var1);
    public abstract TypeBearer getOrNull(int var1);
    public abstract TypeBearer get(int var1);
    public abstract TypeBearer getCategory1(int var1);
    public abstract TypeBearer getCategory2(int var1);
    public abstract LocalsArray merge(LocalsArray var1);
    public abstract LocalsArraySet mergeWithSubroutineCaller(LocalsArray var1, int var2);
    protected abstract OneLocalsArray getPrimary();
}

