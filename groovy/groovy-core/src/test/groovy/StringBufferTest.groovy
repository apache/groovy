class StringBufferTest extends GroovyTestCase {
    void testSize() {
        def x = new StringBuffer()
        assert x.size() == x.length()
        x = new StringBuffer('some text')
        assert x.size() == x.length()
    }
}