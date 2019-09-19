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

import groovy.test.GroovyTestCase

/**
 * StatementWriter.writeTryCatchFinally visits the finally block
 * twice, once for the normal path and once again for the "catch all"
 * path. When the OptimizingStatementWriter is used DeclarationExpressions
 * are rewritten to BinaryExpressions to allow splitting between fast and
 * slow paths.  Because the expression is modified variable declarations
 * are lost if the statement is visited more than once.
 *
 * This is not a problem for scripts because the property call that is
 * generated will succeed because of the script context.  So to reproduce
 * the issue it must be contained in a class.
 */
class Groovy7248Bug extends GroovyTestCase {

    void testFinallyDeclaredVariableExpression() {
        assertScript '''
            class Test {
                long run() {
                    long dur = 0
                    long start = 0
                    try {
                        start++
                    } finally {
                        long end = 2
                        long time = end - start
                        dur = time
                    }
                    dur
                }
            }
            assert new Test().run() == 1
        '''
    }

    void testReturnStatementDeclaration() {
        assertScript '''
            class Foo {
                int test() {
                    int x = 2
                    int y = x - 1
                }
            }
            assert new Foo().test() == 1
        '''
    }

}
