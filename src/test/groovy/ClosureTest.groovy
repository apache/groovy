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

import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.junit.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail
import static java.lang.reflect.Modifier.isPublic
import static java.lang.reflect.Modifier.isStatic

final class ClosureTest {

    private int count

    def incrementCallCount() {
        count += 1
    }

    def assertClosure(Closure block) {
        block.call(this)
    }

    protected void callBlock(int n, Closure block) {
        for (i in 0..n) {
            block.call(this)
        }
    }

    protected void callBlock2(n, block) {
        for (i in 0..n) {
            block.call(this)
        }
    }

    //--------------------------------------------------------------------------

    @Test
    void testSimpleBlockCall() {
        def c = { ClosureTest ct -> ct.incrementCallCount() }

        assertClosure(c)
        assert count == 1

        assertClosure { ClosureTest ct -> ct.incrementCallCount() }
        assert count == 2
    }

    @Test
    void testVariableLengthParameterList() {
        def c1 = { Object[] args -> args.each { count += it } }
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

    @Test
    void testBlockAsParameter() {
        callBlock(5, { ClosureTest ct -> ct.incrementCallCount() })
        assert count == 6

        callBlock2(5, { ClosureTest ct -> ct.incrementCallCount() })
        assert count == 12
    }

    @Test
    void testMethodClosure() {
        def block = this.&incrementCallCount

        block.call()

        assert count == 1

        block = Math.&min

        assert block.call(3, 7) == 3
    }

    private int numAgents = 4
    private boolean testDone = false
    private static class TinyAgent {
        int x
    }

    @Test
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

    // GROOVY-6989
    @Test
    void testEach() {
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

    @Test
    void testEachWithArray() {
        def l = []
        l << ([1, 2] as Object[])
        l.each {
            assert it == [1, 2] as Object[]
        }
    }

    @Test
    void testEachWithIndex() {
        def str = ''
        def sum = 0
        ['a', 'b', 'c', 'd'].eachWithIndex { item, index -> str += item; sum += index }
        assert str == 'abcd' && sum == 6
    }

    @Test
    void testMapEachWithIndex() {
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

    @Test
    void testMapEachWithIndexKV() {
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

    // GROOVY-2089: Closure properties
    @Test
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

        assert c.metaClass != c.getMetaClass() // no idea why this isn't equal
    }

    @Test
    void testGetPropertiesGenerically() {
        // ensure closure meta-class is the original
        Closure.metaClass = null

        Closure.metaClass.properties.each { property ->
            int modifiers = property.modifiers
            if (isPublic(modifiers) && !isStatic(modifiers)) {
                Closure closure = { -> }
                closure."$property.name" == closure.(MetaProperty.getGetterName(property.name, property.type))()
            }
        }
    }

    @Test
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

    // GROOVY-2150: ensure list call is available on closure
    @Test
    void testCallClosureWithList() {
        def list = [1, 2]
        def cl = { a, b -> a + b }
        assert cl(list) == 3
    }

    // GROOVY-4484: ensure variable can be used in assignment inside closure
    @Test
    void testDeclarationOutsideWithAssignmentInsideAndReferenceInNestedClosure() {
        assertScript '''
            class Dummy { }
            Dummy foo(arg){new Dummy()}

            def phasePicker
            def c = {
                phasePicker = foo(bar: {phasePicker})
            }

            assert c() instanceof Dummy
        '''
    }

    @Test
    void testIdentity() {
        def identity = Closure.IDENTITY

        assert identity(42) == 42
        assert identity([42, true, 'foo']) == [42, true, 'foo']

        def items = [0, 1, 2, '', 'foo', [], ['bar'], true, false]
        assert items.grep(identity) == [1, 2, 'foo', ['bar'], true]
        assert items.findAll(identity) == [1, 2, 'foo', ['bar'], true]
        assert items.grep(identity).groupBy(identity) == [1: [1], 2: [2], 'foo': ['foo'], ['bar']: [['bar']], (true): [true]]
        assert items.collect(identity) == items

        def twice = { it + it }
        def alsoTwice = twice >> identity
        assert alsoTwice(6) == 12
        def twiceToo = identity >> twice
        assert twiceToo(6) == 12

        def fortyTwo = identity.curry(42)
        assert fortyTwo() == 42
        def foo = identity.rcurry('foo')
        assert foo() == 'foo'

        def map = [a: 1, b: 2]
        assert map.collectEntries(identity) == map
    }

    @Test
    void testClosureDehydrateAndRehydrate() {
        def closure = { 'Hello' }
        assert closure.delegate != null
        assert closure.owner != null
        assert closure.thisObject != null
        assert closure() == 'Hello'

        def serializable = closure.dehydrate()
        assert serializable !== closure
        assert serializable.delegate == null
        assert serializable.owner == null
        assert serializable.thisObject == null
        assert serializable() == 'Hello'

        def rehydrate = serializable.rehydrate(closure.delegate, closure.owner, closure.thisObject)
        assert rehydrate !== serializable
        assert rehydrate !== closure
        assert rehydrate.delegate === closure.delegate
        assert rehydrate.owner === closure.owner
        assert rehydrate.thisObject === closure.thisObject
        assert rehydrate() == 'Hello'
    }

    // GROOVY-5151
    @Test
    void testClosureSerialization() {
        // without dehydrate, as Controller is not serializable, the serialization will fail
        shouldFail NotSerializableException, '''
            class Controller { // not Serializable
                def action = { 'Hello' }
                def action2 = { action() } // call to other closure
            }
            def ctrl = new Controller()
            def baos = new ByteArrayOutputStream()
            baos.withObjectOutputStream {
                it << ctrl.action
            }
        '''

        // dehydrated action1 should be serializable
        assertScript '''
            class Controller { // not Serializable
                def action = { 'Hello' }
                def action2 = { action() } // call to other closure
            }
            def ctrl = new Controller()
            def baos = new ByteArrayOutputStream()
            baos.withObjectOutputStream {
                it << ctrl.action.dehydrate()
            }
        '''

        // dehydrated action2 should be serializable
        assertScript '''
            class Controller { // not Serializable
                def action = { 'Hello' }
                def action2 = { action() } // call to other closure
            }
            def ctrl = new Controller()
            def baos = new ByteArrayOutputStream()
            baos.withObjectOutputStream {
                it << ctrl.action2.dehydrate()
            }
        '''

        // restore action
        assertScript '''
            class Controller { // not Serializable
                def action = { 'Hello' }
                def action2 = { action() } // call to other closure
            }
            def ctrl = new Controller()
            def baos = new ByteArrayOutputStream()
            baos.withObjectOutputStream {
                it << ctrl.action.dehydrate()
            }
            byte[] arr = baos.toByteArray()
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
            def baos = new ByteArrayOutputStream()
            baos.withObjectOutputStream {
                it << ctrl.action2.dehydrate()
            }
            byte[] arr = baos.toByteArray()
            def rehyd
            new ByteArrayInputStream(arr).withObjectInputStream(this.class.classLoader) {
                it.eachObject { o -> rehyd = o }
            }
            ctrl = new Controller() // assert new instance
            rehyd = rehyd.rehydrate(ctrl, ctrl, ctrl)
            assert rehyd() == 'Hello'
        '''

        shouldFail NotSerializableException, '''
            class X {}
            def x = new X()
            def cl = {x}
            def baos = new ByteArrayOutputStream()
            baos.withObjectOutputStream {
                it << cl.dehydrate()
            }
        '''
    }

    // GROOVY-5875
    @Test
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

    @Test
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

    @Test
    void testStaticInnerClassOwnerWithPropertyMissingImplementation() {
        def err = shouldFail MultipleCompilationErrorsException, '''
            class ClosureTestA {
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
        '''

        assert err.message.contains('"methodMissing" implementations are not supported on static inner classes as a synthetic version of "methodMissing" is added during compilation for the purpose of outer class delegation.')
        assert err.message.contains('"propertyMissing" implementations are not supported on static inner classes as a synthetic version of "propertyMissing" is added during compilation for the purpose of outer class delegation.')
    }

    @Test
    void testInnerClassOwnerWithPropertyMissingImplementation() {
        assertScript '''
            class ClosureTestA {
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

            def a = new ClosureTestA()
            def b = new ClosureTestA.ClosureTestB(a)
        '''
    }

    @Test
    void testStaticInnerClassHierarchyWithMethodMissing() {
        def err = shouldFail MultipleCompilationErrorsException, '''
            class ClosureTestA {
                static class ClosureTestB {
                    def methodMissing(String myName, Object myArgs) {
                        return 42
                    }
                }

                static class ClosureTestB1 extends ClosureTestB {
                }
            }
        '''

        assert err.message.contains('"methodMissing" implementations are not supported on static inner classes as a synthetic version of "methodMissing" is added during compilation for the purpose of outer class delegation.')
    }

    // GROOVY-3142
    @Test
    void testClosureAccessToEnclosingClassPrivateField() {
        assertScript '''
            class C {
                private String string = 'foo'
                def test(List<String> strings) {
                    strings.collect { this.@string + it }
                }
            }

            def result = new C().test(['bar','baz'])
            assert result == ['foobar','foobaz']

            class D extends C {
            }

            result = new D().test(['bar','baz'])
            assert result == ['foobar','foobaz']
        '''
    }
}
