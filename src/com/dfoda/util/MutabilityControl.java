package com.dfoda.util;

import com.dex.util.ErroCtx;

public class MutabilityControl {
    public boolean mutavel;

    public MutabilityControl() {
        this.mutavel = true;
    }

    public MutabilityControl(boolean mutavel) {
        this.mutavel = mutavel;
    }

    public void setImmutable() {
        this.mutavel = false;
    }

    public final boolean isImmutable() {
        return !this.mutavel;
    }

    public final boolean isMutable() {
        return this.mutavel;
    }

    public final void throwIfImmutable() {
        if(!this.mutavel) throw new ErroCtx("Instancia imutavel");
    }

    public final void throwIfMutable() {
        if(this.mutavel) throw new ErroCtx("Instancia mutavel");
    }
}

