package com.dfoda.dex.arquivo;

import com.dfoda.dex.arquivo.DexFile;
import com.dfoda.dex.arquivo.IdItem;
import com.dfoda.dex.arquivo.ItemType;
import com.dfoda.otimizadores.rop.cst.CstString;
import com.dfoda.otimizadores.rop.cst.CstType;
import com.dfoda.util.AnnotatedOutput;
import com.dfoda.util.Hex;

public final class TypeIdItem extends IdItem {
    public TypeIdItem(CstType type) {
        super(type);
    }

    @Override
    public ItemType itemType() {
        return ItemType.TYPE_TYPE_ID_ITEM;
    }

    @Override
    public int writeSize() {
        return 4;
    }

    @Override
    public void addContents(DexFile file) {
        file.getStringIds().intern(this.getDefiningClass().getDescriptor());
    }

    @Override
    public void writeTo(DexFile file, AnnotatedOutput out) {
        CstType type = this.getDefiningClass();
        CstString descriptor = type.getDescriptor();
        int idx = file.getStringIds().indexOf(descriptor);
        if(out.annotates()) {
            out.annotate(0, this.indexString() + ' ' + descriptor.toHuman());
            out.annotate(4, "  descriptor_idx: " + Hex.u4(idx));
        }
        out.writeInt(idx);
    }
}

