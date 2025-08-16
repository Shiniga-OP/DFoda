package com.dfoda.dex.codigo;

import com.dfoda.otimizadores.rop.codigo.SourcePosition;
import com.dfoda.otimizadores.rop.codigo.RegisterSpecList;

public final class CodeAddress extends ZeroSizeInsn {
    public final boolean bindsClosely;

    public CodeAddress(SourcePosition position) {
        this(position, false);
    }

    public CodeAddress(SourcePosition position, boolean bindsClosely) {
        super(position);
        this.bindsClosely = bindsClosely;
    }

    @Override
    public final DalvInsn withRegisters(RegisterSpecList registers) {
        return new CodeAddress(this.getPosition());
    }

    @Override
    protected String argString() {
        return null;
    }

    @Override
    protected String listingString0(boolean noteIndices) {
        return "code-address";
    }

    public boolean getBindsClosely() {
        return this.bindsClosely;
    }
}

