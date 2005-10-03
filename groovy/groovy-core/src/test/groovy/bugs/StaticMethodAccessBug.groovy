// TODO: 

package groovy.bugs

import groovy.bugs.priv.*

class StaticMethodAccessBug extends GroovyTestCase {

    void testAccess() {

        System.out.println( "1234" )
        println( StaticMethodPublic.sayHello("a") )
        println( StaticMethodPackaged.sayHello("c") )
        shouldFail { println( StaticMethodProtected.sayHello("b") ) }
        shouldFail { println( StaticMethodPrivate.sayHello("d") ) }

        StaticMethodPublic.sayHello("a")
        StaticMethodPackaged.sayHello("c")
        shouldFail { StaticMethodPrivate.sayHello("d") }

        try {
            StaticMethodProtected.sayHello("b")
        }
        catch (Exception ex) {
            assert ( ex instanceof IllegalAccessException )
        }

        try {
            println( StaticMethodPrivate.sayHello("d") )
        }
        catch (Exception ex) {
            // println( ex )
            assert ( ex instanceof IllegalAccessException )
        }
    }

  /*
    static void main(args) {
        System.out.println( "abcd" )
        println( "1234" )

        println( StaticMethodPublic.sayHello("a") )
        println( StaticMethodPackaged.sayHello("c") )

        try {
            println( StaticMethodProtected.sayHello("b") )
        }
        catch (Exception ex) {
            assert( ex instanceof IllegalAccessException )
        }

        try {
            println( StaticMethodPrivate.sayHello("d") )
        }
        catch (Exception ex) {
            assert( ex instanceof IllegalAccessException )
        }

        try {
            StaticMethodPrivate?.say()
        }
        catch (Exception ex) {
            assert( ex instanceof IllegalAccessException )
        }

        try {
            StaticMethodPrivate.say()
        }
        catch (Exception ex) {
            assert( ex instanceof IllegalAccessException )
        }
    }
  */
}
