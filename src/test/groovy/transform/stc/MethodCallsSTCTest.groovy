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
package groovy.transform.stc

import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.codehaus.groovy.tools.javac.JavaAwareCompilationUnit

import static org.codehaus.groovy.control.customizers.builder.CompilerCustomizationBuilder.withConfig

/**
 * Unit tests for static type checking : method calls.
 */
class MethodCallsSTCTest extends StaticTypeCheckingTestCase {

    @Override
    protected void configure() {
        withConfig(config) {
            imports {
                alias 'A', 'groovy.transform.stc.MethodCallsSTCTest.MyMethodCallTestClass'
                alias 'B', 'groovy.transform.stc.MethodCallsSTCTest.MyMethodCallTestClass2'
                alias 'C', 'groovy.transform.stc.MethodCallsSTCTest.MyMethodCallTestClass3'
            }
        }
    }

    void testMethodCallOnInstance() {
        assertScript '''
            A a = new A()
            assert a.add(1,1)==2
        '''
    }

    void testBestChoiceMethodCallOnInstance() {
        assertScript '''
            A a = new A()
            assert a.add(1d,1d)==3
        '''
    }

    void testMissingInstanceMethod() {
        shouldFailWithMessages '''
            A a = new A()
            assert a.foo(1,1)==2
        ''',
        'Cannot find matching method'
    }

    void testMethodCallOnInstanceWithVarArgs() {
        assertScript '''
            A a = new A()
            assert a.sum(1,1,2)==4
        '''
    }

    void testMethodCallOnInstanceWithVarArgs2() {
        assertScript '''
            A a = new A()
            int[] arr = [1,1,2]
            assert a.sum(arr)==4
        '''
    }

    void testStaticMethodCall() {
        assertScript '''
            String echo = A.echo 'echo'
            assert  echo == 'echo'
        '''
    }

    void testMissingStaticMethod() {
        shouldFailWithMessages '''
            A.missing 'echo'
        ''',
        'Cannot find matching method'
    }

    void testStaticMethodWithVarArgs() {
        assertScript '''
            int mul = A.mul([1,2,3] as int[])
            assert mul == 6
        '''
    }

    void testStaticMethodWithVarArgs2() {
        assertScript '''
            int mul = A.mul(1,2,3)
            assert mul == 6
        '''
    }

    void testStaticMethodCallWithInheritance() {
        assertScript '''
            String echo = B.echo 'echo'
            assert echo == 'echo'
        '''
    }

    void testStaticMethodCallThroughInstance() {
        assertScript '''
            A a = new A()
            String echo = a.echo 'echo'
            assert echo == 'echo'
        '''
    }

    void testStaticMethodCallOnJDK() {
        assertScript '''
            int[] arr = [3,2,1]
            Arrays.sort(arr)
            assert arr == [1,2,3] as int[]
        '''
    }

    void testStaticMethodCallOnJDK2() {
        assertScript '''
            String[] arr = ['3','2','1']
            Arrays.sort(arr)
            assert arr == ['1','2','3'] as String[]
        '''
    }

    void testStaticMethodCallOnJDK3() {
        assertScript '''
            List arr = ['3','2','1']
            Collections.sort(arr)
            assert arr == ['1','2','3']
        '''
    }

    void testStaticMethodCallOnJDK4() {
        assertScript '''
            List<String> arr = ['3','2','1']
            Collections.sort(arr)
            assert arr == ['1','2','3']
        '''
    }

    void testPlusStaticMethodCall() {
        assertScript '''
            static int foo() { 1 }
            assert 1 + foo() == 2
        '''
    }

    void testExplicitTargetMethodWithCast() {
        assertScript '''
            String foo(String str) { 'STRING' }
            String foo(Object o) { 'OBJECT' }
            assert foo('call') == 'STRING'
            assert foo((Object)'call') == 'OBJECT'
        '''
    }

    void testGenericMethodCall() {
        assertScript '''
            C c = new C()
            String[] args = ['a','b','c']
            assert c.identity(args) == args
        '''
    }

    void testGenericMethodCallWithVarArg() {
        assertScript '''
            C c = new C()
            assert c.identity('a','b','c') == ['a','b','c']
        '''
    }

    void testGenericMethodCallWithVarArgAndSingleArg() {
        assertScript '''
            C c = new C()
            assert c.identity('a') == ['a']
        '''
    }

    void testGenericMethodCallWithVarArgAndNoArg() {
        assertScript '''
            C c = new C()
            assert c.identity() == []
        '''
    }

    void testGenericMethodCall2() {
        assertScript '''
            B c = new B<String>()
            String[] args = ['a','b','c']
            assert c.identity(args) == args
        '''
    }

    void testGenericMethodCall3() {
        shouldFailWithMessages '''
            B c = new B<Integer>()
            String[] args = ['a','b','c']
            assert c.identity(args) == args
        ''',
        'Cannot call groovy.transform.stc.MethodCallsSTCTest$MyMethodCallTestClass2#identity(java.lang.Integer[]) with arguments [java.lang.String[]]'
    }

    // GROOVY-8909
    void testGenericMethodCall4() {
        assertScript '''
            void m(List<Object> list) {
                assert list.size() == 3
            }
            m([1,2,3])
        '''
        // no coercion like assignment
        shouldFailWithMessages '''
            void m(Set<Integer> set) {
            }
            m([1,2,3,3])
        ''',
        'Cannot find matching method','m(java.util.ArrayList<java.lang.Integer>). Please check if the declared type is correct and if the method exists.'
    }

    // GROOVY-7106, GROOVY-7274, GROOVY-9844
    void testGenericMethodCall5() {
        assertScript '''
            void m(Map<CharSequence,Number> map) {
                assert map.size() == 3
                assert map['a'] == 1
                assert 'z' !in map
            }
            m([a:1,b:2,c:3])
        '''

        assertScript '''
            void m(Map<String,Object> map) {
            }
            m([d:new Date(), i:1, s:""])
        '''

        assertScript '''
            void m(Map<String,Object> map) {
            }
            ['x'].each {
                m([(it): it.toLowerCase()])
            }
        '''
    }

    void testNullSafeCall() {
        assertScript '''
            String str = null
            assert str?.toString() == null
        '''
    }

    void testCallToSuper() {
        assertScript '''
            class Foo {
                int foo() { 1 }
            }
            class Bar extends Foo {
                int foo() { super.foo() }
            }
            def bar = new Bar()
            assert bar.foo() == 1
        '''
    }

