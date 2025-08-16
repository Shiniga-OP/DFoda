package com.dfoda.otimizadores.ca.inter;

import com.dfoda.otimizadores.rop.cst.ConstantPool;
import com.dfoda.otimizadores.rop.cst.CstType;
import com.dfoda.otimizadores.rop.tipo.TypeList;
import com.dfoda.otimizadores.rop.cst.CstString;
import com.dfoda.otimizadores.ca.codigo.BootstrapMethodsList;

public interface ClassFile extends HasAttribute {
    public int getMagic();
    public int getMinorVersion();
    public int getMajorVersion();
    public int getAccessFlags();
    public CstType getThisClass();
    public CstType getSuperclass();
    public ConstantPool getConstantPool();
    public TypeList getInterfaces();
    public FieldList getFields();
    public MethodList getMethods();

    @Override
    public AttributeList getAttributes();
    public BootstrapMethodsList getBootstrapMethods();
    public CstString getSourceFile();
}

