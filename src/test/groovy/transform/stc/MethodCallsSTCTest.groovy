/*
 * Copyright 2003-2012 the original author or authors.
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
package groovy.transform.stc

import org.codehaus.groovy.control.customizers.ImportCustomizer;
import static org.codehaus.groovy.control.CompilerConfiguration.DEFAULT as config

/**
 * Unit tests for static type checking : method calls.
 *
 * @author Cedric Champeau
 */
class MethodCallsSTCTest extends StaticTypeCheckingTestCase {
    @Override
    protected void configure() {
        final ImportCustomizer ic = new ImportCustomizer()
        ic.addImport('A','groovy.transform.stc.MethodCallsSTCTest.MyMethodCallTestClass')
        ic.addImport('B','groovy.transform.stc.MethodCallsSTCTest.MyMethodCallTestClass2')
        ic.addImport('C','groovy.transform.stc.MethodCallsSTCTest.MyMethodCallTestClass3')
        config.addCompilationCustomizers(
                ic
        )
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
        // this is bad style, but supported by java
        assertScript '''
            A a = new A()
            String echo = a.echo 'echo'
            assert  echo == 'echo'
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

    // GROOVY-5175
    void testCallMethodAcceptingArrayWithNull() {
        assertClass '''
            class Foo {
                def say() {
                    methodWithArrayParam(null) // STC Error
                }
                def methodWithArrayParam(String[] s) {

                }
            }
        '''
    }

    // GROOVY-5175
    void testCallMethodWithNull() {
        assertClass '''
            class Foo {
                def say() {
                    methodWithArrayParam(null)
                }
                def methodWithArrayParam(Date date) {

                }
            }
        '''
    }

    // GROOVY-5175
    void testCallMethodWithNullAndAnotherParameter() {
        assertClass '''
            class Foo {
                def say() {
                    methodWithArrayParam(null, new Date())
                }
                def methodWithArrayParam(Date date1, Date date2) {

                }
            }
        '''
    }

    // GROOVY-5175
    void testAmbiguousCallMethodWithNullAndAnotherParameter() {
        shouldFailWithMessages '''
            class Foo {
                def say() {
                    methodWithArrayParam(null, new Date())
                }
                def methodWithArrayParam(Date date1, Date date2) {

                }
                def methodWithArrayParam(String o, Date date2) {

                }
            }
        ''', 'Reference to method is ambiguous'
    }

    // GROOVY-5175
    void testDisambiguateCallMethodWithNullAndAnotherParameter() {
        assertClass '''
            class Foo {
                def say() {
                    methodWithArrayParam((Date)null, new Date())
                }
                def methodWithArrayParam(Date date1, Date date2) {

                }
                def methodWithArrayParam(String o, Date date2) {

                }
            }
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
                boolean instanceMethod() { true }

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
        assertScript '''import groovy.transform.stc.MethodCallsSTCTest.Child2
            class A {
                int delegate() {
                    @ASTTest(phase=INSTRUCTION_SELECTION, value={
                        def md = node.rightExpression.getNodeMetaData(DIRECT_METHOD_CALL_TARGET)
                        assert md.declaringClass.nameWithoutPackage == 'MethodCallsSTCTest$ChildWithPublic'
                    })
                    int res = new Child2().m()
                    res
                }
            }
            assert new A().delegate() == 2
        '''
    }

    // GROOVY-5580
    void testGetNameAsPropertyFromSuperInterface() {
        assertScript '''interface Upper { String getName() }
        interface Lower extends Upper {}
        String foo(Lower impl) {
            impl.name // getName() called with the property notation
        }
        assert foo({ 'bar' } as Lower) == 'bar'
        '''
    }

    void testGetNameAsPropertyFromSuperInterfaceUsingConcreteImpl() {
        assertScript '''interface Upper { String getName() }
        interface Lower extends Upper {}
        class Foo implements Lower { String getName() { 'bar' } }
        String foo(Foo impl) {
            impl.name // getName() called with the property notation
        }
        assert foo(new Foo()) == 'bar'
        '''
    }

    void testGetNameAsPropertyFromSuperInterfaceUsingConcreteImplSubclass() {
        assertScript '''interface Upper { String getName() }
        interface Lower extends Upper {}
        class Foo implements Lower { String getName() { 'bar' } }
        class Bar extends Foo {}
        String foo(Bar impl) {
            impl.name // getName() called with the property notation
        }
        assert foo(new Bar()) == 'bar'
        '''
    }

    // GROOVY-5580: getName variant
    void testGetNameFromSuperInterface() {
        assertScript '''interface Upper { String getName() }
        interface Lower extends Upper {}
        String foo(Lower impl) {
            impl.getName()
        }
        assert foo({ 'bar' } as Lower) == 'bar'
        '''
    }

    void testGetNameFromSuperInterfaceUsingConcreteImpl() {
        assertScript '''interface Upper { String getName() }
        interface Lower extends Upper {}
        class Foo implements Lower { String getName() { 'bar' } }
        String foo(Foo impl) {
             impl.getName()
        }
        assert foo(new Foo()) == 'bar'
        '''
    }

    void testGetNameFromSuperInterfaceUsingConcreteImplSubclass() {
        assertScript '''interface Upper { String getName() }
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
                'The spread operator cannot be used as argument of method or closure calls with static type checking because the number of arguments cannot be determined at compile time'
    }

    void testBoxingShouldCostMore() {
        if (config.optimizationOptions.indy) return;
        assertScript '''
            int foo(int x) { 1 }
            int foo(Integer x) { 2 }
            int bar() {
                foo(1)
            }
            assert bar() == 1
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
            interface Top { void close() }
            interface Middle extends Top {}
            interface Bottom extends Middle {}
            void foo(Bottom obj) {
               obj.close()
            }
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
    
    // GROOVY-5883 and GROOVY-6270
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

    static class MyMethodCallTestClass {

        static int mul(int... args) { args.toList().inject(1) { x,y -> x*y } }
        static String echo(String msg) {
            msg
        }

        int add(int x, int y) { x+y }
        int add(double x, double y) { 2*x+y } // stupid but useful for tests :)
        int sum(int... args) { args.toList().sum() }

    }

    static class MyMethodCallTestClass2<T> extends MyMethodCallTestClass {
        T[] identity(T... args) { args }
    }

    static class MyMethodCallTestClass3 extends MyMethodCallTestClass2<String> {}

    static class GroovyPage {
        public final void printHtmlPart(final int partNumber) {}
        public final void createTagBody(int bodyClosureIndex, Closure<?> bodyClosure) {}
    }

    public static class BaseWithProtected {
        protected int m() { 1 }
    }

    public static class ChildWithPublic extends BaseWithProtected {
        public int m() { 2 }
    }

    public static class Child2 extends ChildWithPublic {
    }
}
