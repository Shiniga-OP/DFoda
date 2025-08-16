package com.dfoda.dex.arquivo;

import com.dfoda.dex.arquivo.DexFile;
import com.dfoda.dex.arquivo.IndexedItem;
import com.dfoda.dex.arquivo.Item;
import com.dfoda.dex.arquivo.Section;
import com.dfoda.otimizadores.rop.cst.Constant;
import com.dfoda.util.AnnotatedOutput;
import java.util.Collection;

public abstract class UniformItemSection extends Section {
    public UniformItemSection(String name, DexFile file, int alignment) {
        super(name, file, alignment);
    }

    @Override
    public final int writeSize() {
        Collection<? extends Item> items = this.items();
        int sz = items.size();
        if(sz == 0) return 0;
        return sz * items.iterator().next().writeSize();
    }

    public abstract IndexedItem get(Constant var1);

    @Override
    protected final void prepare0() {
        DexFile file = this.getFile();
        this.orderItems();
        for (Item item : this.items()) {
            item.addContents(file);
        }
    }

    @Override
    protected final void writeTo0(AnnotatedOutput out) {
        DexFile file = this.getFile();
        int alignment = this.getAlignment();
        for (Item item : this.items()) {
            item.writeTo(file, out);
            out.alignTo(alignment);
        }
    }

    @Override
    public final int getAbsoluteItemOffset(Item item) {
        IndexedItem ii = (IndexedItem)item;
        int relativeOffset = ii.getIndex() * ii.writeSize();
        return this.getAbsoluteOffset(relativeOffset);
    }

    protected abstract void orderItems();
}

