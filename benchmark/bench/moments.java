// $Id: moments.java,v 1.1 2004-11-23 08:08:44 bfulgham Exp $
// http://www.bagley.org/~doug/shootout/

import java.io.*;
import java.util.*;
import java.text.*;
import java.lang.Math;

public class moments {
    public static void main(String[] args) {
    String line;
    Vector nums = new Vector();
    double num, sum = 0.0;
    double mean = 0.0;
    double average_deviation = 0.0;
    double standard_deviation = 0.0;
    double variance = 0.0;
    double skew = 0.0;
    double kurtosis = 0.0;
    double median = 0.0;
    double deviation = 0.0;
    int i, n, mid = 0;

        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            while ((line = in.readLine()) != null) {
        num = Double.parseDouble(line);
        sum += num;
        nums.add(new Double(num));
            }
        } catch (IOException e) {
            System.err.println(e);
            return;
        }

    n = nums.size();
    mean = sum/n;
    for (i=0; i<n; i++) {
        deviation = ((Double)nums.get(i)).doubleValue() - mean;
        average_deviation += Math.abs(deviation);
        variance += Math.pow(deviation,2);
        skew += Math.pow(deviation,3);
        kurtosis += Math.pow(deviation,4);
    }
    average_deviation /= n;
    variance /= (n - 1);
    standard_deviation = Math.sqrt(variance);
    if (variance != 0.0) {
        skew /= (n * variance * standard_deviation);
        kurtosis = kurtosis/(n * variance * variance) - 3.0;
    }
    
    Collections.sort(nums);

    mid = (n/2);
    median = (n % 2 != 0) ?
        ((Double)nums.get(mid)).doubleValue() :
        (((Double)nums.get(mid)).doubleValue() +
         ((Double)nums.get(mid-1)).doubleValue())/2;
    
    NumberFormat nf = NumberFormat.getInstance();
    nf.setMaximumFractionDigits(13);
    nf.setGroupingUsed(false);
    nf.setMaximumFractionDigits(6);
    nf.setMinimumFractionDigits(6);

    System.out.println("n:                  " + n);
    System.out.println("median:             " + nf.format(median));
    System.out.println("mean:               " + nf.format(mean));
    System.out.println("average_deviation:  " + nf.format(average_deviation));
    System.out.println("standard_deviation: " + nf.format(standard_deviation));
    System.out.println("variance:           " + nf.format(variance));
    System.out.println("skew:               " + nf.format(skew));
    System.out.println("kurtosis:           " + nf.format(kurtosis));
    }
}

