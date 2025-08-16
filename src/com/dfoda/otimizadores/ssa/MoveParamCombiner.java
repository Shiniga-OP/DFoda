package com.dfoda.otimizadores.ssa;

import java.util.HashSet;
import java.util.List;
import com.dfoda.otimizadores.rop.codigo.RegisterSpec;
import com.dfoda.otimizadores.rop.codigo.LocalItem;
import com.dfoda.otimizadores.rop.codigo.CstInsn;
import com.dfoda.otimizadores.rop.cst.CstInteger;

public class MoveParamCombiner {
    private final SsaMethod ssaMeth;

    public static void process(SsaMethod ssaMethod) {
        new MoveParamCombiner(ssaMethod).run();
    }

    private MoveParamCombiner(SsaMethod ssaMeth) {
        this.ssaMeth = ssaMeth;
    }

    private void run() {
        final RegisterSpec[] paramSpecs = new RegisterSpec[this.ssaMeth.getParamWidth()];
        final HashSet<SsaInsn> deletedInsns = new HashSet<SsaInsn>();
        this.ssaMeth.forEachInsn(new SsaInsn.Visitor(){

            @Override
            public void visitMoveInsn(NormalSsaInsn insn) {
            }

            @Override
            public void visitPhiInsn(PhiInsn phi) {
            }

            @Override
            public void visitNonMoveInsn(NormalSsaInsn insn) {
                if (insn.getOpcode().getOpcode() != 3) {
                    return;
                }
                int param = MoveParamCombiner.this.getParamIndex(insn);
                if (paramSpecs[param] == null) {
                    paramSpecs[param] = insn.getResult();
                } else {
                    LocalItem newLocal;
                    final RegisterSpec specA = paramSpecs[param];
                    final RegisterSpec specB = insn.getResult();
                    LocalItem localA = specA.getLocalItem();
                    LocalItem localB = specB.getLocalItem();
                    if (localA == null) {
                        newLocal = localB;
                    } else if (localB == null) {
                        newLocal = localA;
                    } else if (localA.equals(localB)) {
                        newLocal = localA;
                    } else {
                        return;
                    }
                    MoveParamCombiner.this.ssaMeth.getDefinitionForRegister(specA.getReg()).setResultLocal(newLocal);
                    RegisterMapper mapper = new RegisterMapper(){

                        @Override
                        public int getNewRegisterCount() {
                            return MoveParamCombiner.this.ssaMeth.getRegCount();
                        }

                        @Override
                        public RegisterSpec map(RegisterSpec registerSpec) {
                            if (registerSpec.getReg() == specB.getReg()) {
                                return specA;
                            }
                            return registerSpec;
                        }
                    };
                    List<SsaInsn> uses = MoveParamCombiner.this.ssaMeth.getUseListForRegister(specB.getReg());
                    for (int i = uses.size() - 1; i >= 0; --i) {
                        SsaInsn use = uses.get(i);
                        use.mapSourceRegisters(mapper);
                    }
                    deletedInsns.add(insn);
                }
            }
        });
        this.ssaMeth.deleteInsns(deletedInsns);
    }

    private int getParamIndex(NormalSsaInsn insn) {
        CstInsn cstInsn = (CstInsn)insn.getOriginalRopInsn();
        int param = ((CstInteger)cstInsn.getConstant()).getValue();
        return param;
    }
}

