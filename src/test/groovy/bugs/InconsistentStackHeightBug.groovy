/**
 * @version $Revision$
 */
class InconsistentStackHeightBug extends GroovyTestCase {

    void testBug() {
        server = 0
        def tmp = server + 1
        try {
        }
        finally {
        }
    }

    void testBug2() {
        server = 0
        def tmp = server == 2
        try {
        }
        finally {
        }
    }

    void testBug3() {
        server = 0
        def foo = server
        try {
        }
        finally {
        }
    }
}