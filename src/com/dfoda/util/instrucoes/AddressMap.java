package com.dfoda.util.instrucoes;

import java.util.HashMap;

public final class AddressMap {
    public final HashMap<Integer, Integer> map = new HashMap();

    public int get(int keyAddress) {
        Integer value = this.map.get(keyAddress);
        return value == null ? -1 : value;
    }

    public void put(int keyAddress, int valueAddress) {
        this.map.put(keyAddress, valueAddress);
    }
}

