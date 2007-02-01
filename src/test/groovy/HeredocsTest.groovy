package groovy

class HeredocsTest extends GroovyTestCase {

    void testHeredocs() {
        def name = "James"
        def s = """
abcd
efg

hijk
     
hello ${name}
        
"""
        println s
        assert s != null
        assert s instanceof GString

        assert s.contains("i")
        assert s.contains("James")
    }
    
    void testDollarEscaping() {
        def s = """
hello \${name}
"""
        println s
        assert s != null
        assert s.contains('$')
        def c = s.count('$')
        assert c == 1
    }
}
