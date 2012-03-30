// $Id: except.java,v 1.1 2004-05-23 04:36:29 bfulgham Exp $
// http://www.bagley.org/~doug/shootout/
// Collection class code is from my friend Phil Chu, Thanks Phil!

import java.io.*;
import java.util.*;
import java.text.*;

class Lo_Exception extends Exception {
    int num = 0;
    public Lo_Exception(int num) {
        this.num = num;
    }
    public String toString() {
        return "Lo_Exception, num = " + this.num;
    }
}

class Hi_Exception extends Exception {
    int num = 0;
    public Hi_Exception(int num) {
        this.num = num;
    }
    public String toString() {
        return "Hi_Exception, num = " + this.num;
    }
}

public class except {
    static int Lo = 0;
    static int Hi = 0;

    public static void main(String args[]) throws IOException {
        int n = Integer.parseInt(args[0]);

        for (int i=0; i<n; i++) {
            some_function(i);
        }
        System.out.println("Exceptions: HI=" + Hi + " / LO=" + Lo);
    }

    public static void some_function(int n) {
        try {
            hi_function(n);
        } catch (Exception e) {
            System.out.println("We shouldn't get here: " + e);
        }
    }

    public static void hi_function(int n) throws Hi_Exception, Lo_Exception {
        try {
            lo_function(n);
        } catch (Hi_Exception e) {
            Hi++;
        }
    }

    public static void lo_function(int n) throws Hi_Exception, Lo_Exception {
        try {
            blowup(n);
        } catch (Lo_Exception e) {
            Lo++;
        }
    }

    public static void blowup(int n) throws Hi_Exception, Lo_Exception {
        if ((n % 2) == 0) {
            throw new Lo_Exception(n);
        } else {
            throw new Hi_Exception(n);
        }
    }
}
