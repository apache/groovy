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

import org.codehaus.groovy.control.CompilerConfiguration;

final class BaseScriptTransformTest extends gls.CompilableTestSupport {

    void testInheritsFromCustomScript() {
        assertScript '''
            abstract class Custom extends Script {
            }

            @groovy.transform.BaseScript Custom self

            assert this.class.superclass == Custom
        '''
    }

    void testBaseScriptMustExtendsScript() {
        shouldNotCompile '''
            abstract class Custom {
            }

            @groovy.transform.BaseScript Custom self
        '''
    }

    void testThisObjectIsAssignedToBaseScriptVariable() {
        assertScript '''
            abstract class Custom extends Script {
            }

            @groovy.transform.BaseScript Custom self

            assert this == self
        '''
    }

    void testNotAllowedForClassFields() {
        shouldNotCompile '''
            abstract class Custom extends Script {
            }

            class Inner {
                @groovy.transform.BaseScript Custom nope
            }
        '''
    }

    void testNotAllowedForScriptInnerClassFields() {
        shouldNotCompile '''
            abstract class Custom extends Script {
            }

            class Inner {
                @groovy.transform.BaseScript Custom nope
            }

            println Inner.class.name
        '''
    }

    void testNotAllowedInClassMethods() {
        shouldNotCompile '''
            abstract class Custom extends Script {
            }

            class Inner {
                void test() {
                    @groovy.transform.BaseScript Custom nope
                }
            }
        '''
    }

    void testNotAllowedInScriptInnerClassMethods() {
        shouldNotCompile '''
            abstract class Custom extends Script {
            }

            class Inner {
                void test() {
                    @groovy.transform.BaseScript Custom nope
                }
            }

            println Inner.class.name
        '''
    }

    abstract class MyCustomScript extends Script {}

    void testBaseScriptFromCompiler() {
        CompilerConfiguration config = new CompilerConfiguration()
        config.scriptBaseClass = MyCustomScript.name
        GroovyShell shell = new GroovyShell(config)

        shell.evaluate('''
            abstract class Custom extends Script {
                int meaningOfLife = 42
            }

            @groovy.transform.BaseScript Custom self

            assert meaningOfLife == 42
        ''')
    }

    // GROOVY-6585
    void testBaseScriptAbstractMethod() {
        CompilerConfiguration config = new CompilerConfiguration()
        config.scriptBaseClass = MyCustomScript.name
        GroovyShell shell = new GroovyShell(config)

        def answer = shell.evaluate('''
            abstract class Custom extends Script {
                private int _meaningOfLife = 0
                int getMeaningOfLife() { _meaningOfLife }
                void setMeaningOfLife(int v) { _meaningOfLife = v }

                abstract def runScript()

                def preRun() { meaningOfLife |= 2 }
                def postRun() { meaningOfLife |= 8 }
                def run() {
                   preRun()
                   runScript()
                   postRun()
                   assert meaningOfLife == 42
                   meaningOfLife
                }
            }

            @groovy.transform.BaseScript Custom self

            meaningOfLife |= 32
            assert meaningOfLife == 34
        ''')

        assert answer == 42
    }

    void testBaseScriptImplementsRunMethod() {
        def result = new GroovyShell().evaluate('''
            class Custom extends Script {
                boolean iBeenRun
                def run() { iBeenRun = true }
            }

            @groovy.transform.BaseScript Custom self

            assert !iBeenRun

            super.run()

            assert iBeenRun

            iBeenRun
        ''')

        assert result
    }

    void testBaseScriptCanImplementRunMethodWithArgs() {
        assertScript '''
            abstract class Custom extends Script {
                def run() { run(null) }
                abstract run(Object x)
            }

            @groovy.transform.BaseScript Custom self

            println "hello world"
        '''
    }

    void testScriptCanOverrideRun() {
        assertScript '''
            abstract class Custom extends Script {
                def depth = 3
                def run() { myRun() }
                abstract myRun()
            }

            def run() {
                while (depth-- > 0) {
                    println "Going super"
                    super.run()
                }
            }

            @groovy.transform.BaseScript Custom self

            println "hello world"
        '''
    }

    void testScriptCanOverrideRunButNotIfFinal() {
        shouldNotCompile '''
            abstract class Custom extends Script {
                def depth = 3
                final def run() { myRun() }
                abstract myRun()
            }

            def run() {
                while (depth-- > 0) {
                    println "Going super"
                    super.run()
                }
            }

            @groovy.transform.BaseScript Custom self

            println "hello world"
        '''
    }

    void testBaseScriptOnImport() {
        def result = new GroovyShell().evaluate('''
            @BaseScript(Custom)
            import groovy.transform.BaseScript

            class Custom extends Script {
                boolean iBeenRun
                def run() { iBeenRun = true }
            }

            assert !iBeenRun

            super.run()

            assert iBeenRun

            iBeenRun
        ''')

        assert result
    }

    // GROOVY-6706
    void testBaseScriptOnImport2() {
        assertScript '''
            @BaseScript(Custom)
            import groovy.transform.BaseScript

            assert did_before
            assert !did_after

            42

            abstract class Custom extends Script {
                boolean did_before = false
                boolean did_after = false

                def run() {
                    before()
                    def r = internalRun()
                    after()
                    assert r == 42
                }

                abstract internalRun()

                def before() { did_before = true }
                def after()  { did_after = true  }
            }
        '''
    }

    void testBaseScriptOnPackage() {
        def result = new GroovyShell().evaluate('''
            @BaseScript(Custom)
            package foo

            import groovy.transform.BaseScript

            class Custom extends Script {
                boolean iBeenRun
                def run() { iBeenRun = true }
            }

            assert !iBeenRun

            super.run()

            assert iBeenRun

            iBeenRun
        ''')

        assert result
    }

    // GROOVY-6586
    void testBaseScriptVsBinding() {
        assertScript '''
            abstract class Custom extends Script {
                private int _something = 1
                int getSomething() { _something }
                void setSomething(int i) { _something = i }
            }

            @groovy.transform.BaseScript Custom self

            assert binding.variables.size() == 0
            assert something == 1
            assert binding.variables.size() == 0
            something = 2
            assert binding.variables.size() == 0
            assert something == 2
        '''
    }

    void testShouldNotAllowClassMemberIfUsedOnADeclaration() {
        shouldNotCompile '''import groovy.transform.BaseScript

            @BaseScript(Script) Script foo
            println 'ok'
        '''
    }

    void testShouldNotAllowClassMemberIsNotClassLiteral() {
        shouldNotCompile '''
            @BaseScript('Script')
            import groovy.transform.BaseScript
            println 'ok'
        '''
    }

    void testShouldNotAllowBaseScriptOnMultipleAssignment() {
        shouldNotCompile '''import groovy.transform.BaseScript

            @BaseScript def (Script a, Script b) = [null,null]
            println 'ok'
        '''
    }

    void testShouldNotAllowBaseScriptOnVariableAssignment() {
        shouldNotCompile '''import groovy.transform.BaseScript

            @BaseScript a = null
            println 'ok'
        '''
    }
}