    // GROOVY-10897
    void testCallToSuper2() {
        assertScript '''
            interface A10897 {
                def m()
            }
            interface B10897 extends A10897 {
                @Override def m()
            }
            class C10897 implements A10897 {
                @Override def m() { "C" }
            }
            class D10897 extends C10897 implements B10897 {
            }
            class E10897 extends D10897 {
                @Override
                def m() {
                    "E then " + super.m()
                }
            }
            assert new E10897().m() == 'E then C'
        '''
    }

    // GROOVY-10494
    void testCallToSuperDefault() {
        assertScript '''
            interface I<T> {
                default m(T t) {
                    return t
                }
            }
            class C10494 implements I<String> {
                @Override m(String s) {
                    I.super.m(s)
                }
            }
            String result = new C10494().m('works')
            assert result == 'works'
        '''

        shouldFailWithMessages '''
            interface I<T> {
                default void m(T t) {
                }
            }
            class C10494 implements I<String> {
                @Override void m(String s) {
                    super.m(s)
                }
            }
        ''',
        'Default method m(T) requires qualified super'
    }

    // GROOVY-10922
    void testCallToSuperGenerated() {
        assertScript '''
            interface Foo {
                String getString()
                void setString(String s)
            }
            class Bar implements Foo {
                String string
            }
            class Baz extends Bar {
                String object
                @Override
                void setString(String string) {
                    super.setString(string)
                    object = string
                }
            }

            def obj = new Baz()
            obj.setString('xx')
            assert obj.object == 'xx'
            assert obj.string == 'xx'
        '''
    }

    void testMethodCallFromSuperOwner() {
        assertScript '''
            class Child extends groovy.transform.stc.MethodCallsSTCTest.GroovyPage {
                void foo() {
                    createTagBody(1) { ->
                        printHtmlPart(2)
                    }
                }
            }
            new Child()
        '''
    }

    void testCallToPrivateInnerClassMethod() {
        assertScript '''
            class Outer {
                static class Inner {
                    private static void foo() {}
                }
                static main(args) { Inner.foo() }
            }
        '''
    }

    void testCallToPrivateOuterClassMethod() {
        assertScript '''
            class Outer {
                private static void foo() {}
                static class Inner {
                    private static void bar() { Outer.foo() }
                }
            }
            new Outer.Inner()
        '''
    }

    void testCallToPrivateInnerClassConstant() {
        assertScript '''
            class Outer {
                static class Inner {
                    private static int foo = 42
                }
                static main(args) { Inner.foo }
            }
        '''
    }

    void testCallToPrivateOuterClassConstant() {
        assertScript '''
            class Outer {
                private static int foo = 42
                static class Inner {
                    private static void bar() { Outer.foo }
                }
            }
            new Outer.Inner()
        '''
    }

    void testReferenceToInaccessiblePrivateMethod() {
        shouldFail(MultipleCompilationErrorsException) {
            assertScript '''
                class Main {
                    static main(args) { Peer.foo() }
                }
                class Peer {
                    private static void foo() {}
                }
            '''
        }
    }

    // GROOVY-6647
    void testReferenceToInaccessiblePrivateConstructor() {
        shouldFailWithMessages '''
            class Main {
                private Main() {}
            }

            class Peer {
                def foo() { new Main() }
            }
        ''',
        'Cannot find matching constructor Main()'
    }

    // GROOVY-8509
    void testCallProtectedFromClassInSamePackage() {
        assertScript '''
            class Foo {
                protected Foo() {}
                protected int m() { 123 }
            }
            class Bar {
                int test() {
                    new Foo().m()
                }
            }
            assert new Bar().test() == 123
        '''
    }

    // GROOVY-7862
    void testCallProtectedMethodFromInnerClassInSeparatePackage() {
        assertScript '''
            import groovy.transform.stc.MethodCallsSTCTest.BaseWithProtected as Foo

            class Bar extends Foo {
                class Baz {
                    int test() {
                        m()
                    }
                }
                int test() {
                    new Baz().test()
                }
            }
            assert new Bar().test() == 1
        '''
    }

    // GROOVY-7063
    void testCallProtectedMethodFromSubclassClosureInDifferentPackage() {
        assertScript '''
            import groovy.transform.stc.MethodCallsSTCTest.BaseWithProtected as Foo

            class Bar extends Foo {
                int baz() {
                    def c = {
                        m()
                    }
                    c.call()
                }
            }
            def bar = new Bar()
            assert bar.baz() == 1
        '''
    }

    // GROOVY-7264
    void testCallProtectedMethodWithGenericTypes() {
        assertScript '''
            class Foo<T> {
                protected boolean m(T t) {
                    true
                }
            }
            class Bar extends Foo<Integer> {
                int baz() {
                    def c = {
                        m(123)
                    }
                    c.call() ? 1 : 0
                }
            }
            def bar = new Bar()
            assert bar.baz() == 1
        '''
    }

    // GROOVY-5175
    void testCallMethodAcceptingArrayWithNull() {
        assertClass '''
            class Main {
                def bar(String[] s) {
                }
                def foo() {
                    bar(null)
                }
            }
        '''
    }

    // GROOVY-5175
    void testCallMethodWithNull() {
        assertClass '''
            class Main {
                def bar(Date date) {
                }
                def foo() {
                    bar(null)
                }
            }
        '''
    }

    // GROOVY-5175
    void testCallMethodWithNullAndAnotherParameter() {
        assertClass '''
            class Main {
                def bar(Date date1, Date date2) {
                }
                def foo() {
                    bar(null, new Date())
                }
            }
        '''
    }

    // GROOVY-5175
    void testAmbiguousCallMethodWithNullAndAnotherParameter() {
        shouldFailWithMessages '''
            class Main {
                def bar(Date date1, Date date2) {
                }
                def bar(String str, Date date) {
                }
                def foo() {
                    bar(null, new Date())
                }
            }
        ''',
        'Reference to method is ambiguous'
    }

    // GROOVY-5175
    void testDisambiguateCallMethodWithNullAndAnotherParameter() {
        assertClass '''
            class Main {
                def bar(Date date1, Date date2) {
                }
                def bar(String str, Date date) {
                }
                def foo() {
                    bar((Date)null, new Date())
                }
            }
        '''
    }

    void testMethodCallWithDefaultParams() {
        assertScript '''
            class Support {
                Support(String name, String val, List arg=null, Set set = null, Date suffix = new Date()) {
                    "$name$val$suffix"
                }
            }
            new Support(null, null, null, null)
        '''
    }

