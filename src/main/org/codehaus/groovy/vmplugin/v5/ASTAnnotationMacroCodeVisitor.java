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
import org.codehaus.groovy.ast.ASTAnnotationMacro;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.AnnotatedNode;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Collection;

/**
 * @author Danno Ferrin (shemnon)
 */
public class ASTAnnotationMacroCodeVisitor extends ClassCodeVisitorSupport {

    private SourceUnit source;
    private GeneratorContext context;
    private Map<String, ASTAnnotationMacro> annotationsMap;
    private List<ASTNode[]> targetNodes;

    public ASTAnnotationMacroCodeVisitor() {
        annotationsMap = new HashMap<String, ASTAnnotationMacro>();
    }

    protected SourceUnit getSourceUnit() {
        return source;
    }

    public void visitClass(ClassNode classNode) {
        if (!annotationsMap.isEmpty()) {
            // only descend if we have annotations to look for
            targetNodes = new LinkedList<ASTNode[]>();
            super.visitClass(classNode);
            for (ASTNode[] node : targetNodes) {
                annotationsMap.get(((AnnotationNode) node[0]).getClassNode().getName())
                    .visit((AnnotationNode) node[0], (AnnotatedNode) node[1], source, context);
            }
        }
    }

    public void visitAnnotations(AnnotatedNode node) {
        super.visitAnnotations(node);
        for (AnnotationNode annotation : (Collection<AnnotationNode>) node.getAnnotations().values()) {
            String name = annotation.getClassNode().getName();
            if (annotationsMap.containsKey(name)) {
                targetNodes.add(new ASTNode[] {annotation, node});
            }
        }
    }

    public void addAnnotation(String name, ASTAnnotationMacro macro) {
        annotationsMap.put(name, macro);
    }

    public CompilationUnit.PrimaryClassNodeOperation getOperation() {
        return new CompilationUnit.PrimaryClassNodeOperation() {
            public void call(SourceUnit source, GeneratorContext context, ClassNode classNode) throws CompilationFailedException {
                ASTAnnotationMacroCodeVisitor.this.source = source;
                ASTAnnotationMacroCodeVisitor.this.context = context;
                visitClass(classNode);
            }
        };
    }
}

