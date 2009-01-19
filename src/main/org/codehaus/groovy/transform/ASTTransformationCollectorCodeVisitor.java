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

package org.codehaus.groovy.transform;

import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.messages.SimpleMessage;

import java.util.Collection;

/**
 * This visitor walks the AST tree and collects references to Annotations that
 * are annotated themselves by {@link GroovyASTTransformation}. Each such
 * annotation is added.
 * <p/>
 * This visitor is only intended to be executed once, during the
 * SEMANTIC_ANALYSIS phase of compilation.
 *
 * @author Danno Ferrin (shemnon)
 */
public class ASTTransformationCollectorCodeVisitor extends ClassCodeVisitorSupport {
    private SourceUnit source;
    private ClassNode classNode;

    public ASTTransformationCollectorCodeVisitor(SourceUnit source) {
        this.source = source;
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
        for (AnnotationNode annotation : (Collection<AnnotationNode>) node.getAnnotations()) {
            ClassNode annotationClassNode = annotation.getClassNode();
            if (!annotationClassNode.isResolved()) continue;
            GroovyASTTransformationClass transformClassAnnotation =
                (GroovyASTTransformationClass) annotation.getClassNode().getTypeClass().getAnnotation(GroovyASTTransformationClass.class);
            if (transformClassAnnotation == null) {
                // stop if there is no appropriately typed annotation
                continue;
            }
            for (String transformClass : transformClassAnnotation.value()) {
                try {
                    Class klass = source.getClassLoader().loadClass(transformClass, false, true, false);
                    if (ASTTransformation.class.isAssignableFrom(klass)) {
                        classNode.addTransform(klass, annotation);
                    } else {
                        source.getErrorCollector().addError(
                                new SimpleMessage(
                                        "Not an ASTTransformatnion: " + transformClass
                                        + " declared by " + annotation.getClassNode().getName(),
                                        source));
                    }
                } catch (ClassNotFoundException e) {
                    source.getErrorCollector().addError(
                            new SimpleMessage(
                                    "Could find class for Transformation Processor " + transformClass
                                    + " declared by " + annotation.getClassNode().getName(),
                                    source));
                }
            }
        }
    }

}