    void testMethodCallArgumentUsingInstanceOf() {
        assertScript '''
            void foo(String str) { 'String' }
            def o
            if (o instanceof String) {
                foo(o)
            }
        '''
    }

    void testShouldFindStaticMethod() {
        assertScript '''
            static String foo(String s) {
                'String'
            }
            foo('String')
        '''
    }

    void testShouldFailWithNoMatchingMethod() {
        shouldFailWithMessages '''
            static String foo(String s) {
                'String'
            }
            static String foo(Integer s) {
                'Integer'
            }
            static String foo(Boolean s) {
                'Boolean'
            }
            ['foo',123,true].each { foo(it) }
        ''',
        'Cannot find matching method'
    }

    void testShouldNotFailThanksToInstanceOfChecks() {
        assertScript '''
            static String foo(String s) {
                'String'
            }
            static String foo(Integer s) {
                'Integer'
            }
            static String foo(Boolean s) {
                'Boolean'
            }
            ['foo',123,true].each {
                if (it instanceof String) {
                    foo((String)it)
                } else if (it instanceof Boolean) {
                    foo((Boolean)it)
                } else if (it instanceof Integer) {
                    foo((Integer)it)
                }
            }
        '''
    }

    void testShouldNotFailThanksToInstanceOfChecksAndWithoutExplicitCasts() {
        assertScript '''
            static String foo(String s) {
                'String'
            }
            static String foo(Integer s) {
                'Integer'
            }
            static String foo(Boolean s) {
                'Boolean'
            }
            ['foo',123,true].each {
                if (it instanceof String) {
                    foo(it)
                } else if (it instanceof Boolean) {
                    foo(it)
                } else if (it instanceof Integer) {
                    foo(it)
                }
            }
        '''
    }

    void testShouldNotFailThanksToInstanceOfChecksAndWithoutExplicitCasts2() {
        assertScript '''
            static String foo(String s) {
                'String'
            }
            static String foo(Integer s) {
                'Integer'
            }
            static String foo(Boolean s) {
                'Boolean'
            }
            ['foo',123,true].each { argument ->
                if (argument instanceof String) {
                    foo(argument)
                } else if (argument instanceof Boolean) {
                    foo(argument)
                } else if (argument instanceof Integer) {
                    foo(argument)
                }
            }
        '''
    }

    void testShouldFailWithMultiplePossibleMethods() {
        shouldFailWithMessages '''
            static String foo(String s) {
                'String'
            }
            static String foo(Integer s) {
                'Integer'
            }
            static String foo(Boolean s) {
                'Boolean'
            }
            ['foo',123,true].each {
                if (it instanceof String || it instanceof Boolean || it instanceof Integer) {
                    foo(it)
                }
            }
        ''',
        'Reference to method is ambiguous'
    }

    void testShouldFailWithMultiplePossibleMethods2() {
        shouldFailWithMessages '''
            static String foo(String s) {
                'String'
            }
            static String foo(Integer s) {
                'Integer'
            }
            static String foo(Boolean s) {
                'Boolean'
            }
            ['foo',123,true].each { argument ->
                if (argument instanceof String || argument instanceof Boolean || argument instanceof Integer) {
                    foo(argument)
                }
            }
        ''',
        'Reference to method is ambiguous'
    }

    // GROOVY-5703
    void testShouldNotConvertStringToStringArray() {
        assertScript '''
            int printMsgs(String ... msgs) {
                int i = 0
                for(String s : msgs) { i++ }

                i
            }
            assert printMsgs('foo') == 1
            assert printMsgs('foo','bar') == 2
        '''
    }

    // GROOVY-5780
    void testShouldNotConvertGStringToStringArray() {
        assertScript '''
            int printMsgs(String ... msgs) {
                int i = 0
                for(String s : msgs) { i++ }

                i
            }
            assert printMsgs("f${'o'}o") == 1
            assert printMsgs("${'foo'}","${'bar'}") == 2
        '''
    }

    void testInstanceOfOnExplicitParameter() {
        assertScript '''
            1.with { obj ->
                if (obj instanceof String) {
                    obj.toUpperCase()
                }
            }
        '''
    }

    void testSAMWithExplicitParameter1() {
        assertScript '''
            public interface SAM {
                boolean run(String var1, Thread th);
            }

            static boolean foo(SAM sam) {
               sam.run("foo",  new Thread())
            }

            static def callSAM() {
                foo { str, th ->
                    str.toUpperCase().equals(th.getName())
                }
            }
        '''
    }

    // GROOVY-8241
    void testSAMWithExplicitParameter2() {
        assertScript '''
            static boolean foo(java.util.function.Predicate<? super String> p) {
                p.test('bar')
            }

            foo { it -> it.toUpperCase(); return true }
        '''
    }

    // GROOVY-7061
    void testSAMWithExplicitParameter3() {
        assertScript '''
            List<Integer> nums = [1, 2, 3, -2, -5, 6]
            Collections.sort(nums, { a, b -> a.abs() <=> b.abs() })
        '''
    }

    // GROOVY-7061
    void testSAMWithExplicitParameter4() {
        assertScript '''
            def foo(List<String> strings) {
                strings.stream().filter { s -> s.length() < 10 }.toArray()
            }
            def words = ["orange", "sit", "test", "flabbergasted", "honorific"]
            foo(words)
        '''
    }

    // GROOVY-5226, GROOVY-11290
    void testShouldFailBecauseVariableIsReassigned() {
        String foo = 'def foo(CharSequence cs) { }'

        shouldFailWithMessages foo + '''
            def it
            if (it instanceof String) {
                it = new Date()
                foo(it)
            }
        ''',
        'Cannot find matching method','#foo(java.util.Date)'

        shouldFailWithMessages foo + '''
            def bar(CharSequence cs) { }
            def it
            if (it instanceof CharSequence) {
                if (it instanceof String) {
                    it = new Date()
                    foo(it)
                }
                bar(it) // it is CharSequence or Date
            }
        ''',
        'Cannot find matching method','#foo(java.util.Date)',
        'Cannot find matching method','#bar(java.util.Date)'
    }

