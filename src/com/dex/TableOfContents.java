package com.dex;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import com.dex.util.ErroCtx;

public final class TableOfContents {
    public final Sessao header = new Sessao(0);
    public final Sessao stringIds = new Sessao(1);
    public final Sessao typeIds = new Sessao(2);
    public final Sessao protoIds = new Sessao(3);
    public final Sessao fieldIds = new Sessao(4);
    public final Sessao methodIds = new Sessao(5);
    public final Sessao classDefs = new Sessao(6);
    public final Sessao callSiteIds = new Sessao(7);
    public final Sessao methodHandles = new Sessao(8);
    public final Sessao mapList = new Sessao(4096);
    public final Sessao typeLists = new Sessao(4097);
    public final Sessao annotationSetRefLists = new Sessao(4098);
    public final Sessao annotationSets = new Sessao(4099);
    public final Sessao classDatas = new Sessao(8192);
    public final Sessao codes = new Sessao(8193);
    public final Sessao stringDatas = new Sessao(8194);
    public final Sessao debugInfos = new Sessao(8195);
    public final Sessao annotations = new Sessao(8196);
    public final Sessao encodedArrays = new Sessao(8197);
    public final Sessao annotationsDirectories = new Sessao(8198);
    public final Sessao[] sessoes = new Sessao[]{this.header, this.stringIds, this.typeIds, this.protoIds, this.fieldIds, this.methodIds, this.classDefs, this.mapList, this.callSiteIds, this.methodHandles, this.typeLists, this.annotationSetRefLists, this.annotationSets, this.classDatas, this.codes, this.stringDatas, this.debugInfos, this.annotations, this.encodedArrays, this.annotationsDirectories};
    public int apiNivel;
    public int checksum;
    public byte[] signature = new byte[20];
    public int fileSize;
    public int linkSize;
    public int linkOff;
    public int dataSize;
    public int dataOff;

    public void readFrom(Dex dex) throws IOException {
        this.readHeader(dex.open(0));
        this.readMap(dex.open(this.mapList.off));
        this.computeSizesFromOffsets();
    }

    private void readHeader(Dex.Section headerIn) throws UnsupportedEncodingException {
        byte[] magic = headerIn.readByteArray(8);
        if(!FormatoDex.eSuportadoMagic(magic)) {
            String msg = String.format("Unexpected magic: [0x%02x, 0x%02x, 0x%02x, 0x%02x, 0x%02x, 0x%02x, 0x%02x, 0x%02x]", magic[0], magic[1], magic[2], magic[3], magic[4], magic[5], magic[6], magic[7]);
            throw new ErroCtx(msg);
        }
        this.apiNivel = FormatoDex.magicPraAPI(magic);
        this.checksum = headerIn.readInt();
        this.signature = headerIn.readByteArray(20);
        this.fileSize = headerIn.readInt();
        int headerSize = headerIn.readInt();
        if(headerSize != 112) throw new ErroCtx("Unexpected header: 0x" + Integer.toHexString(headerSize));
        int endianTag = headerIn.readInt();
        if(endianTag != 305419896) throw new ErroCtx("Unexpected endian tag: 0x" + Integer.toHexString(endianTag));
        this.linkSize = headerIn.readInt();
        this.linkOff = headerIn.readInt();
        this.mapList.off = headerIn.readInt();
        if(this.mapList.off == 0) throw new ErroCtx("Cannot merge dex files that do not contain a map");
        this.stringIds.tam = headerIn.readInt();
        this.stringIds.off = headerIn.readInt();
        this.typeIds.tam = headerIn.readInt();
        this.typeIds.off = headerIn.readInt();
        this.protoIds.tam = headerIn.readInt();
        this.protoIds.off = headerIn.readInt();
        this.fieldIds.tam = headerIn.readInt();
        this.fieldIds.off = headerIn.readInt();
        this.methodIds.tam = headerIn.readInt();
        this.methodIds.off = headerIn.readInt();
        this.classDefs.tam = headerIn.readInt();
        this.classDefs.off = headerIn.readInt();
        this.dataSize = headerIn.readInt();
        this.dataOff = headerIn.readInt();
    }

