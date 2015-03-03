/*
 * Copyright 2003-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package metaprogramming

class ASTXFormSpecTest extends GroovyTestCase {
    void testIntro() {
        def gcl = new GroovyClassLoader()
        def gse = new GroovyShell(gcl)

        gse.parse '''package gep

            import groovy.transform.CompileStatic
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation

            // tag::withlogging_ann[]
            import org.codehaus.groovy.transform.GroovyASTTransformationClass

            import java.lang.annotation.ElementType
            import java.lang.annotation.Retention
            import java.lang.annotation.RetentionPolicy
            import java.lang.annotation.Target

            @Retention(RetentionPolicy.SOURCE)
            @Target([ElementType.METHOD])
            @GroovyASTTransformationClass(["gep.WithLoggingASTTransformation"])
            public @interface WithLogging {
            }
            // end::withlogging_ann[]

            // tag::withlogging_xform[]
            @CompileStatic                                                                  // <1>
            @GroovyASTTransformation(phase=CompilePhase.SEMANTIC_ANALYSIS)                  // <2>
            class WithLoggingASTTransformation implements ASTTransformation {               // <3>

                @Override
                void visit(ASTNode[] nodes, SourceUnit sourceUnit) {                        // <4>
                    MethodNode method = (MethodNode) nodes[1]                               // <5>

                    def startMessage = createPrintlnAst("Starting $method.name")            // <6>
                    def endMessage = createPrintlnAst("Ending $method.name")                // <7>

                    def existingStatements = ((BlockStatement)method.code).statements       // <8>
                    existingStatements.add(0, startMessage)                                 // <9>
                    existingStatements.add(endMessage)                                      // <10>

                }

                private static Statement createPrintlnAst(String message) {                 // <11>
                    new ExpressionStatement(
                        new MethodCallExpression(
                            new VariableExpression("this"),
                            new ConstantExpression("println"),
                            new ArgumentListExpression(
                                new ConstantExpression(message)
                            )
                        )
                    )
                }
            }
            // end::withlogging_xform[]
            WithLogging
        '''

        gse.evaluate '''package gep
            // tag::withlogging_example[]
            @WithLogging
            def greet() {
                println "Hello World"
            }

            greet()
            // end::withlogging_example[]
        '''
    }
}
