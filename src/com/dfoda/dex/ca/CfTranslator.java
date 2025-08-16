package com.dfoda.dex.ca;

import com.dfoda.dex.arquivo.ClassDefItem;
import com.dfoda.dexer.DFodaCtx;
import com.dex.util.ErroCtx;
import com.dfoda.dex.DexOptions;
import com.dfoda.dex.arquivo.DexFile;
import com.dfoda.otimizadores.rop.cst.CstType;
import com.dfoda.otimizadores.rop.cst.CstString;
import com.dfoda.otimizadores.rop.anotacao.Annotations;
import com.dfoda.dex.arquivo.FieldIdsSection;
import com.dfoda.dex.arquivo.MethodIdsSection;
import com.dfoda.dex.arquivo.MethodHandlesSection;
import com.dfoda.otimizadores.rop.cst.ConstantPool;
import com.dfoda.otimizadores.rop.cst.Constant;
import com.dfoda.otimizadores.rop.cst.CstFieldRef;
import com.dfoda.otimizadores.rop.cst.CstMethodRef;
import com.dfoda.otimizadores.rop.cst.CstBaseMethodRef;
import com.dfoda.otimizadores.rop.cst.CstEnumRef;
import com.dfoda.otimizadores.rop.cst.CstMethodHandle;
import com.dfoda.otimizadores.rop.cst.CstInvokeDynamic;
import com.dfoda.otimizadores.rop.cst.CstCallSite;
import com.dfoda.otimizadores.rop.cst.CstCallSiteRef;
import com.dfoda.otimizadores.rop.codigo.AccessFlags;
import com.dfoda.otimizadores.rop.cst.TypedConstant;
import com.dfoda.dex.arquivo.EncodedField;
import com.dfoda.otimizadores.rop.tipo.Type;
import com.dfoda.otimizadores.rop.cst.CstBoolean;
import com.dfoda.otimizadores.rop.cst.CstInteger;
import com.dfoda.otimizadores.rop.cst.CstByte;
import com.dfoda.otimizadores.rop.cst.CstChar;
import com.dfoda.otimizadores.rop.cst.CstShort;
import com.dfoda.otimizadores.rop.anotacao.AnnotationsList;
import com.dfoda.otimizadores.rop.codigo.DexTranslationAdvice;
import com.dfoda.otimizadores.rop.codigo.RopMethod;
import com.dfoda.otimizadores.ssa.Optimizer;
import com.dfoda.otimizadores.rop.codigo.LocalVariableInfo;
import com.dfoda.dex.arquivo.EncodedMethod;
import com.dfoda.otimizadores.rop.tipo.TypeList;
import com.dfoda.otimizadores.ca.direto.DirectClassFile;
import com.dfoda.dex.arquivo.CallSiteIdsSection;
import com.dfoda.otimizadores.rop.cst.CstInterfaceMethodRef;
import com.dfoda.otimizadores.ca.codigo.BootstrapMethodsList;
import com.dfoda.otimizadores.ca.inter.FieldList;
import com.dfoda.otimizadores.ca.inter.Field;
import com.dfoda.otimizadores.ca.inter.MethodList;
import com.dfoda.otimizadores.ca.inter.Method;
import com.dfoda.dex.codigo.DalvCode;
import com.dfoda.otimizadores.ca.codigo.ConcreteMethod;
import com.dfoda.otimizadores.ca.codigo.Ropper;
import com.dfoda.otimizadores.rop.codigo.LocalVariableExtractor;
import com.dfoda.dex.codigo.RopTranslator;

public class CfTranslator {
    public static ClassDefItem translate(DFodaCtx ctx, DirectClassFile cf, byte[] bytes, CfOptions cfOptions, DexOptions dexOptions, DexFile dexArq) {
        try {
            return CfTranslator.translate0(ctx, cf, bytes, cfOptions, dexOptions, dexArq);
        } catch(RuntimeException e) {
            String msg = "processando... " + cf.getFilePath();
            throw ErroCtx.comCtx(e, msg);
        }
    }

