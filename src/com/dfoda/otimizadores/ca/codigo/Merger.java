package com.dfoda.otimizadores.ca.codigo;

import com.dfoda.util.MutabilityControl;
import com.dfoda.otimizadores.rop.tipo.TypeBearer;
import com.dfoda.otimizadores.rop.tipo.Type;
import com.dfoda.util.Hex;

public final class Merger {
    public static OneLocalsArray mergeLocals(OneLocalsArray locals1, OneLocalsArray locals2) {
        if (locals1 == locals2) {
            return locals1;
        }
        int sz = locals1.getMaxLocals();
        MutabilityControl result = null;
        if (locals2.getMaxLocals() != sz) {
            throw new SimException("mismatched maxLocals values");
        }
        for (int i = 0; i < sz; ++i) {
            TypeBearer tb2;
            TypeBearer tb1 = locals1.getOrNull(i);
            TypeBearer resultType = Merger.mergeType(tb1, tb2 = locals2.getOrNull(i));
            if (resultType == tb1) continue;
            if (result == null) {
                result = locals1.copy();
            }
            if (resultType == null) {
                ((OneLocalsArray)result).invalidate(i);
                continue;
            }
            ((OneLocalsArray)result).set(i, resultType);
        }
        if (result == null) {
            return locals1;
        }
        result.setImmutable();
        return (OneLocalsArray) result;
    }

    public static ExecutionStack mergeStack(ExecutionStack stack1, ExecutionStack stack2) {
        if (stack1 == stack2) {
            return stack1;
        }
        int sz = stack1.size();
        MutabilityControl result = null;
        if (stack2.size() != sz) {
            throw new SimException("mismatched stack depths");
        }
        for (int i = 0; i < sz; ++i) {
            TypeBearer tb2;
            TypeBearer tb1 = stack1.peek(i);
            TypeBearer resultType = Merger.mergeType(tb1, tb2 = stack2.peek(i));
            if (resultType == tb1) continue;
            if (result == null) {
                result = stack1.copy();
            }
            try {
                if (resultType == null) {
                    throw new SimException("incompatible: " + tb1 + ", " + tb2);
                }
                ((ExecutionStack)result).change(i, resultType);
                continue;
            }
            catch (SimException ex) {
                ex.addContext("...while merging stack[" + Hex.u2(i) + "]");
                throw ex;
            }
        }
        if (result == null) {
            return stack1;
        }
        result.setImmutable();
        return (ExecutionStack) result;
    }

    public static TypeBearer mergeType(TypeBearer ft1, TypeBearer ft2) {
        Type type2;
        if (ft1 == null || ft1.equals(ft2)) {
            return ft1;
        }
        if (ft2 == null) {
            return null;
        }
        Type type1 = ft1.getType();
        if (type1 == (type2 = ft2.getType())) {
            return type1;
        }
        if (type1.isReference() && type2.isReference()) {
            if (type1 == Type.KNOWN_NULL) {
                return type2;
            }
            if (type2 == Type.KNOWN_NULL) {
                return type1;
            }
            if (type1.isArray() && type2.isArray()) {
                TypeBearer componentUnion = Merger.mergeType(type1.getComponentType(), type2.getComponentType());
                if (componentUnion == null) {
                    return Type.OBJECT;
                }
                return ((Type)componentUnion).getArrayType();
            }
            return Type.OBJECT;
        }
        if (type1.isIntlike() && type2.isIntlike()) {
            return Type.INT;
        }
        return null;
    }

    public static boolean isPossiblyAssignableFrom(TypeBearer supertypeBearer, TypeBearer subtypeBearer) {
        Type subtype;
        Type supertype = supertypeBearer.getType();
        if (supertype.equals(subtype = subtypeBearer.getType())) {
            return true;
        }
        int superBt = supertype.getBasicType();
        int subBt = subtype.getBasicType();
        if (superBt == 10) {
            supertype = Type.OBJECT;
            superBt = 9;
        }
        if (subBt == 10) {
            subtype = Type.OBJECT;
            subBt = 9;
        }
        if (superBt != 9 || subBt != 9) {
            return supertype.isIntlike() && subtype.isIntlike();
        }
        if (supertype == Type.KNOWN_NULL) {
            return false;
        }
        if (subtype == Type.KNOWN_NULL) {
            return true;
        }
        if (supertype == Type.OBJECT) {
            return true;
        }
        if (supertype.isArray()) {
            if (!subtype.isArray()) {
                return false;
            }
            do {
                supertype = supertype.getComponentType();
                subtype = subtype.getComponentType();
            } while (supertype.isArray() && subtype.isArray());
            return Merger.isPossiblyAssignableFrom(supertype, subtype);
        }
        if (subtype.isArray()) {
            return supertype == Type.SERIALIZABLE || supertype == Type.CLONEABLE;
        }
        return true;
    }
}

