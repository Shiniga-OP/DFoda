package com.dfoda.util;

public final class Hex {
    public static String u8(long v) {
        char[] res = new char[16];
        for(int i = 0; i < 16; ++i) {
            res[15 - i] = Character.forDigit((int)v & 0xF, 16);
            v >>= 4;
        }
        return new String(res);
    }

    public static String u4(int v) {
        char[] res = new char[8];
        for(int i = 0; i < 8; ++i) {
            res[7 - i] = Character.forDigit(v & 0xF, 16);
            v >>= 4;
        }
        return new String(res);
    }

    public static String u3(int v) {
        char[] res = new char[6];
        for(int i = 0; i < 6; ++i) {
            res[5 - i] = Character.forDigit(v & 0xF, 16);
            v >>= 4;
        }
        return new String(res);
    }

    public static String u2(int v) {
        char[] res = new char[4];
        for(int i = 0; i < 4; ++i) {
            res[3 - i] = Character.forDigit(v & 0xF, 16);
            v >>= 4;
        }
        return new String(res);
    }

    public static String u2or4(int v) {
        if(v == (char)v) return u2(v);
        return u4(v);
    }

    public static String u1(int v) {
        char[] res = new char[2];
        for(int i = 0; i < 2; ++i) {
            res[1 - i] = Character.forDigit(v & 0xF, 16);
            v >>= 4;
        }
        return new String(res);
    }

    public static String uNibble(int v) {
        char[] res = new char[]{Character.forDigit(v & 0xF, 16)};
        return new String(res);
    }

    public static String s8(long v) {
        char[] res = new char[17];
        if(v < 0L) {
            res[0] = 45;
            v = -v;
        } else res[0] = 43;
        for(int i = 0; i < 16; ++i) {
            res[16 - i] = Character.forDigit((int)v & 0xF, 16);
            v >>= 4;
        }
        return new String(res);
    }

    public static String s4(int v) {
        char[] res = new char[9];
        if(v < 0) {
            res[0] = 45;
            v = -v;
        } else res[0] = 43;
        for(int i = 0; i < 8; ++i) {
            res[8 - i] = Character.forDigit(v & 0xF, 16);
            v >>= 4;
        }
        return new String(res);
    }

    public static String s2(int v) {
        char[] res = new char[5];
        if(v < 0) {
            res[0] = 45;
            v = -v;
        } else res[0] = 43;
        for(int i = 0; i < 4; ++i) {
            res[4 - i] = Character.forDigit(v & 0xF, 16);
            v >>= 4;
        }
        return new String(res);
    }

    public static String s1(int v) {
        char[] res = new char[3];
        if(v < 0) {
            res[0] = 45;
            v = -v;
        } else res[0] = 43;
        for(int i = 0; i < 2; ++i) {
            res[2 - i] = Character.forDigit(v & 0xF, 16);
            v >>= 4;
        }
        return new String(res);
    }

    public static String mostrar(byte[] arr, int antes, int tam, int saidaAntes, int bpl, int enderecoTam) {
        int fim = antes + tam;
        if((antes | tam | fim) < 0 || fim > arr.length) throw new IndexOutOfBoundsException("arr.length " + arr.length + "; " + antes + "..!" + fim);
        if(saidaAntes < 0) throw new IllegalArgumentException("saidaAntes < 0");
        if(tam == 0) return "";
        StringBuilder sb = new StringBuilder(tam * 4 + 6);
        int c = 0;
        while(tam > 0) {
            if(c == 0) {
                String astr;
                switch(enderecoTam) {
                    case 2: 
                        astr = u1(saidaAntes);
                    break;   
                    case 4:
                        astr = u2(saidaAntes);
                    break;
                    case 6:
                        astr = u3(saidaAntes);
                     break;
                    default:
                        astr = u4(saidaAntes);
					break;
                }
                sb.append(astr);
                sb.append(": ");
			} else if(c % 2 == 0) {
                sb.append(' ');
            }
            sb.append(u1(arr[antes]));
            ++saidaAntes;
            ++antes;
            if(++c == bpl) {
                sb.append('\n');
                c = 0;
            }
            --tam;
        }
        if(c != 0) sb.append('\n');
        return sb.toString();
    }
}
