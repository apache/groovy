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

import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.Phases
import org.codehaus.groovy.control.messages.WarningMessage
import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

/**
 * A closure, lambda or method reference written for a slot declared with a type parameter, such as {@code E} of
 * {@code List.add(E)} or {@code V} of {@code putAt(Map<K, V>, K, V)}, where the call or assignment binds the type
 * parameter to a functional interface: statically compiled code compiles a method reference argument for that
 * interface, and the class of a closure literal or lambda implements it. Dynamic and type-checked code pass them
 * as they did.
 *
 * @see FunctionalLiteralSiteTest
 */
final class FunctionalTypeArgumentTest {

    private final GroovyShell shell = GroovyShell.withConfig {
        imports {
            star 'groovy.transform'
            star 'java.util.function'
        }
    }

    @Test
    void testLambdaImplementsAndMethodReferenceIsCompiledForTheInterfaceTheCallBindsItTo() {
        assertScript shell, '''
            import java.util.concurrent.atomic.AtomicReference
            interface Shout extends Function<String, String> { }
            @CompileStatic
            class C {
                static <T extends Function<String, String>> T second(T a, T b) { b }
                static <T> void addAll(List<T> list, T... items) { list.addAll(items) }
                static <T> T id(T t) { t }
                static List<Supplier<String>> suppliers() {
                    List<Supplier<String>> list = []
                    list.add(() -> 'add')
                    list.add(0, () -> 'insert')
                    list.set(0, () -> 'set')
                    list << (() -> 'shift')
                    addAll(list, () -> 'vararg', () -> 'varargs')
                    list.add(new AtomicReference<Supplier<String>>(() -> 'constructor').get())
                    list.add(C.<Supplier<String>>id(() -> 'type argument'))
                    list
                }
                static Map<String, Function<String, String>> functions() {
                    Map<String, Function<String, String>> map = [:]
                    map.put('lambda', (String s) -> s + '?')
                    map.putIfAbsent('implicit', s -> s.toUpperCase())
                    map.put('reference', String::toUpperCase)
                    map
                }
                static Shout shout() {
                    Shout shout = (String s) -> s + '!'
                    second(shout, (String s) -> s + '?') // T is Shout
                }
            }

            def suppliers = C.suppliers()
            assert suppliers*.get() == ['set', 'add', 'shift', 'vararg', 'varargs', 'constructor', 'type argument']
            // a lambda is a closure, as before, whose class implements the interface
            assert suppliers.every { it instanceof Supplier && it instanceof Closure }
            def functions = C.functions()
            assert functions.collectEntries { k, f -> [k, f.apply('ab')] } == [lambda: 'ab?', implicit: 'AB', reference: 'AB']
            assert functions.lambda instanceof Closure && functions.implicit instanceof Closure
            assert !(functions.reference instanceof Closure) // a method reference argument is compiled for the interface
            def shout = C.shout()
            assert shout.apply('a') == 'a?'
            assert shout instanceof Shout && shout instanceof Closure
        '''
    }

    @Test
    void testLambdaStaysAClosureAndMethodReferenceOperandAMethodClosure() {
        assertScript shell, '''
            @CompileStatic
            class C {
                static List<Supplier<String>> suppliers() {
                    List<Supplier<String>> list = []
                    list.add(() -> 'add')
                    list << (() -> 'shift')
                    list
                }
                static Map<String, Function<String, String>> functions() {
                    Map<String, Function<String, String>> map = [:]
                    map.put('shout', (String s) -> s.toUpperCase())
                    map.put('reference', String::toUpperCase)
                    map
                }
                static List<Function<String, String>> references(boolean flag) {
                    List<Function<String, String>> list = []
                    list.add(String::toUpperCase)
                    list << String::trim
                    list.add(flag ? String::trim : String::strip)
                    list
                }
                static String read(List<Function<String, String>> list, int i) {
                    list[i].apply(' ab ')
                }
            }

            // a lambda is compiled as a closure, as before, and dynamic code calls it as one
            def suppliers = C.suppliers()
            assert suppliers.every { it instanceof Closure }
            assert suppliers.collect { it() } == ['add', 'shift']
            def functions = C.functions()
            assert functions.shout instanceof Closure
            assert functions.shout('hi') == 'HI'
            assert functions.shout.call('hi') == 'HI'

            // a method reference argument is compiled for the interface
            assert !(functions.reference instanceof Closure)
            assert functions.reference.apply('hi') == 'HI'
            def references = C.references(true)
            assert references[0] instanceof Function && !(references[0] instanceof Closure)
            assert C.read(references, 0) == ' AB '

            // a method reference operand or branch of a conditional argument is a method closure, as before
            assert references[1] instanceof org.codehaus.groovy.runtime.MethodClosure
            assert references[2] instanceof org.codehaus.groovy.runtime.MethodClosure
            assert references[1](' ab ') == 'ab' && references[2](' ab ') == 'ab'
            groovy.test.GroovyAssert.shouldFail(ClassCastException) { C.read(references, 1) }
        '''
    }

