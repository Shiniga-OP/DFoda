package com.dfoda.otimizadores.ssa;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import com.dfoda.otimizadores.rop.cst.TypedConstant;
import com.dfoda.otimizadores.rop.codigo.RegisterSpec;
import com.dfoda.otimizadores.rop.codigo.Rops;
import com.dfoda.otimizadores.rop.codigo.Rop;
import com.dfoda.otimizadores.rop.codigo.SourcePosition;
import com.dfoda.otimizadores.rop.codigo.PlainCstInsn;
import com.dfoda.otimizadores.rop.codigo.ThrowingCstInsn;
import com.dfoda.otimizadores.rop.codigo.PlainInsn;
import com.dfoda.otimizadores.rop.codigo.RegisterSpecList;
import com.dfoda.otimizadores.rop.tipo.StdTypeList;
import com.dfoda.otimizadores.rop.cst.Constant;
import com.dfoda.otimizadores.rop.tipo.TypeBearer;
import com.dfoda.otimizadores.rop.cst.CstString;
import com.dfoda.otimizadores.rop.codigo.LocalItem;

public class ConstCollector {
    private static final int MAX_COLLECTED_CONSTANTS = 5;
    private static final boolean COLLECT_STRINGS = false;
    private static final boolean COLLECT_ONE_LOCAL = false;
    private final SsaMethod ssaMeth;

    public static void process(SsaMethod ssaMethod) {
        ConstCollector cc = new ConstCollector(ssaMethod);
        cc.run();
    }

    private ConstCollector(SsaMethod ssaMethod) {
        this.ssaMeth = ssaMethod;
    }

    private void run() {
        int regSz = this.ssaMeth.getRegCount();
        ArrayList<TypedConstant> constantList = this.getConstsSortedByCountUse();
        int toCollect = Math.min(constantList.size(), 5);
        SsaBasicBlock start = this.ssaMeth.getEntryBlock();
        HashMap<TypedConstant, RegisterSpec> newRegs = new HashMap<TypedConstant, RegisterSpec>(toCollect);
        for (int i = 0; i < toCollect; ++i) {
            TypedConstant cst = constantList.get(i);
            RegisterSpec result = RegisterSpec.make(this.ssaMeth.makeNewSsaReg(), cst);
            Rop constRop = Rops.opConst(cst);
            if (constRop.getBranchingness() == 1) {
                start.addInsnToHead(new PlainCstInsn(Rops.opConst(cst), SourcePosition.NO_INFO, result, RegisterSpecList.EMPTY, cst));
            } else {
                SsaBasicBlock entryBlock = this.ssaMeth.getEntryBlock();
                SsaBasicBlock successorBlock = entryBlock.getPrimarySuccessor();
                SsaBasicBlock constBlock = entryBlock.insertNewSuccessor(successorBlock);
                constBlock.replaceLastInsn(new ThrowingCstInsn(constRop, SourcePosition.NO_INFO, RegisterSpecList.EMPTY, StdTypeList.EMPTY, (Constant)cst));
                SsaBasicBlock resultBlock = constBlock.insertNewSuccessor(successorBlock);
                PlainInsn insn = new PlainInsn(Rops.opMoveResultPseudo(result.getTypeBearer()), SourcePosition.NO_INFO, result, RegisterSpecList.EMPTY);
                resultBlock.addInsnToHead(insn);
            }
            newRegs.put(cst, result);
        }
        this.updateConstUses(newRegs, regSz);
    }

