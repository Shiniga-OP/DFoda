package com.dfoda.otimizadores.ca.codigo;

import com.dfoda.util.IntList;
import com.dfoda.util.LabeledItem;
import com.dfoda.util.Hex;

public final class ByteBlock implements LabeledItem {
    private final int label;
    private final int start;
    private final int end;
    private final IntList successors;
    private final ByteCatchList catches;

    public ByteBlock(int label, int start, int end, IntList successors, ByteCatchList catches) {
        if (label < 0) {
            throw new IllegalArgumentException("label < 0");
        }
        if (start < 0) {
            throw new IllegalArgumentException("start < 0");
        }
        if (end <= start) {
            throw new IllegalArgumentException("end <= start");
        }
        if (successors == null) {
            throw new NullPointerException("targets == null");
        }
        int sz = successors.size();
        for (int i = 0; i < sz; ++i) {
            if (successors.get(i) >= 0) continue;
            throw new IllegalArgumentException("successors[" + i + "] == " + successors.get(i));
        }
        if (catches == null) {
            throw new NullPointerException("catches == null");
        }
        this.label = label;
        this.start = start;
        this.end = end;
        this.successors = successors;
        this.catches = catches;
    }

    public String toString() {
        return '{' + Hex.u2(this.label) + ": " + Hex.u2(this.start) + ".." + Hex.u2(this.end) + '}';
    }

    @Override
    public int getLabel() {
        return this.label;
    }

    public int getStart() {
        return this.start;
    }

    public int getEnd() {
        return this.end;
    }

    public IntList getSuccessors() {
        return this.successors;
    }

    public ByteCatchList getCatches() {
        return this.catches;
    }
}

