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
package org.codehaus.groovy.classgen.asm.sc

import org.codehaus.groovy.classgen.asm.AbstractBytecodeTestCase
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.Phases
import org.codehaus.groovy.runtime.InvokerHelper
import org.junit.jupiter.api.Test

import java.lang.reflect.Proxy
import java.lang.reflect.UndeclaredThrowableException
import java.util.function.Function

import static groovy.test.GroovyAssert.shouldFail

/**
 * Tests for the incubating {@code groovy.target.closure.sam} option: a statically compiled closure literal
 * converted to a functional interface (passed to a SAM parameter, assigned to or returned as a SAM type,
 * cast or coerced with {@code as}) is wrapped in a class generated at compile time instead of a runtime
 * proxy, with the same behavior. The property is read per closure, so it is toggled per compilation here.
 */
final class StaticCompileClosureSamTest extends AbstractBytecodeTestCase {

    private static final String PROP = CompilerConfiguration.CLOSURE_SAM_CONVERSION

    private static <T> T withSam(boolean enabled, Closure<T> work) {
        String previous = System.getProperty(PROP)
        if (enabled) System.setProperty(PROP, 'true') else System.clearProperty(PROP)
        try {
            work.call()
        } finally {
            if (previous != null) System.setProperty(PROP, previous) else System.clearProperty(PROP)
        }
    }

    /** Compiles and runs the script with the option enabled, returning its value. */
    private static Object eval(String script, boolean enabled = true) {
        withSam(enabled) { new GroovyShell().evaluate(script) }
    }

    private static boolean isProxy(Object value) {
        Proxy.isProxyClass(value.getClass())
    }

    /** Compiles the source and returns the names of the adapter classes generated for it. */
    private static List<String> adapterClassNames(boolean enabled, String source) {
        withSam(enabled) {
            def unit = new CompilationUnit()
            unit.addSource('script', source)
            unit.compile(Phases.CLASS_GENERATION)
            unit.classes*.name.findAll { it.endsWith('$SamAdapter') }
        }
    }

    private static final String FORMS = '''
        import groovy.transform.CompileStatic
        import java.util.function.IntBinaryOperator
        import java.util.stream.IntStream

        @CompileStatic
        class C {
            static int argument(int n) { IntStream.range(0, n).reduce(0) { int a, int b -> a + b } }
            static IntBinaryOperator variable() { IntBinaryOperator op = { int a, int b -> a + b }; op }
            static IntBinaryOperator returned() { return { int a, int b -> a * b } }
            static IntBinaryOperator coerced() { { int a, int b -> a - b } as IntBinaryOperator }
            static IntBinaryOperator cast() { (IntBinaryOperator) { int a, int b -> a % b } }
            static IntBinaryOperator passed() { seen { int a, int b -> a + b } }
            static IntBinaryOperator seen(IntBinaryOperator op) { op }
        }
    '''

    @Test
    void conversionNeedsNoRuntimeCastOrCoercion() {
        withSam(true) {
            compile(classNamePattern: 'C', FORMS) // the sequence holds every method of C
            assert !sequence.hasSequence(['INVOKEDYNAMIC cast(']) : sequence
            assert !sequence.hasSequence(['INVOKEDYNAMIC invoke(']) : sequence // asType
            assert sequence.hasStrictSequence([
                'INVOKESPECIAL C$_argument_closure1.<init>',
                'NEW C$_argument_closure1$SamAdapter',
                'DUP_X1',
                'SWAP',
                'INVOKESPECIAL C$_argument_closure1$SamAdapter.<init> (LC$_argument_closure1;)V',
                'INVOKEINTERFACE java/util/stream/IntStream.reduce'
            ])
        }
        assert adapterClassNames(true, FORMS).size() == 6 // one for each closure
    }

