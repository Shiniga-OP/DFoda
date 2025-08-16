package com.dfoda.otimizadores.ca.codigo;

import com.dex.util.ErroCtx;

public class SimException extends ErroCtx {
    public SimException(String message) {
        super(message);
    }

    public SimException(Throwable cause) {
        super(cause);
    }

    public SimException(String message, Throwable cause) {
        super(message, cause);
    }
}

