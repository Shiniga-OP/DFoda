package com.dfoda.otimizadores.ca.direto;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import com.dex.util.FileUtils;

public class ClassPathOpener {
    public final String camNome;
    public final Consumidor consumidor;
    public final boolean sort;
    public FiltroArqNome filtro;
    public static final FiltroArqNome aceitaTudo = new FiltroArqNome(){
        @Override
        public boolean aceitar(String cam) {
            return true;
        }
    };

    public ClassPathOpener(String camNome, boolean sort, Consumidor consumidor) {
        this(camNome, sort, aceitaTudo, consumidor);
    }

    public ClassPathOpener(String camNome, boolean sort, FiltroArqNome filtro, Consumidor consumidor) {
        this.camNome = camNome;
        this.sort = sort;
        this.consumidor = consumidor;
        this.filtro = filtro;
    }

    public boolean processo() {
        File arq = new File(this.camNome);
        return this.processOne(arq, true);
    }

    public boolean processOne(File arq, boolean topLevel) {
        try {
            if(arq.isDirectory()) {
                return this.processarDir(arq, topLevel);
            }
            String cam = arq.getPath();
            if(cam.endsWith(".zip") || cam.endsWith(".jar") || cam.endsWith(".apk")) return this.processArchive(arq);
            if(this.filtro.aceitar(cam)) {
                byte[] bytes = FileUtils.readFile(arq);
                return this.consumidor.processarArqBytes(cam, arq.lastModified(), bytes);
            }
            return false;
        } catch(Exception e) {
            this.consumidor.seErrar(e);
            return false;
        }
    }

    private static int compareClassNames(String a, String b) {
        a = a.replace('$', '0');
        b = b.replace('$', '0');
        a = a.replace("package-info", "");
        b = b.replace("package-info", "");
        return a.compareTo(b);
    }

    public boolean processarDir(File dir, boolean topLevel) {
        if(topLevel) dir = new File(dir, ".");
        File[] arqs = dir.listFiles();
        boolean any = false;
        if(this.sort) {
            Arrays.sort(arqs, new Comparator<File>(){
                @Override
                public int compare(File a, File b) {
                    return ClassPathOpener.compareClassNames(a.getName(), b.getName());
                }
            });
        }
        for(int i = 0; i < arqs.length; ++i) any |= this.processOne(arqs[i], false);
        return any;
    }

    public boolean processArchive(File arq) throws IOException {
        ZipFile zip = new ZipFile(arq);
        ArrayList<? extends ZipEntry> entriesList = Collections.list(zip.entries());
        if(this.sort) {
            Collections.sort(entriesList, new Comparator<ZipEntry>(){
                @Override
                public int compare(ZipEntry a, ZipEntry b) {
                    return ClassPathOpener.compareClassNames(a.getName(), b.getName());
                }
            });
        }
        this.consumidor.aoIniciarProcesso(arq);
        ByteArrayOutputStream baos = new ByteArrayOutputStream(40000);
        byte[] buf = new byte[20000];
        boolean any = false;
        for(ZipEntry zipEntry : entriesList) {
            byte[] bytes;
            boolean isDirectory = zipEntry.isDirectory();
            String cam = zipEntry.getName();
            if(!this.filtro.aceitar(cam)) continue;
            if(!isDirectory) {
                int lido;
                InputStream en = zip.getInputStream(zipEntry);
                baos.reset();
                while((lido = en.read(buf)) != -1) baos.write(buf, 0, lido);
                en.close();
                bytes = baos.toByteArray();
            } else {
                bytes = new byte[]{};
            }
            any |= this.consumidor.processarArqBytes(cam, zipEntry.getTime(), bytes);
        }
        zip.close();
        return any;
    }

    public static interface FiltroArqNome {
        public boolean aceitar(String var1);
    }

    public static interface Consumidor {
        public boolean processarArqBytes(String var1, long var2, byte[] var4);
        public void seErrar(Exception var1);
        public void aoIniciarProcesso(File var1);
    }
}

