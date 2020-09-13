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

package metaprogramming

import groovy.test.GroovyTestCase
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.expr.BinaryExpression
import org.codehaus.groovy.ast.stmt.ReturnStatement
import org.codehaus.groovy.ast.tools.GeneralUtils
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.AbstractASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformationClass

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

class MacroExpressionTest extends GroovyTestCase {

    void testCreateExpressions() {
        assertScript '''
        // add::addgettwosample[]
        package metaprogramming

        @AddGetTwo
        class A {

        }

        assert new A().getTwo() == 2
        // tag::addgettwosample[]
        '''
    }
}

// tag::addgettwoannotation[]
@Retention(RetentionPolicy.SOURCE)
@Target([ElementType.TYPE])
@GroovyASTTransformationClass(["metaprogramming.AddGetTwoASTTransformation"])
@interface AddGetTwo { }
// end::addgettwoannotation[]

import static org.objectweb.asm.Opcodes.ACC_PUBLIC

// tag::addgettwotransformation[]
@GroovyASTTransformation(phase = CompilePhase.INSTRUCTION_SELECTION)
class AddGetTwoASTTransformation extends AbstractASTTransformation {

    BinaryExpression onePlusOne() {
        return macro(false) { 1 + 1 }                                      // <1>
    }

    @Override
    void visit(ASTNode[] nodes, SourceUnit source) {
        ClassNode classNode = nodes[1]
        BinaryExpression expression = onePlusOne()                         // <2>
        ReturnStatement returnStatement = GeneralUtils.returnS(expression) // <3>

        MethodNode methodNode =
                new MethodNode("getTwo",
                        ACC_PUBLIC,
                        ClassHelper.Integer_TYPE,
                        [] as Parameter[],
                        [] as ClassNode[],
                        returnStatement                                    // <4>
                )

        classNode.addMethod(methodNode)                                    // <5>
    }
}
// end::addgettwotransformation[]
