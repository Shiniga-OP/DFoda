package com.dfoda.util;

import java.io.PrintWriter;
import java.io.Writer;

public final class Writers {
    public static PrintWriter printWriterFor(Writer writer) {
        if (writer instanceof PrintWriter) {
            return (PrintWriter)writer;
        }
        return new PrintWriter(writer);
    }
}

