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

import groovy.transform.CompileStatic
import org.codehaus.groovy.runtime.InvokerHelper
import org.junit.jupiter.api.Test

import java.lang.reflect.InvocationHandler
import java.lang.reflect.Proxy
import java.util.concurrent.Callable
import java.util.concurrent.Executor
import java.util.concurrent.ForkJoinPool
import java.util.function.BiFunction
import java.util.function.Function
import java.util.function.IntBinaryOperator
import java.util.function.Supplier
import java.util.function.UnaryOperator

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

/**
 * Tests call syntax, {@code f(args)} and {@code f.call(args)}, in dynamic code
 * on a functional value: a lambda or method reference, or a Groovy coercion
 * proxy. Call syntax invokes the abstract method of its functional interface,
 * and only when the call would otherwise fail with a MissingMethodException.
 */
final class FunctionalValueCallSyntaxTest {

    private static final int HOT = 20_000 // enough calls for a call site to be linked

    //--------------------------------------------------------------------------
    // values that get call syntax

    @Test
    void testNativeLambda() {
        def f = FunctionalValueCallSyntaxValues.trimmer()
        assert f !instanceof Closure
        assert f.getClass().isHidden()
        assert f(' a ') == 'a'
        assert f.call(' b ') == 'b'
    }

    @Test
    void testMethodReference() {
        def f = FunctionalValueCallSyntaxValues.trimReference()
        assert f !instanceof Closure
        assert f.getClass().isHidden()
        assert f(' a ') == 'a'
        assert f.call(' b ') == 'b'
    }

    @Test
    void testLambdaOfTheJdk() {
        def f = Function.identity()
        assert f(1) == 1
        assert f.call('x') == 'x'

        f = UnaryOperator.identity()
        assert f(2) == 2

        // a serializable lambda, which also implements a marker interface
        def c = Comparator.comparing(FunctionalValueCallSyntaxValues.length())
        assert c instanceof Serializable
        assert c('a', 'bb') < 0
        assert c.call('bb', 'a') > 0
    }

    @Test
    void testClosureCoercionProxy() {
        def f = { it.trim() } as Function<String, String>
        assert f !instanceof Closure
        assert Proxy.isProxyClass(f.getClass())
        assert f(' a ') == 'a'
        assert f.call(' b ') == 'b'

        Function<String, String> g = { it.toUpperCase() }
        assert Proxy.isProxyClass(g.getClass())
        assert g('a') == 'A'
        assert g.call('b') == 'B'
    }

    @Test
    void testMapCoercionProxy() {
        def f = [apply: { it * 2 }] as Function
        assert Proxy.isProxyClass(f.getClass())
        assert f(2) == 4
        assert f.call(3) == 6
    }

    @Test
    void testNoArguments() {
        def s = FunctionalValueCallSyntaxValues.supplier()
        assert s() == 'supplied'
        assert s.call() == 'supplied'

        s = { -> 'coerced' } as Supplier
        assert s() == 'coerced'
        assert s.call() == 'coerced'
    }

    @Test
    void testTwoArguments() {
        def f = FunctionalValueCallSyntaxValues.adder()
        assert f(1, 2) == 3
        assert f.call(3, 4) == 7

        f = { a, b -> a * b } as BiFunction
        assert f(2, 3) == 6
        assert f.call(4, 5) == 20
    }

    @Test
    void testPrimitiveParametersAndReturn() {
        def f = FunctionalValueCallSyntaxValues.intAdder()
        assert f(1, 2) == 3
        assert f.call(3, 4) == 7
    }

    @Test
    void testNoResult() {
        def log = []
        def r = FunctionalValueCallSyntaxValues.runner(log)
        assert r() == null
        assert r.call() == null
        assert log == ['ran', 'ran']
    }

    @Test
    void testArgumentConversion() {
        def f = FunctionalValueCallSyntaxValues.namer()
        def x = 'a'
        assert f("$x$x") == 'name:aa'
        assert f.call("$x") == 'name:a'
        assert f(null) == 'name:null'
    }

    @Test
    void testSpreadArguments() {
        def f = FunctionalValueCallSyntaxValues.adder()
        def args = [1, 2]
        assert f(*args) == 3
        assert f.call(*args) == 3
    }

