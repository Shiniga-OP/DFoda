package com.dex;

import com.dex.util.Unsigned;

public class CallSiteId implements Comparable<CallSiteId> {
    public final Dex dex;
    public final int offset;

    public CallSiteId(Dex dex, int offset) {
        this.dex = dex;
        this.offset = offset;
    }

    @Override
    public int compareTo(CallSiteId o) {
        return Unsigned.compare(this.offset, o.offset);
    }

    public int getCallSiteOffset() {
        return this.offset;
    }

    public void writeTo(Dex.Section out) {
        out.writeInt(this.offset);
    }

    public String toString() {
        if(this.dex == null) return String.valueOf(this.offset);
        return this.dex.protoIds().get(this.offset).toString();
    }
}

