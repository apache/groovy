package groovy.bugs

/**
 * @version $Revision$
 */
class CastWhenUsingClosuresBug extends GroovyTestCase {

    void testBug() {
        def a = 1

        def list = [1]
        list.each { a = it }
    }
}