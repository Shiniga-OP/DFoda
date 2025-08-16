package com.dfoda.dexer;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.Arrays;
import java.util.TreeMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import com.dfoda.dex.arquivo.DexFile;
import com.dfoda.otimizadores.rop.tipo.Prototype;
import com.dfoda.otimizadores.rop.codigo.RegisterSpec;
import com.dfoda.otimizadores.rop.cst.CstType;
import com.dfoda.otimizadores.rop.tipo.Type;
import com.dex.Dex;
import com.dfoda.junta.DexMerger;
import com.dfoda.junta.CollisionPolicy;
import com.dfoda.dex.arquivo.ClassDefItem;
import com.dfoda.dex.arquivo.EncodedMethod;
import com.dfoda.otimizadores.rop.cst.CstNat;
import com.dfoda.otimizadores.rop.cst.CstString;
import com.dfoda.otimizadores.rop.anotacao.Annotations;
import com.dfoda.otimizadores.rop.anotacao.AnnotationsList;
import com.dfoda.otimizadores.rop.anotacao.Annotation;
import com.dfoda.dex.DexOptions;
import com.dex.util.FileUtils;
import com.dfoda.otimizadores.ca.direto.ClassPathOpener;
import com.dfoda.otimizadores.ca.direto.ClassPathOpener.FiltroArqNome;
import com.dfoda.otimizadores.ca.direto.ClassPathOpener.Consumidor;
import com.dfoda.otimizadores.ca.direto.DirectClassFile;
import com.dfoda.dex.ca.CfTranslator;
import com.dfoda.otimizadores.ca.direto.StdAttributeFactory;
import com.dfoda.dex.ca.CfOptions;
import com.dex.util.ErroCtx;

public class Main {
    public static final String extensao = ".dex";
    private static final String IN_RE_CORE_CLASSES = "Ill-advised or mistaken usage of a core class (java.* or javax.*)\nwhen not building a core library.\n\nThis is often due to inadvertently including a core library file\nin your application's project, when using an IDE (such as\nEclipse). If you are sure you're not intentionally defining a\ncore class, then this is the most likely explanation of what's\ngoing on.\n\nHowever, you might actually be trying to define a class in a core\nnamespace, the source of which you may have taken, for example,\nfrom a non-Android virtual machine project. This will most\nassuredly not work. At a minimum, it jeopardizes the\ncompatibility of your app with future versions of the platform.\nIt is also often of questionable legality.\n\nIf you really intend to build a core library -- which is only\nappropriate as part of creating a full virtual machine\ndistribution, as opposed to compiling an application -- then use\nthe \"--core-library\" option to suppress this error message.\n\nIf you go ahead and use \"--core-library\" but are in fact\nbuilding an application, then be forewarned that your application\nwill still fail to build or run, at some point. Please be\nprepared for angry customers who find, for example, that your\napplication ceases to function once they upgrade their operating\nsystem. You will be to blame for this problem.\n\nIf you are legitimately using some code that happens to be in a\ncore package, then the easiest safe alternative you have is to\nrepackage that code. That is, move the classes in question into\nyour own package namespace. This means that they will never be in\nconflict with core system classes. JarJar is a tool that may help\nyou in this endeavor. If you find that you cannot do this, then\nthat is an indication that the path you are on will ultimately\nlead to pain, suffering, grief, and lamentation.\n";
    private static final String MANIFEST_NAME = "META-INF/MANIFEST.MF";
    private static final Attributes.Name CREATED_BY = new Attributes.Name("Created-By");
    private static final String[] JAVAX_CORE = new String[]{"accessibility", "crypto", "imageio", "management", "naming", "net", "print", "rmi", "security", "sip", "sound", "sql", "swing", "transaction", "xml"};
    public AtomicInteger erros = new AtomicInteger(0);
    public Args args;
    private DexFile saidaDex;
    private TreeMap<String, byte[]> saidaRes;
    private final List<byte[]> libraryDexBuffers = new ArrayList<byte[]>();
    private ExecutorService classTranslatorPool;
    private ExecutorService classDefItemConsumer;
    private List<Future<Boolean>> addToDexFutures = new ArrayList<Future<Boolean>>();
    public ExecutorService dexSaidaPool;
    public List<Future<byte[]>> dexSaidasFuturas = new ArrayList<Future<byte[]>>();
    private Object dexRotationLock = new Object();
    private int maxMethodIdsInProcess = 0;
    private int maxFieldIdsInProcess = 0;
    private volatile boolean anyFilesProcessed;
    private long minimumFileAge = 0L;
    private Set<String> classesNoDex = null;
    private List<byte[]> dexSaidaArrays = new ArrayList<byte[]>();
    private OutputStreamWriter humanOutWriter = null;
    public final DFodaCtx ctx;

    public Main(DFodaCtx ctx) {
        this.ctx = ctx;
    }
	
	public static final String uso = "uso:\n  dx --dex [--debug] [--verbose] [--posicoes=<estilo>] [--sem-locais]\n  [--sem-otimizar] [--status] [--[sem-]otimizar-lista=<arquivo>] [--no-strict]\n  [--manter-classes] [--saida=<arquivo>] [--mostrar-so=<arquivo>] [--mostrar-largura=<n>]\n  [--mostrar-metodo=<nome>[*]] [--verbose-mostrar] [--sem-arquivos] [--nucleo-biblis]\n [--sem-aviso]\n";

