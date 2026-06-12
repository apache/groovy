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
package org.codehaus.groovy.ast;

import groovy.lang.groovydoc.Groovydoc;
import groovy.lang.groovydoc.GroovydocHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Base class for any AST node which is capable of being annotated
 */
public class AnnotatedNode extends ASTNode implements GroovydocHolder<AnnotatedNode> {
    private List<AnnotationNode> annotations = Collections.emptyList();
    private ClassNode declaringClass;
    private boolean synthetic;

    /**
     * Returns all annotations attached to this AST node.
     * Annotations are runtime-visible or source-level metadata attached to language elements.
     *
     * @return list of {@link AnnotationNode}, or empty list if none
     */
    public List<AnnotationNode> getAnnotations() {
        return annotations;
    }

    /**
     * Returns annotations of a specific type attached to this AST node.
     * Filters the annotation list by the provided type.
     *
     * @param type the annotation type to filter by
     * @return list of matching {@link AnnotationNode}, or empty list if none
     */
    public List<AnnotationNode> getAnnotations(final ClassNode type) {
        List<AnnotationNode> annotations = new ArrayList<>();
        for (AnnotationNode node : getAnnotations()) {
            if (type.equals(node.getClassNode())) {
                annotations.add(node);
            }
        }
        return annotations;
    }

    /**
     * Adds a new annotation of the specified type to this node.
     * Creates an {@link AnnotationNode} and attaches it.
     *
     * @param type the annotation type as a {@link ClassNode}
     * @return the newly created {@link AnnotationNode}
     */
    public AnnotationNode addAnnotation(final ClassNode type) {
        AnnotationNode node = new AnnotationNode(type);
        addAnnotation(node);
        return node;
    }

    /**
     * Attaches an annotation node to this AST node.
     * Does nothing if the annotation is null. Lazily initializes the annotations list.
     *
     * @param annotation the {@link AnnotationNode} to attach
     */
    public void addAnnotation(AnnotationNode annotation) {
        if (annotation != null) {
            if (annotations == Collections.EMPTY_LIST) {
                annotations = new ArrayList<>(3);
            }
            annotations.add(annotation);
        }
    }

    /**
     * Attaches a list of annotations to this AST node.
     * Individually adds each annotation in the list.
     *
     * @param annotations list of {@link AnnotationNode} objects to attach
     */
    public void addAnnotations(List<AnnotationNode> annotations) {
        for (AnnotationNode annotation : annotations) {
            addAnnotation(annotation);
        }
    }

    /**
     * Returns the class that declares this annotated node.
     * For class members (fields, methods), this is the enclosing class.
     * Returns null for top-level declarations.
     *
     * @return the declaring {@link ClassNode}, or null if none
     */
    public /*@Nullable*/ ClassNode getDeclaringClass() {
        return declaringClass;
    }

    /**
     * Sets the class that declares this annotated node.
     * Establishes the ownership relationship for class members.
     *
     * @param declaringClass the declaring {@link ClassNode}
     */
    public void setDeclaringClass(ClassNode declaringClass) {
        this.declaringClass = declaringClass;
    }

    /**
     * Returns the Groovydoc associated with this node.
     * Groovydoc is documentation metadata attached to source elements.
     * Returns {@link Groovydoc#EMPTY_GROOVYDOC} if no documentation is available.
     *
     * @return the {@link Groovydoc} for this node
     */
    @Override
    public Groovydoc getGroovydoc() {
        Groovydoc groovydoc = getNodeMetaData(DOC_COMMENT);
        return (groovydoc != null ? groovydoc : Groovydoc.EMPTY_GROOVYDOC);
    }

    /**
     * Returns this node instance (used by {@link GroovydocHolder} interface).
     *
     * @return this AnnotatedNode
     */
    @Override
    public AnnotatedNode getInstance() {
        return this;
    }

    /**
     * Checks whether this node has a real source position.
     * Returns false for compiler-generated nodes (like default constructors).
     * This flag distinguishes user-written code from synthetic compiler output.
     *
     * @return true if this node was explicitly written in source code
     * @see #hasNoRealSourcePosition()
     */
    public boolean hasNoRealSourcePosition() {
        return Boolean.TRUE.equals(getNodeMetaData("org.codehaus.groovy.ast.AnnotatedNode.hasNoRealSourcePosition"));
    }

    /**
     * Marks whether this node has a real source position.
     * Used to distinguish compiler-generated nodes from user-written code.
     *
     * @param hasNoRealSourcePosition false if this node was explicitly written in source
     */
    public void setHasNoRealSourcePosition(boolean hasNoRealSourcePosition) {
        if (hasNoRealSourcePosition) {
            putNodeMetaData("org.codehaus.groovy.ast.AnnotatedNode.hasNoRealSourcePosition", Boolean.TRUE);
        } else {
            removeNodeMetaData("org.codehaus.groovy.ast.AnnotatedNode.hasNoRealSourcePosition");
        }
    }

    /**
     * Checks whether this node was added by the compiler (synthetic).
     * Synthetic nodes include auto-generated methods, constructors, and other compiler-inserted elements.
     *
     * Note: This flag is distinct from the {@code synthetic} modifier for classes, fields, and methods in bytecode.
     *
     * @return true if this node was generated by the compiler, false if user-written
     */
    public boolean isSynthetic() {
        return synthetic;
    }

    /**
     * Marks this node as having been added by the compiler (synthetic).
     * Synthetic nodes include auto-generated methods, constructors, and other compiler-inserted elements.
     *
     * Note: This flag is distinct from the {@code synthetic} modifier for classes, fields, and methods in bytecode.
     *
     * @param synthetic true to mark as compiler-generated, false for user-written
     */
    public void setSynthetic(boolean synthetic) {
        this.synthetic = synthetic;
    }
}
