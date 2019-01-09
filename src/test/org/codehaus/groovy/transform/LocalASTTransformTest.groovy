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
package org.codehaus.groovy.transform

import org.codehaus.groovy.control.CompilationFailedException

class LocalASTTransformTest extends GroovyShellTestCase {

    void testVerifyCompilePhaseBeforeSemanticAnalysisWithStringClassName()  {
        def gcl = new GroovyClassLoader()

        gcl.parseClass """
            package org.codehaus.groovy.transform

            import java.lang.annotation.Retention
            import java.lang.annotation.RetentionPolicy
            import java.lang.annotation.Target
            import java.lang.annotation.ElementType

            @Retention(RetentionPolicy.RUNTIME)
            @Target([ElementType.TYPE])
            @GroovyASTTransformationClass("org.codehaus.groovy.transform.LocalTransformASTTransformation")
            public @interface LocalTransform {}
        """

        gcl.parseClass """
            package org.codehaus.groovy.transform

            import org.codehaus.groovy.control.CompilePhase

            @GroovyASTTransformation(phase = CompilePhase.CONVERSION)
            public class LocalTransformASTTransformation extends AbstractASTTransformation {
                void visit(org.codehaus.groovy.ast.ASTNode[] nodes, org.codehaus.groovy.control.SourceUnit source) {}
            }
        """

        shouldFail CompilationFailedException, {
            gcl.parseClass """
                import org.codehaus.groovy.transform.*

                @LocalTransform class Test {}
            """
        }
    }

    void testVerifyCompilePhaseBeforeSemanticAnalysisWithClassReference()  {
        def gcl = new GroovyClassLoader()

        gcl.parseClass """
                package org.codehaus.groovy.transform

                import org.codehaus.groovy.control.CompilePhase

                @GroovyASTTransformation(phase = CompilePhase.CONVERSION)
                public class LocalTransformASTTransformation1 extends AbstractASTTransformation {
                    void visit(org.codehaus.groovy.ast.ASTNode[] nodes, org.codehaus.groovy.control.SourceUnit source) {}
                }
            """

        gcl.parseClass """
                        package org.codehaus.groovy.transform

                        import org.codehaus.groovy.control.CompilePhase

                        @GroovyASTTransformation(phase = CompilePhase.CONVERSION)
                        public class LocalTransformASTTransformation2 extends AbstractASTTransformation {
                            void visit(org.codehaus.groovy.ast.ASTNode[] nodes, org.codehaus.groovy.control.SourceUnit source) {}
                        }
                    """

        gcl.parseClass """
                package org.codehaus.groovy.transform

                import java.lang.annotation.Retention
                import java.lang.annotation.RetentionPolicy
                import java.lang.annotation.Target
                import java.lang.annotation.ElementType

                @Retention(RetentionPolicy.RUNTIME)
                @Target([ElementType.TYPE])
                @GroovyASTTransformationClass(classes = [org.codehaus.groovy.transform.LocalTransformASTTransformation1, org.codehaus.groovy.transform.LocalTransformASTTransformation2])
                public @interface LocalTransform {}
            """

        shouldFail CompilationFailedException, {
            gcl.parseClass """
                    import org.codehaus.groovy.transform.*

                    @LocalTransform class Test {}
                """
        }
    }

    void testVerifyAnnotatedWithGroovyASTTransformation() {
        def gcl = new GroovyClassLoader()

        gcl.parseClass """
                        package org.codehaus.groovy.transform

                        import org.codehaus.groovy.control.CompilePhase

                        public class LocalTransformASTTransformation extends AbstractASTTransformation {
                            void visit(org.codehaus.groovy.ast.ASTNode[] nodes, org.codehaus.groovy.control.SourceUnit source) {}
                        }
                    """

        gcl.parseClass """
                       package org.codehaus.groovy.transform

                       import java.lang.annotation.Retention
                       import java.lang.annotation.RetentionPolicy
                       import java.lang.annotation.Target
                       import java.lang.annotation.ElementType

                       @Retention(RetentionPolicy.RUNTIME)
                       @Target([ElementType.TYPE])
                       @GroovyASTTransformationClass('org.codehaus.groovy.transform.LocalTransformASTTransformation')
                       public @interface LocalTransform {}
                   """

        def message = shouldFail CompilationFailedException, {
            gcl.parseClass """
                    import org.codehaus.groovy.transform.*

                    @LocalTransform class Test {}
                """
        }

        assertTrue message.contains("AST transformation implementation classes must be annotated with org.codehaus.groovy.transform.GroovyASTTransformation. org.codehaus.groovy.transform.LocalTransformASTTransformation lacks this annotation.")
    }
}




