package com.dfoda.util.instrucoes;

import com.dfoda.util.IndexType;
import com.dfoda.util.OpcodeInfo;
import com.dfoda.util.Opcodes;
import com.dfoda.util.instrucoes.CodeInput;
import com.dfoda.util.instrucoes.CodeOutput;
import com.dfoda.util.instrucoes.InstructionCodec;
import com.dfoda.util.instrucoes.ShortArrayCodeInput;
import com.dfoda.util.Hex;
import java.io.EOFException;
import com.dex.util.ErroCtx;

public abstract class DecodedInstruction {
    public final InstructionCodec format;
    public final int opcode;
    public final int index;
    public final IndexType indexType;
    public final int target;
    public final long literal;

    public static DecodedInstruction decode(CodeInput in) throws EOFException {
        int opcodeUnit = in.read();
        int opcode = Opcodes.extractOpcodeFromUnit(opcodeUnit);
        InstructionCodec format = OpcodeInfo.getFormat(opcode);
        return format.decode(opcodeUnit, in);
    }

    public static DecodedInstruction[] decodeAll(short[] encodedInstructions) {
        int size = encodedInstructions.length;
        DecodedInstruction[] decoded = new DecodedInstruction[size];
        ShortArrayCodeInput in = new ShortArrayCodeInput(encodedInstructions);
        try {
            while (in.hasMore()) {
                decoded[in.cursor()] = DecodedInstruction.decode(in);
            }
        } catch(EOFException e) {
            throw new ErroCtx("Erro: "+e);
        }
        return decoded;
    }

    public DecodedInstruction(InstructionCodec formato, int opcode, int indice, IndexType indiceTipo, int marca, long literal) {
        if(formato == null) throw new NullPointerException("formato == null");
        if(!Opcodes.isValidShape(opcode)) throw new IllegalArgumentException("invalid opcode");
		
        this.format = formato;
        this.opcode = opcode;
        this.index = indice;
        this.indexType = indiceTipo;
        this.target = marca;
        this.literal = literal;
    }

    public final InstructionCodec getFormat() {
        return this.format;
    }

    public final int getOpcode() {
        return this.opcode;
    }

    public final short getOpcodeUnit() {
        return (short)this.opcode;
    }

    public final int getIndex() {
        return this.index;
    }

    public final short getIndexUnit() {
        return (short)this.index;
    }

    public final IndexType getIndexType() {
        return this.indexType;
    }

    public final int getTarget() {
        return this.target;
    }

    public final int getTarget(int baseAddress) {
        return this.target - baseAddress;
    }

    public final short getTargetUnit(int baseAddress) {
        int relativeTarget = this.getTarget(baseAddress);
        if (relativeTarget != (short)relativeTarget) {
            throw new ErroCtx("Target out of range: " + Hex.s4(relativeTarget));
        }
        return (short)relativeTarget;
    }

    public final int getTargetByte(int baseAddress) {
        int relativeTarget = this.getTarget(baseAddress);
        if (relativeTarget != (byte)relativeTarget) {
            throw new ErroCtx("Target out of range: " + Hex.s4(relativeTarget));
        }
        return relativeTarget & 0xFF;
    }

    public final long getLiteral() {
        return this.literal;
    }

    public final int getLiteralInt() {
        if(this.literal != (long)((int)this.literal)) throw new ErroCtx("Literal fora da desgraça do alcance: " + Hex.u8(this.literal));
        return (int)this.literal;
    }

    public final short getLiteralUnit() {
        if(this.literal != (long)((short)this.literal)) throw new ErroCtx("Literal fora da porra do alcance: " + Hex.u8(this.literal));
        return (short)this.literal;
    }

    public final int getLiteralByte() {
        if(this.literal != (long)((byte)this.literal)) throw new ErroCtx("Literal fora de alcance: " + Hex.u8(this.literal));
        return (int)this.literal & 0xFF;
    }

    public final int getLiteralNibble() {
        if(this.literal < -8 || this.literal > 7) throw new ErroCtx("Literal fora de alcance: " + Hex.u8(this.literal));
        return (int)this.literal & 0xF;
    }

    public abstract int getRegisterCount();

    public int getA() {
        return 0;
    }

    public int getB() {
        return 0;
    }

    public int getC() {
        return 0;
    }

    public int getD() {
        return 0;
    }

    public int getE() {
        return 0;
    }

    public final short getRegisterCountUnit() {
        int registerCount = this.getRegisterCount();
        if((registerCount & 0xFFFF0000) != 0) throw new ErroCtx("Register count out of range: " + Hex.u8(registerCount));
        return (short)registerCount;
    }

    public final short getAUnit() {
        int a = this.getA();
        if((a & 0xFFFF0000) != 0) throw new ErroCtx("Register A out of range: " + Hex.u8(a));
        return (short)a;
    }

    public final short getAByte() {
        int a = this.getA();
        if((a & 0xFFFFFF00) != 0) throw new ErroCtx("Register A out of range: " + Hex.u8(a));
        return (short)a;
    }

    public final short getANibble() {
        int a = this.getA();
        if((a & 0xFFFFFFF0) != 0) throw new ErroCtx("Register A out of range: " + Hex.u8(a));
        return (short)a;
    }

    public final short getBUnit() {
        int b = this.getB();
        if((b & 0xFFFF0000) != 0) throw new ErroCtx("Register B out of range: " + Hex.u8(b));
        return (short)b;
    }

    public final short getBByte() {
        int b = this.getB();
        if((b & 0xFFFFFF00) != 0) throw new ErroCtx("Register B out of range: " + Hex.u8(b));
        return (short)b;
    }

    public final short getBNibble() {
        int b = this.getB();
        if((b & 0xFFFFFFF0) != 0) throw new ErroCtx("Register B out of range: " + Hex.u8(b));
        return (short)b;
    }

    public final short getCUnit() {
        int c = this.getC();
        if((c & 0xFFFF0000) != 0) throw new ErroCtx("Register C out of range: " + Hex.u8(c));
        return (short)c;
    }

    public final short getCByte() {
        int c = this.getC();
        if((c & 0xFFFFFF00) != 0) throw new ErroCtx("Register C out of range: " + Hex.u8(c));
        return (short)c;
    }

    public final short getCNibble() {
        int c = this.getC();
        if((c & 0xFFFFFFF0) != 0) throw new ErroCtx("Register C out of range: " + Hex.u8(c));
        return (short)c;
    }

    public final short getDUnit() {
        int d = this.getD();
        if((d & 0xFFFF0000) != 0) throw new ErroCtx("Register D out of range: " + Hex.u8(d));
        return (short)d;
    }

    public final short getDByte() {
        int d = this.getD();
        if((d & 0xFFFFFF00) != 0) throw new ErroCtx("Register D out of range: " + Hex.u8(d));
        return (short)d;
    }

    public final short getDNibble() {
        int d = this.getD();
        if((d & 0xFFFFFFF0) != 0) throw new ErroCtx("Register D out of range: " + Hex.u8(d));
        return (short)d;
    }

    public final short getENibble() {
        int e = this.getE();
        if((e & 0xFFFFFFF0) != 0) throw new ErroCtx("Register E out of range: " + Hex.u8(e));
        return (short)e;
    }

    public final void encode(CodeOutput out) {
        this.format.encode(this, out);
    }

    public abstract DecodedInstruction withIndex(int var1);

    public DecodedInstruction withProtoIndex(int newIndex, int newProtoIndex) {
        throw new IllegalStateException(this.getClass().toString());
    }

    public short getProtoIndex() {
        throw new IllegalStateException(this.getClass().toString());
    }
}

