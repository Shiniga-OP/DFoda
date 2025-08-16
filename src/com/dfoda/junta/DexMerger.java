package com.dfoda.junta;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import com.dex.Dex;
import com.dfoda.dexer.DFodaCtx;
import com.dex.TableOfContents;
import com.dex.TypeList;
import com.dex.ProtoId;
import com.dex.CallSiteId;
import com.dex.MethodHandle;
import com.dex.FieldId;
import com.dex.MethodId;
import com.dex.Annotation;
import com.dex.ClassDef;
import com.dex.ClassData;
import com.dex.Code;
import com.dex.util.ErroCtx;

public final class DexMerger {
    public final Dex[] dexes;
    public final IndexMap[] indexMaps;
    public final CollisionPolicy collisionPolicy;
    public final DFodaCtx ctx;
    private final WriterSizes writerSizes;
    private final Dex dexOut;
    private final Dex.Section headerOut;
    private final Dex.Section idsDefsOut;
    private final Dex.Section mapListOut;
    private final Dex.Section typeListOut;
    private final Dex.Section classDataOut;
    private final Dex.Section codeOut;
    private final Dex.Section stringDataOut;
    private final Dex.Section debugInfoOut;
    private final Dex.Section encodedArrayOut;
    private final Dex.Section annotationsDirectoryOut;
    private final Dex.Section annotationSetOut;
    private final Dex.Section annotationSetRefListOut;
    private final Dex.Section annotationOut;
    private final TableOfContents contentsOut;
    private final InstructionTransformer instructionTransformer;
    private int compactWasteThreshold = 0x100000;

    public DexMerger(Dex[] dexes, CollisionPolicy collisionPolicy, DFodaCtx context) throws IOException {
        this(dexes, collisionPolicy, context, new WriterSizes(dexes));
    }

    private DexMerger(Dex[] dexes, CollisionPolicy collisionPolicy, DFodaCtx context, WriterSizes writerSizes) throws IOException {
        this.dexes = dexes;
        this.collisionPolicy = collisionPolicy;
        this.ctx = context;
        this.writerSizes = writerSizes;
        this.dexOut = new Dex(writerSizes.size());
        this.indexMaps = new IndexMap[dexes.length];
        for (int i = 0; i < dexes.length; ++i) {
            this.indexMaps[i] = new IndexMap(this.dexOut, dexes[i].getTableOfContents());
        }
        this.instructionTransformer = new InstructionTransformer();
        this.headerOut = this.dexOut.appendSection(writerSizes.header, "header");
        this.idsDefsOut = this.dexOut.appendSection(writerSizes.idsDefs, "ids defs");
        this.contentsOut = this.dexOut.getTableOfContents();
        this.contentsOut.dataOff = this.dexOut.getNextSectionStart();
        this.contentsOut.mapList.off = this.dexOut.getNextSectionStart();
        this.contentsOut.mapList.tam = 1;
        this.mapListOut = this.dexOut.appendSection(writerSizes.mapList, "map list");
        this.contentsOut.typeLists.off = this.dexOut.getNextSectionStart();
        this.typeListOut = this.dexOut.appendSection(writerSizes.typeList, "type list");
        this.contentsOut.annotationSetRefLists.off = this.dexOut.getNextSectionStart();
        this.annotationSetRefListOut = this.dexOut.appendSection(writerSizes.annotationsSetRefList, "annotation set ref list");
        this.contentsOut.annotationSets.off = this.dexOut.getNextSectionStart();
        this.annotationSetOut = this.dexOut.appendSection(writerSizes.annotationsSet, "annotation sets");
        this.contentsOut.classDatas.off = this.dexOut.getNextSectionStart();
        this.classDataOut = this.dexOut.appendSection(writerSizes.classData, "class data");
        this.contentsOut.codes.off = this.dexOut.getNextSectionStart();
        this.codeOut = this.dexOut.appendSection(writerSizes.code, "code");
        this.contentsOut.stringDatas.off = this.dexOut.getNextSectionStart();
        this.stringDataOut = this.dexOut.appendSection(writerSizes.stringData, "string data");
        this.contentsOut.debugInfos.off = this.dexOut.getNextSectionStart();
        this.debugInfoOut = this.dexOut.appendSection(writerSizes.debugInfo, "debug info");
        this.contentsOut.annotations.off = this.dexOut.getNextSectionStart();
        this.annotationOut = this.dexOut.appendSection(writerSizes.annotation, "annotation");
        this.contentsOut.encodedArrays.off = this.dexOut.getNextSectionStart();
        this.encodedArrayOut = this.dexOut.appendSection(writerSizes.encodedArray, "encoded array");
        this.contentsOut.annotationsDirectories.off = this.dexOut.getNextSectionStart();
        this.annotationsDirectoryOut = this.dexOut.appendSection(writerSizes.annotationsDirectory, "annotations directory");
        this.contentsOut.dataSize = this.dexOut.getNextSectionStart() - this.contentsOut.dataOff;
    }

    public void setCompactWasteThreshold(int compactWasteThreshold) {
        this.compactWasteThreshold = compactWasteThreshold;
    }

