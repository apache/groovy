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

    void testCallClosure1() {
        assertScript '''
            def c = { return 'foo' }
            assert c() == 'foo'
        '''
    }

    void testCallClosure2() {
        assertScript '''
            def c = { -> return 'foo' }
            assert c() == 'foo'
        '''
    }

    @NotYetImplemented
    void testCallClosure3() {
        shouldFailWithMessages '''
            def c = { -> }
            c("")
        ''',
        'Cannot call closure that accepts [] with [java.lang.String]'
    }

    void testCallClosure4() {
        assertScript '''
            def c = { int a, int b -> a + b }
            assert c(5, 7) == 12
        '''
    }

    void testCallClosure5() {
        shouldFailWithMessages '''
            def c = { int a, int b -> a + b }
            c('5', '7')
        ''',
        'Cannot call closure that accepts [int, int] with [java.lang.String, java.lang.String]'
    }

    void testCallClosure6() {
        assertScript '''
            def result = { int a, int b -> a + b }(5, 7)
            assert result == 12
        '''
    }

    void testCallClosure7() {
        shouldFailWithMessages '''
            { int a, int b -> a + b }('5', 7)
        ''',
        'Cannot call closure that accepts [int, int] with [java.lang.String, int]'
    }

    // GROOVY-6365
    void testCallClosure8() {
        assertScript '''
            def c = { Object[] args -> args.length }
            assert c('one', 'two') == 2
        '''
    }

    // GROOVY-10071
    void testCallClosure9() {
        assertScript '''
            def c = { ... zeroOrMore -> return 'foo' + zeroOrMore }
            assert c('bar', 'baz') == 'foo[bar, baz]'
            assert c('bar') == 'foo[bar]'
            assert c() == 'foo[]'
        '''
    }

    // GROOVY-10072
    void testCallClosure10() {
        assertScript '''
            def c = { p = 'foo' -> return p }
            assert c('bar') == 'bar'
            assert c() == 'foo'
        '''
    }

    // GROOVY-10072
    void testCallClosure11() {
        assertScript '''
            def c = { Number n, Number total = 41 -> total += n }
            assert c(1) == 42
        '''
    }

    // GROOVY-10072
    void testCallClosure12() {
        shouldFailWithMessages '''
            def c = { Number n, Number total = new Date() -> total += n }
        ''',
        'Cannot assign value of type java.util.Date to variable of type java.lang.Number'
    }

    // GROOVY-10636
    void testCallClosure13() {
        assertScript '''
            def f(Closure<Number>... closures) {
                closures*.call().sum()
            }
            Object result = f({->1},{->2})
            assert result == 3
        '''
    }

    // GROOVY-10636
    void testCallClosure14() {
        shouldFailWithMessages '''
            def f(Closure<Number>... closures) {
            }
            f({->1},{->'x'})
        ''',
        'Cannot return value of type java.lang.String for closure expecting java.lang.Number'
    }

    // GROOVY-11023
    void testCallClosure15() {
        assertScript '''
            def c = { p, q = p.toString() -> '' + p + q }
            assert c('foo', 'bar') == 'foobar'
            assert c('foo') == 'foofoo'
        '''
    }

    //

    void testClosureReturnTypeInference1() {
        assertScript '''
            def c = { int a, int b -> return a + b }
            int total = c(2, 3)
            assert total == 5
        '''
    }

    void testClosureReturnTypeInference2() {
        assertScript '''
            int total = { int a, int b -> return a + b }(2, 3)
        '''
    }

    void testClosureReturnTypeInference3() {
        shouldFailWithMessages '''
            def c = { int x ->
                if (x == 0) {
                    1L
                } else {
                    x
                }
            }
            byte res = c(0)
        ''',
        'Possible loss of precision from long to byte'
    }

    // GROOVY-9907
    void testClosureReturnTypeInference4() {
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

    // GROOVY-9971
    void testClosureReturnTypeInference5() {
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

    // GROOVY-10082, GROOVY-10277
    void testClosureReturnTypeInference6() {
        assertScript '''
            class A {}
            class B extends A {}
            Closure<A> c = { -> new B() }

            def result = c()
            assert result instanceof A
            assert result instanceof B
        '''
        shouldFailWithMessages '''
            Closure<String> c = { -> 42 }
        ''',
        'Cannot return value of type int for closure expecting java.lang.String'
    }

    // GROOVY-10091, GROOVY-10277
    void testClosureReturnTypeInference7() {
        shouldFailWithMessages '''
            class A<T> {}
            class B extends A<Number> {}
            class X extends A<String> {}
            class Y<Z> extends A<Number> {}

            Closure<A<Number>> c
            c = { -> return new B() }
            c = { -> return new X() }
            c = { -> return new Y<String>() }
        ''',
        'Cannot return value of type X for closure expecting A<java.lang.Number>'
    }

    // GROOVY-10792
    void testClosureReturnTypeInference8() {
        shouldFailWithMessages '''
            void proc(Closure<Boolean> c) {
                boolean result = c().booleanValue()
                assert !result
            }
            def list = []
            proc {
                list
            }
        ''',
        'Cannot return value of type java.util.ArrayList<java.lang.Object> for closure expecting java.lang.Boolean'
    }

    // GROOVY-8202
    void testClosureReturnTypeInference9() {
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
            x.charAt(0) // available in (CharSequence & ...)
        '''
    }

    void testClosureSharedVariable2() {
        shouldFailWithMessages '''
            def x = '123';
            { -> x = 123 }
            x.charAt(0) // not available in (Serializable & ...)
        ''',
        'Cannot find matching method (java.io.Serializable & ','#charAt(int)'
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
        ''',
        'Cannot find matching method A#m()'
    }

    // GROOVY-10356
    void testClosureSharedVariable4() {
        assertScript '''
            interface A {
                void m()
            }
            def a = (A) null
            def x = { ->
                a = null
            }
            a?.m()
        '''
    }

    // GROOVY-10052
    void testClosureSharedVariable5() {
        assertScript '''
            String x
            def f = { ->
                x = Optional.of('x').orElseThrow{ new Exception() }
            }
            assert f() == 'x'
            assert x == 'x'
        '''
    }

    // GROOVY-10052
    void testClosureSharedVariable6() {
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

    void testRecurseClosureCallAsMethod() {
        assertScript '''
            Closure<Integer> cl
            cl = { int x -> x == 0 ? x : 1+cl(x-1) }
        '''
    }

    void testFibClosureCallAsMethod() {
        assertScript '''
            Closure<Integer> fib
            fib = { int x-> x<1?x:fib(x-1)+fib(x-2) }
            fib(2)
        '''
    }

    void testFibClosureCallAsMethodFromWithinClass() {
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
        ''',
        'Cannot find matching method java.lang.Object#plus(java.lang.Object)'
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

    // from Grails
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
            Closure a = { int i ->
                println "First closure "+ i
            }
            Closure b = { String s ->
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

    // GROOVY-10221
    void testClosureArgumentCheckWithFlowTyping2() {
        assertScript '''
            class C<T1, T2 extends T1> {
                void test() {
                    def one = { T2 x -> "" }
                    Closure<T2> two = { T2 x -> x }
                    one(two((T2) null))
                }
            }
            new C<Number,Integer>().test()
        '''
    }

    // GROOVY-5705
    void testNPEWhenCallingClosureFromAField() {
        assertScript '''
            class Test {
                Closure c = { it }
                void test() {
                    c("123")
                }
            }

            new Test().test()
        '''
    }

    // GROOVY-6219, GROOVY-10277
    void testClosureReturnDoesNotMatchTarget() {
        shouldFailWithMessages '''
            void printMessage(Closure<String> messageProvider) {
                println "Received message : ${messageProvider()}"
            }

            void testMessage() {
                printMessage { int x, int y -> x+y }
            }
        ''',
        'Cannot return value of type int for closure expecting java.lang.String'
    }

    void testSAMsInMethodSelection1() {
        assertScript '''
            interface MySAM {
                def someMethod()
            }
            def foo(MySAM sam) {
                sam.someMethod()
            }
            assert foo { -> 1 } == 1
            assert foo(() -> 1) == 1
        '''
    }

    // GROOVY-6189, GROOVY-9852
    void testSAMsInMethodSelection2() {
        // overloads with classes implemented by Closure
        [
            'groovy.lang.Closure'            : 'not',
            'groovy.lang.GroovyCallable'     : 'not',
            'groovy.lang.GroovyObject'       : 'not',
            'groovy.lang.GroovyObjectSupport': 'not',

            'java.lang.Object'               : 'sam',
            'java.lang.Runnable'             : 'not',
            'java.lang.Cloneable'            : 'not',
            'java.io.Serializable'           : 'not',
            'java.util.concurrent.Callable'  : 'not',
        ].each { type, which ->
            assertScript """
                interface MySAM {
                    def someMethod()
                }
                def foo($type ref) { 'not' }
                def foo(MySAM sam) { sam.someMethod() }
                assert foo { 'sam' } == '$which' : '$type'
                assert foo(() -> 'sam') == '$which' : '$type'
            """
        }
    }

    // GROOVY-9881
    void testSAMsInMethodSelection3() {
        // Closure implements both and Runnable is "closer"
        assertScript '''
            import java.util.concurrent.Callable
            def foo(Callable c) { 'call' }
            def foo(Runnable r) { 'run'  }
            def which = foo {
                print 'bar'
            }
            assert which == 'run'
        '''
    }

    void testSAMsInMethodSelection4() {
        shouldFailWithMessages '''
            interface One { void m() }
            interface Two { void m() }
            def foo(One one) { one.m() }
            def foo(Two two) { two.m() }
            foo {
                print 'bar'
            }
        ''',
        'Reference to method is ambiguous. Cannot choose between'
    }

    void testSAMsInMethodSelection5() {
        for (spec in ['','x,y ->']) {
            shouldFailWithMessages """
                import java.util.function.Function
                import java.util.function.Supplier
                def foo(Function f) { f.apply(0) }
                def foo(Supplier s) { s.get() }
                foo { $spec
                    'bar'
                }
            """,
            'Reference to method is ambiguous. Cannot choose between'
        }
    }

    void testSAMsInMethodSelection6() {
        for (spec in ['->','x ->']) {
            assertScript """
                import java.util.function.Function
                import java.util.function.Supplier
                def foo(Function f) { f.apply(0) }
                def foo(Supplier s) { s.get() }
                def ret = foo { $spec
                    'bar'
                }
                assert ret == 'bar'
            """
        }
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
    void testAccessStaticFieldFromNestedClosure() {
        assertScript '''
            class A {
              public static final CONST = "a"

              static List doSomething() {
                return (0..1).collect { int x ->
                  (0..1).collect { int y ->
                    return CONST
                  }
                }
              }
            }
            def result = A.doSomething()
            assert result == [['a','a'],['a','a']]
        '''
    }

    // GROOVY-11360
    void testLexicalScopeVersusGetDynamicProperty() {
        config.warningLevel = org.codehaus.groovy.control.messages.WarningMessage.POSSIBLE_ERRORS
        config.targetDirectory = File.createTempDir()
        def parentDir = File.createTempDir()
        try {
            def c = new File(parentDir, 'C.groovy')
            c.write '''
                class C {
                    private static final value = "C"

                    def m(D d) {
                        d.with {
                            return value
                        }
                    }
                }
            '''
            def d = new File(parentDir, 'D.groovy')
            d.write '''
                class D {
                    def get(String name) {
                        if (name == "value") "D"
                    }
                }
            '''
            def e = new File(parentDir, 'E.groovy')
            e.write '''
                String result = new C().m(new D())
                assert result == 'D'
            '''

            def loader = new GroovyClassLoader(this.class.classLoader)
            new org.codehaus.groovy.control.CompilationUnit(config, null, loader).with {
                addSources(c, d, e)
                compile()

                assert errorCollector.hasWarnings()
                assert errorCollector.warnings[0].message.startsWith(
                        'The field: value of class: C is hidden by a dynamic property.')
            }
            loader.addClasspath(config.targetDirectory.absolutePath)
            loader.loadClass('E', true).main()
        } finally {
            config.targetDirectory.deleteDir()
            parentDir.deleteDir()
        }
    }

    // GROOVY-9089
    void testOwnerVersusDelegateFromNestedClosure() {
        String declarations = '''
            class A {
                def p = 'outer delegate'
                def m() { return this.p }
            }
            class B {
                def p = 'inner delegate'
                def m() { return this.p }
            }
            void outer(@DelegatesTo(value=A, strategy=Closure.DELEGATE_FIRST) Closure block) {
                new A().with(block)
            }
            void inner(@DelegatesTo(value=B, strategy=Closure.DELEGATE_FIRST) Closure block) {
                new B().with(block)
            }
        '''

        assertScript declarations + '''
            outer {
                inner {
                    assert m() == 'inner delegate'
                    assert owner.m() == 'outer delegate'
                    assert delegate.m() == 'inner delegate'
                }
            }
        '''

        assertScript declarations + '''
            outer {
                inner {
                    assert p == 'inner delegate'
                    assert owner.p == 'outer delegate'
                    assert delegate.p == 'inner delegate'
                }
            }
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

    void testTypeCheckingOfClosureInMapConstructorCalls() {
        shouldFailWithMessages '''
            class Foo {
                Number bar
                Object baz
            }

            new Foo(bar: 123, baz: { ->
                int i = Integer
            })
        ''',
        'Cannot assign value of type java.lang.Class<java.lang.Integer> to variable of type int'

        shouldFailWithMessages '''
            import groovy.transform.NamedParam

            class Foo {
                Foo(@NamedParam(value='bar',type=Number) Map<String,?> named, Object positional) {
                }
            }

            new Foo(bar: Number, { ->
                int i = Integer
            })
        ''',
        'Cannot assign value of type java.lang.Class<java.lang.Integer> to variable of type int',
        "named param 'bar' has type 'java.lang.Class<java.lang.Number>' but expected 'java.lang.Number'"
    }

    // GROOVY-10602
    void testMethodAndClosureParametersDefaultArguments() {
        assertScript '''import java.util.function.*
            String test(Closure one = { p ->
                Closure two = { Supplier s = { -> p } ->
                    s.get()
                }
                two()
            }) {
                one('foo')
            }

            String result = test()
            assert result == 'foo'
        '''
    }
}