    // GROOVY-5226, GROOVY-11290
    void testShouldNotFailEvenIfVariableIsReassigned() {
        String foobar = 'def foo(int i) { }\ndef bar(CharSequence cs) { }'

        assertScript foobar + '''
            def it = ""
            if (it instanceof String) {
                bar(it)
                it = 123
                foo(it)
            }
        '''

        assertScript foobar + '''
            def it = ""
            if (it instanceof CharSequence) {
                bar(it)
                if (it instanceof String) {
                    bar(it)
                    it = 123
                    foo(it)
                } else {
                    bar(it)
                }
            }
        '''

        assertScript foobar + '''
            def it = ~/regexp/
            if (it !instanceof String) {
                it = 123
                foo(it)
            } else {
                bar(it)
            }
        '''
    }

    // GROOVY-5226, GROOVY-11290
    void testShouldNotFailEvenIfVariableIsReassignedAndMultipleInstanceOf() {
        assertScript '''
            def foo(int i) { 'int' }
            def foo(Date d) { 'Date' }
            def it = ""
            if (it instanceof String) {
                it = 123
                foo(it)
                if (it instanceof Date) {
                    foo(it)
                }
            }
        '''
    }

    void testOneDefaultParam() {
        assertScript '''
            String m(String val = 'hello') {
                return val.toUpperCase()
            }
            assert m() == 'HELLO'
            assert m('bye') == 'BYE'
        '''
    }

    void testOneDefaultParamWithWrongArgType() {
        shouldFailWithMessages '''
            String m(String val = 'hello') {
                return val.toUpperCase()
            }
            assert m(123) == 'HELLO'
        ''',
        '#m(int)'
    }

    void testOneDefaultParamAndOneWithout() {
        assertScript '''
            String m(String val = 'hello', int append) {
                return val.toUpperCase() + append
            }
            assert m(1) == 'HELLO1'
            assert m('bye',2) == 'BYE2'
        '''
    }

    void testOneDefaultParamAndOneWithoutWithWrongArgType() {
        shouldFailWithMessages '''
            String m(String val = 'hello', int append) {
                return val.toUpperCase() + append
            }
            m('test', new Object())
        ''',
        'm(java.lang.String, java.lang.Object)'
    }

    void testMultipleDefaultArgs() {
        assertScript '''
            String m(String first = 'first', String second, String third = 'third') {
                return first.toUpperCase() + ' ' + second.toUpperCase() + ' ' + third.toUpperCase()
            }
            assert m('hello') == 'FIRST HELLO THIRD'
        '''
    }

    void testMultipleDefaultArgsWithMixedTypes() {
        assertScript '''
            String m(String first = 'first', int second, String third = 'third') {
                return first.toUpperCase() + ' ' + second + ' ' + third.toUpperCase()
            }
            assert m(123) == 'FIRST 123 THIRD'
            assert m('f',123) == 'F 123 THIRD'
            assert m('f',123,'s') == 'F 123 S'
        '''
    }

    void testMultipleDefaultArgsWithMixedTypesAndTooManyArgs() {
        shouldFailWithMessages '''
            String m(String first = 'first', int second, String third = 'third') {
                return first.toUpperCase() + ' ' + second + ' ' + third.toUpperCase()
            }
            m('f',123,'s', 'too many args')
        ''',
        '#m(java.lang.String, int, java.lang.String, java.lang.String)'
    }

    void testMultipleDefaultArgsWithMixedTypesAndWrongType() {
        shouldFailWithMessages '''
            String m(String first = 'first', int second, String third = 'third') {
                return first.toUpperCase() + ' ' + second + ' ' + third.toUpperCase()
            }
            m('hello') // no value set for "second"
        ''',
        '#m(java.lang.String)'
    }

    void testShouldNotFailWithAmbiguousMethodSelection() {
        assertScript '''
            StringBuffer sb = new StringBuffer()
            sb.append('foo')
        '''
    }

    // GROOVY-10362
    void testShouldNotFailWithAmbiguousMethodSelection2() {
        assertScript '''
            interface I1<T, U, V extends T> {
                U m(U u, V v)
            }
            interface I2<W extends Boolean, X> extends I1<W, W, W   > {
            }
            interface I3<Y                   > extends I2<Boolean, Y> {
            }
            interface I4<Z extends Number    > extends I3<Z         > {
            }
            class C10362 implements I4<Integer> {
                Boolean m(Boolean x, Boolean y) { x == y }
            }
            abstract class D10362<T1 extends Integer, T2 extends T1> extends C10362 {
            }

            C10362 x = new D10362<Integer,Integer>() {}
            x.m(true, false) // Cannot choose between [Boolean C#m(Boolean,Boolean), U I1#m(U,V)]
        '''
    }

    void testShouldBeAbleToCallMethodUsingDoubleWithDoubleFloatLongIntShortOrByte() {
        assertScript '''
            double square(double x) { x*x }
            assert square(2.0d) == 4.0d
            assert square(2.0f) == 4.0d
            assert square(2L) == 4.0d
            assert square(2) == 4.0d
            assert square((short)2) == 4.0d
            assert square((byte)2) == 4.0d
        '''
    }

    void testShouldNotBeAbleToCallMethodUsingFloatWithDouble() {
        shouldFailWithMessages '''
            float square(float x) { x*x }
            assert square(2.0d) == 4.0d
        ''',
        '#square(double)'
    }

    void testShouldNotBeAbleToCallMethodUsingLongWithFloatOrDouble() {
        shouldFailWithMessages '''
            float square(long x) { x*x }
            assert square(2.0d) == 4.0d
            assert square(2.0f) == 4.0d
        ''',
        '#square(double)', '#square(float)'
    }

    void testShouldNotAllowMethodCallFromStaticInitializer() {
        shouldFailWithMessages '''
            class Foo {
                void instanceMethod() {}
                static {
                    instanceMethod()
                }
            }
            new Foo()
        ''',
        'Non-static method Foo#instanceMethod cannot be called from static context'
    }

    void testShouldNotAllowMethodCallFromStaticMethod() {
        shouldFailWithMessages '''
            class Foo {
                void instanceMethod() {}
                static void staticMethod() {
                    instanceMethod()
                }
            }
            Foo.staticMethod()
        ''',
        'Non-static method Foo#instanceMethod cannot be called from static context'
    }

    void testShouldNotAllowMethodCallFromStaticField() {
        shouldFailWithMessages '''
            class Foo {
                boolean instanceMethod() {}
                static FOO = instanceMethod()
            }
            new Foo()
        ''',
        'Non-static method Foo#instanceMethod cannot be called from static context'
    }

