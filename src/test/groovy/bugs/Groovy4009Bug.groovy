package groovy.bugs

class Groovy4009Bug extends GroovyTestCase {
    void testNoOfTimesResolveCallIsMade() {
        try {
            assertScript """
                class Test {
                    static main(args) {
                        Int4009 x = 1
                        def y = x + x
                    }
                }
            """
            fail('Should have failed as type Int4009 should not be resolved')
        } catch (RuntimeException ex) {
            assert ex.message.count('unable to resolve class Int4009') == 1
        }
    }
}
