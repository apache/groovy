// $Id: sieve.java,v 1.1 2004-05-23 07:14:28 bfulgham Exp $
// http://www.bagley.org/~doug/shootout/

public class sieve {
    public static void main(String args[]) {
        int NUM = Integer.parseInt(args[0]);
        boolean [] flags = new boolean[8192 + 1];
        int count = 0;
        while (NUM-- > 0) {
            count = 0;
            for (int i=2; i <= 8192; i++) {
                flags[i] = true;
            }
            for (int i=2; i <= 8192; i++) {
                if (flags[i]) {
                    // remove all multiples of prime: i
                    for (int k=i+i; k <= 8192; k+=i) {
                        flags[k] = false;
                    }
                    count++;
                }
            }
        }
        System.out.print("Count: " + count + "\n");
    }
}

