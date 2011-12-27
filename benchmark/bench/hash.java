// $Id: hash.java,v 1.1 2004-05-23 05:06:51 bfulgham Exp $
// http://www.bagley.org/~doug/shootout/

// this program is modified from:
//   http://cm.bell-labs.com/cm/cs/who/bwk/interps/pap.html
// Timing Trials, or, the Trials of Timing: Experiments with Scripting
// and User-Interface Languages</a> by Brian W. Kernighan and
// Christopher J. Van Wyk.

import java.io.*;
import java.util.*;

public class hash {

    public static void main(String args[]) throws IOException {
        int n = Integer.parseInt(args[0]);
        int i, c;
        String s = "";
        Integer ii;
        // the original program used:
        // Hashtable ht = new Hashtable();
        // John Olsson points out that Hashtable is for synchronized access
        // and we should use instead:
        HashMap ht = new HashMap();

        c = 0;
        for (i = 1; i <= n; i++)
            ht.put(Integer.toString(i, 16), new Integer(i));
        for (i = 1; i <= n; i++)
            // The original code converted to decimal string this way:
            // if (ht.containsKey(i+""))
            if (ht.containsKey(Integer.toString(i, 10)))
                c++;

        System.out.println(c);
    }
}

