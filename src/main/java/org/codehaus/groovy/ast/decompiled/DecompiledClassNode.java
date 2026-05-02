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
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.MixinNode;
import org.codehaus.groovy.ast.RecordComponentNode;
import org.codehaus.groovy.classgen.Verifier;
import org.objectweb.asm.Opcodes;

import java.lang.reflect.Modifier;
import java.util.List;
import java.util.function.Supplier;

/**
 * Represents a {@link ClassNode} for classes loaded from compiled bytecode files decompiled using ASM.
 * This class provides lazy initialization of class metadata, interfaces, superclasses, fields, and methods
 * to support efficient runtime class loading and introspection without parsing source code.
 *
 * <p>Decompiled class nodes are marked as non-primary and resolved, enabling Groovy to use them during
 * compilation when source is unavailable. Methods and constructors are wrapped in lazy-loading proxies
 * when they are private, deferring full metadata reconstruction until needed.
 *
 * <p>Class modifiers for inner classes are taken from the INNERCLASS bytecode attribute rather than the
 * top-level access flags, ensuring accurate representation of nested class visibility.
 *
 * @see AsmDecompiler
 * @see AsmReferenceResolver
 * @see DecompiledClassNode#lazyInitSupers()
 * @see DecompiledClassNode#lazyInitMembers()
 */
public class DecompiledClassNode extends ClassNode {

    private final ClassStub classData;
    private final AsmReferenceResolver resolver;

    /**
     * Creates a {@link DecompiledClassNode} from a class stub extracted from bytecode.
     *
     * @param classData the {@link ClassStub} containing bytecode-derived metadata
     * @param resolver the {@link AsmReferenceResolver} used to resolve class references
     */
    public DecompiledClassNode(final ClassStub classData, final AsmReferenceResolver resolver) {
        super(classData.className, getModifiers(classData), null, null, MixinNode.EMPTY_ARRAY);
        this.classData = classData;
        this.resolver = resolver;
        isPrimaryNode = false;
    }

    /**
     * Extracts the correct access modifiers for a class from its bytecode representation.
     * For inner classes, returns the INNERCLASS modifiers which accurately reflect visibility;
     * otherwise returns the top-level class access modifiers.
     *
     * @param classData the {@link ClassStub} containing class metadata
     * @return the appropriate access modifiers including visibility and static flags
     */
    private static int getModifiers(ClassStub classData) {
        return (classData.innerClassModifiers != -1 ? classData.innerClassModifiers : classData.accessModifiers);
    }

    /**
     * Extracts the compilation timestamp from static field metadata if present.
     * Groovy embeds compilation timestamps in synthetic static field names using a special encoding.
     *
     * @return the compilation timestamp in milliseconds, or {@code Long.MAX_VALUE} if not available
     * @see org.codehaus.groovy.classgen.Verifier#getTimestampFromFieldName(String)
     */
    public long getCompilationTimeStamp() {
        if (classData.fields != null) {
            for (FieldStub field : classData.fields) {
                if (Modifier.isStatic(field.accessModifiers)) {
                    Long timestamp = Verifier.getTimestampFromFieldName(field.fieldName);
                    if (timestamp != null) {
                        return timestamp;
                    }
                }
            }
        }
        return Long.MAX_VALUE;
    }

    /**
     * Resolves the runtime JVM {@link Class} object for this decompiled class node.
     *
     * @return the loaded {@link Class} instance from the current classloader
     * @throws GroovyBugError if the class cannot be resolved at runtime
     * @see AsmReferenceResolver#resolveJvmClass(String)
     */
    @Override
    public Class getTypeClass() {
        return resolver.resolveJvmClass(getName());
    }

    /**
     * Determines whether this class has generic type parameters by inspecting the bytecode signature.
     * A parameterized class signature begins with {@code '<'} to indicate type variable declarations.
     *
     * @return {@code true} if the class signature contains generic type parameters
     */
    public boolean isParameterized() {
        return (classData.signature != null && classData.signature.charAt(0) == '<');
    }

    /**
     * Reports that this class node is fully resolved from bytecode.
     * Decompiled classes are always resolved, unlike source-based classes which may be unresolved during compilation.
     *
     * @return always {@code true} for decompiled classes
     */
    @Override
    public boolean isResolved() {
        return true;
    }

