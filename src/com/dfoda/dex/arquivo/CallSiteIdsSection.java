package com.dfoda.dex.arquivo;

import java.util.Collection;
import java.util.TreeMap;
import com.dfoda.otimizadores.rop.cst.CstCallSiteRef;
import com.dfoda.otimizadores.rop.cst.CstCallSite;
import com.dfoda.otimizadores.rop.cst.Constant;

public final class CallSiteIdsSection extends UniformItemSection {
    private final TreeMap<CstCallSiteRef, CallSiteIdItem> callSiteIds = new TreeMap();
    private final TreeMap<CstCallSite, CallSiteItem> callSites = new TreeMap();

    public CallSiteIdsSection(DexFile dexFile) {
        super("call_site_ids", dexFile, 4);
    }

    @Override
    public IndexedItem get(Constant cst) {
        if (cst == null) {
            throw new NullPointerException("cst == null");
        }
        this.throwIfNotPrepared();
        IndexedItem result = this.callSiteIds.get((CstCallSiteRef)cst);
        if (result == null) {
            throw new IllegalArgumentException("not found");
        }
        return result;
    }

    @Override
    protected void orderItems() {
        int index = 0;
        for (CallSiteIdItem callSiteId : this.callSiteIds.values()) {
            callSiteId.setIndex(index++);
        }
    }

    @Override
    public Collection<? extends Item> items() {
        return this.callSiteIds.values();
    }

    public synchronized void intern(CstCallSiteRef cstRef) {
        if (cstRef == null) {
            throw new NullPointerException("cstRef");
        }
        this.throwIfPrepared();
        CallSiteIdItem result = this.callSiteIds.get(cstRef);
        if (result == null) {
            result = new CallSiteIdItem(cstRef);
            this.callSiteIds.put(cstRef, result);
        }
    }

    void addCallSiteItem(CstCallSite callSite, CallSiteItem callSiteItem) {
        if (callSite == null) {
            throw new NullPointerException("callSite == null");
        }
        if (callSiteItem == null) {
            throw new NullPointerException("callSiteItem == null");
        }
        this.callSites.put(callSite, callSiteItem);
    }

    CallSiteItem getCallSiteItem(CstCallSite callSite) {
        if (callSite == null) {
            throw new NullPointerException("callSite == null");
        }
        return this.callSites.get(callSite);
    }
}

