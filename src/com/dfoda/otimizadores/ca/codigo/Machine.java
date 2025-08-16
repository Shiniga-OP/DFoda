package com.dfoda.otimizadores.ca.codigo;

import java.util.ArrayList;
import com.dfoda.otimizadores.rop.tipo.Prototype;
import com.dfoda.otimizadores.rop.tipo.Type;
import com.dfoda.otimizadores.rop.cst.Constant;
import com.dfoda.otimizadores.rop.codigo.LocalItem;

public interface Machine {
    public Prototype getPrototype();
    public void clearArgs();
    public void popArgs(Frame var1, int var2);
    public void popArgs(Frame var1, Prototype var2);
    public void popArgs(Frame var1, Type var2);
    public void popArgs(Frame var1, Type var2, Type var3);
    public void popArgs(Frame var1, Type var2, Type var3, Type var4);
    public void localArg(Frame var1, int var2);
    public void localInfo(boolean var1);
    public void auxType(Type var1);
    public void auxIntArg(int var1);
    public void auxCstArg(Constant var1);
    public void auxTargetArg(int var1);
    public void auxSwitchArg(SwitchList var1);
    public void auxInitValues(ArrayList<Constant> var1);
    public void localTarget(int var1, Type var2, LocalItem var3);
    public void run(Frame var1, int var2, int var3);
}

