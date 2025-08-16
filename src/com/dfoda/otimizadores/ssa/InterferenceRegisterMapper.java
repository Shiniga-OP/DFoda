package com.dfoda.otimizadores.ssa;

import java.util.ArrayList;
import com.dfoda.util.BitIntSet;
import com.dfoda.otimizadores.ssa.atras.InterferenceGraph;
import com.dfoda.util.IntSet;
import com.dfoda.otimizadores.rop.codigo.RegisterSpec;
import com.dfoda.otimizadores.rop.codigo.RegisterSpecList;

public class InterferenceRegisterMapper extends BasicRegisterMapper {
    private final ArrayList<BitIntSet> newRegInterference = new ArrayList();
    private final InterferenceGraph oldRegInterference;

    public InterferenceRegisterMapper(InterferenceGraph oldRegInterference, int countOldRegisters) {
        super(countOldRegisters);
        this.oldRegInterference = oldRegInterference;
    }

    @Override
    public void addMapping(int oldReg, int newReg, int category) {
        super.addMapping(oldReg, newReg, category);
        this.addInterfence(newReg, oldReg);
        if (category == 2) {
            this.addInterfence(newReg + 1, oldReg);
        }
    }

    public boolean interferes(int oldReg, int newReg, int category) {
        if (newReg >= this.newRegInterference.size()) {
            return false;
        }
        IntSet existing = this.newRegInterference.get(newReg);
        if (existing == null) {
            return false;
        }
        if (category == 1) {
            return existing.has(oldReg);
        }
        return existing.has(oldReg) || this.interferes(oldReg, newReg + 1, category - 1);
    }

    public boolean interferes(RegisterSpec oldSpec, int newReg) {
        return this.interferes(oldSpec.getReg(), newReg, oldSpec.getCategory());
    }

    private void addInterfence(int newReg, int oldReg) {
        this.newRegInterference.ensureCapacity(newReg + 1);
        while (newReg >= this.newRegInterference.size()) {
            this.newRegInterference.add(new BitIntSet(newReg + 1));
        }
        this.oldRegInterference.mergeInterferenceSet(oldReg, this.newRegInterference.get(newReg));
    }

    public boolean areAnyPinned(RegisterSpecList oldSpecs, int newReg, int targetCategory) {
        int sz = oldSpecs.size();
        for (int i = 0; i < sz; ++i) {
            RegisterSpec oldSpec = oldSpecs.get(i);
            int r = this.oldToNew(oldSpec.getReg());
            if (r != newReg && (oldSpec.getCategory() != 2 || r + 1 != newReg) && (targetCategory != 2 || r != newReg + 1)) continue;
            return true;
        }
        return false;
    }
}

