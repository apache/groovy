package groovy.bugs

import org.codehaus.groovy.control.MultipleCompilationErrorsException

class Groovy3645Bug extends GroovyTestCase {
    void testMethodCallOnSuperInAStaticMethod() {
        try{
            assertScript """
                class Foo3645 {
                    static main(args) {
                        super.bar()
                    }
                }
            """
            fail("Script compilation should have failed saying that 'super' cannot be used in a static context.")
        } catch(MultipleCompilationErrorsException ex) {
            assertTrue ex.message.contains("'super' cannot be used in a static context")
        }
    }
}