    public static void rodar(String... args) {
        boolean mostrarUso = false;
        try {
            for(int i = 0; i < args.length; ++i) {
                String arg = args[i];
                if(arg.equals("--") || !arg.startsWith("--")) {
                    mostrarUso = true;
                    break;
                }
                if(arg.equals("--dex")) {
                    main(sem(args, i));
                    break;
                }
                if(arg.equals("--mostrar")) {
                    com.dfoda.dexer.mostrar.Main.rodar(sem(args, i));
                    break;
                }
                if(arg.equals("--versao")) {
                    System.err.println("dFoda versao 0.0.1");
                    break;
                }
                if(arg.equals("--ajuda")) {
                    mostrarUso = true;
                    break;
                }
            }
        } catch(RuntimeException e) {
            mostrarUso = true; 
            System.out.println("\nEXCEÇÃO DE NIVEL SUPERIOR:");
            e.printStackTrace();
            return;
        } catch(Throwable e) {
            System.err.println("\nERRO DE NÍVEL SUPERIOR:");
            e.printStackTrace();
            if(e instanceof NoClassDefFoundError || e instanceof NoSuchMethodError) System.err.println("Nota: você pode ta usando uma maquina virtual ou biblioteca de classes incompativel(Esse programa é conhecido por ser incompativel com versões recentes do GCJ)");
            return;
        }
        if(mostrarUso) {
            System.err.println(uso);
            return;
        }
    }

    public static String[] sem(String[] orig, int n) {
        int tam = orig.length - 1;
        String[] newa = new String[tam];
        System.arraycopy(orig, 0, newa, 0, n);
        System.arraycopy(orig, n + 1, newa, n, tam - n);
        return newa;
    }

    public static void main(String[] argArray) throws IOException {
        DFodaCtx ctx = new DFodaCtx();
        Args argss = new Args(ctx);
        argss.analisar(argArray);
        int resultado = new Main(ctx).rodarDfoda(argss);
        if(resultado != 0) return;
    }

    public static void clearInternTables() {
        Prototype.clearInternTable();
        RegisterSpec.clearInternTable();
        CstType.clearInternTable();
        Type.clearInternTable();
    }

    public static int run(Args arguments) throws IOException {
        return new Main(new DFodaCtx()).rodarDfoda(arguments);
    }

    public int rodarDfoda(Args argsParam) throws IOException {
        this.erros.set(0);
        this.libraryDexBuffers.clear();
        this.args = argsParam;
        this.args.makeOptionsObjects();
        OutputStream saidaLegivel = null;
        if(this.args.humanOutName != null) {
            saidaLegivel = this.openOutput(this.args.humanOutName);
            this.humanOutWriter = new OutputStreamWriter(saidaLegivel);
        }
        try {
            if(this.args.multiDex) {
                int n = this.rodarMultiDex();
                return n;
            }
            int n = this.runMonoDex();
            return n;
        } finally {
            this.closeOutput(saidaLegivel);
        }
    }

    public int runMonoDex() throws IOException {
        File incremental = null;
        if(this.args.incremental) {
            if(this.args.outName == null) {
                this.ctx.err.println("error: no incremental output name specified");
                return -1;
            }
            incremental = new File(this.args.outName);
            if(incremental.exists()) {
                this.minimumFileAge = incremental.lastModified();
            }
        }
        if(!this.processarTodosArqs()) return 1;
        if(this.args.incremental && !this.anyFilesProcessed) return 0;
        byte[] arraySaida = null;
        if(!(this.saidaDex.isEmpty() && this.args.humanOutName == null || (arraySaida = this.escreverDex(this.saidaDex)) != null)) return 2;
        if(this.args.incremental) arraySaida = this.mergeIncremental(arraySaida, incremental);
        arraySaida = this.mergeLibraryDexBuffers(arraySaida);
        if(this.args.jarSaida) {
            this.saidaDex = null;
            if(arraySaida != null) this.saidaRes.put("classes.dex", arraySaida);
            if(!this.criarJar(this.args.outName)) return 3;
        } else if(arraySaida != null && this.args.outName != null) {
            OutputStream saida = this.openOutput(this.args.outName);
            saida.write(arraySaida);
            this.closeOutput(saida);
        }
        return 0;
    }
	
    public int rodarMultiDex() throws IOException {
        assert (!this.args.incremental);
        if(this.args.mainDexListFile != null) {
            this.classesNoDex = new HashSet<String>();
            Main.readPathsFromFile(this.args.mainDexListFile, this.classesNoDex);
        }
        this.dexSaidaPool = Executors.newFixedThreadPool(this.args.numThreads);
        if(!this.processarTodosArqs()) return 1;
        if(!this.libraryDexBuffers.isEmpty()) throw new ErroCtx("Library dex files are not supported in multi-dex mode");
        if(this.saidaDex != null) {
            this.dexSaidasFuturas.add(this.dexSaidaPool.submit(new DexWriter(this.saidaDex)));
            this.saidaDex = null;
        }
        try {
            this.dexSaidaPool.shutdown();
            if(!this.dexSaidaPool.awaitTermination(600L, TimeUnit.SECONDS)) throw new RuntimeException("Timed out waiting for dex writer threads.");
            for(Future<byte[]> f : this.dexSaidasFuturas) this.dexSaidaArrays.add(f.get());
        } catch(InterruptedException e) {
            this.dexSaidaPool.shutdownNow();
            throw new RuntimeException("A dex writer thread has been interrupted.");
        } catch(Exception e) {
            this.dexSaidaPool.shutdownNow();
            throw new RuntimeException("Unexpected exception in dex writer thread");
        }
        if(this.args.jarSaida) {
            for(int i = 0; i < this.dexSaidaArrays.size(); ++i) this.saidaRes.put(obterNomeDex(i), this.dexSaidaArrays.get(i));
            if(!this.criarJar(this.args.outName)) return 3;
        } else if(this.args.outName != null) {
            File saidaDir = new File(this.args.outName);
            assert(saidaDir.isDirectory());
            for(int i = 0; i < this.dexSaidaArrays.size(); ++i) {
                FileOutputStream saida = new FileOutputStream(new File(saidaDir, obterNomeDex(i)));
                try {
                    saida.write(this.dexSaidaArrays.get(i));
                    continue;
                } finally {
                    this.closeOutput(saida);
                }
            }
        }
        return 0;
    }

    public static String obterNomeDex(int i) {
        if(i == 0) return "classes.dex";
        return "classes" + (i + 1) + extensao;
    }

    public static void readPathsFromFile(String fileName, Collection<String> paths) throws IOException {
		try(BufferedReader bfr = new BufferedReader(new FileReader(fileName))) {
			String linha;
			while((linha = bfr.readLine()) != null) paths.add(corrigirCam(linha));
		}
	}

