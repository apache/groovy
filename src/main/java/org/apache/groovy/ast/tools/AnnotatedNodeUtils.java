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
package org.apache.groovy.ast.tools;

import groovy.transform.Generated;
import groovy.transform.Internal;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.PropertyNode;

import java.util.List;

/**
 * Utility class for working with AnnotatedNodes
 */
public class AnnotatedNodeUtils {
    private static final ClassNode GENERATED_TYPE = ClassHelper.make(Generated.class);
    private static final ClassNode INTERNAL_TYPE = ClassHelper.make(Internal.class);

    private AnnotatedNodeUtils() { }

    /**
     * Marks the supplied node with {@link Generated @Generated} when generation metadata is available.
     *
     * @param containingClass the class currently receiving generated members
     * @param nodeToMark the node to annotate
     * @return the supplied node
     */
    public static <T extends AnnotatedNode> T markAsGenerated(final ClassNode containingClass, final T nodeToMark) {
        return markAsGenerated(containingClass, nodeToMark, false);
    }

    /**
     * Marks the supplied node with {@link Generated @Generated}.
     *
     * @param containingClass the class currently receiving generated members
     * @param nodeToMark the node to annotate
     * @param skipChecks whether to skip the usual source-context checks
     * @return the supplied node
     */
    public static <T extends AnnotatedNode> T markAsGenerated(final ClassNode containingClass, final T nodeToMark, final boolean skipChecks) {
        boolean shouldAnnotate = skipChecks || (containingClass.getModule() != null && containingClass.getModule().getContext() != null);
        if (shouldAnnotate && !isGenerated(nodeToMark)) {
            nodeToMark.addAnnotation(new AnnotationNode(GENERATED_TYPE));
        }
        return nodeToMark;
    }

    /**
     * Checks whether the supplied node carries the given annotation.
     *
     * @param node the node to inspect
     * @param annotation the annotation type to look for
     * @return {@code true} if the node has at least one matching annotation
     */
    public static boolean hasAnnotation(final AnnotatedNode node, final ClassNode annotation) {
        List<?> annots = node.getAnnotations(annotation);
        return (annots != null && !annots.isEmpty());
    }

    /**
     * Checks whether the supplied node has been marked as generated.
     *
     * @param node the node to inspect
     * @return {@code true} if the node carries {@link Generated @Generated}
     */
    public static boolean isGenerated(final AnnotatedNode node) {
        return hasAnnotation(node, GENERATED_TYPE);
    }

    /**
     * Marks a node with the {@link Internal @Internal} annotation.
     *
     * @since 6.0.0
     */
    public static <T extends AnnotatedNode> T markAsInternal(final T nodeToMark) {
        if (!isInternal(nodeToMark)) {
            nodeToMark.addAnnotation(new AnnotationNode(INTERNAL_TYPE));
        }
        return nodeToMark;
    }

    /**
     * Checks whether a node is annotated with {@link Internal @Internal}.
     *
     * @since 6.0.0
     */
    public static boolean isInternal(final AnnotatedNode node) {
        return hasAnnotation(node, INTERNAL_TYPE);
    }

    /**
     * Checks whether an AST node is deemed internal, either by name convention
     * (contains {@code $}) or by being annotated with {@link Internal @Internal}.
     *
     * @since 6.0.0
     */
    public static boolean deemedInternal(final AnnotatedNode node) {
        if (isInternal(node)) return true;
        String name;
        if (node instanceof FieldNode fn) {
            name = fn.getName();
        } else if (node instanceof PropertyNode pn) {
            name = pn.getName();
        } else if (node instanceof MethodNode mn) {
            name = mn.getName();
        } else {
            return false;
        }
        return name.contains("$");
    }
}