    private Dex mergeDexes() throws IOException {
        this.mergeStringIds();
        this.mergeTypeIds();
        this.mergeTypeLists();
        this.mergeProtoIds();
        this.mergeFieldIds();
        this.mergeMethodIds();
        this.mergeMethodHandles();
        this.mergeAnnotations();
        this.unionAnnotationSetsAndDirectories();
        this.mergeCallSiteIds();
        this.mergeClassDefs();
        Arrays.sort(this.contentsOut.sessoes);
        this.contentsOut.header.off = 0;
        this.contentsOut.header.tam = 1;
        this.contentsOut.fileSize = this.dexOut.getLength();
        this.contentsOut.computeSizesFromOffsets();
        this.contentsOut.writeHeader(this.headerOut, this.mergeApiLevels());
        this.contentsOut.writeMap(this.mapListOut);
        this.dexOut.writeHashes();
        return this.dexOut;
    }

    public Dex merge() throws IOException {
        if(this.dexes.length == 1) return this.dexes[0];
        if(this.dexes.length == 0) return null;
        long start = System.nanoTime();
        Dex result = this.mergeDexes();
        WriterSizes compactedSizes = new WriterSizes(this);
        int wastedByteCount = this.writerSizes.size() - compactedSizes.size();
        if(wastedByteCount > this.compactWasteThreshold) {
            DexMerger compacter = new DexMerger(new Dex[]{this.dexOut, new Dex(0)}, CollisionPolicy.FAIL, this.ctx, compactedSizes);
            result = compacter.mergeDexes();
            this.ctx.saida.printf("Result compacted from %.1fKiB to %.1fKiB to save %.1fKiB%n", Float.valueOf((float)this.dexOut.getLength() / 1024.0f), Float.valueOf((float)result.getLength() / 1024.0f), Float.valueOf((float)wastedByteCount / 1024.0f));
        }
        long elapsed = System.nanoTime() - start;
        for(int i = 0; i < this.dexes.length; ++i) this.ctx.saida.printf("Merged dex #%d (%d defs/%.1fKiB)%n", i + 1, this.dexes[i].getTableOfContents().classDefs.tam, Float.valueOf((float)this.dexes[i].getLength() / 1024.0f));
        this.ctx.saida.printf("Result is %d defs/%.1fKiB. Took %.1fs%n", result.getTableOfContents().classDefs.tam, Float.valueOf((float)result.getLength() / 1024.0f), Float.valueOf((float)elapsed / 1.0E9f));
        return result;
    }

    public int mergeApiLevels() {
        int maxApi = -1;
        for(int i = 0; i < this.dexes.length; ++i) {
            int dexMinApi = this.dexes[i].getTableOfContents().apiNivel;
            if(maxApi >= dexMinApi) continue;
            maxApi = dexMinApi;
        }
        return maxApi;
    }

    public void mergeStringIds() {
        new IdMerger<String>(this.idsDefsOut){
            @Override
            public TableOfContents.Sessao getSection(TableOfContents tableOfContents) {
                return tableOfContents.stringIds;
            }

            @Override
            public String read(Dex.Section in, IndexMap indexMap, int index) {
                return in.readString();
            }

            @Override
            public void updateIndex(int offset, IndexMap indexMap, int oldIndex, int newIndex) {
                indexMap.stringIds[oldIndex] = newIndex;
            }

            @Override
            public void write(String value) {
                ++DexMerger.this.contentsOut.stringDatas.tam;
                DexMerger.this.idsDefsOut.writeInt(DexMerger.this.stringDataOut.getPosition());
                DexMerger.this.stringDataOut.writeStringData(value);
            }
        }.mergeSorted();
    }

    public void mergeTypeIds() {
        new IdMerger<Integer>(this.idsDefsOut){

            @Override
            TableOfContents.Sessao getSection(TableOfContents tableOfContents) {
                return tableOfContents.typeIds;
            }

            @Override
            Integer read(Dex.Section in, IndexMap indexMap, int index) {
                int stringIndex = in.readInt();
                return indexMap.adjustString(stringIndex);
            }

            @Override
            void updateIndex(int offset, IndexMap indexMap, int oldIndex, int newIndex) {
                if(newIndex < 0 || newIndex > 65535) throw new ErroCtx("type ID not in [0, 0xffff]: " + newIndex);
                indexMap.typeIds[oldIndex] = (short)newIndex;
            }

            @Override
            void write(Integer value) {
                DexMerger.this.idsDefsOut.writeInt(value);
            }
        }.mergeSorted();
    }

    private void mergeTypeLists() {
        new IdMerger<TypeList>(this.typeListOut){

            @Override
            TableOfContents.Sessao getSection(TableOfContents tableOfContents) {
                return tableOfContents.typeLists;
            }

            @Override
            TypeList read(Dex.Section in, IndexMap indexMap, int index) {
                return indexMap.adjustTypeList(in.readTypeList());
            }

            @Override
            void updateIndex(int offset, IndexMap indexMap, int oldIndex, int newIndex) {
                indexMap.putTypeListOffset(offset, DexMerger.this.typeListOut.getPosition());
            }

            @Override
            void write(TypeList value) {
                DexMerger.this.typeListOut.writeTypeList(value);
            }
        }.mergeUnsorted();
    }

