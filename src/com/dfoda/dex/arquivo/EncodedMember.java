package com.dfoda.dex.arquivo;

import java.io.PrintWriter;
import com.dfoda.util.ToHuman;
import com.dfoda.otimizadores.rop.cst.CstString;
import com.dfoda.util.AnnotatedOutput;

public abstract class EncodedMember implements ToHuman {
    private final int accessFlags;

    public EncodedMember(int accessFlags) {
        this.accessFlags = accessFlags;
    }

    public final int getAccessFlags() {
        return this.accessFlags;
    }

    public abstract CstString getName();
    public abstract void debugPrint(PrintWriter var1, boolean var2);
    public abstract void addContents(DexFile var1);
    public abstract int encode(DexFile var1, AnnotatedOutput var2, int var3, int var4);
}

