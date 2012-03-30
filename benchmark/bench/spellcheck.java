// $Id: spellcheck.java,v 1.1 2004-05-23 07:14:28 bfulgham Exp $
// http://www.bagley.org/~doug/shootout/

import java.io.*;
import java.util.*;

public class spellcheck {
    public static void main(String args[]) throws IOException {
        int n = Integer.parseInt(args[0]);
        HashMap dict = new HashMap();
        String word;

        try {
            BufferedReader in = new BufferedReader(new FileReader("Usr.Dict.Words"));
            while ((word = in.readLine()) != null) {
                dict.put(word, new Integer(1));
            }
            in.close();
        } catch (IOException e) {
            System.err.println(e);
            return;
        }

        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            while ((word = in.readLine()) != null) {
                if (!dict.containsKey(word)) {
                    System.out.println(word);
                }
            }
        } catch (IOException e) {
            System.err.println(e);
            return;
        }
    }
}