    @Test
    void testSafeCall() {
        def f = FunctionalValueCallSyntaxValues.trimmer()
        assert f?.call(' a ') == 'a'
        f = null
        assert f?.call(' a ') == null
    }

    // f() fits a variadic abstract method
    @Test
    void testVariadicMethod() {
        for (f in [FunctionalValueCallSyntaxValues.counter(), { Object[] args -> args.length } as FunctionalValueCallSyntaxCounter]) {
            assert f() == 0
            assert f.call() == 0
            assert f(1) == 1
            assert f(1, 2, 3) == 3
            assert f.call('a', 'b') == 2
        }
    }

    @Test
    void testExceptionOfTheMethod() {
        def f = FunctionalValueCallSyntaxValues.thrower()
        def e = shouldFail(IllegalStateException) { f(1) }
        assert e.message == 'boom'
        e = shouldFail(IllegalStateException) { InvokerHelper.invokeMethod(f, 'call', 1) }
        assert e.message == 'boom'
    }

    @Test
    void testMetaClassApi() {
        def f = FunctionalValueCallSyntaxValues.trimmer()
        assert InvokerHelper.invokeMethod(f, 'call', [' a '] as Object[]) == 'a'
        assert InvokerHelper.getMetaClass(f).invokeMethod(f, 'call', [' b '] as Object[]) == 'b'

        f = { it.trim() } as Function
        assert InvokerHelper.invokeMethod(f, 'call', [' a '] as Object[]) == 'a'
    }

    @Test
    void testOneCallSiteManyReceivers() {
        def receivers = [
                FunctionalValueCallSyntaxValues.trimmer(),
                FunctionalValueCallSyntaxValues.trimReference(),
                { it.trim() } as Function,
                { it.trim() },
                String.&trim
        ]
        HOT.times { i ->
            def f = receivers[i % receivers.size()]
            assert f(' a ') == 'a'
        }
    }

    //--------------------------------------------------------------------------
    // values that do not get call syntax

    @Test
    void testObjectsThatImplementAFunctionalInterfaceAreNotCallable() {
        Runnable anonymous = new Runnable() {
            @Override void run() { throw new AssertionError('run') }
        }
        Executor executor = ForkJoinPool.commonPool()
        def values = [
                [1, 2],                                     // Iterable
                new Thread(anonymous),                      // Runnable
                new ByteArrayInputStream(new byte[0]),      // AutoCloseable
                'abc',                                      // Comparable
                42,                                         // Comparable
                new Random(),                               // RandomGenerator
                executor,                                   // Executor
                anonymous,
                FunctionalValueCallSyntaxSign.PLUS,         // an enum constant
                new FunctionalValueCallSyntaxDoubler(),     // a class of the user
                Comparator.naturalOrder(),                  // an enum constant of the JDK
                Comparator.reverseOrder(),                  // a class of the JDK
                new StringWriter()                          // several interfaces
        ]
        for (value in values) {
            def e = shouldFail(MissingMethodException) { value() }
            assert e.method == 'call'
            e = shouldFail(MissingMethodException) { value.call() }
            assert e.method == 'call'
            e = shouldFail(MissingMethodException) { value(1) }
            assert e.method == 'call'
            e = shouldFail(MissingMethodException) { InvokerHelper.invokeMethod(value, 'call', 1) }
            assert e.method == 'call'
        }
    }

    @Test
    void testProxyWithAnotherHandlerIsNotCallable() {
        def p = Proxy.newProxyInstance(null, [Function] as Class[], { proxy, method, args -> 'handled' } as InvocationHandler)
        assert p.apply(1) == 'handled'
        shouldFail(MissingMethodException) { p(1) }
        shouldFail(MissingMethodException) { p.call(1) }
    }

    // the handler belongs to the proxy, not to its class
    @Test
    void testProxyHandlerIsCheckedForEachInstance() {
        def coerced = { 'coerced' } as Function
        def other = Proxy.newProxyInstance(null, [Function] as Class[], { proxy, method, args -> 'handled' } as InvocationHandler)
        assert coerced.getClass() == other.getClass()

        def call = { f -> f(1) }
        HOT.times { assert call(coerced) == 'coerced' }
        shouldFail(MissingMethodException) { call(other) }
        assert call(coerced) == 'coerced'
        shouldFail(MissingMethodException) { call(other) }
    }