    @Test
    void convertedValueIsNotAProxyButLooksLikeOne() {
        def c = eval(FORMS + '\nC')
        assert c.argument(5) == 10
        def ops = [c.variable(), c.returned(), c.coerced(), c.cast(), c.passed()]
        assert ops*.applyAsInt(7, 3) == [10, 21, 4, 1, 10]
        for (op in ops) {
            assert !isProxy(op)
            assert op instanceof Serializable
            assert !(op instanceof Closure)
            assert !(op instanceof GroovyObject)
            assert op.toString() ==~ 'C\\$_\\w+_closure\\d+@\\p{XDigit}+' // the closure's
        }
    }

    @Test
    void disabledByDefault() {
        withSam(false) {
            compile(classNamePattern: 'C', FORMS)
            assert sequence.hasSequence(['INVOKEDYNAMIC cast(Lgroovy/lang/Closure;)Ljava/util/function/IntBinaryOperator;'])
            assert sequence.hasSequence(['INVOKEDYNAMIC invoke(Lgroovy/lang/Closure;Ljava/lang/Class;)'])
        }
        assert adapterClassNames(false, FORMS).isEmpty()
        def c = eval(FORMS + '\nC', false)
        assert isProxy(c.variable())
        assert c.variable().applyAsInt(7, 3) == 10
    }

    @Test
    void genericInterfaceWithInferredParameterType() {
        def c = eval '''
            import java.util.function.Function
            @groovy.transform.CompileStatic
            class C {
                static Function<String, Integer> length() { Function<String, Integer> f = { s -> s.length() }; f }
            }
            C
        '''
        def f = c.length()
        assert !isProxy(f)
        assert f.apply('abcd') == 4
    }

    @Test
    void argumentsAreConvertedAsByTheProxy() {
        def c = eval '''
            import java.util.function.Function
            @groovy.transform.CompileStatic
            class C {
                static Function<String, Integer> length() { Function<String, Integer> f = { String s -> s?.length() }; f }
            }
            C
        '''
        def f = c.length()
        assert !isProxy(f)
        def x = 'ab'
        assert f.apply("${x}cd") == 4 // a GString is converted to the String parameter
        assert f.apply(null) == null
        shouldFail(MissingMethodException) { f.apply(42) } // an argument doCall cannot take
    }

    @Test
    void primitiveAndBoxedParametersAndResults() {
        def c = eval '''
            import java.util.function.*
            @groovy.transform.CompileStatic
            class C {
                static IntUnaryOperator boxedParameter() { IntUnaryOperator op = { Integer i -> i + 1 }; op }
                static Function<Integer, Integer> primitiveParameter() { Function<Integer, Integer> f = { int i -> i * 2 }; f }
                static ToLongFunction<String> longResult() { ToLongFunction<String> f = { String s -> (long) s.length() }; f }
                static DoubleBinaryOperator doubles() { DoubleBinaryOperator op = { double a, double b -> a / b }; op }
                static LongFunction<String> longParameter() { LongFunction<String> f = { long n -> 'n' + n }; f }
            }
            C
        '''
        assert c.boxedParameter().applyAsInt(41) == 42
        assert c.primitiveParameter().apply(21) == 42
        shouldFail(NullPointerException) { c.primitiveParameter().apply(null) }
        shouldFail(MissingMethodException) { c.primitiveParameter().apply(21L) }
        assert c.longResult().applyAsLong('abc') == 3L
        assert c.doubles().applyAsDouble(1d, 4d) == 0.25d
        assert c.longParameter().apply(Long.MAX_VALUE) == 'n' + Long.MAX_VALUE
        for (value in [c.boxedParameter(), c.primitiveParameter(), c.longResult(), c.doubles(), c.longParameter()]) {
            assert !isProxy(value)
        }
    }

    @Test
    void arrayParameters() {
        def c = eval '''
            import java.util.function.*
            @groovy.transform.CompileStatic
            class C {
                static ToIntFunction<String[]> length() { ToIntFunction<String[]> f = { String[] a -> a.length }; f }
                static Function<String[], String> joined() { Function<String[], String> f = { String... a -> a.join(',') }; f }
            }
            C
        '''
        assert c.length().applyAsInt(['a', 'b'] as String[]) == 2
        assert c.joined().apply(['a', 'b'] as String[]) == 'a,b'
        assert !isProxy(c.length())
        assert !isProxy(c.joined())
    }

