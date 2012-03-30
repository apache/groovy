/*
 * The Computer Language Benchmarks Game
 * http://shootout.alioth.debian.org/
 *
 * contributed by Pilho Kim
 */

def countSieve(m, primes) {
    def i, k
    def count = 0

    i = 2
    while (i <= m) {
        primes[i] = true
        i++
    }

    i = 2
    while (i <= m) {
        if (primes[i]) {
            k = i + i
            while (k <= m) {
                primes[k] = false
                k += i
            }
            count++
        }
        i++
    }
    return count
}

def padNumber(number, fieldLen) {
    def bareNumber = "" + number
    def numSpaces = fieldLen - bareNumber.length()
    def sb = new StringBuffer(' ' * numSpaces)
    sb.append(bareNumber)
    return sb.toString()
}

def n = 2
if (args.length > 0)
    n = args[0].toInteger()
if (n < 2)
    n = 2

def m = (1 << n) * 10000
def flags = new boolean[m+1]

[n, n-1, n-2].each {
    def k = (1<<it) * 10000
    def s1 = padNumber(k, 8)
    def s2 = padNumber(countSieve(k, flags), 9)
    println("Primes up to $s1$s2")
}
