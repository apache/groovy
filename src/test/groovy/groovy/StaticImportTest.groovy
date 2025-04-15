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

import static groovy.API.*
import static groovy.Container5087.*
import static groovy.Foo4964.*
import static groovy.Outer1.*
import static groovy.Outer2.Inner2
import static groovy.Outer2.Inner2 as InnerAlias2
import static groovy.StaticImportChild.*
import static groovy.StaticImportTarget.x
import static groovy.StaticImportTarget.z
import static java.lang.Boolean.FALSE as F
import static java.lang.Math.*
import static java.text.DateFormat.MEDIUM
import static java.text.DateFormat.MEDIUM as M
import static java.util.Calendar.getInstance as now
import static java.util.jar.Attributes.*
import static java.util.jar.Attributes.Name as AttrName
import static java.util.regex.Pattern.*
import static junit.framework.Assert.format

import org.codehaus.groovy.runtime.DefaultGroovyMethods as DGM

final class StaticImportTest extends groovy.test.GroovyTestCase {

    void testFieldWithAliasInExpression() {
        assert !F
    }

    void testMethodAndField() {
        assert cos(2 * PI) == 1.0
    }

    static myStaticMethod() {
        cos(2 * PI)
    }

    void testMethodAndFieldInStaticContext() {
        assert myStaticMethod() == 1.0
    }

    void testMethodAndFieldInClosure() {
        def closure = { cos(2 * PI) }
        assert closure() == 1.0
    }

    void testFieldAsObjectExpression() {
        assert PI.equals(Math.PI)
    }

    void testFieldAsArgumentList() {
        assert ('' + PI.toString()).contains('3.14')
    }

    void testFieldAliasing() {
        assert MEDIUM == M
    }

    // GROOVY-1809
    void testMethodAliasing() {
        // making this not possible on one line?
        def now = now().time
        assert now.class == Date
    }

    void testWildCardAliasing() {
        assert MULTILINE == java.util.regex.Pattern.MULTILINE
    }

    private format(a, b, c, ignored) { format(a, b, c) }

    void testMethodDefCanUseStaticallyImportedMethodWithSameNameButDiffArgs() {
        assert format('different', 'abc', 'aBc', 3) == 'different expected:<abc> but was:<aBc>'
    }

    void testAssertEqualsFromJUnit() {
        double[] values = [3.9999, 4.0001, 0.00021, 0.00019]
        assertEquals(values[0], values[1], values[2])
        shouldFail(junit.framework.AssertionFailedError) {
            assertEquals(values[0], values[1], values[3])
        }
    }

    void testStaticImportFromGroovy() {
        def nonstaticval = new StaticImportTarget().y('he', 3)
        def staticval = x('he', 3)
        assert nonstaticval == staticval
    }

    void testStaticImportWithVarArgs() {
        assert noArrayMethod('one', 1) == 'noArrayMethod(one, 1)'
        assert API.arrayMethod('two', 1, 2, 3) == 'arrayMethod(two, 1, 2, 3)'
        assert arrayMethod('three', 1, 2, 3) == 'arrayMethod(three, 1, 2, 3)'
    }

    void testStaticImportFromParentClass() {
        assert cmethod() == 'hello from child'
        assert pmethod() == 'hello from parent'
        assert cfield == 21
        assert pfield == 42
    }

    void testStaticImportAndDefaultValue() {
        assertScript '''
            import static Foo.*
            import static Bar.*

            class Bar {
                static void bar() {
                    assert foo(10,1000) == 1010
                    assert foo(10) == 110
                }
            }

            class Foo {
                static int foo(int x, int y = 100) {x+y}
            }

            Bar.bar()
        '''
    }

    // GROOVY-11180
    void testStaticImportAndPackageScope() {
        assertScript '''import groovy.transform.*
            import static Foo.CONST

            class Foo {
                @PackageScope static final String CONST = "value"
            }

            assert CONST == "value"
        '''

        assertScript '''import groovy.transform.*
            import static Foo.CONST

            @PackageScope(PackageScopeTarget.FIELDS)
            class Foo {
                static final String CONST = "value"
            }

            assert CONST == "value"
        '''
    }

