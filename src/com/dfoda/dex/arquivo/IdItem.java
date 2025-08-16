package com.dfoda.dex.arquivo;

import com.dfoda.otimizadores.rop.cst.CstType;

public abstract class IdItem extends IndexedItem {
    private final CstType type;

    public IdItem(CstType type) {
        if (type == null) {
            throw new NullPointerException("type == null");
        }
        this.type = type;
    }

    @Override
    public void addContents(DexFile file) {
        TypeIdsSection typeIds = file.getTypeIds();
        typeIds.intern(this.type);
    }

    public final CstType getDefiningClass() {
        return this.type;
    }
}