    // GROOVY-5495
    void testShouldFindMethodFromSuperInterface() {
        assertScript '''
            class ClassUnderTest {
                void methodFromString(SecondInterface si) {
                    si.methodFromSecondInterface()
                    si.methodFromFirstInterface()
                }
            }

            interface FirstInterface {
                void methodFromFirstInterface()
            }

            interface SecondInterface extends FirstInterface {
                void methodFromSecondInterface()
            }

            new ClassUnderTest()
        '''
    }

    void testShouldNotBeAmbiguousCall() {
        assertScript '''
            (0..10).find { int x -> x < 5 }
        '''
    }

    void testEqualsCalledOnInterface() {
        assertScript '''
            Serializable ser = (Serializable) new Integer(1)
            if (ser !=null) { // ser.equals(null)
                println 'ok'
                int hash = ser.hashCode()
                String str = ser.toString()
                try {
                    ser.notify()
                } catch (e) {}
                try {
                    ser.notifyAll()
                } catch (e) {}

                try {
                    ser.wait()
                } catch (e) {}
            }
        '''
    }

    // GROOVY-5534
    void testSafeDereference() {
        assertScript '''
            def foo() {
               File bar
               bar?.name
            }
            assert foo() == null
        '''
    }

    // GROOVY-5540
    void testChoosePublicMethodInHierarchy() {
        assertScript '''import groovy.transform.stc.MethodCallsSTCTest.Child2
            class Foo {
                int delegate() {
                    @ASTTest(phase=INSTRUCTION_SELECTION, value={
                        def md = node.rightExpression.getNodeMetaData(DIRECT_METHOD_CALL_TARGET)
                        assert md.declaringClass.nameWithoutPackage == 'MethodCallsSTCTest$ChildWithPublic'
                    })
                    int res = new Child2().m()
                    res
                }
            }
            assert new Foo().delegate() == 2
        '''
    }

    // GROOVY-5580
    void testGetNameAsPropertyFromSuperInterface() {
        assertScript '''
            interface Upper { String getName() }
            interface Lower extends Upper {}
            String foo(Lower impl) {
                impl.name // getName() called with the property notation
            }
            assert foo({ 'bar' } as Lower) == 'bar'
        '''
    }

    void testGetNameAsPropertyFromSuperInterfaceUsingConcreteImpl() {
        assertScript '''
            interface Upper { String getName() }
            interface Lower extends Upper {}
            class Foo implements Lower { String getName() { 'bar' } }
            String foo(Foo impl) {
                impl.name // getName() called with the property notation
            }
            assert foo(new Foo()) == 'bar'
        '''
    }

    void testGetNameAsPropertyFromSuperInterfaceUsingConcreteImplSubclass() {
        assertScript '''
            interface Upper { String getName() }
            interface Lower extends Upper {}
            class Foo implements Lower { String getName() { 'bar' } }
            class Bar extends Foo {}
            String foo(Bar impl) {
                impl.name // getName() called with the property notation
            }
            assert foo(new Bar()) == 'bar'
        '''
    }

    void testIsGetterAsPropertyFromSuperInterface() {
        assertScript '''
            interface Upper { boolean isBar() }
            interface Lower extends Upper {}
            boolean foo(Lower impl) {
                impl.bar // isBar() called with the property notation
            }
            assert foo({ true } as Lower)
        '''
    }

    void testIsGetterAsPropertyFromSuperInterfaceUsingConcreteImpl() {
        assertScript '''
            interface Upper { boolean isBar() }
            interface Lower extends Upper {}
            class Foo implements Lower { boolean isBar() { true } }
            boolean foo(Foo impl) {
                impl.bar // isBar() called with the property notation
            }
            assert foo(new Foo())
        '''
    }

    void testIsGetterAsPropertyFromSuperInterfaceUsingConcreteImplSubclass() {
        assertScript '''
            interface Upper { boolean isBar() }
            interface Lower extends Upper {}
            class Foo implements Lower { boolean isBar() { true } }
            class Bar extends Foo {}
            boolean foo(Bar impl) {
                impl.bar // isBar() called with the property notation
            }
            assert foo(new Bar())
        '''
    }

    // GROOVY-5580: getName variant
    void testGetNameFromSuperInterface() {
        assertScript '''
            interface Upper { String getName() }
            interface Lower extends Upper {}
            String foo(Lower impl) {
                impl.getName()
            }
            assert foo({ 'bar' } as Lower) == 'bar'
        '''
    }

    void testGetNameFromSuperInterfaceViaConcreteType1() {
        assertScript '''
            interface Upper { String getName() }
            interface Lower extends Upper {}
            class Foo implements Lower { String getName() { 'bar' } }
            String foo(Foo impl) {
                impl.getName()
            }
            assert foo(new Foo()) == 'bar'
        '''
    }

    void testGetNameFromSuperInterfaceViaConcreteType2() {
        assertScript '''
            interface Upper { String getName() }
            interface Lower extends Upper {}
            class Foo implements Lower { String getName() { 'bar' } }
            class Bar extends Foo {}
            String foo(Bar impl) {
                impl.getName()
            }
            assert foo(new Bar()) == 'bar'
        '''
    }

    void testSpreadArgsRestrictedInNonStaticMethodCall() {
        // GROOVY-10597
        assertScript '''
            def m(int i, String... strings) {
                '' + i + strings.join('')
            }
            List<String> strings() {['3','4']}
            assert m(1, '2', *strings(), '5') == '12345'
        '''

        shouldFailWithMessages '''
            def foo(String one, String... zeroOrMore) {
            }
            def bar(String[] strings) {
                foo(*strings)
            }
        ''',
        'The spread operator cannot be used as argument of method or closure calls with static type checking because the number of arguments cannot be determined at compile time'

        shouldFailWithMessages '''
            def foo(String a, String b, int c, double d, double e) {
            }
            def bar(String[] strings, int i, double[] numbers) {
                foo(*strings, i, *numbers)
            }
        ''',
        'The spread operator cannot be used as argument of method or closure calls with static type checking because the number of arguments cannot be determined at compile time',
        'The spread operator cannot be used as argument of method or closure calls with static type checking because the number of arguments cannot be determined at compile time',
        'Cannot find matching method '
    }