    private ArrayList<TypedConstant> getConstsSortedByCountUse() {
        int regSz = this.ssaMeth.getRegCount();
        final HashMap<TypedConstant, Integer> countUses = new HashMap<TypedConstant, Integer>();
        HashSet usedByLocal = new HashSet();
        for (int i = 0; i < regSz; ++i) {
            RegisterSpec result;
            TypeBearer typeBearer;
            SsaInsn insn = this.ssaMeth.getDefinitionForRegister(i);
            if (insn == null || insn.getOpcode() == null || !(typeBearer = (result = insn.getResult()).getTypeBearer()).isConstant()) continue;
            TypedConstant cst = (TypedConstant)typeBearer;
            if (insn.getOpcode().getOpcode() == 56) {
                int pred = insn.getBlock().getPredecessors().nextSetBit(0);
                ArrayList<SsaInsn> predInsns = this.ssaMeth.getBlocks().get(pred).getInsns();
                insn = predInsns.get(predInsns.size() - 1);
            }
            if (insn.canThrow()) {
                if (!(cst instanceof CstString)) continue;
                continue;
            }
            if (this.ssaMeth.isRegALocal(result)) continue;
            Integer has = (Integer)countUses.get(cst);
            if (has == null) {
                countUses.put(cst, 1);
                continue;
            }
            countUses.put(cst, has + 1);
        }
        ArrayList<TypedConstant> constantList = new ArrayList<TypedConstant>();
        for (Map.Entry entry : countUses.entrySet()) {
            if ((Integer)entry.getValue() <= 1) continue;
            constantList.add((TypedConstant)entry.getKey());
        }
        Collections.sort(constantList, new Comparator<Constant>(){

            @Override
            public int compare(Constant a, Constant b) {
                int ret = (Integer)countUses.get(b) - (Integer)countUses.get(a);
                if (ret == 0) {
                    ret = a.compareTo(b);
                }
                return ret;
            }

            @Override
            public boolean equals(Object obj) {
                return obj == this;
            }
        });
        return constantList;
    }

    private void fixLocalAssignment(RegisterSpec origReg, RegisterSpec newReg) {
        for (SsaInsn use : this.ssaMeth.getUseListForRegister(origReg.getReg())) {
            RegisterSpec localAssignment = use.getLocalAssignment();
            if (localAssignment == null || use.getResult() == null) continue;
            LocalItem local = localAssignment.getLocalItem();
            use.setResultLocal(null);
            newReg = newReg.withLocalItem(local);
            SsaInsn newInsn = SsaInsn.makeFromRop(new PlainInsn(Rops.opMarkLocal(newReg), SourcePosition.NO_INFO, null, RegisterSpecList.make(newReg)), use.getBlock());
            ArrayList<SsaInsn> insns = use.getBlock().getInsns();
            insns.add(insns.indexOf(use) + 1, newInsn);
        }
    }

    private void updateConstUses(HashMap<TypedConstant, RegisterSpec> newRegs, int origRegCount) {
        HashSet usedByLocal = new HashSet();
        ArrayList<SsaInsn>[] useList = this.ssaMeth.getUseListCopy();
        for (int i = 0; i < origRegCount; ++i) {
            TypedConstant cst;
            final RegisterSpec newReg;
            SsaInsn insn = this.ssaMeth.getDefinitionForRegister(i);
            if (insn == null) continue;
            final RegisterSpec origReg = insn.getResult();
            TypeBearer typeBearer = insn.getResult().getTypeBearer();
            if (!typeBearer.isConstant() || (newReg = newRegs.get(cst = (TypedConstant)typeBearer)) == null || this.ssaMeth.isRegALocal(origReg)) continue;
            RegisterMapper mapper = new RegisterMapper(){

                @Override
                public int getNewRegisterCount() {
                    return ConstCollector.this.ssaMeth.getRegCount();
                }

                @Override
                public RegisterSpec map(RegisterSpec registerSpec) {
                    if (registerSpec.getReg() == origReg.getReg()) {
                        return newReg.withLocalItem(registerSpec.getLocalItem());
                    }
                    return registerSpec;
                }
            };
            for (SsaInsn use : useList[origReg.getReg()]) {
                if (use.canThrow() && use.getBlock().getSuccessors().cardinality() > 1) continue;
                use.mapSourceRegisters(mapper);
            }
        }
    }
}

