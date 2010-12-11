/*
The Computer Language Shootout
http://shootout.alioth.debian.org/

contributed by Jochen Hinrichsen
*/

def A(x, y) {
   // TODO: return statement is stated optional, but does not work w/o
   if (x == 0) return y+1
   if (y == 0) return A(x-1, 1)
   return A(x-1, A(x, y-1))
}

def n = this.args[0].toInteger()
def result = A(3, n)
println("Ack(3,${n}): ${result}")