    private void readMap(Dex.Section en) throws IOException {
        int mapaTam = en.readInt();
        Sessao previous = null;
        for(int i = 0; i < mapaTam; ++i) {
            short tipo = en.readShort();
            en.readShort();
            Sessao sessao = this.getSection(tipo);
            int tam = en.readInt();
            int antes = en.readInt();
            if(sessao.tam != 0 && sessao.tam != tam || sessao.off != -1 && sessao.off != antes) throw new ErroCtx("Unexpected map value for 0x" + Integer.toHexString(tipo));
            sessao.tam = tam;
            sessao.off = antes;
            if(previous != null && previous.off > sessao.off) throw new ErroCtx("Map is unsorted at " + previous + ", " + sessao);
            previous = sessao;
        }
        Arrays.sort(this.sessoes);
    }

    public void computeSizesFromOffsets() {
        int fim = this.dataOff + this.dataSize;
        for(int i = this.sessoes.length - 1; i >= 0; --i) {
            Sessao sessao = this.sessoes[i];
            if(sessao.off == -1) continue;
            if(sessao.off > fim) throw new ErroCtx("Map is unsorted at " + sessao);
            sessao.byteCount = fim - sessao.off;
            fim = sessao.off;
        }
    }

    private Sessao getSection(short tipo) {
        for(Sessao sessao : this.sessoes) {
            if(sessao.tipo != tipo) continue;
            return sessao;
        }
        throw new IllegalArgumentException("No such map item: " + tipo);
    }

    public void writeHeader(Dex.Section out, int api) throws IOException {
        out.write(FormatoDex.APIPraMagic(api).getBytes("UTF-8"));
        out.writeInt(this.checksum);
        out.write(this.signature);
        out.writeInt(this.fileSize);
        out.writeInt(112);
        out.writeInt(305419896);
        out.writeInt(this.linkSize);
        out.writeInt(this.linkOff);
        out.writeInt(this.mapList.off);
        out.writeInt(this.stringIds.tam);
        out.writeInt(this.stringIds.off);
        out.writeInt(this.typeIds.tam);
        out.writeInt(this.typeIds.off);
        out.writeInt(this.protoIds.tam);
        out.writeInt(this.protoIds.off);
        out.writeInt(this.fieldIds.tam);
        out.writeInt(this.fieldIds.off);
        out.writeInt(this.methodIds.tam);
        out.writeInt(this.methodIds.off);
        out.writeInt(this.classDefs.tam);
        out.writeInt(this.classDefs.off);
        out.writeInt(this.dataSize);
        out.writeInt(this.dataOff);
    }

    public void writeMap(Dex.Section out) throws IOException {
        int count = 0;
        for(Sessao sessao : this.sessoes) {
            if(!sessao.existe()) continue;
            ++count;
        }
        out.writeInt(count);
        for(Sessao sessao : this.sessoes) {
            if(!sessao.existe()) continue;
            out.writeShort(sessao.tipo);
            out.writeShort((short)0);
            out.writeInt(sessao.tam);
            out.writeInt(sessao.off);
        }
    }

    public static class Sessao implements Comparable<Sessao> {
        public final short tipo;
        public int tam = 0;
        public int off = -1;
        public int byteCount = 0;

        public Sessao(int type) {
            this.tipo = (short)type;
        }

        public boolean existe() {
            return this.tam > 0;
        }

        @Override
        public int compareTo(Sessao sessao) {
            if(this.off != sessao.off) return this.off < sessao.off ? -1 : 1;
            return 0;
        }

        public String emString() {
            return String.format("Sessao[tipo=%#x,off=%#x,tam=%#x]", this.tipo, this.off, this.tam);
        }
    }
}

