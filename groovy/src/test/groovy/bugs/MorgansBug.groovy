package groovy.bugs

/**
 * @author Morgan Hankins
 * @version $Revision$
 */
class MorgansBug extends GroovyTestCase {

    void testBug() {
        def result = 4 + "x"
        assert result == "4x"
    }
}