    @Test
    void testDynamicCodeReadingALambdaStoredByStaticallyCompiledCode() {
        assertScript shell, '''
            @CompileStatic
            List<Supplier<String>> make() {
                List<Supplier<String>> list = []
                list.add(() -> 'x')
                list
            }

            @CompileStatic
            Map<String, Function<String, String>> commands() {
                Map<String, Function<String, String>> commands = [:]
                commands.put('shout', (String s) -> s.toUpperCase())
                commands
            }

            def list = make()
            assert list[0].get() == 'x'
            // a lambda is an instance of the interface and still a closure
            assert list[0]() == 'x'
            assert list[0].call() == 'x'
            def commands = commands()
            assert commands.shout.apply('hi') == 'HI'
            assert commands.shout('hi') == 'HI'
            assert commands.shout.call('hi') == 'HI'
            assert commands.shout.curry('hi')() == 'HI'
        '''
    }

    @Test
    void testTypeCheckedCodePassesALambdaOrMethodReferenceAsBefore() {
        def err = shouldFail shell, '''
            @TypeChecked
            void test() {
                List<Function<String, String>> list = []
                list.add(String::toUpperCase)
            }
        '''
        assert err.message.contains("Argument is a method reference, but parameter type 'E' is not a functional interface")

        assertScript shell, '''
            @TypeChecked
            List<Supplier<String>> make() {
                List<Supplier<String>> list = []
                list.add(() -> 'x') // compiled as a closure
                list
            }

            def list = make()
            assert list[0] instanceof Closure && !(list[0] instanceof Supplier)
            groovy.test.GroovyAssert.shouldFail(MissingMethodException) { list[0].get() }
        '''
    }

    @Test
    void testClosureLiteralImplementsTheInterfaceTheCallBindsItTo() {
        assertScript shell, '''
            import java.util.concurrent.atomic.AtomicReference
            interface Shout extends Function<String, String> { }
            @CompileStatic
            class C {
                static <T extends Function<String, String>> T second(T a, T b) { b }
                static <T> void addAll(List<T> list, T... items) { list.addAll(items) }
                static <T> T id(T t) { t }
                static List<Supplier<String>> suppliers(boolean flag) {
                    List<Supplier<String>> list = []
                    list.add { 'add' }
                    list.add(0) { 'insert' }
                    list.set(0) { 'set' }
                    list << { 'shift' }
                    addAll(list, { 'vararg' }, { 'varargs' })
                    list.add(flag ? { 'then' } : { 'else' })
                    list.add(new AtomicReference<Supplier<String>>({ 'constructor' }).get())
                    list.add(C.<Supplier<String>>id { 'type argument' })
                    list
                }
                static Map<String, Function<String, String>> functions() {
                    Map<String, Function<String, String>> map = [:]
                    map.put('put') { String s -> s + '!' }
                    map.putIfAbsent('implicit') { it.toUpperCase() }
                    map
                }
                static Shout shout() {
                    Shout shout = { String s -> s + '!' }
                    second(shout, { String s -> s + '?' }) // T is Shout
                }
            }

            def suppliers = C.suppliers(true)
            assert suppliers*.get() == ['set', 'add', 'shift', 'vararg', 'varargs', 'then', 'constructor', 'type argument']
            assert suppliers.every { it instanceof Supplier && it instanceof Closure }
            assert C.suppliers(false)[5].get() == 'else'
            def functions = C.functions()
            assert functions.collectEntries { k, f -> [k, f.apply('ab')] } == [put: 'ab!', implicit: 'AB']
            def shout = C.shout()
            assert shout.apply('a') == 'a?'
            assert shout instanceof Shout && shout instanceof Closure
        '''
    }

