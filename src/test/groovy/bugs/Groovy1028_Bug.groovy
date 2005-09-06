/**
 * Test for the issue GROOVY-1028
 *
 *     Accessible to the initialized static fields of interfaces.
 *
 *  @author  Ken Barclay <k.barclay@napier.ac.uk>
 *  @author  Pilho Kim <phkim@cluecom.co.kr>
 */

package groovy.bugs

class Groovy1028_Bug extends GroovyTestCase {
    void testStaticFieldOfInterface() {
        // println "isInterfacee()? ${Groovy1028Class.class.isInterface()}"    // true
        // println "Hello: ${Groovy1028Class.class.FORENAME}"	               // Hello: Foo
        assert !(Groovy1028Class.class.isInterface())
        assert Groovy1028Class.class.FORENAME == "Foo"

        // println "isInterfacee()? ${Groovy1028Interface.class.isInterface()}"   // true
        // println "Hello: ${Groovy1028Interface.class.FORENAME}"                 // Hello: Foo
        assert Groovy1028Interface.class.isInterface()
        assert Groovy1028Interface.class.FORENAME == "Foo"
    }

    static void main(args) {
        new Groovy1028_Bug().testStaticFieldOfInterface()
    }
}

interface Groovy1028Interface {
    public static final String SURNAME = "Bar"
    public static final String FORENAME = "Foo"
}

abstract class Groovy1028Class {
    public static final String SURNAME	= "Bar"
    public static final String FORENAME	= "Foo"
}


