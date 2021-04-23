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

import groovy.test.NotYetImplemented

/**
 * Unit tests for static type checking : closures.
 */
class ClosuresSTCTest extends StaticTypeCheckingTestCase {

    void testClosureWithoutArguments() {
        assertScript '''
        def clos = { println "hello!" }

        println "Executing the Closure:"
        clos() //prints "hello!"
        '''
    }

    // GROOVY-9079: no params to statically type check but shouldn't get NPE
    void testClosureWithoutArgumentsExplicit() {
        assertScript '''
            import java.util.concurrent.Callable

            String makeFoo() {
                Callable<String> call = { -> 'foo' }
                call()
            }

            assert makeFoo() == 'foo'
        '''
    }

    void testClosureWithArguments() {
        assertScript '''
            def printSum = { int a, int b -> print a+b }
            printSum( 5, 7 ) //prints "12"
        '''

        shouldFailWithMessages '''
            def printSum = { int a, int b -> print a+b }
            printSum( '5', '7' ) //prints "12"
        ''', 'Closure argument types: [int, int] do not match with parameter types: [java.lang.String, java.lang.String]'
    }

    void testClosureWithArgumentsAndNoDef() {
        assertScript '''
            { int a, int b -> print a+b }(5,7)
        '''
    }

    void testClosureWithArgumentsNoDefAndWrongType() {
        shouldFailWithMessages '''
            { int a, int b -> print a+b }('5',7)
        ''', 'Closure argument types: [int, int] do not match with parameter types: [java.lang.String, int]'
    }

    void testClosureReturnTypeInference1() {
        assertScript '''
            def closure = { int x, int y -> return x+y }
            int total = closure(2,3)
        '''
    }

    void testClosureReturnTypeInference2() {
        shouldFailWithMessages '''
            def closure = { int x, int y -> return x+y }
            int total = closure('2',3)
        ''', 'Closure argument types: [int, int] do not match with parameter types: [java.lang.String, int]'
    }

    void testClosureReturnTypeInference3() {
        assertScript '''
            int total = { int x, int y -> return x+y }(2,3)
        '''
    }

    void testClosureReturnTypeInference4() {
        shouldFailWithMessages '''
            def cl = { int x ->
                if (x==0) {
                    1L
                } else {
                    x // int
                }
            }
            byte res = cl(0) // should throw an error because return type inference should be a long
        ''', 'Possible loss of precision from long to byte'
    }

    // GROOVY-9907
    void testClosureReturnTypeInference5() {
        assertScript '''
            Integer foo(x) {
                if (x instanceof Integer) {
                    def bar = { -> return x }
                    return bar.call()
                }
                return 0
            }
            assert foo(1) == 1
        '''
    }

    // GROOVY-8427
    void testClosureReturnTypeInference6() {
        assertScript '''
            import java.util.function.Consumer

            class C {
                static <T> void m(T a, Consumer<T> c) {
                    c.accept(a)
                }
                static void main(args) {
                    def c = { ->
                        int x = 0
                        m('') {
                            print 'void return'
                        }
                    }
                    c.call()
                }
            }
        '''
    }

    // GROOVY-8202
    void testClosureReturnTypeInference7() {
        assertScript '''
            void proc() {
            }
            String test0(flag) {
              if (flag) {
                'foo'
              } else {
                proc()
              }
            }
            String test1(flag) {
              Closure<String> c = { ->
                if (flag) {
                  'bar'
                } else {
                  proc()
                  null
                }
              }
              c.call()
            }
            String test2(flag) {
              Closure<String> c = { -> // Cannot assign Closure<Object> to Closure<String>
                if (flag) {
                  'baz'
                } else {
                  proc()
                }
              }
              c.call()
            }

            assert test0(true) == 'foo'
            assert test1(true) == 'bar'
            assert test2(true) == 'baz'
            assert test0(false) == null
            assert test1(false) == null
            assert test2(false) == null
        '''

        assertScript '''
            Closure<Void> c = { flag ->
                if (flag) {
                    print 'true'
                } else {
                    print 'false'
                }
            }
        '''
    }

    // GROOVY-9971
    void testClosureReturnTypeInference8() {
        assertScript '''
            def m(Closure<String> c) {
                c.call()
            }
            final x = 123
            Closure<String> c = { -> "x=$x" }
            String type = c.call().class.name
            assert type == 'java.lang.String'
            type = (m { -> "x=$x" }).class.name
            assert type == 'java.lang.String' // not GStringImpl
        '''
    }

