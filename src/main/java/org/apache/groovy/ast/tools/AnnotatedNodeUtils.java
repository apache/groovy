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
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;

import java.util.List;

/**
 * Utility class for working with AnnotatedNodes
 */
public class AnnotatedNodeUtils {
    private static final ClassNode GENERATED_TYPE = ClassHelper.make(Generated.class);

    private AnnotatedNodeUtils() {
    }

    public static <T extends AnnotatedNode> T markAsGenerated(ClassNode containingClass, T nodeToMark) {
        boolean shouldAnnotate = containingClass.getModule() != null && containingClass.getModule().getContext() != null;
        if (shouldAnnotate && !hasAnnotation(nodeToMark, GENERATED_TYPE)) {
            nodeToMark.addAnnotation(new AnnotationNode(GENERATED_TYPE));
        }
        return nodeToMark;
    }

    public static boolean hasAnnotation(AnnotatedNode node, ClassNode annotation) {
        List annots = node.getAnnotations(annotation);
        return (annots != null && !annots.isEmpty());
    }
}
