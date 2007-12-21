package groovy

class DollarEscapingTest extends GroovyTestCase {

    void testEscaping() {
        def foo = "hello \${foo}"
        
        assert foo instanceof String
        
        def c = foo.count('$')
        
        assert c == 1 , foo
    }
}