    public byte[] mergeIncremental(byte[] update, File base) throws IOException {
        Dex dexA = null;
        Dex dexB = null;
        if(update != null) {
            dexA = new Dex(update);
        }
        if(base.exists()) {
            dexB = new Dex(base);
        }
        if(dexA == null && dexB == null) {
            return null;
        }
        Dex result = dexA == null ? dexB : (dexB == null ? dexA : new DexMerger(new Dex[]{dexA, dexB}, CollisionPolicy.KEEP_FIRST, this.ctx).merge());
        ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
        result.writeTo(bytesOut);
        return bytesOut.toByteArray();
    }

    public byte[] mergeLibraryDexBuffers(byte[] outArray) throws IOException {
        ArrayList<Dex> dexes = new ArrayList<Dex>();
        if(outArray != null) dexes.add(new Dex(outArray));
        for(byte[] libraryDex : this.libraryDexBuffers) dexes.add(new Dex(libraryDex));
        if(dexes.isEmpty()) return null;
        Dex merged = new DexMerger(dexes.toArray(new Dex[dexes.size()]), CollisionPolicy.FAIL, this.ctx).merge();
        return merged.getBytes();
    }

    public boolean processarTodosArqs() {
        createDexFile();
        if(args.jarSaida) saidaRes = new TreeMap<String, byte[]>();

        anyFilesProcessed = false;
        String[] arqsNomes = args.fileNames;
        Arrays.sort(arqsNomes);

        classTranslatorPool = new ThreadPoolExecutor(args.numThreads,
													 args.numThreads, 0, TimeUnit.SECONDS,
													 new ArrayBlockingQueue<Runnable>(2 * args.numThreads, true),
													 new ThreadPoolExecutor.CallerRunsPolicy());
        classDefItemConsumer = Executors.newSingleThreadExecutor();
        try {
            if(args.mainDexListFile != null) {
                FiltroArqNome mainPassFilter = args.strictNameCheck ? new MainDexListFilter() : new BestEffortMainDexListFilter();

                for(int i = 0; i < arqsNomes.length; i++) processarUm(arqsNomes[i], mainPassFilter);

                if(dexSaidasFuturas.size() > 0) throw new ErroCtx("Too many classes in " + Args.MAIN_DEX_LIST_OPTION + ", main dex capacity exceeded");

                if(args.minimalMainDex) {
                    synchronized(dexRotationLock) {
                        while(maxMethodIdsInProcess > 0 || maxFieldIdsInProcess > 0) {
                            try {
                                dexRotationLock.wait();
                            } catch(InterruptedException e) {
                                System.out.println("erro: " + e);
                            }
                        }
                    }
                    rotaDexArq();
                }
                FiltroArqNome filtro = new RemoveModuleInfoFilter(new NaoFiltro(mainPassFilter));
                for(int i = 0; i < arqsNomes.length; i++) processarUm(arqsNomes[i], filtro);
            } else {
                FiltroArqNome filtro = new RemoveModuleInfoFilter(ClassPathOpener.aceitaTudo);
                for(int i = 0; i < arqsNomes.length; i++) processarUm(arqsNomes[i], filtro);
            }
        } catch(RuntimeException e) {
            System.out.println("erro: " + e);
        }
        try {
            classTranslatorPool.shutdown();
            classTranslatorPool.awaitTermination(600L, TimeUnit.SECONDS);
            classDefItemConsumer.shutdown();
            classDefItemConsumer.awaitTermination(600L, TimeUnit.SECONDS);

            for(Future<Boolean> f : addToDexFutures) {
                try {
                    f.get();
                } catch(ExecutionException e) {
                    int conta = erros.incrementAndGet();
                    if(conta < 10) {
                        if(args.debug) {
                            ctx.err.println("Uncaught translation error:");
                            e.getCause().printStackTrace(ctx.err);
                        } else ctx.err.println("Uncaught translation error: " + e.getCause());
                    } else {
                        throw new InterruptedException("Too many errors");
                    }
                }
            }
        } catch(InterruptedException ie) {
            classTranslatorPool.shutdownNow();
            classDefItemConsumer.shutdownNow();
            throw new RuntimeException("Translation has been interrupted", ie);
        } catch(Exception e) {
            classTranslatorPool.shutdownNow();
            classDefItemConsumer.shutdownNow();
            e.printStackTrace(ctx.saida);
            throw new RuntimeException("Unexpected exception in translator thread.", e);
        }

        int errorNum = erros.get();
        if(errorNum != 0) {
            ctx.err.println(errorNum + " error" +
			((errorNum == 1) ? "" : "s") + "; aborting");
            return false;
        }

        if(args.incremental && !anyFilesProcessed) return true;

        if(!(anyFilesProcessed || args.emptyOk)) {
            ctx.err.println("no classfiles specified");
            return false;
        }
        if(args.otimizar && args.status) ctx.codigoStatus.dumpStatistics(ctx.saida);
        return true;
    }

    public void createDexFile() {
        this.saidaDex = new DexFile(this.args.dexOptions);
        if(this.args.dumpWidth != 0) this.saidaDex.setDumpWidth(this.args.dumpWidth);
    }

    public void rotaDexArq() {
        if(this.saidaDex != null) {
            if(this.dexSaidaPool != null) {
                this.dexSaidasFuturas.add(this.dexSaidaPool.submit(new DexWriter(this.saidaDex)));
            } else {
                this.dexSaidaArrays.add(this.escreverDex(this.saidaDex));
            }
        }
        this.createDexFile();
    }

    public void processarUm(String camNome, FiltroArqNome filtro) {
        ClassPathOpener opener = new ClassPathOpener(camNome, true, filtro, new FileBytesConsumer());
        if(opener.processo()) this.attStatus(true);
    }

    public void attStatus(boolean res) {
        this.anyFilesProcessed |= res;
    }

