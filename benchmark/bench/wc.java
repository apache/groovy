// $Id: wc.java,v 1.2 2004-06-20 08:39:47 bfulgham Exp $
// http://www.bagley.org/~doug/shootout/
// with help from Dirus@programmer.net

import java.io.*;
import java.util.*;
import java.text.*;

// this program modified from:
//   http://cm.bell-labs.com/cm/cs/who/bwk/interps/pap.html
// Timing Trials, or, the Trials of Timing: Experiments with Scripting
// and User-Interface Languages</a> by Brian W. Kernighan and
// Christopher J. Van Wyk.

public class wc {
    public static void main(String[] args) {
        int nl = 0, nw = 0, nc = 0;

        try {
            byte[] buff = new byte[4096];
            boolean inword = false;
            int length;
            char c;

            while ((length = System.in.read(buff)) != -1) {
                nc += length;
                for(int i = 0; i < length; i++) {
                    c = (char)buff[i];
                    if (c == '\n')
                        ++nl;
                    if (Character.isWhitespace(c))
                        inword = false;
                    else if (inword == false) {
                        ++nw;
                        inword = true;
                    }
                }
            }
        } catch (IOException e) {
            System.err.println(e);
            return;
        }
        System.out.println(Integer.toString(nl) + " " +
                           Integer.toString(nw) + " " +
                           Integer.toString(nc));
    }
}
