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
package bugs

import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.shouldFail

/**
 * GROOVY-9979: {@code @CompileStatic} must report the same numeric
 * precision-loss errors for an assignment, a method argument, and a
 * return (historically {@code Byte bar() { return (Float) 4324141.4 }}
 * was accepted while the assignment and argument forms were rejected).
 */
final class Groovy9979 {

    @Test
    void testStaticCompilerRejectsFloatToByteAssignment() {
        def err = shouldFail(MultipleCompilationErrorsException) {
            new GroovyShell().evaluate '''
                @groovy.transform.CompileStatic
                def f() {
                    final Byte f = (Float) 32.3
                }
            '''
        }
        assert err.message.contains('Possible loss of precision from java.lang.Float to java.lang.Byte')
    }

    @Test
    void testStaticCompilerRejectsFloatToByteArgument() {
        def err = shouldFail(MultipleCompilationErrorsException) {
            new GroovyShell().evaluate '''
                @groovy.transform.CompileStatic
                class Main {
                    static void baz(Byte x) {}
                    static void foo() { baz((Float) 13.3) }
                }
            '''
        }
        assert err.message.contains('Cannot find matching method Main#baz(java.lang.Float)')
    }

    @Test
    void testStaticCompilerRejectsFloatToByteReturn() {
        def err = shouldFail(MultipleCompilationErrorsException) {
            new GroovyShell().evaluate '''
                @groovy.transform.CompileStatic
                class Main {
                    static Byte bar() {
                        return (Float) 4324141.4
                    }
                }
            '''
        }
        assert err.message.contains('Possible loss of precision from java.lang.Float to java.lang.Byte')
    }
}
