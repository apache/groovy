package groovy.bugs

class MethodCallWithoutParensInStaticMethodBug extends GroovyTestCase {

    void testBug() {
        staticMethod()
    }
    
    static void staticMethod() {
        println 'hello'[1]
    }
}