    public boolean processarArqBytes(String nome, long ultimoModificado, byte[] bytes) {
        boolean manterResources;
        boolean eClasse = nome.endsWith(".class");
        boolean eClassesDex = nome.equals("classes.dex");
        manterResources = this.saidaRes != null;
        if(!(eClasse || eClassesDex || manterResources)) {
            if(this.args.verbose) this.ctx.saida.println("ignored resource " + nome);
            return false;
        }
        if(this.args.verbose) this.ctx.saida.println("processing " + nome + "...");
        String nomeCerto = corrigirCam(nome);
		TreeMap<String, byte[]> arvoreMapa = this.saidaRes;
        if(eClasse) {
            if(manterResources && this.args.manterClassesNoJar) {
                synchronized(arvoreMapa) {
                    this.saidaRes.put(nomeCerto, bytes);
                }
            }
            if(ultimoModificado < this.minimumFileAge) return true;
            this.processarClass(nomeCerto, bytes);
            return false;
        }
        if(eClassesDex) {
            List<byte[]> lista = this.libraryDexBuffers;
            synchronized(lista) {
                this.libraryDexBuffers.add(bytes);
            }
            return true;
        }
        synchronized(arvoreMapa) {
            this.saidaRes.put(nomeCerto, bytes);
        }
        return true;
    }

    public boolean processarClass(String nome, byte[] bytes) {
        if(!this.args.coreLibrary) this.checarNomeClass(nome);
        try {
            new DirectClassFileConsumer(nome, bytes, null).call(new ClassPraAnalisar(nome, bytes).call());
        } catch(ErroCtx e) {
            throw e;
        } catch(Exception e) {
            throw new RuntimeException("Erro analisando classes", e);
        }
        return true;
    }

    public DirectClassFile parseClass(String name, byte[] bytes) {
        DirectClassFile cf = new DirectClassFile(bytes, name, this.args.cfOptions.strictNameCheck);
        cf.setAttributeFactory(StdAttributeFactory.THE_ONE);
        cf.getMagic();
        return cf;
    }

    public ClassDefItem traduzirClasse(byte[] bytes, DirectClassFile cf) {
        try {
            return CfTranslator.translate(this.ctx, cf, bytes, this.args.cfOptions, this.args.dexOptions, this.saidaDex);
        } catch(ErroCtx e) {
            this.ctx.err.println("Erro processando: ");
            if(this.args.debug) e.printStackTrace(this.ctx.err);
            else e.logCtx(this.ctx.err);
            this.erros.incrementAndGet();
            return null;
        }
    }
	
    public boolean addClasseNoDex(ClassDefItem classe) {
        DexFile dexArq = this.saidaDex;
        synchronized (dexArq) {
            this.saidaDex.add(classe);
        }
        return true;
    }

    public void checarNomeClass(String nome) {
        boolean bogus = false;
        if(nome.startsWith("java/")) {
            bogus = true;
        } else if(nome.startsWith("javax/")) {
            int slashAt = nome.indexOf(47, 6);
            if(slashAt == -1) bogus = true;
            else {
                String pkg = nome.substring(6, slashAt);
                bogus = Arrays.binarySearch(JAVAX_CORE, pkg) >= 0;
            }
        }
        if(!bogus) return;
        this.ctx.err.println("Erro processanso: \"" + nome + "\":\n\n" + IN_RE_CORE_CLASSES);
        this.erros.incrementAndGet();
        throw new RuntimeException();
    }

    public byte[] escreverDex(DexFile saidaDex) {
        byte[] saidaArray = null;
        try {
            try {
                if(this.args.methodToDump != null) {
                    saidaDex.toDex(null, false);
                    this.mostrarMetodo(saidaDex, this.args.methodToDump, this.humanOutWriter);
                } else {
                    saidaArray = saidaDex.toDex(this.humanOutWriter, this.args.verboseDump);
                }
                if(this.args.status) this.ctx.saida.println(saidaDex.getStatistics().toHuman());
            } finally {
                if(this.humanOutWriter != null) this.humanOutWriter.flush();
            }
        } catch(Exception e) {
            if(this.args.debug) {
                this.ctx.err.println("\nErro escrevendo a saida:");
                e.printStackTrace(this.ctx.err);
            } else this.ctx.err.println("\nErro escrevendo a saida: " + e.getMessage());
            return null;
        }
        return saidaArray;
    }
	
    public boolean criarJar(String arqNome) {
        try {
            Manifest manifesto = this.makeManifest();
            OutputStream saida = this.openOutput(arqNome);
            JarOutputStream jarSaida = new JarOutputStream(saida, manifesto);
            try {
                for(Map.Entry<String, byte[]> e : this.saidaRes.entrySet()) {
                    String nome = e.getKey();
                    byte[] conteudos = e.getValue();
                    JarEntry en = new JarEntry(nome);
                    int tam = conteudos.length;
                    if(this.args.verbose) this.ctx.saida.println("escrevendo " + nome + ", tamanho " + tam);
                    en.setSize(tam);
                    jarSaida.putNextEntry(en);
                    jarSaida.write(conteudos);
                    jarSaida.closeEntry();
                }
            }
            finally {
                jarSaida.finish();
                jarSaida.flush();
                this.closeOutput(saida);
            }
        } catch(Exception e) {
            if(this.args.debug) {
                this.ctx.err.println("\ntrouble writing output:");
                e.printStackTrace(this.ctx.err);
            } else {
                this.ctx.err.println("\ntrouble writing output: " + e.getMessage());
            }
            return false;
        }
        return true;
    }

    private Manifest makeManifest() throws IOException {
        Attributes attribs;
        Manifest manifest;
        byte[] manifestBytes = this.saidaRes.get(MANIFEST_NAME);
        if (manifestBytes == null) {
            manifest = new Manifest();
            attribs = manifest.getMainAttributes();
            attribs.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        } else {
            manifest = new Manifest(new ByteArrayInputStream(manifestBytes));
            attribs = manifest.getMainAttributes();
            this.saidaRes.remove(MANIFEST_NAME);
        }
        String createdBy = attribs.getValue(CREATED_BY);
        createdBy = createdBy == null ? "" : createdBy + " + ";
        createdBy = createdBy + "dx 1.16";
        attribs.put(CREATED_BY, createdBy);
        attribs.putValue("Dex-Location", "classes.dex");
        return manifest;
    }

