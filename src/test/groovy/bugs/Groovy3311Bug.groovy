package groovy.bugs

class Groovy3311Bug extends GroovyTestCase  {
    static period = new Groovy3311Bug()
    static period2 = 24
    def Groovy3311Bug() {
        // the pre-defined constant for 24 should be correctly init before next statement
        assert (24 != null)
    }
    
    def void testStaticInitUsingOwnConstructorUsingAPredefinedConstant() {
        assert (Groovy3311Bug.period2 == 24) 
    }
}
