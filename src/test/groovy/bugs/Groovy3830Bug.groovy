package groovy.bugs

class Groovy3830Bug extends GroovyTestCase {
    void testCallSitesUsageInAnInterface() {
        assert I3830.i == 2
        assert I3830.i2 == 5
        assert I3830.i3 == 6
    }
}

interface I3830 {
    Integer i = 2
    Integer i2 = i + 3
    Integer i3 = i * 3
}