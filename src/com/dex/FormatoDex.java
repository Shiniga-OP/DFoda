package com.dex;

public final class FormatoDex {
    public static final String API_28 = "039";
    public static final String API_26 = "038";
    public static final String API_24 = "037";
    public static final String API_13 = "035";
    public static final String MAGIC_PREFIXO = "dex\n";
    public static final String MAGIC_SUFIXO = "\u0000";
	
    public static int magicPraAPI(byte[] magic) {
        if(magic.length != 8) return -1;
        if(magic[0] != 100 || magic[1] != 101 || magic[2] != 120 || magic[3] != 10 || magic[7] != 0) return -1;
        String versao = "" + (char)magic[4] + (char)magic[5] + (char)magic[6];
        if(versao.equals(API_13)) return 13;
        if(versao.equals(API_24)) return 24;
        if (versao.equals(API_26)) return 26;
        if(versao.equals("039")) return 28;
        if(versao.equals("039")) return 28;
        return -1;
    }

    public static String APIPraMagic(int API_ESPERADA) {
        String versao = API_ESPERADA >= 28 ? "039" : (API_ESPERADA >= 28 ? "039" : (API_ESPERADA >= 26 ? API_26 : (API_ESPERADA >= 24 ? API_24 : API_13)));
        return MAGIC_PREFIXO + versao + MAGIC_SUFIXO;
    }

    public static boolean eSuportadoMagic(byte[] magic) {
        int api = magicPraAPI(magic);
        return api > 0;
    }
}

