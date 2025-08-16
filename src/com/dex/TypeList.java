package com.dex;

import java.io.Serializable;
import com.dex.util.Unsigned;

public final class TypeList implements Comparable<TypeList> {
    public static final TypeList EMPTY = new TypeList(null, Dex.EMPTY_SHORT_ARRAY);
    private final Dex dex;
    private final short[] types;

    public TypeList(Dex dex, short[] types) {
        this.dex = dex;
        this.types = types;
    }

    public short[] getTypes() {
        return this.types;
    }

    @Override
    public int compareTo(TypeList other) {
        for (int i = 0; i < this.types.length && i < other.types.length; ++i) {
            if (this.types[i] == other.types[i]) continue;
            return Unsigned.compare(this.types[i], other.types[i]);
        }
        return Unsigned.compare(this.types.length, other.types.length);
    }

    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("(");
        int typesLength = this.types.length;
        for (int i = 0; i < typesLength; ++i) {
            result.append(this.dex != null ? (Serializable)((Object)this.dex.typeNames().get(this.types[i])) : Short.valueOf(this.types[i]));
        }
        result.append(")");
        return result.toString();
    }
}

