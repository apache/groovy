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

import org.codehaus.groovy.ast.AnnotatedNode;
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

/**
 * Utility class that converts annotation metadata extracted from bytecode into corresponding AST node representations.
 * Handles JSR 175 (annotation) and JSR 308 (type annotation) conversions from ASM-parsed structures
 * to Groovy AST AnnotationNode instances.
 *
 * <p>Key responsibilities:
 * <ul>
 *   <li>Converting {@link AnnotationStub} objects (bytecode metadata) to {@link AnnotationNode} instances</li>
 *   <li>Transforming annotation element values (primitives, enums, nested annotations, arrays) to AST expressions</li>
 *   <li>Applying annotations to {@link AnnotatedNode} and type annotations to {@link ClassNode}</li>
 *   <li>Lazy initialization of annotation metadata including retention policy discovery</li>
 * </ul>
 *
 * <p>The conversion handles various annotation value types according to JVMS §4.7.16-4.7.21:
 * <ul>
 *   <li>Primitive constants (int, boolean, String, etc.) → {@link ConstantExpression}</li>
 *   <li>Class types → {@link ClassExpression}</li>
 *   <li>Enum constants → {@link PropertyExpression}</li>
 *   <li>Nested annotations → {@link AnnotationConstantExpression}</li>
 *   <li>Arrays and collections → {@link ListExpression}</li>
 * </ul>
 *
 * <p>Inner class {@link DecompiledAnnotationNode} implements lazy initialization of retention policy
 * and target annotations using double-checked locking, deferring VM plugin configuration until needed.
 *
 * @see AnnotationStub
 * @see AnnotatedStub
 * @see AnnotatedTypeStub
 * @see AsmReferenceResolver
 */
class Annotations {
    /**
     * Creates an {@link AnnotationNode} from bytecode annotation metadata.
     * Resolves the annotation class using the provided resolver and converts all annotation members
     * to corresponding AST expressions. Returns null if the annotation class cannot be resolved
     * (e.g., not present on classpath).
     *
     * <p>If resolution fails, the annotation is silently skipped to allow compilation with missing
     * annotation types (e.g., runtime-only annotations not in classpath).
     *
     * @param annotation the {@link AnnotationStub} containing bytecode annotation metadata
     * @param resolver the {@link AsmReferenceResolver} used to resolve the annotation class type
     * @return the created {@link AnnotationNode}, or null if the annotation class cannot be resolved
     * @see DecompiledAnnotationNode
     */
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

    /**
     * Adds a single annotation member to the given {@link AnnotationNode} if the member value
     * can be successfully converted to an AST expression. Silently skips members with unconvertible values.
     *
     * @param resolver the {@link AsmReferenceResolver} used for type resolution
     * @param node the {@link AnnotationNode} to add the member to
     * @param entry the key-value pair from annotation member map (name → value)
     */
    private static void addMemberIfFound(AsmReferenceResolver resolver, AnnotationNode node, Map.Entry<String, Object> entry) {
        Expression value = annotationValueToExpression(entry.getValue(), resolver);
        if (value != null) {
            node.addMember(entry.getKey(), value);
        }
    }

