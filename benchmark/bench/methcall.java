// $Id: methcall.java,v 1.1 2004-05-23 07:14:27 bfulgham Exp $
// http://www.bagley.org/~doug/shootout/
// Collection class code is from my friend Phil Chu, Thanks Phil!

import java.io.*;
import java.util.*;
import java.text.*;

class Toggle {
    boolean state = true;
    public Toggle(boolean start_state) {
        this.state = start_state;
    }
    public boolean value() {
        return(this.state);
    }
    public Toggle activate() {
        this.state = !this.state;
        return(this);
    }
}

class NthToggle extends Toggle {
    int count_max = 0;
    int counter = 0;

    public NthToggle(boolean start_state, int max_counter) {
        super(start_state);
        this.count_max = max_counter;
        this.counter = 0;
    }
    public Toggle activate() {
        this.counter += 1;
        if (this.counter >= this.count_max) {
            this.state = !this.state;
            this.counter = 0;
        }
        return(this);
    }
}

public class methcall {
    public static void main(String args[]) throws IOException {
        int n = Integer.parseInt(args[0]);

        boolean val = true;
        Toggle toggle = new Toggle(val);
        for (int i=0; i<n; i++) {
            val = toggle.activate().value();
        }
        System.out.println((val) ? "true" : "false");

        val = true;
        NthToggle ntoggle = new NthToggle(true, 3);
        for (int i=0; i<n; i++) {
            val = ntoggle.activate().value();
        }
        System.out.println((val) ? "true" : "false");
    }
}
