/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package groovy.bugs

import groovy.transform.CompileStatic
import org.junit.Test

import static groovy.test.GroovyAssert.assertScript

@CompileStatic
final class Groovy8002 {

    @Test
    void testSourcePositionOfMethodInChainAssignmentCS() {
        assertScript '''
            import groovy.transform.*
            import static org.codehaus.groovy.control.CompilePhase.*

            class B {
                private String z
                void setZero(String zero) { z = zero }
            }

            @CompileStatic
            class C {
                String x
                B b = new B()
                @ASTTest(phase=CLASS_GENERATION, value={
                    def expr = node.code.statements[-1].expression.rightExpression.@call
                    assert expr.class.simpleName == 'PoppingMethodCallExpression'
                    assert expr.lineNumber > 0 && expr.columnNumber > 0

                    def zero = expr.method // "zero" in "x = b.zero = 'X'"
                    assert zero.lineNumber > 0
                    assert zero.columnNumber > 0
                    assert zero.lastLineNumber == zero.lineNumber
                    assert zero.lastColumnNumber == zero.columnNumber + 4
                })
                C() {
                    x = b.zero = 'X'
                }
            }

            def c = new C()
            assert c.x == 'X'
            assert c.b.@z == 'X'
        '''
    }
}
