package groovy.bugs

/**
 * @version $Revision$
 */
class Bytecode7Bug extends GroovyTestCase {

    void testDuplicateVariables() {
        if (true) {
            def a = 123
        }
        if (true) {
            def a = 456
        }
    }

    void testDuplicateVariablesInClosures() {
        def coll = [1]

        coll.each {
            def a = 123
        }
        coll.each {
            def a = 456
        }
    }
}