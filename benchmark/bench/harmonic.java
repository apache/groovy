/* The Great Computer Language Shootout
   http://shootout.alioth.debian.org/
 
   contributed by Paul Lofte
*/

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class harmonic {

    static final NumberFormat formatter = new DecimalFormat("#.000000000");
    
    public static void main(String[] args) {
        int n = 10000000;
        if (args.length > 0) n = Integer.parseInt(args[0]);

        double partialSum = 0.0;
        for (int i=1; i<=n; i++) partialSum += 1.0/i;
        
        System.out.println(formatter.format(partialSum));
    }
}
