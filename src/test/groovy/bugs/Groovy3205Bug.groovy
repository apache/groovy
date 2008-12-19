package groovy.bugs

class Groovy3205Bug extends GroovyTestCase {
    def void testOverrideToStringInMapOfClosures() {
        def proxyImpl = [
                control: { "new control" },
                toString: { "new toString" }
        ] as IGroovy3205Bug
        assert proxyImpl.control() == "new control"
        assert proxyImpl.toString() == "new toString"
    }
}

class IGroovy3205Bug {
    String control() { "original control" }
    String toString() { "original toString" }
}
