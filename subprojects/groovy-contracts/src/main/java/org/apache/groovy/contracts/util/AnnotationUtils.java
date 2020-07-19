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
package org.apache.groovy.contracts.util;

import org.apache.groovy.contracts.generation.CandidateChecks;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>Helper methods for reading/getting {@link org.codehaus.groovy.ast.AnnotationNode} instances.</p>
 */
public class AnnotationUtils {

    /**
     * Checks whether the given {@link org.codehaus.groovy.ast.ClassNode} is annotated
     * with an annotations of the given package or full annotatedNode name.
     *
     * @param annotatedNode     the {@link org.codehaus.groovy.ast.AnnotatedNode} to search for the given annotation
     * @param typeOrPackageName can either be a part of the package or the complete annotation class name
     * @return <tt>true</tt> if an annotation was found, <tt>false</tt> otherwise
     */
    public static boolean hasAnnotationOfType(AnnotatedNode annotatedNode, String typeOrPackageName) {
        for (AnnotationNode annotation : annotatedNode.getAnnotations()) {
            if (annotation.getClassNode().getName().startsWith(typeOrPackageName)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Gets the next {@link org.codehaus.groovy.ast.AnnotationNode} instance in the inheritance line which is annotated
     * with the given Annotation class <tt>anno</tt>.
     *
     * @param type the {@link org.codehaus.groovy.ast.ClassNode} to check for the annotation
     * @param anno the annotation to watch out for
     * @return the next {@link org.codehaus.groovy.ast.AnnotationNode} in the inheritance line, or <tt>null</tt>
     */
    public static List<AnnotationNode> getAnnotationNodeInHierarchyWithMetaAnnotation(ClassNode type, ClassNode anno) {
        List<AnnotationNode> result = new ArrayList<AnnotationNode>();
        for (AnnotationNode annotation : type.getAnnotations()) {
            if (annotation.getClassNode().getAnnotations(anno).size() > 0) {
                result.add(annotation);
            }
        }

        if (result.isEmpty() && type.getSuperClass() != null) {
            return getAnnotationNodeInHierarchyWithMetaAnnotation(type.getSuperClass(), anno);
        } else {
            return result;
        }
    }

    /**
     * <p>Checks whether there exists a {@link MethodNode} up the inheritance tree where exists an annotation which is annotated
     * with <tt>metaAnnotationClassNode</tt>.</p>
     *
     * @param type                    the origin {@link ClassNode}
     * @param originMethodNode        the origin {@link MethodNode}
     * @param metaAnnotationClassNode the {@link ClassNode} of the meta-annotation
     * @return a list of {@link AnnotationNode} all annotated with <tt>metaAnnotationClassNode</tt>
     */
    public static List<AnnotationNode> getAnnotationNodeInHierarchyWithMetaAnnotation(ClassNode type, MethodNode originMethodNode, ClassNode metaAnnotationClassNode) {
        List<AnnotationNode> result = new ArrayList<AnnotationNode>();

        while (type != null) {
            MethodNode methodNode = type.getMethod(originMethodNode.getName(), originMethodNode.getParameters());
            if (methodNode != null) {
                for (AnnotationNode annotation : methodNode.getAnnotations()) {
                    if (annotation.getClassNode().getAnnotations(metaAnnotationClassNode).size() > 0) {
                        result.add(annotation);
                    }
                }

                if (result.size() > 0) return result;
            }

            type = type.getSuperClass();
        }

        return result;
    }

    /**
     * Loads all annotation nodes of the given {@link org.codehaus.groovy.ast.AnnotatedNode} instance which are marked
     * with the annotation <tt>metaAnnotationClassName</tt>.
     *
     * @param annotatedNode           an {@link org.codehaus.groovy.ast.AnnotatedNode} from which the annotations are checked
     * @param metaAnnotationClassName the name of the meta annotation
     * @return a list of {@link AnnotationNode} instances which implement the given <tt>metaAnnotationClass</tt>
     */
    public static List<AnnotationNode> hasMetaAnnotations(AnnotatedNode annotatedNode, String metaAnnotationClassName) {

        ArrayList<AnnotationNode> result = new ArrayList<AnnotationNode>();

        for (AnnotationNode annotationNode : annotatedNode.getAnnotations()) {
            if (CandidateChecks.isRuntimeClass(annotationNode.getClassNode())) continue;

            // is the annotation marked with the given meta annotation?
            List<AnnotationNode> metaAnnotations = annotationNode.getClassNode().getAnnotations(ClassHelper.makeWithoutCaching(metaAnnotationClassName));
            if (metaAnnotations.isEmpty()) {
                metaAnnotations = hasMetaAnnotations(annotationNode.getClassNode(), metaAnnotationClassName);
            }

            if (metaAnnotations.size() > 0) result.add(annotationNode);
        }
        return result;
    }
}
