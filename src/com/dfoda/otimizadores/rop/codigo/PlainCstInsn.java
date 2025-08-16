package com.dfoda.otimizadores.rop.codigo;

import com.dfoda.otimizadores.rop.tipo.TypeList;
import com.dfoda.otimizadores.rop.tipo.StdTypeList;
import com.dfoda.otimizadores.rop.cst.Constant;
import com.dfoda.otimizadores.rop.tipo.Type;

public final class PlainCstInsn extends CstInsn {
    public PlainCstInsn(Rop opcode, SourcePosition position, RegisterSpec result, RegisterSpecList sources, Constant cst) {
        super(opcode, position, result, sources, cst);
        if (opcode.getBranchingness() != 1) {
            throw new IllegalArgumentException("opcode with invalid branchingness: " + opcode.getBranchingness());
        }
    }

    @Override
    public TypeList getCatches() {
        return StdTypeList.EMPTY;
    }

    @Override
    public void accept(Insn.Visitor visitor) {
        visitor.visitPlainCstInsn(this);
    }

    @Override
    public Insn withAddedCatch(Type type) {
        throw new UnsupportedOperationException("unsupported");
    }

    @Override
    public Insn withRegisterOffset(int delta) {
        return new PlainCstInsn(this.getOpcode(), this.getPosition(), this.getResult().withOffset(delta), this.getSources().withOffset(delta), this.getConstant());
    }

    @Override
    public Insn withNewRegisters(RegisterSpec result, RegisterSpecList sources) {
        return new PlainCstInsn(this.getOpcode(), this.getPosition(), result, sources, this.getConstant());
    }
}

