package com.dfoda.otimizadores.ssa;

import com.dfoda.util.BitIntSet;
import com.dfoda.util.ListIntSet;
import com.dfoda.util.IntSet;

public final class SetFactory {
    private static final int DOMFRONT_SET_THRESHOLD_SIZE = 3072;
    private static final int INTERFERENCE_SET_THRESHOLD_SIZE = 3072;
    private static final int LIVENESS_SET_THRESHOLD_SIZE = 3072;

    static IntSet makeDomFrontSet(int szBlocks) {
        return szBlocks <= 3072 ? new BitIntSet(szBlocks) : new ListIntSet();
    }

    public static IntSet makeInterferenceSet(int countRegs) {
        return countRegs <= 3072 ? new BitIntSet(countRegs) : new ListIntSet();
    }

    static IntSet makeLivenessSet(int countRegs) {
        return countRegs <= 3072 ? new BitIntSet(countRegs) : new ListIntSet();
    }
}

