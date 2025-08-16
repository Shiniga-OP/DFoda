package com.dfoda.otimizadores.rop.codigo;

import com.dfoda.util.IntList;
import com.dfoda.otimizadores.rop.tipo.StdTypeList;
import com.dfoda.otimizadores.rop.tipo.TypeList;
import com.dfoda.otimizadores.rop.tipo.Type;

public final class SwitchInsn extends Insn {
    private final IntList cases;

    public SwitchInsn(Rop opcode, SourcePosition position, RegisterSpec result, RegisterSpecList sources, IntList cases) {
        super(opcode, position, result, sources);
        if (opcode.getBranchingness() != 5) {
            throw new IllegalArgumentException("bogus branchingness");
        }
        if (cases == null) {
            throw new NullPointerException("cases == null");
        }
        this.cases = cases;
    }

    @Override
    public String getInlineString() {
        return this.cases.toString();
    }

    @Override
    public TypeList getCatches() {
        return StdTypeList.EMPTY;
    }

    @Override
    public void accept(Insn.Visitor visitor) {
        visitor.visitSwitchInsn(this);
    }

    @Override
    public Insn withAddedCatch(Type type) {
        throw new UnsupportedOperationException("unsupported");
    }

    @Override
    public Insn withRegisterOffset(int delta) {
        return new SwitchInsn(this.getOpcode(), this.getPosition(), this.getResult().withOffset(delta), this.getSources().withOffset(delta), this.cases);
    }

    @Override
    public boolean contentEquals(Insn b) {
        return false;
    }

    @Override
    public Insn withNewRegisters(RegisterSpec result, RegisterSpecList sources) {
        return new SwitchInsn(this.getOpcode(), this.getPosition(), result, sources, this.cases);
    }

    public IntList getCases() {
        return this.cases;
    }
}

