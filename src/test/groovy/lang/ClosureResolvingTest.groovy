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
package groovy.lang

import org.junit.After
import org.junit.Ignore
import org.junit.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

/**
 * Tests how closures resolve to either a delegate or an owner for a given resolveStrategy
 *
 * @since 1.5
 */
final class ClosureResolvingTest {

    def foo = 'bar'
    def bar = 'foo'

    def doStuff() { 'stuff' }

    static class TestResolve1 {
        def foo = 'hello'

        def doStuff() { 'foo' }
    }

    static class TestResolve2 {
        def foo = 'hello'
        def bar = 'world'

        def doStuff() { 'bar' }
    }

    //--------------------------------------------------------------------------

    @After
    void tearDown() {
        Closure.metaClass = null
    }

    @Test
    void testResolveToSelf() {
        def c = { foo }
        assert c.call() == 'bar'

        c.resolveStrategy = Closure.TO_SELF

        shouldFail {
            c.call()
        }

        def metaClass = c.class.metaClass
        metaClass.getFoo = {-> 'hello!' }

        c.metaClass = metaClass

        assert c.call() == 'hello!'

        c = { doStuff() }
        c.resolveStrategy = Closure.TO_SELF
        shouldFail {
            c.call()
        }
        metaClass = c.class.metaClass
        metaClass.doStuff = {-> 'hello' }
        c.metaClass = metaClass

        assert c.call() == 'hello'
    }

    @Test
    void testResolveDelegateFirst() {

        def c = { foo }

        assert c.call() == 'bar'

        c.resolveStrategy = Closure.DELEGATE_FIRST
        c.delegate = [foo: 'hello!']

        assert c.call() == 'hello!'


        c = { doStuff() }
        c.resolveStrategy = Closure.DELEGATE_FIRST

        assert c.call() == 'stuff'
        c.delegate = new TestResolve1()
        assert c.call() == 'foo'
    }

    @Test // GROOVY-7701
    void testResolveDelegateFirst2() {
        assertScript '''
            class Foo {
                List type
            }
            class Bar {
                int type = 10
                List<Foo> something = { ->
                    List<Foo> tmp = []
                    def foo = new Foo()
                    foo.with {
                        type = ['String']
                    //  ^^^^ should be Foo.type, not Bar.type
                    }
                    tmp.add(foo)
                    return tmp
                }()
            }

            def bar = new Bar()
            assert bar.type == 10
            assert bar.something*.type == [['String']]
            assert bar.type == 10
        '''
    }

    @Test
    void testResolveOwnerFirst() {
        def c = { foo }

        assert c.call() == 'bar'

        c.delegate = [foo: 'hello!']

        assert c.call() == 'bar'

        c = { doStuff() }
        c.delegate = new TestResolve1()
        assert c.call() == 'stuff'
    }

    @Test
    void testResolveDelegateOnly() {
        def c = { foo + bar }

        assert c.call() == 'barfoo'

        c.resolveStrategy = Closure.DELEGATE_FIRST

        c.delegate = new TestResolve1()

        assert c.call() == 'hellofoo'

        c.resolveStrategy = Closure.DELEGATE_ONLY
        shouldFail {
            c.call()
        }

        c.delegate = new TestResolve2()

        assert c.call() == 'helloworld'

        c = { doStuff() }
        c.resolveStrategy = Closure.DELEGATE_ONLY
        c.delegate = new TestResolve1()
        assert c.call() == 'foo'
    }

    @Test
    void testResolveOwnerOnly() {
        def c = { foo + bar }

        assert c.call() == 'barfoo'
        c.resolveStrategy = Closure.OWNER_ONLY

        c.delegate = new TestResolve2()
        assert c.call() == 'barfoo'

        c = { doStuff() }
        assert c.call() == 'stuff'
        c.resolveStrategy = Closure.OWNER_ONLY
        c.delegate = new TestResolve1()
        assert c.call() == 'stuff'
    }

    @Test
    void testOwnerDelegateChain() {
        assertScript '''
            class TestResolve3 {
                def del
                String toString() { del }
                def whoisThis() { return this }
                def met() { return "I'm the method inside '$del'" }
            }

            def outerdel = new TestResolve3(del: "outer delegate")
            def innerdel = new TestResolve3(del: "inner delegate")

            def cout = {
                assert delegate == outerdel
                assert delegate.whoisThis() == outerdel
                assert delegate.del == "outer delegate"
                assert delegate.met() == "I'm the method inside 'outer delegate'"

                assert whoisThis() == outerdel
                assert del == "outer delegate"
                assert met() == "I'm the method inside 'outer delegate'"

                def cin = {
                    assert delegate == innerdel
                    assert delegate.whoisThis() == innerdel
                    assert delegate.del == "inner delegate"
                    assert delegate.met() == "I'm the method inside 'inner delegate'"

                    assert whoisThis() == outerdel
                    assert del == "outer delegate"
                    assert met() == "I'm the method inside 'outer delegate'"

                }

                cin.delegate = innerdel
                cin()
            }

            cout.delegate = outerdel
            cout()
        '''
    }

    @Test // GROOVY-7232
    void testOwnerDelegateChain2() {
        assertScript '''
            def outer = { ->
                def inner = { ->
                    [x, keySet()]
                }
                inner.resolveStrategy = Closure.DELEGATE_ONLY
                inner.delegate = [x: 1, f: 0]
                inner.call()
            }
            //outer.resolveStrategy = Closure.OWNER_FIRST
            outer.delegate = [x: 0, g: 0]
            def result = outer.call()

            assert result.flatten() == [1, 'x', 'f']
        '''
    }

    @Test // GROOVY-7232
    void testOwnerDelegateChain3() {
        assertScript '''
            def outer = { ->
                def inner = { ->
                    def inner_inner = { ->
                        [x, keySet()]
                    }
                    //inner_inner.resolveStrategy = Closure.OWNER_FIRST
                    return inner_inner.call()
                }
                inner.resolveStrategy = Closure.DELEGATE_ONLY
                inner.delegate = [x: 1, f: 0]
                inner()
            }
            //outer.resolveStrategy = Closure.OWNER_FIRST
            outer.delegate = [x: 0, g: 0]
            def result = outer.call()

            assert result.flatten() == [1, 'x', 'f']
        '''
    }

    @Ignore @Test // GROOVY-7232
    void testOwnerDelegateChain4() {
        assertScript '''
            @GrabResolver(name='grails', root='https://repo.grails.org/grails/core')
            @Grab('org.grails:grails-web-url-mappings:4.0.1')
            @GrabExclude('org.codehaus.groovy:*')
            import grails.web.mapping.*

            def linkGenerator = new LinkGeneratorFactory().create { ->
                group('/g') { ->
                    '/bars'(resources: 'bar') { ->
                        owner.owner.collection { ->                             // TODO: remove qualifier
                            '/baz'(controller: 'bar', action: 'baz')
                        }
                    }
                }
            }

            def link = linkGenerator.link(controller: 'bar', action: 'baz', params: [barId: 1])
            assert link == 'http://localhost/g/bars/1/baz'
        '''
    }

    @Test // GROOVY-2686
    void testDelegateClosureProperty() {
        assertScript '''
            def c = { -> p() }
            c.delegate = [p: { -> 'value' }]

            assert c() == 'value'
        '''
    }
}
