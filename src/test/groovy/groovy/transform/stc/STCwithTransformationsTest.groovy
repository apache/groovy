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
package groovy.transform.stc

import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer
import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation
import org.junit.jupiter.api.Test

/**
 * Unit tests for static type checking and AST.
 */
final class STCwithTransformationsTest extends StaticTypeCheckingTestCase {

    @Override
    protected void configure() {
        config.addCompilationCustomizers(
                new ASTTransformationCustomizer(new TestTransformation())
        )
    }

    @GroovyASTTransformation
    private static class TestTransformation implements ASTTransformation {

        @Override
        void visit(ASTNode[] nodes, SourceUnit source) {
            def initExpr = new ConstantExpression(5L)
            source.AST.classes[1]?.addProperty('j', 1, new ClassNode(Long), initExpr, null, null)
        }
    }

    @Test
    void testShouldFailWithDynamicVariable() {
        shouldFailWithMessages '''
            class Test{
                long test() {
                    i + 6
                }
            }

            new Test().test()
        ''',
        'The variable [i] is undeclared'
    }

    @Test
    void testCheckedInjectedProperty() {
        def result = assertScript '''
            class Test {
                long test() {
                    j + 6
                }
            }

            new Test().test()
        '''
        assert result == 11
    }
}