    private static ClassDefItem translate0(DFodaCtx ctx, DirectClassFile cf, byte[] bytes, CfOptions cfOptions, DexOptions dexOptions, DexFile dexFile) {
        ctx.opcoesOtimiza.loadOptimizeLists(cfOptions.optimizeListFile, cfOptions.dontOptimizeListFile);
        CstType thisClass = cf.getThisClass();
        int classAccessFlags = cf.getAccessFlags() & 0xFFFFFFDF;
        CstString sourceFile = cfOptions.positionInfo == 1 ? null : cf.getSourceFile();
        ClassDefItem out = new ClassDefItem(thisClass, classAccessFlags, cf.getSuperclass(), cf.getInterfaces(), sourceFile);
        Annotations classAnnotations = AttributeTranslator.getClassAnnotations(cf, cfOptions);
        if (classAnnotations.size() != 0) {
            out.setClassAnnotations(classAnnotations, dexFile);
        }
        FieldIdsSection fieldIdsSection = dexFile.getFieldIds();
        MethodIdsSection methodIdsSection = dexFile.getMethodIds();
        MethodHandlesSection methodHandlesSection = dexFile.getMethodHandles();
        CallSiteIdsSection callSiteIds = dexFile.getCallSiteIds();
        CfTranslator.processFields(cf, out, dexFile);
        CfTranslator.processMethods(ctx, cf, cfOptions, dexOptions, out, dexFile);
        ConstantPool constantPool = cf.getConstantPool();
        int constantPoolSize = constantPool.size();
        for (int i = 0; i < constantPoolSize; ++i) {
            Constant constant = constantPool.getOrNull(i);
            if (constant instanceof CstMethodRef) {
                methodIdsSection.intern((CstBaseMethodRef)constant);
                continue;
            }
            if (constant instanceof CstInterfaceMethodRef) {
                methodIdsSection.intern(((CstInterfaceMethodRef)constant).toMethodRef());
                continue;
            }
            if (constant instanceof CstFieldRef) {
                fieldIdsSection.intern((CstFieldRef)constant);
                continue;
            }
            if (constant instanceof CstEnumRef) {
                fieldIdsSection.intern(((CstEnumRef)constant).getFieldRef());
                continue;
            }
            if (constant instanceof CstMethodHandle) {
                methodHandlesSection.intern((CstMethodHandle)constant);
                continue;
            }
            if (!(constant instanceof CstInvokeDynamic)) continue;
            CstInvokeDynamic cstInvokeDynamic = (CstInvokeDynamic)constant;
            int index = cstInvokeDynamic.getBootstrapMethodIndex();
            BootstrapMethodsList.Item bootstrapMethod = cf.getBootstrapMethods().get(index);
            CstCallSite callSite = CstCallSite.make(bootstrapMethod.getBootstrapMethodHandle(), cstInvokeDynamic.getNat(), bootstrapMethod.getBootstrapMethodArguments());
            cstInvokeDynamic.setDeclaringClass(cf.getThisClass());
            cstInvokeDynamic.setCallSite(callSite);
            for (CstCallSiteRef ref : cstInvokeDynamic.getReferences()) {
                callSiteIds.intern(ref);
            }
        }
        return out;
    }

    private static void processFields(DirectClassFile cf, ClassDefItem out, DexFile dexFile) {
        CstType thisClass = cf.getThisClass();
        FieldList fields = cf.getFields();
        int sz = fields.size();
        for (int i = 0; i < sz; ++i) {
            Field one = fields.get(i);
            try {
                CstFieldRef field = new CstFieldRef(thisClass, one.getNat());
                int accessFlags = one.getAccessFlags();
                if (AccessFlags.isStatic(accessFlags)) {
                    TypedConstant constVal = one.getConstantValue();
                    EncodedField fi = new EncodedField(field, accessFlags);
                    if (constVal != null) {
                        constVal = CfTranslator.coerceConstant(constVal, field.getType());
                    }
                    out.addStaticField(fi, constVal);
                } else {
                    EncodedField fi = new EncodedField(field, accessFlags);
                    out.addInstanceField(fi);
                }
                Annotations annotations = AttributeTranslator.getAnnotations(one.getAttributes());
                if (annotations.size() != 0) {
                    out.addFieldAnnotations(field, annotations, dexFile);
                }
                dexFile.getFieldIds().intern(field);
                continue;
            }
            catch (RuntimeException ex) {
                String msg = "...while processing " + one.getName().toHuman() + " " + one.getDescriptor().toHuman();
                throw ErroCtx.comCtx(ex, msg);
            }
        }
    }

    private static TypedConstant coerceConstant(TypedConstant constant, Type type) {
        Type constantType = constant.getType();
        if (constantType.equals(type)) {
            return constant;
        }
        switch (type.getBasicType()) {
            case 1: {
                return CstBoolean.make(((CstInteger)constant).getValue());
            }
            case 2: {
                return CstByte.make(((CstInteger)constant).getValue());
            }
            case 3: {
                return CstChar.make(((CstInteger)constant).getValue());
            }
            case 8: {
                return CstShort.make(((CstInteger)constant).getValue());
            }
        }
        throw new UnsupportedOperationException("can't coerce " + constant + " to " + type);
    }