    private void mergeProtoIds() {
        new IdMerger<ProtoId>(this.idsDefsOut){

            @Override
            TableOfContents.Sessao getSection(TableOfContents tableOfContents) {
                return tableOfContents.protoIds;
            }

            @Override
            ProtoId read(Dex.Section in, IndexMap indexMap, int index) {
                return indexMap.adjust(in.readProtoId());
            }

            @Override
            void updateIndex(int offset, IndexMap indexMap, int oldIndex, int newIndex) {
                if (newIndex < 0 || newIndex > 65535) {
                    throw new ErroCtx("proto ID not in [0, 0xffff]: " + newIndex);
                }
                indexMap.protoIds[oldIndex] = (short)newIndex;
            }

            @Override
            void write(ProtoId value) {
                value.writeTo(DexMerger.this.idsDefsOut);
            }
        }.mergeSorted();
    }

    private void mergeCallSiteIds() {
        new IdMerger<CallSiteId>(this.idsDefsOut){

            @Override
            TableOfContents.Sessao getSection(TableOfContents tableOfContents) {
                return tableOfContents.callSiteIds;
            }

            @Override
            CallSiteId read(Dex.Section in, IndexMap indexMap, int index) {
                return indexMap.adjust(in.readCallSiteId());
            }

            @Override
            void updateIndex(int offset, IndexMap indexMap, int oldIndex, int newIndex) {
                indexMap.callSiteIds[oldIndex] = newIndex;
            }

            @Override
            void write(CallSiteId value) {
                value.writeTo(DexMerger.this.idsDefsOut);
            }
        }.mergeSorted();
    }

    private void mergeMethodHandles() {
        new IdMerger<MethodHandle>(this.idsDefsOut){

            @Override
            TableOfContents.Sessao getSection(TableOfContents tableOfContents) {
                return tableOfContents.methodHandles;
            }

            @Override
            MethodHandle read(Dex.Section in, IndexMap indexMap, int index) {
                return indexMap.adjust(in.readMethodHandle());
            }

            @Override
            void updateIndex(int offset, IndexMap indexMap, int oldIndex, int newIndex) {
                indexMap.methodHandleIds.put(oldIndex, indexMap.methodHandleIds.size());
            }

            @Override
            void write(MethodHandle value) {
                value.writeTo(DexMerger.this.idsDefsOut);
            }
        }.mergeUnsorted();
    }

    private void mergeFieldIds() {
        new IdMerger<FieldId>(this.idsDefsOut){

            @Override
            TableOfContents.Sessao getSection(TableOfContents tableOfContents) {
                return tableOfContents.fieldIds;
            }

            @Override
            FieldId read(Dex.Section in, IndexMap indexMap, int index) {
                return indexMap.adjust(in.readFieldId());
            }

            @Override
            void updateIndex(int offset, IndexMap indexMap, int oldIndex, int newIndex) {
                if (newIndex < 0 || newIndex > 65535) {
                    throw new ErroCtx("field ID not in [0, 0xffff]: " + newIndex);
                }
                indexMap.fieldIds[oldIndex] = (short)newIndex;
            }

            @Override
            void write(FieldId value) {
                value.writeTo(DexMerger.this.idsDefsOut);
            }
        }.mergeSorted();
    }

    private void mergeMethodIds() {
        new IdMerger<MethodId>(this.idsDefsOut){

            @Override
            TableOfContents.Sessao getSection(TableOfContents tableOfContents) {
                return tableOfContents.methodIds;
            }

            @Override
            MethodId read(Dex.Section in, IndexMap indexMap, int index) {
                return indexMap.adjust(in.readMethodId());
            }

            @Override
            void updateIndex(int offset, IndexMap indexMap, int oldIndex, int newIndex) {
                if (newIndex < 0 || newIndex > 65535) {
                    throw new ErroCtx("method ID not in [0, 0xffff]: " + newIndex);
                }
                indexMap.methodIds[oldIndex] = (short)newIndex;
            }

            @Override
            void write(MethodId methodId) {
                methodId.writeTo(DexMerger.this.idsDefsOut);
            }
        }.mergeSorted();
    }

    private void mergeAnnotations() {
        new IdMerger<Annotation>(this.annotationOut){

            @Override
            TableOfContents.Sessao getSection(TableOfContents tableOfContents) {
                return tableOfContents.annotations;
            }

            @Override
            Annotation read(Dex.Section in, IndexMap indexMap, int index) {
                return indexMap.adjust(in.readAnnotation());
            }

            @Override
            void updateIndex(int offset, IndexMap indexMap, int oldIndex, int newIndex) {
                indexMap.putAnnotationOffset(offset, DexMerger.this.annotationOut.getPosition());
            }

            @Override
            void write(Annotation value) {
                value.writeTo(DexMerger.this.annotationOut);
            }
        }.mergeUnsorted();
    }

    private void mergeClassDefs() {
        SortableType[] types = this.getSortedTypes();
        this.contentsOut.classDefs.off = this.idsDefsOut.getPosition();
        this.contentsOut.classDefs.tam = types.length;
        for (SortableType type : types) {
            Dex in = type.getDex();
            this.transformClassDef(in, type.getClassDef(), type.getIndexMap());
        }
    }

