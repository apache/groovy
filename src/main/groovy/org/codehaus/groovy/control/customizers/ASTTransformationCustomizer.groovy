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
package org.codehaus.groovy.control.customizers

import groovy.transform.AutoFinal
import groovy.transform.CompilationUnitAware
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.classgen.GeneratorContext
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformationClass

import java.lang.annotation.Annotation

import static org.codehaus.groovy.ast.tools.GeneralUtils.classX
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX
import static org.codehaus.groovy.ast.tools.GeneralUtils.listX
import static org.codehaus.groovy.ast.tools.GeneralUtils.propX

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
 * @since 1.8.0
 */
@AutoFinal @CompileStatic
class ASTTransformationCustomizer extends CompilationCustomizer implements CompilationUnitAware {

    private boolean applied // global xforms
    protected CompilationUnit compilationUnit
    private final AnnotationNode annotationNode
            final ASTTransformation transformation

    /**
     * Creates an AST transformation customizer using the specified annotation. The transformation classloader can
     * be used if the transformation class cannot be loaded from the same class loader as the annotation class.
     * It's assumed that the annotation is not annotated with {@code GroovyASTTransformationClass} and so the
     * second argument supplies the link to the ASTTransformation class that should be used.
     * @param transformationAnnotation
     * @param astTransformationClassName
     * @param transformationClassLoader
     */
    ASTTransformationCustomizer(Class<? extends Annotation> transformationAnnotation, String astTransformationClassName, ClassLoader transformationClassLoader) {
        super(findPhase(transformationAnnotation, astTransformationClassName, transformationClassLoader))
        Class<ASTTransformation> clazz = findASTTransformationClass(transformationAnnotation, astTransformationClassName, transformationClassLoader)
        this.transformation = clazz.getConstructor().newInstance()
        this.annotationNode = new AnnotationNode(ClassHelper.make(transformationAnnotation))
    }

    /**
     * Creates an AST transformation customizer using the specified annotation. It's assumed that the annotation
     * is not annotated with {@code GroovyASTTransformationClass} and so the second argument supplies the link to
     * the ASTTransformation class that should be used.
     * @param transformationAnnotation
     * @param astTransformationClassName
     */
    ASTTransformationCustomizer(Class<? extends Annotation> transformationAnnotation, String astTransformationClassName) {
        this(transformationAnnotation, astTransformationClassName, transformationAnnotation.classLoader)
    }

    /**
     * Creates an AST transformation customizer using the specified annotation. The transformation classloader can
     * be used if the transformation class cannot be loaded from the same class loader as the annotation class.
     * Additionally, you can pass a map of parameters that will be used to parameterize the annotation.
     * It's assumed that the annotation is not annotated with {@code GroovyASTTransformationClass} and so the
     * second argument supplies the link to the ASTTransformation class that should be used.
     * @param transformationAnnotation
     * @param astTransformationClassName
     * @param transformationClassLoader
     */
    ASTTransformationCustomizer(Map annotationParams, Class<? extends Annotation> transformationAnnotation, String astTransformationClassName, ClassLoader transformationClassLoader) {
        super(findPhase(transformationAnnotation, astTransformationClassName, transformationClassLoader))
        Class<ASTTransformation> clazz = findASTTransformationClass(transformationAnnotation, astTransformationClassName, transformationClassLoader)
        this.transformation = clazz.getConstructor().newInstance()
        this.annotationNode = new AnnotationNode(ClassHelper.make(transformationAnnotation))
        this.annotationParameters = annotationParams
    }

    ASTTransformationCustomizer(Map annotationParams, Class<? extends Annotation> transformationAnnotation, String astTransformationClassName) {
        this(annotationParams, transformationAnnotation, transformationAnnotation.classLoader)
    }

    /**
     * Creates an AST transformation customizer using the specified annotation. The transformation classloader can
     * be used if the transformation class cannot be loaded from the same class loader as the annotation class.
     * @param transformationAnnotation
     * @param transformationClassLoader
     */
    ASTTransformationCustomizer(Class<? extends Annotation> transformationAnnotation, ClassLoader transformationClassLoader) {
        super(findPhase(transformationAnnotation, transformationClassLoader))
        Class<ASTTransformation> clazz = findASTTransformationClass(transformationAnnotation, transformationClassLoader)
        this.transformation = clazz.getConstructor().newInstance()
        this.annotationNode = new AnnotationNode(ClassHelper.make(transformationAnnotation))
    }

    /**
     * Creates an AST transformation customizer using the specified annotation.
     * @param transformationAnnotation
     */
    ASTTransformationCustomizer(Class<? extends Annotation> transformationAnnotation) {
        this(transformationAnnotation, transformationAnnotation.classLoader)
    }

    /**
     * Creates an AST transformation customizer using the specified transformation.
     */
    ASTTransformationCustomizer(ASTTransformation transformation) {
        super(findPhase(transformation))
        this.transformation = transformation
        this.annotationNode = null
    }

    /**
     * Creates an AST transformation customizer using the specified annotation. The transformation classloader can
     * be used if the transformation class cannot be loaded from the same class loader as the annotation class.
     * Additionally, you can pass a map of parameters that will be used to parameterize the annotation.
     * @param transformationAnnotation
     * @param transformationClassLoader
     */
    ASTTransformationCustomizer(Map annotationParams, Class<? extends Annotation> transformationAnnotation, ClassLoader transformationClassLoader) {
        super(findPhase(transformationAnnotation, transformationClassLoader))
        Class<ASTTransformation> clazz = findASTTransformationClass(transformationAnnotation, transformationClassLoader)
        this.transformation = clazz.getConstructor().newInstance()
        this.annotationNode = new AnnotationNode(ClassHelper.make(transformationAnnotation))
        this.annotationParameters = annotationParams
    }

