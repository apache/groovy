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
import groovy.test.GroovyTestCase
import groovy.transform.CompileStatic

@CompileStatic
class BaseScriptSpecTest extends GroovyTestCase {
    void testSimpleScript() {
        def script = '''
// tag::simple_script[]
println 'Hello from Groovy'
// end::simple_script[]
assert (this instanceof Script)
'''
        def shell = new GroovyShell()
        def o = shell.parse(script)
        assert o instanceof Script
    }

    void testScriptWithBinding() {
        // tag::integ_binding[]
        def binding = new Binding()             // <1>
        def shell = new GroovyShell(binding)    // <2>
        binding.setVariable('x',1)              // <3>
        binding.setVariable('y',3)
        shell.evaluate 'z=2*x+y'                // <4>
        assert binding.getVariable('z') == 5    // <5>
        // end::integ_binding[]
    }

    void testBaseClassThroughConfig() {
        assertScript '''import org.codehaus.groovy.control.CompilerConfiguration
            // tag::baseclass_def[]
            abstract class MyBaseClass extends Script {
                String name
                public void greet() { println "Hello, $name!" }
            }
            // end::baseclass_def[]

            // tag::use_custom_conf[]
            def config = new CompilerConfiguration()                                // <1>
            config.scriptBaseClass = 'MyBaseClass'                                  // <2>
            def shell = new GroovyShell(this.class.classLoader, config)             // <3>
            shell.evaluate """
                setName 'Judith'                                                    // <4>
                greet()
            """
            // end::use_custom_conf[]
        '''
    }

    void testBaseClassThroughBaseScript() {
        assertScript '''
            abstract class MyBaseClass extends Script {
                String name
                public void greet() { println "Hello, $name!" }
            }

            def shell = new GroovyShell(this.class.classLoader)
            shell.evaluate """
                // tag::use_basescript[]
                import groovy.transform.BaseScript

                @BaseScript MyBaseClass baseScript
                setName 'Judith'
                greet()
                // end::use_basescript[]
            """
        '''

        assertScript '''
            abstract class MyBaseClass extends Script {
                String name
                public void greet() { println "Hello, $name!" }
            }

            def shell = new GroovyShell(this.class.classLoader)
            shell.evaluate """
                // tag::use_basescript_alt[]
                @BaseScript(MyBaseClass)
                import groovy.transform.BaseScript

                setName 'Judith'
                greet()
                // end::use_basescript_alt[]
            """
        '''
    }


    void testBaseClassCustomRunMethod() {
        assertScript '''import org.codehaus.groovy.control.CompilerConfiguration

            // tag::custom_run_method[]
            abstract class MyBaseClass extends Script {
                int count
                abstract void scriptBody()                              // <1>
                def run() {
                    count++                                             // <2>
                    scriptBody()                                        // <3>
                    count                                               // <4>
                }
            }
            // end::custom_run_method[]


            def conf = new CompilerConfiguration()
            conf.scriptBaseClass = 'MyBaseClass'
            def shell = new GroovyShell(this.class.classLoader, conf)

            // tag::custom_run_eval[]
            def result = shell.evaluate """
                println 'Ok'
            """
            assert result == 1
            // end::custom_run_eval[]

            // tag::custom_run_parse[]
            def script = shell.parse("println 'Ok'")
            assert script.run() == 1
            assert script.run() == 2
            // end::custom_run_parse[]
        '''
    }

}
