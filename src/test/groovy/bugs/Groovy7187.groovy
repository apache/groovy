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
package bugs

import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript

/**
 * GROOVY-7187: {@code AstBuilder.buildFromSpec} {@code staticMethodCall}
 * with an {@code argumentList} used from an AST transformation used to
 * fail on the second script compiled with a fresh {@code GroovyClassLoader}:
 * {@code staticMethodCall could not be invoked. Expected to receive
 * parameters [ClassNode, String, Expression] but found [ClassNode, String]}.
 */
final class Groovy7187 {

    @Test
    void testStaticMethodCallInAstTransformAcrossTwoScripts() {
        assertScript '''
            import org.codehaus.groovy.ast.ASTNode
            import org.codehaus.groovy.ast.ClassNode
            import org.codehaus.groovy.ast.MethodNode
            import org.codehaus.groovy.ast.builder.AstBuilder
            import org.codehaus.groovy.control.CompilePhase
            import org.codehaus.groovy.control.SourceUnit
            import org.codehaus.groovy.transform.ASTTransformation
            import org.codehaus.groovy.transform.GroovyASTTransformation
            import org.codehaus.groovy.transform.GroovyASTTransformationClass

            import java.lang.annotation.ElementType
            import java.lang.annotation.Retention
            import java.lang.annotation.RetentionPolicy
            import java.lang.annotation.Target

            import static org.objectweb.asm.Opcodes.ACC_PRIVATE

            class Pico {
                static String load(String picoName, String dir) {
                    picoName + ':' + dir
                }
            }

            @Retention(RetentionPolicy.SOURCE)
            @Target([ElementType.TYPE])
            @GroovyASTTransformationClass('LoadPanelTransform')
            @interface LoadPanel {}

            @GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
            class LoadPanelTransform implements ASTTransformation {
                void visit(ASTNode[] nodes, SourceUnit source) {
                    if (nodes == null || nodes.length < 2 || !(nodes[1] instanceof ClassNode)) return
                    ((ClassNode) nodes[1]).addMethod(createLoadPanelMethod())
                }

                private MethodNode createLoadPanelMethod() {
                    def ast = new AstBuilder().buildFromSpec {
                        method('loadPanel', ACC_PRIVATE, String) {
                            parameters {
                                parameter 'picoName': String.class
                                parameter 'panelName': String.class
                            }
                            exceptions {}
                            block {
                                returnStatement {
                                    staticMethodCall(Pico, 'load') {
                                        argumentList {
                                            variable 'picoName'
                                            constant 'picos'
                                        }
                                    }
                                }
                            }
                            annotations {}
                        }
                    }
                    return ast[0]
                }
            }

            def parent = this.class.classLoader
            def sources = [
                \'\'\'
                    @LoadPanel
                    class Script1 {
                        String run() { loadPanel('one', 'panelA') }
                    }
                \'\'\',
                \'\'\'
                    @LoadPanel
                    class Script2 {
                        String run() { loadPanel('two', 'panelB') }
                    }
                \'\'\'
            ]
            def results = sources.collect { src ->
                def gcl = new GroovyClassLoader(parent)
                def c = gcl.parseClass(src)
                c.getDeclaredConstructor().newInstance().run()
            }
            assert results == ['one:picos', 'two:picos']
        '''
    }
}
