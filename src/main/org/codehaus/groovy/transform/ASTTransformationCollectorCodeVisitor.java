/*
 * Copyright 2008-2013 the original author or authors.
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

package org.codehaus.groovy.transform;

import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.messages.ExceptionMessage;
import org.codehaus.groovy.control.messages.SimpleMessage;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.codehaus.groovy.syntax.SyntaxException;

import groovy.lang.GroovyClassLoader;
import groovy.transform.AnnotationCollector;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * This visitor walks the AST tree and collects references to Annotations that
 * are annotated themselves by {@link GroovyASTTransformation}. Each such
 * annotation is added.
 * <p>
 * This visitor is only intended to be executed once, during the
 * SEMANTIC_ANALYSIS phase of compilation.
 *
 * @author Danno Ferrin (shemnon)
 * @author Roshan Dawrani (roshandawrani)
 * @author Jochen Theodorou (blackdrag)
 */
public class ASTTransformationCollectorCodeVisitor extends ClassCodeVisitorSupport {
    private SourceUnit source;
    private ClassNode classNode;
    private GroovyClassLoader transformLoader;

    public ASTTransformationCollectorCodeVisitor(SourceUnit source, GroovyClassLoader transformLoader) {
        this.source = source;
        this.transformLoader = transformLoader;
    }

    protected SourceUnit getSourceUnit() {
        return source;
    }

    public void visitClass(ClassNode klassNode) {
        ClassNode oldClass = classNode;
        classNode = klassNode;
        super.visitClass(classNode);
        classNode = oldClass;
    }

    /**
     * If the annotation is annotated with {@link GroovyASTTransformation}
     * the annotation is added to <code>stageVisitors</code> at the appropriate processor visitor.
     *
     * @param node the node to process
     */
    public void visitAnnotations(AnnotatedNode node) {
        super.visitAnnotations(node);
        
        List<AnnotationNode> collected = new ArrayList<AnnotationNode>();
        for (Iterator<AnnotationNode> it = node.getAnnotations().iterator(); it.hasNext();) {
            AnnotationNode annotation = it.next();
            if (addCollectedAnnotations(collected, annotation, node)) it.remove();
        }
        node.getAnnotations().addAll(collected);
        
        for (AnnotationNode annotation : node.getAnnotations()) {
            Annotation transformClassAnnotation = getTransformClassAnnotation(annotation.getClassNode());
            if (transformClassAnnotation == null) {
                // skip if there is no such annotation
                continue;
            }
            addTransformsToClassNode(annotation, transformClassAnnotation);
        }
    }
    
    private void assertStringConstant(Expression exp) {
        if (exp==null) return;
        if (!(exp instanceof ConstantExpression)) {
            source.getErrorCollector().addErrorAndContinue(new SyntaxErrorMessage(new SyntaxException(
                    "Expected a String constant.", exp.getLineNumber(), exp.getColumnNumber()), 
                    source));
        }
        ConstantExpression ce = (ConstantExpression) exp;
        if (!(ce.getValue() instanceof String)) {
            source.getErrorCollector().addErrorAndContinue(new SyntaxErrorMessage(new SyntaxException(
                    "Expected a String constant.", exp.getLineNumber(), exp.getColumnNumber()), 
                    source));
        }
    }
    
    private boolean addCollectedAnnotations(List<AnnotationNode> collected, AnnotationNode aliasNode, AnnotatedNode origin) {
        ClassNode classNode = aliasNode.getClassNode();
        boolean ret = false;
        for (AnnotationNode annotation : classNode.getAnnotations()) {
            if (annotation.getClassNode().getName().equals(AnnotationCollector.class.getName())) {
                Expression processorExp = annotation.getMember("processor");
                AnnotationCollectorTransform act = null;
                assertStringConstant(processorExp);
                if (processorExp!=null) {
                    String className = (String) ((ConstantExpression) processorExp).getValue();
                    Class klass = loadTransformClass(className, aliasNode);
                    if (klass!=null) {
                        try {
                            act = (AnnotationCollectorTransform) klass.newInstance();
                        } catch (InstantiationException e) {
                            source.getErrorCollector().addErrorAndContinue(new ExceptionMessage(e, true, source));
                        } catch (IllegalAccessException e) {
                            source.getErrorCollector().addErrorAndContinue(new ExceptionMessage(e, true, source));
                        }
                    }
                } else {
                    act = new AnnotationCollectorTransform();
                }
                if (act!=null) collected.addAll(act.visit(annotation, aliasNode, origin, source));
                ret = true;
            }
        }
        return ret;
    }

