package groovy.mock.interceptor

import groovy.mock.interceptor.MockFor
import junit.framework.AssertionFailedError

class MockForWithZeroRangeTest extends GroovyTestCase {
    void testMockWithZeroRangeDemandAndNoCall() {
        MockFor mockForFoo = new MockFor(Foo)
        mockForFoo.demand.createBar(0..0) {}
        mockForFoo.use {
            println 'Foo is not called'
        }
        // We should get here and the test should pass.
    }

    void testMockWithZeroRangeDemandAndOneCall() {
        MockFor mockForFoo = new MockFor(Foo)
        mockForFoo.demand.createBar(0..0) {}
        shouldFail(AssertionFailedError) {
            mockForFoo.use {
                new Foo().createBar()
            }
        }
    }
}


class Foo {
    def createBar() {
        println 'bar'
    }
}