/*
 * Copyright 2003-2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.groovy.control.customizers

import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.transform.GroovyASTTransformation
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.classgen.GeneratorContext
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.transform.GroovyASTTransformationClass
import java.lang.annotation.Annotation
import org.codehaus.groovy.ast.expr.ClosureExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.ClassExpression
import org.codehaus.groovy.ast.Parameter

/**
 * This customizer allows applying an AST transformation to a source unit with
 * several strategies.
 *
 * Creating a customizer with the {@link ASTTransformationCustomizer#ASTTransformationCustomizer(Class)
 * class constructor} will trigger an AST transformation for
 * each class node of a source unit. However, you cannot pass parameters to the annotation so the default values
 * will be used. Writing :
 * <pre>
 *     def configuration = new CompilerConfiguration()
 *     configuration.addCompilationCustomizers(new ASTTransformationCustomizer(Log))
 *     def shell = new GroovyShell(configuration)
 *     shell.evaluate("""
 *        class MyClass {
 *
 *        }""")
 * </pre>
 *
 * is equivalent to :
 *
 * <pre>
 *     def shell = new GroovyShell()
 *     shell.evaluate("""
 *        &#64;Log
 *        class MyClass {
 *
 *        }""")
 * </pre>
 *
 * The class passed as a constructor parameter must be an AST transformation annotation.
 *
 * Alternatively, you can apply a global AST transformation by calling the
 * {@link ASTTransformationCustomizer#ASTTransformationCustomizer(ASTTransformation) AST transformation
 * constructor}. In that case, the transformation is applied once for the whole source unit.
 *
 * Unlike a global AST transformation declared in the META-INF/services/org.codehaus.groovy.transform.ASTTransformation
 * file, which are applied if the file is in the classpath, using this customizer you'll have the choice to apply
 * your transformation selectively. It can also be useful to debug global AST transformations without having to
 * package your annotation in a jar file.
 *
 * @author Cedric Champeau
 *
 * @since 1.8.0
 * 
 */
class ASTTransformationCustomizer extends CompilationCustomizer {
    private final AnnotationNode annotationNode;
    private final ASTTransformation transformation
    private boolean applied = false; // used for global AST transformations

    ASTTransformationCustomizer(final Class<? extends Annotation> transformationAnnotation) {
        super(findPhase(transformationAnnotation))
        final Class<ASTTransformation> clazz = findASTTranformationClass(transformationAnnotation)
        this.transformation = clazz.newInstance()
        this.annotationNode = new AnnotationNode(ClassHelper.make(transformationAnnotation))
    }

    ASTTransformationCustomizer(final ASTTransformation transformation) {
        super(findPhase(transformation))
        this.transformation = transformation
        this.annotationNode = null
    }

    ASTTransformationCustomizer(final Map annotationParams, final Class<? extends Annotation> transformationAnnotation) {
        super(findPhase(transformationAnnotation))
        final Class<ASTTransformation> clazz = findASTTranformationClass(transformationAnnotation)
        this.transformation = clazz.newInstance()
        this.annotationNode = new AnnotationNode(ClassHelper.make(transformationAnnotation))
        setAnnotationParameters(annotationParams)
    }

    ASTTransformationCustomizer(final Map annotationParams, final ASTTransformation transformation) {
        this(transformation)
        setAnnotationParameters(annotationParams)
    }


    private static Class<ASTTransformation> findASTTranformationClass(Class<? extends Annotation> anAnnotationClass) {
        final GroovyASTTransformationClass annotation = anAnnotationClass.getAnnotation(GroovyASTTransformationClass)
        if (annotation==null) throw new IllegalArgumentException("Provided class doesn't look like an AST @interface")

        Class[] classes = annotation.classes()
        String[] classesAsStrings = annotation.value()
        if (classes.length+classesAsStrings.length>1) {
            throw new IllegalArgumentException("AST transformation customizer doesn't support AST transforms with multiple classes")
        }
        return classes?classes[0]:Class.forName(classesAsStrings[0])
    }

    private static CompilePhase findPhase(ASTTransformation transformation) {
        if (transformation==null) throw new IllegalArgumentException("Provided transformation must not be null")
        final Class<?> clazz = transformation.class
        final GroovyASTTransformation annotation = clazz.getAnnotation(GroovyASTTransformation)
        if (annotation==null) throw new IllegalArgumentException("Provided ast transformation is not annotated with "+GroovyASTTransformation.name)

        annotation.phase()
    }

    private static CompilePhase findPhase(Class<? extends Annotation> annotationClass) {
        Class<ASTTransformation> clazz = findASTTranformationClass(annotationClass);

        findPhase(clazz.newInstance())
    }

    /**
     * Specify annotation parameters. For example, if the annotation is :
     * <pre>@Log(value='logger')</pre>
     * You could create an AST transformation customizer and specify the "value" parameter thanks to this method:
     * <pre>annotationParameters = [value: 'logger']
     *
     * Note that you cannot specify annotation closure values directly. If the annotation you want to add takes
     * a closure as an argument, you will have to set a {@link ClosureExpression} instead. This can be done by either
     * creating a custom {@link ClosureExpression} from code, or using the {@link org.codehaus.groovy.ast.builder.AstBuilder}.
     *
     * Here is an example :
     * <pre>
     *        // add @Contract({distance >= 0 })
     *        customizer = new ASTTransformationCustomizer(Contract)
     *        final expression = new AstBuilder().buildFromCode(CompilePhase.CONVERSION) {->
     *            distance >= 0
     *        }.expression[0]
     *        customizer.annotationParameters = [value: expression]</pre>
     *
     * @param params the annotation parameters
     *
     * @since 1.8.1
     */
    public void setAnnotationParameters(Map<String,Object> params) {
        if (params==null || annotationNode==null) return;
        params.each { key, value ->
            if (!annotationNode.classNode.getMethod(key)) {
                throw new IllegalArgumentException("${annotationNode.classNode.name} does not accept any [$key] parameter")
            }
            if (value instanceof Closure) {
                throw new IllegalArgumentException("Direct usage of closure is not supported by the AST " +
                "compilation customizer. Please use ClosureExpression instead.")
            } else if (value instanceof ClosureExpression) {
                // avoid NPEs due to missing source code
                value.setLineNumber(0)
                value.setLastLineNumber(0)
                annotationNode.addMember(key, value)
            } else if (value instanceof Class) {
                annotationNode.addMember(key, new ClassExpression(ClassHelper.make(value)))
            } else {
                annotationNode.addMember(key, new ConstantExpression(value))
            }
        }
    }

    @Override
    void call(SourceUnit source, GeneratorContext context, ClassNode classNode) {
        if (annotationNode!=null) {
            // this is a local ast transformation which is applied on every class node
            annotationNode.sourcePosition = classNode
            transformation.visit([annotationNode, classNode] as ASTNode[], source)
        } else {
            // this is a global AST transformation
            if (!applied) transformation.visit(null, source)
        }
        applied = true
    }
}
