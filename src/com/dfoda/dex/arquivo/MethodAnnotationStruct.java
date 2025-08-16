package com.dfoda.dex.arquivo;

import com.dfoda.otimizadores.rop.cst.CstMethodRef;
import com.dfoda.util.ToHuman;
import com.dfoda.util.AnnotatedOutput;
import com.dfoda.util.Hex;
import com.dfoda.otimizadores.rop.anotacao.Annotations;

public final class MethodAnnotationStruct implements ToHuman, Comparable<MethodAnnotationStruct> {
    private final CstMethodRef method;
    private AnnotationSetItem annotations;

    public MethodAnnotationStruct(CstMethodRef method, AnnotationSetItem annotations) {
        if (method == null) {
            throw new NullPointerException("method == null");
        }
        if (annotations == null) {
            throw new NullPointerException("annotations == null");
        }
        this.method = method;
        this.annotations = annotations;
    }

    public int hashCode() {
        return this.method.hashCode();
    }

    public boolean equals(Object other) {
        if (!(other instanceof MethodAnnotationStruct)) {
            return false;
        }
        return this.method.equals(((MethodAnnotationStruct)other).method);
    }

    @Override
    public int compareTo(MethodAnnotationStruct other) {
        return this.method.compareTo(other.method);
    }

    public void addContents(DexFile file) {
        MethodIdsSection methodIds = file.getMethodIds();
        MixedItemSection wordData = file.getWordData();
        methodIds.intern(this.method);
        this.annotations = wordData.intern(this.annotations);
    }

    public void writeTo(DexFile file, AnnotatedOutput out) {
        int methodIdx = file.getMethodIds().indexOf(this.method);
        int annotationsOff = this.annotations.getAbsoluteOffset();
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
        return this.method.toHuman() + ": " + this.annotations;
    }

    public CstMethodRef getMethod() {
        return this.method;
    }

    public Annotations getAnnotations() {
        return this.annotations.getAnnotations();
    }
}

