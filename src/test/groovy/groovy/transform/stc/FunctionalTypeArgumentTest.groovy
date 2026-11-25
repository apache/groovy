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

import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

/**
 * A method reference written as an argument for a parameter declared with a type parameter, such as {@code E} of
 * {@code List.add(E)}, where the call binds the type parameter to a functional interface: statically compiled
 * code compiles it for that interface. Lambdas, closures and operator operands are compiled as before, and
 * dynamic and type-checked code pass a method reference as they did.
 */
final class FunctionalTypeArgumentTest {

    private final GroovyShell shell = GroovyShell.withConfig {
        imports {
            star 'groovy.transform'
            star 'java.util.function'
        }
    }

    @Test
    void testMethodReferenceIsCompiledForTheInterfaceTheCallBindsItTo() {
        assertScript shell, '''
            import java.util.concurrent.atomic.AtomicReference
            interface Shout extends Function<String, String> { }
            @CompileStatic
            class C {
                static <T extends Function<String, String>> T second(T a, T b) { b }
                static <T> void addAll(List<T> list, T... items) { list.addAll(items) }
                static <T> T id(T t) { t }
                static List<Function<String, String>> functions() {
                    List<Function<String, String>> list = []
                    list.add(String::trim)
                    list.add(0, String::toUpperCase)
                    list.set(1, String::toLowerCase)
                    addAll(list, String::strip, String::stripLeading)
                    list.add(new AtomicReference<Function<String, String>>(String::stripTrailing).get())
                    list.add(C.<Function<String, String>>id(String::trim))
                    list
                }
                static Map<String, Function<String, String>> commands() {
                    Map<String, Function<String, String>> map = [:]
                    map.put('upper', String::toUpperCase)
                    map.putIfAbsent('trim', String::trim)
                    map
                }
                static Shout shout() {
                    Shout shout = (String s) -> s + '!'
                    second(shout, String::trim) // T is Shout
                }
            }

            def functions = C.functions()
            assert functions*.apply(' Ab ') == [' AB ', ' ab ', 'Ab', 'Ab ', ' Ab', 'Ab']
            assert functions.every { it instanceof Function && !(it instanceof Closure) }
            def commands = C.commands()
            assert commands.collectEntries { k, f -> [k, f.apply(' ab ')] } == [upper: ' AB ', trim: 'ab']
            def shout = C.shout()
            assert shout.apply(' a ') == 'a'
            assert shout instanceof Shout
        '''
    }

    @Test
    void testLambdaAndClosureAreCompiledAsClosures() {
        assertScript shell, '''
            @CompileStatic
            class C {
                static List<Supplier<String>> suppliers() {
                    List<Supplier<String>> list = []
                    list.add(() -> 'lambda')
                    list.add { 'closure' }
                    list << (() -> 'shift')
                    list
                }
                static Map<String, Function<String, String>> functions() {
                    Map<String, Function<String, String>> map = [:]
                    map.put('shout', (String s) -> s.toUpperCase())
                    map
                }
            }

            def suppliers = C.suppliers()
            assert suppliers.every { it instanceof Closure }
            assert suppliers.collect { it() } == ['lambda', 'closure', 'shift']
            def functions = C.functions()
            assert functions.shout instanceof Closure
            assert functions.shout('hi') == 'HI'
            assert functions.shout.call('hi') == 'HI'
        '''
    }

    @Test
    void testMethodReferenceOperandIsCompiledAsAClosure() {
        assertScript shell, '''
            @CompileStatic
            List<Function<String, String>> make() {
                List<Function<String, String>> list = []
                list << String::trim
                list
            }

            def list = make()
            assert list[0] instanceof Closure
            assert list[0](' ab ') == 'ab'
        '''
    }

    @Test
    void testDynamicCodeReadingAMethodReferenceStoredByStaticallyCompiledCode() {
        assertScript shell, '''
            @CompileStatic
            Map<String, Function<String, String>> commands() {
                Map<String, Function<String, String>> commands = [:]
                commands.put('trim', String::trim)
                commands
            }

            def commands = commands()
            assert commands.trim.apply(' hi ') == 'hi'
            // the method reference is an instance of the interface, not a closure
            groovy.test.GroovyAssert.shouldFail(MissingMethodException) { commands.trim(' hi ') }
            groovy.test.GroovyAssert.shouldFail(MissingMethodException) { commands.trim.call(' hi ') }
        '''
    }

    @Test
    void testDynamicCodePassesAMethodReferenceAsAClosure() {
        assertScript shell, '''
            List<Function<String, String>> make() {
                List<Function<String, String>> list = []
                list.add(String::trim)
                list
            }

            def list = make()
            assert list[0] instanceof Closure
            assert list[0](' ab ') == 'ab'
        '''
    }

    @Test
    void testTypeCheckedCodeRejectsAMethodReferenceAndPassesALambdaAsAClosure() {
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
}
