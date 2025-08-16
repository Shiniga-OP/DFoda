package com.dfoda.dex.arquivo;

import com.dfoda.otimizadores.rop.cst.CstCallSiteRef;
import com.dfoda.util.AnnotatedOutput;
import com.dfoda.otimizadores.rop.cst.CstCallSite;
import com.dfoda.util.Hex;

public final class CallSiteIdItem extends IndexedItem implements Comparable {
    private static final int ITEM_SIZE = 4;
    final CstCallSiteRef invokeDynamicRef;
    public CallSiteItem data;

    public CallSiteIdItem(CstCallSiteRef invokeDynamicRef) {
        this.invokeDynamicRef = invokeDynamicRef;
        this.data = null;
    }

    @Override
    public ItemType itemType() {
        return ItemType.TYPE_CALL_SITE_ID_ITEM;
    }

    @Override
    public int writeSize() {
        return 4;
    }

    @Override
    public void addContents(DexFile file) {
        CstCallSite callSite = this.invokeDynamicRef.getCallSite();
        CallSiteIdsSection callSiteIds = file.getCallSiteIds();
        CallSiteItem callSiteItem = callSiteIds.getCallSiteItem(callSite);
        if (callSiteItem == null) {
            MixedItemSection byteData = file.getByteData();
            callSiteItem = new CallSiteItem(callSite);
            byteData.add(callSiteItem);
            callSiteIds.addCallSiteItem(callSite, callSiteItem);
        }
        this.data = callSiteItem;
    }

    @Override
    public void writeTo(DexFile file, AnnotatedOutput out) {
        int offset = this.data.getAbsoluteOffset();
        if (out.annotates()) {
            out.annotate(0, this.indexString() + ' ' + this.invokeDynamicRef.toString());
            out.annotate(4, "call_site_off: " + Hex.u4(offset));
        }
        out.writeInt(offset);
    }

    public int compareTo(Object o) {
        CallSiteIdItem other = (CallSiteIdItem)o;
        return this.invokeDynamicRef.compareTo(other.invokeDynamicRef);
    }
}

