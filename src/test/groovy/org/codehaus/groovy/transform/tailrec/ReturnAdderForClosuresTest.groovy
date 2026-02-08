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
package org.codehaus.groovy.transform.tailrec

import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.builder.AstAssert
import org.codehaus.groovy.ast.builder.AstBuilder
import org.codehaus.groovy.control.CompilePhase
import org.junit.Before
import org.junit.Test

class ReturnAdderForClosuresTest {

    ReturnAdderForClosures adder

    @Before
    void init() {
        adder = new ReturnAdderForClosures()
    }

    @Test
    void returnIsAddToRecursiveCallEmbeddedInClosure() throws Exception {
        MethodNode method = new AstBuilder().buildFromString(CompilePhase.SEMANTIC_ANALYSIS, true, '''
            class Target {
                int myMethod(int n) {
                    def next = { r1 ->
                        myMethod(n - 2)
                    }
                    return next()
                }
            }
		''')[1].getMethods('myMethod')[0]

        MethodNode methodExpected = new AstBuilder().buildFromString(CompilePhase.SEMANTIC_ANALYSIS, true, '''
            class Target {
                int myMethod(int n) {
                    def next = { r1 ->
                        return myMethod(n - 2)
                    }
                    return next()
                }
            }
		''')[1].getMethods('myMethod')[0]

        adder.visitMethod(method)

        AstAssert.assertSyntaxTree([methodExpected], [method])
    }

}