    // GROOVY-11323
    void testStaticImportAndInterfaceDefaultMethod() {
        assertScript '''import static Bar.*
            interface Foo {
                default m() { 'Foo' }
            }
            class Bar {
                static  m() { 'Bar' }
            }

            class Baz implements Foo {
                void test() {
                    assert m() == 'Foo'
                }
            }

            new Baz().test()
        '''
    }

    void testStaticImportProperty() {
        for (imports in ['import static Foo.getX; import static Foo.setX', 'import static Foo.*']) {
            assertScript """$imports
                class Foo {
                    static x = 'foo'
                }
                assert x == 'foo'
                x = 'bar'
                assert getX() == 'bar'
                setX('baz')
                assert 'baz' == x
            """
            assertScript """$imports
                class Foo {
                    static x = 'foo'
                    static getX() { x + '_get' }
                }
                assert x == 'foo_get'
                x = 'bar'
                assert getX() == 'bar_get'
                setX('baz')
                assert 'baz_get' == x
            """
            assertScript """$imports
                class Foo {
                    static x = 'foo'
                    static void setX(newx) { x = newx + '_set' }
                }
                assert x == 'foo'
                x = 'bar'
                assert getX() == 'bar_set'
                setX('baz')
                assert 'baz_set' == x
            """
            assertScript """$imports
                class Foo {
                    static x = 'foo'
                    static getX() { x + '_get' }
                    static void setX(newx) { x = newx + '_set' }
                }
                assert x == 'foo_get'
                x = 'bar'
                assert getX() == 'bar_set_get'
                setX('baz')
                assert 'baz_set_get' == x
            """
        }
    }

    void testStaticImportPropertyBooleanAlternative() {
        for (imports in ['import static Foo.isX; import static Foo.setX', 'import static Foo.x', 'import static Foo.*']) {
            assertScript """$imports
                class Foo {
                    static x
                    static boolean isX() { !!x }
                }
                assert !x
                x = true
                assert isX()
                setX(false)
                assert !x
            """
            assertScript """$imports
                class Foo {
                    static x
                    static boolean isX() { !!x }
                    static void setX(newx) { x = newx }
                }
                assert !x
                x = false
                assert !isX()
                setX(true)
                assert x
            """
        }
    }

    void testStaticImportPropertyWithPublicField() {
        for (imports in ['import static Foo.x', 'import static Foo.*']) {
            assertScript """$imports
                class Foo {
                    public static x = 'foo'
                }
                assert x == 'foo'
                x = 'bar'
                assert x == 'bar'
                x = 'baz'
                assert x == 'baz'
            """
            assertScript """$imports
                class Foo {
                    public static x = 'foo'
                    static getX() { x + '_get' }
                }
                assert x == 'foo_get'
                x = 'bar'
                assert getX() == 'bar_get'
                x = 'baz'
                assert x == 'baz_get'
            """
            assertScript """$imports
                class Foo {
                    public static x = 'foo'
                    static void setX(newx) { x = newx + '_set' }
                }
                assert x == 'foo'
                x = 'bar'
                assert x == 'bar_set'
                setX('baz')
                assert x == 'baz_set'
            """
            assertScript """$imports
                class Foo {
                    public static x = 'foo'
                    static getX() { x + '_get' }
                    static void setX(newx) { x = newx + '_set' }
                }
                assert x == 'foo_get'
                x = 'bar'
                assert getX() == 'bar_set_get'
                setX('baz')
                assert x == 'baz_set_get'
            """
        }
        assertScript '''
            import static Foo.getX; import static Foo.setX
            class Foo { public static x = 'foo'; static getX() { x + '_get' }; static void setX(newx) { x = newx + '_set' } }
            assert getX() == 'foo_get'; setX('bar'); assert getX() == 'bar_set_get'; setX('baz'); assert 'baz_set_get' == getX()
        '''
    }

