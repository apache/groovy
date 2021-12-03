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

import org.junit.Ignore
import org.junit.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

final class ThisAndSuperTest {

    @Test
    void testOverwrittenSuperMethod() {
        def helper = new TestForSuperHelper2()
        assert helper.foo() == 2
        assert helper.callFooInSuper() == 1
    }

    @Test
    void testClosureUsingSuperAndThis() {
        def helper = new TestForSuperHelper2()
        assert helper.aClosureUsingThis() == 2
        assert helper.aClosureUsingSuper() == 1
        // accessing private method should not be changed
        // by a public method of the same name and signature!
        assert helper.closureUsingPrivateMethod() == 'bar'
        assert helper.bar() == 'no bar'

        assert helper.aField == 'I am a field'
        helper.closureFieldAccessUsingImplicitThis(1)
        assert helper.aField == 1
        helper.closureFieldAccessUsingExplicitThis(2)
        assert helper.aField == 2
    }

    @Test
    void testClosureDelegateAndThis() {
        def map = [:]
        def helper = new TestForSuperHelper2()

        helper.aField = 'I am a field'
        helper.closureFieldAccessUsingExplicitThis.delegate = map
        helper.closureFieldAccessUsingExplicitThis(3)
        assert helper.aField == 3
        assert map.aField == null

        helper.aField = 'I am a field'
        helper.closureFieldAccessUsingImplicitThis.delegate = map
        helper.closureFieldAccessUsingImplicitThis(4)
        assert helper.aField == 4
        assert map.aField == null

        def closure = {this.foo = 1}
        shouldFail {
            closure()
        }
        closure.delegate = map
        shouldFail {
            closure()
        }
        assert map.foo == null

        closure = {foo = 1}
        shouldFail {
            closure()
        }
        closure.delegate = map
        closure()
        assert map.foo == 1
    }

    @Test
    void testConstructorChain() {
        assertScript '''
            class TestForSuperHelper3 {
                def x

                TestForSuperHelper3(int i) {
                    this("1")
                    x = 1
                }

                TestForSuperHelper3(Object j) {
                    x = "Object"
                }
            }

            class TestForSuperHelper4 extends TestForSuperHelper3 {
                TestForSuperHelper4() {
                    super(1)
                }

                TestForSuperHelper4(Object j) {
                    super(j)
                }
            }

            def helper = new TestForSuperHelper4()
            assert helper.x == 1
            helper = new TestForSuperHelper4("foo")
            assert helper.x == "Object"
        '''
    }

    @Test
    void testSuperAsTypeChain() {
        assertScript '''
            class C {
                def <T> T asType(Class<T> type) {
                    if (type == Object[]) {
                        return [this] as Object[]
                    }
                    return super.asType(type)
                }
            }

            def x = new C()
            def y = x as Object[]
            assert y instanceof Object[]
            assert y[0] === x

            try {
                x as Integer
                assert false : 'should fail'
            } catch (ClassCastException cce) {
            }
        '''
    }

    @Test
    void testSuperEachChain() {
        assertScript '''
            class C {
                def out = []
                def each(Closure c) {
                    out << "start each in subclass"
                    super.each(c)
                    out << "end of each in subclass"
                }
            }

            def x = new C()
            x.each {
                x.out << "I am ${it.class.name}"
            }

            assert x.out.size() == 3
            assert x.out[0] == "start each in subclass"
            assert x.out[1] == "I am C"
            assert x.out[2] == "end of each in subclass"
        '''
    }

    @Ignore @Test // GROOVY-5285
    void testSuperSetMetaClassChain() {
        assertScript '''
            class C {
                void setMetaClass(MetaClass metaClass) {
                    super.setMetaClass(metaClass)
                }
            }

            def x = new C()
            x.metaClass = x.metaClass
        '''
    }

