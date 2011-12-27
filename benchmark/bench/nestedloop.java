// $Id: nestedloop.java,v 1.1 2004-05-23 07:14:28 bfulgham Exp $
// http://www.bagley.org/~doug/shootout/

import java.io.*;
import java.util.*;

public class nestedloop {
    public static void main(String args[]) throws IOException {
        int n = Integer.parseInt(args[0]);
        int x = 0;
        for (int a=0; a<n; a++)
            for (int b=0; b<n; b++)
                for (int c=0; c<n; c++)
                    for (int d=0; d<n; d++)
                        for (int e=0; e<n; e++)
                            for (int f=0; f<n; f++)
                                x++;
        System.out.println(x);
    }
}
