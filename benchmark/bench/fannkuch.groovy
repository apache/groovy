/*
 * The Computer Language Benchmarks Game
 * http://shootout.alioth.debian.org/
 *
 * contributed by Brian Schlining
 */

def n = 7
if (args.length > 0) {
    n = Integer.parseInt(args[0])
}
println("Pfannkuchen(" + n + ") = " + fannkuch(n))

def fannkuch(int n) {
    int check = 0
    int[] perm = new int[n]
    int[] perm1 = new int[n]
    int[] count = new int[n]
    int[] maxPerm = new int[n]
    int maxFlipsCount = 0
    int m = n - 1

    for (i in 0..<n) {
        perm1[i] = i
    }
    int r = n

    while (true) {
        // write-out the first 30 permutations
        if (check < 30){
            for (i in 0..<n) {
                print(perm1[i] + 1)
            }
            print("\n")
            check++
        }

        while (r != 1) { 
            count[r - 1] = r
            r--
        }
        if (!(perm1[0] == 0 || perm1[m] == m)) {
            for (i in 0..<n) {
                perm[i] = perm1[i]
            }
            
            int flipsCount = 0
            int k

            while (!((k = perm[0]) == 0)) {
                int k2 = (k + 1) >> 1
                for (i in 0..<k2) {
                    int temp = perm[i] 
                    perm[i] = perm[k - i] 
                    perm[k - i] = temp
                }
                flipsCount++
            }

            if (flipsCount > maxFlipsCount) {
                maxFlipsCount = flipsCount
                for (i in 0..<n) {
                    maxPerm[i] = perm1[i]
                }
            }
        }

        while (true) {
            if (r == n) {
                return maxFlipsCount
            }
            int perm0 = perm1[0]
            int i = 0
            while (i < r) {
                int j = i + 1
                perm1[i] = perm1[j]
                i = j
            }
            perm1[r] = perm0

            count[r] = count[r] - 1
            if (count[r] > 0) {
                break
            }
            r++
        }
    }
}
