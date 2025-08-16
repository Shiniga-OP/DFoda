package com.dex.util;

import java.io.PrintStream;
import java.io.PrintWriter;

public class ErroCtx extends RuntimeException {
    public StringBuffer ctx;

    public static ErroCtx comCtx(Throwable e, String str) {
        ErroCtx e2 = e instanceof ErroCtx ? (ErroCtx)e : new ErroCtx(e);
        e2.addContext(str);
        return e2;
    }

    public ErroCtx(String msg) {
        this(msg, null);
    }

    public ErroCtx(Throwable causa) {
        this(null, causa);
    }

    public ErroCtx(String msg, Throwable causa) {
        super(msg != null ? msg : (causa != null ? causa.getMessage() : null), causa);
        if(causa instanceof ErroCtx) {
            String ctx = ((ErroCtx)causa).ctx.toString();
            this.ctx = new StringBuffer(ctx.length() + 200);
            this.ctx.append(ctx);
        } else this.ctx = new StringBuffer(200);
    }

    @Override
    public void printStackTrace(PrintStream saida) {
        super.printStackTrace(saida);
        saida.println(this.ctx);
    }

    @Override
    public void printStackTrace(PrintWriter saida) {
        super.printStackTrace(saida);
        saida.println(this.ctx);
    }

    public void addContext(String str) {
        if(str == null) throw new NullPointerException("str == null");
        this.ctx.append(str);
        if(!str.endsWith("\n")) this.ctx.append('\n');
    }

    public void logCtx(PrintStream saida) {
        saida.println(this.getMessage());
        saida.print(this.ctx);
    }

    public void logCtx(PrintWriter saida) {
        saida.println(this.getMessage());
        saida.print(this.ctx);
    }
}

