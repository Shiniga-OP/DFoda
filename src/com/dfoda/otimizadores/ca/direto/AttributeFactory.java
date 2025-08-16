package com.dfoda.otimizadores.ca.direto;

import com.dfoda.otimizadores.ca.inter.Attribute;
import com.dfoda.otimizadores.ca.inter.ParseObserver;
import com.dfoda.util.ByteArray;
import com.dfoda.otimizadores.rop.cst.CstString;
import com.dfoda.otimizadores.rop.cst.ConstantPool;
import com.dfoda.util.Hex;
import com.dfoda.otimizadores.ca.atributo.RawAttribute;
import com.dex.util.ErroCtx;

public class AttributeFactory {
    public static final int CTX_CLASS = 0;
    public static final int CTX_FIELD = 1;
    public static final int CTX_METHOD = 2;
    public static final int CTX_CODE = 3;
    public static final int CTX_COUNT = 4;

    public final Attribute parse(DirectClassFile cf, int context, int offset, ParseObserver observer) {
        if (cf == null) {
            throw new NullPointerException("cf == null");
        }
        if (context < 0 || context >= 4) {
            throw new IllegalArgumentException("bad context");
        }
        CstString name = null;
        try {
            ByteArray bytes = cf.getBytes();
            ConstantPool pool = cf.getConstantPool();
            int nameIdx = bytes.getUnsignedShort(offset);
            int length = bytes.getInt(offset + 2);
            name = (CstString)pool.get(nameIdx);
            if (observer != null) {
                observer.parsed(bytes, offset, 2, "name: " + name.toHuman());
                observer.parsed(bytes, offset + 2, 4, "length: " + Hex.u4(length));
            }
            return this.parse0(cf, context, name.getString(), offset + 6, length, observer);
        }
        catch (ErroCtx ex) {
            ex.addContext("...while parsing " + (name != null ? name.toHuman() + " " : "") + "attribute at offset " + Hex.u4(offset));
            throw ex;
        }
    }

    protected Attribute parse0(DirectClassFile cf, int context, String name, int offset, int length, ParseObserver observer) {
        ByteArray bytes = cf.getBytes();
        ConstantPool pool = cf.getConstantPool();
        RawAttribute result = new RawAttribute(name, bytes, offset, length, pool);
        if (observer != null) {
            observer.parsed(bytes, offset, length, "attribute data");
        }
        return result;
    }
}

