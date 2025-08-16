package com.dfoda.dex.arquivo;

import com.dfoda.util.AnnotatedOutput;
import com.dfoda.util.Hex;

public final class AnnotationSetRefItem extends OffsettedItem {
    public static final int ALIGNMENT = 4;
    public static final int WRITE_SIZE = 4;
    public AnnotationSetItem annotations;

    public AnnotationSetRefItem(AnnotationSetItem annotations) {
        super(4, 4);
        if(annotations == null) throw new NullPointerException("annotations == null");
        this.annotations = annotations;
    }

    @Override
    public ItemType itemType() {
        return ItemType.TYPE_ANNOTATION_SET_REF_ITEM;
    }

    @Override
    public void addContents(DexFile file) {
        MixedItemSection wordData = file.getWordData();
        this.annotations = wordData.intern(this.annotations);
    }

    @Override
    public String toHuman() {
        return this.annotations.toHuman();
    }

    @Override
    protected void writeTo0(DexFile file, AnnotatedOutput out) {
        int annotationsOff = this.annotations.getAbsoluteOffset();
        if (out.annotates()) {
            out.annotate(4, "  annotations_off: " + Hex.u4(annotationsOff));
        }
        out.writeInt(annotationsOff);
    }
}

