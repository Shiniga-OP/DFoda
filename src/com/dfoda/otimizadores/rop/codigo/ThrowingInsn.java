package com.dfoda.otimizadores.rop.codigo;

import com.dfoda.otimizadores.rop.tipo.TypeList;
import com.dfoda.otimizadores.rop.tipo.Type;

public final class ThrowingInsn extends Insn {
    private final TypeList catches;

    public static String toCatchString(TypeList catches) {
        StringBuilder sb = new StringBuilder(100);
        sb.append("catch");
        int sz = catches.size();
        for (int i = 0; i < sz; ++i) {
            sb.append(" ");
            sb.append(catches.getType(i).toHuman());
        }
        return sb.toString();
    }

    public ThrowingInsn(Rop opcode, SourcePosition position, RegisterSpecList sources, TypeList catches) {
        super(opcode, position, null, sources);
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
        return ThrowingInsn.toCatchString(this.catches);
    }

    @Override
    public TypeList getCatches() {
        return this.catches;
    }

    @Override
    public void accept(Insn.Visitor visitor) {
        visitor.visitThrowingInsn(this);
    }

    @Override
    public Insn withAddedCatch(Type type) {
        return new ThrowingInsn(this.getOpcode(), this.getPosition(), this.getSources(), this.catches.withAddedType(type));
    }

    @Override
    public Insn withRegisterOffset(int delta) {
        return new ThrowingInsn(this.getOpcode(), this.getPosition(), this.getSources().withOffset(delta), this.catches);
    }

    @Override
    public Insn withNewRegisters(RegisterSpec result, RegisterSpecList sources) {
        return new ThrowingInsn(this.getOpcode(), this.getPosition(), sources, this.catches);
    }
}