    // GROOVY-12561
    @Test
    void testClosureLiteralImplementsAnInterfaceWithWildcardTypeArguments() {
        assertScript shell, '''
            @CompileStatic
            class C {
                static List<Object> make() {
                    List<Function<? super String, Integer>> lengths = []
                    lengths.add { String s -> s.length() }
                    List<Supplier<? extends CharSequence>> suppliers = []
                    suppliers.add { 'supplied' }
                    List<Function<?, String>> anything = []
                    anything.add { Object o -> o.toString() + '!' }
                    List<Consumer<? super String>> consumers = []
                    List<String> consumed = []
                    consumers.add { String s -> consumed << s }
                    consumers[0].accept('consumed')
                    Function<? super String, Integer> length = lengths[0]
                    [length.apply('abc'), suppliers[0].get(), anything[0], consumed, lengths[0], consumers[0]]
                }
            }

            def values = C.make()
            assert values[0] == 3 && values[1] == 'supplied' && values[3] == ['consumed']
            // the class implements the interface with the type argument the wildcard stands for, and its erasure
            assert values[2].apply(1) == '1!' && values[4].apply('ab') == 2
            assert values[2] instanceof Function && values[4] instanceof Function && values[5] instanceof Consumer
            assert values[4].call('abcd') == 4 // still a closure
        '''
    }

    // GROOVY-12561
    @Test
    void testClosureLiteralIsPassedAsBeforeWhereItsParametersAreNotTheInterfaceMethods() {
        assertScript shell, '''
            @CompileStatic
            class C {
                static List<Object> make() {
                    List<Function<String, Object>> list = []
                    list.add { String... parts -> parts.length }        // variadic
                    list.add { String s, String suffix = '!' -> s + suffix } // a default value
                    list.add { String a, String b -> a + b }             // more parameters
                    list.add { -> 'none' }                               // fewer parameters
                    list << { String... parts -> parts.length }
                    List<BiFunction<String, String, String>> pairs = []
                    pairs.add { it }                                     // one parameter for two
                    List<Object> values = []
                    values.addAll(list)
                    values.addAll(pairs)
                    values
                }
            }

            def values = C.make()
            assert values.every { it instanceof Closure && !(it instanceof Function) && !(it instanceof BiFunction) }
            assert values[0]('abc') == 1 && values[1]('a') == 'a!' && values[2]('a', 'b') == 'ab' && values[3]() == 'none'
        '''
    }

    // GROOVY-12561, GROOVY-12824
    @Test
    void testClosureLiteralGetsParameterTypesFromTheInterfaceAsAnOperandConditionalOrNamedArgument() {
        assertScript shell, '''
            class Holder<T> { T value }
            @CompileStatic
            class C {
                static List<Function<String, String>> make(boolean flag) {
                    List<Function<String, String>> list = []
                    list << { it.toUpperCase() }                          // operand of leftShift(List<T>, T)
                    list.add(flag ? { it.toUpperCase() } : { it.trim() }) // branches of a conditional argument
                    list.add(flag ? (s -> s * 2) : (s -> s))              // lambdas
                    list << new Holder<Function<String, String>>(value: { it.toUpperCase() }).value // named argument
                    list
                }
            }

            def list = C.make(true)
            assert list*.apply(' a ') == [' A ', ' A ', ' a  a ', ' A ']
            assert list.every { it instanceof Closure && it instanceof Function }
            assert C.make(false)[1].apply(' a ') == 'a'
        '''

        def err = shouldFail shell, '''
            @TypeChecked
            void test() {
                List<Function<String, String>> list = []
                list << { it.toUpperCase() } // passed as it is
            }
        '''
        assert err.message.contains('Cannot find matching method java.lang.Object#toUpperCase()')
    }