    void testStaticImportPropertyWithAliases() {
        for (imports in ['import static Foo.getX as getZ; import static Foo.setX as setZ', 'import static Foo.x as z']) {
            assertScript """$imports
                class Foo {
                    static x = 'foo'
                }
                assert z == 'foo'
                z = 'bar'
                assert getZ() == 'bar'
                setZ('baz')
                assert z == 'baz'
            """
            assertScript """$imports
                class Foo {
                    static x = 'foo'
                    static getX() { x + '_get' }
                }
                assert z == 'foo_get'
                z = 'bar'
                assert getZ() == 'bar_get'
                setZ('baz')
                assert z == 'baz_get'
            """
            assertScript """$imports
                class Foo {
                    static x = 'foo'
                    static void setX(newx) { x = newx + '_set' }
                }
                assert z == 'foo'
                z = 'bar'
                assert getZ() == 'bar_set'
                setZ('baz')
                assert z == 'baz_set'
            """
            assertScript """$imports
                class Foo {
                    static x = 'foo'
                    static getX() { x + '_get' }
                    static void setX(newx) { x = newx + '_set' }
                }
                assert z == 'foo_get'
                z = 'bar'
                assert getZ() == 'bar_set_get'
                setZ('baz')
                assert z == 'baz_set_get'
            """
        }

        assertScript '''
            import groovy.transform.CompileStatic
            import static Pogo.setP as store
            class Pogo {
                static p
            }
            assert Pogo.p == null
            @CompileStatic test() {
                store('')
                assert Pogo.p == ''
            }
            test()
        '''
    }

    // GROOVY-9382, GROOVY-10133
    void testStaticImportPropertyWithChoices() {
        assertScript '''
            import static Foo.isX
            import static Foo.getX
            class Foo {
                static boolean isX() { true }
                static boolean getX() { false }
            }
            assert x
        '''

        def err = shouldFail '''
            import static Foo.isX
            class Foo { static isX() {} }

            x
        '''
        assert err =~ /No such property: x for class/

        err = shouldFail '''
            import static Foo.isX as isY
            class Foo { static isX() {} }

            y
        '''
        assert err =~ /No such property: y for class/
    }

    // GROOVY-8389, GROOVY-10329
    void testStaticImportPropertyWithClosure() {
        for (imports in ['import static Foo.bar; import static Foo.baz', 'import static Foo.*']) {
            for (tag in ['@groovy.transform.CompileDynamic', '@groovy.transform.CompileStatic']) {
                assertScript """$imports
                    class Foo {
                        static Closure<String> bar = { -> 'property' }
                        static Closure<String> baz = { -> 'property' }
                    }
                    String bar() {
                        'method'
                    }
                    $tag
                    String test() {
                        bar() + ':' + baz()
                    }
                    String result = test()
                    assert result == 'method:property'
                """
            }
        }
    }

    // GROOVY-8389
    void testStaticImportMethodVsLocalMethod() {
        assertScript '''
            import static Foo.bar
            class Foo {
                static bar = 'foo'
            }
            def bar() {
                'bar'
            }
            @groovy.transform.CompileStatic
            def test() {
                bar()
            }
            assert test() == 'bar'
        '''

        assertScript '''
            import static Foo.bar
            class Foo {
                static bar = 'foo'
            }
            static bar() {
                'bar'
            }
            @groovy.transform.CompileStatic
            def test() {
                bar()
            }
            assert test() == 'bar'
        '''

        assertScript '''
            import static Foo.baz
            import static Foo.bar
            class Foo {
                static bar() { 'foobar' }
                static baz() { 'foobaz' }
            }
            def bar() {
                'bar'
            }
            @groovy.transform.CompileStatic
            def test() {
                "${bar()}${baz()}"
            }
            assert test() == 'barfoobaz'
        '''

        assertScript '''
            import static Foo.baz
            import static Foo.bar
            class Foo {
                static bar() { 'foobar' }
                static baz() { 'foobaz' }
            }
            static bar() {
                'bar'
            }
            @groovy.transform.CompileStatic
            def test() {
                "${bar()}${baz()}"
            }
            assert test() == 'barfoobaz'
        '''
    }