    void testSpreadArgsRestrictedInStaticMethodCall() {
        // GROOVY-10597
        assertScript '''
            static m(int i, String... strings) {
                return '' + i + strings.join('')
            }
            List<String> strings = ['3','4']
            assert m(1,'2',*strings,'5') == '12345'
        '''

        shouldFailWithMessages '''
            static foo(String one, String... zeroOrMore) {
            }
            static bar(String[] strings) {
                foo(*strings)
            }
        ''',
        'The spread operator cannot be used as argument of method or closure calls with static type checking because the number of arguments cannot be determined at compile time'

        shouldFailWithMessages '''
            static foo(String a, String b, int c, double d, double e) {
            }
            static bar(String[] strings, int i, double[] numbers) {
                foo(*strings, i, *numbers)
            }
        ''',
        'The spread operator cannot be used as argument of method or closure calls with static type checking because the number of arguments cannot be determined at compile time',
        'The spread operator cannot be used as argument of method or closure calls with static type checking because the number of arguments cannot be determined at compile time',
        'Cannot find matching method '
    }

    void testSpreadArgsRestrictedInConstructorCall() {
        // GROOVY-10597
        assertScript '''
            class Foo {
                Foo(String one, String... zeroOrMore) {
                    String result = one + zeroOrMore.join('')
                    assert result == 'ABC'
                }
            }
            new Foo('A', *['B'], 'C')
        '''

        shouldFailWithMessages '''
            class Foo {
                Foo(String one, String... zeroOrMore) {
                }
            }
            new Foo(*['A','B'])
        ''',
        'The spread operator cannot be used as argument of method or closure calls with static type checking because the number of arguments cannot be determined at compile time'

        shouldFailWithMessages '''
            class Foo {
                Foo(String a, String b) {
                }
            }
            new Foo(*['A','B'])
        ''',
        'Cannot find matching constructor Foo(',
        'The spread operator cannot be used as argument of method or closure calls with static type checking because the number of arguments cannot be determined at compile time'
    }

    void testSpreadArgsRestrictedInClosureCall() {
        // GROOVY-10597
        assertScript '''
            def closure = { String one, String... zeroOrMore ->
                return one + zeroOrMore.join('')
            }
            String result = closure('A', *['B','C'])
            assert result == 'ABC'
        '''

        shouldFailWithMessages '''
            def closure = { String one, String... zeroOrMore -> }
            def strings = ['A','B','C']
            closure(*strings)
        ''',
        'The spread operator cannot be used as argument of method or closure calls with static type checking because the number of arguments cannot be determined at compile time'

        shouldFailWithMessages '''
            def closure = { String a, String b, String c -> }
            def strings = ['A','B','C']
            closure(*strings)
        ''',
        'The spread operator cannot be used as argument of method or closure calls with static type checking because the number of arguments cannot be determined at compile time',
        'Cannot call closure that accepts [java.lang.String, java.lang.String, java.lang.String] with '
    }

    // GROOVY-8488
    void testBigDecimalLiteralArgument() {
        assertScript '''
            def m1(double d) { Double.valueOf(d) }
            def m2(float f) { Float.valueOf(f) }
            assert m1(1.0) == 1.0d
            assert m2(1.0) == 1.0f
        '''

        shouldFailWithMessages '''
            class Foo {
                def m1(long l) { Long.valueOf(l) }
                def m2(int i) { new Integer(i) }
                void test() {
                    m1(1.0)
                    m2(1.0)
                }
            }
        ''',
        'Cannot find matching method Foo#m1(java.math.BigDecimal)',
        'Cannot find matching method Foo#m2(java.math.BigDecimal)'

        shouldFailWithMessages '''
            class Foo {
                def m1(long l) { Long.valueOf(l) }
                def m2(int i) { new Integer(i) }
                void test() {
                    m1(1g)
                    m2(1g)
                }
            }
        ''',
        'Cannot find matching method Foo#m1(java.math.BigInteger)',
        'Cannot find matching method Foo#m2(java.math.BigInteger)'
    }

    void testBoxingShouldCostMore() {
        assertScript '''
            int foo(int x) { 1 }
            int foo(Integer x) { 2 }

            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                lookup('mce').each {
                    def call = it.expression
                    def target = call.getNodeMetaData(DIRECT_METHOD_CALL_TARGET)
                    assert target.parameters[0].type == int_TYPE
                }
            })
            int bar() {
                mce: foo(1)
            }
            bar()
            // commented out the next line because this is something
            // the dynamic runtime cannot ensure
            //assert bar() == 1
        '''
    }

    // GROOVY-5645
    void testSuperCallWithVargs() {
        assertScript '''
            class Base {
                int foo(int x, Object... args) { 1 }
                int foo(Object... args) { 2 }
            }
            class Child extends Base {
                void bar() {
                    assert foo(1, 'a') == 1
                    super.foo(1, 'a') == 1
                }
            }
            new Child().bar()
        '''
    }

    void testVargsSelection1() {
        assertScript '''
            int foo(int x, Object... args) { 1 }
            int foo(Object... args) { 2 }
            assert foo(1) == 1
            assert foo() == 2
            assert foo(1,2) == 1
        '''
    }

    void testVargsSelection2() {
        assertScript '''
            int sum(int x) { 1 }
            int sum(int... args) {
                0
            }
            assert sum(1) == 1
        '''
    }

    void testVargsSelection3() {
        assertScript '''
            int sum(int x) { 1 }
            int sum(int y, int... args) {
                0
            }
            assert sum(1) == 1
        '''
    }

    // GROOVY-6147
    void testVargsSelection4() {
        assertScript '''
            int select(Object a, String s) { 1 }
            int select(Object a, String s, Object[] args) { 2 }
            def o = new Date()
            def s = 'String'
            @ASTTest(phase=INSTRUCTION_SELECTION,value={
                def method = node.rightExpression.getNodeMetaData(DIRECT_METHOD_CALL_TARGET)
                assert method.name == 'select'
                assert method.parameters.length==2
            })
            def result = select(o,s)
            assert result == 1
        '''
    }

    // GROOVY-6195
    void testVargsSelection5() {
        assertScript '''
            def list = ['a', 'b', 'c']
            Object[] arr = list.toArray()
            println arr
        '''
    }

    // GROOVY-6235
    void testVargsSelection6() {
        assertScript '''import org.codehaus.groovy.classgen.asm.sc.support.Groovy6235SupportSub as Support
            def b = new Support()
            assert b.overload() == 1
            assert b.overload('a') == 1
            assert b.overload('a','b') == 2
        '''
    }

