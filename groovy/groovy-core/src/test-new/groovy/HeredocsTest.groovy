class HeredocsTest extends GroovyTestCase {

    void testHeredocs() {
        name = "James"
        s = """
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
        s = """
hello \${name}
"""
        println s
        assert s != null
        assert s.contains('$')
        c = s.count('$')
        assert c == 1
    }
}
