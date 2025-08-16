package com.dfoda.dex.codigo;

import com.dfoda.otimizadores.rop.cst.Constant;
import com.dfoda.otimizadores.rop.cst.CstBaseMethodRef;
import com.dfoda.otimizadores.rop.cst.CstCallSiteRef;
import com.dfoda.otimizadores.rop.cst.CstProtoRef;
import com.dfoda.util.AnnotatedOutput;
import com.dfoda.util.FixedSizeList;
import com.dfoda.util.IndentingWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import com.dex.util.ErroCtx;

public final class DalvInsnList extends FixedSizeList {
    public final int regCount;

    public static DalvInsnList makeImmutable(ArrayList<DalvInsn> lista, int regCount) {
        int tam = lista.size();
        DalvInsnList resultado = new DalvInsnList(tam, regCount);
        for(int i = 0; i < tam; ++i) resultado.set(i, lista.get(i));
        resultado.setImmutable();
        return resultado;
    }

    public DalvInsnList(int size, int regCount) {
        super(size);
        this.regCount = regCount;
    }

    public DalvInsn get(int n) {
        return (DalvInsn)this.get0(n);
    }

    public void set(int n, DalvInsn insn) {
        this.set0(n, insn);
    }

    public int codeSize() {
        int sz = this.size();
        if (sz == 0) {
            return 0;
        }
        DalvInsn last = this.get(sz - 1);
        return last.getNextAddress();
    }

    public void writeTo(AnnotatedOutput out) {
        int startCursor = out.getCursor();
        int sz = this.size();
        if(out.annotates()) {
            boolean verbose = out.isVerbose();
            for(int i = 0; i < sz; ++i) {
                DalvInsn insn = (DalvInsn)this.get0(i);
                int codeBytes = insn.codeSize() * 2;
                String s = codeBytes != 0 || verbose ? insn.listingString("  ", out.getAnnotationWidth(), true) : null;
                if(s != null) {
                    out.annotate(codeBytes, s);
                    continue;
                }
                if(codeBytes == 0) continue;
                out.annotate(codeBytes, "");
            }
        }
        for(int i = 0; i < sz; ++i) {
            DalvInsn insn = (DalvInsn)this.get0(i);
            try {
                insn.writeTo(out);
                continue;
            } catch(RuntimeException e) {
                throw ErroCtx.comCtx(e, "Erro enquanto escrevia: " + insn);
            }
        }
        int written = (out.getCursor() - startCursor) / 2;
        if (written != this.codeSize()) {
            throw new RuntimeException("write length mismatch; expected " + this.codeSize() + " but actually wrote " + written);
        }
    }

    public int getRegistersSize() {
        return this.regCount;
    }

    public int getOutsSize() {
        int sz = this.size();
        int result = 0;
        for (int i = 0; i < sz; ++i) {
            DalvInsn insn = (DalvInsn)this.get0(i);
            int count = 0;
            if (insn instanceof CstInsn) {
                Constant cst = ((CstInsn)insn).getConstant();
                if (cst instanceof CstBaseMethodRef) {
                    CstBaseMethodRef methodRef = (CstBaseMethodRef)cst;
                    boolean isStatic = insn.getOpcode().getFamily() == 113;
                    count = methodRef.getParameterWordCount(isStatic);
                } else if (cst instanceof CstCallSiteRef) {
                    CstCallSiteRef invokeDynamicRef = (CstCallSiteRef)cst;
                    count = invokeDynamicRef.getPrototype().getParameterTypes().getWordCount();
                }
            } else {
                if (!(insn instanceof MultiCstInsn)) continue;
                if (insn.getOpcode().getFamily() != 250) {
                    throw new RuntimeException("Expecting invoke-polymorphic");
                }
                MultiCstInsn mci = (MultiCstInsn)insn;
                CstProtoRef proto = (CstProtoRef)mci.getConstant(1);
                count = proto.getPrototype().getParameterTypes().getWordCount();
                ++count;
            }
            if (count <= result) continue;
            result = count;
        }
        return result;
    }

    public void debugPrint(Writer out, String prefix, boolean verbose) {
        IndentingWriter iw = new IndentingWriter(out, 0, prefix);
        int sz = this.size();
        try {
            for (int i = 0; i < sz; ++i) {
                DalvInsn insn = (DalvInsn)this.get0(i);
                String s = insn.codeSize() != 0 || verbose ? insn.listingString("", 0, verbose) : null;
                if (s == null) continue;
                iw.write(s);
            }
            iw.flush();
        }
        catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void debugPrint(OutputStream out, String prefix, boolean verbose) {
        OutputStreamWriter w = new OutputStreamWriter(out);
        this.debugPrint(w, prefix, verbose);
        try {
            ((Writer)w).flush();
        }
        catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}

