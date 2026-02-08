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
import org.junit.Test

import static org.objectweb.asm.Opcodes.ACC_PUBLIC

class InWhileLoopWrapperTest {

	InWhileLoopWrapper wrapper = new InWhileLoopWrapper()

	@Test
	void wrapWholeMethodBody() throws Exception {
		MethodNode methodToWrap = new AstBuilder().buildFromSpec {
			method('myMethod', ACC_PUBLIC, int.class) {
				parameters {}
				exceptions {}
				block { returnStatement{ constant 2 } }
			}
		}[0]
		
		MethodNode expectedWrap = new AstBuilder().buildFromSpec {
			method('myMethod', ACC_PUBLIC, int.class) {
				parameters {}
				exceptions {}
				block {
					whileStatement {
						booleanExpression { constant true }
                        block {
                            tryCatch {
                                block {
                                    returnStatement{  constant 2 }
                                }
                                empty() //finally block
                                catchStatement {
                                    parameter 'ignore': GotoRecurHereException.class
                                    //block {
                                        continueStatement {
                                            label InWhileLoopWrapper.LOOP_LABEL
                                        }
                                    //}
                                }
                            }
                        }
					}
				}
			}
		}[0]
		
		wrapper.wrap(methodToWrap)
		AstAssert.assertSyntaxTree([expectedWrap], [methodToWrap])
		assert methodToWrap.code.statements[0].loopBlock.statements[0].statementLabel == InWhileLoopWrapper.LOOP_LABEL
	}
}