    /**
     * Determines if this class is sealed, checking both Java sealed class annotations
     * and Groovy {@code @Sealed} transform annotations.
     *
     * @return {@code true} if the class is declared as sealed with either Java or Groovy mechanisms
     */
    @Override
    public boolean isSealed() {
        // check Groovy "sealed"
        List<AnnotationStub> annotations = classData.annotations;
        if (annotations != null) {
            for (AnnotationStub stub : annotations) {
                if ("groovy.transform.Sealed".equals(stub.className)) {
                    return true;
                }
            }
        }
        // check Java "sealed"
        try {
            return getTypeClass().isSealed();
        } catch (AssertionError | LinkageError ignored) {
        }
        return false;
    }

    /**
     * Prevents renaming of decompiled class nodes, as they represent immutable bytecode definitions.
     *
     * @param name ignored
     * @return never returns normally
     * @throws UnsupportedOperationException always, as bytecode classes are immutable
     */
    @Override
    public String setName(String name) {
        throw new UnsupportedOperationException();
    }

    /**
     * Prevents redirection of decompiled class nodes to alternate implementations.
     *
     * @param cn ignored
     * @throws UnsupportedOperationException always, as bytecode classes cannot be redirected
     */
    @Override
    public void setRedirect(ClassNode cn) {
        throw new UnsupportedOperationException();
    }

    /**
     * Prevents modification of generic type usage for decompiled class nodes.
     *
     * @param b ignored
     * @throws UnsupportedOperationException always, as bytecode classes are immutable
     */
    @Override
    public void setUsingGenerics(boolean b) {
        throw new UnsupportedOperationException();
    }

    /**
     * Prevents marking decompiled class nodes as generic placeholders.
     *
     * @param b ignored
     * @throws UnsupportedOperationException always, as bytecode classes are immutable
     */
    @Override
    public void setGenericsPlaceHolder(boolean b) {
        throw new UnsupportedOperationException();
    }

    //--------------------------------------------------------------------------

    @Override
    public List<AnnotationNode> getAnnotations() {
        lazyInitSupers();
        return super.getAnnotations();
    }

    @Override
    public List<AnnotationNode> getAnnotations(ClassNode type) {
        lazyInitSupers();
        return super.getAnnotations(type);
    }

    @Override
    public GenericsType[] getGenericsTypes() {
        lazyInitSupers();
        return super.getGenericsTypes();
    }

    @Override
    public ClassNode[] getInterfaces() {
        lazyInitSupers();
        return super.getInterfaces();
    }

    @Override
    public List<RecordComponentNode> getRecordComponents() {
        lazyInitSupers();
        return super.getRecordComponents();
    }

    @Override
    public ClassNode[] getUnresolvedInterfaces(boolean useRedirect) {
        lazyInitSupers();
        return super.getUnresolvedInterfaces(useRedirect);
    }

    @Override
    public ClassNode getUnresolvedSuperClass(boolean useRedirect) {
        lazyInitSupers();
        return super.getUnresolvedSuperClass(useRedirect);
    }

    @Override
    public boolean isUsingGenerics() {
        lazyInitSupers();
        return super.isUsingGenerics();
    }

    private volatile boolean supersInitialized;

    /**
     * Lazily initializes class-level metadata including superclass, interfaces, annotations, and generics.
     * This method uses double-checked locking to ensure thread-safe initialization while minimizing synchronization overhead.
     * Initialization is deferred until the metadata is first requested via {@code getGenericsTypes()},
     * {@code getInterfaces()}, or similar accessor methods.
     *
     * @see ClassSignatureParser#configureClass(DecompiledClassNode, ClassStub, AsmReferenceResolver)
     * @see Annotations#addAnnotations(ClassStub, DecompiledClassNode, AsmReferenceResolver)
     */
    private void lazyInitSupers() {
        if (supersInitialized) return;

        synchronized (lazyInitLock) {
            if (!supersInitialized) {
                ClassSignatureParser.configureClass(this, classData, resolver);
                Annotations.addAnnotations(classData, this, resolver);
                supersInitialized = true;
            }
        }
    }

    //--------------------------------------------------------------------------

    @Override
    public List<ConstructorNode> getDeclaredConstructors() {
        lazyInitMembers();
        return super.getDeclaredConstructors();
    }

