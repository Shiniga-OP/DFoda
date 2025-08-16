package com.dfoda.dex.arquivo;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import com.dfoda.otimizadores.rop.cst.Constant;

public final class HeaderSection extends UniformItemSection {
    private final List<HeaderItem> list;

    public HeaderSection(DexFile file) {
        super(null, file, 4);
        HeaderItem item = new HeaderItem();
        item.setIndex(0);
        this.list = Collections.singletonList(item);
    }

    @Override
    public IndexedItem get(Constant cst) {
        return null;
    }

    @Override
    public Collection<? extends Item> items() {
        return this.list;
    }

    @Override
    protected void orderItems() {
    }
}

