package com.dfoda.otimizadores.rop.cst;

import com.dfoda.otimizadores.rop.tipo.Type;

public final class CstEnumRef extends CstMemberRef {
    private CstFieldRef fieldRef = null;

    public CstEnumRef(CstNat nat) {
        super(new CstType(nat.getFieldType()), nat);
    }

    @Override
    public String typeName() {
        return "enum";
    }

    @Override
    public Type getType() {
        return this.getDefiningClass().getClassType();
    }

    public CstFieldRef getFieldRef() {
        if (this.fieldRef == null) {
            this.fieldRef = new CstFieldRef(this.getDefiningClass(), this.getNat());
        }
        return this.fieldRef;
    }
}

