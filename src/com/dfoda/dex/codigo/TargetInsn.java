package com.dfoda.dex.codigo;

import com.dfoda.otimizadores.rop.codigo.RegisterSpecList;
import com.dfoda.otimizadores.rop.codigo.SourcePosition;

public final class TargetInsn extends FixedSizeInsn {
    public CodeAddress target;

    public TargetInsn(Dop opcode, SourcePosition position, RegisterSpecList registers, CodeAddress target) {
        super(opcode, position, registers);
        if(target == null) throw new NullPointerException("target == null");
        this.target = target;
    }

    @Override
    public DalvInsn withOpcode(Dop opcode) {
        return new TargetInsn(opcode, this.getPosition(), this.getRegisters(), this.target);
    }

    @Override
    public DalvInsn withRegisters(RegisterSpecList registers) {
        return new TargetInsn(this.getOpcode(), this.getPosition(), registers, this.target);
    }

    public TargetInsn withNewTargetAndReversed(CodeAddress target) {
        Dop opcode = this.getOpcode().getOppositeTest();
        return new TargetInsn(opcode, this.getPosition(), this.getRegisters(), target);
    }

    public CodeAddress getTarget() {
        return this.target;
    }

    public int getTargetAddress() {
        return this.target.getAddress();
    }

    public int getTargetOffset() {
        return this.target.getAddress() - this.getAddress();
    }

    public boolean hasTargetOffset() {
        return this.hasAddress() && this.target.hasAddress();
    }

    @Override
    protected String argString() {
        if (this.target == null) {
            return "????";
        }
        return this.target.identifierString();
    }
}

