package com.dfoda.otimizadores.rop.codigo;

import com.dfoda.otimizadores.rop.tipo.TypeList;
import com.dfoda.otimizadores.rop.cst.Constant;
import com.dfoda.otimizadores.rop.cst.CstString;
import com.dfoda.otimizadores.rop.tipo.Type;

public final class ThrowingCstInsn extends CstInsn {
    private final TypeList catches;

    public ThrowingCstInsn(Rop opcode, SourcePosition position, RegisterSpecList sources, TypeList catches, Constant cst) {
        super(opcode, position, null, sources, cst);
        if (opcode.getBranchingness() != 6) {
            throw new IllegalArgumentException("opcode with invalid branchingness: " + opcode.getBranchingness());
        }
        if (catches == null) {
            throw new NullPointerException("catches == null");
        }
        this.catches = catches;
    }

    @Override
    public String getInlineString() {
        Constant cst = this.getConstant();
        String constantString = cst.toHuman();
        if (cst instanceof CstString) {
            constantString = ((CstString)cst).toQuoted();
        }
        return constantString + " " + ThrowingInsn.toCatchString(this.catches);
    }

    @Override
    public TypeList getCatches() {
        return this.catches;
    }

    @Override
    public void accept(Insn.Visitor visitor) {
        visitor.visitThrowingCstInsn(this);
    }

    @Override
    public Insn withAddedCatch(Type type) {
        return new ThrowingCstInsn(this.getOpcode(), this.getPosition(), this.getSources(), this.catches.withAddedType(type), this.getConstant());
    }

    @Override
    public Insn withRegisterOffset(int delta) {
        return new ThrowingCstInsn(this.getOpcode(), this.getPosition(), this.getSources().withOffset(delta), this.catches, this.getConstant());
    }

    @Override
    public Insn withNewRegisters(RegisterSpec result, RegisterSpecList sources) {
        return new ThrowingCstInsn(this.getOpcode(), this.getPosition(), sources, this.catches, this.getConstant());
    }
}

