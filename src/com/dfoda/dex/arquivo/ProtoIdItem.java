package com.dfoda.dex.arquivo;

import com.dfoda.otimizadores.rop.cst.CstString;
import com.dfoda.otimizadores.rop.tipo.Prototype;
import com.dfoda.otimizadores.rop.tipo.StdTypeList;
import com.dfoda.otimizadores.rop.tipo.Type;
import com.dfoda.util.AnnotatedOutput;
import com.dfoda.util.Hex;

public final class ProtoIdItem extends IndexedItem {
    private final Prototype prototype;
    private final CstString shortForm;
    private TypeListItem parameterTypes;

    public ProtoIdItem(Prototype prototype) {
        if (prototype == null) {
            throw new NullPointerException("prototype == null");
        }
        this.prototype = prototype;
        this.shortForm = ProtoIdItem.makeShortForm(prototype);
        StdTypeList parameters = prototype.getParameterTypes();
        this.parameterTypes = parameters.size() == 0 ? null : new TypeListItem(parameters);
    }

    private static CstString makeShortForm(Prototype prototype) {
        StdTypeList parameters = prototype.getParameterTypes();
        int size = parameters.size();
        StringBuilder sb = new StringBuilder(size + 1);
        sb.append(ProtoIdItem.shortFormCharFor(prototype.getReturnType()));
        for (int i = 0; i < size; ++i) {
            sb.append(ProtoIdItem.shortFormCharFor(parameters.getType(i)));
        }
        return new CstString(sb.toString());
    }

    private static char shortFormCharFor(Type type) {
        char descriptorChar = type.getDescriptor().charAt(0);
        if (descriptorChar == '[') {
            return 'L';
        }
        return descriptorChar;
    }

    @Override
    public ItemType itemType() {
        return ItemType.TYPE_PROTO_ID_ITEM;
    }

    @Override
    public int writeSize() {
        return 12;
    }

    @Override
    public void addContents(DexFile file) {
        StringIdsSection stringIds = file.getStringIds();
        TypeIdsSection typeIds = file.getTypeIds();
        MixedItemSection typeLists = file.getTypeLists();
        typeIds.intern(this.prototype.getReturnType());
        stringIds.intern(this.shortForm);
        if (this.parameterTypes != null) {
            this.parameterTypes = typeLists.intern(this.parameterTypes);
        }
    }

    @Override
    public void writeTo(DexFile file, AnnotatedOutput out) {
        int shortyIdx = file.getStringIds().indexOf(this.shortForm);
        int returnIdx = file.getTypeIds().indexOf(this.prototype.getReturnType());
        int paramsOff = OffsettedItem.getAbsoluteOffsetOr0(this.parameterTypes);
        if (out.annotates()) {
            StringBuilder sb = new StringBuilder();
            sb.append(this.prototype.getReturnType().toHuman());
            sb.append(" proto(");
            StdTypeList params = this.prototype.getParameterTypes();
            int size = params.size();
            for (int i = 0; i < size; ++i) {
                if (i != 0) {
                    sb.append(", ");
                }
                sb.append(params.getType(i).toHuman());
            }
            sb.append(")");
            out.annotate(0, this.indexString() + ' ' + sb.toString());
            out.annotate(4, "  shorty_idx:      " + Hex.u4(shortyIdx) + " // " + this.shortForm.toQuoted());
            out.annotate(4, "  return_type_idx: " + Hex.u4(returnIdx) + " // " + this.prototype.getReturnType().toHuman());
            out.annotate(4, "  parameters_off:  " + Hex.u4(paramsOff));
        }
        out.writeInt(shortyIdx);
        out.writeInt(returnIdx);
        out.writeInt(paramsOff);
    }
}

