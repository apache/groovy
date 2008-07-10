package groovy.bugs

class Groovy2610Bug extends GroovyTestCase {
    void testMe () {
      calc (10G)
    }

    def calc ( n ) {
        Factorial.iterative ( n ) ;
    }
}

class Factorial {
  static BigInteger iterative ( BigInteger n ) {
    BigInteger total =1G
    while ( n-- > 1 ) { total *= n }
    total
  }
}
