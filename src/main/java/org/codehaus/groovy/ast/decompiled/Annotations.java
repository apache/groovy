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
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.expr.AnnotationConstantExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.vmplugin.VMPluginFactory;
import org.objectweb.asm.Type;
import org.objectweb.asm.TypePath;
import org.objectweb.asm.TypeReference;

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
     * Applies the type annotations (JSR 308) targeting the given position to the given type,
     * walking each annotation's type path (JVMS §4.7.20.2) to locate the exact annotated type
     * within the possibly generic or array type.
     *
     * <p>The returned node may differ from the input node: type annotations may only be attached
     * to per-use redirect nodes, so shared (cached) nodes are replaced by an annotated
     * {@link ClassNode#getPlainNodeReference(boolean) plain node reference} when required.
     * A type annotation lands in {@link ClassNode#getTypeAnnotations()} of the per-use node at
     * the exact JVMS position: an empty type path denotes the outermost type at the given site,
     * so for {@code String @A []} the annotation ends up on the array node while for
     * {@code @A String []} (type path "[") it ends up on the component node.
     *
     * <p>Like regular annotations, type annotations whose classes cannot be resolved are silently
     * skipped, as are annotations whose position cannot be mapped onto the AST (e.g. type
     * arguments of a raw type use).
     *
     * @param stubs the type annotations of the enclosing class or member, or {@code null}
     * @param sort the {@link TypeReference} sort identifying the targeted position
     * @param index the position index (formal parameter, exception, supertype or type parameter
     *        index) or {@code -1} for positions without an index (fields, method return types,
     *        the superclass)
     * @param type the resolved type at that position
     * @param resolver the {@link AsmReferenceResolver} used to resolve annotation class types
     * @return the annotated type: either the input node or a per-use replacement for it
     * @see ClassNode#addTypeAnnotation(AnnotationNode)
     */
    static ClassNode applyTypeAnnotations(List<TypeAnnotationStub> stubs, int sort, int index, ClassNode type, AsmReferenceResolver resolver) {
        if (stubs != null) {
            for (TypeAnnotationStub stub : stubs) {
                TypeReference reference = new TypeReference(stub.typeRef);
                if (reference.getSort() != sort || positionIndex(reference) != index) continue;
                AnnotationNode annotationNode = createAnnotationNode(stub, resolver);
                if (annotationNode != null) {
                    TypePath typePath = stub.typePath == null ? null : TypePath.fromString(stub.typePath);
                    type = attach(type, typePath, 0, annotationNode);
                }
            }
        }
        return type;
    }

    /**
     * Extracts the position index from a type reference for sorts that carry one,
     * mirroring the {@code index} parameter of
     * {@link #applyTypeAnnotations(List, int, int, ClassNode, AsmReferenceResolver)}.
     *
     * @param reference the type reference
     * @return the formal parameter, exception, supertype ({@code -1} for the superclass) or
     *         type parameter index, or {@code -1} for sorts without an index
     */
    private static int positionIndex(TypeReference reference) {
        switch (reference.getSort()) {
            case TypeReference.CLASS_EXTENDS:
                return reference.getSuperTypeIndex();
            case TypeReference.METHOD_FORMAL_PARAMETER:
                return reference.getFormalParameterIndex();
            case TypeReference.THROWS:
                return reference.getExceptionIndex();
            case TypeReference.CLASS_TYPE_PARAMETER:
            case TypeReference.METHOD_TYPE_PARAMETER:
                return reference.getTypeParameterIndex();
            default:
                return -1;
        }
    }

    /**
     * Walks the remaining type path steps into the structure of the given type and attaches
     * the annotation to the addressed type. Steps that cannot be mapped onto the AST cause
     * the annotation to be dropped silently, so that reading arbitrary bytecode never fails.
     *
     * @param type the type to walk into
     * @param typePath the type path, or {@code null} for the type itself
     * @param step the current step within the type path
     * @param annotation the annotation to attach
     * @return the annotated type: either the input node or a per-use replacement for it
     */
    private static ClassNode attach(ClassNode type, TypePath typePath, int step, AnnotationNode annotation) {
        if (typePath == null || step >= typePath.getLength()) {
            return annotate(type, annotation);
        }
        switch (typePath.getStep(step)) {
          case TypePath.ARRAY_ELEMENT: {
            if (!type.isArray()) return type;
            ClassNode componentType = type.getComponentType();
            ClassNode newComponentType = attach(componentType, typePath, step + 1, annotation);
            if (newComponentType == componentType) return type;
            ClassNode newType = newComponentType.makeArray();
            for (AnnotationNode existing : type.getTypeAnnotations()) {
                newType = annotate(newType, existing);
            }
            return newType;
          }
          case TypePath.TYPE_ARGUMENT: {
            GenericsType[] genericsTypes = type.getGenericsTypes();
            int i = typePath.getStepArgument(step);
            if (genericsTypes == null || i >= genericsTypes.length) return type; // raw type use
            GenericsType genericsType = genericsTypes[i];
            if (step + 1 < typePath.getLength() && typePath.getStep(step + 1) == TypePath.WILDCARD_BOUND) {
                ClassNode[] upperBounds = genericsType.getUpperBounds();
                ClassNode lowerBound = genericsType.getLowerBound();
                if (upperBounds != null && upperBounds.length > 0) {
                    upperBounds[0] = attach(upperBounds[0], typePath, step + 2, annotation);
                } else if (lowerBound != null) {
                    ClassNode newLowerBound = attach(lowerBound, typePath, step + 2, annotation);
                    if (newLowerBound != lowerBound) {
                        GenericsType newGenericsType = new GenericsType(genericsType.getType(), null, newLowerBound);
                        newGenericsType.setWildcard(genericsType.isWildcard());
                        genericsTypes[i] = newGenericsType;
                    }
                }
            } else {
                genericsType.setType(attach(genericsType.getType(), typePath, step + 1, annotation));
            }
            return type;
          }
          case TypePath.INNER_TYPE:
            // Groovy models a nested type use with a single ClassNode, so annotations on
            // the nested type are treated as annotations on the whole type
            return attach(type, typePath, step + 1, annotation);
          default: // WILDCARD_BOUND without a preceding TYPE_ARGUMENT step: malformed
            return type;
        }
    }

    /**
     * Attaches the annotation to the given type, first substituting a per-use
     * {@link ClassNode#getPlainNodeReference(boolean) plain node reference} if the type is a
     * shared node: type annotations belong to a single use of a type and must never be attached
     * to the globally shared (cached or resolved) nodes; see
     * {@link ClassNode#addTypeAnnotation(AnnotationNode)}.
     *
     * @param type the type to annotate
     * @param annotation the annotation to attach
     * @return the annotated type: either the input node or a per-use replacement for it
     */
    private static ClassNode annotate(ClassNode type, AnnotationNode annotation) {
        if (!type.isRedirectNode()) {
            type = type.getPlainNodeReference(false);
        }
        type.addTypeAnnotation(annotation);
        return type;
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
