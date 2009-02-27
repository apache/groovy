package groovy.bugs

class Groovy3383Bug extends GroovyTestCase {
    void testClassUsageInInterfaceDef() {
        assertScript """
			interface Groovy3383 {
			   Class type = Groovy3383.class
			}
			
			def t = Groovy3383.type
			assert t.name == "Groovy3383"
        """
    }
}