/**
 * @author Morgan Hankins
 * @version $Revision$
 */
class MorgansBug extends GroovyTestCase {

    void testBug() {
        result = 4 + "x"
        assert result == "4x"
    }
}