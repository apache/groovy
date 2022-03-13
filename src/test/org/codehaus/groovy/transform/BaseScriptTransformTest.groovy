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
import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.junit.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

/**
 * Tests for the {@code @BaseScript} AST transform.
 */
final class BaseScriptTransformTest {

    private final GroovyShell shell = new GroovyShell(new CompilerConfiguration().
        addCompilationCustomizers(new ImportCustomizer().tap { addImports('groovy.transform.BaseScript') })
    )

    @Test
    void testInheritsFromCustomScript() {
        assertScript shell, '''
            abstract class Custom extends Script {
            }

            @BaseScript Custom self

            assert this.class.superclass == Custom
        '''
    }

    @Test
    void testBaseScriptMustExtendsScript() {
        shouldFail shell, '''
            abstract class Custom {
            }

            @BaseScript Custom self
        '''
    }

    @Test
    void testThisObjectIsAssignedToBaseScriptVariable() {
        assertScript shell, '''
            abstract class Custom extends Script {
            }

            @BaseScript Custom self

            assert this == self
        '''
    }

    @Test
    void testNotAllowedForClassFields() {
        shouldFail shell, '''
            abstract class Custom extends Script {
            }

            class Inner {
                @BaseScript Custom nope
            }
        '''
    }

    @Test
    void testNotAllowedForScriptInnerClassFields() {
        shouldFail shell, '''
            abstract class Custom extends Script {
            }

            class Inner {
                @BaseScript Custom nope
            }

            println Inner.class.name
        '''
    }

    @Test
    void testNotAllowedInClassMethods() {
        shouldFail shell, '''
            abstract class Custom extends Script {
            }

            class Inner {
                void test() {
                    @BaseScript Custom nope
                }
            }
        '''
    }

    @Test
    void testNotAllowedInScriptInnerClassMethods() {
        shouldFail shell, '''
            abstract class Custom extends Script {
            }

            class Inner {
                void test() {
                    @BaseScript Custom nope
                }
            }

            println Inner.class.name
        '''
    }

    abstract class MyCustomScript extends Script {}

    @Test
    void testBaseScriptFromCompiler() {
        shell.config.scriptBaseClass = MyCustomScript.name
        shell.evaluate '''
            abstract class Custom extends Script {
                int meaningOfLife = 42
            }

            @BaseScript Custom self

            assert meaningOfLife == 42
        '''
    }

    @Test // GROOVY-6585
    void testBaseScriptAbstractMethod() {
        def answer = shell.evaluate '''
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

            @BaseScript Custom self

            meaningOfLife |= 32
            assert meaningOfLife == 34
        '''
        assert answer == 42
    }

    @Test
    void testBaseScriptImplementsRunMethod() {
        def result = shell.evaluate '''
            class Custom extends Script {
                boolean iBeenRun
                def run() { iBeenRun = true }
            }

            @BaseScript Custom self

            assert !iBeenRun

            super.run()

            assert iBeenRun

            iBeenRun
        '''
        assert result
    }

    @Test
    void testBaseScriptCanImplementRunMethodWithArgs() {
        assertScript shell, '''
            abstract class Custom extends Script {
                def run() { run(null) }
                abstract run(Object x)
            }

            @BaseScript Custom self

            println "hello world"
        '''
    }

    @Test
    void testScriptCanOverrideRun() {
        assertScript shell, '''
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

            @BaseScript Custom self

            println "hello world"
        '''
    }

    @Test
    void testScriptCanOverrideRunButNotIfFinal() {
        shouldFail shell, '''
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

            @BaseScript Custom self

            println "hello world"
        '''
    }

    @Test
    void testBaseScriptOnImport() {
        assertScript '''
            @BaseScript(Custom)
            import groovy.transform.BaseScript

            class Custom extends Script {
                boolean iBeenRun
                def run() { iBeenRun = true }
            }

            assert !iBeenRun

            super.run()

            assert iBeenRun
        '''
    }

    @Test // GROOVY-6706
    void testBaseScriptOnImport2() {
        assertScript '''
            @BaseScript(Custom)
            import groovy.transform.BaseScript

            assert did_before
            assert !did_after

            return 42

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

    @Test
    void testBaseScriptOnPackage() {
        assertScript shell, '''
            @BaseScript(Custom)
            package foo

            class Custom extends Script {
                boolean iBeenRun
                def run() { iBeenRun = true }
            }

            assert !iBeenRun

            super.run()

            assert iBeenRun
        '''
    }

    @Test // GROOVY-6586
    void testBaseScriptVsBinding() {
        assertScript shell, '''
            abstract class Custom extends Script {
                private int _something = 1
                int getSomething() { _something }
                void setSomething(int i) { _something = i }
            }

            @BaseScript Custom self

            assert binding.variables.size() == 0
            assert something == 1
            assert binding.variables.size() == 0
            something = 2
            assert binding.variables.size() == 0
            assert something == 2
        '''
    }

    @Test
    void testShouldNotAllowClassMemberIfUsedOnADeclaration() {
        shouldFail shell, '''
            @BaseScript(Script) Script foo
            println 'ok'
        '''
    }

    @Test
    void testShouldNotAllowClassMemberIsNotClassLiteral() {
        shouldFail '''
            @BaseScript('Script')
            import groovy.transform.BaseScript
            println 'ok'
        '''
    }

    @Test
    void testShouldNotAllowBaseScriptOnMultipleAssignment() {
        shouldFail shell, '''
            @BaseScript def (Script a, Script b) = [null,null]
            println 'ok'
        '''
    }

    @Test
    void testShouldNotAllowBaseScriptOnVariableAssignment() {
        shouldFail shell, '''
            @BaseScript a = null
            println 'ok'
        '''
    }
}
