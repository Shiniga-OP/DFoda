package com.dfoda.otimizadores.ca.direto;

import com.dfoda.otimizadores.ca.inter.StdAttributeList;
import com.dfoda.otimizadores.ca.inter.ParseObserver;
import com.dfoda.util.ByteArray;
import com.dfoda.util.Hex;
import com.dfoda.otimizadores.ca.inter.Attribute;
import com.dex.util.ErroCtx;

public final class AttributeListParser {
    private final DirectClassFile cf;
    private final int context;
    private final int offset;
    private final AttributeFactory attributeFactory;
    private final StdAttributeList list;
    private int endOffset;
    private ParseObserver observer;

    public AttributeListParser(DirectClassFile cf, int context, int offset, AttributeFactory attributeFactory) {
        if (cf == null) {
            throw new NullPointerException("cf == null");
        }
        if (attributeFactory == null) {
            throw new NullPointerException("attributeFactory == null");
        }
        int size = cf.getBytes().getUnsignedShort(offset);
        this.cf = cf;
        this.context = context;
        this.offset = offset;
        this.attributeFactory = attributeFactory;
        this.list = new StdAttributeList(size);
        this.endOffset = -1;
    }

    public void setObserver(ParseObserver observer) {
        this.observer = observer;
    }

    public int getEndOffset() {
        this.parseIfNecessary();
        return this.endOffset;
    }

    public StdAttributeList getList() {
        this.parseIfNecessary();
        return this.list;
    }

    private void parseIfNecessary() {
        if (this.endOffset < 0) {
            this.parse();
        }
    }

    private void parse() {
        int sz = this.list.size();
        int at = this.offset + 2;
        ByteArray bytes = this.cf.getBytes();
        if (this.observer != null) {
            this.observer.parsed(bytes, this.offset, 2, "attributes_count: " + Hex.u2(sz));
        }
        for (int i = 0; i < sz; ++i) {
            try {
                if (this.observer != null) {
                    this.observer.parsed(bytes, at, 0, "\nattributes[" + i + "]:\n");
                    this.observer.changeIndent(1);
                }
                Attribute attrib = this.attributeFactory.parse(this.cf, this.context, at, this.observer);
                at += attrib.byteLength();
                this.list.set(i, attrib);
                if (this.observer == null) continue;
                this.observer.changeIndent(-1);
                this.observer.parsed(bytes, at, 0, "end attributes[" + i + "]\n");
                continue;
            }
            catch (ErroCtx ex) {
                ex.addContext("...while parsing attributes[" + i + "]");
                throw ex;
            }
            catch (RuntimeException ex) {
                ErroCtx pe = new ErroCtx(ex);
                pe.addContext("...while parsing attributes[" + i + "]");
                throw pe;
            }
        }
        this.endOffset = at;
    }
}

