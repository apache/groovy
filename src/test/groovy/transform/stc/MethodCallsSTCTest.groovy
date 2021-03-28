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
import org.codehaus.groovy.control.customizers.ImportCustomizer

/**
 * Unit tests for static type checking : method calls.
 */
class MethodCallsSTCTest extends StaticTypeCheckingTestCase {

    @Override
    protected void configure() {
        config.addCompilationCustomizers(new ImportCustomizer().tap {
            addImport('A', 'groovy.transform.stc.MethodCallsSTCTest.MyMethodCallTestClass' )
            addImport('B', 'groovy.transform.stc.MethodCallsSTCTest.MyMethodCallTestClass2')
            addImport('C', 'groovy.transform.stc.MethodCallsSTCTest.MyMethodCallTestClass3')
        })
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
        ''', 'Cannot find matching method'
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
        ''', 'Cannot find matching method'
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
            assert  echo == 'echo'
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
            assert arr == ['1','2','3']  as String[]
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
            assert 1+foo() == 2
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
        ''', 'Cannot call groovy.transform.stc.MethodCallsSTCTest$MyMethodCallTestClass2#identity(java.lang.Integer[]) with arguments [java.lang.String[]]'
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
        ''', 'm(java.util.List <java.lang.Integer>). Please check if the declared type is correct and if the method exists.'
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
        ''', '[Static type checking] - Cannot find matching method Main#<init>()'
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
                def bar(String o, Date date2) {
                }
                def foo() {
                    bar(null, new Date())
                }
            }
        ''', 'Reference to method is ambiguous'
    }

    // GROOVY-5175
    void testDisambiguateCallMethodWithNullAndAnotherParameter() {
        assertClass '''
            class Test {
                def bar(Date date1, Date date2) {
                }
                def bar(String o, Date date2) {
                }
                def foo() {
                    bar((Date)null, new Date())
                }
            }
        '''
    }

