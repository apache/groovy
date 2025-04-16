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

import org.codehaus.groovy.ast.builder.AstAssert
import org.codehaus.groovy.ast.builder.AstBuilder
import org.codehaus.groovy.ast.stmt.IfStatement
import org.codehaus.groovy.ast.stmt.ReturnStatement
import org.junit.Test

class TernaryToIfStatementConverterTest {

    @Test
    void simpleTernary() {
        ReturnStatement statement = new AstBuilder().buildFromSpec {
            returnStatement {
                ternary {
                    booleanExpression {
                        constant true
                    }
                    constant 1
                    constant 2
                }
            }
        }[0]

        IfStatement expected = new AstBuilder().buildFromSpec {
            ifStatement {
                booleanExpression {
                    constant true
                }
                returnStatement {
                    constant 1
                }
                returnStatement {
                    constant 2
                }
            }
        }[0]

        def ifStatement = new TernaryToIfStatementConverter().convert(statement)

        AstAssert.assertSyntaxTree([expected], [ifStatement])
    }

}
