/*
* The Computer Language Benchmarks Game
* http://shootout.alioth.debian.org/
*
* Based on contribution of Eckehard Berns
* Based on code by Heiner Marxen
* and the ATS version by Hongwei Xi
* convert to Java by The Anh Tran
*/

import java.util.concurrent.atomic.AtomicInteger;

public final class fannkuch implements Runnable
{
    private final int n;
    private final int[] flip_max_arr;
    private final AtomicInteger remain_task = new AtomicInteger(0);
    
    public static void main(String[] args)
    {
        int x = (args.length > 0) ? Integer.parseInt(args[0]) : 7;
        fannkuch f = new fannkuch(x);
        System.out.format("Pfannkuchen(%d) = %d\n", x, f.fank_game());
    }
    
    public fannkuch(int N)
    {
        n = N;
        // hold flip_count result for each swap index
        flip_max_arr = new int[n];
    }
    
    private final int fank_game()
    {
        Thread[] th = new Thread[Runtime.getRuntime().availableProcessors()];
        for (int i = 0; i < th.length; i++)
        {
            th[i] = new Thread(this);
            th[i].start();
        }
        
        print_30_permut();
        
        for (Thread t : th)
        {
            try {
                t.join();
            }
            catch (InterruptedException ie)
            {   }
        }
        
        int mx = 0;
        for (int i : flip_max_arr)
            if (mx < i)
                mx = i;
        return mx;
    }
    
    // In order to divide tasks 'equally' for many threads, permut generation
    // strategy is different than that of original single thread.
    // this function will 'correctly' print first 30 permutations
    private final void print_30_permut()
    {
        // declare and initialize
        final int[] permutation = new int[n];
        for ( int i = 0; i < n; i++ )
        {
            permutation[i] = i;
            System.out.print((1 + i));
        }
        System.out.println();
        
        final int[] perm_remain = new int[n];
        for ( int i = 1; i <= n; i++ )
            perm_remain[i -1] = i;
        
        int numPermutationsPrinted = 1;
        for ( int pos_right = 2; pos_right <= n; pos_right++ )
        {
            int pos_left = pos_right -1;
            do
            {
                // rotate down perm[0..prev] by one
                next_perm(permutation, pos_left);
                
                if (--perm_remain[pos_left] > 0)
                {
                    if (numPermutationsPrinted++ < 30)
                    {
                        for (int i = 0; i < n; ++i)
                            System.out.print((1 + permutation[i]));
                        System.out.println();
                    }
                    else
                        return;
                    
                    for ( ; pos_left != 1; --pos_left)
                        perm_remain[pos_left -1] = pos_left;
                }
                else
                    ++pos_left;
            } while (pos_left < pos_right);
        }
    }
    
    public void run()
    {
        final int[] permutation = new int[n];
        final int[] perm_remain = new int[n];
        final int[] perm_flip = new int[n];

        int pos_right;
        while ((pos_right = remain_task.getAndIncrement()) < (n - 1))
        {
            int flip_max = 0;

            for (int i = 0; i < n - 1; i++)
                permutation[i] = i;

            permutation[pos_right] = (n - 1);
            permutation[n - 1] = (pos_right);

            for (int i = 1; i <= n; i++)
                perm_remain[i - 1] = i;

            int pos_left = n - 2;
            while (pos_left < n - 1)
            {
                // rotate down perm[0..r] by one
                next_perm(permutation, pos_left);

                if (--perm_remain[pos_left] > 0)
                {
                    for (; pos_left != 1; --pos_left)
                        perm_remain[pos_left - 1] = pos_left;

                    if ((permutation[0] != 0) && (permutation[n - 1] != (n - 1)))
                    {
                        System.arraycopy(permutation, 0, perm_flip, 0, n);
                        int flipcount = count_flip(perm_flip);
                        if (flip_max < flipcount)
                            flip_max = flipcount;
                    }
                }
                else
                    pos_left++;
            }

            // update max_flip foreach flipping position
            flip_max_arr[pos_right] = flip_max;
        }
    }


    // Take a permut array, continuously flipping until first element is '1'
    // Return flipping times
    private static final int count_flip(final int[] perm_flip)
    {
        // cache first element, avoid swapping perm[0] and perm[k]
        int v0 = perm_flip[0];
        int tmp;

        int flip_count = 0;
        do
        {
            for (int i = 1, j = v0 - 1; i < j; ++i, --j)
            {
                tmp = perm_flip[i];
                perm_flip[i] = perm_flip[j];
                perm_flip[j] = tmp;
            }

            tmp = perm_flip[v0];
            perm_flip[v0] = v0;
            v0 = tmp;

            flip_count++;
        } while (v0 != 0); // first element == '1' ?

        return flip_count;
    }

    // Return next permut, by rotating elements [0 - position] one 'step'
    // next_perm('1234', 2) -> '2314'
    private static final void next_perm(final int[] permutation, int position)
    {
        int perm0 = permutation[0];

        for (int i = 0; i < position; ++i)
            permutation[i] = permutation[i + 1];
        permutation[position] = perm0;
    }
}
