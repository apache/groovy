package groovy

class MultilineStringTest extends GroovyTestCase {

    void testMultilineString() {
        def s = """abcd
efg

        hijk
        
"""
        println(s)
        assert s != null
        def idx = s.indexOf("i")
        assert idx > 0
    }
}
