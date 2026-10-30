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

import groovy.transform.stc.ClosuresSTCTest
import org.junit.jupiter.api.Test

/**
 * Unit tests for static compilation: closures.
 */
final class ClosuresStaticCompileTest extends ClosuresSTCTest implements StaticCompilationTestSupport {

    // GROOVY-5584
    @Test
    void testEachOnMapClosure() {
        assertScript '''
            def test() {
                def result = ""
                [a:1, b:3].each { key, value -> result += "$key$value" }
                assert result == "a1b3"
            }
            test()
        '''
    }

    // GROOVY-5811
    @Test
    void testClosureExceptionUnwrap() {
        assertScript '''
            @groovy.transform.InheritConstructors
            public class MyException extends Exception {}

            void foo() {
                throw new MyException()
            }

            void bar() {
                boolean caught = false
                try {
                    def cl = { foo() }
                    cl()
                } catch (MyException e) {
                    caught = true
                } finally {
                    assert caught
                }
            }

            bar()
        '''
    }

    // GROOVY-6522
    @Test
    void testShouldCallClosure() {
        assertScript '''
            class Sample {
                Closure formatFirstName, formatLastName

                void doStuff (String fname, String lname, Closure callback) {
                    formatFirstName(fname) { String fnameError, String formattedFname ->
                        formatLastName(lname) { String lnameError, String formattedLname ->
                            String errors = "${fnameError ? "${fnameError}, " : ''}${lnameError ?: ''}"
                            callback(errors, formattedFname, formattedLname)
                        }
                    }
                }
            }

            Closure ffn = { String fname, Closure callback ->
                String firstInitial = fname?.substring(0,1)
                if (!firstInitial)
                    callback('invalid first name', null)
                else
                    callback(null, firstInitial.toLowerCase())
            }

            Closure fln = { String lname, Closure callback ->
                String lastPrefix = lname?.size() > 2 ? lname.substring(0,3) : null
                if (!lastPrefix)
                    callback('invalid last name', null)
                else
                    callback(null, lastPrefix.toLowerCase())
            }

            Sample sample = new Sample(formatFirstName: ffn, formatLastName: fln)

            sample.doStuff('John', 'Doe') { String errors, String formattedFname, String formattedLname ->
                if (errors)
                    println errors
                else
                    println "${formattedFname}.${formattedLname}"
            }
        '''
    }

    @Test
    void testMethodVersusPropertyOnCompoundAssignmentTarget() {
        assertScript '''
            import org.codehaus.groovy.ast.expr.*
            import org.codehaus.groovy.ast.stmt.*

            class Pogo {
                String string = ''
                void setString(String string) {
                    this.string = string
                }
            }

            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                node = node.code.statements[0].expression
                assert node instanceof MethodCallExpression

                node = node.arguments.expressions[0].code.statements[0].expression
                assert node instanceof BinaryExpression

                assert node.leftExpression instanceof PropertyExpression
                def target = node.leftExpression.getNodeMetaData(DIRECT_METHOD_CALL_TARGET)

                assert target != null && target.name == 'setString'
            })
            void test(Pogo pogo) {
                pogo.with {
                    string += 'x'
                }
            }

            test(new Pogo())
        '''
    }

    // GROOVY-10979
    @Test
    void testClosurePassedForATypeParameterIsConvertedToTheInterfaceTheCallBindsItTo() {
        assertScript '''
            import java.util.concurrent.atomic.AtomicReference
            import java.util.function.Function
            import java.util.function.Supplier
            interface Shout extends Function<String, String> { }
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
                    Closure<String> variable = { 'variable' }
                    list.add(variable)
                    list.add(flag ? { 'then' } : { 'else' })
                    list.add(new AtomicReference<Supplier<String>>({ 'constructor' }).get())
                    list.add(C.<Supplier<String>>id { 'type argument' })
                    list
                }
                static Map<String, Function<String, String>> functions() {
                    Map<String, Function<String, String>> map = [:]
                    map.put('put') { String s -> s + '!' }
                    map.putIfAbsent('implicit') { it.toUpperCase() }
                    map.put('lambda', (String s) -> s + '?')
                    map.put('reference', String::toUpperCase)
                    map
                }
                static Shout shout() {
                    Shout shout = { String s -> s + '!' }
                    second(shout, { String s -> s + '?' }) // T is Shout
                }
            }

            def suppliers = C.suppliers(true)
            assert suppliers*.get() == ['set', 'add', 'shift', 'vararg', 'varargs', 'variable', 'then', 'constructor', 'type argument']
            assert suppliers.every { it instanceof Supplier && !(it instanceof Closure) }
            assert C.suppliers(false)[6].get() == 'else'
            def functions = C.functions()
            assert functions.collectEntries { k, f -> [k, f.apply('ab')] } == [put: 'ab!', implicit: 'AB', lambda: 'ab?', reference: 'AB']
            assert functions.values().every { !(it instanceof Closure) }
            def shout = C.shout()
            assert shout.apply('a') == 'a?'
            assert shout instanceof Shout
        '''
    }

    // GROOVY-10979
    @Test
    void testClosureReturnedForATypeParameterIsConvertedToTheInterfaceTheCallBindsItTo() {
        assertScript '''
            import java.util.function.Supplier
            class C {
                static <T> T pick(T a, T b) { b }
            }

            List<Supplier<String>> list = []
            list.add(C.pick({ 'a' }, { 'b' })) // T of pick is Closure, E of add is Supplier
            list << C.pick({ 'a' }, { 'c' })
            Collections.addAll(list, C.pick({ 'a' }, { 'd' }), C.pick({ 'a' }, { 'e' }))
            Map<String, Supplier<String>> map = [:]
            map.put('k', C.pick({ 'a' }, { 'f' }))
            list.add(map.k)
            assert list*.get() == ['b', 'c', 'd', 'e', 'f']
            assert list.every { it instanceof Supplier && !(it instanceof Closure) }
        '''
    }
}
