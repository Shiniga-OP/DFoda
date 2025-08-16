package com.dfoda.dex.ca;

import java.util.ArrayList;
import com.dfoda.otimizadores.rop.tipo.TypeList;
import com.dfoda.otimizadores.rop.anotacao.Annotations;
import com.dfoda.otimizadores.rop.anotacao.Annotation;
import com.dfoda.otimizadores.rop.codigo.AccessFlags;
import com.dfoda.otimizadores.rop.cst.CstType;
import com.dfoda.otimizadores.rop.cst.CstNat;
import com.dfoda.otimizadores.rop.tipo.StdTypeList;
import com.dfoda.otimizadores.rop.anotacao.AnnotationsList;
import com.dfoda.otimizadores.ca.inter.Method;
import com.dfoda.otimizadores.ca.inter.AttributeList;
import com.dfoda.otimizadores.ca.atributo.AttExceptions;
import com.dfoda.otimizadores.ca.direto.DirectClassFile;
import com.dfoda.util.Warning;
import com.dfoda.dex.arquivo.AnnotationUtils;
import com.dfoda.otimizadores.ca.atributo.AttRuntimeVisibleAnnotations;
import com.dfoda.otimizadores.ca.atributo.AttRuntimeInvisibleAnnotations;
import com.dfoda.otimizadores.ca.atributo.AttSourceDebugExtension;
import com.dfoda.otimizadores.ca.atributo.InnerClassList;
import com.dfoda.otimizadores.ca.atributo.AttRuntimeInvisibleParameterAnnotations;
import com.dfoda.otimizadores.ca.inter.MethodList;
import com.dfoda.otimizadores.rop.anotacao.AnnotationVisibility;
import com.dfoda.otimizadores.rop.anotacao.NameValuePair;
import com.dfoda.otimizadores.ca.atributo.AttSignature;
import com.dfoda.otimizadores.ca.atributo.AttEnclosingMethod;
import com.dfoda.otimizadores.rop.cst.CstMethodRef;
import com.dfoda.otimizadores.ca.atributo.AttInnerClasses;
import com.dfoda.otimizadores.ca.atributo.AttRuntimeVisibleParameterAnnotations;
import com.dfoda.otimizadores.ca.atributo.AttAnnotationDefault;
import com.dfoda.otimizadores.rop.tipo.Type;

public class AttributeTranslator {
    public static TypeList getExceptions(Method method) {
        AttributeList attribs = method.getAttributes();
        AttExceptions exceptions = (AttExceptions)attribs.findFirst("Exceptions");
        if (exceptions == null) {
            return StdTypeList.EMPTY;
        }
        return exceptions.getExceptions();
    }

    public static Annotations getAnnotations(AttributeList attribs) {
        Annotations result = AttributeTranslator.getAnnotations0(attribs);
        Annotation signature = AttributeTranslator.getSignature(attribs);
        Annotation sourceDebugExtension = AttributeTranslator.getSourceDebugExtension(attribs);
        if (signature != null) {
            result = Annotations.combine(result, signature);
        }
        if (sourceDebugExtension != null) {
            result = Annotations.combine(result, sourceDebugExtension);
        }
        return result;
    }

    public static Annotations getClassAnnotations(DirectClassFile cf, CfOptions args) {
        Annotation annotationDefault;
        CstType thisClass = cf.getThisClass();
        AttributeList attribs = cf.getAttributes();
        Annotations result = AttributeTranslator.getAnnotations(attribs);
        Annotation enclosingMethod = AttributeTranslator.translateEnclosingMethod(attribs);
        try {
            Annotations innerClassAnnotations = AttributeTranslator.translateInnerClasses(thisClass, attribs, enclosingMethod == null);
            if (innerClassAnnotations != null) {
                result = Annotations.combine(result, innerClassAnnotations);
            }
        }
        catch (Warning warn) {
            args.warn.println("warning: " + warn.getMessage());
        }
        if (enclosingMethod != null) {
            result = Annotations.combine(result, enclosingMethod);
        }
        if (AccessFlags.isAnnotation(cf.getAccessFlags()) && (annotationDefault = AttributeTranslator.translateAnnotationDefaults(cf)) != null) {
            result = Annotations.combine(result, annotationDefault);
        }
        return result;
    }

    public static Annotations getMethodAnnotations(Method method) {
        Annotations result = AttributeTranslator.getAnnotations(method.getAttributes());
        TypeList exceptions = AttributeTranslator.getExceptions(method);
        if (exceptions.size() != 0) {
            Annotation throwsAnnotation = AnnotationUtils.makeThrows(exceptions);
            result = Annotations.combine(result, throwsAnnotation);
        }
        return result;
    }

    private static Annotations getAnnotations0(AttributeList attribs) {
        AttRuntimeVisibleAnnotations visible = (AttRuntimeVisibleAnnotations)attribs.findFirst("RuntimeVisibleAnnotations");
        AttRuntimeInvisibleAnnotations invisible = (AttRuntimeInvisibleAnnotations)attribs.findFirst("RuntimeInvisibleAnnotations");
        if (visible == null) {
            if (invisible == null) {
                return Annotations.EMPTY;
            }
            return invisible.getAnnotations();
        }
        if (invisible == null) {
            return visible.getAnnotations();
        }
        return Annotations.combine(visible.getAnnotations(), invisible.getAnnotations());
    }

