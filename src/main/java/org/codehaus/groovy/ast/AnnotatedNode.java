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

    public List<AnnotationNode> getAnnotations() {
        return annotations;
    }

    public List<AnnotationNode> getAnnotations(ClassNode type) {
        List<AnnotationNode> ret = new ArrayList<>(annotations.size());
        for (AnnotationNode node : annotations) {
            if (type.equals(node.getClassNode())) {
                ret.add(node);
            }
        }
        return ret;
    }

    public void addAnnotation(AnnotationNode annotation) {
        if (annotation != null) {
            if (annotations == Collections.EMPTY_LIST) {
                annotations = new ArrayList<>(3);
            }
            annotations.add(annotation);
        }
    }

    public void addAnnotations(List<AnnotationNode> annotations) {
        for (AnnotationNode annotation : annotations) {
            addAnnotation(annotation);
        }
    }

    public /*@Nullable*/ ClassNode getDeclaringClass() {
        return declaringClass;
    }

    public void setDeclaringClass(ClassNode declaringClass) {
        this.declaringClass = declaringClass;
    }

    @Override
    public Groovydoc getGroovydoc() {
        Groovydoc groovydoc = getNodeMetaData(DOC_COMMENT);
        return (groovydoc != null ? groovydoc : Groovydoc.EMPTY_GROOVYDOC);
    }

    @Override
    public AnnotatedNode getInstance() {
        return this;
    }

    /**
     * Returns true for default constructors added by the compiler.
     * <p>
     * See GROOVY-4161
     */
    public boolean hasNoRealSourcePosition() {
        return Boolean.TRUE.equals(getNodeMetaData("org.codehaus.groovy.ast.AnnotatedNode.hasNoRealSourcePosition"));
    }

    public void setHasNoRealSourcePosition(boolean hasNoRealSourcePosition) {
        if (hasNoRealSourcePosition) {
            putNodeMetaData("org.codehaus.groovy.ast.AnnotatedNode.hasNoRealSourcePosition", Boolean.TRUE);
        } else {
            removeNodeMetaData("org.codehaus.groovy.ast.AnnotatedNode.hasNoRealSourcePosition");
        }
    }

    /**
     * Indicates if this node was added by the compiler.
     * <p>
     * <b>Note</b>: This method has nothing to do with the synthetic flag for classes, fields, methods or properties.
     */
    public boolean isSynthetic() {
        return synthetic;
    }

    /**
     * Sets this node as a node added by the compiler.
     * <p>
     * <b>Note</b>: This method has nothing to do with the synthetic flag for classes, fields, methods or properties.
     */
    public void setSynthetic(boolean synthetic) {
        this.synthetic = synthetic;
    }
}
