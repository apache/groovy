
/* The Computer Language Shootout
   http://shootout.alioth.debian.org/
   contributed by Josh Goldfoot
   based on the Nice entry by Isaac Guoy
*/

import java.io.*;
import java.lang.*;
import java.util.regex.*;

public class regexdna {
    
    public regexdna() {
    }

    public static void main(String[] args) {
        BufferedReader r = new BufferedReader(new InputStreamReader(System.in));
        StringBuffer sb = new StringBuffer(10240);
        char[] cbuf = new char[10240];
        int charsRead = 0;
        try {
            while ((charsRead = r.read(cbuf, 0, 10240)) != -1) 
                sb.append(cbuf, 0, charsRead);
        } catch (java.io.IOException e) {
            return;
        }
        String sequence = sb.toString();
        
        int initialLength = sequence.length();
        sequence = Pattern.compile(">.*\n|\n").matcher(sequence).replaceAll("");
        int codeLength = sequence.length();
        
        String[] variants = { "agggtaaa|tttaccct" ,"[cgt]gggtaaa|tttaccc[acg]", "a[act]ggtaaa|tttacc[agt]t", 
                 "ag[act]gtaaa|tttac[agt]ct", "agg[act]taaa|ttta[agt]cct", "aggg[acg]aaa|ttt[cgt]ccct",                     
                 "agggt[cgt]aa|tt[acg]accct", "agggta[cgt]a|t[acg]taccct", "agggtaa[cgt]|[acg]ttaccct" };
        for (int i = 0; i < variants.length; i++) {
            int count = 0;
            Matcher m = Pattern.compile(variants[i]).matcher(sequence);
            while (m.find())
                count++;
            System.out.println(variants[i] + " " + count);
        }
        
        sequence = Pattern.compile("B").matcher(sequence).replaceAll("(c|g|t)");
        sequence = Pattern.compile("D").matcher(sequence).replaceAll("(a|g|t)");
        sequence = Pattern.compile("H").matcher(sequence).replaceAll("(a|c|t)");
        sequence = Pattern.compile("K").matcher(sequence).replaceAll("(g|t)");
        sequence = Pattern.compile("M").matcher(sequence).replaceAll("(a|c)");
        sequence = Pattern.compile("N").matcher(sequence).replaceAll("(a|c|g|t)");
        sequence = Pattern.compile("R").matcher(sequence).replaceAll("(a|g)");
        sequence = Pattern.compile("S").matcher(sequence).replaceAll("(c|g)");
        sequence = Pattern.compile("V").matcher(sequence).replaceAll("(a|c|g)");
        sequence = Pattern.compile("W").matcher(sequence).replaceAll("(a|t)");
        sequence = Pattern.compile("Y").matcher(sequence).replaceAll("(c|t)");
        
        System.out.println();
        System.out.println(initialLength);
        System.out.println(codeLength);
        System.out.println(sequence.length());
    }
}