    @Test
    void implicitParameterDefaultValuesAndDestructuring() {
        def c = eval '''
            import java.util.function.*
            @groovy.transform.CompileStatic
            class C {
                static Supplier<Integer> noParameter() { Supplier<Integer> s = { 42 }; s }
                static Function<Object, Object> implicitIt() { Function<Object, Object> f = { it }; f }
                static BiFunction<Integer, Integer, Integer> allArguments() { BiFunction<Integer, Integer, Integer> f = { Integer a, Integer b = 10 -> a + b }; f }
                static List<String> destructured() { List<String> out = []; [['a', 'b'], ['c', 'd']].forEach { a, b -> out << a + b }; out }
                static Consumer<List<String>> seen(Consumer<List<String>> c) { c }
                static Consumer<List<String>> destructuredArgument(List<String> out) { seen { a, b -> out << a + b } }
                static Consumer<List<String>> destructuredVariable(List<String> out) { Consumer<List<String>> c = { a, b -> out << a + b }; c }
            }
            C
        '''
        assert c.noParameter().get() == 42
        assert c.implicitIt().apply('x') == 'x'
        assert c.allArguments().apply(1, 2) == 3
        assert c.destructured() == ['ab', 'cd']
        for (value in [c.noParameter(), c.implicitIt(), c.allArguments()]) {
            assert !isProxy(value)
        }
        def out = []
        c.destructuredArgument(out).accept(['a', 'b'])
        c.destructuredVariable(out).accept(['c', 'd'])
        assert out == ['ab', 'cd']
        assert isProxy(c.destructuredArgument([])) // a closure that destructures its argument converts at run time
        assert isProxy(c.destructuredVariable([]))
    }

    @Test
    void resultIsConvertedToTheMethodReturnType() {
        def c = eval '''
            import java.util.function.*
            @groovy.transform.CompileStatic
            class C {
                static Predicate<String> truth() { Predicate<String> p = { String s -> s }; p }
                static Consumer<List<String>> voidResult() { Consumer<List<String>> c = { List<String> l -> l << 'x' }; c }
                static Comparator<String> comparator() { Comparator<String> c = { String a, String b -> b <=> a }; c }
            }
            C
        '''
        assert c.truth().test('a')
        assert !c.truth().test('')
        def list = []
        c.voidResult().accept(list)
        assert list == ['x']
        assert ['a', 'c', 'b'].sort(false, c.comparator()) == ['c', 'b', 'a']
        assert !isProxy(c.truth())
    }

    @Test
    void defaultMethodsAndUserInterfaces() {
        def c = eval '''
            import java.util.function.*
            interface Shout extends Function<String, String> { }
            interface Greeter {
                String greet(String name)
                default String greetAll(List<String> names) { names.collect { greet(it) }.join(' ') }
            }
            @groovy.transform.CompileStatic
            class C {
                static Shout shout() { Shout s = { String x -> x.toUpperCase() }; s }
                static Greeter greeter() { Greeter g = { String n -> "hi $n".toString() }; g }
                static String composed() {
                    Function<String, String> f = { String x -> x + '!' }
                    f.andThen({ String x -> x * 2 } as Function<String, String>).apply('a')
                }
                static Object raw(Function f, Object argument) { f.apply(argument) } // as from Java, through the erased method
            }
            C
        '''
        assert c.shout().apply('a') == 'A'
        assert ((Function) c.shout()).apply('b') == 'B'
        def x = 'a'
        assert c.raw(c.shout(), "${x}b") == 'AB' // a GString is converted for apply(String), as by the proxy
        shouldFail(MissingMethodException) { c.raw(c.shout(), 42) }
        assert c.greeter().greetAll(['a', 'b']) == 'hi a hi b'
        assert c.composed() == 'a!a!'
        assert !isProxy(c.shout())
        assert !isProxy(c.greeter())
    }

