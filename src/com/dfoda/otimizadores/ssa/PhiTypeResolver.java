package com.dfoda.otimizadores.ssa;

import java.util.BitSet;
import java.util.List;
import com.dfoda.otimizadores.rop.codigo.RegisterSpec;
import com.dfoda.otimizadores.rop.codigo.LocalItem;
import com.dfoda.otimizadores.rop.codigo.RegisterSpecList;
import com.dfoda.otimizadores.rop.tipo.TypeBearer;
import com.dfoda.otimizadores.ca.codigo.Merger;

public class PhiTypeResolver {
    SsaMethod ssaMeth;
    private final BitSet worklist;

    public static void process(SsaMethod ssaMeth) {
        new PhiTypeResolver(ssaMeth).run();
    }

    private PhiTypeResolver(SsaMethod ssaMeth) {
        this.ssaMeth = ssaMeth;
        this.worklist = new BitSet(ssaMeth.getRegCount());
    }

    private void run() {
        SsaInsn definsn;
        int reg;
        int regCount = this.ssaMeth.getRegCount();
        for (reg = 0; reg < regCount; ++reg) {
            definsn = this.ssaMeth.getDefinitionForRegister(reg);
            if (definsn == null || definsn.getResult().getBasicType() != 0) continue;
            this.worklist.set(reg);
        }
        while (0 <= (reg = this.worklist.nextSetBit(0))) {
            this.worklist.clear(reg);
            definsn = (PhiInsn)this.ssaMeth.getDefinitionForRegister(reg);
            if (!this.resolveResultType((PhiInsn)definsn)) continue;
            List<SsaInsn> useList = this.ssaMeth.getUseListForRegister(reg);
            int sz = useList.size();
            for (int i = 0; i < sz; ++i) {
                SsaInsn useInsn = useList.get(i);
                RegisterSpec resultReg = useInsn.getResult();
                if (resultReg == null || !(useInsn instanceof PhiInsn)) continue;
                this.worklist.set(resultReg.getReg());
            }
        }
    }

    private static boolean equalsHandlesNulls(LocalItem a, LocalItem b) {
        return a == b || a != null && a.equals(b);
    }

    boolean resolveResultType(PhiInsn insn) {
        insn.updateSourcesToDefinitions(this.ssaMeth);
        RegisterSpecList sources = insn.getSources();
        RegisterSpec first = null;
        int firstIndex = -1;
        int szSources = sources.size();
        for (int i = 0; i < szSources; ++i) {
            RegisterSpec rs = sources.get(i);
            if (rs.getBasicType() == 0) continue;
            first = rs;
            firstIndex = i;
        }
        if (first == null) {
            return false;
        }
        LocalItem firstLocal = first.getLocalItem();
        TypeBearer mergedType = first.getType();
        boolean sameLocals = true;
        for (int i = 0; i < szSources; ++i) {
            RegisterSpec rs;
            if (i == firstIndex || (rs = sources.get(i)).getBasicType() == 0) continue;
            sameLocals = sameLocals && PhiTypeResolver.equalsHandlesNulls(firstLocal, rs.getLocalItem());
            mergedType = Merger.mergeType(mergedType, rs.getType());
        }
        if (mergedType == null) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < szSources; ++i) {
                sb.append(sources.get(i).toString());
                sb.append(' ');
            }
            throw new RuntimeException("Couldn't map types in phi insn:" + sb);
        }
		TypeBearer newResultType = mergedType;
        LocalItem newLocal = sameLocals ? firstLocal : null;
        RegisterSpec result = insn.getResult();
        if (result.getTypeBearer() == newResultType && PhiTypeResolver.equalsHandlesNulls(newLocal, result.getLocalItem())) {
            return false;
        }
        insn.changeResultType(newResultType, newLocal);
        return true;
    }
}

