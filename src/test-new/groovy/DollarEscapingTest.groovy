class DollarEscapingTest extends GroovyTestCase {

    void testEscaping() {
        foo = "hello \${foo}"
        
        assert foo instanceof String
        
        c = foo.count('$')
        
        assert c == 1 , foo
    }
}
