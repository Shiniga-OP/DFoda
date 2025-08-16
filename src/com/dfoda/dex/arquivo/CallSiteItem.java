package com.dfoda.dex.arquivo;

import com.dfoda.otimizadores.rop.cst.CstCallSite;
import com.dfoda.util.ByteArrayAnnotatedOutput;
import com.dfoda.util.AnnotatedOutput;

public final class CallSiteItem extends OffsettedItem {
    private final CstCallSite value;
    private byte[] encodedForm;

    public CallSiteItem(CstCallSite value) {
        super(1, CallSiteItem.writeSize(value));
        this.value = value;
    }

    private static int writeSize(CstCallSite value) {
        return -1;
    }

    @Override
    protected void place0(Section addedTo, int offset) {
        ByteArrayAnnotatedOutput out = new ByteArrayAnnotatedOutput();
        ValueEncoder encoder = new ValueEncoder(addedTo.getFile(), out);
        encoder.writeArray(this.value, true);
        this.encodedForm = out.praByteArray();
        this.setWriteSize(this.encodedForm.length);
    }

    @Override
    public String toHuman() {
        return this.value.toHuman();
    }

    public String toString() {
        return this.value.toString();
    }

    @Override
    protected void writeTo0(DexFile file, AnnotatedOutput out) {
        if (out.annotates()) {
            out.annotate(0, this.offsetString() + " call site");
            ValueEncoder encoder = new ValueEncoder(file, out);
            encoder.writeArray(this.value, true);
        } else {
            out.write(this.encodedForm);
        }
    }

    @Override
    public ItemType itemType() {
        return ItemType.TYPE_ENCODED_ARRAY_ITEM;
    }

    @Override
    public void addContents(DexFile file) {
        ValueEncoder.addContents(file, this.value);
    }
}

