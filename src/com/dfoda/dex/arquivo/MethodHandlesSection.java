package com.dfoda.dex.arquivo;

import java.util.Collection;
import java.util.TreeMap;
import com.dfoda.otimizadores.rop.cst.CstMethodHandle;
import com.dfoda.otimizadores.rop.cst.Constant;

public final class MethodHandlesSection
extends UniformItemSection {
    private final TreeMap<CstMethodHandle, MethodHandleItem> methodHandles = new TreeMap();

    public MethodHandlesSection(DexFile dexFile) {
        super("method_handles", dexFile, 8);
    }

    @Override
    public IndexedItem get(Constant cst) {
        if (cst == null) {
            throw new NullPointerException("cst == null");
        }
        this.throwIfNotPrepared();
        IndexedItem result = this.methodHandles.get((CstMethodHandle)cst);
        if (result == null) {
            throw new IllegalArgumentException("not found");
        }
        return result;
    }

    @Override
    protected void orderItems() {
        int index = 0;
        for (MethodHandleItem item : this.methodHandles.values()) {
            item.setIndex(index++);
        }
    }

    @Override
    public Collection<? extends Item> items() {
        return this.methodHandles.values();
    }

    public void intern(CstMethodHandle methodHandle) {
        if (methodHandle == null) {
            throw new NullPointerException("methodHandle == null");
        }
        this.throwIfPrepared();
        MethodHandleItem result = this.methodHandles.get(methodHandle);
        if (result == null) {
            result = new MethodHandleItem(methodHandle);
            this.methodHandles.put(methodHandle, result);
        }
    }

    int indexOf(CstMethodHandle cstMethodHandle) {
        return this.methodHandles.get(cstMethodHandle).getIndex();
    }
}

