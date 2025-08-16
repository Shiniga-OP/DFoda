package com.dfoda.dex.arquivo;

import java.util.ArrayList;
import com.dfoda.util.ToHuman;
import com.dfoda.otimizadores.rop.cst.CstMethodRef;
import com.dfoda.otimizadores.rop.anotacao.AnnotationsList;
import com.dfoda.otimizadores.rop.anotacao.Annotations;
import com.dfoda.util.AnnotatedOutput;
import com.dfoda.util.Hex;

public final class ParameterAnnotationStruct implements ToHuman, Comparable<ParameterAnnotationStruct> {
    private final CstMethodRef method;
    private final AnnotationsList annotationsList;
    private final UniformListItem<AnnotationSetRefItem> annotationsItem;

    public ParameterAnnotationStruct(CstMethodRef method, AnnotationsList annotationsList, DexFile dexFile) {
        if (method == null) {
            throw new NullPointerException("method == null");
        }
        if (annotationsList == null) {
            throw new NullPointerException("annotationsList == null");
        }
        this.method = method;
        this.annotationsList = annotationsList;
        int size = annotationsList.size();
        ArrayList<AnnotationSetRefItem> arrayList = new ArrayList<AnnotationSetRefItem>(size);
        for (int i = 0; i < size; ++i) {
            Annotations annotations = annotationsList.get(i);
            AnnotationSetItem item = new AnnotationSetItem(annotations, dexFile);
            arrayList.add(new AnnotationSetRefItem(item));
        }
        this.annotationsItem = new UniformListItem(ItemType.TYPE_ANNOTATION_SET_REF_LIST, arrayList);
    }

    public int hashCode() {
        return this.method.hashCode();
    }

    public boolean equals(Object other) {
        if (!(other instanceof ParameterAnnotationStruct)) {
            return false;
        }
        return this.method.equals(((ParameterAnnotationStruct)other).method);
    }

    @Override
    public int compareTo(ParameterAnnotationStruct other) {
        return this.method.compareTo(other.method);
    }

    public void addContents(DexFile file) {
        MethodIdsSection methodIds = file.getMethodIds();
        MixedItemSection wordData = file.getWordData();
        methodIds.intern(this.method);
        wordData.add(this.annotationsItem);
    }

    public void writeTo(DexFile file, AnnotatedOutput out) {
        int methodIdx = file.getMethodIds().indexOf(this.method);
        int annotationsOff = this.annotationsItem.getAbsoluteOffset();
        if (out.annotates()) {
            out.annotate(0, "    " + this.method.toHuman());
            out.annotate(4, "      method_idx:      " + Hex.u4(methodIdx));
            out.annotate(4, "      annotations_off: " + Hex.u4(annotationsOff));
        }
        out.writeInt(methodIdx);
        out.writeInt(annotationsOff);
    }

    @Override
    public String toHuman() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.method.toHuman());
        sb.append(": ");
        boolean first = true;
        for (AnnotationSetRefItem item : this.annotationsItem.getItems()) {
            if (first) {
                first = false;
            } else {
                sb.append(", ");
            }
            sb.append(item.toHuman());
        }
        return sb.toString();
    }

    public CstMethodRef getMethod() {
        return this.method;
    }

    public AnnotationsList getAnnotationsList() {
        return this.annotationsList;
    }
}