    // GROOVY-8145
    void testStaticImportAnyVsGeneratedField() {
        for (what in ['*','log']) {
            assertScript """
                import static Foo.$what
                class Foo {
                    private static log = [info: Closure.IDENTITY]
                }
                @groovy.util.logging.Log
                class Bar {
                    def test() {
                        log.info('bar')
                    }
                }
                assert new Bar().test() == null // return from logger
            """
        }
        for (which in ['public','protected','@groovy.transform.PackageScope']) {
            assertScript """
                import static Foo.*
                class Foo {
                    $which static log = [info: Closure.IDENTITY]
                }
                @groovy.util.logging.Log
                class Bar {
                    def test() {
                        log.info('bar')
                    }
                }
                assert new Bar().test() == 'bar'
            """
        }
    }

    void testConstructorArgsAliasing() {
        // not recommended style to use statics in constructors but supported
        assertScript '''
            class Foo {
                static x
            }
            import static Foo.x as z
            new Foo(z:'hi')
            assert z == 'hi'
        '''
    }

    void testMethodCallWithThisTargetIsNotResolvedToStaticallyImportedMethod() {
        // not using shouldFail() to avoid closure scope
        try {
            this.z()
            fail()
        } catch (MissingMethodException expected) {}
    }

    void testMethodCallWithSuperTargetIsNotResolvedToStaticallyImportedMethod() {
        // not using shouldFail() to avoid closure scope
        try {
            super.z()
            fail()
        } catch (MissingMethodException expected) {}
    }

    // GROOVY-3945, GROOVY-10329, GROOVY-11056
    void testStaticImportOfClosureProperty() {
        for (imports in ['import static groovy.StaticImportTarget.cl', 'import static groovy.StaticImportTarget.*']) {
            assertScript """$imports
                String result = cl()
                assert result == 'StaticImportTarget#static closure called'
            """
            assertScript """$imports
                def fn = { -> cl() }
                String result = fn()
                assert result == 'StaticImportTarget#static closure called'
            """
        }

        assertScript """import static groovy.StaticImportTarget.cl as fn
            String result = fn()
            assert result == 'StaticImportTarget#static closure called'
        """
    }

    // GROOVY-7490, GROOVY-10329, GROOVY-11056
    void testStaticImportOfCallableProperty() {
        for (imports in ['import static Pogo.callable_property', 'import static Pogo.*']) {
            for (use in ['@groovy.transform.CompileStatic caller()', 'def caller()', 'def caller =']) {
                assertScript """$imports
                    class WithCall {
                        String call(String input) {
                            return input
                        }
                    }
                    class Pogo {
                        static final WithCall callable_property = new WithCall()
                    }
                    $use {
                        callable_property('works')
                    }
                    String result = caller()
                    assert result == 'works'
                """
            }
        }
    }

    // GROOVY-4145
    void testStaticPropertyImportedImplementedAsGetter() {
        assertScript '''
            import static Groovy4145.foo4145
            class Groovy4145 {
                static getFoo4145() {
                    return 3
                }
            }
            assert foo4145 == 3
        '''
    }

    // GROOVY-4228
    void testMethodCallExpressionInStaticContextWithInstanceVariableShouldFail() {
        def err = shouldFail '''
            class B {
                def c = new Object()
                static main(args) {
                    c.foo()
                }
            }
        '''
        assert err =~ /Apparent variable 'c' was found in a static scope but doesn't refer to a local variable, static field or class/
    }

    // GROOVY-10396
    void testStaticImportVersusThisOrSuperMethod1() {
        assertScript '''
            import static groovy.Extension10396.*

            static void test() {
                println 'x'
            }
            strings.clear()
            test()

            assert 'x' in strings
        '''
    }

