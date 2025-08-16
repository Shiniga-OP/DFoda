package com.dfoda.dex.arquivo;

import com.dfoda.dex.arquivo.DexFile;
import com.dfoda.dex.arquivo.ItemType;
import com.dfoda.util.AnnotatedOutput;

public abstract class Item {
    public abstract ItemType itemType();

    public final String typeName() {
        return this.itemType().toHuman();
    }

    public abstract int writeSize();

    public abstract void addContents(DexFile var1);

    public abstract void writeTo(DexFile var1, AnnotatedOutput var2);
}

