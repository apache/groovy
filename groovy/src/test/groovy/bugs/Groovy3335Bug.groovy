package groovy.bugs

class Groovy3335Bug extends GroovyTestCase {
    void testClassToString() {
        // the following call was resulting in a MethodSelectionException
        // because Integer class defines static toString(int) and toString(int, int) methods
        println Integer.class.toString()    
    }
}
