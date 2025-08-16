package com.dfoda.dex.arquivo;

import com.dfoda.otimizadores.rop.cst.CstBaseMethodRef;

public final class MethodIdItem extends MemberIdItem {
    public MethodIdItem(CstBaseMethodRef method) {
        super(method);
    }

    @Override
    public ItemType itemType() {
        return ItemType.TYPE_METHOD_ID_ITEM;
    }

    @Override
    public void addContents(DexFile file) {
        super.addContents(file);
        ProtoIdsSection protoIds = file.getProtoIds();
        protoIds.intern(this.getMethodRef().getPrototype());
    }

    public CstBaseMethodRef getMethodRef() {
        return (CstBaseMethodRef)this.getRef();
    }

    @Override
    protected int getTypoidIdx(DexFile file) {
        ProtoIdsSection protoIds = file.getProtoIds();
        return protoIds.indexOf(this.getMethodRef().getPrototype());
    }

    @Override
    protected String getTypoidName() {
        return "proto_idx";
    }
}