    private static Annotation getSignature(AttributeList attribs) {
        AttSignature signature = (AttSignature)attribs.findFirst("Signature");
        if (signature == null) {
            return null;
        }
        return AnnotationUtils.makeSignature(signature.getSignature());
    }

    private static Annotation getSourceDebugExtension(AttributeList attribs) {
        AttSourceDebugExtension extension = (AttSourceDebugExtension)attribs.findFirst("SourceDebugExtension");
        if (extension == null) {
            return null;
        }
        return AnnotationUtils.makeSourceDebugExtension(extension.getSmapString());
    }

    private static Annotation translateEnclosingMethod(AttributeList attribs) {
        AttEnclosingMethod enclosingMethod = (AttEnclosingMethod)attribs.findFirst("EnclosingMethod");
        if (enclosingMethod == null) {
            return null;
        }
        CstType enclosingClass = enclosingMethod.getEnclosingClass();
        CstNat nat = enclosingMethod.getMethod();
        if (nat == null) {
            return AnnotationUtils.makeEnclosingClass(enclosingClass);
        }
        return AnnotationUtils.makeEnclosingMethod(new CstMethodRef(enclosingClass, nat));
    }

    private static Annotations translateInnerClasses(CstType thisClass, AttributeList attribs, boolean needEnclosingClass) {
        AttInnerClasses innerClasses = (AttInnerClasses)attribs.findFirst("InnerClasses");
        if (innerClasses == null) {
            return null;
        }
        InnerClassList list = innerClasses.getInnerClasses();
        int size = list.size();
        InnerClassList.Item foundThisClass = null;
        ArrayList<Type> membersList = new ArrayList<Type>();
        for (int i = 0; i < size; ++i) {
            InnerClassList.Item item = list.get(i);
            CstType innerClass = item.getInnerClass();
            if (innerClass.equals(thisClass)) {
                foundThisClass = item;
                continue;
            }
            if (!thisClass.equals(item.getOuterClass())) continue;
            membersList.add(innerClass.getClassType());
        }
        int membersSize = membersList.size();
        if (foundThisClass == null && membersSize == 0) {
            return null;
        }
        Annotations result = new Annotations();
        if (foundThisClass != null) {
            result.add(AnnotationUtils.makeInnerClass(foundThisClass.getInnerName(), foundThisClass.getAccessFlags()));
            if (needEnclosingClass) {
                CstType outer = foundThisClass.getOuterClass();
                if (outer == null) {
                    throw new Warning("Ignoring InnerClasses attribute for an anonymous inner class\n(" + thisClass.toHuman() + ") that doesn't come with an\nassociated EnclosingMethod attribute. This class was probably produced by a\ncompiler that did not target the modern .class file format. The recommended\nsolution is to recompile the class from source, using an up-to-date compiler\nand without specifying any \"-target\" type options. The consequence of ignoring\nthis warning is that reflective operations on this class will incorrectly\nindicate that it is *not* an inner class.");
                }
                result.add(AnnotationUtils.makeEnclosingClass(foundThisClass.getOuterClass()));
            }
        }
        if (membersSize != 0) {
            StdTypeList typeList = new StdTypeList(membersSize);
            for (int i = 0; i < membersSize; ++i) {
                typeList.set(i, (Type)membersList.get(i));
            }
            typeList.setImmutable();
            result.add(AnnotationUtils.makeMemberClasses(typeList));
        }
        result.setImmutable();
        return result;
    }

    public static AnnotationsList getParameterAnnotations(Method method) {
        AttributeList attribs = method.getAttributes();
        AttRuntimeVisibleParameterAnnotations visible = (AttRuntimeVisibleParameterAnnotations)attribs.findFirst("RuntimeVisibleParameterAnnotations");
        AttRuntimeInvisibleParameterAnnotations invisible = (AttRuntimeInvisibleParameterAnnotations)attribs.findFirst("RuntimeInvisibleParameterAnnotations");
        if (visible == null) {
            if (invisible == null) {
                return AnnotationsList.EMPTY;
            }
            return invisible.getParameterAnnotations();
        }
        if (invisible == null) {
            return visible.getParameterAnnotations();
        }
        return AnnotationsList.combine(visible.getParameterAnnotations(), invisible.getParameterAnnotations());
    }

    private static Annotation translateAnnotationDefaults(DirectClassFile cf) {
        CstType thisClass = cf.getThisClass();
        MethodList methods = cf.getMethods();
        int sz = methods.size();
        Annotation result = new Annotation(thisClass, AnnotationVisibility.EMBEDDED);
        boolean any = false;
        for (int i = 0; i < sz; ++i) {
            Method one = methods.get(i);
            AttributeList attribs = one.getAttributes();
            AttAnnotationDefault oneDefault = (AttAnnotationDefault)attribs.findFirst("AnnotationDefault");
            if (oneDefault == null) continue;
            NameValuePair pair = new NameValuePair(one.getNat().getName(), oneDefault.getValue());
            result.add(pair);
            any = true;
        }
        if (!any) {
            return null;
        }
        result.setImmutable();
        return AnnotationUtils.makeAnnotationDefault(result);
    }
}

