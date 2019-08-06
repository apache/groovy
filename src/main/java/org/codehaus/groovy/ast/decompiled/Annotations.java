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
package org.codehaus.groovy.ast.decompiled;

import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.expr.AnnotationConstantExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.vmplugin.VMPluginFactory;
import org.objectweb.asm.Type;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;

import static org.codehaus.groovy.ast.tools.GeneralUtils.nullX;

class Annotations {
    static AnnotationNode createAnnotationNode(AnnotationStub annotation, AsmReferenceResolver resolver) {
        ClassNode classNode = resolver.resolveClassNullable(Type.getType(annotation.className).getClassName());
        if (classNode == null) {
            // there might be annotations not present in the classpath
            // e.g. java.lang.Synthetic (http://forge.ow2.org/tracker/?aid=307392&group_id=23&atid=100023&func=detail)
            // so skip them
            return null;
        }

        AnnotationNode node = new DecompiledAnnotationNode(classNode);
        for (Map.Entry<String, Object> entry : annotation.members.entrySet()) {
            addMemberIfFound(resolver, node, entry);
        }
        return node;
    }

    private static void addMemberIfFound(AsmReferenceResolver resolver, AnnotationNode node, Map.Entry<String, Object> entry) {
        Expression value = annotationValueToExpression(entry.getValue(), resolver);
        if (value != null) {
            node.addMember(entry.getKey(), value);
        }
    }

    private static Expression annotationValueToExpression(Object value, AsmReferenceResolver resolver) {
        if (value instanceof TypeWrapper) {
            ClassNode type = resolver.resolveClassNullable(Type.getType(((TypeWrapper) value).desc).getClassName());
            return type != null ? new ClassExpression(type) : null;
        }

        if (value instanceof EnumConstantWrapper) {
            EnumConstantWrapper wrapper = (EnumConstantWrapper) value;
            return new PropertyExpression(new ClassExpression(resolver.resolveType(Type.getType(wrapper.enumDesc))), wrapper.constant);
        }

        if (value instanceof AnnotationStub) {
            AnnotationNode annotationNode = createAnnotationNode((AnnotationStub) value, resolver);
            return annotationNode != null ? new AnnotationConstantExpression(annotationNode) : nullX();
        }

        if (value != null && value.getClass().isArray()) {
            ListExpression elementExprs = new ListExpression();
            int len = Array.getLength(value);
            for (int i = 0; i != len; ++i) {
                elementExprs.addExpression(annotationValueToExpression(Array.get(value, i), resolver));
            }
            return elementExprs;
        }

        if (value instanceof List) {
            ListExpression elementExprs = new ListExpression();
            for (Object o : (List) value) {
                elementExprs.addExpression(annotationValueToExpression(o, resolver));
            }
            return elementExprs;
        }

        return new ConstantExpression(value);
    }

    private static class DecompiledAnnotationNode extends AnnotationNode {
        private final Object initLock;
        private volatile boolean lazyInitDone;

        public DecompiledAnnotationNode(ClassNode type) {
            super(type);
            initLock = new Object();
        }

        private void lazyInit() {
            if (lazyInitDone) return;
            synchronized (initLock) {
                if (!lazyInitDone) {
                    for (AnnotationNode annotation : getClassNode().getAnnotations()) {
                        VMPluginFactory.getPlugin().configureAnnotationNodeFromDefinition(annotation, this);
                    }
                    lazyInitDone = true;
                }
            }
        }

        @Override
        public boolean isTargetAllowed(int target) {
            lazyInit();
            return super.isTargetAllowed(target);
        }

        @Override
        public boolean hasRuntimeRetention() {
            lazyInit();
            return super.hasRuntimeRetention();
        }

        @Override
        public boolean hasSourceRetention() {
            lazyInit();
            return super.hasSourceRetention();
        }

        @Override
        public boolean hasClassRetention() {
            lazyInit();
            return super.hasClassRetention();
        }
    }
}
