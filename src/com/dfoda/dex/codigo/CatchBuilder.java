package com.dfoda.dex.codigo;

import com.dfoda.otimizadores.rop.tipo.Type;
import java.util.HashSet;

public interface CatchBuilder {
    public CatchTable build();

    public boolean hasAnyCatches();

    public HashSet<Type> getCatchTypes();
}

