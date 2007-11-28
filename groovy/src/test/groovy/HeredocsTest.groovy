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
        assert s != null
        assert s instanceof GString
        assert s.contains("i")
        assert s.contains("James")
        def numlines = s.count('\n')
        assert numlines == 8
    }

    void testDollarEscaping() {
        def s = """
hello \${name}
"""
        assert s != null
        assert s.contains('$')
        def c = s.count('$')
        assert c == 1
        assert s == '\nhello ${name}\n'
    }
}
