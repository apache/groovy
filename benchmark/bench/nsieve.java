/* The Great Computer Language Shootout
   http://shootout.alioth.debian.org/

   contributed by Alexei Svitkine
*/

public class nsieve
{
   static int nsieve(int m, boolean[] isPrime)
   {
      for (int i=2; i <= m; i++) isPrime[i] = true;
      int count = 0;

      for (int i=2; i <= m; i++) {
         if (isPrime[i]) {
            for (int k=i+i; k <= m; k+=i) isPrime[k] = false;
            count++;
         }
      }
      return count;
   }

   public static String padNumber(int number, int fieldLen)
   {
      StringBuffer sb = new StringBuffer();
      String bareNumber = "" + number;
      int numSpaces = fieldLen - bareNumber.length();

      for (int i = 0; i < numSpaces; i++)
         sb.append(" ");

      sb.append(bareNumber);

      return sb.toString();
   }

   public static void main(String[] args)
   {
      int n = 2;
      if (args.length > 0) n = Integer.parseInt(args[0]);
      if (n < 2) n = 2;

      int m = (1<<n)*10000;
      boolean[] flags = new boolean[m+1];

      System.out.println("Primes up to " + padNumber(m, 8) + " " + padNumber(nsieve(m,flags), 8));
      m = (1<<n-1)*10000;
      System.out.println("Primes up to " + padNumber(m, 8) + " " + padNumber(nsieve(m,flags), 8));
      m = (1<<n-2)*10000;
      System.out.println("Primes up to " + padNumber(m, 8) + " " + padNumber(nsieve(m,flags), 8));
   }
}