    @Test // GROOVY-9884
    void testSuperSetPropertyChain() {
        assertScript '''
            class A {
                def p = "p"
            }
            class B extends A {
            }
            class C extends B {
                void setProperty(String name, Object value) {
                    super.setProperty(name, value)
                }
            }

            def x = new C()
            x.setProperty("p", "q")
            assert x.p == "q"
        '''
    }

    @Test // GROOVY-2555
    void testSuperDotAbstractMethodShouldBeTreatedLikeMissingMethod() {
        shouldFail MissingMethodException, '''
            abstract class A {
                abstract void m()
            }
            class C extends A {
                void m() {
                    super.m()
                }
            }
            new C().m()
        '''
    }

    @Test // GROOVY-4945
    void testSuperDotSelfMethod1() {
        def err = shouldFail MissingMethodException, '''
            class C {
                void test() {
                    super.whatever()
                }
                void whatever() {
                    assert false : 'should not have been called!'
                }
            }
            new C().test()
        '''
        assert err =~ /No signature of method: java\.lang\.Object\.whatever\(\) is applicable for argument types: \(\) values: \[\]/
    }

    @Test // GROOVY-9615
    void testSuperDotSelfMethod2() {
        def err = shouldFail MissingMethodException, '''
            class Outer {
                class Inner {
                    void test() {
                        super.whatever()
                    }
                }
                void whatever() {
                    assert false : 'should not have been called!'
                }
            }
            new Outer.Inner(new Outer()).test()
        '''
        assert err =~ /No signature of method: java\.lang\.Object\.whatever\(\) is applicable for argument types: \(\) values: \[\]/
    }

    @Test // GROOVY-6001
    void testSuperDotMethod1() {
        assertScript '''
            class The {
                static log = ""
            }

            interface PortletRequest {}
            interface PortletResponse {}
            interface HttpServletRequest {}
            interface HttpServletResponse {}
            class HttpServletRequestWrapper implements HttpServletRequest {}
            class PortletRequestImpl extends HttpServletRequestWrapper implements PortletRequest {}
            class ClientDataRequestImpl extends PortletRequestImpl {}
            class ResourceRequestImpl extends ClientDataRequestImpl {}
            class Application {}
            interface HttpServletRequestListener {
                void onRequestStart(HttpServletRequest request, HttpServletResponse response);
                void onRequestEnd(HttpServletRequest request, HttpServletResponse response);
            }
            interface PortletRequestListener {
                void onRequestStart(PortletRequest request, PortletResponse response);
                void onRequestEnd(PortletRequest request, PortletResponse response);
            }

            class FooApplication extends Application implements HttpServletRequestListener, PortletRequestListener{
                void onRequestStart(HttpServletRequest request, HttpServletResponse response) {
                    The.log += "FooApplication.onRequestStart(HttpServletRequest, HttpServletResponse)"
                }
                void onRequestEnd(HttpServletRequest request, HttpServletResponse response) {
                    The.log += "FooApplication.onRequestEnd(HttpServletRequest, HttpServletResponse)"
                }
                void onRequestStart(PortletRequest request, PortletResponse response) {
                    The.log += "FooApplication.onRequestStart(PortletRequest, PortletResponse)"
                }
                void onRequestEnd(PortletRequest request, PortletResponse response) {
                    The.log += "FooApplication.onRequestEnd(PortletRequest, PortletResponse)"
                }
            }

            class BazApplication extends FooApplication {
                void onRequestStart(PortletRequest request, PortletResponse response) {
                    The.log += 'BazApplication.onRequestStart(PortletRequest, PortletResponse)'
                    super.onRequestStart(request, response);
                }
            }

            BazApplication application = new BazApplication()
            The.log = ""
            PortletRequest request = new ResourceRequestImpl()
            application.onRequestStart(request, null)
            assert The.log == "BazApplication.onRequestStart(PortletRequest, PortletResponse)FooApplication.onRequestStart(PortletRequest, PortletResponse)"
        '''
    }

