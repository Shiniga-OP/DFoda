package com.dfoda.dex.ca;

import java.io.PrintStream;

public class CfOptions {
    public int positionInfo = 2;
    public boolean localInfo = false;
    public boolean strictNameCheck = true;
    public boolean optimize = false;
    public String optimizeListFile = null;
    public String dontOptimizeListFile = null;
    public boolean statistics;
    public PrintStream warn = System.err;
}