    public OutputStream openOutput(String nome) throws IOException {
        if(nome.equals("-") || nome.startsWith("-.")) return this.ctx.saida;
        return new FileOutputStream(nome);
    }

    public void closeOutput(OutputStream stream) throws IOException {
        if(stream == null) return;
        stream.flush();
        if(stream != this.ctx.saida) stream.close();
    }

    public static String corrigirCam(String cam) {
        int idc;
        if(File.separatorChar == '\\') cam = cam.replace('\\', '/');
        if((idc = cam.lastIndexOf("/./")) != -1) return cam.substring(idc + 3);
        if(cam.startsWith("./")) return cam.substring(2);
        return cam;
    }

    public void mostrarMetodo(DexFile dex, String fqNome, OutputStreamWriter saida) {
        boolean wildcard = fqNome.endsWith("*");
        int ultimoDot = fqNome.lastIndexOf(46);
        if(ultimoDot <= 0 || ultimoDot == fqNome.length() - 1) {
            this.ctx.err.println("Nome completo do metodo é invalido: " + fqNome);
            return;
        }
        String classeNome = fqNome.substring(0, ultimoDot).replace('.', '/');
        String metodoNome = fqNome.substring(ultimoDot + 1);
        ClassDefItem classe = dex.getClassOrNull(classeNome);
        if(classe == null) {
            this.ctx.err.println("Nenhuma classe assim: " + classeNome);
            return;
        }
        if(wildcard) metodoNome = metodoNome.substring(0, metodoNome.length() - 1);
        ArrayList<EncodedMethod> todosMts = classe.getMethods();
        TreeMap<CstNat, EncodedMethod> mts = new TreeMap<CstNat, EncodedMethod>();
        for(EncodedMethod mt : todosMts) {
            String mtNome = mt.getName().getString();
            if((!wildcard || !mtNome.startsWith(metodoNome)) && (wildcard || !mtNome.equals(metodoNome))) continue;
            mts.put(mt.getRef().getNat(), mt);
        }
        if(mts.size() == 0) {
            this.ctx.err.println("no such method: " + fqNome);
            return;
        }
        PrintWriter pw = new PrintWriter(saida);
        for(EncodedMethod mt : mts.values()) {
            mt.debugPrint(pw, this.args.verboseDump);
            CstString fonteArq = classe.getSourceFile();
            if(fonteArq != null) pw.println("  conteudo do arquivo: " + fonteArq.toQuoted());
            Annotations metodoAnotacoes = classe.getMethodAnnotations(mt.getRef());
            AnnotationsList paramAnotacoes = classe.getParameterAnnotations(mt.getRef());
            if(metodoAnotacoes != null) {
                pw.println("  anotações do metodo:");
                for(Annotation a : metodoAnotacoes.getAnnotations()) pw.println("    " + a);
            }
            if(paramAnotacoes == null) continue;
            pw.println("  anotações do parametro:");
            int sz = paramAnotacoes.size();
            for(int i = 0; i < sz; ++i) {
                pw.println("    parametro " + i);
                Annotations anotacoes = paramAnotacoes.get(i);
                for(Annotation a : anotacoes.getAnnotations()) pw.println("      " + a);
            }
        }
        pw.flush();
    }

    public class DexWriter implements Callable<byte[]> {
        public final DexFile dexArq;

        public DexWriter(DexFile dexArq) {
            this.dexArq = dexArq;
        }

        @Override
        public byte[] call() throws IOException {
            return Main.this.escreverDex(this.dexArq);
        }
    }

    public class ClassDefItemConsumer implements Callable<Boolean> {
        String name;
        Future<ClassDefItem> futureClazz;
        int maxMethodIdsInClass;
        int maxFieldIdsInClass;

        private ClassDefItemConsumer(String name, Future<ClassDefItem> futureClazz, int maxMethodIdsInClass, int maxFieldIdsInClass) {
            this.name = name;
            this.futureClazz = futureClazz;
            this.maxMethodIdsInClass = maxMethodIdsInClass;
            this.maxFieldIdsInClass = maxFieldIdsInClass;
        }

        @Override
        public Boolean call() throws Exception {
            try {
                ClassDefItem classe = this.futureClazz.get();
                if(classe != null) {
                    Main.this.addClasseNoDex(classe);
                    Main.this.attStatus(true);
                }
                return true;
            } catch(ExecutionException e) {
                Throwable t = e.getCause();
                throw t instanceof Exception ? (Exception)t : e;
            } finally {
                if(Main.this.args.multiDex) {
                    Object object = Main.this.dexRotationLock;
                    synchronized (object) {
                        Main.this.maxMethodIdsInProcess -= this.maxMethodIdsInClass;
                        Main.this.maxFieldIdsInProcess -= this.maxFieldIdsInClass;
                        Main.this.dexRotationLock.notifyAll();
                    }
                }
            }
        }
    }
    public class ClassePraTraduzir implements Callable<ClassDefItem> {
        String nome;
        byte[] bytes;
        DirectClassFile classArq;

        public ClassePraTraduzir(String nome, byte[] bytes, DirectClassFile classArq) {
            this.nome = nome;
            this.bytes = bytes;
            this.classArq = classArq;
        }

        @Override
        public ClassDefItem call() {
            ClassDefItem classe = Main.this.traduzirClasse(this.bytes, this.classArq);
            return classe;
        }
    }

    public class DirectClassFileConsumer implements Callable<Boolean> {
        String nome;
        byte[] bytes;
        Future<DirectClassFile> dcff;

        public DirectClassFileConsumer(String nome, byte[] bytes, Future<DirectClassFile> dcff) {
            this.nome = nome;
            this.bytes = bytes;
            this.dcff = dcff;
        }

