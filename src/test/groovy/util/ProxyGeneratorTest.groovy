/*
 * Copyright 2003-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package groovy.util

/**
 * @author Paul King
 */
class ProxyGeneratorTest extends GroovyTestCase {

    void testAggregateFromBaseClass() {
        Map map = [myMethodB: {"the new B"}, myMethodX: {"the injected X"}]
        def testClass = ProxyGenerator.instantiateAggregateFromBaseClass(map, TestClass)
        assert testClass instanceof TestClass
        assert testClass.myMethodA() == "the original A"
        assert testClass.myMethodB() == "the new B"
        assert testClass.myMethodX() == "the injected X"
    }

    void testAggregateFromAbstractBaseClass() {
        Map map = [myMethodG: {"the concrete G"}, myMethodX: {"the injected X"}]
        def testClass = ProxyGenerator.instantiateAggregateFromBaseClass(map, AbstractClass)
        assert testClass instanceof AbstractClass
        assert testClass.myMethodA() == "the original A"
        assert testClass.myMethodG() == "the concrete G"
        assert testClass.myMethodX() == "the injected X"
    }

    void testAggregateFromInterface() {
        Map map = [myMethodC: {"the injected C"}]
        def testClass = ProxyGenerator.instantiateAggregateFromInterface(map, TestInterface)
        assert testClass instanceof TestInterface
        assert testClass instanceof GroovyObject
        assert testClass.myMethodC() == "the injected C"
    }

    void testAggregate() {
        Map map = [myMethodE: {"the injected E"}, myMethodB: {"the new B"}, myMethodX: {"the injected X"}]
        def testClass = ProxyGenerator.instantiateAggregate(map, [TestInterface, TestOtherInterface], TestClass)
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
        def testClass = ProxyGenerator.instantiateDelegate(map, [TestInterface, TestOtherInterface], delegate)
        assert testClass instanceof TestInterface
        assert testClass instanceof TestOtherInterface
        assert testClass.myMethodA() == "the original A"
        assert testClass.myMethodB() == "the new B"
        assert testClass.myMethodX() == "the injected X"
        assert testClass.myMethodE() == "the injected E"
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

abstract class AbstractClass {
    def myMethodA() { return "the original A" }
    abstract myMethodG()
}
