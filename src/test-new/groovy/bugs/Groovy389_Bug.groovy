/**
 *  Verifies that closures work inside case blocks.
 *
 */

class Groovy389_Bug extends GroovyTestCase {
 
    void testBug() {
       a = [10, 11, 12]
       b = 0
       
       switch( "list" ) {
          case "list":
             a.each { b = b + 1 }
             break
       }

       assert b == 3
    }

}
