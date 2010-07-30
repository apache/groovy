/*
    The Computer Language Shootout
    http://shootout.alioth.debian.org/

    contributed by Jochen Hinrichsen
*/

private def approximate(n) {
    // create unit vector
    def u = [1.0D] * n as Double []

    // 20 steps of the power method
    def v = [0.0D] * n as Double []

    for (i in 1..10) {
        MultiplyAtAv(n,u,v)
        MultiplyAtAv(n,v,u)
    }

    // B=AtA         A multiplied by A transposed
    // v.Bv /(v.v)   eigenvalue of v
    double vBv = 0.0d, vv = 0.0D
    for (i in 0..<n) {
        vBv += u[i]*v[i]
        vv  += v[i]*v[i]
    }

    return Math.sqrt(vBv/vv)
}


/* return element i,j of infinite matrix A */
private def A(i,j) {
    return 1 / ((i+j)*(i+j+1)/2.0D +i+1)
}

/* multiply vector v by matrix A */
def MultiplyAv(n, v, Av){
    for (i in 0..<n) {
        Av[i] = 0.0D
        for (j in 0..<n) Av[i] += A(i,j)*v[j]
    }
}

/* multiply vector v by matrix A transposed */
def MultiplyAtv(n, v, Atv){
    for (i in 0..<n) {
        Atv[i] = 0.0D
        for (j in 0..<n) Atv[i] += A(j,i)*v[j]
    }
}

/* multiply vector v by matrix A and then by matrix A transposed */
def MultiplyAtAv(n, v, AtAv){
    Double[] u = new Double[n]
    MultiplyAv(n, v, u)
    MultiplyAtv(n, u, AtAv)
}


long start = System.currentTimeMillis ()
assert start >= 0

def n = (args.length == 0 ? 100 : args[0/*(int)0.0d*/].toInteger())
def nf = java.text.NumberFormat.getInstance()
nf.setMaximumFractionDigits(9)
nf.setMinimumFractionDigits(9)
nf.setGroupingUsed(false)
println(nf.format(approximate(n)))

println "${System.currentTimeMillis () - start}ms"