    // a coercion to an abstract class or a trait is not a functional value
    @Test
    void testOtherCoercionsAreNotCallable() {
        def a = { 'x' } as FunctionalValueCallSyntaxAbstract
        assert a.get() == 'x'
        assert !Proxy.isProxyClass(a.getClass())
        shouldFail(MissingMethodException) { a() }

        def t = { 'x' } as FunctionalValueCallSyntaxTrait
        assert t.get() == 'x'
        assert !Proxy.isProxyClass(t.getClass())
        shouldFail(MissingMethodException) { t() }

        def m = [hasNext: { false }, next: { null }] as Iterator
        assert Proxy.isProxyClass(m.getClass())
        shouldFail(MissingMethodException) { m() }
    }

    //--------------------------------------------------------------------------
    // call syntax comes after everything else

    @Test
    void testCallMethodOfTheInterfaceWins() {
        def c = FunctionalValueCallSyntaxValues.callable()
        assert c() == 'called'
        assert c.call() == 'called'
        shouldFail(MissingMethodException) { c(1) }

        def g = FunctionalValueCallSyntaxValues.greeter()
        assert g.greet('you') == 'hi you'
        assert g('you') == 'default call you' // not greet
        assert g.call('you') == 'default call you'
    }

    @Test
    void testCategoryMethodWins() {
        def f = FunctionalValueCallSyntaxValues.trimmer()
        def call = { -> f(' a ') }
        HOT.times { assert call() == 'a' }
        use(FunctionalValueCallSyntaxCategory) {
            assert call() == 'category  a '
            assert f.call(' b ') == 'category  b '
        }
        assert call() == 'a'
    }

    @Test
    void testCallMethodAddedToTheInterfaceWins() {
        def f = FunctionalValueCallSyntaxValues.namer()
        def call = { -> f('a') }
        HOT.times { assert call() == 'name:a' }
        try {
            FunctionalValueCallSyntaxNamer.metaClass.call = { String s -> 'expando ' + s }
            assert call() == 'expando a'
            assert f.call('b') == 'expando b'
            assert InvokerHelper.invokeMethod(f, 'call', 'c') == 'expando c'
        } finally {
            GroovySystem.metaClassRegistry.removeMetaClass(FunctionalValueCallSyntaxNamer)
        }
        assert call() == 'name:a'
    }

    // the call must reach the outer class, not the run() or get() that makes it
    @Test
    void testInnerClassCallsCallMethodOfOuterClass() {
        assertScript '''
            import java.util.function.Supplier

            class Outer {
                def call() { 'outer' }
                def call(x) { "outer $x" }
                def viaSupplier() {
                    Supplier s = new Supplier() {
                        @Override get() { call() }
                    }
                    s.get()
                }
                def viaRunnable() {
                    def result = []
                    Runnable r = new Runnable() {
                        @Override void run() { result << call(1) }
                    }
                    r.run()
                    result
                }
            }

            assert new Outer().viaSupplier() == 'outer'
            assert new Outer().viaRunnable() == ['outer 1']
        '''
    }

    // a functional value held by a property is not called in place of methodMissing
    @Test
    void testMethodMissingOfTheOwnerOfAPropertyWins() {
        def owner = new FunctionalValueCallSyntaxOwner(f: FunctionalValueCallSyntaxValues.trimmer(), g: { it.trim() } as Function)
        assert owner.f(' a ') == 'missing f'
        assert owner.g(' a ') == 'missing g'
        assert owner.callBoth(' a ') == ['missing f', 'missing g']

        // the value of the property is callable
        assert owner.f.call(' a ') == 'a'
        assert owner.g.call(' a ') == 'a'
    }

