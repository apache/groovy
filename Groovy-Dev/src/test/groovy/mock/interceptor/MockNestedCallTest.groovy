package groovy.mock.interceptor

class MockNestedCallTest extends GroovyTestCase {

    void testRestore() {
        def mockTail = new MockFor(Coin)
        mockTail.demand.flip(0..9) {"tail"}

        def mockHead = new MockFor(Coin)
        mockHead.demand.flip(0..9) {"head"}

        def c = new Coin()
        assert c.flip() == "edge"
        mockTail.use(c) {
            assert c.flip() == "tail"
            mockHead.use(c) {
                assert c.flip() == "head"
            }
            assert c.flip() == "tail"
        }
        assert c.flip() == "edge"
    }
}

class Coin {
    def flip() { "edge" }
}
