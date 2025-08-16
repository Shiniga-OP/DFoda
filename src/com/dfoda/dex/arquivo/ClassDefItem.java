package com.dfoda.dex.arquivo;

import com.dfoda.dex.arquivo.AnnotationsDirectoryItem;
import com.dfoda.dex.arquivo.ClassDataItem;
import com.dfoda.dex.arquivo.DexFile;
import com.dfoda.dex.arquivo.EncodedArrayItem;
import com.dfoda.dex.arquivo.EncodedField;
import com.dfoda.dex.arquivo.EncodedMethod;
import com.dfoda.dex.arquivo.IndexedItem;
import com.dfoda.dex.arquivo.ItemType;
import com.dfoda.dex.arquivo.MixedItemSection;
import com.dfoda.dex.arquivo.OffsettedItem;
import com.dfoda.dex.arquivo.StringIdsSection;
import com.dfoda.dex.arquivo.TypeIdsSection;
import com.dfoda.dex.arquivo.TypeListItem;
import com.dfoda.otimizadores.rop.anotacao.Annotations;
import com.dfoda.otimizadores.rop.anotacao.AnnotationsList;
import com.dfoda.otimizadores.rop.codigo.AccessFlags;
import com.dfoda.otimizadores.rop.cst.Constant;
import com.dfoda.otimizadores.rop.cst.CstArray;
import com.dfoda.otimizadores.rop.cst.CstFieldRef;
import com.dfoda.otimizadores.rop.cst.CstMethodRef;
import com.dfoda.otimizadores.rop.cst.CstString;
import com.dfoda.otimizadores.rop.cst.CstType;
import com.dfoda.otimizadores.rop.tipo.StdTypeList;
import com.dfoda.otimizadores.rop.tipo.TypeList;
import com.dfoda.util.AnnotatedOutput;
import com.dfoda.util.Hex;
import com.dfoda.util.Writers;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;

public final class ClassDefItem extends IndexedItem {
    private final CstType thisClass;
    private final int accessFlags;
    private final CstType superclass;
    private TypeListItem interfaces;
    private final CstString sourceFile;
    private final ClassDataItem classData;
    private EncodedArrayItem staticValuesItem;
    private AnnotationsDirectoryItem annotationsDirectory;

    public ClassDefItem(CstType thisClass, int accessFlags, CstType superclass, TypeList interfaces, CstString sourceFile) {
        if (thisClass == null) {
            throw new NullPointerException("thisClass == null");
        }
        if (interfaces == null) {
            throw new NullPointerException("interfaces == null");
        }
        this.thisClass = thisClass;
        this.accessFlags = accessFlags;
        this.superclass = superclass;
        this.interfaces = interfaces.size() == 0 ? null : new TypeListItem(interfaces);
        this.sourceFile = sourceFile;
        this.classData = new ClassDataItem(thisClass);
        this.staticValuesItem = null;
        this.annotationsDirectory = new AnnotationsDirectoryItem();
    }

    @Override
    public ItemType itemType() {
        return ItemType.TYPE_CLASS_DEF_ITEM;
    }

    @Override
    public int writeSize() {
        return 32;
    }

    @Override
    public void addContents(DexFile file) {
        TypeIdsSection typeIds = file.getTypeIds();
        MixedItemSection byteData = file.getByteData();
        MixedItemSection wordData = file.getWordData();
        MixedItemSection typeLists = file.getTypeLists();
        StringIdsSection stringIds = file.getStringIds();
        typeIds.intern(this.thisClass);
        if (!this.classData.isEmpty()) {
            MixedItemSection classDataSection = file.getClassData();
            classDataSection.add(this.classData);
            CstArray staticValues = this.classData.getStaticValuesConstant();
            if (staticValues != null) {
                this.staticValuesItem = byteData.intern(new EncodedArrayItem(staticValues));
            }
        }
        if (this.superclass != null) {
            typeIds.intern(this.superclass);
        }
        if (this.interfaces != null) {
            this.interfaces = typeLists.intern(this.interfaces);
        }
        if (this.sourceFile != null) {
            stringIds.intern(this.sourceFile);
        }
        if (!this.annotationsDirectory.isEmpty()) {
            if (this.annotationsDirectory.isInternable()) {
                this.annotationsDirectory = wordData.intern(this.annotationsDirectory);
            } else {
                wordData.add(this.annotationsDirectory);
            }
        }
    }

