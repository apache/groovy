package org.codehaus.groovy.transform

import org.codehaus.groovy.control.CompilationFailedException

/**
 * @author Andre Steingress
 */
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




