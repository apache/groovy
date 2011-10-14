package groovy.transform.stc


/**
 * Created by IntelliJ IDEA.
 * User: cedric
 * Date: 04/10/11
 * Time: 14:36
 */

/**
 * Unit tests for static type checking : type inference.
 *
 * @author Cedric Champeau
 */
class TypeInferenceSTCTest extends StaticTypeCheckingTestCase {

    /*
    void testInstanceOf() {
        assertScript """
        Object o
        if (o instanceof String) o.toUpperCase()
        """
    }*/

    void testStringToInteger() {
        assertScript """
        def name = "123" // we want type inference
        name.toInteger() // toInteger() is defined by DGM
        """
    }

    void testGStringMethods() {
        assertScript '''
            def myname = 'Cedric'
            "My upper case name is ${myname.toUpperCase()}"
            println "My upper case name is ${myname}".toUpperCase()
        '''
    }


}