    void testMethodCallWithDefaultParams() {
        assertScript '''
            import groovy.transform.*
            @TypeChecked(TypeCheckingMode.SKIP)
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
        ''', 'Cannot find matching method'
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
        ''', 'Reference to method is ambiguous'
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
        ''', 'Reference to method is ambiguous'
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
            void test() {
                List<Integer> nums = [1, 2, 3, -2, -5, 6]
                Collections.sort(nums, { a, b -> a.abs() <=> b.abs() })
            }
            test()
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

    void testShouldFailBecauseVariableIsReassigned() {
        shouldFailWithMessages '''
            static String foo(String s) {
                'String'
            }
            def it
            if (it instanceof String) {
                it = new Date()
                foo(it)
            }
        ''', 'foo(java.util.Date)'
    }

    void testShouldNotFailEvenIfVariableIsReassigned() {
        assertScript '''
            static String foo(int val) {
                'int'
            }
            def it
            if (it instanceof String) {
                it = 123
                foo(it)
            }
        '''
    }

    void testShouldNotFailEvenIfVariableIsReassignedAndInstanceOfIsEmbed() {
        assertScript '''
            static String foo(int val) {
                'int'
            }
            static String foo(Date val) {
                'Date'
            }
            def it
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
        ''', '#m(int)'
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
        ''', 'm(java.lang.String, java.lang.Object)'
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
        ''', '#m(java.lang.String, int, java.lang.String, java.lang.String)'
    }

    void testMultipleDefaultArgsWithMixedTypesAndWrongType() {
        shouldFailWithMessages '''
            String m(String first = 'first', int second, String third = 'third') {
                return first.toUpperCase() + ' ' + second + ' ' + third.toUpperCase()
            }
            m('hello') // no value set for "second"
        ''', '#m(java.lang.String)'
    }

    void testShouldNotFailWithAmbiguousMethodSelection() {
        assertScript '''
            StringBuffer sb = new StringBuffer()
            sb.append('foo')
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
        ''', '#square(double)'
    }

    void testShouldNotBeAbleToCallMethodUsingLongWithFloatOrDouble() {
        shouldFailWithMessages '''
            float square(long x) { x*x }
            assert square(2.0d) == 4.0d
            assert square(2.0f) == 4.0d
        ''', '#square(double)', '#square(float)'
    }

    void testShouldNotAllowMethodCallFromStaticContext() {
        shouldFailWithMessages '''
            class A {
                void instanceMethod() {}
                static void staticMethod() {
                    instanceMethod() // calling instance method from static context
                }
            }
            A.staticMethod()
        ''', 'Non static method A#instanceMethod cannot be called from static context'
    }

    void testShouldNotAllowMethodCallFromStaticConstructor() {
        shouldFailWithMessages '''
            class A {
                void instanceMethod() {}
                static {
                    instanceMethod() // calling instance method from static context
                }
            }
            new A()
        ''', 'Non static method A#instanceMethod cannot be called from static context'
    }

    void testShouldNotAllowMethodCallFromStaticField() {
        shouldFailWithMessages '''
            class A {
                boolean instanceMethod() {}
                static FOO = instanceMethod()
            }
            new A()
        ''', 'Non static method A#instanceMethod cannot be called from static context'
    }

    // GROOVY-5495
    void testShouldFindMethodFromSuperInterface() {
        assertScript '''
            class ClassUnderTest {
                void methodFromString(SecondInterface si) {
                    si.methodFromSecondInterface();
                    si.methodFromFirstInterface();
                }
            }

            interface FirstInterface {
                void methodFromFirstInterface();
            }

            interface SecondInterface extends FirstInterface {
                void methodFromSecondInterface();
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
        assertScript '''
            import groovy.transform.stc.MethodCallsSTCTest.Child2 as C2
            class A {
                int delegate() {
                    @ASTTest(phase=INSTRUCTION_SELECTION, value={
                        def md = node.rightExpression.getNodeMetaData(DIRECT_METHOD_CALL_TARGET)
                        assert md.declaringClass.nameWithoutPackage == 'MethodCallsSTCTest$ChildWithPublic'
                    })
                    int res = new C2().m()
                    res
                }
            }
            assert new A().delegate() == 2
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

    void testGetNameFromSuperInterfaceUsingConcreteImpl() {
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

    void testGetNameFromSuperInterfaceUsingConcreteImplSubclass() {
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

    void testSpreadArgsForbiddenInMethodCall() {
        shouldFailWithMessages '''
            void foo(String a, String b, int c, double d1, double d2) {}
            void bar(String[] args, int c, double[] nums) {
                foo(*args, c, *nums)
            }
        ''',
        'The spread operator cannot be used as argument of method or closure calls with static type checking because the number of arguments cannot be determined at compile time',
        'The spread operator cannot be used as argument of method or closure calls with static type checking because the number of arguments cannot be determined at compile time',
        'Cannot find matching method'
    }

    void testSpreadArgsForbiddenInStaticMethodCall() {
        shouldFailWithMessages '''
            static void foo(String a, String b, int c, double d1, double d2) {}
            static void bar(String[] args, int c, double[] nums) {
                foo(*args, c, *nums)
            }
        ''',
        'The spread operator cannot be used as argument of method or closure calls with static type checking because the number of arguments cannot be determined at compile time',
        'The spread operator cannot be used as argument of method or closure calls with static type checking because the number of arguments cannot be determined at compile time',
        'Cannot find matching method'
    }

    void testSpreadArgsForbiddenInConstructorCall() {
        shouldFailWithMessages '''
            class SpreadInCtor {
                SpreadInCtor(String a, String b) { }
            }
            new SpreadInCtor(*['A', 'B'])
        ''',
        'The spread operator cannot be used as argument of method or closure calls with static type checking because the number of arguments cannot be determined at compile time',
        'Cannot find matching method SpreadInCtor#<init>(java.util.List <E extends java.lang.Object>)'
    }

    void testSpreadArgsForbiddenInClosureCall() {
        shouldFailWithMessages '''
            def closure = { String a, String b, String c -> println "$a $b $c" }
            def strings = ['A', 'B', 'C']
            closure(*strings)
        ''',
        'The spread operator cannot be used as argument of method or closure calls with static type checking because the number of arguments cannot be determined at compile time',
        'Closure argument types: [java.lang.String, java.lang.String, java.lang.String] do not match with parameter types: [java.lang.Object]'
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

    void testVargsSelection() {
        assertScript '''
            int foo(int x, Object... args) { 1 }
            int foo(Object... args) { 2 }
            assert foo(1) == 1
            assert foo() == 2
            assert foo(1,2) == 1
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
        assertScript '''
            import java.nio.charset.Charset
            Charset charset = Charset.forName('UTF-8')
            assert charset instanceof Charset
        '''
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
                    super.foo() // compiles and creates StackOverFlow
                }

            }
            Bottom.foo()
            assert Top.called
        '''
    }

    void testShouldFindSetProperty() {
        assertScript '''
            class A {
                int x
                void foo() {
                    this.setProperty('x', 1)
                }
            }
            def a = new A()
            a.foo()
            assert a.x == 1
        '''
    }

    // GROOVY-5888
    void testStaticContextScoping() {
        assertScript '''
            class A {
                static List foo = 'a,b,c'.split(/,/).toList()*.trim()
            }
            assert A.foo == ['a','b','c']
        '''
    }

    // GROOVY-6147
    void testVargsCallWithOverloadedMethod() {
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
    void testShouldNotThrowAmbiguousVargs() {
        assertScript '''
            def list = ['a', 'b', 'c']
            Object[] arr = list.toArray()
            println arr
        '''
    }

    void testOverloadedMethodWithVargs() {
        assertScript '''import org.codehaus.groovy.classgen.asm.sc.support.Groovy6235SupportSub as Support
            def b = new Support()
            assert b.overload() == 1
            assert b.overload('a') == 1
            assert b.overload('a','b') == 2
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
        ''', 'Cannot find matching method java.lang.Double#isFiniteMissing(double)'

        shouldFailWithMessages '''
            String.doSomething()
        ''', 'Cannot find matching method java.lang.String#doSomething()'
    }

    // GROOVY-6646
    void testNPlusVargsCallInOverloadSituation() {
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

    // GROOVY-6776
    void testPrimtiveParameterAndNullArgument() {
        shouldFailWithMessages '''
            def foo(int i){}
            def bar() {
                foo null
            }
            bar()
        ''', '#foo(int) with arguments [<unknown parameter type>]'
    }

    // GROOVY-6751
    void testMethodInBothInterfaceAndSuperclass() {
        assertScript '''
            interface Ifc {
              Object getProperty(String s)
            }

            class DuplicateMethodInIfc implements Ifc {}  // implemented in groovy.lang.GroovyObject

            class Tester {
              DuplicateMethodInIfc dup = new DuplicateMethodInIfc()
              Object obj = dup.getProperty("foo")
            }

            try { new Tester()}
            catch(groovy.lang.MissingPropertyException expected) {}
        '''
    }

    // GROOVY-7813
    void testNonStaticOuterMethodCannotBeCalledFromStaticClass() {
        shouldFailWithMessages '''
            class Foo {
                def bar() { 2 }

                static class Baz {
                    def doBar() { bar() }
                }
            }
            null
        ''', 'Non static method Foo#bar cannot be called from static context'
    }

    void testStaticOuterMethodCanBeCalledFromStaticClass() {
        assertScript '''
            class Foo {
                static def bar() { 2 }

                static class Baz {
                    def doBar() {
                        bar()
                    }
                }
            }
            assert new Foo.Baz().doBar() == 2
        '''
    }

    void testInheritedMethodCanBeCalledFromStaticClass() {
        assertScript '''
            class Bar {
                def bar() { 1 }
            }

            class Foo {
                static class Baz extends Bar {
                    def doBar() {
                        bar()
                    }
                }
            }
            assert new Foo.Baz().doBar() == 1
        '''
    }

    // GROOVY-8445
    void testClosureToFunctionalInterface() {
        assertScript '''
            public class Main {
                public static void main(String[] args) {
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
