package com.dfoda.dex.codigo;

import com.dfoda.otimizadores.rop.codigo.RegisterSpec;
import com.dfoda.otimizadores.rop.codigo.SourcePosition;
import com.dfoda.otimizadores.rop.codigo.RegisterSpecList;
import com.dfoda.otimizadores.ssa.RegisterMapper;

public final class LocalStart extends ZeroSizeInsn {
    private final RegisterSpec local;

    public static String localString(RegisterSpec spec) {
        return spec.regString() + ' ' + spec.getLocalItem().toString() + ": " + spec.getTypeBearer().toHuman();
    }

    public LocalStart(SourcePosition position, RegisterSpec local) {
        super(position);
        if (local == null) {
            throw new NullPointerException("local == null");
        }
        this.local = local;
    }

    @Override
    public DalvInsn withRegisterOffset(int delta) {
        return new LocalStart(this.getPosition(), this.local.withOffset(delta));
    }

    @Override
    public DalvInsn withRegisters(RegisterSpecList registers) {
        return new LocalStart(this.getPosition(), this.local);
    }

    public RegisterSpec getLocal() {
        return this.local;
    }

    @Override
    protected String argString() {
        return this.local.toString();
    }

    @Override
    protected String listingString0(boolean noteIndices) {
        return "local-start " + LocalStart.localString(this.local);
    }

    @Override
    public DalvInsn withMapper(RegisterMapper mapper) {
        return new LocalStart(this.getPosition(), mapper.map(this.local));
    }
}

