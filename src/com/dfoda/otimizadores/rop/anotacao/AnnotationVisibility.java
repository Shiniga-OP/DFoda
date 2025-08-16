package com.dfoda.otimizadores.rop.anotacao;

import com.dfoda.util.ToHuman;

public enum AnnotationVisibility implements ToHuman {
    RUNTIME("runtime"),
    BUILD("build"),
    SYSTEM("system"),
    EMBEDDED("embedded");

    public final String human;

    private AnnotationVisibility(String human) {
        this.human = human;
    }

    @Override
    public String toHuman() {
        return this.human;
    }
}

