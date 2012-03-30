/*
 * The Computer Language Benchmarks Game
 * http://shootout.alioth.debian.org/
 *
 * contributed by James Durbin 
 * slightly modified from Java version from
 * Anthony Donnefort and Razii
 */

import java.io.*

class ReversibleByteArray extends java.io.ByteArrayOutputStream {

    static final byte[] cmp = new byte[128]

    static {
        for (int i = 0; i < cmp.length; i++) cmp[i] = (byte) i

        cmp[(byte)'t'] = cmp[(byte)'T'] = (byte)'A'
        cmp[(byte)'a'] = cmp[(byte)'A'] = (byte)'T'
        cmp[(byte)'g'] = cmp[(byte)'G'] = (byte)'C'
        cmp[(byte)'c'] = cmp[(byte)'C'] = (byte)'G'
        cmp[(byte)'v'] = cmp[(byte)'V'] = (byte)'B'
        cmp[(byte)'h'] = cmp[(byte)'H'] = (byte)'D'
        cmp[(byte)'r'] = cmp[(byte)'R'] = (byte)'Y'
        cmp[(byte)'m'] = cmp[(byte)'M'] = (byte)'K'
        cmp[(byte)'y'] = cmp[(byte)'Y'] = (byte)'R'
        cmp[(byte)'k'] = cmp[(byte)'K'] = (byte)'M'
        cmp[(byte)'b'] = cmp[(byte)'B'] = (byte)'V'
        cmp[(byte)'d'] = cmp[(byte)'D'] = (byte)'H'
        cmp[(byte)'u'] = cmp[(byte)'U'] = (byte)'A'
    }

    void reverse() throws Exception {
        if (count > 0) {
            int begin = 0, end = count - 1
            while (buf[begin++] != '\n')
                while (begin <= end) {
                    if (buf[begin] == '\n') begin++
                    if (buf[end] == '\n') end--
                    if (begin <= end) {
                        byte tmp = buf[begin]
                        buf[begin++] = cmp[buf[end]]
                        buf[end--] = cmp[tmp]
                    }
                }
            System.out.write(buf, 0, count)
        }
    }
}

byte[] line = new byte[82]
int read
ReversibleByteArray buf = new ReversibleByteArray()
while ((read = System.in.read(line)) != -1) {
    int i = 0, last = 0
    while (i < read) {
        if (line[i] == '>') {
            buf.write(line, last, i - last)
            buf.reverse()
            buf.reset()
            last = i
        }
        i++
    }
    buf.write(line, last, read - last)
}
buf.reverse()
