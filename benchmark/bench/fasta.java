/*
 * The Great Computer Language Shootout 
 * http://shootout.alioth.debian.org/
 * 
 * modified by Mehmet D. AKIN
 *
 */

import java.io.IOException;
import java.io.OutputStream;

class fasta {
    public static final int IM = 139968;
    public static final int IA = 3877;
    public static final int IC = 29573;
    public static int last = 42;

    public static final int LINE_LENGTH = 60;

    // pseudo-random number generator
    public static final double random(double max) {
        last = (last * IA + IC) % IM;
        return max * last / IM;
    }

    // Weighted selection from alphabet
    public static String ALU = 
              "GGCCGGGCGCGGTGGCTCACGCCTGTAATCCCAGCACTTTGG"
            + "GAGGCCGAGGCGGGCGGATCACCTGAGGTCAGGAGTTCGAGA"
            + "CCAGCCTGGCCAACATGGTGAAACCCCGTCTCTACTAAAAAT"
            + "ACAAAAATTAGCCGGGCGTGGTGGCGCGCGCCTGTAATCCCA"
            + "GCTACTCGGGAGGCTGAGGCAGGAGAATCGCTTGAACCCGGG"
            + "AGGCGGAGGTTGCAGTGAGCCGAGATCGCGCCACTGCACTCC"
            + "AGCCTGGGCGACAGAGCGAGACTCCGTCTCAAAAA";
    public static byte[] ALUB = ALU.getBytes(); 

    public static final frequency[] IUB = new frequency[] {
            new frequency('a', 0.27), 
            new frequency('c', 0.12),
            new frequency('g', 0.12), 
            new frequency('t', 0.27),
            
            new frequency('B', 0.02), 
            new frequency('D', 0.02),
            new frequency('H', 0.02), 
            new frequency('K', 0.02),
            new frequency('M', 0.02), 
            new frequency('N', 0.02),
            new frequency('R', 0.02), 
            new frequency('S', 0.02),
            new frequency('V', 0.02), 
            new frequency('W', 0.02),
            new frequency('Y', 0.02) };

    public static final frequency[] HomoSapiens = new frequency[] {
            new frequency('a', 0.3029549426680d),
            new frequency('c', 0.1979883004921d),
            new frequency('g', 0.1975473066391d),
            new frequency('t', 0.3015094502008d)};

    public static void makeCumulative(frequency[] a) {
        double cp = 0.0;
        for (int i = 0; i < a.length; i++) {
            cp += a[i].p;
            a[i].p = cp;
        }
    }

    // naive
    public final static byte selectRandom(frequency[] a) {
        int len = a.length;
        double r = random(1.0);
        for (int i = 0; i < len; i++)
            if (r < a[i].p)
                return a[i].c;
        return a[len - 1].c;
    }

    static int BUFFER_SIZE = 1024;
    static int index = 0;
    static byte[] bbuffer = new byte[BUFFER_SIZE];
    static final void makeRandomFasta(String id, String desc,frequency[] a, int n, OutputStream writer) throws IOException
    {
        index = 0;
        int m = 0;
        String descStr = ">" + id + " " + desc + '\n'; 
        writer.write(descStr.getBytes());
        while (n > 0) {
            if (n < LINE_LENGTH) m = n;  else m = LINE_LENGTH;
            if(BUFFER_SIZE - index < m){
                writer.write(bbuffer, 0, index);
                index = 0;
            }
            for (int i = 0; i < m; i++) {
                bbuffer[index++] = selectRandom(a);
            }
            bbuffer[index++] = '\n';
            n -= LINE_LENGTH;
        }
        if(index != 0) writer.write(bbuffer, 0, index);
    }    
    
    static final void makeRepeatFasta(String id, String desc, String alu, int n, OutputStream writer) throws IOException
    {
        index = 0;
        int m = 0;
        int k = 0;
        int kn = ALUB.length;
        String descStr = ">" + id + " " + desc + '\n'; 
        writer.write(descStr.getBytes());
        while (n > 0) {
            if (n < LINE_LENGTH) m = n; else m = LINE_LENGTH;
            if(BUFFER_SIZE - index < m){
                writer.write(bbuffer, 0, index);
                index = 0;
            }
            for (int i = 0; i < m; i++) {
                if (k == kn) k = 0;
                bbuffer[index++] = ALUB[k];
                k++;
            }
            bbuffer[index++] = '\n';
            n -= LINE_LENGTH;
        }
        if(index != 0) writer.write(bbuffer, 0, index);
    }
    
    public static void main(String[] args) throws IOException {
        makeCumulative(HomoSapiens);
        makeCumulative(IUB);
        int n = 2500000;
        if (args.length > 0)
            n = Integer.parseInt(args[0]);
        OutputStream out = System.out;
        makeRepeatFasta("ONE", "Homo sapiens alu", ALU, n * 2, out);
        makeRandomFasta("TWO", "IUB ambiguity codes", IUB, n * 3, out);
        makeRandomFasta("THREE", "Homo sapiens frequency", HomoSapiens, n * 5, out);
        out.close();
    }

    public static class frequency {
        public byte c;
        public double p;

        public frequency(char c, double p) {
            this.c = (byte)c;
            this.p = p;
        }
    }
}