    private static void processMethods(DFodaCtx context, DirectClassFile cf, CfOptions cfOptions, DexOptions dexOptions, ClassDefItem out, DexFile dexFile) {
        CstType thisClass = cf.getThisClass();
        MethodList methods = cf.getMethods();
        int sz = methods.size();
        for (int i = 0; i < sz; ++i) {
            Method one = methods.get(i);
            try {
                AnnotationsList list;
                DalvCode code;
                boolean isConstructor;
                CstMethodRef meth = new CstMethodRef(thisClass, one.getNat());
                int accessFlags = one.getAccessFlags();
                boolean isStatic = AccessFlags.isStatic(accessFlags);
                boolean isPrivate = AccessFlags.isPrivate(accessFlags);
                boolean isNative = AccessFlags.isNative(accessFlags);
                boolean isAbstract = AccessFlags.isAbstract(accessFlags);
                isConstructor = meth.isInstanceInit() || meth.isClassInit();
                if (isNative || isAbstract) {
                    code = null;
                } else {
                    ConcreteMethod concrete = new ConcreteMethod(one, cf, cfOptions.positionInfo != 1, cfOptions.localInfo);
                    DexTranslationAdvice advice = DexTranslationAdvice.THE_ONE;
                    RopMethod rmeth = Ropper.convert(concrete, advice, methods, dexOptions);
                    RopMethod nonOptRmeth = null;
                    int paramSize = meth.getParameterWordCount(isStatic);
                    String canonicalName = thisClass.getClassType().getDescriptor() + "." + one.getName().getString();
                    if (cfOptions.optimize && context.opcoesOtimiza.shouldOptimize(canonicalName)) {
                        nonOptRmeth = rmeth;
                        rmeth = Optimizer.optimize(rmeth, paramSize, isStatic, cfOptions.localInfo, advice);
                        if (cfOptions.statistics) {
                            context.codigoStatus.updateRopStatistics(nonOptRmeth, rmeth);
                        }
                    }
                    LocalVariableInfo locals = null;
                    if (cfOptions.localInfo) {
                        locals = LocalVariableExtractor.extract(rmeth);
                    }
                    code = RopTranslator.translate(rmeth, cfOptions.positionInfo, locals, paramSize, dexOptions);
                    if (cfOptions.statistics && nonOptRmeth != null) {
                        CfTranslator.updateDexStatistics(context, cfOptions, dexOptions, rmeth, nonOptRmeth, locals, paramSize, concrete.getCode().size());
                    }
                }
                if (AccessFlags.isSynchronized(accessFlags)) {
                    accessFlags |= 0x20000;
                    if (!isNative) {
                        accessFlags &= 0xFFFFFFDF;
                    }
                }
                if (isConstructor) {
                    accessFlags |= 0x10000;
                }
                TypeList exceptions = AttributeTranslator.getExceptions(one);
                EncodedMethod mi = new EncodedMethod(meth, accessFlags, code, exceptions);
                if (meth.isInstanceInit() || meth.isClassInit() || isStatic || isPrivate) {
                    out.addDirectMethod(mi);
                } else {
                    out.addVirtualMethod(mi);
                }
                Annotations annotations = AttributeTranslator.getMethodAnnotations(one);
                if (annotations.size() != 0) {
                    out.addMethodAnnotations(meth, annotations, dexFile);
                }
                if ((list = AttributeTranslator.getParameterAnnotations(one)).size() != 0) {
                    out.addParameterAnnotations(meth, list, dexFile);
                }
                dexFile.getMethodIds().intern(meth);
                continue;
            }
            catch (RuntimeException ex) {
                String msg = "...while processing " + one.getName().toHuman() + " " + one.getDescriptor().toHuman();
                throw ErroCtx.comCtx(ex, msg);
            }
        }
    }

    private static void updateDexStatistics(DFodaCtx context, CfOptions cfOptions, DexOptions dexOptions, RopMethod optRmeth, RopMethod nonOptRmeth, LocalVariableInfo locals, int paramSize, int originalByteCount) {
        DalvCode optCode = RopTranslator.translate(optRmeth, cfOptions.positionInfo, locals, paramSize, dexOptions);
        DalvCode nonOptCode = RopTranslator.translate(nonOptRmeth, cfOptions.positionInfo, locals, paramSize, dexOptions);
        DalvCode.AssignIndicesCallback callback = new DalvCode.AssignIndicesCallback(){

            @Override
            public int getIndex(Constant cst) {
                return 0;
            }
        };
        optCode.assignIndices(callback);
        nonOptCode.assignIndices(callback);
        context.codigoStatus.updateDexStatistics(nonOptCode, optCode);
        context.codigoStatus.updateOriginalByteCount(originalByteCount);
    }
}