    // GROOVY-6646
    void testVargsSelection7() {
        assertScript '''
            def foo(Class... cs) { "Classes" }
            def foo(String... ss) { "Strings" }

            assert foo(List, Map) == "Classes"
            assert foo("2","1") == "Strings"
        '''
        assertScript '''
            def foo(Class<?>... cs) { "Classes" }
            def foo(String... ss) { "Strings" }

            assert foo(List, Map) == "Classes"
            assert foo("2","1") == "Strings"
        '''
    }

    // GROOVY-8737
    void testVargsSelection8() {
        String methods = '''
            String m(String key, Object[] args) {
                "key=$key, args=$args"
            }
            String m(String key, Object[] args, Object[] parts) {
                "key=$key, args=$args, parts=$parts"
            }
            String m(String key, Object[] args, String[] names) {
                "key=$key, args=$args, names=$names"
            }
        '''
        assertScript methods + '''
            String result = m( 'hello', new Object[]{'world'} ) // exact match for m(String,Object[])
            assert result == 'key=hello, args=[world]'
        '''
        assertScript methods + '''
            String result = m( 'hello', new String[]{'world'} )
            assert result == 'key=hello, args=[world]'
        '''
        assertScript methods + '''
            String result = m( "${'hello'}", 'world' )
            assert result == 'key=hello, args=[world]'
        '''
        assertScript methods + '''
            String result = m( 'hello', 'world' )
            assert result == 'key=hello, args=[world]'
        '''

        assertScript methods + '''
            String result = m( 'hello', new String[]{'there'}, 'Steve' )
            assert result == 'key=hello, args=[there], names=[Steve]'
        '''
    }

    // GROOVY-11053
    void testVargsSelection9() {
        assertScript '''
            @Grab('org.apache.commons:commons-lang3:3.11')
            import org.apache.commons.lang3.ArrayUtils

            byte[] one = new byte[1]
            byte[] oneAlso = ArrayUtils.removeAll(one)
            byte[] none = ArrayUtils.removeAll(one, 0)

            assert none.length == 0
            assert oneAlso.length == 1
        '''
    }

    // GROOVY-5525
    void testShouldFindArraysCopyOf() {
        assertScript '''
            class CopyOf {
                public static void main(String[] args) {
                    def copy = Arrays.copyOf(args, 1)
                    assert copy.length == 1
                }
            }
        '''
    }

    // GROOVY-5702
    void testShouldFindInterfaceMethod() {
        assertScript '''
            interface OtherCloseable {
                void close()
            }

            abstract class MyCloseableChannel implements OtherCloseable {  }

            class Test {
                static void test(MyCloseableChannel mc) {
                    mc?.close()
                }
            }

            Test.test(null)
        '''
    }

    void testShouldFindInheritedInterfaceMethod() {
        assertScript '''
            interface Top { void foo() }
            interface Middle extends Top {}
            interface Bottom extends Middle {}

            void test(Bottom b) {
               b.foo()
            }
        '''
    }

    void testShouldFindInheritedInterfaceMethod2() {
        assertScript '''
            interface Top { int foo(int i) }
            interface Middle extends Top { int foo(String s) }
            interface Bottom extends Middle {}

            void test(Bottom b) {
                b.foo(123)
            }
        '''
    }

    void testShouldFindInheritedInterfaceMethod3() {
        assertScript '''
            interface Top { int foo(int i) }
            interface Middle extends Top { }
            interface Bottom extends Middle { int foo(String s) }

            void test(Bottom b) {
                b.foo(123)
            }
        '''
    }

    void testShouldFindInheritedInterfaceMethod4() {
        assertScript '''
            interface Top { int foo(int i) }
            interface Middle extends Top { int foo(String s) }
            abstract class Bottom implements Middle {}

            int test(Bottom b) {
                b.foo(123)
            }
            def bot = new Bottom() {
                int foo(int i) { 1 }
                int foo(String s) { 2 }
            }
            assert test(bot) == 1
        '''
    }

    void testShouldFindInheritedInterfaceMethod5() {
        assertScript '''
            interface Top { int foo(int i) }
            interface Middle extends Top { }
            abstract class Bottom implements Middle { abstract int foo(String s) }

            int test(Bottom b) {
                b.foo(123)
            }
            def bot = new Bottom() {
                int foo(int i) { 1 }
                int foo(String s) { 2 }
            }
            assert test(bot) == 1
        '''
    }

    // GROOVY-9890
    void testShouldFindInheritedInterfaceDefaultMethod() {
        assertScript '''
            class Impl implements groovy.bugs.groovy9890.Face {
                @Override def foo(String s) {
                    return s
                }
                // abstract def foo(long n)
            }
            void test() {
                def result = new Impl().foo(42L)
                assert result.class == Long.class
            }
            test()
        '''
    }

    // GROOVY-9890
    void testShouldFindInheritedInterfaceDefaultMethodJava() {
        assertScript '''
            void test() {
                def result = new groovy.bugs.groovy9890.ImplJ().foo(42L)
                assert result.class == Long.class
            }
            test()
        '''
    }

    // GROOVY-5743
    void testClosureAsParameter() {
        assertScript '''
            Integer a( String s, Closure<Integer> b ) {
                b( s )
            }

            assert a( 'tim' ) { 0 } == 0
        '''
    }

    // GROOVY-5743
    void testClosureAsParameterWithDefaultValue() {
        assertScript '''
            Integer a( String s, Closure<Integer> b = {String it -> it.length()}) {
                b( s )
            }

            assert a( 'tim' ) == 3
        '''
    }

    // GROOVY-5712
    void testClassForNameVsCharsetForName() {
        assertScript '''import java.nio.charset.Charset
            Charset charset = Charset.forName('UTF-8')
            assert charset instanceof Charset
        '''
    }

    // GROOVY-10939
    void testClassHashCodeVsObjectHashCode() {
        assertScript '''
            int h = this.getClass().hashCode()
        '''
    }

    // GROOVY-10341
    void testCallAbstractSuperMethod() {
        shouldFailWithMessages '''
            abstract class Foo {
                abstract def m()
            }
            class Bar extends Foo {
                @Override
                def m() {
                    super.m()
                }
            }
        ''',
        'Abstract method m() cannot be called directly'
    }

    // GROOVY-5810
    void testCallStaticSuperMethod() {
        assertScript '''
            class Top {
                static boolean called = false
                public static foo() {
                    called = true
                }
            }

            class Bottom extends Top {
                public static foo() {
                    super.foo()
                }
            }
            Bottom.foo()
            assert Top.called
        '''
    }

