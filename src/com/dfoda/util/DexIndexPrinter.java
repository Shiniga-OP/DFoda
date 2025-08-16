package com.dfoda.util;

import java.io.File;
import java.io.IOException;
import com.dex.Dex;
import com.dex.TableOfContents;
import com.dex.ProtoId;
import com.dex.FieldId;
import com.dex.MethodId;
import com.dex.ClassDef;

public final class DexIndexPrinter {
    private final Dex dex;
    private final TableOfContents tableOfContents;

    public DexIndexPrinter(File file) throws IOException {
        this.dex = new Dex(file);
        this.tableOfContents = this.dex.getTableOfContents();
    }

    private void printMap() {
        for (TableOfContents.Sessao section : this.tableOfContents.sessoes) {
            if (section.off == -1) continue;
            System.out.println("section " + Integer.toHexString(section.tipo) + " off=" + Integer.toHexString(section.off) + " size=" + Integer.toHexString(section.tam) + " byteCount=" + Integer.toHexString(section.byteCount));
        }
    }

    private void printStrings() throws IOException {
        int index = 0;
        for (String string : this.dex.strings()) {
            System.out.println("string " + index + ": " + string);
            ++index;
        }
    }

    private void printTypeIds() throws IOException {
        int index = 0;
        for (Integer type : this.dex.typeIds()) {
            System.out.println("type " + index + ": " + this.dex.strings().get(type));
            ++index;
        }
    }

    private void printProtoIds() throws IOException {
        int index = 0;
        for (ProtoId protoId : this.dex.protoIds()) {
            System.out.println("proto " + index + ": " + protoId);
            ++index;
        }
    }

    private void printFieldIds() throws IOException {
        int index = 0;
        for (FieldId fieldId : this.dex.fieldIds()) {
            System.out.println("field " + index + ": " + fieldId);
            ++index;
        }
    }

    private void printMethodIds() throws IOException {
        int index = 0;
        for (MethodId methodId : this.dex.methodIds()) {
            System.out.println("methodId " + index + ": " + methodId);
            ++index;
        }
    }

    private void printTypeLists() throws IOException {
        if (this.tableOfContents.typeLists.off == -1) {
            System.out.println("No type lists");
            return;
        }
        Dex.Section in = this.dex.open(this.tableOfContents.typeLists.off);
        for (int i = 0; i < this.tableOfContents.typeLists.tam; ++i) {
            int size = in.readInt();
            System.out.print("Type list i=" + i + ", size=" + size + ", elements=");
            for (int t = 0; t < size; ++t) {
                System.out.print(" " + this.dex.typeNames().get(in.readShort()));
            }
            if (size % 2 == 1) {
                in.readShort();
            }
            System.out.println();
        }
    }

    private void printClassDefs() {
        int index = 0;
        for (ClassDef classDef : this.dex.classDefs()) {
            System.out.println("class def " + index + ": " + classDef);
            ++index;
        }
    }

    public static void main(String[] args) throws IOException {
        DexIndexPrinter indexPrinter = new DexIndexPrinter(new File(args[0]));
        indexPrinter.printMap();
        indexPrinter.printStrings();
        indexPrinter.printTypeIds();
        indexPrinter.printProtoIds();
        indexPrinter.printFieldIds();
        indexPrinter.printMethodIds();
        indexPrinter.printTypeLists();
        indexPrinter.printClassDefs();
    }
}