    @Test // GROOVY-7655
    void testSuperDotMethod2() {
        assertScript '''
            class A {
                def myMethod(item) {
                    aCalled = true
                    item+"A"
                }
                protected boolean aCalled
            }

            class B extends A {
                def myMethod(item) {
                    bCalled = true
                    super.myMethod(item+"B")
                }
                protected boolean bCalled
            }

            class C extends B {
                def cMethod(item) {
                    cCalled = true
                    super.myMethod(item+"C")
                }
                protected boolean cCalled
            }

            def c = new C()
            c.cMethod("")

            assert c.aCalled
            assert c.bCalled
            assert c.cCalled
        '''
    }

    @Test // GROOVY-7655
    void testSuperDotMethod3() {
        assertScript '''
            class A {
                def myMethod(item) {
                    aCalled = true
                    item+"A"
                }
                protected boolean aCalled
            }

            class B extends A {
                def myMethod(item) {
                    bCalled = true
                    super.myMethod(item+"B")
                }
                protected boolean bCalled
            }

            class C extends B {
            }

            class D extends C {
                def dMethod( def item ) {
                    dCalled = true
                    super.myMethod(item+"D")
                }
                protected boolean dCalled
            }

            def d = new D()
            d.dMethod("")

            assert d.aCalled
            assert d.bCalled
            assert d.dCalled
        '''
    }

    @Test // GROOVY-10302
    void testSuperDotMethod4() {
        shouldFail ClassNotFoundException, '''
            void test() {
                def list = []
                def loader = new GroovyClassLoader() {
                    @Override
                    protected Class<?> findClass(String name) throws ClassNotFoundException {
                        list.add(name); super.findClass(name)
                    }
                }
                try (loader) {
                    loader.findClass('foo.bar.Baz')
                } finally {
                    assert 'foo.bar.Baz' in list
                }
            }
            test()
        '''
    }

    @Test // GROOVY-9851
    void testPrivateSuperMethod() {
        shouldFail MissingMethodException, '''
            abstract class A {
                private x() {
                }
            }
            class C extends A {
                void m() {
                    super.x()
                }
            }
            new C().m()
        '''
    }

    @Test // GROOVY-8999
    void testPrivateSuperField1() {
        def err = shouldFail MissingFieldException, '''
            abstract class A {
                private x = 1
                def getX() { 2 }
            }
            class C extends A {
                private x = 3
                def m() { super.@x }
            }
            new C().m()
        '''

        assert err =~ /No such field: x for class: A/
    }

    @Test // GROOVY-8999
    void testPrivateSuperField2() {
        def err = shouldFail MissingFieldException, '''
            abstract class A {
                private x = 1
                def getX() { 2 }
                void setX(x) { this.x = 3 }
            }
            class C extends A {
                private x = 4
                def m() { super.@x = 5; return x }
            }
            new C().m()
        '''

        assert err =~ /No such field: x for class: A/
    }

    // https://github.com/apache/groovy/commit/b62e4d3165b4d899a3b6c71dba2858c9362b2e1b
    @Test // TODO: Does this belong in another test suite?
    void testStaticMetaClassClosure() {
        assertScript '''
            class A {
            }
            A.metaClass.static.something << { -> '123' }

            assert A.something() == '123'
        '''
    }
}

//------------------------------------------------------------------------------

class TestForSuperHelper1 {
    def foo() {1}

    private bar() {'bar'}

    def closureUsingPrivateMethod() {bar()}
}

class TestForSuperHelper2 extends TestForSuperHelper1 {
    def foo() {2}

    def callFooInSuper() {super.foo()}

    def aClosureUsingSuper = {super.foo()}
    def aClosureUsingThis = {this.foo()}

    def bar() {'no bar'}

    public aField = 'I am a field'
    def closureFieldAccessUsingImplicitThis = {x -> aField = x}
    def closureFieldAccessUsingExplicitThis = {x -> this.aField = x}
}
