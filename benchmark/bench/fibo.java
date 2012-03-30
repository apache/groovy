// $Id: fibo.java,v 1.3 2005-04-25 19:01:38 igouy-guest Exp $
// http://www.bagley.org/~doug/shootout/

public class fibo {
    public static void main(String args[]) {
        int N = Integer.parseInt(args[0]);
        System.out.println(fib(N));
    }
    public static int fib(int n) {
        if (n < 2) return(1);
        return( fib(n-2) + fib(n-1) );
    }
}
