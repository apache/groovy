package groovy.bugs

/**
 * @version $Revision$
 */
class InconsistentStackHeightBug extends GroovyTestCase {

    void testBug() {
        def server = 0
        def tmp = server + 1
        try {
        }
        finally {
        }
    }

    void testBug2() {
        def server = 0
        def tmp = server == 2
        try {
        }
        finally {
        }
    }

    void testBug3() {
        def server = 0
        def foo = server
        try {
        }
        finally {
        }
    }
}