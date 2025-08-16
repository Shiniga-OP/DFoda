package com.dfoda.dex.arquivo;

import com.dex.Leb128;
import com.dfoda.dex.arquivo.DexFile;
import com.dfoda.dex.arquivo.ItemType;
import com.dfoda.dex.arquivo.OffsettedItem;
import com.dfoda.otimizadores.rop.cst.CstString;
import com.dfoda.util.AnnotatedOutput;
import com.dfoda.util.ByteArray;
import com.dfoda.util.Hex;

public final class StringDataItem extends OffsettedItem {
    public final CstString value;

    public StringDataItem(CstString value) {
        super(1, StringDataItem.writeSize(value));
        this.value = value;
    }

    private static int writeSize(CstString value) {
        int utf16Size = value.getUtf16Size();
        return Leb128.unsignedLeb128Size(utf16Size) + value.getUtf8Size() + 1;
    }

    @Override
    public ItemType itemType() {
        return ItemType.TYPE_STRING_DATA_ITEM;
    }

    @Override
    public void addContents(DexFile file) {
    }

    @Override
    public void writeTo0(DexFile file, AnnotatedOutput out) {
        ByteArray bytes = this.value.getBytes();
        int utf16Size = this.value.getUtf16Size();
        if (out.annotates()) {
            out.annotate(Leb128.unsignedLeb128Size(utf16Size), "utf16_size: " + Hex.u4(utf16Size));
            out.annotate(bytes.size() + 1, this.value.toQuoted());
        }
        out.writeUleb128(utf16Size);
        out.write(bytes);
        out.writeByte(0);
    }

    @Override
    public String toHuman() {
        return this.value.toQuoted();
    }

    @Override
    protected int compareTo0(OffsettedItem other) {
        StringDataItem otherData = (StringDataItem)other;
        return this.value.compareTo(otherData.value);
    }
}

