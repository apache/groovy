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
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ASTAnnotationTransformation;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.AnnotatedNode;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * This class handles the invocation of the ASTAnnotationTransformation
 * when it is encountered by a tree walk.  One instance of each exists
 * for each phase of the compilation it applies to.  Before invocation the
 * @{@link ASTAnnotationTransformationCollectorCodeVisitor} will add a list
 * of annotations that this cisitor should be concerned about.  All other
 * annotations are ignored, wether or not they are GroovyASTTransformation
 * annotated or not.
 *
 * A Two-pass method is used, first all candidate annotations are added to a
 * list then the transformations are called on those collected annotations.
 * This is done to avoid concurrent modification exceptions durring the AST tree
 * walk and allows the transformations to alter any portion of the AST tree.
 * Hence annotations that are added in this phase will not be processed as
 * transformations.  They will only be handled in later phases (and then only
 * if the type was in the AST prior to any AST transformations being run
 * against it)
 *
 * @author Danno Ferrin (shemnon)
 */
public class ASTAnnotationTransformationCodeVisitor extends ClassCodeVisitorSupport {

    private SourceUnit source;
    private GeneratorContext context;
    private Map<String, ASTAnnotationTransformation> annotationsMap;
    private List<ASTNode[]> targetNodes;

    public ASTAnnotationTransformationCodeVisitor() {
        annotationsMap = new HashMap<String, ASTAnnotationTransformation>();
    }

    protected SourceUnit getSourceUnit() {
        return source;
    }

    /**
     * Main loop entry.
     *
     * First, it delegates to teh super visitClass so we can collect the
     * relevant annotations in an AST tree walk.
     *
     * Second, calls the visit method on the transformation for each relevant
     * annotation found.
     *
     * @param classNode
     */
    public void visitClass(ClassNode classNode) {
        // only descend if we have annotations to look for
        if (!annotationsMap.isEmpty()) {
            targetNodes = new LinkedList<ASTNode[]>();

            // fist pass, collect nodes
            super.visitClass(classNode);

            // second pass, call visit on all of the collected nodes
            for (ASTNode[] node : targetNodes) {
                annotationsMap.get(((AnnotationNode) node[0]).getClassNode().getName())
                    .visit((AnnotationNode) node[0], (AnnotatedNode) node[1], source, context);
            }
        }
    }

    /**
     * Addes the annotation to the internal target list if a match is found
     * @param node
     */
    public void visitAnnotations(AnnotatedNode node) {
        super.visitAnnotations(node);
        for (AnnotationNode annotation : (Collection<AnnotationNode>) node.getAnnotations().values()) {
            String name = annotation.getClassNode().getName();
            if (annotationsMap.containsKey(name)) {
                targetNodes.add(new ASTNode[] {annotation, node});
            }
        }
    }

    /**
     * Used to see if the annotation is already added.
     *
     * @param annotationTypeName
     * @return
     */
    public boolean hasAnnotation(String annotationTypeName) {
        return annotationsMap.containsKey(annotationTypeName);
    }

    /**
     * Adds the particular transformation to this phase.
     *
     * @param name
     * @param transformation
     */
    public void addAnnotation(String name, ASTAnnotationTransformation transformation) {
        annotationsMap.put(name, transformation);
    }

    /**
     * Wraps itself as a PrimaryClassNodeOperation, suitable for inserton into
     * the CompilationUnit's phase operations.
     *
     * @return
     */
    public CompilationUnit.PrimaryClassNodeOperation getOperation() {
        return new CompilationUnit.PrimaryClassNodeOperation() {
            public void call(SourceUnit source, GeneratorContext context, ClassNode classNode) throws CompilationFailedException {
                ASTAnnotationTransformationCodeVisitor.this.source = source;
                ASTAnnotationTransformationCodeVisitor.this.context = context;
                visitClass(classNode);
            }
        };
    }

}

