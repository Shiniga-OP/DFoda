package com.dfoda.dex.arquivo;

import com.dfoda.otimizadores.rop.anotacao.Annotation;
import com.dfoda.otimizadores.rop.anotacao.AnnotationVisibility;
import com.dfoda.otimizadores.rop.anotacao.NameValuePair;
import com.dfoda.otimizadores.rop.cst.Constant;
import com.dfoda.otimizadores.rop.cst.CstString;
import com.dfoda.util.AnnotatedOutput;
import com.dfoda.util.ByteArrayAnnotatedOutput;
import java.util.Arrays;
import java.util.Comparator;

public final class AnnotationItem extends OffsettedItem {
    public static final int VISIBILITY_BUILD = 0;
    public static final int VISIBILITY_RUNTIME = 1;
    public static final int VISIBILITY_SYSTEM = 2;
    public static final int ALIGNMENT = 1;
    public static final TypeIdSorter TYPE_ID_SORTER = new TypeIdSorter();
    public final Annotation annotation;
    public TypeIdItem type;
    public byte[] encodedForm;

    public static void sortByTypeIdIndex(AnnotationItem[] array) {
        Arrays.sort(array, TYPE_ID_SORTER);
    }

    public AnnotationItem(Annotation annotation, DexFile dexFile) {
        super(1, -1);
        if(annotation == null) throw new NullPointerException("annotation == null");
        this.annotation = annotation;
        this.type = null;
        this.encodedForm = null;
        this.addContents(dexFile);
    }

    @Override
    public ItemType itemType() {
        return ItemType.TYPE_ANNOTATION_ITEM;
    }

    public int hashCode() {
        return this.annotation.hashCode();
    }

    @Override
    protected int compareTo0(OffsettedItem other) {
        AnnotationItem otherAnnotation = (AnnotationItem)other;
        return this.annotation.compareTo(otherAnnotation.annotation);
    }

    @Override
    public String toHuman() {
        return this.annotation.toHuman();
    }

    @Override
    public void addContents(DexFile file) {
        this.type = file.getTypeIds().intern(this.annotation.getType());
        ValueEncoder.addContents(file, this.annotation);
    }

    @Override
    protected void place0(Section addedTo, int offset) {
        ByteArrayAnnotatedOutput out = new ByteArrayAnnotatedOutput();
        ValueEncoder encoder = new ValueEncoder(addedTo.getFile(), out);
        encoder.writeAnnotation(this.annotation, false);
        this.encodedForm = out.praByteArray();
        this.setWriteSize(this.encodedForm.length + 1);
    }

    public void annotateTo(AnnotatedOutput out, String prefix) {
        out.annotate(0, prefix + "visibility: " + this.annotation.getVisibility().toHuman());
        out.annotate(0, prefix + "type: " + this.annotation.getType().toHuman());
        for (NameValuePair pair : this.annotation.getNameValuePairs()) {
            CstString name = pair.getName();
            Constant value = pair.getValue();
            out.annotate(0, prefix + name.toHuman() + ": " + ValueEncoder.constantToHuman(value));
        }
    }

    @Override
    protected void writeTo0(DexFile file, AnnotatedOutput out) {
        boolean annotates = out.annotates();
        AnnotationVisibility visibility = this.annotation.getVisibility();
        if (annotates) {
            out.annotate(0, this.offsetString() + " annotation");
            out.annotate(1, "  visibility: VISBILITY_" + visibility);
        }
        switch (visibility) {
            case BUILD: {
                out.writeByte(0);
                break;
            }
            case RUNTIME: {
                out.writeByte(1);
                break;
            }
            case SYSTEM: {
                out.writeByte(2);
                break;
            }
            default: {
                throw new RuntimeException("shouldn't happen");
            }
        }
        if (annotates) {
            ValueEncoder encoder = new ValueEncoder(file, out);
            encoder.writeAnnotation(this.annotation, true);
        } else {
            out.write(this.encodedForm);
        }
    }

    private static class TypeIdSorter
    implements Comparator<AnnotationItem> {
        private TypeIdSorter() {
        }

        @Override
        public int compare(AnnotationItem item1, AnnotationItem item2) {
            int index2;
            int index1 = item1.type.getIndex();
            if (index1 < (index2 = item2.type.getIndex())) {
                return -1;
            }
            if (index1 > index2) {
                return 1;
            }
            return 0;
        }
    }
}

