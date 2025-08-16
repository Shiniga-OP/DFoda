package com.dfoda.dex.codigo;
import com.dfoda.otimizadores.rop.codigo.SourcePosition;
import com.dfoda.otimizadores.rop.codigo.RegisterSpecList;
import com.dfoda.util.AnnotatedOutput;

public final class OddSpacer extends VariableSizeInsn {
    public OddSpacer(SourcePosition position) {
        super(position, RegisterSpecList.EMPTY);
    }

    @Override
    public int codeSize() {
        return this.getAddress() & 1;
    }

    @Override
    public void writeTo(AnnotatedOutput out) {
        if (this.codeSize() != 0) {
            out.writeShort(InsnFormat.codeUnit(0, 0));
        }
    }

    @Override
    public DalvInsn withRegisters(RegisterSpecList registers) {
        return new OddSpacer(this.getPosition());
    }

    @Override
    protected String argString() {
        return null;
    }

    @Override
    protected String listingString0(boolean noteIndices) {
        if (this.codeSize() == 0) {
            return null;
        }
        return "nop // spacer";
    }
}

