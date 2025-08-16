package com.dfoda.otimizadores.ca.inter;

import com.dfoda.otimizadores.rop.tipo.Prototype;
import com.dfoda.otimizadores.rop.cst.CstNat;
import com.dfoda.otimizadores.rop.cst.CstType;
import com.dfoda.otimizadores.rop.codigo.AccessFlags;

public final class StdMethod extends StdMember implements Method {
    private final Prototype effectiveDescriptor;

    public StdMethod(CstType definingClass, int accessFlags, CstNat nat, AttributeList attributes) {
        super(definingClass, accessFlags, nat, attributes);
        String descStr = this.getDescriptor().getString();
        this.effectiveDescriptor = Prototype.intern(descStr, definingClass.getClassType(), AccessFlags.isStatic(accessFlags), nat.isInstanceInit());
    }

    @Override
    public Prototype getEffectiveDescriptor() {
        return this.effectiveDescriptor;
    }
}

