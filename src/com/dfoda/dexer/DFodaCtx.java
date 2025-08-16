package com.dfoda.dexer;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import com.dfoda.dex.ca.CodeStatistics;
import com.dfoda.dex.ca.OptimizerOptions;

public class DFodaCtx {
    public final CodeStatistics codigoStatus = new CodeStatistics();
    public final OptimizerOptions opcoesOtimiza = new OptimizerOptions();
    public final PrintStream saida;
    public final PrintStream err;
    public final PrintStream noop = new PrintStream(new OutputStream(){
        @Override
        public void write(int b) throws IOException {}
    });

    public DFodaCtx(OutputStream saida, OutputStream err) {
        this.saida = new PrintStream(saida);
        this.err = new PrintStream(err);
    }

    public DFodaCtx() {
        this(System.out, System.err);
    }
}

