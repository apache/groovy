/**
 * @version $Revision$
 */
class InconsistentStackHeightBug extends GroovyTestCase {

    void testBug() {
        server = 0
        /** @todo the following fails
        server + 1
        */
        dummy = server + 1
        try {
        }
        finally {
        }
    }
}