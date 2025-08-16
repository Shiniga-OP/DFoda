package com.dfoda.dexer.mostrar;

import java.io.UnsupportedEncodingException;
import com.dfoda.util.HexParser;
import com.dex.util.FileUtils;
import com.dex.util.ErroCtx;

public class Main {
    public static final Args argsAnalisados = new Args();

    public static void rodar(String... args) {
        String arg;
        int i;
        for(i = 0; i < args.length && !(arg = args[i]).equals("--") && arg.startsWith("--"); ++i) {
            if(arg.equals("--bytes")) {
                argsAnalisados.bytesCrus = true;
                continue;
            }
            if(arg.equals("--basico-blocos")) {
                argsAnalisados.blocosBasicos = true;
                continue;
            }
            if(arg.equals("--rop-blocos")) {
                argsAnalisados.blocosRop = true;
                continue;
            }
            if(arg.equals("--otimizar")) {
                argsAnalisados.otimizar = true;
                continue;
            }
            if(arg.equals("--ssa-blocos")) {
                argsAnalisados.blocosSsa = true;
                continue;
            }
            if(arg.startsWith("--ssa-passo=")) {
                argsAnalisados.passoSsa = arg.substring(arg.indexOf(61) + 1);
                continue;
            }
            if(arg.equals("--debug")) {
                argsAnalisados.debug = true;
                continue;
            }
            if(arg.equals("--no")) {
                argsAnalisados.noMostrar = true;
                continue;
            }
            if(arg.equals("--strict")) {
                argsAnalisados.strictParse = true;
                continue;
            }
            if(arg.startsWith("--largura=")) {
                arg = arg.substring(arg.indexOf(61) + 1);
                argsAnalisados.largura = Integer.parseInt(arg);
                continue;
            }
            if(arg.startsWith("--metodo=")) {
                argsAnalisados.metodo = arg = arg.substring(arg.indexOf(61) + 1);
                continue;
            }
            System.err.println("Opção desconhecida: " + arg);
            throw new RuntimeException("uso");
        }
        if(i == args.length) {
            System.err.println("Sem arquivos de entrada");
            throw new RuntimeException("uso");
        }
        while(i < args.length) {
            try {
                String nome = args[i];
                System.out.println("reading " + nome + "...");
                byte[] bytes = FileUtils.readFile(nome);
                if(!nome.endsWith(".class")) {
                    String fonte;
                    try {
                        fonte = new String(bytes, "utf-8");
                    } catch(UnsupportedEncodingException e) {
                        throw new RuntimeException("Saporra não devia acontecer não: "+e);
                    }
                    bytes = HexParser.parse(fonte);
                }
                processarUm(nome, bytes);
            } catch(ErroCtx e) {
                System.err.println("\nErro analisando:");
                if(argsAnalisados.debug) e.printStackTrace();
                e.logCtx(System.err);
            }
            ++i;
        }
    }

    public static void processarUm(String nome, byte[] bytes) {
        if(argsAnalisados.noMostrar) NoMostrado.mostrar(bytes, nome, argsAnalisados);
        else if(argsAnalisados.blocosBasicos) BlockDumper.dump(bytes, System.out, nome, false, argsAnalisados);
        else if(argsAnalisados.blocosRop) BlockDumper.dump(bytes, System.out, nome, true, argsAnalisados);
        else if(argsAnalisados.blocosSsa) {
            argsAnalisados.otimizar = false;
            SsaDumper.dump(bytes, System.out, nome, argsAnalisados);
        } else ClassDumper.dump(bytes, System.out, nome, argsAnalisados);
    }
}
