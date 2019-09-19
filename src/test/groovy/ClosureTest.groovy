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
package groovy

import groovy.test.GroovyTestCase
import org.codehaus.groovy.control.MultipleCompilationErrorsException

import static groovy.lang.Closure.IDENTITY

/**
 * Tests Closures in Groovy
 */
class ClosureTest extends GroovyTestCase {

    def count

    void testSimpleBlockCall() {
        count = 0

        def block = { owner -> owner.incrementCallCount() }

        assertClosure(block)
        assert count == 1

        assertClosure({ owner -> owner.incrementCallCount() })
        assert count == 2
    }

    void testVariableLengthParameterList() {

        def c1 = { Object[] args -> args.each { count += it } }

        count = 0
        c1(1, 2, 3)
        assert count == 6

        count = 0
        c1(1)
        assert count == 1

        count = 0
        c1([1, 2, 3] as Object[])
        assert count == 6

        def c2 = { a, Object[] args -> count += a; args.each { count += it } }

        count = 0
        c2(1, 2, 3)
        assert count == 6

        count = 0
        c2(1)
        assert count == 1

        count = 0
        c2(1, [2, 3] as Object[])
        assert count == 6
    }

    void testBlockAsParameter() {
        count = 0

        callBlock(5, { owner -> owner.incrementCallCount() })
        assert count == 6

        callBlock2(5, { owner -> owner.incrementCallCount() })
        assert count == 12
    }

    void testMethodClosure() {
        def block = this.&incrementCallCount

        count = 0

        block.call()

        assert count == 1

        block = Math.&min

        assert block.call(3, 7) == 3
    }

    def incrementCallCount() {
        //System.out.println("invoked increment method!")
        count = count + 1
    }

    def assertClosure(Closure block) {
        assert block != null
        block.call(this)
    }

    protected void callBlock(Integer num, Closure block) {
        for (i in 0..num) {
            block.call(this)
        }
    }

    protected void callBlock2(num, block) {
        for (i in 0..num) {
            block.call(this)
        }
    }


    int numAgents = 4
    boolean testDone = false

    void testIntFieldAccess() {
        def agents = new ArrayList();
        numAgents.times {
            TinyAgent btn = new TinyAgent()
            testDone = true
            btn.x = numAgents
            agents.add(btn)
        }
        assert agents.size() == numAgents
    }

    void testWithIndex() {
        def str = ''
        def sum = 0
        ['a', 'b', 'c', 'd'].eachWithIndex { item, index -> str += item; sum += index }
        assert str == 'abcd' && sum == 6
    }

    void testMapWithEntryIndex() {
        def keyStr = ''
        def valStr = ''
        def sum = 0
        ['a': 'z', 'b': 'y', 'c': 'x', 'd': 'w'].eachWithIndex { entry, index ->
            keyStr += entry.key
            valStr += entry.value
            sum += index
        }
        assert keyStr == 'abcd' && valStr == 'zyxw' && sum == 6
    }

    void testMapWithKeyValueIndex() {
        def keyStr = ''
        def valStr = ''
        def sum = 0
        ['a': 'z', 'b': 'y', 'c': 'x', 'd': 'w'].eachWithIndex { k, v, index ->
            keyStr += k
            valStr += v
            sum += index
        }
        assert keyStr == 'abcd' && valStr == 'zyxw' && sum == 6
    }

    /**
     * Test access to Closure's properties
     * cf GROOVY-2089
     */
    void testGetProperties() {
        def c = { println it }

        assert c.delegate == c.getDelegate()
        assert c.owner == c.getOwner()
        assert c.maximumNumberOfParameters == c.getMaximumNumberOfParameters()
        assert c.parameterTypes == c.getParameterTypes()
        assert c.class == c.getClass()
        assert c.directive == c.getDirective()
        assert c.resolveStrategy == c.getResolveStrategy()
        assert c.thisObject == c.getThisObject()

        // no idea why this one fails
        // assert c.metaClass == c.getMetaClass()
    }

    void testGetPropertiesGenerically() {
        // ensure closure metaclass is the original one
        Closure.metaClass = null

        Closure.metaClass.properties.each { property ->
            def closure = { println it }
            closure."$property.name" == closure."${MetaProperty.getGetterName(property.name, property.type)}"()
        }
    }

    void testSetProperties() {
        def c = { println it }

        def myDelegate = new Object()
        c.delegate = myDelegate
        assert c.getDelegate() == myDelegate

        c.resolveStrategy = Closure.DELEGATE_ONLY
        assert c.getResolveStrategy() == Closure.DELEGATE_ONLY

        c.directive = Closure.DONE
        assert c.directive == Closure.DONE

        // like in testGetProperties(), don't know how to test metaClass property
    }

