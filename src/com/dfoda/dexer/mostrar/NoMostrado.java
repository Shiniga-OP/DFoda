package com.dfoda.dexer.mostrar;

import com.dfoda.otimizadores.ca.inter.ParseObserver;
import com.dfoda.otimizadores.ca.direto.DirectClassFile;
import com.dfoda.dex.DexOptions;
import com.dfoda.util.ByteArray;
import com.dfoda.otimizadores.ca.direto.StdAttributeFactory;
import com.dfoda.otimizadores.ca.inter.Method;
import com.dfoda.otimizadores.ca.codigo.ConcreteMethod;
import com.dfoda.otimizadores.rop.codigo.DexTranslationAdvice;
import com.dfoda.otimizadores.rop.codigo.RopMethod;
import com.dfoda.otimizadores.ca.codigo.Ropper;
import com.dfoda.otimizadores.rop.codigo.AccessFlags;
import com.dfoda.otimizadores.ca.inter.Member;
import com.dfoda.otimizadores.rop.codigo.BasicBlockList;
import com.dfoda.util.Hex;
import com.dfoda.otimizadores.rop.codigo.BasicBlock;
import com.dfoda.otimizadores.ssa.Optimizer;
import com.dfoda.util.IntList;

public class NoMostrado implements ParseObserver {
    public DirectClassFile arqClasse;
    public final byte[] bytes;
    public final String arqCam;
    public final boolean strictParse;
    public final boolean otimizar;
    public final Args args;
    public final DexOptions dexOptions;

    static void mostrar(byte[] bytes, String arqCam, Args args) {
        new NoMostrado(bytes, arqCam, args).run();
    }

    public NoMostrado(byte[] bytes, String arqCam, Args args) {
        this.bytes = bytes;
        this.arqCam = arqCam;
        this.strictParse = args.strictParse;
        this.otimizar = args.otimizar;
        this.args = args;
        this.dexOptions = new DexOptions();
    }

    private void run() {
        ByteArray ba = new ByteArray(this.bytes);
        this.arqClasse = new DirectClassFile(ba, this.arqCam, this.strictParse);
        this.arqClasse.setAttributeFactory(StdAttributeFactory.THE_ONE);
        this.arqClasse.getMagic();
        DirectClassFile liveCf = new DirectClassFile(ba, this.arqCam, this.strictParse);
        liveCf.setAttributeFactory(StdAttributeFactory.THE_ONE);
        liveCf.setObserver(this);
        liveCf.getMagic();
    }

    protected boolean shouldDumpMethod(String nome) {
        return this.args.metodo == null || this.args.metodo.equals(nome);
    }

    @Override
    public void changeIndent(int indentDelta) {}

    @Override
    public void parsed(ByteArray bytes, int offset, int len, String human) {}

    @Override
    public void startParsingMember(ByteArray bytes, int antes, String nome, String descritor) {}

    @Override
    public void endParsingMember(ByteArray bytes, int antes, String nome, String descritor, Member membro) {
        if(!(membro instanceof Method)) return;
        if(!this.shouldDumpMethod(nome)) return;
		
        ConcreteMethod mt = new ConcreteMethod((Method)membro, this.arqClasse, true, true);
        DexTranslationAdvice advice = DexTranslationAdvice.THE_ONE;
        RopMethod rmt = Ropper.convert(mt, advice, this.arqClasse.getMethods(), this.dexOptions);
        if(this.otimizar) {
            boolean eEstatico = AccessFlags.isStatic(mt.getAccessFlags());
            rmt = Optimizer.optimize(rmt, BaseDumper.computeParamWidth(mt, eEstatico), eEstatico, true, advice);
        }
        System.out.println("digrafo " + nome + "{");
        System.out.println("\tprimeiro -> n" + Hex.u2(rmt.getFirstLabel()) + ";");
        BasicBlockList blocks = rmt.getBlocks();
        int sz = blocks.size();
        for(int i = 0; i < sz; ++i) {
            BasicBlock bb = blocks.get(i);
            int label = bb.getLabel();
            IntList successors = bb.getSuccessors();
            if(successors.size() == 0) {
                System.out.println("\tn" + Hex.u2(label) + " -> retorna;");
                continue;
            }
            if(successors.size() == 1) {
                System.out.println("\tn" + Hex.u2(label) + " -> n" + Hex.u2(successors.get(0)) + ";");
                continue;
            }
            System.out.print("\tn" + Hex.u2(label) + " -> {");
            for(int j = 0; j < successors.size(); ++j) {
                int successor = successors.get(j);
                if(successor == bb.getPrimarySuccessor()) continue;
                System.out.print(" n" + Hex.u2(successor) + " ");
            }
            System.out.println("};");
            System.out.println("\tn" + Hex.u2(label) + " -> n" + Hex.u2(bb.getPrimarySuccessor()) + " [label=\"primary\"];");
        }
        System.out.println("}");
    }
}

