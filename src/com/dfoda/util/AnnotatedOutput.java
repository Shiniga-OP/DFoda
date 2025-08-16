package com.dfoda.util;

public interface AnnotatedOutput extends Output {
    public boolean annotates();
    public boolean isVerbose();
    public void annotate(String var1);
    public void annotate(int var1, String var2);
    public void endAnnotation();
    public int getAnnotationWidth();
}

