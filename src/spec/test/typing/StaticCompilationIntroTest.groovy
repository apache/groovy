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
package typing

import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

final class StaticCompilationIntroTest {

    private static String TYPESAFE_PROGRAM = '''
        // tag::intro_typesafe[]
        class Computer {
            int compute(String str) {
                str.length()
            }
            String compute(int x) {
                String.valueOf(x)
            }
        }

        @groovy.transform.TypeChecked
        void test() {
            def computer = new Computer()
            computer.with {
                assert compute(compute('foobar')) =='6'
            }
        }
        // end::intro_typesafe[]
    '''

    private static String TYPESAFE_COMPILESTATIC_PROGRAM = '''
        // tag::intro_typesafe_compilestatic[]
        class Computer {
            int compute(String str) {
                str.length()
            }
            String compute(int x) {
                String.valueOf(x)
            }
        }

        @groovy.transform.CompileStatic
        void test() {
            def computer = new Computer()
            computer.with {
                assert compute(compute('foobar')) =='6'
            }
        }
        // end::intro_typesafe_compilestatic[]
    '''

    private static final String RUN = '''
        test()
    '''

    private static final String RUNTIME_MAGIC = '''
        // tag::intro_typesafe_magic[]
        Computer.metaClass.compute = { String str -> new Date() }
        // end::intro_typesafe_magic[]
    '''

    @Test
    void testTypeSafeProgram() {
        assertScript(TYPESAFE_PROGRAM+RUN)
    }

    @Test
    void testTypeSafeProgramBroken() {
        def e = shouldFail(MissingMethodException, TYPESAFE_PROGRAM+RUNTIME_MAGIC+RUN)
        assert e.message.contains('No signature of method: compute for class: Computer is applicable for argument types: (Date)')
    }

    @Test
    void testTypeSafeProgramFixedWithCompileStatic() {
        assertScript(TYPESAFE_COMPILESTATIC_PROGRAM+RUNTIME_MAGIC+RUN)
    }
}
