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

import asciidoctor.Utils
import groovy.test.GroovyTestCase
import groovy.transform.CompileStatic
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.CompilerConfiguration

import java.lang.annotation.ElementType
import java.lang.annotation.Target

class ASTXFormSpecTest extends GroovyTestCase {
    void testLocalTransform() {
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

    void testClassCodeTransformer() {
        def gcl = new GroovyClassLoader()
        def gse = new GroovyShell(gcl)

        gse.parse '''package gep

            import groovy.transform.CompileStatic
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.ClassCodeExpressionTransformer
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation


            import org.codehaus.groovy.transform.GroovyASTTransformationClass

            import java.lang.annotation.ElementType
            import java.lang.annotation.Retention
            import java.lang.annotation.RetentionPolicy
            import java.lang.annotation.Target

            @Retention(RetentionPolicy.SOURCE)
            @Target([ElementType.METHOD])
            @GroovyASTTransformationClass(["gep.ShoutASTTransformation"])
            public @interface Shout {
            }

            // tag::shout_xform[]
            @CompileStatic
            @GroovyASTTransformation(phase=CompilePhase.SEMANTIC_ANALYSIS)
            class ShoutASTTransformation implements ASTTransformation {

                @Override
                void visit(ASTNode[] nodes, SourceUnit sourceUnit) {
                    ClassCodeExpressionTransformer trn = new ClassCodeExpressionTransformer() {         // <1>
                        private boolean inArgList = false
                        @Override
                        protected SourceUnit getSourceUnit() {
                            sourceUnit                                                                  // <2>
                        }

                        @Override
                        Expression transform(final Expression exp) {
                            if (exp instanceof ArgumentListExpression) {
                                inArgList = true
                            } else if (inArgList &&
                                exp instanceof ConstantExpression && exp.value instanceof String) {
                                return new ConstantExpression(exp.value.toUpperCase())                  // <3>
                            }
                            def trn = super.transform(exp)
                            inArgList = false
                            trn
                        }
                    }
                    trn.visitMethod((MethodNode)nodes[1])                                               // <4>
                }
            }
            // end::shout_xform[]
            Shout
        '''

        gse.evaluate '''package gep
            // tag::shout_example[]
            @Shout
            def greet() {
                println "Hello World"
            }

            greet()
            // end::shout_example[]
        '''
    }

    // tag::breakpoint_missed[]
    static class Subject {
        @MyTransformToDebug
        void methodToBeTested() {}
    }

    void testMyTransform() {
        def c = new Subject()
        c.methodToBeTested()
    }
    // end::breakpoint_missed[]

    // tag::breakpoint_hit[]
    void testMyTransformWithBreakpoint() {
        assertScript '''
            import metaprogramming.MyTransformToDebug

            class Subject {
                @MyTransformToDebug
                void methodToBeTested() {}
            }
            def c = new Subject()
            c.methodToBeTested()
        '''
    }
    // end::breakpoint_hit[]

    @CompileStatic
    private void doInTmpDir(Closure cl) {
        def baseDir = File.createTempDir()
        try {
            cl.call(new FileTreeBuilder(baseDir))
        } finally {
            baseDir.deleteDir()
        }
    }

    void testGlobalTransform() {
        doInTmpDir { builder ->
            File dir = builder.baseDir
            builder {
                'META-INF' {
                    services {
                        'org.codehaus.groovy.transform.ASTTransformation'(Utils.stripAsciidocMarkup('''
// tag::xform_descriptor_file[]
gep.WithLoggingASTTransformation
// end::xform_descriptor_file[]
'''))
                    }
                }
            }
            def conf = new CompilerConfiguration()
            conf.setTargetDirectory(dir)
            def cu = new CompilationUnit(conf)
            cu.addSource('WithLoggingASTTransformation.groovy','''package gep

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

            // tag::withlogging_xform_global[]
            @CompileStatic                                                                  // <1>
            @GroovyASTTransformation(phase=CompilePhase.SEMANTIC_ANALYSIS)                  // <2>
            class WithLoggingASTTransformation implements ASTTransformation {               // <3>

                @Override
                void visit(ASTNode[] nodes, SourceUnit sourceUnit) {                        // <4>
                    def methods = sourceUnit.AST.methods                                    // <5>
                    methods.each { method ->                                                // <6>
                        def startMessage = createPrintlnAst("Starting $method.name")        // <7>
                        def endMessage = createPrintlnAst("Ending $method.name")            // <8>

                        def existingStatements = ((BlockStatement)method.code).statements   // <9>
                        existingStatements.add(0, startMessage)                             // <10>
                        existingStatements.add(endMessage)                                  // <11>
                    }
                }

                private static Statement createPrintlnAst(String message) {                 // <12>
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
            // end::withlogging_xform_global[]
        ''')
            cu.compile(CompilePhase.FINALIZATION.phaseNumber)

            def gcl2 = new GroovyClassLoader(new URLClassLoader(
                    [dir.toURI().toURL()] as URL[]))
            def shell = new GroovyShell(gcl2)
            shell.evaluate '''
            // tag::withlogging_example_global[]
            def greet() {
                println "Hello World"
            }

            greet()
            // end::withlogging_example_global[]
        '''
        }

    }
}
