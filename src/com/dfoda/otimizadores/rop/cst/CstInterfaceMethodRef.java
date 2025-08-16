package com.dfoda.otimizadores.rop.cst;

public final class CstInterfaceMethodRef extends CstBaseMethodRef {
    private CstMethodRef methodRef = null;

    public CstInterfaceMethodRef(CstType definingClass, CstNat nat) {
        super(definingClass, nat);
    }

    @Override
    public String typeName() {
        return "ifaceMethod";
    }

    public CstMethodRef toMethodRef() {
        if (this.methodRef == null) {
            this.methodRef = new CstMethodRef(this.getDefiningClass(), this.getNat());
        }
        return this.methodRef;
    }
}

