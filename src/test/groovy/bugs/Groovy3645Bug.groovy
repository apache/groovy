package groovy.bugs

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
        } catch(MissingMethodException ex) {
            assertTrue ex.message.contains("No signature of method: static java.lang.Object.bar()")
        }
    }
}