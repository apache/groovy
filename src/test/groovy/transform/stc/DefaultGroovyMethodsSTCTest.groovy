package groovy.transform.stc


/**
 * Created by IntelliJ IDEA.
 * User: cedric
 * Date: 04/10/11
 * Time: 14:36
 */

/**
 * Unit tests for static type checking : default groovy methods.
 *
 * @author Cedric Champeau
 */
class DefaultGroovyMethodsSTCTest extends StaticTypeCheckingTestCase {

    void testEach() {
        assertScript """
            ['a','b'].each { // DGM#each(Object, Closure)
                println it // DGM#println(Object,Object)
            }
        """

        assertScript """
            ['a','b'].eachWithIndex { it, i ->// DGM#eachWithIndex(Object, Closure)
                println it // DGM#println(Object,Object)
            }
        """
    }

    void testStringToInteger() {
        assertScript """
        String name = "123"
        name.toInteger() // toInteger() is defined by DGM
        """
    }

/*    void testVariousAssignmentsThenToInteger() {
        assertScript """
        def name = 1
        name = new Object()
        name = '123'
        name.toInteger() // toInteger() is defined by DGM
        """
    }*/

}

