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
 * parameter to a functional interface: statically compiled code compiles a lambda or method reference for that
 * interface, and the class of a closure literal implements it. Dynamic and type-checked code pass them as they did.
 */
final class FunctionalTypeArgumentTest {

    private final GroovyShell shell = GroovyShell.withConfig {
        imports {
            star 'groovy.transform'
            star 'java.util.function'
        }
    }

    @Test
    void testLambdaOrMethodReferenceIsCompiledForTheInterfaceTheCallBindsItTo() {
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
            assert suppliers.every { it instanceof Supplier && !(it instanceof Closure) }
            def functions = C.functions()
            assert functions.collectEntries { k, f -> [k, f.apply('ab')] } == [lambda: 'ab?', implicit: 'AB', reference: 'AB']
            assert functions.values().every { !(it instanceof Closure) }
            def shout = C.shout()
            assert shout.apply('a') == 'a?'
            assert shout instanceof Shout
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

            def list = make()
            assert list[0].get() == 'x'
            // a lambda is an instance of the interface, not a closure
            groovy.test.GroovyAssert.shouldFail(MissingMethodException) { list[0]() }
            groovy.test.GroovyAssert.shouldFail(MissingMethodException) { list[0].call() }
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
            assert list[0] instanceof Closure
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
        assert warnings.every { it == "The Closure is stored as it is, not converted to java.util.function.Supplier<java.lang.String>; write 'as java.util.function.Supplier<java.lang.String>' to convert it" }
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
        assert err.message.contains('written for a type parameter bound to a functional interface')
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
                    List<Supplier<String>> list = []
                    list.add(() -> 'y')
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
