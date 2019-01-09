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

import gls.CompilableTestSupport

class BaseScriptTransformTest extends CompilableTestSupport {

    void testInheritsFromCustomScript() {
        assertScript """
            abstract class CustomScript extends Script {}
  
            @groovy.transform.BaseScript CustomScript baseScript
            assert this.class.superclass == CustomScript
        """
    }

    void testBaseScriptMustExtendsScript() {
        shouldNotCompile """
            abstract class CustomScript {}
  
            @groovy.transform.BaseScript CustomScript baseScript
        """
    }

    void testThisObjectIsAssignedToBaseScriptVariable() {
        assertScript """
            abstract class CustomScript extends Script {}
  
            @groovy.transform.BaseScript CustomScript baseScript
            assert this == baseScript
        """
    }

    void testNotAllowedForClassFields() {
        shouldNotCompile """
            import groovy.transform.*

            abstract class CustomScript extends Script {}

            class Inner {
                @BaseScript CustomScript baseScript
            }
        """
    }

    void testNotAllowedForScriptInnerClassFields() {
        shouldNotCompile """
            import groovy.transform.*

            abstract class CustomScript extends Script {}

            class Inner {
                @BaseScript CustomScript baseScript
            }
            println Inner.class.name
        """
    }

    void testNotAllowedInClassMethods() {
        shouldNotCompile """
            import groovy.transform.*

            abstract class CustomScript extends Script {}

            class Inner {
                def bar() {
                    @BaseScript CustomScript baseScript
                }
            }
        """
    }

    void testNotAllowedInScriptInnerClassMethods() {
        shouldNotCompile """
            import groovy.transform.*

            abstract class CustomScript extends Script {}

            class Inner {
                def bar() {
                    @BaseScript CustomScript baseScript
                }
            }
            println Inner.class.name
        """
    }

    abstract class MyCustomScript extends Script {}

    void testBaseScriptFromCompiler(){
        CompilerConfiguration config = new CompilerConfiguration()
        config.scriptBaseClass = MyCustomScript.name
        GroovyShell shell = new GroovyShell(config)
        
        shell.evaluate('''
            abstract class DeclaredBaseScript extends Script {
                int meaningOfLife = 42
            }
        
            @groovy.transform.BaseScript DeclaredBaseScript baseScript

            assert meaningOfLife == 42
        ''')
    }

    void testBaseScriptAbstractMethod() {
        // https://issues.apache.org/jira/browse/GROOVY-6585
        CompilerConfiguration config = new CompilerConfiguration()
        config.scriptBaseClass = MyCustomScript.name
        GroovyShell shell = new GroovyShell(config)

        def answer = shell.evaluate('''
            abstract class DeclaredBaseScript extends Script {
                int _meaningOfLife = 0
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

            @groovy.transform.BaseScript DeclaredBaseScript baseScript

            meaningOfLife |= 32
            assert meaningOfLife == 34
        ''')

        assert answer == 42
    }

    void testBaseScriptImplementsRunMethod() {
        def result = new GroovyShell().evaluate('''
            class DeclaredBaseScript extends Script {
                boolean iBeenRun
                def run() { iBeenRun = true }
            }

            @groovy.transform.BaseScript DeclaredBaseScript baseScript

            assert !iBeenRun

            super.run()

            assert iBeenRun

            iBeenRun
        ''')

        assert result
    }

    void testBaseScriptCanImplementRunMethodWithArgs() {
        assertScript '''
            abstract class  Foo extends Script {
               def run() {run(null)}
              abstract run(Object x)
            }

            @groovy.transform.BaseScript Foo foo
            println "hello world"
        '''
    }

    void testScriptCanOverrideRun() {
        assertScript '''
            abstract class Foo extends Script {
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

            @groovy.transform.BaseScript Foo foo
            println "hello world"
        '''
    }

    void testScriptCanOverrideRunButNotIfFinal() {
        shouldNotCompile '''
            abstract class Foo extends Script {
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

            @groovy.transform.BaseScript Foo foo
            println "hello world"
        '''
    }

    void testMultipleMethodsWithSameSignatureFails() {
        shouldNotCompile '''
            def run() { println 'hmm' }
            println 'huh?'
        '''
    }

    void testBaseScriptOnImport() {
        def result = new GroovyShell().evaluate('''
            @BaseScript(DeclaredBaseScript)
            import groovy.transform.BaseScript

            class DeclaredBaseScript extends Script {
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

    /**
     * Test GROOVY-6706.  Base script in import (or package) with a SAM.
     */
    void testGROOVY_6706() {
        assertScript '''
@BaseScript(CustomBase)
import groovy.transform.BaseScript

assert did_before
assert !did_after

42

abstract class CustomBase extends Script {
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
}'''
    }

    void testBaseScriptOnPackage() {
        def result = new GroovyShell().evaluate('''
            @BaseScript(DeclaredBaseScript)
            package foo

            import groovy.transform.BaseScript

            class DeclaredBaseScript extends Script {
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
