package com.dfoda.otimizadores.ca.inter;

import com.dfoda.util.FixedSizeList;

public final class StdFieldList extends FixedSizeList implements FieldList {
    public StdFieldList(int size) {
        super(size);
    }

    @Override
    public Field get(int n) {
        return (Field)this.get0(n);
    }

    public void set(int n, Field field) {
        this.set0(n, field);
    }
}

