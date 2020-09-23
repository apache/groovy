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
package groovy.bugs

import org.junit.Test

import static groovy.test.GroovyAssert.assertScript

final class Groovy9691 {

    @Test
    void testMainCallInClosure() {
        assertScript '''
            import org.codehaus.groovy.ast.expr.*

            void sourceSets(Closure block) {
                // no-op
            }

            sourceSets {
                @groovy.transform.ASTTest(phase=SEMANTIC_ANALYSIS, value={
                    def call = node.rightExpression
                    assert call instanceof MethodCall
                    assert !(call instanceof StaticMethodCallExpression)
                })
                x = main {
                    java { srcDirs = [] }
                    groovy { srcDirs = ['src/main'] }
                }
                test {
                    java { srcDirs = [] }
                    groovy { srcDirs = ['src/test'] }
                }
            }
        '''
    }
}
