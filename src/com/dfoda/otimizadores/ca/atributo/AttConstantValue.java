package com.dfoda.otimizadores.ca.atributo;

import com.dfoda.otimizadores.rop.cst.TypedConstant;
import com.dfoda.otimizadores.rop.cst.CstString;
import com.dfoda.otimizadores.rop.cst.CstInteger;
import com.dfoda.otimizadores.rop.cst.CstLong;
import com.dfoda.otimizadores.rop.cst.CstFloat;
import com.dfoda.otimizadores.rop.cst.CstDouble;

public final class AttConstantValue extends BaseAttribute {
    public static final String ATTRIBUTE_NAME = "ConstantValue";
    private final TypedConstant constantValue;

    public AttConstantValue(TypedConstant constantValue) {
        super(ATTRIBUTE_NAME);
        if (!(constantValue instanceof CstString || constantValue instanceof CstInteger || constantValue instanceof CstLong || constantValue instanceof CstFloat || constantValue instanceof CstDouble)) {
            if (constantValue == null) {
                throw new NullPointerException("constantValue == null");
            }
            throw new IllegalArgumentException("bad type for constantValue");
        }
        this.constantValue = constantValue;
    }

    @Override
    public int byteLength() {
        return 8;
    }

    public TypedConstant getConstantValue() {
        return this.constantValue;
    }
}

