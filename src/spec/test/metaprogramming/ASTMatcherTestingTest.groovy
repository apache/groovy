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
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.expr.BinaryExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.macro.matcher.ASTMatcher
import org.codehaus.groovy.macro.transform.MacroClass
import org.codehaus.groovy.transform.AbstractASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformationClass

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

import static org.codehaus.groovy.ast.ClassHelper.Integer_TYPE
import static org.codehaus.groovy.ast.tools.GeneralUtils.*

class ASTMatcherTestingTest extends GroovyTestCase {

    // tag::testexpression[]
    void testTestingSumExpression() {
        use(ASTMatcher) {                 // <1>
            TwiceASTTransformation sample = new TwiceASTTransformation()
            Expression referenceNode = macro {
                a + a                     // <2>
            }.withConstraints {           // <3>
                placeholder 'a'           // <4>
            }

            assert sample
                .sumExpression
                .matches(referenceNode)   // <5>
        }
    }
    // end::testexpression[]

    // tag::executiontesting[]
    void testASTBehavior() {
        assertScript '''
        package metaprogramming

        @Twice
        class AAA {

        }

        assert new AAA().giveMeTwo(1) == 2
        '''
    }
    // end::executiontesting[]
}

@Retention(RetentionPolicy.SOURCE)
@Target([ElementType.TYPE])
@GroovyASTTransformationClass(["metaprogramming.TwiceASTTransformation"])
@interface Twice { }

// tag::twiceasttransformation[]
@GroovyASTTransformation(phase = CompilePhase.INSTRUCTION_SELECTION)
class TwiceASTTransformation extends AbstractASTTransformation {

    static final String VAR_X = 'x'

    @Override
    void visit(ASTNode[] nodes, SourceUnit source) {
        ClassNode classNode = (ClassNode) nodes[1]
        MethodNode giveMeTwo = getTemplateClass(sumExpression)
            .getDeclaredMethods('giveMeTwo')
            .first()

        classNode.addMethod(giveMeTwo)                  // <1>
    }

    BinaryExpression getSumExpression() {               // <2>
        return macro {
            $v{ varX(VAR_X) } +
            $v{ varX(VAR_X) }
        }
    }

    ClassNode getTemplateClass(Expression expression) { // <3>
        return new MacroClass() {
            class Template {
                java.lang.Integer giveMeTwo(java.lang.Integer x) {
                    return $v { expression }
                }
            }
        }
    }
}
// end::twiceasttransformation[]
