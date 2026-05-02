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

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents a component (field) of a record class definition introduced in Java 16.
 * Record components provide immutable fields with automatic generation of accessor methods,
 * constructor parameters, and {@code equals()}/{@code hashCode()}/{@code toString()} implementations.
 * Each component maintains its type and any annotations applied to it.
 *
 * @see ClassNode
 * @see AnnotatedNode
 * @since 4.0.0
 */
public class RecordComponentNode extends AnnotatedNode {
    private final String name;
    private final ClassNode type;

    /**
     * Creates a record component with the specified name and type.
     *
     * @param declaringClass the {@link ClassNode} that declares this record component
     * @param name the name of the record component (never null)
     * @param type the {@link ClassNode} representing the component's type (never null)
     */
    public RecordComponentNode(ClassNode declaringClass, String name, ClassNode type) {
        this(declaringClass, name, type, Collections.emptyList());
    }

    /**
     * Creates a record component with the specified name, type, and annotations.
     * Annotations are applied to this component in the order provided.
     *
     * @param declaringClass the {@link ClassNode} that declares this record component
     * @param name the name of the record component (never null)
     * @param type the {@link ClassNode} representing the component's type (never null)
     * @param annotations a list of {@link AnnotationNode}s to attach to this component
     */
    public RecordComponentNode(ClassNode declaringClass, String name, ClassNode type, List<AnnotationNode> annotations) {
        this.name = name;
        this.type = type;
        setDeclaringClass(declaringClass);
        for (AnnotationNode annotationNode : annotations) {
            addAnnotation(annotationNode);
        }
    }

    /**
     * Returns the name of this record component.
     *
     * @return the component name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the type of this record component.
     *
     * @return the {@link ClassNode} representing this component's type
     */
    public ClassNode getType() {
        return type;
    }

    /**
     * Compares this record component with another object for equality.
     * Two components are equal if they have the same name and belong to the same declaring class.
     *
     * @param o the object to compare with
     * @return true if the objects represent the same record component
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RecordComponentNode that)) return false;
        return name.equals(that.name) && getDeclaringClass().equals(that.getDeclaringClass());
    }

    /**
     * Returns the hash code for this record component based on its name and declaring class.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(getDeclaringClass(), name);
    }
}
