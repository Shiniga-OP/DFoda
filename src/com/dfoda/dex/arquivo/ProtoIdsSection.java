package com.dfoda.dex.arquivo;

import java.util.Collection;
import java.util.TreeMap;
import com.dfoda.otimizadores.rop.tipo.Prototype;
import com.dfoda.otimizadores.rop.cst.Constant;
import com.dfoda.otimizadores.rop.cst.CstProtoRef;
import com.dfoda.util.AnnotatedOutput;
import com.dfoda.util.Hex;

public final class ProtoIdsSection extends UniformItemSection {
    private final TreeMap<Prototype, ProtoIdItem> protoIds = new TreeMap();

    public ProtoIdsSection(DexFile file) {
        super("proto_ids", file, 4);
    }

    @Override
    public Collection<? extends Item> items() {
        return this.protoIds.values();
    }

    @Override
    public IndexedItem get(Constant cst) {
        if (cst == null) {
            throw new NullPointerException("cst == null");
        }
        if (!(cst instanceof CstProtoRef)) {
            throw new IllegalArgumentException("cst not instance of CstProtoRef");
        }
        this.throwIfNotPrepared();
        CstProtoRef protoRef = (CstProtoRef)cst;
        IndexedItem result = this.protoIds.get(protoRef.getPrototype());
        if (result == null) {
            throw new IllegalArgumentException("not found");
        }
        return result;
    }

    public void writeHeaderPart(AnnotatedOutput out) {
        int offset;
        this.throwIfNotPrepared();
        int sz = this.protoIds.size();
        int n = offset = sz == 0 ? 0 : this.getFileOffset();
        if (sz > 65536) {
            throw new UnsupportedOperationException("too many proto ids");
        }
        if (out.annotates()) {
            out.annotate(4, "proto_ids_size:  " + Hex.u4(sz));
            out.annotate(4, "proto_ids_off:   " + Hex.u4(offset));
        }
        out.writeInt(sz);
        out.writeInt(offset);
    }

    public synchronized ProtoIdItem intern(Prototype prototype) {
        if (prototype == null) {
            throw new NullPointerException("prototype == null");
        }
        this.throwIfPrepared();
        ProtoIdItem result = this.protoIds.get(prototype);
        if (result == null) {
            result = new ProtoIdItem(prototype);
            this.protoIds.put(prototype, result);
        }
        return result;
    }

    public int indexOf(Prototype prototype) {
        if (prototype == null) {
            throw new NullPointerException("prototype == null");
        }
        this.throwIfNotPrepared();
        ProtoIdItem item = this.protoIds.get(prototype);
        if (item == null) {
            throw new IllegalArgumentException("not found");
        }
        return item.getIndex();
    }

    @Override
    protected void orderItems() {
        int idx = 0;
        for (Item item : this.items()) {
            ((ProtoIdItem)item).setIndex(idx);
            ++idx;
        }
    }
}

