package com.dfoda.dex.arquivo;

import com.dfoda.otimizadores.rop.cst.CstMethodHandle;
import com.dfoda.util.AnnotatedOutput;
import com.dfoda.util.Hex;
import com.dfoda.otimizadores.rop.cst.Constant;
import com.dfoda.otimizadores.rop.cst.CstFieldRef;
import com.dfoda.otimizadores.rop.cst.CstBaseMethodRef;
import com.dfoda.otimizadores.rop.cst.CstInterfaceMethodRef;

public final class MethodHandleItem extends IndexedItem {
    private final CstMethodHandle methodHandle;

    public MethodHandleItem(CstMethodHandle methodHandle) {
        this.methodHandle = methodHandle;
    }

    @Override
    public ItemType itemType() {
        return ItemType.TYPE_METHOD_HANDLE_ITEM;
    }

    @Override
    public int writeSize() {
        return 8;
    }

    @Override
    public void addContents(DexFile file) {
        MethodHandlesSection methodHandles = file.getMethodHandles();
        methodHandles.intern(this.methodHandle);
    }

    @Override
    public void writeTo(DexFile file, AnnotatedOutput out) {
        int targetIndex = this.getTargetIndex(file);
        int mhType = this.methodHandle.getMethodHandleType();
        if (out.annotates()) {
            out.annotate(0, this.indexString() + ' ' + this.methodHandle.toString());
            String typeComment = " // " + CstMethodHandle.getMethodHandleTypeName(mhType);
            out.annotate(2, "type:     " + Hex.u2(mhType) + typeComment);
            out.annotate(2, "reserved: " + Hex.u2(0));
            String targetComment = " // " + this.methodHandle.getRef().toString();
            if (this.methodHandle.isAccessor()) {
                out.annotate(2, "fieldId:  " + Hex.u2(targetIndex) + targetComment);
            } else {
                out.annotate(2, "methodId: " + Hex.u2(targetIndex) + targetComment);
            }
            out.annotate(2, "reserved: " + Hex.u2(0));
        }
        out.writeShort(mhType);
        out.writeShort(0);
        out.writeShort(this.getTargetIndex(file));
        out.writeShort(0);
    }

    private int getTargetIndex(DexFile file) {
        Constant ref = this.methodHandle.getRef();
        if (this.methodHandle.isAccessor()) {
            FieldIdsSection fieldIds = file.getFieldIds();
            return fieldIds.indexOf((CstFieldRef)ref);
        }
        if (this.methodHandle.isInvocation()) {
            if (ref instanceof CstInterfaceMethodRef) {
                ref = ((CstInterfaceMethodRef)ref).toMethodRef();
            }
            MethodIdsSection methodIds = file.getMethodIds();
            return methodIds.indexOf((CstBaseMethodRef)ref);
        }
        throw new IllegalStateException("Unhandled invocation type");
    }
}