    private SortableType[] getSortedTypes() {
        boolean allDone;
        SortableType[] sortableTypes = new SortableType[this.contentsOut.typeIds.tam];
        for (int i = 0; i < this.dexes.length; ++i) {
            this.readSortableTypes(sortableTypes, this.dexes[i], this.indexMaps[i]);
        }
        do {
            allDone = true;
            for (SortableType sortableType : sortableTypes) {
                if (sortableType == null || sortableType.isDepthAssigned()) continue;
                allDone &= sortableType.tryAssignDepth(sortableTypes);
            }
        } while (!allDone);
        Arrays.sort(sortableTypes, SortableType.NULLS_LAST_ORDER);
        int firstNull = Arrays.asList(sortableTypes).indexOf(null);
        return firstNull != -1 ? Arrays.copyOfRange(sortableTypes, 0, firstNull) : sortableTypes;
    }

    private void readSortableTypes(SortableType[] sortableTypes, Dex buffer, IndexMap indexMap) {
        for (ClassDef classDef : buffer.classDefs()) {
            SortableType sortableType = indexMap.adjust(new SortableType(buffer, indexMap, classDef));
            int t = sortableType.getTypeIndex();
            if (sortableTypes[t] == null) {
                sortableTypes[t] = sortableType;
                continue;
            }
            if (this.collisionPolicy == CollisionPolicy.KEEP_FIRST) continue;
            throw new ErroCtx("Multiple dex files define " + buffer.typeNames().get(classDef.getTypeIndex()));
        }
    }

    private void unionAnnotationSetsAndDirectories() {
        int i;
        for (i = 0; i < this.dexes.length; ++i) {
            this.transformAnnotationSets(this.dexes[i], this.indexMaps[i]);
        }
        for (i = 0; i < this.dexes.length; ++i) {
            this.transformAnnotationSetRefLists(this.dexes[i], this.indexMaps[i]);
        }
        for (i = 0; i < this.dexes.length; ++i) {
            this.transformAnnotationDirectories(this.dexes[i], this.indexMaps[i]);
        }
        for (i = 0; i < this.dexes.length; ++i) {
            this.transformStaticValues(this.dexes[i], this.indexMaps[i]);
        }
    }

    private void transformAnnotationSets(Dex in, IndexMap indexMap) {
        TableOfContents.Sessao section = in.getTableOfContents().annotationSets;
        if (section.existe()) {
            Dex.Section setIn = in.open(section.off);
            for (int i = 0; i < section.tam; ++i) {
                this.transformAnnotationSet(indexMap, setIn);
            }
        }
    }

    private void transformAnnotationSetRefLists(Dex in, IndexMap indexMap) {
        TableOfContents.Sessao section = in.getTableOfContents().annotationSetRefLists;
        if (section.existe()) {
            Dex.Section setIn = in.open(section.off);
            for (int i = 0; i < section.tam; ++i) {
                this.transformAnnotationSetRefList(indexMap, setIn);
            }
        }
    }

    private void transformAnnotationDirectories(Dex in, IndexMap indexMap) {
        TableOfContents.Sessao section = in.getTableOfContents().annotationsDirectories;
        if (section.existe()) {
            Dex.Section directoryIn = in.open(section.off);
            for (int i = 0; i < section.tam; ++i) {
                this.transformAnnotationDirectory(directoryIn, indexMap);
            }
        }
    }

    private void transformStaticValues(Dex in, IndexMap indexMap) {
        TableOfContents.Sessao section = in.getTableOfContents().encodedArrays;
        if (section.existe()) {
            Dex.Section staticValuesIn = in.open(section.off);
            for (int i = 0; i < section.tam; ++i) {
                this.transformStaticValues(staticValuesIn, indexMap);
            }
        }
    }

    private void transformClassDef(Dex in, ClassDef classDef, IndexMap indexMap) {
        this.idsDefsOut.assertFourByteAligned();
        this.idsDefsOut.writeInt(classDef.getTypeIndex());
        this.idsDefsOut.writeInt(classDef.getAccessFlags());
        this.idsDefsOut.writeInt(classDef.getSupertypeIndex());
        this.idsDefsOut.writeInt(classDef.getInterfacesOffset());
        int sourceFileIndex = indexMap.adjustString(classDef.getSourceFileIndex());
        this.idsDefsOut.writeInt(sourceFileIndex);
        int annotationsOff = classDef.getAnnotationsOffset();
        this.idsDefsOut.writeInt(indexMap.adjustAnnotationDirectory(annotationsOff));
        int classDataOff = classDef.getClassDataOffset();
        if (classDataOff == 0) {
            this.idsDefsOut.writeInt(0);
        } else {
            this.idsDefsOut.writeInt(this.classDataOut.getPosition());
            ClassData classData = in.readClassData(classDef);
            this.transformClassData(in, classData, indexMap);
        }
        int staticValuesOff = classDef.getStaticValuesOffset();
        this.idsDefsOut.writeInt(indexMap.adjustEncodedArray(staticValuesOff));
    }