    // GROOVY-5145
    void testCollect() {
        assertScript '''
            List<String> strings = [1,2,3].collect { it.toString() }
        '''
    }

    // GROOVY-5145
    void testCollectWithSubclass() {
        assertScript '''
            class StringClosure extends Closure<String> {
                StringClosure() { super(null,null) }
                void doCall(int x) { x }
            }
            List<String> strings = [1,2,3].collect(new StringClosure())
        '''
    }

    // GROOVY-7701
    void testWithDelegateVsOwnerField() {
        assertScript '''
            class Foo {
                List type
            }

            class Bar {
                int type = 10

                @Lazy
                List<Foo> something = { ->
                    List<Foo> tmp = []
                    def foo = new Foo()
                    foo.with {
                        type = ['String']
                    //  ^^^^ should be Foo.type, not Bar.type
                    }
                    tmp.add(foo)
                    tmp
                }()
            }

            def bar = new Bar()
            assert bar.type == 10
            assert bar.something*.type == [['String']]
            assert bar.type == 10
        '''
    }

    void testClosureSharedVariable1() {
        assertScript '''
            def x = '123';
            { -> x = new StringBuffer() }
            x.charAt(0) // available in String and StringBuffer
        '''
    }

    void testClosureSharedVariable2() {
        shouldFailWithMessages '''
            def x = '123';
            { -> x = 123 }
            x.charAt(0) // available in String but not available in Integer
        ''', 'Cannot find matching method java.io.Serializable or java.lang.Comparable'
    }

    // GROOVY-9516
    void testClosureSharedVariable3() {
        shouldFailWithMessages '''
            class A {}
            class B extends A { def m() {} }
            class C extends A {}

            void test() {
              def x = new B();
              { -> x = new C() }();
              def c = x
              c.m()
            }
        ''', 'Cannot find matching method A#m()'
    }

    // GROOVY-10052
    void testClosureSharedVariable4() {
        assertScript '''
            String x
            def f = { ->
                x = Optional.of('x').orElseThrow{ new Exception() }
            }
            assert f() == 'x'
            assert x == 'x'
        '''
    }

    @NotYetImplemented // GROOVY-10052
    void testClosureSharedVariable5() {
        assertScript '''
            def x
            def f = { ->
                x = Optional.of('x').orElseThrow{ new Exception() }
            }
            assert f() == 'x'
            assert x == 'x'
        '''
    }

    // GROOVY-10052
    void testNotClosureSharedVariable() {
        assertScript '''
            String x = Optional.of('x').orElseThrow{ new Exception() }
            def f = { ->
                String y = Optional.of('y').orElseThrow{ new Exception() }
            }

            assert x == 'x'
            assert f() == 'y'
        '''
    }

    void testClosureCallAsAMethod() {
        assertScript '''
            Closure cl = { 'foo' }
            assert cl() == 'foo'
        '''
    }

    void testClosureCallWithOneArgAsAMethod() {
        assertScript '''
            Closure cl = { int x -> "foo$x" }
            assert cl(1) == 'foo1'
        '''
    }

    void testRecurseClosureCallAsAMethod() {
        assertScript '''
            Closure<Integer> cl
            cl = { int x-> x==0?x:1+cl(x-1) }
        '''
    }

    void testFibClosureCallAsAMethod() {
        assertScript '''
            Closure<Integer> fib
            fib = { int x-> x<1?x:fib(x-1)+fib(x-2) }
            fib(2)
        '''
    }

    void testFibClosureCallAsAMethodFromWithinClass() {
        assertScript '''
            class FibUtil {
                private Closure<Integer> fibo
                FibUtil() {
                    fibo = { int x-> x<1?x:fibo(x-1)+fibo(x-2) }
                }

                int fib(int n) { fibo(n) }
            }
            FibUtil fib = new FibUtil()
            fib.fib(2)
        '''
    }

    void testClosureRecursionWithoutClosureTypeArgument() {
        shouldFailWithMessages '''
            Closure fib
            fib = { int n -> n<2?n:fib(n-1)+fib(n-2) }
        ''', 'Cannot find matching method java.lang.Object#plus(java.lang.Object)'
    }

    void testClosureRecursionWithDef() {
        shouldFailWithMessages '''
            def fib
            fib = { int n -> n<2?n:fib(n-1)+fib(n-2) }
        ''',
                'Cannot find matching method java.lang.Object#plus(java.lang.Object)',
                'Cannot find matching method java.lang.Object#call(int)',
                'Cannot find matching method java.lang.Object#call(int)'
    }

