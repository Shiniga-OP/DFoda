package com.dfoda.junta;

import com.dfoda.util.CodeReader;
import com.dfoda.util.instrucoes.DecodedInstruction;
import com.dfoda.util.instrucoes.ShortArrayCodeOutput;
import com.dex.util.ErroCtx;

public final class InstructionTransformer {
    private final CodeReader reader = new CodeReader();
    private DecodedInstruction[] mappedInstructions;
    private int mappedAt;
    private IndexMap indexMap;

    public InstructionTransformer() {
        this.reader.setAllVisitors(new GenericVisitor());
        this.reader.setStringVisitor(new StringVisitor());
        this.reader.setTypeVisitor(new TypeVisitor());
        this.reader.setFieldVisitor(new FieldVisitor());
        this.reader.setMethodVisitor(new MethodVisitor());
        this.reader.setMethodAndProtoVisitor(new MethodAndProtoVisitor());
        this.reader.setCallSiteVisitor(new CallSiteVisitor());
    }

    public short[] transform(IndexMap indexMap, short[] encodedInstructions) throws ErroCtx {
        DecodedInstruction[] decodedInstructions = DecodedInstruction.decodeAll(encodedInstructions);
        int size = decodedInstructions.length;
        this.indexMap = indexMap;
        this.mappedInstructions = new DecodedInstruction[size];
        this.mappedAt = 0;
        this.reader.visitAll(decodedInstructions);
        ShortArrayCodeOutput out = new ShortArrayCodeOutput(size);
        for (DecodedInstruction instruction : this.mappedInstructions) {
            if (instruction == null) continue;
            instruction.encode(out);
        }
        this.indexMap = null;
        return out.getArray();
    }

    private static void jumboCheck(boolean isJumbo, int newIndex) {
        if (!isJumbo && newIndex > 65535) {
            throw new ErroCtx("Cannot merge new index " + newIndex + " into a non-jumbo instruction!");
        }
    }

    public class CallSiteVisitor implements CodeReader.Visitor {
        @Override
        public void visit(DecodedInstruction[] all, DecodedInstruction one) {
            int callSiteId = one.getIndex();
            int mappedCallSiteId = InstructionTransformer.this.indexMap.adjustCallSite(callSiteId);
            InstructionTransformer.this.mappedInstructions[InstructionTransformer.this.mappedAt++] = one.withIndex(mappedCallSiteId);
        }
    }

    public class MethodAndProtoVisitor implements CodeReader.Visitor {
        @Override
        public void visit(DecodedInstruction[] all, DecodedInstruction one) {
            int methodId = one.getIndex();
            short protoId = one.getProtoIndex();
            InstructionTransformer.this.mappedInstructions[InstructionTransformer.this.mappedAt++] = one.withProtoIndex(InstructionTransformer.this.indexMap.adjustMethod(methodId), InstructionTransformer.this.indexMap.adjustProto(protoId));
        }
    }

    public class MethodVisitor implements CodeReader.Visitor {
        @Override
        public void visit(DecodedInstruction[] all, DecodedInstruction one) {
            int methodId = one.getIndex();
            int mappedId = InstructionTransformer.this.indexMap.adjustMethod(methodId);
            boolean isJumbo = one.getOpcode() == 27;
            InstructionTransformer.jumboCheck(isJumbo, mappedId);
            InstructionTransformer.this.mappedInstructions[InstructionTransformer.this.mappedAt++] = one.withIndex(mappedId);
        }
    }

    public class TypeVisitor implements CodeReader.Visitor {
        @Override
        public void visit(DecodedInstruction[] all, DecodedInstruction one) {
            int typeId = one.getIndex();
            int mappedId = InstructionTransformer.this.indexMap.adjustType(typeId);
            boolean isJumbo = one.getOpcode() == 27;
            InstructionTransformer.jumboCheck(isJumbo, mappedId);
            InstructionTransformer.this.mappedInstructions[InstructionTransformer.this.mappedAt++] = one.withIndex(mappedId);
        }
    }

    public class FieldVisitor implements CodeReader.Visitor {
        @Override
        public void visit(DecodedInstruction[] all, DecodedInstruction one) {
            int fieldId = one.getIndex();
            int mappedId = InstructionTransformer.this.indexMap.adjustField(fieldId);
            boolean isJumbo = one.getOpcode() == 27;
            InstructionTransformer.jumboCheck(isJumbo, mappedId);
            InstructionTransformer.this.mappedInstructions[InstructionTransformer.this.mappedAt++] = one.withIndex(mappedId);
        }
    }

    public class StringVisitor implements CodeReader.Visitor {
        @Override
        public void visit(DecodedInstruction[] all, DecodedInstruction one) {
            int stringId = one.getIndex();
            int mappedId = InstructionTransformer.this.indexMap.adjustString(stringId);
            boolean isJumbo = one.getOpcode() == 27;
            InstructionTransformer.jumboCheck(isJumbo, mappedId);
            InstructionTransformer.this.mappedInstructions[InstructionTransformer.this.mappedAt++] = one.withIndex(mappedId);
        }
    }

    public class GenericVisitor implements CodeReader.Visitor {
        @Override
        public void visit(DecodedInstruction[] all, DecodedInstruction one) {
            InstructionTransformer.this.mappedInstructions[InstructionTransformer.this.mappedAt++] = one;
        }
    }
}

