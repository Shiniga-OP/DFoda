package com.dfoda.otimizadores.ca.direto;

import java.io.IOException;
import com.dfoda.otimizadores.rop.cst.ConstantPool;
import com.dfoda.util.ByteArray;
import com.dfoda.otimizadores.rop.anotacao.Annotations;
import com.dfoda.otimizadores.rop.anotacao.AnnotationVisibility;
import com.dfoda.util.Hex;
import com.dfoda.otimizadores.rop.anotacao.AnnotationsList;
import com.dfoda.otimizadores.rop.anotacao.Annotation;
import com.dfoda.otimizadores.rop.cst.CstString;
import com.dfoda.otimizadores.rop.cst.CstType;
import com.dfoda.otimizadores.rop.tipo.Type;
import com.dfoda.otimizadores.rop.anotacao.NameValuePair;
import com.dfoda.otimizadores.rop.cst.Constant;
import com.dfoda.otimizadores.rop.cst.CstLiteralBits;
import com.dfoda.otimizadores.rop.cst.CstInteger;
import com.dfoda.otimizadores.rop.cst.CstChar;
import com.dfoda.otimizadores.rop.cst.CstByte;
import com.dfoda.otimizadores.rop.cst.CstDouble;
import com.dfoda.otimizadores.rop.cst.CstFloat;
import com.dfoda.otimizadores.rop.cst.CstLong;
import com.dfoda.otimizadores.rop.cst.CstShort;
import com.dfoda.otimizadores.rop.cst.CstBoolean;
import com.dfoda.otimizadores.rop.cst.CstAnnotation;
import com.dfoda.otimizadores.rop.cst.CstEnumRef;
import com.dfoda.otimizadores.rop.cst.CstNat;
import com.dfoda.otimizadores.rop.cst.CstArray;
import com.dfoda.otimizadores.ca.inter.ParseObserver;
import com.dex.util.ErroCtx;

public final class AnalisadorAnotacao {
    public final DirectClassFile cf;
    public final ConstantPool pool;
    public final ByteArray bytes;
    public final ParseObserver obs;
    public final ByteArray.MyDataInputStream entrada;
    public int analisadorCursor;

    public AnalisadorAnotacao(DirectClassFile cf, int antes, int tam, ParseObserver obs) {
        if(cf == null) throw new NullPointerException("cf == null");
        this.cf = cf;
        this.pool = cf.getConstantPool();
        this.obs = obs;
        this.bytes = cf.getBytes().slice(antes, antes + tam);
        this.entrada = this.bytes.makeDataInputStream();
        this.analisadorCursor = 0;
    }

    public Constant analisarValorAtributo() {
        Constant resultado;
        try {
            resultado = this.analisarValor();
            if(this.entrada.available() != 0) throw new ErroCtx("dados extras no atributo");
        } catch(IOException e) {
            throw new RuntimeException("essa desgraça não devia acontecer: ", e);
        }
        return resultado;
    }

    public AnnotationsList analisarParamAtributo(AnnotationVisibility visivel) {
        AnnotationsList resultado;
        try {
            resultado = this.analisarListaAnotacoes(visivel);
            if(this.entrada.available() != 0) throw new ErroCtx("dados extras no atributo");
        } catch(IOException e) {
            throw new RuntimeException("essa desgraça não devia acontecer: ", e);
        }
        return resultado;
    }

    public Annotations parseAnnotationAttribute(AnnotationVisibility visivel) {
        Annotations resultado;
        try {
            resultado = this.analisarAnotacoes(visivel);
            if(this.entrada.available() != 0) throw new ErroCtx("dados extras no atributo");
        } catch(IOException e) {
            throw new RuntimeException("essa desgraça não devia acontecer: ", e);
        }
        return resultado;
    }

    public AnnotationsList analisarListaAnotacoes(AnnotationVisibility visivel) throws IOException {
        int conta = this.entrada.readUnsignedByte();
        if(this.obs != null) this.analisado(1, "num_parametros: " + Hex.u1(conta));
        AnnotationsList exLista = new AnnotationsList(conta);
        for(int i = 0; i < conta; ++i) {
            if(this.obs != null) {
                this.analisado(0, "anotacoes_parametros[" + i + "]:");
                this.obs.changeIndent(1);
            }
            Annotations anotacoes = this.analisarAnotacoes(visivel);
            exLista.set(i, anotacoes);
            if(this.obs == null) continue;
            this.obs.changeIndent(-1);
        }
        exLista.setImmutable();
        return exLista;
    }

    public Annotations analisarAnotacoes(AnnotationVisibility visivel) throws IOException {
        int conta = this.entrada.readUnsignedShort();
        if(this.obs != null) this.analisado(2, "num_anotacoes: " + Hex.u2(conta));
        Annotations anotacoes = new Annotations();
        for(int i = 0; i < conta; ++i) {
            if(this.obs != null) {
                this.analisado(0, "anotacoes[" + i + "]:");
                this.obs.changeIndent(1);
            }
            Annotation anotacao = this.analisarAnotacao(visivel);
            anotacoes.add(anotacao);
            if(this.obs == null) continue;
            this.obs.changeIndent(-1);
        }
        anotacoes.setImmutable();
        return anotacoes;
    }

