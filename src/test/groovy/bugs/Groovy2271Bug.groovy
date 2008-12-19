package groovy.bugs

import groovy.mock.interceptor.StubFor

class Groovy2271Bug extends GroovyTestCase {
    static final String TEST_TEXT ="I'm a mock"
    def void testClosureMock() {
        StubFor fooStub = new StubFor(Groovy2271Foo)

        fooStub.demand.createBar(0..2) {TEST_TEXT}

        Closure closure = {createBar()}

        fooStub.use {
        	Groovy2271Foo foo = new Groovy2271Foo()
            assertEquals(TEST_TEXT, foo.createBar())
            closure.delegate = foo
            assertEquals(TEST_TEXT, closure.call())
        }
    }
}

class Groovy2271Foo {
    def createBar() {
        throw new RuntimeException("We should never get here!")
    }
}

