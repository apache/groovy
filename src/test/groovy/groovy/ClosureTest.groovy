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

import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail
import static java.lang.reflect.Modifier.isPrivate
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
        def c = this.&incrementCallCount

        c.call()

        assert count == 1

        c = Math.&min

        assert c.call(3, 7) == 3
    }

    private int numAgents = 4
    private boolean testDone = false
    private static class TinyAgent {
        int x
    }

    @Test
    void testIntFieldAccess() {
        def agents = []
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
            Object[] array = new Object[]{"1"}
            List<Object[]> list = [array]

            list.each { item ->
                assert item[0] == "1"
            }

            list.each { Object[] item ->
                assert item[0] == "1"
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

    // GROOVY-12675
    @Test
    void testGetPropertiesExcludesPrivateMembersOfClosure() {
        Closure closure = { -> }
        def properties = closure.metaClass.properties

        assert properties.every { !isPrivate(it.modifiers) }
        assert properties*.name.containsAll(['class', 'delegate', 'directive', 'maximumNumberOfParameters',
                'owner', 'parameterTypes', 'resolveStrategy', 'thisObject', 'DONE', 'IDENTITY'])
        assert properties*.name.disjoint(['bcw', 'serialVersionUID', 'thisType'])
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

    // GROOVY-12673
    @Test
    void testWritableClosureThisObject() {
        def w = { x -> x }.asWritable()

        assert w.thisObject === this
        assert w.getThisObject() === this
        assert w.curry(1).getThisObject() === this
        assertScript '''
            @groovy.transform.CompileStatic
            def thisObjectOf(Closure c) { c.asWritable().thisObject }
            assert thisObjectOf { -> } === this
        '''
    }

    // GROOVY-2150: list items spread over closure parameters
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
    void testDehydrateAndRehydrate() {
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

    // GROOVY-12665
    @Test
    void testDehydrateAndRehydrateWritableClosure() {
        assertScript '''
            class O { def foo() { 'o' } }
            class D { def foo() { 'd' } }
            def o = new O(), d = new D()
            def w = { x -> foo() + x }.asWritable()

            def r = w.rehydrate(d, o, o)
            assert r !== w
            assert r instanceof Writable
            assert r.delegate === d
            assert r.owner === o
            assert r.thisObject === o
            assert r(1) == 'o1'
            assert w.curry(1).rehydrate(d, o, o)() == 'o1'
            assert w.delegate === this
            assert w.owner === this

            def k = { x -> x + 1 }.asWritable().dehydrate()
            assert k instanceof Writable
            assert k.delegate === null
            assert k.owner === null
            assert k.thisObject === null
            assert k(1) == 2
            assert k.rehydrate(d, o, o)(1) == 2
        '''
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

    @Test
    void testCallablePropertyFallback() {
        assertScript '''
            class C {
                def getX() {
                    return { -> 'works' }
                }
            }

            Object result = new C().with { x() }
            assert result == 'works'
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

    // GROOVY-10943
    @Test
    void testClosureUnderscorePlaceholder() {
        assertScript '''
            def e = { a, b, _, _ -> a + b }
            def f = { a, _, b, _ -> a + b }
            def g = { _, a, _, b -> a + b }
            assert e(1000, 100, 10, 1) == 1100
            assert f(1000, 100, 10, 1) == 1010
            assert g(1000, 100, 10, 1) == 101
        '''
    }

    // GROOVY-12680
    @Test
    void testMetaClassDoesNotExposePrivateMembersOfClosure() {
        Closure c = { -> 1 }
        def mc = c.metaClass

        for (name in ['bcw', 'mopPerturbed', 'serialVersionUID', 'thisType', 'ThisType', 'PROPERTY_GETTERS']) {
            def err = shouldFail(MissingPropertyException) {
                mc.getProperty(c, name)
            }
            assert err.property == name
            assert err.type == c.getClass()

            err = shouldFail(MissingPropertyException) {
                mc.setProperty(c, name, null)
            }
            assert err.property == name
            assert err.type == c.getClass()

            assert mc.getMetaProperty(name) == null
            assert mc.hasProperty(c, name) == null
        }

        // access from within Closure itself, like any other private member
        assert mc.getProperty(Closure, c, 'serialVersionUID', false, true) instanceof Long
    }

    // GROOVY-12680
    @Test
    void testMetaClassCannotOverwritePrivateMembersOfClosure() {
        assertScript '''
            import static groovy.test.GroovyAssert.shouldFail

            class C {
                private secret = 'C'
                def read() {
                    Closure c = { -> secret }
                    def err = shouldFail(MissingPropertyException) {
                        c.metaClass.setProperty(c, 'thisType', Object)
                    }
                    assert err.property == 'thisType'
                    c()
                }
            }
            class D extends C {
            }

            assert new D().read() == 'C'

            Closure c = { -> 1 }
            def err = shouldFail(MissingPropertyException) {
                c.metaClass.setProperty(c, 'mopPerturbed', true)
            }
            assert err.property == 'mopPerturbed'
        '''
    }

    // GROOVY-12680
    @Test
    void testToSelfDoesNotResolvePrivateMembersOfClosure() {
        Closure c = { -> 1 }
        c.resolveStrategy = Closure.TO_SELF

        for (name in ['bcw', 'mopPerturbed', 'serialVersionUID', 'thisType']) {
            def err = shouldFail(MissingPropertyException) {
                c."$name"
            }
            assert err.property == name

            err = shouldFail(MissingPropertyException) {
                c."$name" = null
            }
            assert err.property == name
        }

        assert c.DONE == Closure.DONE
        assert c.owner === this
        assert c.resolveStrategy == Closure.TO_SELF
        assert c() == 1
    }

    // GROOVY-12675, GROOVY-12680: the property list and the property lookups of a closure's
    // metaclass agree, for every private field of Closure that has no public accessor
    @Test
    void testMetaClassPropertyListAndLookupsAgree() {
        Closure c = { -> 1 }
        def mc = c.metaClass
        def listed = mc.properties*.name as Set

        for (name in listed) {
            assert mc.getMetaProperty(name) != null
            assert mc.hasProperty(c, name) != null
        }

        def hasAccessor = { String name ->
            Closure.methods.any { it.parameterCount == 0 && it.name in ['get' + name.capitalize(), 'is' + name.capitalize()] }
        }
        def privateFields = Closure.declaredFields.findAll { isPrivate(it.modifiers) && !it.synthetic && !hasAccessor(it.name) }*.name
        assert privateFields.containsAll(['bcw', 'mopPerturbed', 'serialVersionUID', 'thisType'])

        for (name in privateFields) {
            assert !(name in listed)
            assert mc.getMetaProperty(name) == null
            assert mc.hasProperty(c, name) == null
            def err = shouldFail(MissingPropertyException) {
                mc.getProperty(c, name)
            }
            assert err.property == name
            err = shouldFail(MissingPropertyException) {
                mc.setProperty(c, name, null)
            }
            assert err.property == name
        }
    }

    // GROOVY-12680
    @Test
    void testMetaClassStillExposesPublicMembersOfClosure() {
        Closure c = { String s, int i -> }
        def mc = c.metaClass

        assert mc.getProperty(c, 'delegate') === this
        assert mc.getProperty(c, 'owner') === this
        assert mc.getProperty(c, 'thisObject') === this
        assert mc.getProperty(c, 'resolveStrategy') == Closure.OWNER_FIRST
        assert mc.getProperty(c, 'directive') == 0
        assert mc.getProperty(c, 'maximumNumberOfParameters') == 2
        assert mc.getProperty(c, 'parameterTypes') == [String, int] as Class[]
        assert mc.getProperty(c, 'class') == c.getClass()
        assert mc.getProperty(c, 'metaClass') != null
        assert mc.getProperty(c, 'DONE') == Closure.DONE
        assert mc.getProperty(c, 'OWNER_FIRST') == Closure.OWNER_FIRST
        assert mc.getProperty(c, 'Owner') === this

        for (name in ['class', 'delegate', 'directive', 'maximumNumberOfParameters', 'metaClass',
                'owner', 'parameterTypes', 'resolveStrategy', 'thisObject', 'DONE', 'IDENTITY']) {
            assert mc.getMetaProperty(name)?.name == name
            assert mc.hasProperty(c, name)?.name == name
        }

        def object = new Object()
        mc.setProperty(c, 'delegate', object)
        mc.setProperty(c, 'resolveStrategy', Closure.DELEGATE_FIRST)
        mc.setProperty(c, 'directive', Closure.DONE)
        assert c.delegate === object
        assert c.resolveStrategy == Closure.DELEGATE_FIRST
        assert c.directive == Closure.DONE
    }

    // GROOVY-12680
    @Test
    void testOwnerAndDelegatePropertiesNamedLikePrivateMembersOfClosure() {
        assertScript '''
            class C {
                private String thisType = 'owner'
                def bcw = 'owner'

                def run() {
                    Closure c = { -> thisType + ':' + bcw + ':' + mopPerturbed }
                    c.delegate = [mopPerturbed: 'delegate']
                    def result = c()
                    Closure d = { -> bcw = 'set'; mopPerturbed = 'set' }
                    d.delegate = [:]
                    d.resolveStrategy = Closure.DELEGATE_FIRST
                    d()
                    [result, bcw, d.delegate.mopPerturbed]
                }
            }

            assert new C().run() == ['owner:owner:delegate', 'owner', 'set']
        '''
    }

    // GROOVY-2433, GROOVY-3073, GROOVY-9987, GROOVY-11128
    @ParameterizedTest
    @ValueSource(strings=['this.', 'owner.', 'thisObject.', ''])
    void testClosureAccessToEnclosingClassPrivateMethod(String who) {
        assertScript """
            class C {
                def getIds() {
                    populateIds()
                }
                def populateIds = { ->
                    ${who}sort([ 1, 5, 3, 4, 2 ])
                }
                private sort(list) {
                    list.sort{ one, two -> one <=> two }
                }
            }

            class D extends C {
                void test() {
                    assert ids == [1,2,3,4,5]
                }
            }

            new D().test()
        """

        assertScript """
            class C {
                protected String protectedMethod() {
                    def closure = { ->
                        ${who}privateMethod()
                    }
                    closure()
                }
                private String privateMethod() {
                    'hello world'
                }
            }

            class D extends C {
                void test() {
                    def result = protectedMethod()
                    assert result == 'hello world'
                }
            }

            new D().test()
        """

        assertScript """
            class C {
                def publicMethod() {
                    [1].each {
                        ${who}privateStaticMethod()
                    }
                }
                private static privateStaticMethod() {
                    'hello world'
                }
            }

            class D extends C {
                void test() {
                    publicMethod()
                }
            }

            new D().test()
        """
    }

    // GROOVY-3142, GROOVY-5438, GROOVY-6335, GROOVY-11128
    @ParameterizedTest
    @ValueSource(strings=['this.@', 'this.', 'owner.', 'thisObject.', ''])
    void testClosureAccessToEnclosingClassPrivateField(String who) {
        assertScript """
            class C {
                String data
                C(arg) {
                    arg.each() { ${who}data = it } // MissingFieldException
                }
            }

            class D extends C {
                D(arg) {
                    super(arg)
                }
            }

            new D(["test"])
        """

        assertScript """
            class C {
                private String data
                C(arg) {
                    arg.each() { ${who}data = it } // ReadOnlyPropertyException
                }
                String getData() { this.@data }
                private void setData(String value) { this.@data = value }
            }

            class D extends C {
                D(arg) {
                    super(arg)
                }
            }

            new D(["test"])
        """

        assertScript """
            class C {
                private String string = 'foo'
                def test(List<String> strings) {
                    strings.collect { ${who}string + it }
                }
            }

            def result = new C().test(['bar','baz'])
            assert result == ['foobar','foobaz']

            class D extends C {
            }

            result = new D().test(['bar','baz'])
            assert result == ['foobar','foobaz']
        """

        assertScript """
            @groovy.util.logging.Log
            class C {
                void test() {
                    1.times {
                        ${who}log.info('sth')
                    }
                }
            }

            class D extends C {
            }

            new D().test()
        """
    }

    // GROOVY-12671
    @Test
    void testOwnerFirstWithNullOwnerProperty() {
        assertScript '''
            class D {
                def bar = 'delegate'
            }
            class C {
                def test() {
                    def d = new D()
                    def c = { -> [bar, bar = 'set', bar += '!'] }.rehydrate(d, null, null)
                    assert c.resolveStrategy == Closure.OWNER_FIRST
                    [c(), d.bar]
                }
            }

            assert new C().test() == [['delegate', 'set', 'set!'], 'set!']
        '''
    }

    // GROOVY-12671
    @Test
    void testOwnerFirstWithNullOwnerPropertyTypeChecked() {
        assertScript '''
            class D {
                def bar = 'delegate'
            }
            @groovy.transform.TypeChecked
            class C {
                String bar = 'owner'
                def test() {
                    def d = new D()
                    def c = { -> bar = bar + '!' }.rehydrate(d, null, null)
                    c()
                    [d.bar, bar]
                }
            }

            assert new C().test() == ['delegate!', 'owner']
        '''
    }

    // GROOVY-12671
    @Test
    void testDehydratedClosureWithDelegateProperty() {
        assertScript '''
            class D {
                def bar = 'delegate'
            }
            class C {
                def test() {
                    def c = { -> bar }.dehydrate()
                    c.delegate = new D()
                    c()
                }
            }

            assert new C().test() == 'delegate'
        '''
    }

    // GROOVY-12671
    @ParameterizedTest
    @ValueSource(strings=['OWNER_FIRST', 'DELEGATE_FIRST', 'OWNER_ONLY', 'DELEGATE_ONLY'])
    void testMissingPropertyWithNullOwnerAndDelegate(String strategy) {
        for (body in ['bar', 'bar = 1']) {
            def err = shouldFail MissingPropertyException, """
                def c = { -> $body }.dehydrate()
                c.resolveStrategy = Closure.$strategy
                c()
            """
            assert err.property == 'bar'
        }
    }

    // GROOVY-12671
    @ParameterizedTest
    @ValueSource(strings=['OWNER_ONLY', 'DELEGATE_ONLY'])
    void testMissingPropertyWithNullOnlyCandidate(String strategy) {
        def args = strategy == 'OWNER_ONLY' ? 'new D(), null, null' : 'null, new D(), null'
        for (body in ['bar', 'bar = 1']) {
            def err = shouldFail MissingPropertyException, """
                class D {
                    def bar = 'not the target'
                }
                def c = { -> $body }.rehydrate($args)
                c.resolveStrategy = Closure.$strategy
                c()
            """
            assert err.property == 'bar'
        }
    }

    // GROOVY-12671
    @ParameterizedTest
    @ValueSource(strings=['OWNER_FIRST', 'DELEGATE_FIRST'])
    void testMissingPropertyWithNullSecondCandidate(String strategy) {
        def args = strategy == 'OWNER_FIRST' ? 'null, new D(), null' : 'new D(), null, null'
        for (body in ['nope', 'nope = 1']) {
            def err = shouldFail MissingPropertyException, """
                class D {
                }
                def c = { -> $body }.rehydrate($args)
                c.resolveStrategy = Closure.$strategy
                c()
            """
            assert err.property == 'nope'
            assert err.type.name == 'D'
        }
    }
}