    /**
     * GROOVY-2150 ensure list call is available on closure
     */
    void testCallClosureWithlist() {
        def list = [1, 2]
        def cl = { a, b -> a + b }
        assert cl(list) == 3
    }

    /**
     * GROOVY-4484 ensure variable can be used in assignment inside closure
     */
    void testDeclarationOutsideWithAssignmentInsideAndReferenceInNestedClosure() {
        assertScript """
            class Dummy{}
            Dummy foo(arg){new Dummy()}

            def phasePicker
            def c = {
                phasePicker = foo(bar: {phasePicker})
            }

            assert c() instanceof Dummy
        """
    }

    void testIdentity() {
        assert IDENTITY(42) == 42
        assert IDENTITY([42, true, 'foo']) == [42, true, 'foo']

        def items = [0, 1, 2, '', 'foo', [], ['bar'], true, false]
        assert items.grep(IDENTITY) == [1, 2, 'foo', ['bar'], true]
        assert items.findAll(IDENTITY) == [1, 2, 'foo', ['bar'], true]
        assert items.grep(IDENTITY).groupBy(IDENTITY) == [1: [1], 2: [2], 'foo': ['foo'], ['bar']: [['bar']], (true): [true]]
        assert items.collect(IDENTITY) == items

        def twice = { it + it }
        def alsoTwice = twice >> IDENTITY
        assert alsoTwice(6) == 12
        def twiceToo = IDENTITY >> twice
        assert twiceToo(6) == 12

        def fortyTwo = IDENTITY.curry(42)
        assert fortyTwo() == 42
        def foo = IDENTITY.rcurry('foo')
        assert foo() == 'foo'

        def map = [a: 1, b: 2]
        assert map.collectEntries(IDENTITY) == map
    }

    void testEachWithArray() {
        def l = []
        l << ([1, 2] as Object[])
        l.each {
            assert it == [1, 2] as Object[]
        }
    }

    void testClosureDehydrateAndRehydrate() {
        def closure = { 'Hello' }
        assert closure.delegate != null
        assert closure.owner != null
        assert closure.thisObject != null
        assert closure() == 'Hello'

        def serializable = closure.dehydrate()
        assert !serializable.is(closure)
        assert serializable.delegate == null
        assert serializable.owner == null
        assert serializable.thisObject == null
        assert serializable() == 'Hello'

        def rehydrate = serializable.rehydrate(closure.delegate, closure.owner, closure.thisObject)
        assert !rehydrate.is(serializable)
        assert !rehydrate.is(closure)
        assert rehydrate.delegate.is(closure.delegate)
        assert rehydrate.owner.is(closure.owner)
        assert rehydrate.thisObject.is(closure.thisObject)
        assert rehydrate() == 'Hello'

    }

    // GROOVY-5151
    void testClosureSerialization() {
        // without dehydrate, as Controller is not serializable, the serialization will fail
        shouldFail(NotSerializableException) {
            assertScript '''
            class Controller { // not Serializable
                def action = { 'Hello' }
                def action2 = { action() } // call to other closure
            }
            def ctrl = new Controller()
            def bos = new ByteArrayOutputStream()
            bos.withObjectOutputStream {
                it << ctrl.action
            }
        '''
        }

        // dehydrated action1 should be serializable
        assertScript '''
            class Controller { // not Serializable
                def action = { 'Hello' }
                def action2 = { action() } // call to other closure
            }
            def ctrl = new Controller()
            def a1 = ctrl.action.dehydrate()
            def bos = new ByteArrayOutputStream()
            bos.withObjectOutputStream {
                it << a1
            }
        '''

        // dehydrated action2 should be serializable
        assertScript '''
            class Controller { // not Serializable
                def action = { 'Hello' }
                def action2 = { action() } // call to other closure
            }
            def ctrl = new Controller()
            def a2 = ctrl.action2.dehydrate()
            def bos = new ByteArrayOutputStream()
            bos.withObjectOutputStream {
                it << a2
            }
        '''

        // restore action
        assertScript '''
            class Controller { // not Serializable
                def action = { 'Hello' }
                def action2 = { action() } // call to other closure
            }
            def ctrl = new Controller()
            def a1 = ctrl.action.dehydrate()
            def bos = new ByteArrayOutputStream()
            bos.withObjectOutputStream {
                it << a1
            }
            byte[] arr = bos.toByteArray()
            def rehyd
            new ByteArrayInputStream(arr).withObjectInputStream(this.class.classLoader) {
                it.eachObject { o -> rehyd = o }
            }
            assert rehyd() == 'Hello'
        '''

        // restore action2
        assertScript '''
            class Controller { // not Serializable
                def action = { 'Hello' }
                def action2 = { action() } // call to other closure
            }
            def ctrl = new Controller()
            def a2 = ctrl.action2.dehydrate()
            def bos = new ByteArrayOutputStream()
            bos.withObjectOutputStream {
                it << a2
            }
            byte[] arr = bos.toByteArray()
            def rehyd
            new ByteArrayInputStream(arr).withObjectInputStream(this.class.classLoader) {
                it.eachObject { o -> rehyd = o }
            }
            ctrl = new Controller() // assert new instance
            rehyd = rehyd.rehydrate(ctrl,ctrl,ctrl)
            assert rehyd() == 'Hello'
        '''

        shouldFail(NotSerializableException) {
            assertScript '''
            class X{}
            def x = new X ()
            def cl = {x}
            def dehyd = cl.dehydrate() // true means to dehydrate non serializable fields too
            def bos = new ByteArrayOutputStream()
            bos.withObjectOutputStream {
              it << dehyd
            }
            '''
        }
    }