    @Test
    void closureSemanticsArePreserved() {
        def c = eval '''
            import java.util.function.*
            @groovy.transform.CompileStatic
            class C {
                String name = 'c'
                Supplier<Object> owner() { Supplier<Object> s = { owner }; s }
                Supplier<Object> delegate() { Supplier<Object> s = { delegate }; s }
                Supplier<Object> thisObject() { Supplier<Object> s = { thisObject }; s }
                Supplier<String> property() { Supplier<String> s = { name }; s }
                IntUnaryOperator recursive() { IntUnaryOperator f = null; f = { int n -> n <= 1 ? 1 : n * f.applyAsInt(n - 1) }; f }
                Function<Integer, Supplier<Integer>> nested() { Function<Integer, Supplier<Integer>> f = { Integer x -> Supplier<Integer> s = { -> x * 2 }; s }; f }
                Supplier<Object> innerOwner() { Supplier<Object> s = { Supplier<Object> inner = { owner }; inner.get() }; s }
                Supplier<Integer> counter() { int count = 0; Supplier<Integer> s = { ++count }; s }
            }
            new C()
        '''
        assert c.owner().get().is(c)
        assert c.delegate().get().is(c)
        assert c.thisObject().get().is(c)
        assert c.property().get() == 'c'
        assert c.recursive().applyAsInt(5) == 120
        def inner = c.nested().apply(21)
        assert inner.get() == 42
        assert !isProxy(inner)
        def outer = c.innerOwner()
        assert outer.get() instanceof Closure // the owner of a nested closure is the enclosing closure,
        assert !outer.get().is(outer)        // which stays wrapped as it was in the proxy
        def counter = c.counter()
        assert [counter.get(), counter.get(), counter.get()] == [1, 2, 3]
    }

    @Test
    void otherConversionContexts() {
        def c = eval '''
            import java.util.function.*
            @groovy.transform.CompileStatic
            class C {
                static final Comparator<String> BY_LENGTH = { String a, String b -> a.length() <=> b.length() }
                Function<String, String> field
                C() { field = { String s -> s.reverse() } }
                static Function<String, String> pick(boolean upper) {
                    upper ? { String s -> s.toUpperCase() } : { String s -> s.toLowerCase() }
                }
            }
            C
        '''
        assert ['ccc', 'a', 'bb'].sort(false, c.BY_LENGTH) == ['a', 'bb', 'ccc']
        assert c.newInstance().field.apply('ab') == 'ba'
        assert c.pick(true).apply('Ab') == 'AB'
        assert c.pick(false).apply('Ab') == 'ab'
        assert !isProxy(c.BY_LENGTH)
        assert !isProxy(c.newInstance().field)
        assert isProxy(c.pick(true)) // a conditional expression still converts at run time
        assert isProxy(c.pick(false))
    }