        @Override
        public Boolean call() throws Exception {
            DirectClassFile cf = this.dcff.get();
            return this.call(cf);
        }

        public Boolean call(DirectClassFile cf) {
            int maxMethodIdsInClass = 0;
            int maxFieldIdsInClass = 0;
            if(Main.this.args.multiDex) {
                int constantPoolSize = cf.getConstantPool().size();
                maxMethodIdsInClass = constantPoolSize + cf.getMethods().size() + 2;
                maxFieldIdsInClass = constantPoolSize + cf.getFields().size() + 9;
                Object object = Main.this.dexRotationLock;
                synchronized (object) {
                    int numFieldIds;
                    int numMethodIds;
                    DexFile dexArq = Main.this.saidaDex;
                    synchronized (dexArq) {
                        numMethodIds = Main.this.saidaDex.getMethodIds().items().size();
                        numFieldIds = Main.this.saidaDex.getFieldIds().items().size();
                    }
                    while(numMethodIds + maxMethodIdsInClass + Main.this.maxMethodIdsInProcess > Main.this.args.maxNumberOfIdxPerDex || numFieldIds + maxFieldIdsInClass + Main.this.maxFieldIdsInProcess > Main.this.args.maxNumberOfIdxPerDex) {
                        if(Main.this.maxMethodIdsInProcess > 0 || Main.this.maxFieldIdsInProcess > 0) {
                            try {
                                Main.this.dexRotationLock.wait();
                            } catch(InterruptedException interruptedException) {}
                        } else {
                            if(Main.this.saidaDex.getClassDefs().items().size() <= 0) break;
                            Main.this.rotaDexArq();
                        }
                        dexArq = Main.this.saidaDex;
                        synchronized(dexArq) {
                            numMethodIds = Main.this.saidaDex.getMethodIds().items().size();
                            numFieldIds = Main.this.saidaDex.getFieldIds().items().size();
                        }
                    }
                    Main.this.maxMethodIdsInProcess += maxMethodIdsInClass;
                    Main.this.maxFieldIdsInProcess += maxFieldIdsInClass;
                }
            }
            Future<ClassDefItem> cdif = Main.this.classTranslatorPool.submit(new ClassePraTraduzir(this.nome, this.bytes, cf));
            Future<Boolean> res = Main.this.classDefItemConsumer.submit(new ClassDefItemConsumer(this.nome, cdif, maxMethodIdsInClass, maxFieldIdsInClass));
            Main.this.addToDexFutures.add(res);
            return true;
        }
    }

    public class ClassPraAnalisar implements Callable<DirectClassFile> {
        public String nome;
        public byte[] bytes;

        public ClassPraAnalisar(String nome, byte[] bytes) {
            this.nome = nome;
            this.bytes = bytes;
        }

        @Override
        public DirectClassFile call() throws Exception {
            DirectClassFile cf = Main.this.parseClass(this.nome, this.bytes);
            return cf;
        }
    }

    public class FileBytesConsumer implements Consumidor {
        @Override
        public boolean processarArqBytes(String nome, long ultimoModificado, byte[] bytes) {
            return Main.this.processarArqBytes(nome, ultimoModificado, bytes);
        }

        @Override
        public void seErrar(Exception e) {
            if(e instanceof RuntimeException) throw (RuntimeException)e;
            if(e instanceof ErroCtx) {
                Main.this.ctx.err.println("\nEXCEPTION FROM SIMULATION:");
                Main.this.ctx.err.println(e.getMessage() + "\n");
                Main.this.ctx.err.println(((ErroCtx)e).ctx.toString());
            } else if(e instanceof ErroCtx) {
                Main.this.ctx.err.println("\nPARSE ERROR:");
                ErroCtx parseException = (ErroCtx)e;
                if(Main.this.args.debug) parseException.printStackTrace(Main.this.ctx.err);
                else parseException.logCtx(Main.this.ctx.err);
            } else {
                Main.this.ctx.err.println("\nUNEXPECTED TOP-LEVEL EXCEPTION:");
                e.printStackTrace(Main.this.ctx.err);
            }
            Main.this.erros.incrementAndGet();
        }

        @Override
        public void aoIniciarProcesso(File arq) {
            if(Main.this.args.verbose) Main.this.ctx.saida.println("processing archive " + arq + "...");
        }
    }

    public static class Args {
        private static final String MINIMAL_MAIN_DEX_OPTION = "--minimal-main-dex";
        private static final String MAIN_DEX_LIST_OPTION = "--main-dex-list";
        private static final String MULTI_DEX_OPTION = "--multi-dex";
        private static final String INCREMENTAL_OPTION = "--incremental";
        public final DFodaCtx ctx;
        public boolean debug = false;
        public boolean avisos = true;
        public boolean verbose = false;
        public boolean verboseDump = false;
        public boolean coreLibrary = false;
        public String methodToDump = null;
        public int dumpWidth = 0;
        public String outName = null;
        public String humanOutName = null;
        public boolean strictNameCheck = true;
        public boolean emptyOk = false;
        public boolean jarSaida = false;
        public boolean manterClassesNoJar = false;
        public int minSdkVersion = 13;
        public int posicaoInfo = 2;
        public boolean localInfo = true;
        public boolean incremental = false;
        public boolean forceJumbo = false;
        public boolean allowAllInterfaceMethodInvokes = false;
        public String[] fileNames;
        public boolean otimizar = true;
        public String optimizeListFile = null;
        public String dontOptimizeListFile = null;
        public boolean status;
        public CfOptions cfOptions;
        public DexOptions dexOptions;
        public int numThreads = 1;
        public boolean multiDex = false;
        public String mainDexListFile = null;
        public boolean minimalMainDex = false;
        public int maxNumberOfIdxPerDex = 65536;
        private List<String> inputList = null;
        public boolean saidaEDir = false;
        public boolean saidaEDirDex = false;

        public Args(DFodaCtx ctx) {
            this.ctx = ctx;
        }

