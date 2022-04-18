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
package org.codehaus.groovy.transform

import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.*

import static groovy.test.GroovyAssert.shouldFail

/**
 * Tests for the {@code @AutoFinal} AST transform.
 */
final class AutoFinalTransformTest extends gls.CompilableTestSupport {

    void testAutoFinalOnClass() {
        // use ASTTest here since final modifier isn't put into bytecode so not available via reflection
        assertScript '''
            import groovy.transform.*
            import static java.lang.reflect.Modifier.isFinal

            @ASTTest(phase=SEMANTIC_ANALYSIS, value={
                assert node.methods.size() == 1
                node.methods[0].with {
                    assert it.name == 'fullName'
                    assert it.parameters.every{ p -> isFinal(p.modifiers) }
                }
                assert node.constructors.size() == 1
                node.constructors[0].with {
                    assert it.parameters.every{ p -> isFinal(p.modifiers) }
                }
            })
            @AutoFinal
            class Person {
                final String first, last
                Person(String first, String last) {
                    this.first = first
                    this.last = last
                }
                String fullName(boolean reversed = false, String separator = ' ') {
                    "${reversed ? last : first}$separator${reversed ? first : last}"
                }
            }

            def js = new Person('John', 'Smith')
            assert js.fullName() == 'John Smith'
            assert js.fullName(true, ', ') == 'Smith, John'
        '''
    }

    void testAutoFinalOnClassButDisabledOnMethod() {
        // use ASTTest here since final modifier isn't put into bytecode so not available via reflection
        assertScript '''
            import groovy.transform.*
            import static java.lang.reflect.Modifier.isFinal

            @ASTTest(phase=SEMANTIC_ANALYSIS, value={
                assert node.methods.size() == 2
                node.methods[0].with {
                    assert it.name == 'fullName'
                    assert it.parameters.every{ p -> isFinal(p.modifiers) }
                }
                node.methods[1].with {
                    assert it.name == 'initials'
                    assert it.parameters.every{ p -> !isFinal(p.modifiers) }
                }
                assert node.constructors.size() == 1
                node.constructors[0].with {
                    assert it.parameters.every{ p -> isFinal(p.modifiers) }
                }
            })
            @AutoFinal
            class Person {
                final String first, last
                Person(String first, String last) {
                    this.first = first
                    this.last = last
                }
                String fullName(boolean reversed = false, String separator = ' ') {
                    "${reversed ? last : first}$separator${reversed ? first : last}"
                }
                @AutoFinal(enabled=false)
                String initials(boolean lower) {
                    def raw = "${first[0]}${last[0]}"
                    lower ? raw.toLowerCase() : raw
                }
            }

            def js = new Person('John', 'Smith')
            assert js.fullName() == 'John Smith'
            assert js.fullName(true, ', ') == 'Smith, John'
            assert js.initials(true) == 'js'
        '''
    }

    // GROOVY-10585
    void testAutoFinalOnClassWithAnInnerInterface() {
        assertScript '''
            @groovy.transform.AutoFinal
            class C {
                interface I {
                }
            }
            new C()
        '''
    }

    // GROOVY-10585
    void testAutoFinalOnMethodButDisabledViaConfig() {
        GroovyShell shell = new GroovyShell(new CompilerConfiguration().addCompilationCustomizers(
            new ASTTransformationCustomizer(groovy.transform.AutoFinal, enabled: false)
        ))
        def err = shouldFail {
            shell.evaluate '''
                class C {
                    void one(x) {
                        x = 1
                    }
                    @groovy.transform.AutoFinal
                    void two(y) {
                        y = 2
                    }
                }
            '''
        }
        assert err =~ /The parameter \[y\] is declared final but is reassigned/
    }
}
