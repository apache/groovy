// $Id: fibo.java,v 1.3 2005-04-25 19:01:38 igouy-guest Exp $
// http://www.bagley.org/~doug/shootout/

int N = args[0] as int
println fib(N)

int fib(int n) {
    if (n < 2) return 1
    return fib(n - 2) + fib(n - 1)
}