        public void parseFlags(AnalisadorArgs parser) {
            while(parser.getNext()) {
                if(parser.eArg("--debug")) {
                    this.debug = true;
                    continue;
                }
                if(parser.eArg("--sem-aviso")) {
                    this.avisos = false;
                    continue;
                }
                if(parser.eArg("--verbose")) {
                    this.verbose = true;
                    continue;
                }
                if(parser.eArg("--verbose-mostrar")) {
                    this.verboseDump = true;
                    continue;
                }
                if(parser.eArg("--sem-arquivos")) {
                    this.emptyOk = true;
                    continue;
                }
                if(parser.eArg("--sem-otimizar")) {
                    this.otimizar = false;
                    continue;
                }
                if(parser.eArg("--no-strict")) {
                    this.strictNameCheck = false;
                    continue;
                }
                if(parser.eArg("--nucleo-bibli")) {
                    this.coreLibrary = true;
                    continue;
                }
                if(parser.eArg("--estatisticas")) {
                    this.status = true;
                    continue;
                }
                if(parser.eArg("--otimizar-lista=")) {
                    if(this.dontOptimizeListFile != null) {
                        this.ctx.err.println("--optimizar-lista e --sen-otimizar-lista são incompativeis");
                        throw new RuntimeException();
                    }
                    this.otimizar = true;
                    this.optimizeListFile = parser.ultimoValor;
                    continue;
                }
                if(parser.eArg("--sem-otimizar-lista=")) {
                    if(this.dontOptimizeListFile != null) {
                        this.ctx.err.println("--optimizar-lista e --sem-otimizar-lista são incompativeis");
                        throw new RuntimeException();
                    }
                    this.otimizar = true;
                    this.dontOptimizeListFile = parser.ultimoValor;
                    continue;
                }
                if(parser.eArg("--manter-classes")) {
                    this.manterClassesNoJar = true;
                    continue;
                }
                if(parser.eArg("--saida=")) {
                    this.outName = parser.ultimoValor;
                    if(new File(this.outName).isDirectory()) {
                        this.jarSaida = false;
                        this.saidaEDir = true;
                        continue;
                    }
                    if(FileUtils.hasArchiveSuffix(this.outName)) {
                        this.jarSaida = true;
                        continue;
                    }
                    if(this.outName.endsWith(Main.extensao) || this.outName.equals("-")) {
                        this.jarSaida = false;
                        this.saidaEDirDex = true;
                        continue;
                    }
                    this.ctx.err.println("extensão de saida desconhecida: " + this.outName);
                    throw new RuntimeException();
                }
                if(parser.eArg("--mostrar-so=")) {
                    this.humanOutName = parser.ultimoValor;
                    continue;
                }
                if(parser.eArg("--mostrar-largura=")) {
                    this.dumpWidth = Integer.parseInt(parser.ultimoValor);
                    continue;
                }
                if(parser.eArg("--mostrar-metodo=")) {
                    this.methodToDump = parser.ultimoValor;
                    this.jarSaida = false;
                    continue;
                }
                if(parser.eArg("--posicoes=")) {
                    String pstr = parser.ultimoValor.intern();
                    if(pstr == "nada") {
                        this.posicaoInfo = 1;
                        continue;
                    }
                    if(pstr == "importante") {
                        this.posicaoInfo = 3;
                        continue;
                    }
                    if(pstr == "linhas") {
                        this.posicaoInfo = 2;
                        continue;
                    }
                    this.ctx.err.println("unknown positions option: " + pstr);
                    throw new RuntimeException();
                }
                if(parser.eArg("--no-locals")) {
                    this.localInfo = false;
                    continue;
                }
                if(parser.eArg("--num-threads=")) {
                    this.numThreads = Integer.parseInt(parser.ultimoValor);
                    continue;
                }
                if(parser.eArg(INCREMENTAL_OPTION)) {
                    this.incremental = true;
                    continue;
                }
                if(parser.eArg("--force-jumbo")) {
                    this.forceJumbo = true;
                    continue;
                }
                if(parser.eArg(MULTI_DEX_OPTION)) {
                    this.multiDex = true;
                    continue;
                }
                if(parser.eArg("--main-dex-list=")) {
                    this.mainDexListFile = parser.ultimoValor;
                    continue;
                }
                if(parser.eArg(MINIMAL_MAIN_DEX_OPTION)) {
                    this.minimalMainDex = true;
                    continue;
                }
                if(parser.eArg("--set-max-idx-number=")) {
                    this.maxNumberOfIdxPerDex = Integer.parseInt(parser.ultimoValor);
                    continue;
                }
                if(parser.eArg("--input-list=")) {
                    File inputListFile = new File(parser.ultimoValor);
                    try {
                        this.inputList = new ArrayList<String>();
                        Main.readPathsFromFile(inputListFile.getAbsolutePath(), this.inputList);
                        continue;
                    } catch(IOException e) {
                        this.ctx.err.println("Unable to read input list file: " + inputListFile.getName());
                        throw new RuntimeException();
                    }
                }
                if(parser.eArg("--min-sdk-version=")) {
                    int value;
                    String arg = parser.ultimoValor;
                    try {
                        value = Integer.parseInt(arg);
                    } catch(NumberFormatException e) {
                        value = -1;
                    }
                    if(value < 1) {
                        System.err.println("improper min-sdk-version option: " + arg);
                        throw new RuntimeException();
                    }
                    this.minSdkVersion = value;
                    continue;
                }
                if(parser.eArg("--allow-all-interface-method-invokes")) {
                    this.allowAllInterfaceMethodInvokes = true;
                    continue;
                }
                this.ctx.err.println("unknown option: " + parser.atual);
                throw new RuntimeException();
            }
        }