    @Test
    void serializable() {
        def c = eval '''
            import java.util.function.Function
            interface SerializableFunction extends Function<String, String>, Serializable { }
            @groovy.transform.CompileStatic
            class C {
                static SerializableFunction make() { SerializableFunction f = { String s -> s * 2 }; f }
                static Function<String, String> plain() { Function<String, String> f = { String s -> s + '!' }; f }
            }
            C
        '''
        for (f in [c.make(), c.plain()]) {
            assert !isProxy(f)
            def bytes = new ByteArrayOutputStream()
            new ObjectOutputStream(bytes).withCloseable { it.writeObject(f) }
            def copy = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray())) {
                @Override
                protected Class<?> resolveClass(ObjectStreamClass desc) {
                    try {
                        c.classLoader.loadClass(desc.name)
                    } catch (ClassNotFoundException ignore) { // arrays and primitives
                        super.resolveClass(desc)
                    }
                }
            }.readObject()
            assert copy.apply('ab') == f.apply('ab')
        }
    }

    @Test
    void exceptionsAreTranslatedAsByTheProxy() {
        def c = eval '''
            import java.util.function.Supplier
            interface IOSupplier { String get() throws IOException }
            @groovy.transform.CompileStatic
            class C {
                static Supplier<String> unchecked() { Supplier<String> s = { throw new IllegalStateException('boom') }; s }
                static Supplier<String> checked() { Supplier<String> s = { throw new IOException('boom') }; s }
                static IOSupplier declared() { IOSupplier s = { throw new IOException('boom') }; s }
                static Supplier<String> wrapped() { Supplier<String> s = { throw new GroovyRuntimeException('outer', new IllegalStateException('inner')) }; s }
            }
            C
        '''
        assert shouldFail(IllegalStateException) { c.unchecked().get() }.message == 'boom'
        assert shouldFail(UndeclaredThrowableException) { c.checked().get() }.cause instanceof IOException
        assert shouldFail(IOException) { c.declared().get() }.message == 'boom'
        assert shouldFail(IllegalStateException) { c.wrapped().get() }.message == 'inner'
        for (value in [c.unchecked(), c.checked(), c.declared(), c.wrapped()]) {
            assert !isProxy(value)
        }
    }

    @Test
    void dynamicCallersSeeOnlyTheInterface() {
        def c = eval '''
            import java.util.function.*
            @groovy.transform.CompileStatic
            class C {
                static Function<String, Integer> length() { Function<String, Integer> f = { String s -> s.length() }; f }
                static Predicate<String> truth() { Predicate<String> p = { String s -> s }; p }
            }
            C
        '''
        def f = c.length()
        assert !isProxy(f) && !isProxy(c.truth())
        assert [f]*.apply('abc') == [3]
        assert InvokerHelper.invokeMethod(f, 'apply', 'abc') == 3
        assert f.&apply.call('abc') == 3
        assert f.with { apply('abc') } == 3
        assert f.andThen { it + 1 }.apply('abc') == 4 // Function#andThen, not Closure#andThen
        assert ['', 'a'].grep(c.truth()) == [] // not called as a closure would be
        shouldFail(MissingMethodException) { f('abc') }
        shouldFail(ClassCastException) { (Closure) f }
    }

    @Test
    void methodNamesOfClosureAreNoObstacle() {
        def c = eval '''
            interface Caller { Object call(Object a, Object b) }
            interface Runner { void run() }
            interface Composer { Object andThen(Closure c) }
            @groovy.transform.CompileStatic
            class C {
                static Caller caller() { Caller c = { a, b -> "$a$b".toString() }; c }
                static Runner runner(List<String> out) { Runner r = { -> out << 'ran' }; r }
                static Composer composer() { Composer c = { Closure x -> x() }; c }
            }
            C
        '''
        assert c.caller().call('a', 'b') == 'ab'
        def out = []
        c.runner(out).run()
        assert out == ['ran']
        assert c.composer().andThen { 42 } == 42
        for (value in [c.caller(), c.runner([]), c.composer()]) {
            assert !isProxy(value)
        }
    }

    @Test
    void typesWithoutAnAdapterKeepTheirConversion() {
        def c = eval '''
            import java.util.concurrent.Callable
            import java.util.function.Function
            abstract class Task { abstract String run(String s) }
            trait Named { abstract String name(String s) }
            interface Overloaded extends Function<String, String> { default String apply(Integer i) { 'int' } }
            @groovy.transform.CompileStatic
            class C {
                static Runnable runnable(List<String> out) { Runnable r = { out << 'ran' }; r }
                static Callable<String> callable() { Callable<String> c = { 'called' }; c }
                static Task task() { Task t = { String s -> s * 2 }; t }
                static Named named() { Named n = { String s -> s + '!' }; n }
                static Overloaded overloaded() { Overloaded o = { String s -> s + '?' }; o }
            }
            C
        '''
        def out = []
        c.runnable(out).run()
        assert out == ['ran']
        assert c.callable().call() == 'called'
        assert c.task().run('ab') == 'abab'
        assert c.named().name('x') == 'x!'
        assert c.overloaded().apply('x') == 'x?'
        assert c.overloaded().apply(1) == 'int'
        assert c.runnable([]) instanceof Closure // a closure already is a Runnable and a Callable
        assert c.callable() instanceof Closure
        assert !(c.task() instanceof Closure)
        assert !(c.named() instanceof Closure)
        assert isProxy(c.overloaded()) // a default method of that name could be overridden
    }

    // GROOVY-12492
    @Test
    void sealedInterfacesKeepTheirConversion() {
        def c = eval '''
            import groovy.transform.CompileStatic
            import java.util.function.IntUnaryOperator
            import org.codehaus.groovy.classgen.asm.sc.ClosureSamShape
            @CompileStatic
            sealed interface Shape permits Square { double area() }
            @CompileStatic
            final class Square implements Shape { double area() { 4d } }
            @CompileStatic
            class C {
                static Shape groovyShape() { Shape s = { -> 1d }; s }
                static ClosureSamShape javaShape() { ClosureSamShape s = { -> 2d }; s }
                static IntUnaryOperator other() { IntUnaryOperator op = { int i -> i + 1 }; op }
            }
            C
        '''
        // the adapter would not be a permitted subclass, so the program compiles as without the option,
        // and the conversion fails at run time as it does there: a proxy cannot implement a sealed interface
        assert shouldFail(IllegalArgumentException) { c.groovyShape() }.message == 'Shape is a sealed interface'
        assert shouldFail(IllegalArgumentException) { c.javaShape() }.message == ClosureSamShape.name + ' is a sealed interface'
        assert c.other().applyAsInt(1) == 2
        assert !isProxy(c.other())
    }

    // GROOVY-12492
    @Test
    void nonPublicInterfaceFromAnotherClassLoaderKeepsItsConversion() {
        for (enabled in [false, true]) {
            withSam(enabled) {
                def parent = new GroovyClassLoader()
                parent.parseClass '''
                    package p
                    @groovy.transform.PackageScope
                    interface Callback { String f(String s) }
                '''
                // a class loader other than the interface's defines C, so C is in another run-time package
                def c = new GroovyClassLoader(parent).parseClass '''
                    package p
                    @groovy.transform.CompileStatic
                    class C {
                        static String other() { 'other' }
                        static Object make() { Callback f = { String s -> s + '!' }; f }
                    }
                '''
                // with the option too, C is defined, where an adapter implementing Callback could not be,
                // and only the conversion fails
                assert c.other() == 'other'
                shouldFail(IllegalAccessError) { c.make() }
            }
        }
    }

    // GROOVY-12492
    @Test
    void defaultValuesAndConstructorArgumentsKeepTheirConversion() {
        def (c, e) = eval '''
            import java.util.function.Function
            @groovy.transform.CompileStatic
            class Holder {
                Function<String, String> f
                Holder() { }
                Holder(Function<String, String> f) { this.f = f }
            }
            @groovy.transform.CompileStatic
            enum E {
                PLAIN({ String s -> s + '-plain' }), COERCED({ String s -> s + '-coerced' } as Function<String, String>)
                final Function<String, String> f
                E(Function<String, String> f) { this.f = f }
            }
            @groovy.transform.CompileStatic
            class C {
                static Function<String, String> defaultValue(Function<String, String> f = { String s -> s + '-default' }) { f }
                static Function<String, String> anonymous() { new Holder({ String s -> s + '-anonymous' }) { }.f }
                static Function<String, String> named() { new Holder(f: { String s -> s + '-named' }).f }
            }
            [C, E]
        '''
        assert c.defaultValue().apply('a') == 'a-default'
        assert e.PLAIN.f.apply('a') == 'a-plain'
        assert e.COERCED.f.apply('a') == 'a-coerced'
        assert c.anonymous().apply('a') == 'a-anonymous'
        assert c.named().apply('a') == 'a-named'
        assert isProxy(c.defaultValue()) // a default parameter value
        assert isProxy(e.PLAIN.f) // an argument of an enum constant
        assert !isProxy(e.COERCED.f) // unless written with as
        assert isProxy(c.anonymous()) // an argument of the constructor of an anonymous class
        assert isProxy(c.named()) // a named argument of a constructor
    }

    // GROOVY-12492
    @Test
    void protectedInterfaceFromAnotherPackageKeepsItsConversion() {
        def c = eval '''
            import org.codehaus.groovy.classgen.asm.sc.ClosureSamCallbacks
            @groovy.transform.CompileStatic
            class C extends ClosureSamCallbacks {
                Object make() { ClosureSamCallbacks.Callback cb = { String s -> s + '!' }; cb }
                String result() { ClosureSamCallbacks.Callback cb = { String s -> s + '?' }; cb.f('a') }
            }
            C
        '''
        def instance = c.newInstance()
        assert instance.result() == 'a?'
        assert isProxy(instance.make())
    }

    // GROOVY-12492
    @Test
    void javaInterfaceRedeclaringItsMethodKeepsItsConversion() {
        def c = eval '''
            import java.util.function.Function
            import org.codehaus.groovy.classgen.asm.sc.ClosureSamStringFunction
            @groovy.transform.CompileStatic
            class C {
                static ClosureSamStringFunction shout() { ClosureSamStringFunction f = { String s -> s.toUpperCase() }; f }
                static Object raw(Function f, Object argument) { f.apply(argument) }
            }
            C
        '''
        def f = c.shout()
        assert f.apply('a') == 'A'
        assert c.raw(f, 'b') == 'B' // through the bridge javac added as a default method, which an adapter would override
        assert isProxy(f)
    }

    // GROOVY-12492
    @Test
    void interfacesWithoutASingleAbstractMethodKeepTheirConversion() {
        def c = eval '''
            interface Mapper<T> { T map(T t) }
            interface StringMapper { String map(String s) }
            interface Both extends Mapper<String>, StringMapper { }
            interface Getter { Object get() }
            interface StringGetter extends Getter { String get() }
            @groovy.transform.CompileStatic
            class C {
                static Both both() { { String s -> s.toUpperCase() } as Both }
                static StringGetter covariant() { { -> 'got' } as StringGetter }
            }
            C
        '''
        assert c.both().map('a') == 'A'
        assert c.covariant().get() == 'got'
        assert isProxy(c.both())
        assert isProxy(c.covariant())
    }

    @Test
    void closuresNotConvertedToAnInterfaceAreUnchanged() {
        def c = eval '''
            @groovy.transform.CompileStatic
            class C {
                static Closure<Integer> plain() { def c = { int i -> i + 1 }; c }
                static List<Integer> dgm() { [1, 2].collect { int i -> i * 2 } }
                static Closure<String> parameter(Closure<String> c) { c }
                static Closure<String> passed() { parameter { 'x' } }
            }
            C
        '''
        assert c.plain()(1) == 2
        assert c.dgm() == [2, 4]
        assert c.passed()() == 'x'
        assert c.plain().getClass().interfaces*.name == ['org.codehaus.groovy.runtime.GeneratedClosure']
        assert c.passed().getClass().interfaces*.name == ['org.codehaus.groovy.runtime.GeneratedClosure']
    }

    @Test
    void withClosurePacking() {
        String previous = System.getProperty(CompilerConfiguration.CLOSURE_PACKING)
        System.setProperty(CompilerConfiguration.CLOSURE_PACKING, 'true')
        try {
            def c = eval(FORMS + '\nC')
            assert c.argument(5) == 10
            def ops = [c.variable(), c.returned(), c.coerced(), c.cast(), c.passed()]
            assert ops*.applyAsInt(7, 3) == [10, 21, 4, 1, 10]
            // a packed closure has no class of its own to adapt, so it converts at run time; the literal returned
            // as it is escapes its method, which keeps it from being packed, so it gets its adapter
            assert ops.collect { isProxy(it) } == [true, false, true, true, true]
        } finally {
            if (previous != null) System.setProperty(CompilerConfiguration.CLOSURE_PACKING, previous)
            else System.clearProperty(CompilerConfiguration.CLOSURE_PACKING)
        }
    }
}