    @Override
    public FieldNode getDeclaredField(final String name) {
        lazyInitMembers();
        return super.getDeclaredField(name);
    }

    @Override
    public List<MethodNode> getDeclaredMethods(final String name) {
        lazyInitMembers();
        return super.getDeclaredMethods(name);
    }

    @Override
    public List<FieldNode> getFields() {
        lazyInitMembers();
        return super.getFields();
    }

    @Override
    public List<MethodNode> getMethods() {
        lazyInitMembers();
        return super.getMethods();
    }

    private volatile boolean membersInitialized;

    /**
     * Lazily initializes all class members including fields, methods, and constructors.
     * This method reconstructs metadata from the bytecode stubs, wrapping private members in lazy proxies
     * to defer further decompilation until the member is accessed. Like {@code lazyInitSupers()},
     * this uses double-checked locking for thread-safe lazy initialization.
     *
     * @see DecompiledClassNode#createMethodNode(MethodStub)
     * @see DecompiledClassNode#createConstructor(MethodStub)
     * @see DecompiledClassNode#createFieldNode(FieldStub)
     */
    private void lazyInitMembers() {
        if (membersInitialized) return;

        synchronized (lazyInitLock) {
            if (!membersInitialized) {
                if (classData.methods != null) {
                    for (MethodStub method : classData.methods) {
                        if ("<init>".equals(method.methodName)) {
                            addConstructor(createConstructor(method));
                        } else {
                            addMethod(createMethodNode(method));
                        }
                    }
                }

                if (classData.fields != null) {
                    for (FieldStub field : classData.fields) {
                        addField(createFieldNode(field));
                    }
                }

                membersInitialized = true;
            }
        }
    }

    /**
     * Creates a {@link FieldNode} from a bytecode field stub, wrapping private fields in lazy proxies.
     * Publicly visible fields are fully initialized immediately, while private fields defer initialization
     * until first access to improve startup performance for large classes.
     *
     * @param field the {@link FieldStub} containing bytecode field metadata
     * @return a {@link FieldNode} with annotations and resolved types
     * @see LazyFieldNode
     */
    private FieldNode createFieldNode(final FieldStub field) {
        Supplier<FieldNode> fieldNodeSupplier = () -> Annotations.addAnnotations(field, MemberSignatureParser.createFieldNode(field, resolver, this), resolver);

        if ((field.accessModifiers & Opcodes.ACC_PRIVATE) != 0) {
            return new LazyFieldNode(fieldNodeSupplier, field.fieldName);
        }

        return fieldNodeSupplier.get();
    }

    /**
     * Creates a {@link MethodNode} from a bytecode method stub, wrapping private methods in lazy proxies.
     * Publicly visible methods are fully initialized immediately, while private methods defer initialization
     * until first access to reduce memory footprint and startup time.
     *
     * @param method the {@link MethodStub} containing bytecode method metadata
     * @return a {@link MethodNode} with parameter annotations and resolved return/parameter types
     * @see LazyMethodNode
     */
    private MethodNode createMethodNode(final MethodStub method) {
        Supplier<MethodNode> methodNodeSupplier = () -> Annotations.addAnnotations(method, MemberSignatureParser.createMethodNode(resolver, method), resolver);

        if ((method.accessModifiers & Opcodes.ACC_PRIVATE) != 0) {
            return new LazyMethodNode(methodNodeSupplier, method.methodName);
        }

        return methodNodeSupplier.get();
    }

    /**
     * Creates a {@link ConstructorNode} from a bytecode constructor stub, wrapping private constructors in lazy proxies.
     *
     * @param method the {@link MethodStub} with method name {@code "<init>"} containing bytecode constructor metadata
     * @return a {@link ConstructorNode} with parameter annotations and resolved parameter types
     * @see LazyConstructorNode
     */
    private ConstructorNode createConstructor(final MethodStub method) {
        Supplier<ConstructorNode> constructorNodeSupplier = () -> (ConstructorNode) Annotations.addAnnotations(method, MemberSignatureParser.createMethodNode(resolver, method), resolver);

        if ((method.accessModifiers & Opcodes.ACC_PRIVATE) != 0) {
            return new LazyConstructorNode(constructorNodeSupplier);
        }

        return constructorNodeSupplier.get();
    }
}