    // GROOVY-12561
    @Test
    void testClosureLiteralAssignedToASubscriptOrPropertyImplementsItsInterface() {
        assertScript shell, '''
            class Holder<T> { T value }
            class Box<T> { public T field }
            @CompileStatic
            class C {
                static List<Object> make() {
                    Map<String, Function<String, String>> map = [:]
                    map['subscript'] = { String s -> s + '!' }
                    map?['safe'] = { it.toUpperCase() }
                    map.property = { String s -> s + '?' }
                    List<Supplier<String>> list = [null]
                    list[0] = { 'list' }
                    def holder = new Holder<Supplier<String>>()
                    holder.value = { 'holder' }
                    def box = new Box<Supplier<String>>()
                    box.@field = { 'box' }
                    def named = new Holder<Supplier<String>>(value: { 'named' })
                    [map, list[0], holder.value, box.field, named.value]
                }
            }

            def (map, list, holder, box, named) = C.make()
            assert map.collectEntries { k, f -> [k, f.apply('ab')] } == [subscript: 'ab!', safe: 'AB', property: 'ab?']
            assert [list, holder, box, named]*.get() == ['list', 'holder', 'box', 'named']
            assert ([list, holder, box, named] + map.values()).every { it instanceof Closure }
        '''
    }

    @Test
    void testDynamicCodeReadingAClosureStoredByStaticallyCompiledCode() {
        assertScript shell, '''
            @CompileStatic
            Map<String, Function<String, String>> make() {
                Map<String, Function<String, String>> map = [:]
                map['shout'] = { String s -> s.toUpperCase() }
                map.put('whisper') { String s -> s.toLowerCase() }
                map
            }

            def h = make()
            assert h.shout('Hi') == 'HI'
            assert h.shout.call('Hi') == 'HI'
            assert h.shout.apply('Hi') == 'HI'
            assert h.whisper('Hi') == 'hi'
            assert h.whisper.invokeMethod('apply', 'Hi') == 'hi'
            def apply = h.whisper.&apply
            assert apply('Hi') == 'hi'
            assert h.whisper.with { apply('Hi') } == 'hi'
            assert h.whisper.respondsTo('apply')
        '''

        assertScript shell, '''
            interface Shout { String shout(String s); default String twice(String s) { shout(s) + shout(s) } }
            @CompileStatic
            List<Shout> make() {
                List<Shout> list = []
                list.add { String s -> s + '!' }
                list
            }

            // a default method of the interface, called as statically compiled code calls it
            def shout = make()[0]
            assert shout.twice('a') == 'a!a!'
            assert shout.invokeMethod('twice', 'a') == 'a!a!'
            def twice = shout.&twice
            assert twice('a') == 'a!a!'
            assert shout.with { twice('a') } == 'a!a!'
            assert shout.respondsTo('twice')
        '''
    }

    @Test
    void testComposedCurriedOrWritableClosureAnswersTheInterfaceMethodOfTheClosureItWrapsByCallingItself() {
        assertScript shell, '''
            @CompileStatic
            List<Function<String, String>> make() {
                List<Function<String, String>> list = []
                list.add { String s -> s.trim() }
                list
            }
            @CompileStatic
            List<BiFunction<String, String, String>> make2() {
                List<BiFunction<String, String, String>> list = []
                list.add { String a, String b -> a + '|' + b }
                list
            }

            def f = make()[0], b = make2()[0]
            def up = { it.toUpperCase() }
            // the wrappers do not implement the interface, but answer its method as calling them does
            [f.andThen(up), f >> up, up << f].each { g ->
                assert g.apply(' a ') == 'A'
                assert g(' a ') == 'A'
            }
            assert f.asWritable().apply(' a ') == 'a'
            assert f.memoize().apply(' a ') == 'a'
            assert f.memoizeAtMost(2).apply(' a ') == 'a'
            assert f.trampoline().apply(' a ') == 'a'
            assert (f >> up).memoize().apply(' a ') == 'A'
            assert (f >> up).trampoline().apply(' a ') == 'A'
            assert b.curry('x').apply('y') == 'x|y'
            assert b.rcurry('y').apply('x') == 'x|y'
            groovy.test.GroovyAssert.shouldFail(MissingMethodException) { b.curry('x', 'y').apply('p', 'q') }
            assert b.curry('x', 'y')() == 'x|y'
            // the wrapped closure itself still answers it
            assert f.apply(' a ') == 'a'
            assert b.apply('p', 'q') == 'p|q'
        '''

        assertScript shell, '''
            interface Shout { String shout(String s); default String twice(String s) { shout(s) + shout(s) } }
            @CompileStatic
            List<Shout> make() {
                List<Shout> list = []
                list.add { String s -> s + '!' }
                list
            }

            def shout = make()[0]
            def g = shout >> { it.toUpperCase() }
            assert g.shout('a') == 'A!'
            // a default method is not the wrapper's: answering it from the wrapped closure would skip the wrapper
            groovy.test.GroovyAssert.shouldFail(MissingMethodException) { g.twice('a') }
            assert shout.twice('a') == 'a!a!'
        '''
    }

