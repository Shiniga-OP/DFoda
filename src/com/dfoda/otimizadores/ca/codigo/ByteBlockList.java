package com.dfoda.otimizadores.ca.codigo;

import com.dfoda.util.LabeledList;
import com.dfoda.util.Hex;

public final class ByteBlockList extends LabeledList {
    public ByteBlockList(int size) {
        super(size);
    }

    public ByteBlock get(int n) {
        return (ByteBlock)this.get0(n);
    }

    public ByteBlock labelToBlock(int label) {
        int idx = this.indexOfLabel(label);
        if (idx < 0) {
            throw new IllegalArgumentException("no such label: " + Hex.u2(label));
        }
        return this.get(idx);
    }

    public void set(int n, ByteBlock bb) {
        super.set(n, bb);
    }
}

