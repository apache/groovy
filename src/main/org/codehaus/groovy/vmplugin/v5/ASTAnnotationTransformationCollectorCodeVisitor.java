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
import org.codehaus.groovy.control.messages.SimpleMessage;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.GroovyASTTransformation;
import org.codehaus.groovy.ast.ASTAnnotationTransformation;
import org.codehaus.groovy.ast.ClassNode;

import java.util.Map;
import java.util.Collection;

/**
 * @author Danno Ferrin (shemnon)
 */
public class ASTAnnotationTransformationCollectorCodeVisitor extends ClassCodeVisitorSupport {
    private SourceUnit source;
    private Map<Integer, ASTAnnotationTransformationCodeVisitor> stageVisitors;

    public ASTAnnotationTransformationCollectorCodeVisitor(Map<Integer, ASTAnnotationTransformationCodeVisitor> stageVisitors) {
        this.stageVisitors = stageVisitors;
    }

    protected SourceUnit getSourceUnit() {
        return source;
    }

    public void visitAnnotations(AnnotatedNode node) {
        super.visitAnnotations(node);
        for (AnnotationNode annotation : (Collection<AnnotationNode>) node.getAnnotations().values()) {
            Class annotationType = annotation.getClassNode().getTypeClass();
            GroovyASTTransformation transformationAnnotation = (GroovyASTTransformation) annotationType.getAnnotation(GroovyASTTransformation.class);
            if (transformationAnnotation == null) {
                continue;
            }
            try {
                stageVisitors.get(transformationAnnotation.phase())
                    .addAnnotation(
                        annotationType.getName(),
                        (ASTAnnotationTransformation) transformationAnnotation.transformationClass().newInstance());
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

    public CompilationUnit.PrimaryClassNodeOperation getOperation() {
        return new CompilationUnit.PrimaryClassNodeOperation() {
            public void call(SourceUnit source, GeneratorContext context, ClassNode classNode) throws CompilationFailedException {
                ASTAnnotationTransformationCollectorCodeVisitor.this.source = source;
                visitClass(classNode);
            }
        };
    }
}
