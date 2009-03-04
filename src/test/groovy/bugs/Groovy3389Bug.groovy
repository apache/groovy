package groovy.bugs

class Groovy3389Bug extends GroovyTestCase {
    void testFieldHidingByLocalVariable() {
        assertScript """
            class Groovy3389 {
                String bar
                void doIt() {
                    def bar = new File('.')
                    assert bar instanceof File
                }
            }
            
            def obj = new Groovy3389()
            obj.doIt()
        """
    }
}