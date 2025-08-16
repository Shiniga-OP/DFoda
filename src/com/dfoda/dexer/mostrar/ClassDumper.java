package com.dfoda.dexer.mostrar;

import java.io.PrintStream;
import com.dfoda.util.ByteArray;
import com.dfoda.otimizadores.ca.direto.DirectClassFile;
import com.dfoda.otimizadores.ca.direto.StdAttributeFactory;

public final class ClassDumper extends BaseDumper {
    public static void dump(byte[] bytes, PrintStream out, String filePath, Args args) {
        ClassDumper cd = new ClassDumper(bytes, out, filePath, args);
        cd.dump();
    }

    private ClassDumper(byte[] bytes, PrintStream out, String filePath, Args args) {
        super(bytes, out, filePath, args);
    }

    public void dump() {
        byte[] bytes = this.getBytes();
        ByteArray ba = new ByteArray(bytes);
        DirectClassFile cf = new DirectClassFile(ba, this.getFilePath(), this.getStrictParse());
        cf.setAttributeFactory(StdAttributeFactory.THE_ONE);
        cf.setObserver(this);
        cf.getMagic();
        int readBytes = this.getReadBytes();
        if (readBytes != bytes.length) {
            this.parsed(ba, readBytes, bytes.length - readBytes, "<extra data at end of file>");
        }
    }
}

