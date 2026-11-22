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
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

/**
 * A lambda or method reference written for a parameter declared with a type parameter, such as {@code E} of
 * {@code List.add(E)}, where the call binds the type parameter to a functional interface: statically compiled
 * code compiles a method reference for that interface, and a lambda too with the incubating
 * {@code groovy.target.lambda.typearg} option, which these tests set unless they say otherwise.
 * Dynamic and type-checked code pass it as they did.
 */
final class FunctionalTypeArgumentTest {

    private static final String PROP = CompilerConfiguration.LAMBDA_TYPE_ARGUMENT

    private String previous

    @BeforeEach
    void setUp() {
        previous = System.getProperty(PROP)
        System.setProperty(PROP, 'true')
    }

    @AfterEach
    void tearDown() {
        if (previous != null) System.setProperty(PROP, previous) else System.clearProperty(PROP)
    }

    /** Runs the work with the option cleared; the property is read whenever a call is type checked. */
    private static <T> T withoutLambdaTypeArgument(Closure<T> work) {
        String value = System.clearProperty(PROP)
        try {
            work.call()
        } finally {
            if (value != null) System.setProperty(PROP, value)
        }
    }

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
    void testLambdaIsCompiledAsAClosureWithoutTheOption() {
        withoutLambdaTypeArgument {
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
                    static List<Function<String, String>> references() {
                        List<Function<String, String>> list = []
                        list.add(String::toUpperCase)
                        list << String::trim
                        list
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

                // a method reference is compiled for the interface whether or not the option is set
                assert !(functions.reference instanceof Closure)
                assert functions.reference.apply('hi') == 'HI'
                def references = C.references()
                assert references*.apply(' ab ') == [' AB ', 'ab']
                assert references.every { it instanceof Function && !(it instanceof Closure) }
            '''
        }
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
            // a lambda is an instance of the interface, not a closure
            groovy.test.GroovyAssert.shouldFail(MissingMethodException) { list[0]() }
            groovy.test.GroovyAssert.shouldFail(MissingMethodException) { list[0].call() }
            def commands = commands()
            assert commands.shout.apply('hi') == 'HI'
            groovy.test.GroovyAssert.shouldFail(MissingMethodException) { commands.shout('hi') }
            groovy.test.GroovyAssert.shouldFail(MissingMethodException) { commands.shout.call('hi') }
        '''
    }

    @Test
    void testTypeCheckedCodePassesALambdaOrMethodReferenceAsBefore() {
        typeCheckedCodePassesALambdaOrMethodReferenceAsBefore()
        withoutLambdaTypeArgument { typeCheckedCodePassesALambdaOrMethodReferenceAsBefore() }
    }

    private void typeCheckedCodePassesALambdaOrMethodReferenceAsBefore() {
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
