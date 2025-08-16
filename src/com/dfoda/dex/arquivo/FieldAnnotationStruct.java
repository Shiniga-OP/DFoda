package com.dfoda.dex.arquivo;

import com.dfoda.otimizadores.rop.anotacao.Annotations;
import com.dfoda.otimizadores.rop.cst.CstFieldRef;
import com.dfoda.util.AnnotatedOutput;
import com.dfoda.util.Hex;
import com.dfoda.util.ToHuman;

public final class FieldAnnotationStruct implements ToHuman, Comparable<FieldAnnotationStruct> {
    private final CstFieldRef field;
    private AnnotationSetItem annotations;

    public FieldAnnotationStruct(CstFieldRef field, AnnotationSetItem annotations) {
        if (field == null) {
            throw new NullPointerException("field == null");
        }
        if (annotations == null) {
            throw new NullPointerException("annotations == null");
        }
        this.field = field;
        this.annotations = annotations;
    }

    public int hashCode() {
        return this.field.hashCode();
    }

    public boolean equals(Object other) {
        if (!(other instanceof FieldAnnotationStruct)) {
            return false;
        }
        return this.field.equals(((FieldAnnotationStruct)other).field);
    }

    @Override
    public int compareTo(FieldAnnotationStruct other) {
        return this.field.compareTo(other.field);
    }

    public void addContents(DexFile file) {
        FieldIdsSection fieldIds = file.getFieldIds();
        MixedItemSection wordData = file.getWordData();
        fieldIds.intern(this.field);
        this.annotations = wordData.intern(this.annotations);
    }

    public void writeTo(DexFile file, AnnotatedOutput out) {
        int fieldIdx = file.getFieldIds().indexOf(this.field);
        int annotationsOff = this.annotations.getAbsoluteOffset();
        if (out.annotates()) {
            out.annotate(0, "    " + this.field.toHuman());
            out.annotate(4, "      field_idx:       " + Hex.u4(fieldIdx));
            out.annotate(4, "      annotations_off: " + Hex.u4(annotationsOff));
        }
        out.writeInt(fieldIdx);
        out.writeInt(annotationsOff);
    }

    @Override
    public String toHuman() {
        return this.field.toHuman() + ": " + this.annotations;
    }

    public CstFieldRef getField() {
        return this.field;
    }

    public Annotations getAnnotations() {
        return this.annotations.getAnnotations();
    }
}