    private void addTransformsToClassNode(AnnotationNode annotation, Annotation transformClassAnnotation) {
        List<String> transformClassNames = getTransformClassNames(annotation, transformClassAnnotation);

        if(transformClassNames.isEmpty()) {
            source.getErrorCollector().addError(new SimpleMessage("@GroovyASTTransformationClass in " +
                    annotation.getClassNode().getName() + " does not specify any transform class names/classes", source));
        }

        for (String transformClass : transformClassNames) {
            Class klass = loadTransformClass(transformClass, annotation); 
            if (klass!=null) {
                verifyAndAddTransform(annotation, klass);
            }
        }
    }

    private Class loadTransformClass(String transformClass, AnnotationNode annotation) {
        try {
            return transformLoader.loadClass(transformClass, false, true, false);
        } catch (ClassNotFoundException e) {
            source.getErrorCollector().addErrorAndContinue(
                    new SimpleMessage(
                            "Could not find class for Transformation Processor " + transformClass
                            + " declared by " + annotation.getClassNode().getName(),
                            source));
        }
        return null;
    }

    private void verifyAndAddTransform(AnnotationNode annotation, Class klass) {
        verifyClass(annotation, klass);
        verifyCompilePhase(annotation, klass);
        addTransform(annotation, klass);
    }

    private void verifyCompilePhase(AnnotationNode annotation, Class<?> klass) {
        GroovyASTTransformation transformationClass = klass.getAnnotation(GroovyASTTransformation.class);
        if (transformationClass != null)  {
            CompilePhase specifiedCompilePhase = transformationClass.phase();
            if (specifiedCompilePhase.getPhaseNumber() < CompilePhase.SEMANTIC_ANALYSIS.getPhaseNumber())  {
                source.getErrorCollector().addError(
                        new SimpleMessage(
                                annotation.getClassNode().getName() + " is defined to be run in compile phase " + specifiedCompilePhase + ". Local AST transformations must run in " + CompilePhase.SEMANTIC_ANALYSIS + " or later!",
                                source));
            }

        } else {
            source.getErrorCollector().addError(
                new SimpleMessage("AST transformation implementation classes must be annotated with " + GroovyASTTransformation.class.getName() + ". " + klass.getName() + " lacks this annotation.", source));
        }
    }

    private void verifyClass(AnnotationNode annotation, Class klass) {
        if (!ASTTransformation.class.isAssignableFrom(klass)) {
            source.getErrorCollector().addError(new SimpleMessage("Not an ASTTransformation: " +
                    klass.getName() + " declared by " + annotation.getClassNode().getName(), source));
        }
    }

    @SuppressWarnings("unchecked")
    private void addTransform(AnnotationNode annotation, Class klass)  {
        classNode.addTransform(klass, annotation);
    }

    private static Annotation getTransformClassAnnotation(ClassNode annotatedType) {
        if (!annotatedType.isResolved()) return null;

        for (Annotation ann : annotatedType.getTypeClass().getAnnotations()) {
            // because compiler clients are free to choose any GroovyClassLoader for
            // resolving ClassNodeS such as annotatedType, we have to compare by name,
            // and cannot cast the return value to GroovyASTTransformationClass
            if (ann.annotationType().getName().equals(GroovyASTTransformationClass.class.getName())){
                return ann;
            }
        }

        return null;
    }

    private List<String> getTransformClassNames(AnnotationNode annotation, Annotation transformClassAnnotation) {
        List<String> result = new ArrayList<String>();

        try {
            Method valueMethod = transformClassAnnotation.getClass().getMethod("value");
            String[] names = (String[]) valueMethod.invoke(transformClassAnnotation);
            result.addAll(Arrays.asList(names));

            Method classesMethod = transformClassAnnotation.getClass().getMethod("classes");
            Class[] classes = (Class[]) classesMethod.invoke(transformClassAnnotation);
            for (Class klass : classes) {
                result.add(klass.getName());
            }

            if(names.length > 0 && classes.length > 0) {
                source.getErrorCollector().addError(new SimpleMessage("@GroovyASTTransformationClass in " +
                        annotation.getClassNode().getName() +
                        " should specify transforms only by class names or by classes and not by both", source));
            }
        } catch (Exception e) {
            source.addException(e);
        }

        return result;
    }
}
