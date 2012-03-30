// $Id: matrix.java,v 1.1 2004-05-23 07:14:27 bfulgham Exp $
// http://www.bagley.org/~doug/shootout/
// modified to use a little less memory by Thomas Holenstein

import java.io.*;
import java.util.*;

public class matrix {
    static int SIZE = 30;

    public static void main(String args[]) {
        int n = Integer.parseInt(args[0]);
        int m1[][] = mkmatrix(SIZE, SIZE);
        int m2[][] = mkmatrix(SIZE, SIZE);
        int mm[][] = new int[SIZE][SIZE];
        for (int i=0; i<n; i++) {
            mmult(SIZE, SIZE, m1, m2, mm);
        }
        System.out.print(mm[0][0]);
        System.out.print(" ");
        System.out.print(mm[2][3]);
        System.out.print(" ");
        System.out.print(mm[3][2]);
        System.out.print(" ");
        System.out.println(mm[4][4]);
    }

    public static int[][] mkmatrix (int rows, int cols) {
        int count = 1;
        int m[][] = new int[rows][cols];
        for (int i=0; i<rows; i++) {
            for (int j=0; j<cols; j++) {
                m[i][j] = count++;
            }
        }
        return(m);
    }

    public static void mmult (int rows, int cols, 
                          int[][] m1, int[][] m2, int[][] m3) {
        for (int i=0; i<rows; i++) {
            for (int j=0; j<cols; j++) {
                int val = 0;
                for (int k=0; k<cols; k++) {
                    val += m1[i][k] * m2[k][j];
                }
                m3[i][j] = val;
            }
        }
    }
}