    // call syntax is for a value, as in statically compiled code: a call through
    // a property, a map entry or a script variable fails as before
    @Test
    void testCallThroughPropertyIsUnchanged() {
        for (f in [FunctionalValueCallSyntaxValues.trimmer(), { it.trim() } as Function]) {
            def holder = new FunctionalValueCallSyntaxHolder(f: f)
            def e = shouldFail(MissingMethodException) { holder.f(' a ') }
            assert e.method == 'f'
            e = shouldFail(MissingMethodException) { holder.callIt(' a ') }
            assert e.method == 'f'

            def map = [f: f]
            e = shouldFail(MissingMethodException) { map.f(' a ') }
            assert e.method == 'f'

            FunctionalValueCallSyntaxHolder.s = f
            try {
                e = shouldFail(MissingMethodException) { FunctionalValueCallSyntaxHolder.s(' a ') }
                assert e.method == 'call' // as before: reports the value of the static property
            } finally {
                FunctionalValueCallSyntaxHolder.s = null
            }

            def shell = new GroovyShell(new Binding(f: f))
            e = shouldFail(MissingMethodException) { shell.evaluate('f(" a ")') }
            assert e.method == 'f'
            assert shell.evaluate('f.call(" a ")') == 'a'
            assert shell.evaluate('def methodMissing(String name, args) { "missing $name" }; f(" a ")') == 'missing f'
        }
    }

    // the owner of a closure is still asked after its delegate
    @Test
    void testOwnerOfClosureIsAskedAfterDelegate() {
        def holder = new FunctionalValueCallSyntaxHolder(f: FunctionalValueCallSyntaxValues.trimmer())
        assert new FunctionalValueCallSyntaxOwner().callOn(holder, Closure.DELEGATE_FIRST) == 'missing f'
        assert new FunctionalValueCallSyntaxOwner().callOn(holder, Closure.OWNER_FIRST) == 'missing f'
    }

    //--------------------------------------------------------------------------
    // the message of a MissingMethodException is unchanged

    @Test
    void testMessageOfMissingMethodException() {
        def calls = [
                { -> def c = { a -> a }; c(1, 2) },
                { -> def s = 'abc'; s(1) },
                { -> def i = 42; i() },
                { -> def e = FunctionalValueCallSyntaxSign.PLUS; e(1) },
                { -> def w = new StringWriter(); w() },
                { -> def o = new FunctionalValueCallSyntaxTwoInterfaces(); o() },
                { -> def f = FunctionalValueCallSyntaxValues.trimmer(); f(1, 2) },
                { -> def f = { it } as Function; f() }
        ]
        for (call in calls) {
            def e = shouldFail(MissingMethodException, call)
            assert e.method == 'call'
            assert e.message.startsWith('No signature of method: ')
            assert !e.message.contains('ambiguous')
            assert !e.message.contains('functional interface')
        }

        def s = 'abc'
        def e = shouldFail(MissingMethodException) { s(1) }
        assert e.message.startsWith('No signature of method: call for class: java.lang.String is applicable for argument types: (Integer) values: [1]\nPossible solutions: ')
    }

    @Test
    void testArgumentsThatDoNotFit() {
        def f = FunctionalValueCallSyntaxValues.trimmer()
        def e = shouldFail(MissingMethodException) { f() } // no argument does not stand for null
        assert e.method == 'call'
        assert e.type == f.getClass()
        assert e.arguments == [] as Object[]
        e = shouldFail(MissingMethodException) { f.call(1, 2) }
        assert e.arguments == [1, 2] as Object[]

        def g = { it } as Function
        e = shouldFail(MissingMethodException) { g() }
        assert e.method == 'call'
        shouldFail(MissingMethodException) { g(1, 2) }

        def s = FunctionalValueCallSyntaxValues.supplier()
        shouldFail(MissingMethodException) { s(1) }
    }

    //--------------------------------------------------------------------------
    // the abstract method must accept the arguments of the call