        public void analisar(String[] args) {
            AnalisadorArgs parser = new AnalisadorArgs(args);
            this.parseFlags(parser);
            this.fileNames = parser.getRemaining();
            if(this.inputList != null && !this.inputList.isEmpty()) {
                this.inputList.addAll(Arrays.asList(this.fileNames));
                this.fileNames = this.inputList.toArray(new String[this.inputList.size()]);
            }
            if(this.fileNames.length == 0) {
                if(!this.emptyOk) {
                    this.ctx.err.println("no input files specified");
                    throw new RuntimeException();
                }
            } else if(this.emptyOk) {
                this.ctx.saida.println("ignoring input files");
            }
            if(this.humanOutName == null && this.methodToDump != null) {
                this.humanOutName = "-";
            }
            if(this.mainDexListFile != null && !this.multiDex) {
                this.ctx.err.println("--main-dex-list is only supported in combination with --multi-dex");
                throw new RuntimeException();
            }
            if(this.minimalMainDex && (this.mainDexListFile == null || !this.multiDex)) {
                this.ctx.err.println("--minimal-main-dex is only supported in combination with --multi-dex and --main-dex-list");
                throw new RuntimeException();
            }
            if(this.multiDex && this.incremental) {
                this.ctx.err.println("--incremental is not supported with --multi-dex");
                throw new RuntimeException();
            }
            if(this.multiDex && this.saidaEDirDex) {
                this.ctx.err.println("Unsupported output \"" + this.outName + "\". " + MULTI_DEX_OPTION + " supports only archive or directory output");
                throw new RuntimeException();
            }
            if(this.saidaEDir && !this.multiDex) {
                this.outName = new File(this.outName, "classes.dex").getPath();
            }
            this.makeOptionsObjects();
        }

        public void parseFlags(String[] flags) {
            this.parseFlags(new AnalisadorArgs(flags));
        }

        public void makeOptionsObjects() {
            this.cfOptions = new CfOptions();
            this.cfOptions.positionInfo = this.posicaoInfo;
            this.cfOptions.localInfo = this.localInfo;
            this.cfOptions.strictNameCheck = this.strictNameCheck;
            this.cfOptions.optimize = this.otimizar;
            this.cfOptions.optimizeListFile = this.optimizeListFile;
            this.cfOptions.dontOptimizeListFile = this.dontOptimizeListFile;
            this.cfOptions.statistics = this.status;
            this.cfOptions.warn = this.avisos ? this.ctx.err : this.ctx.noop;
            this.dexOptions = new DexOptions(this.ctx.err);
            this.dexOptions.minSdkVersion = this.minSdkVersion;
            this.dexOptions.forceJumbo = this.forceJumbo;
            this.dexOptions.allowAllInterfaceMethodInvokes = this.allowAllInterfaceMethodInvokes;
        }

        public static class AnalisadorArgs {
            public final String[] args;
            public int idc;
            public String atual;
            public String ultimoValor;

            public AnalisadorArgs(String[] args) {
                this.args = args;
                this.idc = 0;
            }

            public boolean getNext() {
                if(this.idc >= this.args.length) {
                    return false;
                }
                this.atual = this.args[this.idc];
                if(this.atual.equals("--") || !this.atual.startsWith("--")) return false;
                ++this.idc;
                return true;
            }

            public boolean getNextValue() {
                if(this.idc >= this.args.length) return false;
                this.atual = this.args[this.idc];
                ++this.idc;
                return true;
            }

            public String[] getRemaining() {
                int n = this.args.length - this.idc;
                String[] resto = new String[n];
                if(n > 0) System.arraycopy(this.args, this.idc, resto, 0, n);
                return resto;
            }

            public boolean eArg(String prefixo) {
                int n = prefixo.length();
                if(n > 0 && prefixo.charAt(n - 1) == '=') {
                    if(this.atual.startsWith(prefixo)) {
                        this.ultimoValor = this.atual.substring(n);
                        return true;
                    }
                    if(this.atual.equals(prefixo = prefixo.substring(0, n - 1))) {
                        if(this.getNextValue()) {
                            this.ultimoValor = this.atual;
                            return true;
                        }
                        System.err.println("Missing value after parameter " + prefixo);
                        throw new RuntimeException();
                    }
                    return false;
                }
                return this.atual.equals(prefixo);
            }
        }
    }

    public class BestEffortMainDexListFilter implements FiltroArqNome {
        Map<String, List<String>> mapa = new HashMap<String, List<String>>();
		
        public BestEffortMainDexListFilter() {
            for(String camClasse : Main.this.classesNoDex) {
                String normalizado = corrigirCam(camClasse);
                String simples = this.obterNome(normalizado);
                List<String> camAbs = this.mapa.get(simples);
                if(camAbs == null) {
                    camAbs = new ArrayList<String>(1);
                    this.mapa.put(simples, camAbs);
                }
                camAbs.add(normalizado);
            }
        }

        @Override
        public boolean aceitar(String cam) {
            if(cam.endsWith(".class")) {
                String normalizado = corrigirCam(cam);
                String simples = this.obterNome(normalizado);
                List<String> camsAbs = this.mapa.get(simples);
                if(camsAbs != null) {
                    for(String camAbs : camsAbs) {
                        if(!normalizado.endsWith(camAbs)) continue;
                        return true;
                    }
                }
                return false;
            }
            return true;
        }

        public String obterNome(String cam) {
            int idc = cam.lastIndexOf(47);
            if(idc >= 0) return cam.substring(idc + 1);
            return cam;
        }
    }

    public class MainDexListFilter implements FiltroArqNome {
        @Override
        public boolean aceitar(String camAbs) {
            if(camAbs.endsWith(".class")) {
                String cam = corrigirCam(camAbs);
                return classesNoDex.contains(cam);
            }
            return true;
        }
    }

    public static class RemoveModuleInfoFilter implements FiltroArqNome {
        public final FiltroArqNome delegate;

        public RemoveModuleInfoFilter(FiltroArqNome delegate) {
            this.delegate = delegate;
        }

        @Override
        public boolean aceitar(String cam) {
            return this.delegate.aceitar(cam) && !"module-info.class".equals(cam);
        }
    }

    public static class NaoFiltro implements FiltroArqNome {
        public final FiltroArqNome filtro;

        public NaoFiltro(FiltroArqNome filtro) {
            this.filtro = filtro;
        }

        @Override
        public boolean aceitar(String cam) {
            return !this.filtro.aceitar(cam);
        }
    }
}

