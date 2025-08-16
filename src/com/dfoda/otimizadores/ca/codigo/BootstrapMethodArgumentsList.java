package com.dfoda.otimizadores.ca.codigo;

import com.dfoda.otimizadores.rop.cst.Constant;
import com.dfoda.otimizadores.rop.cst.CstDouble;
import com.dfoda.otimizadores.rop.cst.CstFloat;
import com.dfoda.otimizadores.rop.cst.CstInteger;
import com.dfoda.otimizadores.rop.cst.CstLong;
import com.dfoda.otimizadores.rop.cst.CstMethodHandle;
import com.dfoda.otimizadores.rop.cst.CstProtoRef;
import com.dfoda.otimizadores.rop.cst.CstString;
import com.dfoda.otimizadores.rop.cst.CstType;
import com.dfoda.util.FixedSizeList;

public class BootstrapMethodArgumentsList
extends FixedSizeList {
    public BootstrapMethodArgumentsList(int count) {
        super(count);
    }

    public Constant get(int n) {
        return (Constant)this.get0(n);
    }

    public void set(int n, Constant cst) {
        if (!(cst instanceof CstString || cst instanceof CstType || cst instanceof CstInteger || cst instanceof CstLong || cst instanceof CstFloat || cst instanceof CstDouble || cst instanceof CstMethodHandle || cst instanceof CstProtoRef)) {
            Class<?> klass = cst.getClass();
            throw new IllegalArgumentException("bad type for bootstrap argument: " + klass);
        }
        this.set0(n, cst);
    }
}

