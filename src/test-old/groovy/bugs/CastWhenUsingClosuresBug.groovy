/**
 * @version $Revision$
 */
class CastWhenUsingClosuresBug extends GroovyTestCase {

    void testBug() {
        a = 1
        [1].each { a = it }
    }
}