    @Test
    void testClosureValueIsStoredAsItIs() {
        assertScript shell, '''
            @CompileStatic
            class C {
                static Closure<String> closure = { 'variable' }
                static List<Supplier<String>> make() {
                    List<Supplier<String>> list = []
                    list.add(closure)
                    list << closure
                    list
                }
            }

            def list = C.make()
            assert list.every { it.is(C.closure) }
            assert list.contains(C.closure) && list.indexOf(C.closure) == 0
            assert list.remove((Object) C.closure)
            groovy.test.GroovyAssert.shouldFail(MissingMethodException) { list[0].get() }
        '''

        def warnings = warnings '''
            import groovy.transform.CompileStatic
            import java.util.function.Supplier
            @CompileStatic
            void test(Closure<String> closure, boolean flag) {
                List<Supplier<String>> list = []
                list.add(closure)
                list << closure
                list.add(flag ? { 'literal' } : closure)
                list.add(closure as Supplier<String>)
            }
        '''
        assert warnings.size() == 3
        assert warnings.every { it == warning('java.util.function.Supplier<java.lang.String>') }
    }

    // GROOVY-12561
    @Test
    void testLambdaOrMethodReferenceAssignedToASubscriptOrPropertyGetsNoWarning() {
        String source = '''
            import groovy.transform.CompileStatic
            import java.util.function.Function
            class Holder<T> { T value }
            @CompileStatic
            class W {
                static List<Object> make(Closure<String> closure) {
                    Map<String, Function<String, String>> map = [:]
                    map['lambda'] = (String s) -> s + '!'
                    map['reference'] = String::trim
                    def holder = new Holder<Function<String, String>>()
                    holder.value = (String s) -> s + '?'
                    def field = new Holder<Function<String, String>>()
                    field.@value = String::toUpperCase
                    map['closure'] = closure // warning: stored as a Closure
                    [map['lambda'], map['reference'], holder.value, field.@value]
                }
            }
        '''
        // the assignment compiles a lambda or method reference for the interface, so only the closure is stored as it is
        assert warnings(source) == [warning('java.util.function.Function<java.lang.String, java.lang.String>')]

        assertScript shell, source + '''
            def values = W.make { it }
            assert values.every { it instanceof Function && !(it instanceof Closure) }
            assert values*.apply(' a ') == [' a !', 'a', ' a ?', ' A ']
        '''
    }

    // GROOVY-12561
    @Test
    void testWarningOnlyForAValueStoredAsAPlainClosure() {
        String source = '''
            import groovy.transform.CompileStatic
            import java.util.function.Function
            import org.apache.groovy.util.Closures
            class Holder<T> { T value }
            @CompileStatic
            class W {
                static void make(Closure<String> closure, boolean flag) {
                    Closures.FunctionClosure<String, String> function = Closures.from((Function<String, String>) { String s -> s })
                    List<Function<String, String>> list = []
                    list.add(function)                                   // a Function already
                    list << function
                    Map<String, Function<String, String>> map = [:]
                    map['function'] = function
                    map['lambdas'] = flag ? ((String s) -> s) : ((String s) -> s.trim())
                    map['references'] = flag ? String::trim : String::strip
                    new Holder<Function<String, String>>(value: (String s) -> s) // its class implements the interface
                    list.add(closure)                                    // warning
                    map['branches'] = flag ? closure : closure           // warning for each
                }
            }
        '''
        def warnings = warnings(source)
        assert warnings.size() == 3
        assert warnings.every { it == warning('java.util.function.Function<java.lang.String, java.lang.String>') }
    }