    ASTTransformationCustomizer(Map annotationParams, Class<? extends Annotation> transformationAnnotation) {
        this(annotationParams, transformationAnnotation, transformationAnnotation.classLoader)
    }

    ASTTransformationCustomizer(Map annotationParams, ASTTransformation transformation) {
        this(transformation)
        this.annotationParameters = annotationParams
    }

    void setCompilationUnit(CompilationUnit unit) {
        compilationUnit = unit
    }

    @SuppressWarnings('ClassForName')
    private static Class<ASTTransformation> findASTTransformationClass(Class<? extends Annotation> anAnnotationClass, ClassLoader transformationClassLoader) {
        GroovyASTTransformationClass annotation = anAnnotationClass.getAnnotation(GroovyASTTransformationClass)
        if (annotation == null) throw new IllegalArgumentException("Provided class doesn't look like an AST @interface")

        Class[] classes = annotation.classes()
        String[] classesAsStrings = annotation.value()
        if (classes.length + classesAsStrings.length > 1) {
            throw new IllegalArgumentException("AST transformation customizer doesn't support AST transforms with multiple classes")
        }
        classes ? classes[0] : (Class) Class.forName(classesAsStrings[0], true, transformationClassLoader ?: anAnnotationClass.classLoader)
    }

    @SuppressWarnings('ClassForName')
    private static Class<ASTTransformation> findASTTransformationClass(Class<? extends Annotation> anAnnotationClass, String astTransformationClassName, ClassLoader transformationClassLoader) {
        Class.forName(astTransformationClassName, true, transformationClassLoader ?: anAnnotationClass.classLoader) as Class<ASTTransformation>
    }

    private static CompilePhase findPhase(ASTTransformation transformation) {
        if (transformation == null) throw new IllegalArgumentException('Provided transformation must not be null')
        Class<?> clazz = transformation.class
        GroovyASTTransformation annotation = clazz.getAnnotation(GroovyASTTransformation)
        if (annotation == null) throw new IllegalArgumentException("Provided ast transformation is not annotated with $GroovyASTTransformation.name")

        annotation.phase()
    }

    private static CompilePhase findPhase(Class<? extends Annotation> annotationClass, ClassLoader transformationClassLoader) {
        Class<ASTTransformation> clazz = findASTTransformationClass(annotationClass, transformationClassLoader)

        findPhase(clazz.getConstructor().newInstance())
    }

    private static CompilePhase findPhase(Class<? extends Annotation> annotationClass, String astTransformationClassName, ClassLoader transformationClassLoader) {
        Class<ASTTransformation> clazz = findASTTransformationClass(annotationClass, astTransformationClassName, transformationClassLoader)

        findPhase(clazz.getConstructor().newInstance())
    }

    /**
     * Specify annotation parameters. For example, if the annotation is:
     * <pre>@Log(value='logger')</pre>
     * You could create an AST transformation customizer and specify the "value" parameter thanks to this method:
     * <pre>annotationParameters = [value: 'logger']</pre>
     *
     * Note that you cannot specify annotation closure values directly. If the annotation you want to add takes
     * a closure as an argument, you will have to set a {@link ClosureExpression} instead. This can be done by either
     * creating a custom {@link ClosureExpression} from code, or using the {@link org.codehaus.groovy.ast.builder.AstBuilder}.
     * <p>
     * Here is an example:
     * <pre>
     * // add @Contract({distance >= 0 })
     * def customizer = new ASTTransformationCustomizer(Contract)
     * def expression = new AstBuilder().buildFromCode(CompilePhase.CONVERSION) { ->
     *    distance >= 0
     * }.expression[0]
     * customizer.annotationParameters = [value: expression]</pre>
     *
     * @param params the annotation parameters
     *
     * @since 1.8.1
     */
    @CompileDynamic
    void setAnnotationParameters(Map<String, Object> params) {
        if (annotationNode == null || params == null || params.isEmpty()) return
        params.each { name, value ->
            if (!annotationNode.classNode.getMethod(name)) {
                throw new IllegalArgumentException("${annotationNode.classNode.name} does not accept any [$name] parameter")
            }
            if (value instanceof Closure) {
                throw new IllegalArgumentException('Direct usage of closure is not supported by the AST compilation customizer. Please use ClosureExpression instead.')
            }

            Expression valueExpression

            if (value instanceof Expression) {
                valueExpression = value
                // avoid NPEs due to missing source code
                value.lineNumber = 0; value.lastLineNumber = 0
            } else if (value instanceof Class) {
                valueExpression = classX(value)
            } else if (value instanceof Enum) {
                valueExpression = propX(classX(ClassHelper.make(value.getClass())), value.toString())
            } else if (value instanceof List || value.getClass().isArray()) {
                valueExpression = listX(value.collect { it instanceof Class ? classX(it) : constX(it) })
            } else {
                valueExpression = constX(value)
            }

            annotationNode.addMember(name, valueExpression)
        }
    }

    @Override
    void call(SourceUnit source, GeneratorContext context, ClassNode classNode) {
        if (transformation instanceof CompilationUnitAware) {
            ((CompilationUnitAware) transformation).compilationUnit = compilationUnit
        }
        if (annotationNode != null) {
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
