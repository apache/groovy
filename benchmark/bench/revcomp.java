/*
 * The Computer Language Benchmarks Game
 * http://shootout.alioth.debian.org/
 *
 * contributed by Anthony Donnefort
 * redone by Enotus 2010-11-20
 */

import java.io.*;

public final class revcomp {

    static final byte[] map = new byte[128];

    static {
        String[] mm = {"ACBDGHK\nMNSRUTWVYacbdghkmnsrutwvy", "TGVHCDM\nKNSYAAWBRTGVHCDMKNSYAAWBR"};
        for (int i = 0; i < mm[0].length(); i++)
            map[mm[0].charAt(i)] = (byte) mm[1].charAt(i);
    } 

    static void reverse(byte[] buf, int begin, int end) {
        while (true) {
            byte bb = buf[begin];
            if (bb == '\n') bb = buf[++begin];
            byte be = buf[end];
            if (be == '\n') be = buf[--end];
            if (begin > end) break;
            buf[begin++] = map[be];
            buf[end--] = map[bb];
        }
    }

    public static void main(String[] args) throws IOException {
        final byte[] buf = new byte[System.in.available()];
        System.in.read(buf);

        for (int i = 0; i < buf.length;) {
            while (buf[i++] != '\n');
            int data = i;
            while (i < buf.length && buf[i++] != '>');
            reverse(buf, data, i-2);
        }

        System.out.write(buf);
    }
}
