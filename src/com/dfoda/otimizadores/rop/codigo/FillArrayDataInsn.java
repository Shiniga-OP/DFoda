package com.dfoda.otimizadores.rop.codigo;

import java.util.ArrayList;
import com.dfoda.otimizadores.rop.cst.Constant;
import com.dfoda.otimizadores.rop.tipo.TypeList;
import com.dfoda.otimizadores.rop.tipo.StdTypeList;
import com.dfoda.otimizadores.rop.tipo.Type;

public final class FillArrayDataInsn extends Insn {
    private final ArrayList<Constant> initValues;
    private final Constant arrayType;

    public FillArrayDataInsn(Rop opcode, SourcePosition position, RegisterSpecList sources, ArrayList<Constant> initValues, Constant cst) {
        super(opcode, position, null, sources);
        if (opcode.getBranchingness() != 1) {
            throw new IllegalArgumentException("opcode with invalid branchingness: " + opcode.getBranchingness());
        }
        this.initValues = initValues;
        this.arrayType = cst;
    }

    @Override
    public TypeList getCatches() {
        return StdTypeList.EMPTY;
    }

    public ArrayList<Constant> getInitValues() {
        return this.initValues;
    }

    public Constant getConstant() {
        return this.arrayType;
    }

    @Override
    public void accept(Insn.Visitor visitor) {
        visitor.visitFillArrayDataInsn(this);
    }

    @Override
    public Insn withAddedCatch(Type type) {
        throw new UnsupportedOperationException("unsupported");
    }

    @Override
    public Insn withRegisterOffset(int delta) {
        return new FillArrayDataInsn(this.getOpcode(), this.getPosition(), this.getSources().withOffset(delta), this.initValues, this.arrayType);
    }

    @Override
    public Insn withNewRegisters(RegisterSpec result, RegisterSpecList sources) {
        return new FillArrayDataInsn(this.getOpcode(), this.getPosition(), sources, this.initValues, this.arrayType);
    }
}

