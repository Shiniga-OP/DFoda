package com.dfoda.otimizadores.ca.inter;

import com.dfoda.util.FixedSizeList;

public final class StdMethodList extends FixedSizeList implements MethodList {
    public StdMethodList(int size) {
        super(size);
    }

    @Override
    public Method get(int n) {
        return (Method)this.get0(n);
    }

    public void set(int n, Method method) {
        this.set0(n, method);
    }
}

