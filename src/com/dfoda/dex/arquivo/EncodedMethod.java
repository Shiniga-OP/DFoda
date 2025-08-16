package com.dfoda.dex.arquivo;

import java.io.PrintWriter;
import com.dfoda.otimizadores.rop.cst.CstMethodRef;
import com.dfoda.util.Hex;
import com.dfoda.otimizadores.rop.cst.CstString;
import com.dfoda.util.AnnotatedOutput;
import com.dex.Leb128;
import com.dfoda.otimizadores.rop.codigo.AccessFlags;
import com.dfoda.dex.codigo.DalvCode;
import com.dfoda.otimizadores.rop.tipo.TypeList;

public final class EncodedMethod extends EncodedMember implements Comparable<EncodedMethod> {
    private final CstMethodRef method;
    private final CodeItem code;

    public EncodedMethod(CstMethodRef method, int accessFlags, DalvCode code, TypeList throwsList) {
        super(accessFlags);
        if (method == null) {
            throw new NullPointerException("method == null");
        }
        this.method = method;
        if (code == null) {
            this.code = null;
        } else {
            boolean isStatic = (accessFlags & 8) != 0;
            this.code = new CodeItem(method, code, isStatic, throwsList);
        }
    }

    public boolean equals(Object other) {
        if (!(other instanceof EncodedMethod)) {
            return false;
        }
        return this.compareTo((EncodedMethod)other) == 0;
    }

    @Override
    public int compareTo(EncodedMethod other) {
        return this.method.compareTo(other.method);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(100);
        sb.append(this.getClass().getName());
        sb.append('{');
        sb.append(Hex.u2(this.getAccessFlags()));
        sb.append(' ');
        sb.append(this.method);
        if (this.code != null) {
            sb.append(' ');
            sb.append(this.code);
        }
        sb.append('}');
        return sb.toString();
    }

    @Override
    public void addContents(DexFile file) {
        MethodIdsSection methodIds = file.getMethodIds();
        MixedItemSection wordData = file.getWordData();
        methodIds.intern(this.method);
        if (this.code != null) {
            wordData.add(this.code);
        }
    }

    @Override
    public final String toHuman() {
        return this.method.toHuman();
    }

    @Override
    public final CstString getName() {
        return this.method.getNat().getName();
    }

    @Override
    public void debugPrint(PrintWriter out, boolean verbose) {
        if (this.code == null) {
            out.println(this.getRef().toHuman() + ": abstract or native");
        } else {
            this.code.debugPrint(out, "  ", verbose);
        }
    }

    public final CstMethodRef getRef() {
        return this.method;
    }

    @Override
    public int encode(DexFile file, AnnotatedOutput out, int lastIndex, int dumpSeq) {
        boolean shouldHaveCode;
        int methodIdx = file.getMethodIds().indexOf(this.method);
        int diff = methodIdx - lastIndex;
        int accessFlags = this.getAccessFlags();
        int codeOff = OffsettedItem.getAbsoluteOffsetOr0(this.code);
        boolean hasCode = codeOff != 0;
        shouldHaveCode = (accessFlags & 0x500) == 0;
        if (hasCode != shouldHaveCode) {
            throw new UnsupportedOperationException("code vs. access_flags mismatch");
        }
        if (out.annotates()) {
            out.annotate(0, String.format("  [%x] %s", dumpSeq, this.method.toHuman()));
            out.annotate(Leb128.unsignedLeb128Size(diff), "    method_idx:   " + Hex.u4(methodIdx));
            out.annotate(Leb128.unsignedLeb128Size(accessFlags), "    access_flags: " + AccessFlags.methodString(accessFlags));
            out.annotate(Leb128.unsignedLeb128Size(codeOff), "    code_off:     " + Hex.u4(codeOff));
        }
        out.writeUleb128(diff);
        out.writeUleb128(accessFlags);
        out.writeUleb128(codeOff);
        return methodIdx;
    }
}