    // GROOVY-12561
    @Test
    void testMethodPointerIsStoredAsItIs() {
        String source = '''
            import groovy.transform.CompileStatic
            import java.util.function.Function
            @CompileStatic
            class W {
                static String shout(String s) { s.toUpperCase() }
                static List<Function<String, String>> make() {
                    List<Function<String, String>> list = []
                    list.add(W.&shout)          // warning
                    list << W.&shout            // warning
                    Map<String, Function<String, String>> map = [:]
                    map['k'] = W.&shout         // warning
                    list.add(map['k'])
                    list
                }
            }
        '''
        def warnings = warnings(source)
        assert warnings.size() == 3
        assert warnings.every { it == warning('java.util.function.Function<java.lang.String, java.lang.String>') }

        assertScript shell, source + '''
            // a method pointer is a MethodClosure, which dynamic code calls as a closure
            def list = W.make()
            assert list.every { it instanceof org.codehaus.groovy.runtime.MethodClosure }
            assert list*.call('a') == ['A', 'A', 'A']
        '''
    }

    @Test
    void testClosureLiteralIsPassedAsBeforeWhereItCannotImplementTheInterface() {
        assertScript shell, '''
            import java.util.concurrent.Callable
            interface Caller { String call(String s) }
            abstract class Task { abstract String run(String s) }
            interface SerializableRunnable extends Runnable, Serializable { }
            class Holder<T> { T value }
            @CompileStatic
            class C {
                static List<Object> make() {
                    List<Callable<String>> callables = []
                    callables.add { 'callable' }
                    def runnable = new Holder<Runnable>()
                    runnable.value = { -> }
                    List<Caller> callers = []
                    callers.add { String s -> s }
                    List<Task> tasks = []
                    tasks.add { String s -> s }
                    List<SerializableRunnable> serializables = []
                    serializables.add { -> }
                    [callables[0], runnable.value, callers[0], tasks[0], serializables[0]]
                }
            }

            def values = C.make()
            // a Closure is a Callable and a Runnable; the others have a method of Closure or are not interfaces
            assert values.every { it instanceof Closure }
            assert !(values[2] instanceof Caller) && !(values[3] instanceof Task) && !(values[4] instanceof SerializableRunnable)
            assert values[0].call() == 'callable'
        '''

        assertScript shell, '''
            @CompileStatic
            List<Supplier<String>> make() {
                Supplier<String> variable = { 'variable' } // the slot's type is the interface
                List<Supplier<String>> list = [variable]
                list
            }

            def list = make()
            assert list[0].get() == 'variable' && !(list[0] instanceof Closure)
        '''
    }

    // GROOVY-12561
    @Test
    void testClosureLiteralIsPassedAsBeforeForAnInterfaceItsClassMayNotImplement() {
        assertScript shell, '''
            sealed interface Shout permits Loud { String shout(String s) }
            final class Loud implements Shout { String shout(String s) { s.toUpperCase() } }
            trait Greeter { abstract String greet(String s); String twice(String s) { greet(s) + greet(s) } }
            @CompileStatic
            class C {
                static List<Object> make() {
                    List<Shout> shouts = []
                    shouts.add { String s -> s + '!' }
                    List<Greeter> greeters = []
                    greeters.add { String s -> 'hi ' + s }
                    [shouts[0], greeters[0]]
                }
            }

            // only the classes a sealed interface permits may implement it, and a trait's methods are not added
            def values = C.make()
            assert values.every { it instanceof Closure }
            assert !(values[0] instanceof Shout) && !(values[1] instanceof Greeter)
        '''

        assertScript shell, '''
            interface Owned { String apply(String s); default Object getOwner() { 'default owner' } }
            interface Delegating extends Owned { default Object getDelegate() { 'default delegate' } }
            @CompileStatic
            class C {
                static List<Object> make() {
                    List<Owned> owned = []
                    owned.add { String s -> s }
                    List<Delegating> delegating = []
                    delegating.add { String s -> s }
                    Owned o = owned[0]
                    Delegating d = delegating[0]
                    [owned[0], delegating[0], o.getOwner(), d.getDelegate()]
                }
            }

            // Closure's getOwner and getDelegate would replace the default methods
            def values = C.make()
            assert values[0] instanceof Closure && !(values[0] instanceof Owned)
            assert values[1] instanceof Closure && !(values[1] instanceof Delegating)
            assert values[2] == 'default owner' && values[3] == 'default delegate'
        '''

        def parent = new GroovyClassLoader()
        parent.parseClass '''
            package p
            @groovy.transform.PackageScope
            interface Callback { String f(String s) }
        '''
        // another class loader defines C, so C is in another run-time package than Callback
        def c = new GroovyClassLoader(parent).parseClass '''
            package p
            @groovy.transform.CompileStatic
            class C {
                static List make() {
                    List<Callback> list = []
                    list.add { String s -> s + '!' }
                    list
                }
            }
        '''
        assert c.make()[0] instanceof Closure
    }