    private void transformAnnotationDirectory(Dex.Section directoryIn, IndexMap indexMap) {
        int i;
        ++this.contentsOut.annotationsDirectories.tam;
        this.annotationsDirectoryOut.assertFourByteAligned();
        indexMap.putAnnotationDirectoryOffset(directoryIn.getPosition(), this.annotationsDirectoryOut.getPosition());
        int classAnnotationsOffset = indexMap.adjustAnnotationSet(directoryIn.readInt());
        this.annotationsDirectoryOut.writeInt(classAnnotationsOffset);
        int fieldsSize = directoryIn.readInt();
        this.annotationsDirectoryOut.writeInt(fieldsSize);
        int methodsSize = directoryIn.readInt();
        this.annotationsDirectoryOut.writeInt(methodsSize);
        int parameterListSize = directoryIn.readInt();
        this.annotationsDirectoryOut.writeInt(parameterListSize);
        for (i = 0; i < fieldsSize; ++i) {
            this.annotationsDirectoryOut.writeInt(indexMap.adjustField(directoryIn.readInt()));
            this.annotationsDirectoryOut.writeInt(indexMap.adjustAnnotationSet(directoryIn.readInt()));
        }
        for (i = 0; i < methodsSize; ++i) {
            this.annotationsDirectoryOut.writeInt(indexMap.adjustMethod(directoryIn.readInt()));
            this.annotationsDirectoryOut.writeInt(indexMap.adjustAnnotationSet(directoryIn.readInt()));
        }
        for (i = 0; i < parameterListSize; ++i) {
            this.annotationsDirectoryOut.writeInt(indexMap.adjustMethod(directoryIn.readInt()));
            this.annotationsDirectoryOut.writeInt(indexMap.adjustAnnotationSetRefList(directoryIn.readInt()));
        }
    }

    private void transformAnnotationSet(IndexMap indexMap, Dex.Section setIn) {
        ++this.contentsOut.annotationSets.tam;
        this.annotationSetOut.assertFourByteAligned();
        indexMap.putAnnotationSetOffset(setIn.getPosition(), this.annotationSetOut.getPosition());
        int size = setIn.readInt();
        this.annotationSetOut.writeInt(size);
        for (int j = 0; j < size; ++j) {
            this.annotationSetOut.writeInt(indexMap.adjustAnnotation(setIn.readInt()));
        }
    }

    private void transformAnnotationSetRefList(IndexMap indexMap, Dex.Section refListIn) {
        ++this.contentsOut.annotationSetRefLists.tam;
        this.annotationSetRefListOut.assertFourByteAligned();
        indexMap.putAnnotationSetRefListOffset(refListIn.getPosition(), this.annotationSetRefListOut.getPosition());
        int parameterCount = refListIn.readInt();
        this.annotationSetRefListOut.writeInt(parameterCount);
        for (int p = 0; p < parameterCount; ++p) {
            this.annotationSetRefListOut.writeInt(indexMap.adjustAnnotationSet(refListIn.readInt()));
        }
    }

    private void transformClassData(Dex in, ClassData classData, IndexMap indexMap) {
        ++this.contentsOut.classDatas.tam;
        ClassData.Field[] staticFields = classData.getStaticFields();
        ClassData.Field[] instanceFields = classData.getInstanceFields();
        ClassData.Method[] directMethods = classData.getDirectMethods();
        ClassData.Method[] virtualMethods = classData.getVirtualMethods();
        this.classDataOut.writeUleb128(staticFields.length);
        this.classDataOut.writeUleb128(instanceFields.length);
        this.classDataOut.writeUleb128(directMethods.length);
        this.classDataOut.writeUleb128(virtualMethods.length);
        this.transformFields(indexMap, staticFields);
        this.transformFields(indexMap, instanceFields);
        this.transformMethods(in, indexMap, directMethods);
        this.transformMethods(in, indexMap, virtualMethods);
    }

    private void transformFields(IndexMap indexMap, ClassData.Field[] fields) {
        int lastOutFieldIndex = 0;
        for (ClassData.Field field : fields) {
            int outFieldIndex = indexMap.adjustField(field.getFieldIndex());
            this.classDataOut.writeUleb128(outFieldIndex - lastOutFieldIndex);
            lastOutFieldIndex = outFieldIndex;
            this.classDataOut.writeUleb128(field.getAccessFlags());
        }
    }

    private void transformMethods(Dex in, IndexMap indexMap, ClassData.Method[] methods) {
        int lastOutMethodIndex = 0;
        for (ClassData.Method method : methods) {
            int outMethodIndex = indexMap.adjustMethod(method.getMethodIndex());
            this.classDataOut.writeUleb128(outMethodIndex - lastOutMethodIndex);
            lastOutMethodIndex = outMethodIndex;
            this.classDataOut.writeUleb128(method.getAccessFlags());
            if (method.getCodeOffset() == 0) {
                this.classDataOut.writeUleb128(0);
                continue;
            }
            this.codeOut.alignToFourBytesWithZeroFill();
            this.classDataOut.writeUleb128(this.codeOut.getPosition());
            this.transformCode(in, in.readCode(method), indexMap);
        }
    }

