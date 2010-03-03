package groovy.bugs

class Groovy4038Bug extends GroovyTestCase {
    void testResondsToOnClosures() {
        def c = {String x -> }
        assert c.metaClass.respondsTo(c, "doCall", "Hello")        
        assert c.metaClass.respondsTo(c, "doCall")        
        assert c.metaClass.respondsTo(c, "setResolveStrategy")
    }
}
