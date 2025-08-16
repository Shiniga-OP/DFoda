package com.dfoda.otimizadores.ca.direto;

import com.dfoda.otimizadores.ca.inter.StdMethodList;
import com.dfoda.otimizadores.rop.cst.CstType;
import com.dfoda.otimizadores.rop.codigo.AccessFlags;
import com.dfoda.otimizadores.ca.inter.Member;
import com.dfoda.otimizadores.ca.inter.StdMethod;
import com.dfoda.otimizadores.rop.cst.CstNat;
import com.dfoda.otimizadores.ca.inter.AttributeList;

final class MethodListParser extends MemberListParser {
    private final StdMethodList methods = new StdMethodList(this.getCount());

    public MethodListParser(DirectClassFile cf, CstType definer, int offset, AttributeFactory attributeFactory) {
        super(cf, definer, offset, attributeFactory);
    }

    public StdMethodList getList() {
        this.parseIfNecessary();
        return this.methods;
    }

    @Override
    protected String humanName() {
        return "method";
    }

    @Override
    protected String humanAccessFlags(int accessFlags) {
        return AccessFlags.methodString(accessFlags);
    }

    @Override
    protected int getAttributeContext() {
        return 2;
    }

    @Override
    protected Member set(int n, int accessFlags, CstNat nat, AttributeList attributes) {
        StdMethod meth = new StdMethod(this.getDefiner(), accessFlags, nat, attributes);
        this.methods.set(n, meth);
        return meth;
    }
}

