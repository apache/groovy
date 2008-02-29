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
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ASTSingleNodeTransformation;
import org.codehaus.groovy.ast.ASTNode;
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
public class ASTTransformationCollectorCodeVisitor extends ClassCodeVisitorSupport {
    private SourceUnit source;
    private CompilationUnit compilationUnit;
    private Map<Integer, ASTTransformationCodeVisitor> stageVisitors;

    /**
     * Create the visitor
     *
     * @param stageVisitors The map of {@link ASTTransformationCodeVisitor}s keyed by phase number.
     */
    public ASTTransformationCollectorCodeVisitor(Map<Integer, ASTTransformationCodeVisitor> stageVisitors) {
        this.stageVisitors = stageVisitors;
        this.compilationUnit = compilationUnit;
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
        for (AnnotationNode annotation : (Collection<AnnotationNode>) node.getAnnotations()) {
            Class<? extends GroovyASTTransformation> annotationType = annotation.getClassNode().getTypeClass();
            GroovyASTTransformation transformationAnnotation =
                annotationType.getAnnotation(GroovyASTTransformation.class);
            if (transformationAnnotation == null) {
                // stop if there is no appropriately typed annotation
                continue;
            }
            ASTTransformationCodeVisitor stage = stageVisitors.get(
                transformationAnnotation.phase());
            String annotationTypeName = annotationType.getName();

            if (stage == null) {
                badStageError(node, transformationAnnotation, annotationTypeName);
            } else if (!stage.hasAnnotation(annotationTypeName)) {
                try {
                    Object o = Class.forName(transformationAnnotation.transformationClassName()).newInstance();
                    if (o instanceof ASTSingleNodeTransformation) {
                    stage.addAnnotation(annotationTypeName,
                            (ASTSingleNodeTransformation) o);
                    } else if (o instanceof CompilationUnit.PrimaryClassNodeOperation) {
                        compilationUnit.addPhaseOperation(
                            (CompilationUnit.PrimaryClassNodeOperation)o,
                            transformationAnnotation.phase());
                    } else if (o instanceof CompilationUnit.SourceUnitOperation) {
                        compilationUnit.addPhaseOperation(
                            (CompilationUnit.SourceUnitOperation)o,
                            transformationAnnotation.phase());
                    } else if (o instanceof CompilationUnit.GroovyClassOperation) {
                        compilationUnit.addPhaseOperation(
                            (CompilationUnit.GroovyClassOperation)o);
                    }
                } catch (InstantiationException e) {
                    source.getErrorCollector().addError(
                        new SimpleMessage(
                            "Could not instantiate Transformation Processor " + transformationAnnotation.transformationClassName(),
                            source));
                } catch (IllegalAccessException e) {
                    source.getErrorCollector().addError(
                        new SimpleMessage(
                            "Could not instantiate Transformation Processor " + transformationAnnotation.transformationClassName(),
                            source));
                } catch (ClassNotFoundException e) {
                    source.getErrorCollector().addError(
                        new SimpleMessage(
                            "Could find class for Transformation Processor " + transformationAnnotation.transformationClassName(),
                            source));
                }
            }
        }
    }

    private void badStageError(ASTNode node, GroovyASTTransformation transformationAnnotation, String annotationTypeName) {
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
    }

    /**
     * Wraps itself in a PrimaryClassNodeOpration
     * @return
     */
    public CompilationUnit.PrimaryClassNodeOperation getOperation() {
        return new CompilationUnit.PrimaryClassNodeOperation() {
            public void call(SourceUnit source, GeneratorContext context, ClassNode classNode) throws CompilationFailedException {
                ASTTransformationCollectorCodeVisitor.this.source = source;
                visitClass(classNode);
            }
        };
    }
}
