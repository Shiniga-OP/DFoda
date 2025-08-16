package com.dfoda.otimizadores.ca.inter;

import com.dfoda.otimizadores.rop.cst.CstType;
import com.dfoda.otimizadores.rop.cst.CstString;
import com.dfoda.otimizadores.rop.cst.CstNat;

public interface Member extends HasAttribute {
    public CstType getDefiningClass();
    public int getAccessFlags();
    public CstString getName();
    public CstString getDescriptor();
    public CstNat getNat();

    @Override
    public AttributeList getAttributes();
}

