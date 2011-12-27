/*
 * The Computer Language Benchmarks Game
 * http://shootout.alioth.debian.org/
 *
 * Contributed by Oleg Mazurov, June 2010
 *
 */

import java.util.concurrent.atomic.AtomicInteger;

public final class fannkuchredux implements Runnable
{
    private static final int NCHUNKS = 150;
    private static int CHUNKSZ;
    private static int NTASKS;
    private static int n;
    private static int[] Fact;
    private static int[] maxFlips;
    private static int[] chkSums;
    private static AtomicInteger taskId;

    int[] p, pp, count;

    void print()
    {
        for ( int i = 0; i < p.length; i++ ) {
            System.out.print( p[i] + 1 );
        }
        System.out.println();
    }

    void firstPermutation( int idx )
    {
        for ( int i=0; i<p.length; ++i ) {
           p[i] = i;
        }

        for ( int i=count.length-1; i>0; --i ) {
            int d = idx / Fact[i];
            count[i] = d;
            idx = idx % Fact[i];

            System.arraycopy( p, 0, pp, 0, i+1 );
            for ( int j=0; j<=i; ++j ) {
                p[j] = j+d <= i ? pp[j+d] : pp[j+d-i-1];
            }
        }
    }

    boolean nextPermutation()
    {
        int first = p[1];
        p[1] = p[0];
        p[0] = first;

        int i=1;
        while ( ++count[i] > i ) {
            count[i++] = 0;
            int next = p[0] = p[1];
            for ( int j=1; j<i; ++j ) {
                p[j] = p[j+1];
            }
            p[i] = first;
            first = next;
        }
        return true;
    }

    int countFlips()
    {
        int flips = 1;
        int first = p[0];
        if ( p[first] != 0 ) {
            System.arraycopy( p, 0, pp, 0, pp.length );
            do {
                 ++flips;
                 for ( int lo = 1, hi = first - 1; lo < hi; ++lo, --hi ) {
                    int t = pp[lo];
                    pp[lo] = pp[hi];
                    pp[hi] = t;
                 }
                 int t = pp[first];
                 pp[first] = first;
                 first = t;
            } while ( pp[first] != 0 );
        }
        return flips;
    }

    void runTask( int task )
    {
        int idxMin = task*CHUNKSZ;
        int idxMax = Math.min( Fact[n], idxMin+CHUNKSZ );

        firstPermutation( idxMin );

        int maxflips = 1;
        int chksum = 0;
        for ( int i=idxMin;; ) {

            if ( p[0] != 0 ) {
                int flips = countFlips();
                maxflips = Math.max( maxflips, flips );
                chksum += i%2 ==0 ? flips : -flips;
            }

            if ( ++i == idxMax ) {
                break;
            }

            nextPermutation();
        }
        maxFlips[task] = maxflips;
        chkSums[task]  = chksum;
    }

    public void run()
    {
        p     = new int[n];
        pp    = new int[n];
        count = new int[n];

        int task;
        while ( ( task = taskId.getAndIncrement() ) < NTASKS ) {
            runTask( task );
        }
    }

    static void printResult( int n, int res, int chk )
    {
        System.out.println( chk+"\nPfannkuchen("+n+") = "+res );
    }

    public static void main( String[] args )
    {
        n = args.length > 0 ? Integer.parseInt( args[0] ) : 12;
        if ( n < 0 || n > 12 ) {         // 13! won't fit into int
            printResult( n, -1, -1 );
            return;
        }
        if ( n <= 1 ) {
            printResult( n, 0, 0 );
            return;
        }

        Fact = new int[n+1];
        Fact[0] = 1;
        for ( int i=1; i<Fact.length; ++i ) {
            Fact[i] = Fact[i-1] * i;
        }

        CHUNKSZ = (Fact[n] + NCHUNKS - 1) / NCHUNKS;
        NTASKS = (Fact[n] + CHUNKSZ - 1) / CHUNKSZ;
        maxFlips = new int[NTASKS];
        chkSums  = new int[NTASKS];
        taskId = new AtomicInteger(0);

        int nthreads = Runtime.getRuntime().availableProcessors();
        Thread[] threads = new Thread[nthreads];
        for ( int i=0; i<nthreads; ++i ) {
            threads[i] = new Thread( new fannkuchredux() );
            threads[i].start();
        }
        for ( Thread t : threads ) {
            try {
                t.join();
            }
            catch ( InterruptedException e ) {}
        }

        int res = 0;
        for ( int v : maxFlips ) {
            res = Math.max( res, v );
        }
        int chk = 0;
        for ( int v : chkSums ) {
            chk += v;
        }

        printResult( n, res, chk );
    }
}