    /**
     * Converts a bytecode annotation element value to a corresponding AST expression.
     * Handles all annotation value types according to JVMS §4.7.16:
     * <ul>
     *   <li>{@link TypeWrapper} → {@link ClassExpression}</li>
     *   <li>{@link EnumConstantWrapper} → {@link PropertyExpression} for enum constant access</li>
     *   <li>{@link AnnotationStub} → {@link AnnotationConstantExpression} for nested annotations</li>
     *   <li>Arrays (via {@link Array.getLength()}) → {@link ListExpression} with recursive conversion</li>
     *   <li>{@link List} → {@link ListExpression} with recursive conversion</li>
     *   <li>Primitives and strings → {@link ConstantExpression}</li>
     * </ul>
     *
     * <p>Returns null for type references that cannot be resolved, allowing compilation to continue
     * with partially-resolved annotations.
     *
     * @param value the annotation element value extracted from bytecode
     * @param resolver the {@link AsmReferenceResolver} used to resolve types and class references
     * @return the corresponding AST expression, or null if the value cannot be converted
     */
    private static Expression annotationValueToExpression(Object value, AsmReferenceResolver resolver) {
        if (value instanceof TypeWrapper) {
            ClassNode type = resolver.resolveClassNullable(Type.getType(((TypeWrapper) value).desc).getClassName());
            return type != null ? new ClassExpression(type) : null;
        }

        if (value instanceof EnumConstantWrapper wrapper) {
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

    /**
     * Applies bytecode annotations to an {@link AnnotatedNode} by converting each {@link AnnotationStub}
     * to an {@link AnnotationNode} and attaching it to the target node.
     *
     * <p>This method handles missing annotation classes gracefully - annotations whose types cannot be
     * resolved are silently skipped, allowing compilation to continue even when some annotations are
     * not present on the classpath.
     *
     * @param <T> the annotated node type parameter
     * @param stub the {@link AnnotatedStub} containing bytecode annotations
     * @param node the target {@link AnnotatedNode} to attach annotations to
     * @param resolver the {@link AsmReferenceResolver} used to resolve annotation class types
     * @return the input node with annotations applied (for method chaining)
     * @see ClassNode
     * @see FieldNode
     * @see MethodNode
     */
    static <T extends AnnotatedNode> T addAnnotations(AnnotatedStub stub, T node, AsmReferenceResolver resolver) {
        List<AnnotationStub> annotations = stub.getAnnotations();
        if (annotations != null) {
            for (AnnotationStub annotation : annotations) {
                AnnotationNode annotationNode = createAnnotationNode(annotation, resolver);
                if (annotationNode != null) {
                    node.addAnnotation(annotationNode);
                }
            }
        }
        return node;
    }

    /**
     * Applies type annotations (JSR 308) extracted from bytecode to a {@link ClassNode}.
     * Type annotations provide runtime metadata about generic type usages and are stored separately
     * from declaration annotations according to JVMS §4.7.20-4.7.21.
     *
     * <p>Like regular annotations, type annotations whose classes cannot be resolved are silently skipped.
     *
     * @param <T> the class node type parameter
     * @param stub the {@link AnnotatedTypeStub} containing bytecode type annotations
     * @param node the target {@link ClassNode} to attach type annotations to
     * @param resolver the {@link AsmReferenceResolver} used to resolve annotation class types
     * @return the input node with type annotations applied (for method chaining)
     * @see org.codehaus.groovy.ast.ClassNode#addTypeAnnotation(AnnotationNode)
     */
    static <T extends ClassNode> T addTypeAnnotations(AnnotatedTypeStub stub, T node, AsmReferenceResolver resolver) {
        List<TypeAnnotationStub> annotations = stub.getTypeAnnotations();
        if (annotations != null) {
            for (TypeAnnotationStub annotation : annotations) {
                AnnotationNode annotationNode = createAnnotationNode(annotation, resolver);
                if (annotationNode != null) {
                    node.addTypeAnnotation(annotationNode);
                }
            }
        }
        return node;
    }

    /**
     * Internal {@link AnnotationNode} subclass that lazily initializes retention policy and target annotations
     * using double-checked locking pattern. This avoids performing VM plugin configuration during initial
     * annotation parsing, deferring it until retention/target checks are needed.
     *
     * <p>The lazy initialization calls {@link org.codehaus.groovy.vmplugin.VMPluginFactory#getPlugin()}
     * to configure annotation metadata from the annotation's class definition annotations.
     */
    private static class DecompiledAnnotationNode extends AnnotationNode {
        private final Object initLock;
        private volatile boolean lazyInitDone;

        public DecompiledAnnotationNode(ClassNode type) {
            super(type);
            initLock = new Object();
        }

        /**
         * Initializes this annotation node by invoking the VM plugin to configure retention policy,
         * target restrictions, and other metadata from the annotation class definition.
         * Uses double-checked locking to ensure thread-safe single initialization.
         */
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

        /**
         * Checks if the specified target type is allowed for this annotation.
         * Target information is extracted from the annotation class's @Target meta-annotation.
         *
         * @param target the target element type constant (ElementType enum value)
         * @return true if this annotation can be applied to the target type
         */
        @Override
        public boolean isTargetAllowed(final int target) {
            return super.isTargetAllowed(target);
        }

        /**
         * Determines whether this annotation has runtime retention.
         * Triggers lazy initialization to extract retention policy from the annotation class definition.
         *
         * @return true if this annotation is retained at runtime (RetentionPolicy.RUNTIME)
         */
        @Override
        public boolean hasRuntimeRetention() {
            lazyInit();
            return super.hasRuntimeRetention();
        }

        /**
         * Determines whether this annotation has source retention.
         * Triggers lazy initialization to extract retention policy from the annotation class definition.
         *
         * @return true if this annotation is retained in source code (RetentionPolicy.SOURCE)
         */
        @Override
        public boolean hasSourceRetention() {
            lazyInit();
            return super.hasSourceRetention();
        }

        /**
         * Determines whether this annotation has class retention.
         * Triggers lazy initialization to extract retention policy from the annotation class definition.
         *
         * @return true if this annotation is retained in the compiled class file (RetentionPolicy.CLASS)
         */
        @Override
        public boolean hasClassRetention() {
            lazyInit();
            return super.hasClassRetention();
        }
    }
}
