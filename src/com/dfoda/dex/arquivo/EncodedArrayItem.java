package com.dfoda.dex.arquivo;

import com.dfoda.dex.arquivo.DexFile;
import com.dfoda.dex.arquivo.ItemType;
import com.dfoda.dex.arquivo.OffsettedItem;
import com.dfoda.dex.arquivo.Section;
import com.dfoda.dex.arquivo.ValueEncoder;
import com.dfoda.otimizadores.rop.cst.CstArray;
import com.dfoda.util.AnnotatedOutput;
import com.dfoda.util.ByteArrayAnnotatedOutput;

public final class EncodedArrayItem extends OffsettedItem {
    public static final int ALIGNMENT = 1;
    public final CstArray array;
    public byte[] encodedForm;

    public EncodedArrayItem(CstArray array) {
        super(1, -1);
        if(array == null) throw new NullPointerException("array == null");
        this.array = array;
        this.encodedForm = null;
    }

    @Override
    public ItemType itemType() {
        return ItemType.TYPE_ENCODED_ARRAY_ITEM;
    }

    public int hashCode() {
        return this.array.hashCode();
    }

    @Override
    protected int compareTo0(OffsettedItem other) {
        EncodedArrayItem otherArray = (EncodedArrayItem)other;
        return this.array.compareTo(otherArray.array);
    }

    @Override
    public String toHuman() {
        return this.array.toHuman();
    }

    @Override
    public void addContents(DexFile file) {
        ValueEncoder.addContents(file, this.array);
    }

    @Override
    protected void place0(Section addedTo, int offset) {
        ByteArrayAnnotatedOutput out = new ByteArrayAnnotatedOutput();
        ValueEncoder encoder = new ValueEncoder(addedTo.getFile(), out);
        encoder.writeArray(this.array, false);
        this.encodedForm = out.praByteArray();
        this.setWriteSize(this.encodedForm.length);
    }

    @Override
    protected void writeTo0(DexFile file, AnnotatedOutput out) {
        boolean annotates = out.annotates();
        if (annotates) {
            out.annotate(0, this.offsetString() + " encoded array");
            ValueEncoder encoder = new ValueEncoder(file, out);
            encoder.writeArray(this.array, true);
        } else {
            out.write(this.encodedForm);
        }
    }
}