    void testClosureRecursionWithClosureTypeArgument() {
        assertScript '''
            Closure<Integer> fib
            fib = { int n -> n<2?n:fib(n-1)+fib(n-2) }
        '''
    }

    void testClosureMemoizeWithClosureTypeArgument() {
        assertScript '''
            Closure<Integer> fib
            fib = { int n -> n<2?n:fib(n-1)+fib(n-2) }
            def memoized = fib.memoizeAtMost(2)
            assert fib(5) == memoized(5)
        '''
    }

    // GROOVY-5639
    void testShouldNotThrowClosureSharedVariableError() {
        assertScript '''
        Closure<Void> c = {
            List<String> list = new ArrayList<String>()
            String s = "foo"
            10.times {
                list.add(s)
            }
        }
        '''
    }

    // a case in Grails
    void testShouldNotThrowClosureSharedVariableError2() {
        assertScript '''
            class AntPathMatcher {
                boolean match(String x, String y) { true }
            }
            private String relativePath() { '' }
            def foo() {
                AntPathMatcher pathMatcher = new AntPathMatcher()
                def relPath = relativePath()
                def cl = { String it ->
                    pathMatcher.match(it, relPath)
                }
                cl('foo')
            }

            foo()
        '''
    }

    // GROOVY-5693
    void testClosureArgumentCheckWithFlowTyping() {
        assertScript '''
            Closure a = {
                int i ->
                println "First closure "+ i
            }
            Closure b = {
                String s ->
                println "Second closure "+ s
            }
            a(5)
            Closure c = a
            a=b
            a("Testing!")
            a = c
            a(5)
            a=a
            a(5)
            a = b
            a('Testing!')
        '''
    }

    // GROOVY-5705
    void testNPEWhenCallingClosureFromAField() {
        assertScript '''
            import groovy.transform.*

            class Test {
                Closure c = { it }

                @TypeChecked
                void test() {
                    c("123")
                }
            }

            new Test().test()
        '''
    }

    // GROOVY-6219
    void testShouldFailBecauseClosureReturnTypeDoesnMatchMethodSignature() {
        shouldFailWithMessages '''
            void printMessage(Closure<String> messageProvider) {
                println "Received message : ${messageProvider()}"
            }

            void testMessage() {
                printMessage { int x, int y -> x+y }
            }
        ''', 'Cannot find matching method'
    }

    // GROOVY-6189
    void testSAMsInMethodSelection() {
        // simple direct case
        assertScript """
            interface MySAM {
                def someMethod()
            }
            def foo(MySAM sam) {sam.someMethod()}
            assert foo {1} == 1
        """

        // overloads with classes implemented by Closure
        ["java.util.concurrent.Callable", "Object", "Closure", "GroovyObjectSupport", "Cloneable", "Runnable", "GroovyCallable", "Serializable", "GroovyObject"].each {
            className ->
            assertScript """
                interface MySAM {
                    def someMethod()
                }
                def foo(MySAM sam) {sam.someMethod()}
                def foo($className x) {2}
                assert foo {1} == 2
            """
        }
    }

    void testSAMVariable() {
        assertScript """
            interface SAM { def foo(); }

            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                assert node.getNodeMetaData(INFERRED_TYPE).name == 'SAM'
            })
            SAM s = {1}
            assert s.foo() == 1
            def t = (SAM) {2}
            assert t.foo() == 2
        """
    }

    // GROOVY-7927
    void testSAMGenericsInAssignment() {
        assertScript """
            interface SAM<T,R> { R accept(T t); }
            SAM<Integer,Integer> s = { Integer n -> -n }
            assert s.accept(1) == -1
        """
    }

    void testSAMProperty() {
        assertScript """
            interface SAM { def foo(); }
            class X {
                SAM s
            }
            def x = new X(s:{1})
            assert x.s.foo() == 1
        """
    }

    void testSAMAttribute() {
        assertScript """
            interface SAM { def foo(); }
            class X {
                public SAM s
            }
            def x = new X()
            x.s = {1}
            assert x.s.foo() == 1
            x = new X()
            x.@s = {2}
            assert x.s.foo() == 2
        """
    }

    void testMultipleSAMSignature() {
        assertScript '''
            interface SAM { def foo() }
            def method(SAM a, SAM b) {
                a.foo()
                b.foo()
            }
            method({println 'a'}, {println 'b'})
        '''
    }