    void testShouldFindSetProperty() {
        assertScript '''
            class Foo {
                int p
                void m() {
                    this.setProperty('p', 1)
                }
            }
            def o = new Foo()
            o.m()
            assert o.p == 1
        '''
    }

    // GROOVY-5888
    void testStaticContext1() {
        assertScript '''
            class Foo {
                static List p = 'a,b,c'.split(/,/)*.trim()
            }
            assert Foo.p == ['a','b','c']
        '''
    }

    // GROOVY-11195
    void testStaticContext2() {
        assertScript '''
            class Foo {
                static String p = this.getName() // instance method of Class
            }
            assert Foo.p == 'Foo'
        '''
    }

    // GROOVY-10720
    void testOverloadedMethodWithArray() {
        assertScript '''
            Double[] array = new Double[1]
            def stream = Arrays.stream(array) //stream(T[])
            assert stream.map(d -> 'string')[0] == 'string'
        '''
    }

    // GROOVY-5883, GROOVY-6270
    void testClosureUpperBound() {
        assertScript '''
            class Test<T> {
                def map(Closure<T> mapper) { 1 }
                def m1(Closure<Boolean> predicate) {
                    map { T it -> return predicate(it) ? it : null }
                }
                def m2(Closure<Boolean> predicate) {
                    map { T it -> return predicate(it) ? it : (T) null }
                }
                def m3(Closure<Boolean> predicate) {
                    Closure<T> c = { T it -> return predicate(it) ? it : null }
                    map(c)
                }
            }
            def t = new Test<String>()
            assert t.m1{true} == 1
            assert t.m2{true} == 1
            assert t.m3{true} == 1
        '''
    }

    // GROOVY-6569, GROOVY-6528
    void testMoreExplicitErrorMessageOnStaticMethodNotFound() {
        shouldFailWithMessages '''
            Double.isFiniteMissing(2.0d)
        ''',
        'Cannot find matching method java.lang.Double#isFiniteMissing(double)'

        shouldFailWithMessages '''
            String.doSomething()
        ''',
        'Cannot find matching method java.lang.String#doSomething()'
    }

    // GROOVY-6776
    void testPrimtiveParameterAndNullArgument() {
        shouldFailWithMessages '''
            def foo(int i){}
            def bar() {
                foo null
            }
            bar()
        ''',
        '#foo(int) with arguments [<unknown parameter type>]'
    }

    // GROOVY-6751
    void testMethodInBothInterfaceAndSuperclass1() {
        assertScript '''
            interface Face {
              Object getProperty(String s)
            }
            class Impl implements Face { // implemented in groovy.lang.GroovyObject
            }

            try {
                Impl impl = new Impl()
                impl.getProperty('xx')
            }
            catch(MissingPropertyException expected) {
            }
        '''
    }

    // GROOVY-11341
    void testMethodInBothInterfaceAndSuperclass2() {
        File parentDir = File.createTempDir()
        config.with {
            targetDirectory = File.createTempDir()
            jointCompilationOptions = [memStub: true]
        }
        try {
            new File(parentDir, 'p').mkdir()

            def a = new File(parentDir, 'p/A.java')
            a.write '''package p;
                public interface A {
                    Object getValue();
                }
            '''
            def b = new File(parentDir, 'p/B.java')
            b.write '''package p;
                public class B {
                    public Long getValue() { return 21L; }
                }
            '''
            def c = new File(parentDir, 'p/C.java')
            c.write '''package p;
                public class C extends B implements A {
                    // public bridge Object getValue() { ... }
                }
            '''
            def d = new File(parentDir, 'D.groovy')
            d.write '''
                def pojo = new p.C()
                Long value = pojo.getValue() // Cannot assign value of type Object to variable of type Long
                value += pojo.value
                assert value == 42L
            '''

            def loader = new GroovyClassLoader(this.class.classLoader)
            def cu = new JavaAwareCompilationUnit(config, loader)
            cu.addSources(a, b, c, d)
            cu.compile()

            loader.loadClass('D').main()
        } finally {
            parentDir.deleteDir()
            config.targetDirectory.deleteDir()
        }
    }

    // GROOVY-7987
    void testNonStaticMethodViaStaticReceiver() {
        shouldFailWithMessages '''
            class Foo {
                def m() {}
            }
            Foo.m()
        ''',
        'Non-static method Foo#m cannot be called from static context'
    }

    // GROOVY-7813
    void testNonStaticOuterMethodCannotBeCalledFromStaticClass() {
        shouldFailWithMessages '''
            class Foo {
                def m() {}
                static class Bar {
                    void test() { m() }
                }
            }
        ''',
        'Cannot find matching method Foo$Bar#m()'
    }

    void testStaticOuterMethodCanBeCalledFromStaticClass() {
        assertScript '''
            class Foo {
                static def sm() { 2 }
                static class Bar {
                    void test() {
                        assert sm() == 2
                    }
                }
            }
            new Foo.Bar().test()
        '''
    }

    void testInheritedMethodCanBeCalledFromStaticClass() {
        assertScript '''
            class Foo {
                def m() { 1 }
            }

            class Bar {
                static class Baz extends Foo {
                    void test() {
                        assert m() == 1
                    }
                }
            }
            new Bar.Baz().test()
        '''
    }

    // GROOVY-8445
    void testClosureToFunctionalInterface() {
        assertScript '''
            class Main {
                static main(args) {
                    assert 13 == [1, 2, 3].stream().reduce(7, {Integer r, Integer e -> r + e})
                }
            }
        '''
    }

    //--------------------------------------------------------------------------

    static class MyMethodCallTestClass {
        static String echo(String msg) {
            msg
        }
        static int mul(int... ints) {
            ints.toList().inject(1) { x,y -> x*y }
        }

        int add(int x, int y) { x+y }
        int add(double x, double y) { 2*x+y }
        int sum(int... args) { args.toList().sum() }
    }

    static class MyMethodCallTestClass2<T> extends MyMethodCallTestClass {
        T[] identity(T... args) { args }
    }

    static class MyMethodCallTestClass3 extends MyMethodCallTestClass2<String> {}

    static class GroovyPage {
        final void printHtmlPart(int partNumber) {}
        final void createTagBody(int bodyClosureIndex, Closure<?> bodyClosure) {}
    }

    static class BaseWithProtected {
        protected int m() { 1 }
    }

    static class ChildWithPublic extends BaseWithProtected {
        int m() { 2 }
    }

    static class Child2 extends ChildWithPublic {
    }
}
