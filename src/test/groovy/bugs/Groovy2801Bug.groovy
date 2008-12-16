package groovy.bugs

class Groovy2801Bug extends GroovyTestCase {
    def void testOverrideToStringInMapOfClosures() {
        def proxyImpl = [foo: { "Foo!" }, toString: { "overridden." }] as IGroovy2801Bug
        assert proxyImpl.toString() == "overridden."
    }
}

interface IGroovy2801Bug {
   String foo()
}