    private void transformCode(Dex in, Code code, IndexMap indexMap) {
        ++this.contentsOut.codes.tam;
        this.codeOut.assertFourByteAligned();
        this.codeOut.writeUnsignedShort(code.getRegistersSize());
        this.codeOut.writeUnsignedShort(code.getInsSize());
        this.codeOut.writeUnsignedShort(code.getOutsSize());
        Code.Try[] tries = code.getTries();
        Code.CatchHandler[] catchHandlers = code.getCatchHandlers();
        this.codeOut.writeUnsignedShort(tries.length);
        int debugInfoOffset = code.getDebugInfoOffset();
        if (debugInfoOffset != 0) {
            this.codeOut.writeInt(this.debugInfoOut.getPosition());
            this.transformDebugInfoItem(in.open(debugInfoOffset), indexMap);
        } else {
            this.codeOut.writeInt(0);
        }
        short[] instructions = code.getInstructions();
        short[] newInstructions = this.instructionTransformer.transform(indexMap, instructions);
        this.codeOut.writeInt(newInstructions.length);
        this.codeOut.write(newInstructions);
        if (tries.length > 0) {
            if (newInstructions.length % 2 == 1) {
                this.codeOut.writeShort((short)0);
            }
            Dex.Section triesSection = this.dexOut.open(this.codeOut.getPosition());
            this.codeOut.skip(tries.length * 8);
            int[] offsets = this.transformCatchHandlers(indexMap, catchHandlers);
            this.transformTries(triesSection, tries, offsets);
        }
    }

    private int[] transformCatchHandlers(IndexMap indexMap, Code.CatchHandler[] catchHandlers) {
        int baseOffset = this.codeOut.getPosition();
        this.codeOut.writeUleb128(catchHandlers.length);
        int[] offsets = new int[catchHandlers.length];
        for (int i = 0; i < catchHandlers.length; ++i) {
            offsets[i] = this.codeOut.getPosition() - baseOffset;
            this.transformEncodedCatchHandler(catchHandlers[i], indexMap);
        }
        return offsets;
    }

    private void transformTries(Dex.Section out, Code.Try[] tries, int[] catchHandlerOffsets) {
        for (Code.Try tryItem : tries) {
            out.writeInt(tryItem.getStartAddress());
            out.writeUnsignedShort(tryItem.getInstructionCount());
            out.writeUnsignedShort(catchHandlerOffsets[tryItem.getCatchHandlerIndex()]);
        }
    }

    private void transformDebugInfoItem(Dex.Section in, IndexMap indexMap) {
        ++this.contentsOut.debugInfos.tam;
        int lineStart = in.readUleb128();
        this.debugInfoOut.writeUleb128(lineStart);
        int parametersSize = in.readUleb128();
        this.debugInfoOut.writeUleb128(parametersSize);
        for (int p = 0; p < parametersSize; ++p) {
            int parameterName = in.readUleb128p1();
            this.debugInfoOut.writeUleb128p1(indexMap.adjustString(parameterName));
        }
        while (true) {
            byte opcode = in.readByte();
            this.debugInfoOut.writeByte(opcode);
            switch (opcode) {
                case 0: {
                    return;
                }
                case 1: {
                    int addrDiff = in.readUleb128();
                    this.debugInfoOut.writeUleb128(addrDiff);
                    break;
                }
                case 2: {
                    int lineDiff = in.readSleb128();
                    this.debugInfoOut.writeSleb128(lineDiff);
                    break;
                }
                case 3: 
                case 4: {
                    int registerNum = in.readUleb128();
                    this.debugInfoOut.writeUleb128(registerNum);
                    int nameIndex = in.readUleb128p1();
                    this.debugInfoOut.writeUleb128p1(indexMap.adjustString(nameIndex));
                    int typeIndex = in.readUleb128p1();
                    this.debugInfoOut.writeUleb128p1(indexMap.adjustType(typeIndex));
                    if (opcode != 4) break;
                    int sigIndex = in.readUleb128p1();
                    this.debugInfoOut.writeUleb128p1(indexMap.adjustString(sigIndex));
                    break;
                }
                case 5: 
                case 6: {
                    int registerNum = in.readUleb128();
                    this.debugInfoOut.writeUleb128(registerNum);
                    break;
                }
                case 9: {
                    int nameIndex = in.readUleb128p1();
                    this.debugInfoOut.writeUleb128p1(indexMap.adjustString(nameIndex));
                    break;
                }
            }
        }
    }

    private void transformEncodedCatchHandler(Code.CatchHandler catchHandler, IndexMap indexMap) {
        int catchAllAddress = catchHandler.getCatchAllAddress();
        int[] typeIndexes = catchHandler.getTypeIndexes();
        int[] addresses = catchHandler.getAddresses();
        if (catchAllAddress != -1) {
            this.codeOut.writeSleb128(-typeIndexes.length);
        } else {
            this.codeOut.writeSleb128(typeIndexes.length);
        }
        for (int i = 0; i < typeIndexes.length; ++i) {
            this.codeOut.writeUleb128(indexMap.adjustType(typeIndexes[i]));
            this.codeOut.writeUleb128(addresses[i]);
        }
        if (catchAllAddress != -1) {
            this.codeOut.writeUleb128(catchAllAddress);
        }
    }

