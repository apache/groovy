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

import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.builder.AstAssert
import org.codehaus.groovy.ast.builder.AstBuilder
import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.ReturnStatement
import org.junit.Test

class ReturnStatementToIterationConverterTest {

    @Test
    void oneConstantParameter() {
        ReturnStatement statement = new AstBuilder().buildFromSpec {
            returnStatement {
                methodCall {
                    variable "this"
                    constant "myMethod"
                    argumentList { constant 1 }
                }
            }
        }[0]
        statement.statementLabel = "aLabel"

        BlockStatement expected = new AstBuilder().buildFromSpec {
            block {
                expression {
                    binary {
                        variable '_a_'
                        token '='
                        constant 1
                    }
                }
                continueStatement {
                    label InWhileLoopWrapper.LOOP_LABEL
                }
            }
        }[0]

        Map positionMapping = [0: [name: '_a_', type: ClassHelper.DYNAMIC_TYPE]]
        def block = new ReturnStatementToIterationConverter().convert(statement, positionMapping)

        AstAssert.assertSyntaxTree([expected], [block])
        assert block.statementLabels[0] == "aLabel"
    }

    @Test
    void twoParametersOnlyOneUsedInRecursiveCall() {

        BlockStatement expected = new AstBuilder().buildFromSpec {
            block {
                expression {
                    declaration {
                        variable '__a__'
                        token '='
                        variable '_a_'
                    }
                }
                expression {
                    binary {
                        variable '_a_'
                        token '='
                        constant 1
                    }
                }
                expression {
                    binary {
                        variable '_b_'
                        token '='
                        binary {
                            variable '__a__'
                            token '+'
                            constant 1
                        }
                    }
                }
                continueStatement {
                    label InWhileLoopWrapper.LOOP_LABEL
                }
            }
        }[0]

        ReturnStatement statement = new AstBuilder().buildFromString("""
				return(myMethod(1, _a_ + 1))
		""")[0].statements[0]

        Map positionMapping = [0: [name: '_a_', type: ClassHelper.DYNAMIC_TYPE], 1: [name: '_b_', type: ClassHelper.DYNAMIC_TYPE]]
        def block = new ReturnStatementToIterationConverter().convert(statement, positionMapping)

        AstAssert.assertSyntaxTree([expected], [block])
    }

    @Test
    void twoParametersBothUsedInRecursiveCall() {
        BlockStatement expected = new AstBuilder().buildFromSpec {
            block {
                expression {
                    declaration {
                        variable '__a__'
                        token '='
                        variable '_a_'
                    }
                }
                expression {
                    declaration {
                        variable '__b__'
                        token '='
                        variable '_b_'
                    }
                }
                expression {
                    binary {
                        variable '_a_'
                        token '='
                        binary {
                            variable '__a__'
                            token '+'
                            constant 1
                        }
                    }
                }
                expression {
                    binary {
                        variable '_b_'
                        token '='
                        binary {
                            variable '__b__'
                            token '+'
                            variable '__a__'
                        }
                    }
                }
                continueStatement {
                    label InWhileLoopWrapper.LOOP_LABEL
                }
            }
        }[0]

        ReturnStatement statement = new AstBuilder().buildFromString("""
		return(myMethod(_a_ + 1, _b_ + _a_))
				""")[0].statements[0]


        Map positionMapping = [0: [name: '_a_', type: ClassHelper.DYNAMIC_TYPE], 1: [name: '_b_', type: ClassHelper.DYNAMIC_TYPE]]
        def block = new ReturnStatementToIterationConverter().convert(statement, positionMapping)

        AstAssert.assertSyntaxTree([expected], [block])
    }

    @Test
    void worksWithStaticMethods() {
        ReturnStatement statement = new ReturnStatement(new StaticMethodCallExpression(
                ClassHelper.make(Math, false), "min",
                new ArgumentListExpression(new ConstantExpression(1))))

        BlockStatement expected = new AstBuilder().buildFromSpec {
            block {
                expression {
                    binary {
                        variable '_a_'
                        token '='
                        constant 1
                    }
                }
                continueStatement {
                    label InWhileLoopWrapper.LOOP_LABEL
                }
            }
        }[0]

        Map positionMapping = [0: [name: '_a_', type: ClassHelper.DYNAMIC_TYPE]]
        def block = new ReturnStatementToIterationConverter().convert(statement, positionMapping)

        AstAssert.assertSyntaxTree([expected], [block])
    }
}
