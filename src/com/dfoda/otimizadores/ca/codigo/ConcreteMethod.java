package com.dfoda.otimizadores.ca.codigo;

import com.dfoda.otimizadores.ca.inter.Method;
import com.dfoda.otimizadores.ca.inter.AttributeList;
import com.dfoda.otimizadores.rop.cst.CstString;
import com.dfoda.otimizadores.rop.cst.CstNat;
import com.dfoda.otimizadores.rop.tipo.Prototype;
import com.dfoda.otimizadores.rop.cst.CstType;
import com.dfoda.otimizadores.rop.codigo.SourcePosition;
import com.dfoda.otimizadores.ca.atributo.AttCode;
import com.dfoda.otimizadores.ca.atributo.AttLineNumberTable;
import com.dfoda.otimizadores.ca.atributo.AttLocalVariableTable;
import com.dfoda.otimizadores.ca.atributo.AttLocalVariableTypeTable;
import com.dfoda.otimizadores.ca.inter.ClassFile;

public final class ConcreteMethod implements Method {
    private final Method method;
    private final ClassFile classFile;
    private final AttCode attCode;
    private final LineNumberList lineNumbers;
    private final LocalVariableList localVariables;

    public ConcreteMethod(Method method, ClassFile classFile, boolean keepLines, boolean keepLocals) {
        this.method = method;
        this.classFile = classFile;
        AttributeList attribs = method.getAttributes();
        this.attCode = (AttCode)attribs.findFirst("Code");
        AttributeList codeAttribs = this.attCode.getAttributes();
        LineNumberList lnl = LineNumberList.EMPTY;
        if (keepLines) {
            AttLineNumberTable lnt = (AttLineNumberTable)codeAttribs.findFirst("LineNumberTable");
            while (lnt != null) {
                lnl = LineNumberList.concat(lnl, lnt.getLineNumbers());
                lnt = (AttLineNumberTable)codeAttribs.findNext(lnt);
            }
        }
        this.lineNumbers = lnl;
        LocalVariableList lvl = LocalVariableList.EMPTY;
        if (keepLocals) {
            AttLocalVariableTable lvt = (AttLocalVariableTable)codeAttribs.findFirst("LocalVariableTable");
            while (lvt != null) {
                lvl = LocalVariableList.concat(lvl, lvt.getLocalVariables());
                lvt = (AttLocalVariableTable)codeAttribs.findNext(lvt);
            }
            LocalVariableList typeList = LocalVariableList.EMPTY;
            AttLocalVariableTypeTable lvtt = (AttLocalVariableTypeTable)codeAttribs.findFirst("LocalVariableTypeTable");
            while (lvtt != null) {
                typeList = LocalVariableList.concat(typeList, lvtt.getLocalVariables());
                lvtt = (AttLocalVariableTypeTable)codeAttribs.findNext(lvtt);
            }
            if (typeList.size() != 0) {
                lvl = LocalVariableList.mergeDescriptorsAndSignatures(lvl, typeList);
            }
        }
        this.localVariables = lvl;
    }

    public CstString getSourceFile() {
        return this.classFile.getSourceFile();
    }

    public final boolean isDefaultOrStaticInterfaceMethod() {
        return (this.classFile.getAccessFlags() & 0x200) != 0 && !this.getNat().isClassInit();
    }

    public final boolean isStaticMethod() {
        return (this.getAccessFlags() & 8) != 0;
    }

    @Override
    public CstNat getNat() {
        return this.method.getNat();
    }

    @Override
    public CstString getName() {
        return this.method.getName();
    }

    @Override
    public CstString getDescriptor() {
        return this.method.getDescriptor();
    }

    @Override
    public int getAccessFlags() {
        return this.method.getAccessFlags();
    }

    @Override
    public AttributeList getAttributes() {
        return this.method.getAttributes();
    }

    @Override
    public CstType getDefiningClass() {
        return this.method.getDefiningClass();
    }

    @Override
    public Prototype getEffectiveDescriptor() {
        return this.method.getEffectiveDescriptor();
    }

    public int getMaxStack() {
        return this.attCode.getMaxStack();
    }

    public int getMaxLocals() {
        return this.attCode.getMaxLocals();
    }

    public BytecodeArray getCode() {
        return this.attCode.getCode();
    }

    public ByteCatchList getCatches() {
        return this.attCode.getCatches();
    }

    public LineNumberList getLineNumbers() {
        return this.lineNumbers;
    }

    public LocalVariableList getLocalVariables() {
        return this.localVariables;
    }

    public SourcePosition makeSourcePosistion(int offset) {
        return new SourcePosition(this.getSourceFile(), offset, this.lineNumbers.pcToLine(offset));
    }
}