    void testMultipleSAMSignature2() {
        assertScript '''
            interface SAM { def foo() }
            def method(Object o, SAM a, SAM b) {
                a.foo()
                b.foo()
            }
            method(new Object(), {println 'a'}, {println 'b'})
        '''
    }

    void testMultipleSAMMethodWithClosure() {
        assertScript '''
            interface SAM { def foo() }
            def method(SAM a, SAM b) {
                a.foo()
                b.foo()
            }
            def method(Closure a, SAM b) {
                b.foo()
            }
            def called = false
            method({called = true;println 'a'}, {println 'b'})
            assert !called
        '''
    }

    void testMultipleSAMMethodWithClosureInverted() {
        assertScript '''
            interface SAM { def foo() }
            def method(SAM a, SAM b) {
                a.foo()
                b.foo()
            }
            def method(SAM a, Closure b) {
                a.foo()
            }
            def called = false
            method({println 'a'}, {called=true;println 'b'})
            assert !called
        '''
    }

    void testAmbiguousSAMOverload() {
        shouldFailWithMessages '''
            interface Sammy { def sammy() }
            interface Sam { def sam() }
            def method(Sam sam) { sam.sam() }
            def method(Sammy sammy) { sammy.sammy() }
            method {
                println 'foo'
            }
        ''', 'Reference to method is ambiguous. Cannot choose between'
    }

    void testSAMType() {
        assertScript """
            interface Foo {int foo()}
            Foo f = {1}
            assert f.foo() == 1
            abstract class Bar implements Foo {}
            Bar b = {2}
            assert b.foo() == 2
        """
        shouldFailWithMessages """
            interface Foo2 {
                String toString()
            }
            Foo2 f2 = {int i->"hi"}
        """, "Cannot assign"
        shouldFailWithMessages """
            interface Foo2 {
                String toString()
            }
            abstract class Bar2 implements Foo2 {}
            Bar2 b2 = {"there"}
        """, "Cannot assign"
        assertScript """
            interface Foo3 {
                boolean equals(Object)
                int f()
            }
            Foo3 f3 = {1}
            assert f3.f() == 1
        """
        shouldFailWithMessages """
            interface Foo3 {
                boolean equals(Object)
                int f()
            }
            abstract class Bar3 implements Foo3 {
                int f(){2}
            }
            Bar3 b3 = {2}
        """, "Cannot assign"
    }

    // GROOVY-6238
    void testDirectMethodCallOnClosureExpression() {
        assertScript '''
            @ASTTest(phase=INSTRUCTION_SELECTION,value={
                def dit = node.getNodeMetaData(INFERRED_TYPE)
                def irt = node.rightExpression.getNodeMetaData(INFERRED_TYPE)
                assert irt == CLOSURE_TYPE
                assert dit == CLOSURE_TYPE
            })
            def cl = { it }.curry(42)
            def val = cl.call()
            assert val == 42
        '''
    }

    // GROOVY-6343
    void testAccessStaticFieldFromNestedClosures() {
        assertScript '''
            class A {

              public static final CONST = "a"

              public static List doSomething() {
                return (0..1).collect{ int x ->
                  (0..1).collect{ int y ->
                    return CONST
                  }
                }
              }
            }
            A.doSomething()
        '''
    }

    void testParameterlessClosureToSAMTypeArgumentCoercion() {
        assertScript '''
            interface SamType {
                int sam()
            }

            int foo(SamType samt) {
                samt.sam()
            }

            assert foo { -> 1 }  == 1
        '''
    }

    // GROOVY-9558
    void testPutAtClosureDelegateProperty() {
        assertScript '''
            def config = new org.codehaus.groovy.control.CompilerConfiguration()
            config.tap {
                optimizationOptions['indy'] = true
                optimizationOptions.indy = true
            }
        '''
    }

    // GROOVY-9652
    void testDelegatePropertyAndCharCompareOptimization() {
        ['String', 'Character', 'char'].each { type ->
            assertScript """
                class Node {
                    String name
                    ${type} text
                }
                class Root implements Iterable<Node> {
                    @Override
                    Iterator<Node> iterator() {
                        return [
                            new Node(name: 'term', text: (${type}) 'a'),
                            new Node(name: 'dash', text: (${type}) '-'),
                            new Node(name: 'term', text: (${type}) 'b')
                        ].iterator()
                    }
                }

                void test() {
                    Root root = new Root()
                    root[0].with {
                        assert name == 'term'
                        assert text == 'a' // GroovyCastException: Cannot cast object 'script@b91d8c4' with class 'script' to class 'bugs.Node'
                    }
                }

                test()
            """
        }
    }
}
