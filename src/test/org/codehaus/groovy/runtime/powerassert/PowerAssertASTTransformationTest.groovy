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
package org.codehaus.groovy.runtime.powerassert

class PowerAssertASTTransformationTest extends GroovyShellTestCase {

    void testAddPowerAssertWithoutProperSourcePosition() {

        def gcl = new GroovyClassLoader()
        gcl.parseClass """
            package org.codehaus.groovy.transform

            import java.lang.annotation.Retention
            import java.lang.annotation.RetentionPolicy
            import java.lang.annotation.Target
            import java.lang.annotation.ElementType

            @Retention(RetentionPolicy.RUNTIME)
            @Target([ElementType.METHOD])
            @GroovyASTTransformationClass("org.codehaus.groovy.transform.LocalTransformASTTransformation")
            public @interface LocalTransform {}
        """

        gcl.parseClass """
            package org.codehaus.groovy.transform

            import org.codehaus.groovy.control.CompilePhase
            import org.codehaus.groovy.ast.*
            import org.codehaus.groovy.ast.stmt.*
            import org.codehaus.groovy.ast.expr.*
            import org.codehaus.groovy.syntax.*

            @GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
            public class LocalTransformASTTransformation extends AbstractASTTransformation {
                void visit(org.codehaus.groovy.ast.ASTNode[] nodes, org.codehaus.groovy.control.SourceUnit source) {
                    MethodNode method = (MethodNode) nodes[1]
                    BlockStatement block = (BlockStatement) method.code

                    // we need to reference a variable in the Boolean expression -> causes a call to AssertionWriter#record
                    block.addStatement(new AssertStatement(new BooleanExpression(new BinaryExpression(new VariableExpression("i"), Token.newSymbol(Types.COMPARE_EQUAL, 0, 0), new ConstantExpression("42")))))
                    block.addStatement(new ReturnStatement(ConstantExpression.FALSE))
                }
            }
        """

        gcl.parseClass """
            import org.codehaus.groovy.transform.*

            class Test {

            @LocalTransform def test() { def i = 42 }

            }
        """
    }

    void testAddPowerAssertWitProperSourcePosition() {

        def gcl = new GroovyClassLoader()
        gcl.parseClass """
            package org.codehaus.groovy.transform

            import java.lang.annotation.Retention
            import java.lang.annotation.RetentionPolicy
            import java.lang.annotation.Target
            import java.lang.annotation.ElementType

            @Retention(RetentionPolicy.RUNTIME)
            @Target([ElementType.METHOD])
            @GroovyASTTransformationClass("org.codehaus.groovy.transform.LocalTransformASTTransformation")
            public @interface LocalTransform {}
        """

        gcl.parseClass """
            package org.codehaus.groovy.transform

            import org.codehaus.groovy.control.CompilePhase
            import org.codehaus.groovy.ast.*
            import org.codehaus.groovy.ast.stmt.*
            import org.codehaus.groovy.ast.expr.*
            import org.codehaus.groovy.syntax.*

            @GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
            public class LocalTransformASTTransformation extends AbstractASTTransformation {
                void visit(org.codehaus.groovy.ast.ASTNode[] nodes, org.codehaus.groovy.control.SourceUnit source) {
                    MethodNode method = (MethodNode) nodes[1]
                    BlockStatement block = (BlockStatement) method.code

                    // we need to reference a variable in the Boolean expression -> causes a call to AssertionWriter#record
                    def assertStatement = new AssertStatement(new BooleanExpression(new BinaryExpression(new VariableExpression("i"), Token.newSymbol(Types.COMPARE_EQUAL, 0, 0), new ConstantExpression("42"))))
                    assertStatement.setSourcePosition(method)

                    block.addStatement(assertStatement)
                    block.addStatement(new ReturnStatement(ConstantExpression.FALSE))
                }
            }
        """

        gcl.parseClass """
            import org.codehaus.groovy.transform.*

            class Test {

            @LocalTransform def test() { def i = 42 }

            }
        """
    }
}