    @Override
    public void writeTo(DexFile file, AnnotatedOutput out) {
        boolean annotates = out.annotates();
        TypeIdsSection typeIds = file.getTypeIds();
        int classIdx = typeIds.indexOf(this.thisClass);
        int superIdx = this.superclass == null ? -1 : typeIds.indexOf(this.superclass);
        int interOff = OffsettedItem.getAbsoluteOffsetOr0(this.interfaces);
        int annoOff = this.annotationsDirectory.isEmpty() ? 0 : this.annotationsDirectory.getAbsoluteOffset();
        int sourceFileIdx = this.sourceFile == null ? -1 : file.getStringIds().indexOf(this.sourceFile);
        int dataOff = this.classData.isEmpty() ? 0 : this.classData.getAbsoluteOffset();
        int staticValuesOff = OffsettedItem.getAbsoluteOffsetOr0(this.staticValuesItem);
        if (annotates) {
            out.annotate(0, this.indexString() + ' ' + this.thisClass.toHuman());
            out.annotate(4, "  class_idx:           " + Hex.u4(classIdx));
            out.annotate(4, "  access_flags:        " + AccessFlags.classString(this.accessFlags));
            out.annotate(4, "  superclass_idx:      " + Hex.u4(superIdx) + " // " + (this.superclass == null ? "<none>" : this.superclass.toHuman()));
            out.annotate(4, "  interfaces_off:      " + Hex.u4(interOff));
            if (interOff != 0) {
                TypeList list = this.interfaces.getList();
                int sz = list.size();
                for (int i = 0; i < sz; ++i) {
                    out.annotate(0, "    " + list.getType(i).toHuman());
                }
            }
            out.annotate(4, "  source_file_idx:     " + Hex.u4(sourceFileIdx) + " // " + (this.sourceFile == null ? "<none>" : this.sourceFile.toHuman()));
            out.annotate(4, "  annotations_off:     " + Hex.u4(annoOff));
            out.annotate(4, "  class_data_off:      " + Hex.u4(dataOff));
            out.annotate(4, "  static_values_off:   " + Hex.u4(staticValuesOff));
        }
        out.writeInt(classIdx);
        out.writeInt(this.accessFlags);
        out.writeInt(superIdx);
        out.writeInt(interOff);
        out.writeInt(sourceFileIdx);
        out.writeInt(annoOff);
        out.writeInt(dataOff);
        out.writeInt(staticValuesOff);
    }

    public CstType getThisClass() {
        return this.thisClass;
    }

    public int getAccessFlags() {
        return this.accessFlags;
    }

    public CstType getSuperclass() {
        return this.superclass;
    }

    public TypeList getInterfaces() {
        if (this.interfaces == null) {
            return StdTypeList.EMPTY;
        }
        return this.interfaces.getList();
    }

    public CstString getSourceFile() {
        return this.sourceFile;
    }

    public void addStaticField(EncodedField field, Constant value) {
        this.classData.addStaticField(field, value);
    }

    public void addInstanceField(EncodedField field) {
        this.classData.addInstanceField(field);
    }

    public void addDirectMethod(EncodedMethod method) {
        this.classData.addDirectMethod(method);
    }

    public void addVirtualMethod(EncodedMethod method) {
        this.classData.addVirtualMethod(method);
    }

    public ArrayList<EncodedMethod> getMethods() {
        return this.classData.getMethods();
    }

    public void setClassAnnotations(Annotations annotations, DexFile dexFile) {
        this.annotationsDirectory.setClassAnnotations(annotations, dexFile);
    }

    public void addFieldAnnotations(CstFieldRef field, Annotations annotations, DexFile dexFile) {
        this.annotationsDirectory.addFieldAnnotations(field, annotations, dexFile);
    }

    public void addMethodAnnotations(CstMethodRef method, Annotations annotations, DexFile dexFile) {
        this.annotationsDirectory.addMethodAnnotations(method, annotations, dexFile);
    }

    public void addParameterAnnotations(CstMethodRef method, AnnotationsList list, DexFile dexFile) {
        this.annotationsDirectory.addParameterAnnotations(method, list, dexFile);
    }

    public Annotations getMethodAnnotations(CstMethodRef method) {
        return this.annotationsDirectory.getMethodAnnotations(method);
    }

    public AnnotationsList getParameterAnnotations(CstMethodRef method) {
        return this.annotationsDirectory.getParameterAnnotations(method);
    }

    public void debugPrint(Writer out, boolean verbose) {
        PrintWriter pw = Writers.printWriterFor(out);
        pw.println(this.getClass().getName() + " {");
        pw.println("  accessFlags: " + Hex.u2(this.accessFlags));
        pw.println("  superclass: " + this.superclass);
        pw.println("  interfaces: " + (this.interfaces == null ? "<none>" : this.interfaces));
        pw.println("  sourceFile: " + (this.sourceFile == null ? "<none>" : this.sourceFile.toQuoted()));
        this.classData.debugPrint(out, verbose);
        this.annotationsDirectory.debugPrint(pw);
        pw.println("}");
    }
}

