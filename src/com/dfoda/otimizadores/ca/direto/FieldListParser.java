package com.dfoda.otimizadores.ca.direto;

import com.dfoda.otimizadores.ca.inter.StdFieldList;
import com.dfoda.otimizadores.rop.cst.CstType;
import com.dfoda.otimizadores.ca.inter.Member;
import com.dfoda.otimizadores.rop.codigo.AccessFlags;
import com.dfoda.otimizadores.ca.inter.AttributeList;
import com.dfoda.otimizadores.rop.cst.CstNat;
import com.dfoda.otimizadores.ca.inter.StdField;

final class FieldListParser extends MemberListParser {
    private final StdFieldList fields = new StdFieldList(this.getCount());

    public FieldListParser(DirectClassFile cf, CstType definer, int offset, AttributeFactory attributeFactory) {
        super(cf, definer, offset, attributeFactory);
    }

    public StdFieldList getList() {
        this.parseIfNecessary();
        return this.fields;
    }

    @Override
    protected String humanName() {
        return "field";
    }

    @Override
    protected String humanAccessFlags(int accessFlags) {
        return AccessFlags.fieldString(accessFlags);
    }

    @Override
    protected int getAttributeContext() {
        return 1;
    }

    @Override
    protected Member set(int n, int accessFlags, CstNat nat, AttributeList attributes) {
        StdField field = new StdField(this.getDefiner(), accessFlags, nat, attributes);
        this.fields.set(n, field);
        return field;
    }
}