    @Test
    void testArgumentOfTheWrongType() {
        def f = FunctionalValueCallSyntaxValues.namer()
        assert f('a') == 'name:a'
        // not a ClassCastException from within the lambda
        def e = shouldFail(MissingMethodException) { f(42) }
        assert e.method == 'call'
        e = shouldFail(MissingMethodException) { f.call([1]) }
        assert e.method == 'call'
        e = shouldFail(MissingMethodException) { InvokerHelper.invokeMethod(f, 'call', 42) }
        assert e.method == 'call'
        assert f('b') == 'name:b'

        def g = FunctionalValueCallSyntaxValues.doubler()
        assert g(2) == 4
        shouldFail(MissingMethodException) { g('x') }
        shouldFail(MissingMethodException) { g(null) }
    }

    @Test
    void testOverloadOfTheAbstractMethodIsNotCalled() {
        for (f in [FunctionalValueCallSyntaxValues.doubler(), { int x -> x * 2 } as FunctionalValueCallSyntaxScaler]) {
            assert f(2) == 4
            assert f.scale(2) == 4
            assert f.scale(2, 3) == 12 // a default method with the name of the abstract one
            shouldFail(MissingMethodException) { f(2, 3) }
            shouldFail(MissingMethodException) { f.call(2, 3) }
        }
    }

    @Test
    void testCallSyntaxAndMethodNameShareNoCache() {
        def f = FunctionalValueCallSyntaxValues.doubler()
        def mc = InvokerHelper.getMetaClass(f)
        200.times { i ->
            assert f(i) == i * 2
            assert f.scale(i, 3) == i * 6
            assert f.scale(i) == i * 2
            assert mc.invokeMethod(f, 'call', [i] as Object[]) == i * 2
            assert mc.invokeMethod(f, 'scale', [i, 3] as Object[]) == i * 6
            assert mc.invokeMethod(f, 'scale', [i] as Object[]) == i * 2
            shouldFail(MissingMethodException) { mc.invokeMethod(f, 'call', [i, 3] as Object[]) }
        }
    }

    //--------------------------------------------------------------------------
    // a call site is bound to the abstract method

    @Test
    void testCallSiteDoesNotTakeMissingMethodPath() {
        def f = FunctionalValueCallSyntaxValues.tracer()
        def viaMetaClass = { StackTraceElement[] trace -> trace.any { it.className == MetaClassImpl.name } }

        assert !viaMetaClass(f(1))
        assert !viaMetaClass(f.call(1))
        StackTraceElement[] last = null
        HOT.times { last = f(1) }
        assert !viaMetaClass(last)

        // the meta-class invokes the same method when asked directly
        assert viaMetaClass(InvokerHelper.invokeMethod(f, 'call', 1))
    }

    @Test
    void testCallSiteOfCoercionProxyDoesNotTakeMissingMethodPath() {
        def f = { new Throwable().stackTrace } as Supplier
        def missing = { StackTraceElement[] trace -> trace.any { it.className == MetaClassImpl.name && it.methodName == 'invokeFunctionalCall' } }

        assert !missing(f())
        assert !missing(f.call())
        assert missing(InvokerHelper.invokeMethod(f, 'call', null))
    }

    //--------------------------------------------------------------------------
    // call syntax is all a functional value gets

    @Test
    void testFunctionalValueIsNotAClosure() {
        def f = FunctionalValueCallSyntaxValues.trimmer()
        shouldFail(MissingMethodException) { f.curry(' a ') }
        shouldFail(MissingMethodException) { f.doCall(' a ') }
        shouldFail(MissingPropertyException) { f.delegate }
        shouldFail(MissingMethodException) { f >> f }
        assert !f.isCase(' a ')
        assert [' a '].grep(f) == []
    }

    //--------------------------------------------------------------------------
    // the rule is no wider than the one of statically compiled code

    @Test
    void testStaticallyCompiledCallSyntax() {
        assertScript '''
            import java.util.function.Function
            import java.util.function.Supplier

            @groovy.transform.CompileStatic
            def test(Function<String, String> f, Supplier<String> s) {
                [f(' a '), f.call(' b '), s(), s.call()]
            }

            assert test(String::trim, () -> 'x') == ['a', 'b', 'x', 'x']
            assert test({ it.trim() }, { 'x' }) == ['a', 'b', 'x', 'x']
        '''
        // no argument does not stand for null, and an overload of the abstract method is not called
        for (call in ['f()', 'f.call()', 'f(1, 2)', 's(2, 3)', 's.call(2, 3)']) {
            shouldFail """
                @groovy.transform.CompileStatic
                def test(java.util.function.Function<Object, Object> f, groovy.lang.FunctionalValueCallSyntaxScaler s) {
                    $call
                }
            """
        }
        // a property is not called, unless it holds a closure
        shouldFail '''
            @groovy.transform.CompileStatic
            class C {
                java.util.function.Function<Object, Object> f
                def test() { f(1) }
            }
        '''
    }
}

