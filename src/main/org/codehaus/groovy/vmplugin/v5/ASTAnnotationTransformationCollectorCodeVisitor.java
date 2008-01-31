/*
 * Copyright 2008 the original author or authors.
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

package org.codehaus.groovy.vmplugin.v5;

import org.codehaus.groovy.classgen.GeneratorContext;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.Phases;
import org.codehaus.groovy.control.messages.SimpleMessage;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.GroovyASTTransformation;
import org.codehaus.groovy.ast.ASTAnnotationTransformation;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.syntax.SyntaxException;

import java.util.Map;
import java.util.Collection;

/**
 * This visitor walks the AST tree and collects references to Annotations that
 * are annotated themselves by @{@link GroovyASTTransformation}.  Each such
 * annotation is added
 *
 * This visitor is only intended to be executed once, durring the
 * SEMANTIC_ANALYSIS phase of compilation.
 *
 * @author Danno Ferrin (shemnon)
 */
public class ASTAnnotationTransformationCollectorCodeVisitor extends ClassCodeVisitorSupport {
    private SourceUnit source;
    private Map<Integer, ASTAnnotationTransformationCodeVisitor> stageVisitors;

    /**
     * Create the visitor
     *
     * @param stageVisitors The map of {@link ASTAnnotationTransformationCodeVisitor}s keyed by phase number.
     */
    public ASTAnnotationTransformationCollectorCodeVisitor(Map<Integer, ASTAnnotationTransformationCodeVisitor> stageVisitors) {
        this.stageVisitors = stageVisitors;
    }

    protected SourceUnit getSourceUnit() {
        return source;
    }

    /**
     * If the annotaiton is annotated with @{@link org.codehaus.groovy.ast.GroovyASTTransformation}
     * the annotation is added to stageVisitors at the appropriate processor visitor.
     * @param node
     */
    public void visitAnnotations(AnnotatedNode node) {
        super.visitAnnotations(node);
        for (AnnotationNode annotation : (Collection<AnnotationNode>) node.getAnnotations().values()) {
            Class<? extends GroovyASTTransformation> annotationType = annotation.getClassNode().getTypeClass();
            GroovyASTTransformation transformationAnnotation =
                annotationType.getAnnotation(GroovyASTTransformation.class);
            if (transformationAnnotation == null) {
                continue;
            }
            ASTAnnotationTransformationCodeVisitor stage = stageVisitors.get(
                transformationAnnotation.phase());
            String annotationTypeName = annotationType.getName();

            if (stage == null) {
                try {
                    String phaseName = Phases.getDescription(transformationAnnotation.phase());
                    source.getErrorCollector().addErrorAndContinue(
                        new SyntaxErrorMessage(new SyntaxException(
                            "@" + annotationTypeName + " cannot be handled in phase " + phaseName,
                            node.getLineNumber(),
                            node.getColumnNumber()),
                            source));
                } catch (ArrayIndexOutOfBoundsException aiobe) {
                    source.getErrorCollector().addErrorAndContinue(
                        new SyntaxErrorMessage(new SyntaxException(
                            "@" + annotationTypeName + " specifies a phase that does not exist: " + transformationAnnotation.phase(),
                            node.getLineNumber(),
                            node.getColumnNumber()),
                            source));
                }
            } else if (!stage.hasAnnotation(annotationTypeName)) {
                try {
                    stage.addAnnotation(annotationTypeName,
                        transformationAnnotation.transformationClass().newInstance());
                } catch (InstantiationException e) {
                    source.getErrorCollector().addError(
                        new SimpleMessage(
                            "Could not instantiate Transformation Processor " + transformationAnnotation.transformationClass().getName(),
                            source));
                } catch (IllegalAccessException e) {
                    source.getErrorCollector().addError(
                        new SimpleMessage(
                            "Could not instantiate Transformation Processor " + transformationAnnotation.transformationClass().getName(),
                            source));
                }
            }
        }
    }

    /**
     * Wraps itself in a PrimaryClassNodeOpration
     * @return
     */
    public CompilationUnit.PrimaryClassNodeOperation getOperation() {
        return new CompilationUnit.PrimaryClassNodeOperation() {
            public void call(SourceUnit source, GeneratorContext context, ClassNode classNode) throws CompilationFailedException {
                ASTAnnotationTransformationCollectorCodeVisitor.this.source = source;
                visitClass(classNode);
            }
        };
    }
}
