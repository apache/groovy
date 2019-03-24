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

import groovy.transform.CompileStatic
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.ClassCodeExpressionTransformer
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.macro.matcher.ASTMatcher
import org.codehaus.groovy.transform.AbstractASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformationClass

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

class ASTMatcherFilteringTest extends GroovyTestCase {

    void testFilteringNodes() {
        assertScript '''
        // tag::jokingexample[]
        package metaprogramming

        class Something {
            @Joking
            Integer getResult() {
                return 1 + 1
            }
        }

        assert new Something().result == 3
        // end::jokingexample[]
        '''
    }
}

// tag::jokingannotation[]
@Retention(RetentionPolicy.SOURCE)
@Target([ElementType.METHOD])
@GroovyASTTransformationClass(["metaprogramming.JokingASTTransformation"])
@interface Joking { }
// end::jokingannotation[]

// tag::jokingtransformation[]
@CompileStatic
@GroovyASTTransformation(phase = CompilePhase.INSTRUCTION_SELECTION)
class JokingASTTransformation extends AbstractASTTransformation {
    @Override
    void visit(ASTNode[] nodes, SourceUnit source) {
        MethodNode methodNode = (MethodNode) nodes[1]

        methodNode
            .getCode()
            .accept(new ConvertOnePlusOneToThree(source))  // <1>
    }
}
// end::jokingtransformation[]

// tag::jokingexpressiontransformer[]
class ConvertOnePlusOneToThree extends ClassCodeExpressionTransformer {
    SourceUnit sourceUnit

    ConvertOnePlusOneToThree(SourceUnit sourceUnit) {
        this.sourceUnit = sourceUnit
    }

    @Override
    Expression transform(Expression exp) {
        Expression ref = macro { 1 + 1 }     // <1>

        if (ASTMatcher.matches(ref, exp)) {  // <2>
            return macro { 3 }               // <3>
        }

        return super.transform(exp)
    }
}
// end::jokingexpressiontransformer[]