//------------------------------------------------------------------------------

@CompileStatic
class FunctionalValueCallSyntaxValues {
    static Function<String, String> trimmer() {
        Function<String, String> f = (String s) -> s.trim()
        f
    }

    static Function<String, String> trimReference() {
        Function<String, String> f = String::trim
        f
    }

    static Function<String, Integer> length() {
        Function<String, Integer> f = String::length
        f
    }

    static Supplier<String> supplier() {
        Supplier<String> s = () -> 'supplied'
        s
    }

    static BiFunction<Integer, Integer, Integer> adder() {
        BiFunction<Integer, Integer, Integer> f = (Integer a, Integer b) -> a + b
        f
    }

    static IntBinaryOperator intAdder() {
        IntBinaryOperator f = (int a, int b) -> a + b
        f
    }

    static Runnable runner(List<String> log) {
        Runnable r = () -> { log.add('ran') }
        r
    }

    static Function<Object, Object> thrower() {
        Function<Object, Object> f = (Object o) -> { throw new IllegalStateException('boom') }
        f
    }

    static Function<Object, StackTraceElement[]> tracer() {
        Function<Object, StackTraceElement[]> f = (Object o) -> new Throwable().stackTrace
        f
    }

    static Callable<String> callable() {
        Callable<String> c = () -> 'called'
        c
    }

    static FunctionalValueCallSyntaxCounter counter() {
        FunctionalValueCallSyntaxCounter f = (Object[] args) -> args.length
        f
    }

    static FunctionalValueCallSyntaxNamer namer() {
        FunctionalValueCallSyntaxNamer f = (String s) -> 'name:' + s
        f
    }

    static FunctionalValueCallSyntaxScaler doubler() {
        FunctionalValueCallSyntaxScaler f = (int x) -> x * 2
        f
    }

    static FunctionalValueCallSyntaxGreeter greeter() {
        FunctionalValueCallSyntaxGreeter g = (String name) -> 'hi ' + name
        g
    }
}

interface FunctionalValueCallSyntaxCounter {
    int count(Object... args)
}

interface FunctionalValueCallSyntaxNamer {
    String name(String s)
}

interface FunctionalValueCallSyntaxScaler {
    int scale(int x)
    default int scale(int x, int y) { scale(x) * y }
}

interface FunctionalValueCallSyntaxGreeter {
    String greet(String name)
    default String call(String name) { 'default call ' + name }
}

abstract class FunctionalValueCallSyntaxAbstract {
    abstract Object get()
}

trait FunctionalValueCallSyntaxTrait {
    abstract Object get()
}

enum FunctionalValueCallSyntaxSign implements IntBinaryOperator {
    PLUS

    @Override
    int applyAsInt(int left, int right) { left + right }
}

class FunctionalValueCallSyntaxDoubler implements Function<Integer, Integer> {
    @Override
    Integer apply(Integer i) { i * 2 }
}

class FunctionalValueCallSyntaxTwoInterfaces implements Function<Object, Object>, Supplier<Object> {
    @Override
    Object apply(Object o) { o }

    @Override
    Object get() { null }
}

class FunctionalValueCallSyntaxCategory {
    static call(Function self, Object argument) { 'category ' + argument }
}

class FunctionalValueCallSyntaxHolder {
    static s
    def f

    def callIt(x) { f(x) }
}

class FunctionalValueCallSyntaxOwner {
    def f
    def g

    def methodMissing(String name, args) { 'missing ' + name }

    def callBoth(x) { [f(x), g(x)] }

    def callOn(delegate, int strategy) {
        def c = { -> f(' a ') }
        c.delegate = delegate
        c.resolveStrategy = strategy
        c()
    }
}
