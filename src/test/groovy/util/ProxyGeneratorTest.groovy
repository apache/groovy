/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package groovy.util

class ProxyGeneratorTest extends GroovyTestCase {

    ProxyGenerator generator = ProxyGenerator.INSTANCE

    void testAggregateFromBaseClass() {
        Map map = [myMethodB: {"the new B"}, myMethodX: {"the injected X"}]
        def testClass = generator.instantiateAggregateFromBaseClass(map, TestClass)
        assert testClass instanceof TestClass
        assert testClass.myMethodA() == "the original A"
        assert testClass.myMethodB() == "the new B"
        assert testClass.myMethodX() == "the injected X"
    }

    void testAggregateFromAbstractBaseClass() {
        Map map = [myMethodG: {"the concrete G"}, myMethodX: {"the injected X"}]
        def testClass = generator.instantiateAggregateFromBaseClass(map, AbstractClass)
        assert testClass instanceof AbstractClass
        assert testClass.myMethodA() == "the original A"
        assert testClass.myMethodG() == "the concrete G"
        assert testClass.myMethodX() == "the injected X"
    }

    void testAggregateFromInterface() {
        Map map = [myMethodC: {"the injected C"}]
        def testClass = generator.instantiateAggregateFromInterface(map, TestInterface)
        assert testClass instanceof TestInterface
        assert testClass instanceof GroovyObject
        assert testClass.myMethodC() == "the injected C"
    }

    void testAggregate() {
        Map map = [myMethodE: {"the injected E"}, myMethodB: {"the new B"}, myMethodX: {"the injected X"}]
        def testClass = generator.instantiateAggregate(map, [TestInterface, TestOtherInterface], TestClass)
        assert testClass instanceof TestInterface
        assert testClass instanceof TestOtherInterface
        assert testClass instanceof TestClass
        assert testClass.myMethodA() == "the original A"
        assert testClass.myMethodB() == "the new B"
        assert testClass.myMethodX() == "the injected X"
        assert testClass.myMethodE() == "the injected E"
    }

    void testDelegate() {
        def delegate = new TestClass()
        Map map = [myMethodE: {"the injected E"}, myMethodB: {"the new B"}, myMethodX: {"the injected X"}]
        def testClass = generator.instantiateDelegate(map, [TestInterface, TestOtherInterface], delegate)
        assert testClass instanceof TestInterface
        assert testClass instanceof TestOtherInterface
        assert testClass.myMethodA() == "the original A"
        assert testClass.myMethodB() == "the new B"
        assert testClass.myMethodX() == "the injected X"
        assert testClass.myMethodE() == "the injected E"
    }

    void testDelegateWithBaseClass() {
        def delegate = new TestClass()
        Map map = [myMethodE: {"the injected E"}, myMethodB: {"the new B"}, myMethodX: {"the injected X"}]
        TestClass testClass = generator.instantiateDelegateWithBaseClass(map, [TestInterface, TestOtherInterface], delegate)
        assert testClass instanceof TestInterface
        assert testClass instanceof TestOtherInterface
        assert testClass.myMethodA() == "the original A"
        assert testClass.myMethodB() == "the new B"
        assert testClass.myMethodX() == "the injected X"
        assert testClass.myMethodE() == "the injected E"
    }
    
    void testDelegateForGROOVY_2705() {
        def delegate = [1, 2, 3, 4, 5]
        def testClass = generator.instantiateDelegate([List], delegate)
        assert testClass instanceof List
        assert 5 == testClass.size() 
        assert [1, 2, 3, 4, 5] == testClass.iterator().collect { it }
        assert 3 == testClass[2]
        testClass[3] = 99
        assert 99 == testClass[3]
        testClass.removeAt(2)
        testClass.removeAt(1)
        assert [1, 99, 5] == testClass
    }

    void testUnknownMethodThrowsUnsupportedOperationException() {
        def map = [ myMethodA: { 'some string' } ]
        def proxy = ProxyGenerator.instantiateAggregateFromInterface(map, TestInterface)
        assert proxy instanceof TestInterface
        assertEquals 'some string', proxy.myMethodA()
        shouldFail(UnsupportedOperationException) {
            proxy.myMethodC()
        }
    }

    void testUnknownMethodWithBlankBody() {
        def map = [ myMethodA: { 'some string' } ]
        def gen = new ProxyGenerator()
        gen.emptyMethods = true
        def proxy = gen.instantiateAggregate(map, [TestInterface])
        assert proxy instanceof TestInterface
        assertEquals 'some string', proxy.myMethodA()
        proxy.myMethodC()
    }

    void testProxyWithToString() {
        def map = [ toString: {'hello'} ]
        def gen = new ProxyGenerator()
        def proxy = gen.instantiateAggregateFromBaseClass(map, Object)
        assert proxy.toString() == 'hello'
    }
    
    void testProxyWithClosureChangedAfterCreation() {
        def map = [ toString: { 'hello'} ]
        def gen = new ProxyGenerator()
        def proxy = gen.instantiateAggregateFromBaseClass(map, Object)
        assert proxy.toString() == 'hello'
        map.toString = { 'world' }
        assert proxy.toString() == 'world'
    }
    
    void testProxyMethodUsingLongAsParameter() {
        def map = [ foo: { a,b -> a*b }]
        def gen = new ProxyGenerator()
        def proxy = gen.instantiateAggregateFromBaseClass(map, TestInterfaceWithLong)
        assert proxy.foo(3,3) == 9
    }

    void testProxyMethodUsingDoubleAsParameter() {
        def map = [ foo: { a,b -> a*b }]
        def gen = new ProxyGenerator()
        def proxy = gen.instantiateAggregateFromBaseClass(map, TestInterfaceWithDouble)
        assert proxy.foo(3d,3d) == 9d
    }

    void testProxyMethodUsingVariousTypesAsParameters() {
        def map = [ foo: { a,b,c,d -> a*b+c-d }]
        def gen = new ProxyGenerator()
        def proxy = gen.instantiateAggregateFromBaseClass(map, TestInterfaceWithVariousTypes)
        assert proxy.foo(3, 3d, 4, 1f) == 12d
    }
}

class TestClass {
    def myMethodA() { return "the original A" }
    def myMethodB() { return "the original B" }
}

interface TestInterface {
    def myMethodA()
    def myMethodC()
    def myMethodD()
}

interface TestOtherInterface {
    def myMethodB()
    def myMethodE()
    def myMethodF()
}

interface TestInterfaceWithLong {
    long foo(long a, long b)
}

interface TestInterfaceWithDouble {
    double foo(double a, double b)
}

interface TestInterfaceWithVariousTypes {
    double foo(int a, double b, long c, float d)
}

abstract class AbstractClass {
    def myMethodA() { return "the original A" }
    abstract myMethodG()
}
