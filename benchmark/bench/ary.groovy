// $Id: ary.java,v 1.1 2004-05-22 07:27:00 bfulgham Exp $
// http://www.bagley.org/~doug/shootout/

// this program is modified from:
//   http://cm.bell-labs.com/cm/cs/who/bwk/interps/pap.html
// Timing Trials, or, the Trials of Timing: Experiments with Scripting
// and User-Interface Languages</a> by Brian W. Kernighan and
// Christopher J. Van Wyk.


public class ary {
    public static void main(String[] args) {
        int i, j, k, n = Integer.parseInt(args[0]);
        int[] x = new int[n];
        int[] y = new int[n];

        for (i = 0; i < n; i++)
            x[i] = i + 1;
        for (k = 0; k < 1000; k++ )
            for (j = n-1; j >= 0; j--)
                y[j] += x[j];

        System.out.println(y[0] + " " + y[n-1]);
    }
}
