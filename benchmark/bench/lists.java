// $Id: lists.java,v 1.1 2004-05-23 07:12:55 bfulgham Exp $
// http://www.bagley.org/~doug/shootout/

import java.io.*;
import java.util.*;
import java.text.*;

public class lists {
    static int SIZE = 10000;

    public static void main(String args[]) {
        int n = Integer.parseInt(args[0]);
        int result = 0;
        for (int i = 0; i < n; i++) {
            result = test_lists();
        }
        System.out.println(result);
    }
    public static int test_lists() {
        int result = 0;
        // create a list of integers (Li1) from 1 to SIZE
        LinkedList Li1 = new LinkedList();
        for (int i = 1; i < SIZE+1; i++) {
            Li1.addLast(new Integer(i));
        }
        // copy the list to Li2 (not by individual items)
        LinkedList Li2 = new LinkedList(Li1);
        LinkedList Li3 = new LinkedList();
        // remove each individual item from left side of Li2 and
        // append to right side of Li3 (preserving order)
        while (! Li2.isEmpty()) {
            Li3.addLast(Li2.removeFirst());
        }
        // Li2 must now be empty
        // remove each individual item from right side of Li3 and
        // append to right side of Li2 (reversing list)
        while (! Li3.isEmpty()) {
            Li2.addLast(Li3.removeLast());
        }
        // Li3 must now be empty
        // reverse Li1
        LinkedList tmp = new LinkedList();
        while (! Li1.isEmpty()) {
            tmp.addFirst(Li1.removeFirst());
        }
        Li1 = tmp;
        // check that first item is now SIZE
        if (((Integer)Li1.getFirst()).intValue() != SIZE) {
            System.err.println("first item of Li1 != SIZE");
            return(0);
        }
        // compare Li1 and Li2 for equality
        if (! Li1.equals(Li2)) {
            System.err.println("Li1 and Li2 differ");
            System.err.println("Li1:" + Li1);
            System.err.println("Li2:" + Li2);
            return(0);
        }
        // return the length of the list
        return(Li1.size());
    }
}