    private void transformStaticValues(Dex.Section in, IndexMap indexMap) {
        ++this.contentsOut.encodedArrays.tam;
        indexMap.putEncodedArrayValueOffset(in.getPosition(), this.encodedArrayOut.getPosition());
        indexMap.adjustEncodedArray(in.readEncodedArray()).writeTo(this.encodedArrayOut);
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            DexMerger.printUsage();
            return;
        }
        Dex[] dexes = new Dex[args.length - 1];
        for (int i = 1; i < args.length; ++i) {
            dexes[i - 1] = new Dex(new File(args[i]));
        }
        Dex merged = new DexMerger(dexes, CollisionPolicy.KEEP_FIRST, new DFodaCtx()).merge();
        merged.writeTo(new File(args[0]));
    }

    private static void printUsage() {
        System.out.println("Usage: DexMerger <out.dex> <a.dex> <b.dex> ...");
        System.out.println();
        System.out.println("If a class is defined in several dex, the class found in the first dex will be used.");
    }

    private static class WriterSizes {
        private int header = 112;
        private int idsDefs;
        private int mapList;
        private int typeList;
        private int classData;
        private int code;
        private int stringData;
        private int debugInfo;
        private int encodedArray;
        private int annotationsDirectory;
        private int annotationsSet;
        private int annotationsSetRefList;
        private int annotation;

        public WriterSizes(Dex[] dexes) {
            for(int i = 0; i < dexes.length; ++i) this.plus(dexes[i].getTableOfContents(), false);
            this.fourByteAlign();
        }

        public WriterSizes(DexMerger dexMerger) {
            this.header = dexMerger.headerOut.used();
            this.idsDefs = dexMerger.idsDefsOut.used();
            this.mapList = dexMerger.mapListOut.used();
            this.typeList = dexMerger.typeListOut.used();
            this.classData = dexMerger.classDataOut.used();
            this.code = dexMerger.codeOut.used();
            this.stringData = dexMerger.stringDataOut.used();
            this.debugInfo = dexMerger.debugInfoOut.used();
            this.encodedArray = dexMerger.encodedArrayOut.used();
            this.annotationsDirectory = dexMerger.annotationsDirectoryOut.used();
            this.annotationsSet = dexMerger.annotationSetOut.used();
            this.annotationsSetRefList = dexMerger.annotationSetRefListOut.used();
            this.annotation = dexMerger.annotationOut.used();
            this.fourByteAlign();
        }

        private void plus(TableOfContents contents, boolean exact) {
            this.idsDefs += contents.stringIds.tam * 4 + contents.typeIds.tam * 4 + contents.protoIds.tam * 12 + contents.fieldIds.tam * 8 + contents.methodIds.tam * 8 + contents.classDefs.tam * 32;
            this.mapList = 4 + contents.sessoes.length * 12;
            this.typeList += WriterSizes.fourByteAlign(contents.typeLists.byteCount);
            this.stringData += contents.stringDatas.byteCount;
            this.annotationsDirectory += contents.annotationsDirectories.byteCount;
            this.annotationsSet += contents.annotationSets.byteCount;
            this.annotationsSetRefList += contents.annotationSetRefLists.byteCount;
            if (exact) {
                this.code += contents.codes.byteCount;
                this.classData += contents.classDatas.byteCount;
                this.encodedArray += contents.encodedArrays.byteCount;
                this.annotation += contents.annotations.byteCount;
                this.debugInfo += contents.debugInfos.byteCount;
            } else {
                this.code += (int)Math.ceil((double)contents.codes.byteCount * 1.25);
                this.classData += (int)Math.ceil((double)contents.classDatas.byteCount * 1.67);
                this.encodedArray += contents.encodedArrays.byteCount * 2;
                this.annotation += (int)Math.ceil(contents.annotations.byteCount * 2);
                this.debugInfo += contents.debugInfos.byteCount * 2 + 8;
            }
        }

        private void fourByteAlign() {
            this.header = WriterSizes.fourByteAlign(this.header);
            this.idsDefs = WriterSizes.fourByteAlign(this.idsDefs);
            this.mapList = WriterSizes.fourByteAlign(this.mapList);
            this.typeList = WriterSizes.fourByteAlign(this.typeList);
            this.classData = WriterSizes.fourByteAlign(this.classData);
            this.code = WriterSizes.fourByteAlign(this.code);
            this.stringData = WriterSizes.fourByteAlign(this.stringData);
            this.debugInfo = WriterSizes.fourByteAlign(this.debugInfo);
            this.encodedArray = WriterSizes.fourByteAlign(this.encodedArray);
            this.annotationsDirectory = WriterSizes.fourByteAlign(this.annotationsDirectory);
            this.annotationsSet = WriterSizes.fourByteAlign(this.annotationsSet);
            this.annotationsSetRefList = WriterSizes.fourByteAlign(this.annotationsSetRefList);
            this.annotation = WriterSizes.fourByteAlign(this.annotation);
        }

        private static int fourByteAlign(int position) {
            return position + 3 & 0xFFFFFFFC;
        }

        public int size() {
            return this.header + this.idsDefs + this.mapList + this.typeList + this.classData + this.code + this.stringData + this.debugInfo + this.encodedArray + this.annotationsDirectory + this.annotationsSet + this.annotationsSetRefList + this.annotation;
        }
    }

    abstract class IdMerger<T extends Comparable<T>> {
        private final Dex.Section out;

        protected IdMerger(Dex.Section out) {
            this.out = out;
        }

        public final void mergeSorted() {
            TableOfContents.Sessao[] sections = new TableOfContents.Sessao[DexMerger.this.dexes.length];
            Dex.Section[] dexSections = new Dex.Section[DexMerger.this.dexes.length];
            int[] offsets = new int[DexMerger.this.dexes.length];
            int[] indexes = new int[DexMerger.this.dexes.length];
            TreeMap values = new TreeMap();
            for (int i = 0; i < DexMerger.this.dexes.length; ++i) {
                sections[i] = this.getSection(DexMerger.this.dexes[i].getTableOfContents());
                dexSections[i] = sections[i].existe() ? DexMerger.this.dexes[i].open(sections[i].off) : null;
                offsets[i] = this.readIntoMap(dexSections[i], sections[i], DexMerger.this.indexMaps[i], indexes[i], values, i);
            }
            if (values.isEmpty()) {
                this.getSection((TableOfContents)((DexMerger)DexMerger.this).contentsOut).off = 0;
                this.getSection((TableOfContents)((DexMerger)DexMerger.this).contentsOut).tam = 0;
                return;
            }
            this.getSection((TableOfContents)((DexMerger)DexMerger.this).contentsOut).off = this.out.getPosition();
            int outCount = 0;
            while (!values.isEmpty()) {
                Map.Entry first = values.pollFirstEntry();
                for (Integer dex : (List<Integer>) first.getValue()) {
                    int n = dex;
                    int n2 = indexes[n];
                    indexes[n] = n2 + 1;
                    this.updateIndex(offsets[dex], DexMerger.this.indexMaps[dex], n2, outCount);
                    offsets[dex.intValue()] = this.readIntoMap(dexSections[dex], sections[dex], DexMerger.this.indexMaps[dex], indexes[dex], values, dex);
                }
                this.write((T) first.getKey());
                ++outCount;
            }
            this.getSection((TableOfContents)((DexMerger)DexMerger.this).contentsOut).tam = outCount;
        }

        private int readIntoMap(Dex.Section in, TableOfContents.Sessao section, IndexMap indexMap, int index, TreeMap<T, List<Integer>> values, int dex) {
            int offset;
            int n = offset = in != null ? in.getPosition() : -1;
            if (index < section.tam) {
                T v = this.read(in, indexMap, index);
                List<Integer> l = values.get(v);
                if (l == null) {
                    l = new ArrayList<Integer>();
                    values.put(v, l);
                }
                l.add(dex);
            }
            return offset;
        }

        public final void mergeUnsorted() {
            this.getSection((TableOfContents)((DexMerger)DexMerger.this).contentsOut).off = this.out.getPosition();
            ArrayList<UnsortedValue> all = new ArrayList<UnsortedValue>();
            for (int i = 0; i < DexMerger.this.dexes.length; ++i) {
                all.addAll(this.readUnsortedValues(DexMerger.this.dexes[i], DexMerger.this.indexMaps[i]));
            }
            if (all.isEmpty()) {
                this.getSection((TableOfContents)((DexMerger)DexMerger.this).contentsOut).off = 0;
                this.getSection((TableOfContents)((DexMerger)DexMerger.this).contentsOut).tam = 0;
                return;
            }
            Collections.sort(all);
            int outCount = 0;
            int i = 0;
            while (i < all.size()) {
                UnsortedValue e1 = (UnsortedValue)all.get(i++);
                this.updateIndex(e1.offset, e1.indexMap, e1.index, outCount - 1);
                while (i < all.size() && e1.compareTo((UnsortedValue)all.get(i)) == 0) {
                    UnsortedValue e2 = (UnsortedValue)all.get(i++);
                    this.updateIndex(e2.offset, e2.indexMap, e2.index, outCount - 1);
                }
                this.write(e1.value);
                ++outCount;
            }
            this.getSection((TableOfContents)((DexMerger)DexMerger.this).contentsOut).tam = outCount;
        }

        private List<UnsortedValue> readUnsortedValues(Dex source, IndexMap indexMap) {
            TableOfContents.Sessao section = this.getSection(source.getTableOfContents());
            if (!section.existe()) {
                return Collections.emptyList();
            }
            ArrayList<UnsortedValue> result = new ArrayList<UnsortedValue>();
            Dex.Section in = source.open(section.off);
            for (int i = 0; i < section.tam; ++i) {
                int offset = in.getPosition();
                T value = this.read(in, indexMap, 0);
                result.add(new UnsortedValue(source, indexMap, value, i, offset));
            }
            return result;
        }

        abstract TableOfContents.Sessao getSection(TableOfContents var1);
        abstract T read(Dex.Section var1, IndexMap var2, int var3);
        abstract void updateIndex(int var1, IndexMap var2, int var3, int var4);
        abstract void write(T var1);

        class UnsortedValue implements Comparable<UnsortedValue> {
			final Dex source;
			final IndexMap indexMap;
			final T value;
			final int index;
			final int offset;

			UnsortedValue(Dex source, IndexMap indexMap, T value, int index, int offset) {
				this.source = source;
				this.indexMap = indexMap;
				this.value = value;
				this.index = index;
				this.offset = offset;
			}

			@Override
			public int compareTo(UnsortedValue unsortedValue) {
				return this.value.compareTo(unsortedValue.value);
			}
		}
    }
}

