import groovy.test.GroovyTestCase

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
class ScriptsAndClassesSpecTest extends GroovyTestCase {
    void testMainMethod() {
        assertScript '''
            // tag::groovy_class_with_main_method[]
            class Main {                                    // <1>
                static void main(String... args) {          // <2>
                    println 'Groovy world!'                 // <3>
                }
            }
            // end::groovy_class_with_main_method[]
        '''
        assertScript '''
            // tag::groovy_script[]
            println 'Groovy world!'
            // end::groovy_script[]
        '''

        assertScript '''
            // tag::groovy_script_equiv[]
            import org.codehaus.groovy.runtime.InvokerHelper
            class Main extends Script {                     // <1>
                def run() {                                 // <2>
                    println 'Groovy world!'                 // <3>
                }
                static void main(String[] args) {           // <4>
                    InvokerHelper.runScript(Main, args)     // <5>
                }
            }
            // end::groovy_script_equiv[]
        '''
    }

    void testMethodDefinition() {
        assertScript '''
            // tag::method_in_script[]
            int fib(int n) {
                n < 2 ? 1 : fib(n-1) + fib(n-2)
            }
            assert fib(10)==89
            // end::method_in_script[]
        '''

        assertScript '''
            // tag::multiple_methods_assembly[]
            println 'Hello'                                 // <1>

            int power(int n) { 2**n }                       // <2>

            println "2^6==${power(6)}"                      // <3>
            // end::multiple_methods_assembly[]
        '''

        assertScript '''
            // tag::multiple_methods_assembly_equiv[]
            import org.codehaus.groovy.runtime.InvokerHelper
            class Main extends Script {
                int power(int n) { 2** n}                   // <1>
                def run() {
                    println 'Hello'                         // <2>
                    println "2^6==${power(6)}"              // <3>
                }
                static void main(String[] args) {
                    InvokerHelper.runScript(Main, args)
                }
            }
            // end::multiple_methods_assembly_equiv[]
        '''
    }

    void testScriptVariables() {
        assertScript '''
            // tag::script_with_variables[]
            int x = 1
            int y = 2
            assert x+y == 3
            // end::script_with_variables[]
        '''
        assertScript '''
            // tag::script_with_untyped_variables[]
            x = 1
            y = 2
            assert x+y == 3
            // end::script_with_untyped_variables[]
        '''
    }
}
