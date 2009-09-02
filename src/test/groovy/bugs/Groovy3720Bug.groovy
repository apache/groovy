package groovy.bugs

import groovy.mock.interceptor.*

class Groovy3720Bug extends GroovyTestCase {
    void testCreateStubNode() {
        def stubNodeContext1 = new StubFor(AnotherNode3720) 
        assertNotNull stubNodeContext1.proxyInstance()

        def stubNodeContext2 = new StubFor(MyNode3720) 
        assertNotNull stubNodeContext2.proxyInstance()
    }

    void testCreateStubNodeDelegate() {
        def stubNodeContext1 = new StubFor(AnotherNode3720) 
        assertNotNull stubNodeContext1.proxyDelegateInstance()

        def stubNodeContext2 = new StubFor(MyNode3720) 
        assertNotNull stubNodeContext2.proxyDelegateInstance()
    }
    
    void testCreateMockNode() {
        def mockNodeContext1 = new MockFor(AnotherNode3720) 
        assertNotNull mockNodeContext1.proxyInstance()

        def mockNodeContext2 = new MockFor(MyNode3720) 
        assertNotNull mockNodeContext2.proxyInstance()
    }

    void testCreateMockNodeDelegate() {
        def mockNodeContext1 = new MockFor(AnotherNode3720) 
        assertNotNull mockNodeContext1.proxyDelegateInstance()

        def mockNodeContext2 = new MockFor(MyNode3720) 
        assertNotNull mockNodeContext2.proxyDelegateInstance()
    }
}

abstract class MyNode3720 {}

abstract class BaseNode3720 {
    abstract m1()
}

abstract class AnotherNode3720 extends BaseNode3720 {
    abstract m2()
}