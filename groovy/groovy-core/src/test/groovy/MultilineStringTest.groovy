class MultilineStringTest extends GroovyTestCase {

    void testMultilineString() {
        s = """abcd
efg

        hijk
        
"""
        println(s)
        assert s != null
        idx = s.indexOf("i")
        assert idx > 0
    }
}
