/*
 * The Computer Language Benchmark Games
 * http://shootout.alioth.debian.org/
 *
 * contributed by James Durbin
 * based very closely on Ruby version by jose fco. gonzalez
 */

def seq = new StringBuffer()
seq = System.in.readLines().join("\n") + "\n"

initialLength = seq.length()
seq = (seq =~">.*\n|\n").replaceAll('')
codeLength = seq.length()

[
   'agggtaaa|tttaccct',
   '[cgt]gggtaaa|tttaccc[acg]',
   'a[act]ggtaaa|tttacc[agt]t',
   'ag[act]gtaaa|tttac[agt]ct',
   'agg[act]taaa|ttta[agt]cct',
   'aggg[acg]aaa|ttt[cgt]ccct',
   'agggt[cgt]aa|tt[acg]accct',
   'agggta[cgt]a|t[acg]taccct',
   'agggtaa[cgt]|[acg]ttaccct'
].each{
   println "$it " + (seq =~ it).getCount()
}

[
   B: '(c|g|t)',
   D: '(a|g|t)',
   H: '(a|c|t)',
   K: '(g|t)',
   M: '(a|c)',
   N: '(a|c|g|t)',
   R: '(a|g)',
   S: '(c|t)',
   V: '(a|c|g)',
   W: '(a|t)',
   Y: '(c|t)'
].each { f, r -> 
   seq = (seq =~ f).replaceAll(r)
}

println ''
println initialLength
println codeLength
println seq.length()
