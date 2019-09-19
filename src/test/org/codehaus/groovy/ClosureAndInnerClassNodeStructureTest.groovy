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
package org.codehaus.groovy

import groovy.test.GroovyTestCase
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilationUnit.PrimaryClassNodeOperation
import org.codehaus.groovy.classgen.GeneratorContext
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.control.Phases
import org.codehaus.groovy.ast.ClassNode

/**
 * Before Groovy 1.8, the structure of closure's inner classes
 * was a bit different than it is now in 1.8+.
 *
 * This test checks that closure inner classes are direct child of their enclosing class,
 * instead of being child of the outermost class.
 */
class ClosureAndInnerClassNodeStructureTest extends GroovyTestCase {

    void testStructure() {
        def cu = new CompilationUnit()
        cu.addSource("t.groovy", '''
            exec {                               // t$_run_closure1
                def d = {                        // t$_run_closure1$_closure3
                    def o = new Object() {       // t$1
                        void run() {             //
                            def f = {}           // t$1$_run_closure1
                        }                        //
                    }                            //
                    def e = {}                   // t$_run_closure1$_closure3$_closure4
                }                                //
            }                                    //
            def g = {}                           // t$_run_closure2
        ''')

        def classNodes = [:]

        cu.addPhaseOperation(new PrimaryClassNodeOperation() {
            void call(SourceUnit source, GeneratorContext context, ClassNode cn) {
                def recurse = { ClassNode node ->
                    classNodes[node.name] = node
                    for (icn in node.innerClasses) {
                        classNodes[icn.name] == icn
                        call(icn)
                    }
                }
                recurse(cn)
            }
        }, Phases.CLASS_GENERATION)

        cu.compile(Phases.CLASS_GENERATION)

        def assertParentOf = { String child ->
            [isClass: { String parent ->
                assert classNodes[child].outerClass.name == parent
            }]
        }

        assertParentOf 't$1'                                 isClass 't'
        assertParentOf 't$1$_run_closure1'                   isClass 't$1'
        assertParentOf 't$_run_closure1'                     isClass 't'
        assertParentOf 't$_run_closure2'                     isClass 't'
        assertParentOf 't$_run_closure1$_closure3'           isClass 't$_run_closure1'
        assertParentOf 't$_run_closure1$_closure3$_closure4' isClass 't$_run_closure1$_closure3'
    }

    // GROOVY-5351
    void testGetSimpleName() {
        assertScript '''
            class X {
                static class Y {
                    def foo() {
                        def cl = {return{}}
                        def cl2 = cl()
                        [cl.getClass().getSimpleName(), cl2.getClass().getSimpleName()]
                    }
                }
            }
            def simpleNames = new X.Y().foo()
            assert simpleNames == ['_foo_closure1', '_closure2']
        '''
    }

    //GROOVY-7119 && GROOVY-7120
    void testIrregularMethodName() {
        assertScript '''
            class X {
                def 'foo!bar'() {
                    return {}
                }
            }
            def str = new X().'foo!bar'().getClass().getName()
            assert str == 'X$_foo_bar_closure1'
        '''
    }
}