    // GROOVY-5875
    void testStaticInnerClassDelegateFirstAccess() {
        assertScript '''
             class Owner {
                 Object delegate
                 String ownerProp = "owner"

                 void run() {
                     def c = {
                         delegateProp = ownerProp
                     }
                     c.delegate = delegate
                     c.resolveStrategy = Closure.DELEGATE_FIRST
                     c()
                     assert c.delegate.delegateProp == ownerProp
                 }
             }

             class Container {
                 static class Delegate {
                      String delegateProp = "delegate"
                 }
             }

             def owner = new Owner()
             owner.delegate = new Container.Delegate()
             owner.run()
        '''
    }

    void testStaticInnerClassOwnerFirstAccess() {
        assertScript '''
             class Owner {
                 Object delegate
                 String ownerProp = "owner"

                 void run() {
                     def c = {
                         delegateProp = ownerProp
                     }
                     c.delegate = delegate
                     c.resolveStrategy = Closure.OWNER_FIRST
                     c()
                     assert c.delegate.delegateProp == ownerProp
                 }
             }

             class Container {
                 static class Delegate {
                      String delegateProp = "delegate"
                 }
             }

             def owner = new Owner()
             owner.delegate = new Container.Delegate()
             owner.run()
        '''
    }

    void testStaticInnerClassOwnerWithPropertyMissingImplementation() {
        def gcl = new GroovyClassLoader()
        def msg = shouldFail MultipleCompilationErrorsException, {
            gcl.parseClass('''
                public class ClosureTestA {
                    static class ClosureTestB {
                        def propertyMissing(String myName, Object myValue) {
                            return myValue
                        }

                        def propertyMissing(String myName) {
                            return 42
                        }

                        def methodMissing(String myName, Object myArgs) {
                            return 42
                        }
                    }
                }
            ''')
        }

        assert msg.contains('"methodMissing" implementations are not supported on static inner classes as a synthetic version of "methodMissing" is added during compilation for the purpose of outer class delegation.')
        assert msg.contains('"propertyMissing" implementations are not supported on static inner classes as a synthetic version of "propertyMissing" is added during compilation for the purpose of outer class delegation.')
    }

    void testInnerClassOwnerWithPropertyMissingImplementation() {
        def gcl = new GroovyClassLoader()
        gcl.parseClass('''
                public class ClosureTestA {
                    class ClosureTestB {
                        def propertyMissing(String myName, Object myValue) {
                            return myValue
                        }

                        def propertyMissing(String myName) {
                            return 42
                        }

                        def methodMissing(String myName, Object myArgs) {
                            return 42
                        }
                    }
                }
        ''')
    }

    void testStaticInnerClassHierarchyWithMethodMissing() {
        def gcl = new GroovyClassLoader()
        def msg = shouldFail MultipleCompilationErrorsException, {
            gcl.parseClass('''
                    public class ClosureTestA {
                        static class ClosureTestB {
                            def methodMissing(String myName, Object myArgs) {
                                return 42
                            }
                        }

                        static class ClosureTestB1 extends ClosureTestB {

                        }
                    }
            ''')
        }
        assert msg.contains('"methodMissing" implementations are not supported on static inner classes as a synthetic version of "methodMissing" is added during compilation for the purpose of outer class delegation.')
    }

    // GROOVY-6989
    void testEachCall() {
        assertScript '''
            Object[] arr = new Object[1]
            arr[0] = "1"
            List list = new ArrayList()
            list.add(arr)

            list.each { def obj ->
                assert obj[0] == "1"
            }

            list.each { Object[] obj ->
                assert obj[0] == "1"
            }
        '''
    }
}

public class TinyAgent {
    int x
}
