package com.dfoda.otimizadores.ca.inter;

import com.dfoda.otimizadores.rop.cst.CstNat;
import com.dfoda.otimizadores.rop.cst.CstType;
import com.dfoda.otimizadores.rop.cst.TypedConstant;
import com.dfoda.otimizadores.ca.atributo.AttConstantValue;

public final class StdField extends StdMember implements Field {
    public StdField(CstType definingClass, int accessFlags, CstNat nat, AttributeList attributes) {
        super(definingClass, accessFlags, nat, attributes);
    }

    @Override
    public TypedConstant getConstantValue() {
        AttributeList attribs = this.getAttributes();
        AttConstantValue cval = (AttConstantValue)attribs.findFirst("ConstantValue");
        if (cval == null) {
            return null;
        }
        return cval.getConstantValue();
    }
}

