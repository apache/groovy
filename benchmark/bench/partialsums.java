/* The Computer Language Shootout
   http://shootout.alioth.debian.org/
   contributed by Josh Goldfoot
   modified by Isaac Gouy
*/

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class partialsums {
   private static final double twothirds = 2.0/3.0;
   private static final NumberFormat formatter = new DecimalFormat("#0.000000000");


   public static void main(String[] args) {
      int n = Integer.parseInt(args[0]);

      double a1 = 0.0, a2 = 0.0, a3 = 0.0, a4 = 0.0, a5 = 0.0;
      double a6 = 0.0, a7 = 0.0, a8 = 0.0, a9 = 0.0, alt = -1.0;

      for (int k=1; k<=n; k++){
         double k2 = (double)k * (double)k, k3 = k2 * (double)k;
         double sk = Math.sin(k), ck = Math.cos(k);
         alt = -alt;

         a1 += Math.pow(twothirds,k-1);
         a2 += 1.0/Math.sqrt(k);
         a3 += 1.0/(k*(k+1.0));
         a4 += 1.0/(k3 * sk*sk);
         a5 += 1.0/(k3 * ck*ck);
         a6 += 1.0/k;
         a7 += 1.0/k2;
         a8 += alt/k;
         a9 += alt/(2.0*k -1.0);
      }
      System.out.println(formatter.format(a1) + "\t(2/3)^k");
      System.out.println(formatter.format(a2) + "\tk^-0.5");
      System.out.println(formatter.format(a3) + "\t1/k(k+1)");
      System.out.println(formatter.format(a4) + "\tFlint Hills");
      System.out.println(formatter.format(a5) + "\tCookson Hills");
      System.out.println(formatter.format(a6) + "\tHarmonic");
      System.out.println(formatter.format(a7) + "\tRiemann Zeta");
      System.out.println(formatter.format(a8) + "\tAlternating Harmonic");
      System.out.println(formatter.format(a9) + "\tGregory");
   }
}