    // GROOVY-10396
    void testStaticImportVersusThisOrSuperMethod2() {
        assertScript '''
            import static groovy.Extension10396.*

            def obj = new Object() { // outer class extends Script
                String toString() {
                    println 'AIC::x'
                    super.toString()
                }
            }
            strings.clear()
            obj.toString()

            assert 'x' !in strings
        '''
    }

    // GROOVY-10396
    void testStaticImportVersusThisOrSuperMethod3() {
        assertScript '''
            import static groovy.Extension10396.*

            static void println(String s) { // static overload
            }
            static void test() {
                println 'x'
            }
            strings.clear()
            test()

            assert 'x' !in strings
        '''
    }

    void testStaticStarImportOfStaticInnerClass() {
        assert Inner1.class.name == 'groovy.Outer1$Inner1'
    }

    void testStaticImportOfStaticInnerClass() {
        assert Inner2.class.name == 'groovy.Outer2$Inner2'
    }

    void testStaticImportOfStaticInnerClassWithAlias() {
        assert InnerAlias2.class.name == 'groovy.Outer2$Inner2'
    }

    void testStaticImportOfStaticInnerClassExistingExternalJar() {
        assert Name.class.name == 'java.util.jar.Attributes$Name'
    }

    void testStaticImportOfStaticInnerClassExistingExternalJarWithAlias() {
        assert AttrName.class.name == 'java.util.jar.Attributes$Name'
    }

    void testExplicitStaticMethodCallHasPrecedenceOverStaticImport() {
        Bar4964.run()
    }

    void testMapIndexInLeftExpressionOfEquals() {
        holder = 'foo'
        def map = [:]
        map[holder] = 'bar'
        assert map.containsKey('foo')
        assert map.foo == 'bar'
    }

    void testConstructorParamInLeftExpressionOfEquals() {
        holder = [:]
        new HolderWrapper(holder).foo = 'baz'
        assert holder.foo == 'baz'
    }

    void testMethodParamInLeftExpressionOfEquals() {
        holder = [[a:1, b:2], [c:3]]
        DGM.find(holder) { it.size() == 2 }.a = 4
        assert holder[0].a == 4
        dgmFind(holder) { it.size() == 1 }.c = 7
        assert holder[1].c == 7
    }

    private <T> T dgmFind(Collection<T> col, Closure clos) {
        DGM.find(col, clos)
    }
}

//------------------------------------------------------------------------------

class API {
    static noArrayMethod(String s, int value) {
        "noArrayMethod(${s}, ${value})"
    }

    static arrayMethod(String s, int[] values) {
        "arrayMethod(${s}, " + values.toList().join(", ") + ")"
    }
}

class StaticImportParent {
    static pfield = 42
    static pmethod() { 'hello from parent' }
}

class StaticImportChild extends StaticImportParent {
    static cfield = 21
    static cmethod() { 'hello from child' }
}

class Outer1 {
    static class Inner1 {}
}

class Outer2 {
    static class Inner2 {}
}

class Foo4964 {
    static doIt() { [k: 'foo'] }
}

class Bar4964 {
    static doIt() { [k: 'bar'] }

    /*
     The following `run` method is invoked by `testExplicitStaticMethodCallHasPrecedenceOverStaticImport`
     As the method name shows, "ExplicitStaticMethodCall Has Precedence Over StaticImport", but the original test code does not conform to the intention
     */
    static run() {
//        The original test code is commented as follows:
//        assert doIt().k == 'foo'
//        assert doIt() == [k: 'foo']

        assert doIt().k == 'bar'
        assert doIt() == [k: 'bar']

        assert Bar4964.doIt() == [k: 'bar']
        assert Bar4964.doIt().k == 'bar'
    }
}

class Container5087 {
    static holder
}

class HolderWrapper {
    def holder

    HolderWrapper(holder) {
        this.holder = holder
    }

    void setProperty(String name, Object value) {
        holder[name] = value
    }
}

class Extension10396 {
    static final List<String> strings = []
    static void println(String s) {
        strings << s
    }
}
