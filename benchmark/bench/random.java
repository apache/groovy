// $Id: random.java,v 1.2 2004-08-14 08:19:19 bfulgham Exp $
// http://shootout.alioth.debian.org/
//
// Brent Fulgham:  Changed to use 32-bit integers (like the C
// version), based on a suggestion by Yonik Seeley.

import java.text.*;

public class random {

    public static final int IM = 139968;
    public static final int IA = 3877;
    public static final int IC = 29573;

    public static void main(String args[]) {
        int N = Integer.parseInt(args[0]) - 1;
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(9);
        nf.setMinimumFractionDigits(9);
        nf.setGroupingUsed(false);

        while (N-- > 0) {
            gen_random(100);
        }
        System.out.println(nf.format(gen_random(100)));
    }

    public static int last = 42;
    public static double gen_random(double max) {
        return( max * (last = (last * IA + IC) % IM) / IM );
    }
}