    @Test
    void testTypeCheckedCodePassesAClosureAsBefore() {
        def err = shouldFail shell, '''
            @TypeChecked
            void test() {
                List<Function<String, String>> list = []
                list.add { it.toUpperCase() }
            }
        '''
        assert err.message.contains('Cannot find matching method java.lang.Object#toUpperCase()')

        assertScript shell, '''
            @TypeChecked
            List<Function<String, String>> make() {
                List<Function<String, String>> list = []
                list.add { String s -> s.toUpperCase() }
                list
            }

            def list = make()
            assert list[0] instanceof Closure && !(list[0] instanceof Function)
            groovy.test.GroovyAssert.shouldFail(MissingMethodException) { list[0].apply('x') }
        '''
    }

    @Test
    void testPackedClosures() {
        assertScript shell, '''
            @CompileStatic @PackedClosures
            List<Supplier<String>> make() {
                List<Supplier<String>> list = []
                list.add { 'x' } // the closure keeps its class
                list
            }

            def list = make()
            assert list[0].get() == 'x' && list[0] instanceof Closure
        '''

        def err = shouldFail shell, '''
            @CompileStatic @PackedClosures(mode = PackedClosures.PackMode.STRICT)
            void test() {
                List<Supplier<String>> list = []
                list.add { 'x' }
            }
        '''
        assert err.message.contains('written for a type parameter bound to java.util.function.Supplier<java.lang.String>')
        assert err.message.contains('use mode LENIENT or WARN to compile it unpacked')
    }

    private static String warning(String type) {
        "the closure is stored as a Closure, not as $type: a statically compiled read through $type will fail; write 'as $type' to convert it"
    }

    private static List<String> warnings(String source) {
        def unit = new CompilationUnit(new CompilerConfiguration(warningLevel: WarningMessage.LIKELY_ERRORS))
        unit.addSource('W.groovy', source)
        unit.compile(Phases.CLASS_GENERATION)
        unit.errorCollector.warnings*.message
    }

    @Test
    void testHoistedLambda() {
        String source = '''
            import groovy.transform.CompileStatic
            import java.util.function.Supplier
            @CompileStatic
            class H {
                static List<Supplier<String>> make() {
                    List<Supplier<String>> list = [null]
                    list[0] = () -> 'y' // an assignment compiles a lambda for the interface
                    list
                }
            }
        '''
        String previous = System.getProperty(CompilerConfiguration.LAMBDA_HOISTING)
        try {
            for (hoist in [false, true]) {
                if (hoist) System.setProperty(CompilerConfiguration.LAMBDA_HOISTING, 'true')
                else System.clearProperty(CompilerConfiguration.LAMBDA_HOISTING)

                def unit = new CompilationUnit(new CompilerConfiguration())
                unit.addSource('H.groovy', source)
                unit.compile(Phases.CLASS_GENERATION)
                List<String> names = unit.classes*.name
                assert names.any { it.contains('_lambda') } == !hoist
                assert !names.any { it.contains('_closure') }

                def loader = new GroovyClassLoader()
                unit.classes.each { loader.defineClass(it.name, it.bytes) }
                assert loader.loadClass('H').make()*.get() == ['y']
            }
        } finally {
            if (previous != null) System.setProperty(CompilerConfiguration.LAMBDA_HOISTING, previous)
            else System.clearProperty(CompilerConfiguration.LAMBDA_HOISTING)
        }
    }
}
