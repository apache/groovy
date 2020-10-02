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

import groovy.transform.VisibilityOptions;
import groovy.transform.options.Visibility;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.PropertyExpression;

import java.util.List;
import java.util.Map;

import static org.codehaus.groovy.ast.ClassHelper.makeWithoutCaching;
import static org.codehaus.groovy.transform.AbstractASTTransformation.getMemberStringValue;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PROTECTED;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;

public class VisibilityUtils {
    private static final ClassNode VISIBILITY_OPTIONS_TYPE = makeWithoutCaching(VisibilityOptions.class, false);

    private VisibilityUtils() { }

    /**
     * Determine the correct modifiers by looking for a potential @VisibilityOptions annotation.
     *
     * @param anno The annotation being processed (if any) which may support a 'visibilityId' attribute
     * @param node The node being processed which may also be annotated with @VisibilityOptions
     * @param clazz The type of node being constructed
     * @param originalModifiers The modifier value to adjust or return if no applicable @VisibilityOptions is found
     * @return the updated modifiers
     */
    public static int getVisibility(final AnnotationNode anno, final AnnotatedNode node, final Class<? extends AnnotatedNode> clazz, final int originalModifiers) {
        List<AnnotationNode> annotations = node.getAnnotations(VISIBILITY_OPTIONS_TYPE);
        if (annotations.isEmpty() || anno == null) return originalModifiers;

        String visId = getMemberStringValue(anno, "visibilityId", null);

        Visibility vis = null;
        if (visId == null) {
            vis = getVisForAnnotation(clazz, annotations.get(0), null);
        } else {
            for (AnnotationNode visAnno : annotations) {
                vis = getVisForAnnotation(clazz, visAnno, visId);
                if (vis != Visibility.UNDEFINED) break;
            }
        }
        if (vis == null || vis == Visibility.UNDEFINED) return originalModifiers;

        int result = originalModifiers & ~(ACC_PUBLIC | ACC_PROTECTED | ACC_PRIVATE);
        return result | vis.getModifier();
    }

    private static Visibility getVisForAnnotation(final Class<? extends AnnotatedNode> clazz, final AnnotationNode visAnno, final String visId) {
        Map<String, Expression> visMembers = visAnno.getMembers();
        if (visMembers == null) return Visibility.UNDEFINED;
        String id = getMemberStringValue(visAnno, "id", null);
        if ((id == null && visId != null) || (id != null && !id.equals(visId))) return Visibility.UNDEFINED;

        Visibility vis = null;
        if (clazz.equals(ConstructorNode.class)) {
            vis = getVisibility(visMembers.get("constructor"));
        } else if (clazz.equals(MethodNode.class)) {
            vis = getVisibility(visMembers.get("method"));
        } else if (clazz.equals(ClassNode.class)) {
            vis = getVisibility(visMembers.get("type"));
        }
        if (vis == null || vis == Visibility.UNDEFINED) {
            vis = getVisibility(visMembers.get("value"));
        }
        return vis;
    }

    private static Visibility getVisibility(final Expression e) {
        if (e instanceof PropertyExpression) {
            PropertyExpression pe = (PropertyExpression) e;
            if (pe.getObjectExpression() instanceof ClassExpression && pe.getObjectExpression().getText().equals("groovy.transform.options.Visibility")) {
                return Visibility.valueOf(pe.getPropertyAsString());
            }
        }
        return Visibility.UNDEFINED;
    }
}
