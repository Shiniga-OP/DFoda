package com.dfoda.dex.arquivo;

import java.io.PrintWriter;
import com.dfoda.dex.codigo.DalvCode;
import com.dfoda.otimizadores.rop.cst.CstMethodRef;
import com.dex.util.ErroCtx;
import com.dfoda.util.AnnotatedOutput;
import com.dfoda.dex.codigo.PositionList;
import com.dfoda.dex.codigo.LocalList;
import com.dfoda.dex.codigo.DalvInsnList;

public class DebugInfoItem extends OffsettedItem {
    private static final int ALIGNMENT = 1;
    private static final boolean ENABLE_ENCODER_SELF_CHECK = false;
    private final DalvCode code;
    private byte[] encoded;
    private final boolean isStatic;
    private final CstMethodRef ref;

    public DebugInfoItem(DalvCode code, boolean isStatic, CstMethodRef ref) {
        super(1, -1);
        if (code == null) {
            throw new NullPointerException("code == null");
        }
        this.code = code;
        this.isStatic = isStatic;
        this.ref = ref;
    }

    @Override
    public ItemType itemType() {
        return ItemType.TYPE_DEBUG_INFO_ITEM;
    }

    @Override
    public void addContents(DexFile file) {
    }

    @Override
    protected void place0(Section addedTo, int offset) {
        try {
            this.encoded = this.encode(addedTo.getFile(), null, null, null, false);
            this.setWriteSize(this.encoded.length);
        }
        catch (RuntimeException ex) {
            throw ErroCtx.comCtx(ex, "...while placing debug info for " + this.ref.toHuman());
        }
    }

    @Override
    public String toHuman() {
        throw new RuntimeException("unsupported");
    }

    public void annotateTo(DexFile file, AnnotatedOutput out, String prefix) {
        this.encode(file, prefix, null, out, false);
    }

    public void debugPrint(PrintWriter out, String prefix) {
        this.encode(null, prefix, out, null, false);
    }

    @Override
    protected void writeTo0(DexFile file, AnnotatedOutput out) {
        if (out.annotates()) {
            out.annotate(this.offsetString() + " debug info");
            this.encode(file, null, null, out, true);
        }
        out.write(this.encoded);
    }

    private byte[] encode(DexFile file, String prefix, PrintWriter debugPrint, AnnotatedOutput out, boolean consume) {
        byte[] result = this.encode0(file, prefix, debugPrint, out, consume);
        return result;
    }

    private byte[] encode0(DexFile file, String prefix, PrintWriter debugPrint, AnnotatedOutput out, boolean consume) {
        PositionList positions = this.code.getPositions();
        LocalList locals = this.code.getLocals();
        DalvInsnList insns = this.code.getInsns();
        int codeSize = insns.codeSize();
        int regSize = insns.getRegistersSize();
        DebugInfoEncoder encoder = new DebugInfoEncoder(positions, locals, file, codeSize, regSize, this.isStatic, this.ref);
        byte[] result = debugPrint == null && out == null ? encoder.convert() : encoder.convertAndAnnotate(prefix, debugPrint, out, consume);
        return result;
    }
}