    public Annotation analisarAnotacao(AnnotationVisibility visivel) throws IOException {
        this.requirirTam(4);
        int tipoIdc = this.entrada.readUnsignedShort();
        int numElemens = this.entrada.readUnsignedShort();
        CstString tipoTex = (CstString)this.pool.get(tipoIdc);
        CstType tipo = new CstType(Type.intern(tipoTex.getString()));
        if(this.obs != null) {
            this.analisado(2, "tipo: " + tipo.toHuman());
            this.analisado(2, "num_elementos: " + numElemens);
        }
        Annotation anotacao = new Annotation(tipo, visivel);
        for(int i = 0; i < numElemens; ++i) {
            if(this.obs != null) {
                this.analisado(0, "elementos[" + i + "]:");
                this.obs.changeIndent(1);
            }
            NameValuePair elemen = this.analisarElemen();
            anotacao.add(elemen);
            if(this.obs == null) continue;
            this.obs.changeIndent(-1);
        }
        anotacao.setImmutable();
        return anotacao;
    }

    public NameValuePair analisarElemen() throws IOException {
        this.requirirTam(5);
        int elemenNomeIdc = this.entrada.readUnsignedShort();
        CstString elemenNome = (CstString)this.pool.get(elemenNomeIdc);
        if(this.obs != null) {
            this.analisado(2, "elemento_nome: " + elemenNome.toHuman());
            this.analisado(0, "valor: ");
            this.obs.changeIndent(1);
        }
        Constant valor = this.analisarValor();
        if(this.obs != null) this.obs.changeIndent(-1);
        return new NameValuePair(elemenNome, valor);
    }

    public Constant analisarValor() throws IOException {
        int marca = this.entrada.readUnsignedByte();
        if(this.obs != null) {
            CstString marcaLegivel = new CstString(Character.toString((char)marca));
            this.analisado(1, "marca: " + marcaLegivel.toQuoted());
        }
        switch(marca) {
            case 66: {
                CstLiteralBits valor = (CstInteger)this.analisarConst();
                return CstByte.make(((CstInteger)valor).getValue());
            }
            case 67: {
                CstLiteralBits valor = (CstInteger)this.analisarConst();
                return CstChar.make(((CstInteger)valor).getValue());
            }
            case 68: {
                return (CstDouble)this.analisarConst();
            }
            case 70: {
                return (CstFloat)this.analisarConst();
            }
            case 73: {
                return (CstInteger)this.analisarConst();
            }
            case 74: {
                return (CstLong)this.analisarConst();
            }
            case 83: {
                CstLiteralBits valor = (CstInteger)this.analisarConst();
                return CstShort.make(((CstInteger)valor).getValue());
            }
            case 90: {
                CstLiteralBits valor = (CstInteger)this.analisarConst();
                return CstBoolean.make(((CstInteger)valor).getValue());
            }
            case 99: {
                int classInfoIdc = this.entrada.readUnsignedShort();
                CstString valor = (CstString)this.pool.get(classInfoIdc);
                Type tipo = Type.internReturnType(valor.getString());
                if(this.obs != null) this.analisado(2, "classe_info: " + tipo.toHuman());
                return new CstType(tipo);
            }
            case 115: {
                return this.analisarConst();
            }
            case 101: {
                this.requirirTam(4);
                int tipoNomeIdc = this.entrada.readUnsignedShort();
                int constNomeIdc = this.entrada.readUnsignedShort();
                CstString tipoNome = (CstString)this.pool.get(tipoNomeIdc);
                CstString constNome = (CstString)this.pool.get(constNomeIdc);
                if(this.obs != null) {
                    this.analisado(2, "tipo_nome: " + tipoNome.toHuman());
                    this.analisado(2, "const_nome: " + constNome.toHuman());
                }
                return new CstEnumRef(new CstNat(constNome, tipoNome));
            }
            case 64: {
                Annotation anotacao = this.analisarAnotacao(AnnotationVisibility.EMBEDDED);
                return new CstAnnotation(anotacao);
            }
            case 91: {
                this.requirirTam(2);
                int numValores = this.entrada.readUnsignedShort();
                CstArray.List lista = new CstArray.List(numValores);
                if(this.obs != null) {
                    this.analisado(2, "num_valores: " + numValores);
                    this.obs.changeIndent(1);
                }
                for(int i = 0; i < numValores; ++i) {
                    if(this.obs != null) {
                        this.obs.changeIndent(-1);
                        this.analisado(0, "elemento_valor[" + i + "]:");
                        this.obs.changeIndent(1);
                    }
                    lista.set(i, this.analisarValor());
                }
				if(this.obs != null) this.obs.changeIndent(-1);
                lista.setImmutable();
                return new CstArray(lista);
            }
        }
        throw new ErroCtx("unknown annotation tag: " + Hex.u1(marca));
    }

    public Constant analisarConst() throws IOException {
        int constValorIdc = this.entrada.readUnsignedShort();
        Constant valor = this.pool.get(constValorIdc);
        if(this.obs != null) {
            String legivel = valor instanceof CstString ? ((CstString)valor).toQuoted() : valor.toHuman();
            this.analisado(2, "const_valor: " + legivel);
        }
        return valor;
    }

    public void requirirTam(int tamRequirido) throws IOException {
        if(this.entrada.available() < tamRequirido) throw new ErroCtx("truncated annotation attribute");
    }

    public void analisado(int tam, String msg) {
        this.obs.parsed(this.bytes, this.analisadorCursor, tam, msg);
        this.analisadorCursor += tam;
    }
}

