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

    // GROOVY-12561
    @Test
    void testClosureAssignedToASubscriptOrPropertyIsConvertedToItsInterface() {
        assertScript '''
            import java.util.function.Function
            import java.util.function.Supplier
            class Holder<T> { T value }
            class Box<T> { public T content }
            class C {
                static Map<String, Function<String, String>> functions(boolean flag) {
                    Map<String, Function<String, String>> map = [:]
                    map['subscript'] = { String s -> s + '!' }
                    map?['safe'] = { String s -> s + '?' }
                    map.property = { String s -> s.toUpperCase() }
                    map['conditional'] = flag ? { String s -> s.reverse() } : { String s -> s }
                    map
                }
                static List<Supplier<String>> suppliers() {
                    List<Supplier<String>> list = [null]
                    def value = (list[0] = { 'element' })
                    def holder = new Holder<Supplier<String>>()
                    holder.value = { 'property' }
                    def box = new Box<Supplier<String>>()
                    box.@content = { 'field' }
                    def named = new Holder<Supplier<String>>(value: { 'named' })
                    [list[0], value, holder.value, box.content, named.value]
                }
            }

            def functions = C.functions(true)
            assert functions.collectEntries { k, f -> [k, f.apply('ab')] } == [subscript: 'ab!', safe: 'ab?', property: 'AB', conditional: 'ba']
            assert functions.values().every { it instanceof Function && !(it instanceof Closure) }
            def (element, value, property, field, named) = C.suppliers()
            assert [element, value, property, field, named]*.get() == ['element', 'element', 'property', 'field', 'named']
            assert [element, value, property, field, named].every { it instanceof Supplier && !(it instanceof Closure) }
            assert value.is(element) // the value of the assignment is the converted closure
        '''
    }
}
