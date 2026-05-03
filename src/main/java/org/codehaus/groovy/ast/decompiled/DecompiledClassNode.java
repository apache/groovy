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
 * This class bridges the gap between raw bytecode metadata and Groovy's AST representation, providing
 * lazy initialization of all class members to enable efficient runtime class loading and introspection
 * without requiring source code.
 *
 * <h2>Architectural Role</h2>
 * <p>DecompiledClassNode is a deferred factory for AST nodes: it collects bytecode metadata from a
 * {@link ClassStub} and reconstructs full AST nodes (methods, fields, constructors) on demand via
 * lazy proxy classes ({@link LazyFieldNode}, {@link LazyMethodNode}, {@link LazyConstructorNode}).
 * This enables Groovy to compile and introspect compiled classes without full bytecode decompilation upfront.
 *
 * <h2>Key Differences from Regular ClassNode</h2>
 * <ul>
 *   <li><strong>Immutability:</strong> Decompiled classes are read-only representations of bytecode;
 *       modification methods throw {@link UnsupportedOperationException}. See
 *       {@link #setName(String)}, {@link #setRedirect(ClassNode)}, {@link #setUsingGenerics(boolean)},
 *       {@link #setGenericsPlaceHolder(boolean)}.</li>
 *   <li><strong>Always resolved:</strong> {@link #isResolved()} always returns {@code true}.
 *       Unlike source-based classes that transition from unresolved to resolved, decompiled classes
 *       are inherently resolved because their metadata is extracted from compiled bytecode.</li>
 *   <li><strong>Non-primary:</strong> {@code isPrimaryNode = false}. This indicates the class is
 *       derived from compiled bytecode rather than primary source compilation.</li>
 *   <li><strong>Lazy initialization:</strong> Superclass and member information is initialized on first access,
 *       not at construction. This defers expensive metadata parsing until needed.</li>
 *   <li><strong>JVM introspection fallback:</strong> Certain operations (e.g., {@link #isSealed()})
 *       delegate to runtime JVM reflection via {@link #getTypeClass()}.</li>
 * </ul>
 *
 * <h2>Lazy Initialization Strategy</h2>
 * <p>DecompiledClassNode uses a two-phase lazy initialization pattern with double-checked locking:
 * <ul>
 *   <li><strong>Phase 1 - Superclass/Interface metadata:</strong> Triggered by accessors like
 *       {@link #getGenericsTypes()}, {@link #getInterfaces()}, {@link #getAnnotations()}.
 *       {@link #lazyInitSupers()} parses generic signatures and initializes annotations.</li>
 *   <li><strong>Phase 2 - Class members:</strong> Triggered by accessors like {@link #getMethods()},
 *       {@link #getFields()}, {@link #getDeclaredConstructors()}. {@link #lazyInitMembers()} unpacks
 *       method and field stubs into AST nodes.</li>
 * </ul>
 * Private members are wrapped in lazy proxies ({@link LazyFieldNode}, {@link LazyMethodNode},
 * {@link LazyConstructorNode}) to further defer parsing their complex signatures until accessed.
 *
 * <h2>Thread Safety</h2>
 * <p>DecompiledClassNode is thread-safe for lazy initialization. Both {@link #lazyInitSupers()}
 * and {@link #lazyInitMembers()} use double-checked locking with volatile flags to ensure single
 * initialization across multiple threads. The implementation protects initialization with
 * {@code lazyInitLock} (inherited from ClassNode) to serialize critical sections.
 *
 * <h2>Caching Strategy</h2>
 * <p>The ASM decompiler and reference resolver are cached at the DecompiledClassNode level but
 * not across multiple class loads. When the same bytecode is parsed by different
 * {@link AsmDecompiler} instances, separate DecompiledClassNode instances are created.
 * This ensures consistency within a compilation context.
 *
 * <h2>Inner Class Modifiers</h2>
 * <p>For nested classes, the INNERCLASS bytecode attribute (JVMS 4.7.6) provides authoritative
 * access modifiers that correctly reflect nested visibility. {@link #getModifiers(ClassStub)}
 * prefers {@code innerClassModifiers} over the top-level {@code accessModifiers} when available.
 *
 * <h2>Timestamp Extraction</h2>
 * <p>Groovy embeds compilation timestamps in synthetic static field names for change detection.
 * {@link #getCompilationTimeStamp()} decodes this metadata using {@link org.codehaus.groovy.classgen.Verifier#getTimestampFromFieldName(String)}.
 *
 * @see AsmDecompiler#parseClass(java.net.URL)
 * @see AsmReferenceResolver
 * @see ClassStub
 * @see LazyFieldNode
 * @see LazyMethodNode
 * @see LazyConstructorNode
 * @see ClassSignatureParser#configureClass(DecompiledClassNode, ClassStub, AsmReferenceResolver)
 * @see MemberSignatureParser#createFieldNode(FieldStub, AsmReferenceResolver, DecompiledClassNode)
 * @see MemberSignatureParser#createMethodNode(AsmReferenceResolver, MethodStub)
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
     * @throws org.codehaus.groovy.GroovyBugError if the class cannot be resolved at runtime
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

    /**
     * Returns all annotations attached to this class.
     * Triggers lazy initialization of superclass and interface metadata.
     *
     * @return list of {@link AnnotationNode} objects
     */
    @Override
    public List<AnnotationNode> getAnnotations() {
        lazyInitSupers();
        return super.getAnnotations();
    }

    /**
     * Returns annotations of a specific type attached to this class.
     * Triggers lazy initialization of superclass and interface metadata.
     *
     * @param type the annotation type to filter by
     * @return list of matching {@link AnnotationNode} objects
     */
    @Override
    public List<AnnotationNode> getAnnotations(ClassNode type) {
        lazyInitSupers();
        return super.getAnnotations(type);
    }

    /**
     * Returns the generic type parameters of this class.
     * Triggers lazy initialization of generic type information from the class signature.
     *
     * @return array of {@link GenericsType} objects, or empty array if not generic
     */
    @Override
    public GenericsType[] getGenericsTypes() {
        lazyInitSupers();
        return super.getGenericsTypes();
    }

    /**
     * Returns the interfaces implemented by this class.
     * Triggers lazy initialization of interface metadata from the bytecode.
     *
     * @return array of {@link ClassNode} objects representing interfaces
     */
    @Override
    public ClassNode[] getInterfaces() {
        lazyInitSupers();
        return super.getInterfaces();
    }

    /**
     * Returns the record components of this record class (Java 16+).
     * Triggers lazy initialization of superclass and interface metadata.
     *
     * @return list of {@link RecordComponentNode} objects, or empty list if not a record
     */
    @Override
    public List<RecordComponentNode> getRecordComponents() {
        lazyInitSupers();
        return super.getRecordComponents();
    }

    /**
     * Returns the unresolved (not yet redirected) interfaces implemented by this class.
     * Triggers lazy initialization of interface metadata.
     *
     * @param useRedirect whether to follow class redirects (typically false for decompiled classes)
     * @return array of {@link ClassNode} objects representing interfaces
     */
    @Override
    public ClassNode[] getUnresolvedInterfaces(boolean useRedirect) {
        lazyInitSupers();
        return super.getUnresolvedInterfaces(useRedirect);
    }

    /**
     * Returns the unresolved (not yet redirected) superclass of this class.
     * Triggers lazy initialization of superclass metadata.
     *
     * @param useRedirect whether to follow class redirects (typically false for decompiled classes)
     * @return the {@link ClassNode} representing the superclass
     */
    @Override
    public ClassNode getUnresolvedSuperClass(boolean useRedirect) {
        lazyInitSupers();
        return super.getUnresolvedSuperClass(useRedirect);
    }

    /**
     * Indicates whether this class is using generic types.
     * Triggers lazy initialization of generic type information.
     *
     * @return {@code true} if the class uses generic type parameters
     */
    @Override
    public boolean isUsingGenerics() {
        lazyInitSupers();
        return super.isUsingGenerics();
    }

    private volatile boolean supersInitialized;

    /**
     * Lazily initializes class-level metadata including superclass, interfaces, annotations, and generics.
     *
     * <p>This method parses generic type signatures and reconstructs generic type parameters using
     * {@link ClassSignatureParser#configureClass(DecompiledClassNode, ClassStub, AsmReferenceResolver)}.
     * It also attaches annotations extracted from the bytecode via
     * {@link Annotations#addAnnotations(ClassStub, DecompiledClassNode, AsmReferenceResolver)}.
     *
     * <p><strong>Thread Safety:</strong> Uses double-checked locking with {@code supersInitialized} flag
     * and {@code lazyInitLock} to ensure thread-safe single initialization across concurrent accesses.
     * The flag is declared volatile to ensure visibility of initialization completion across threads.
     *
     * <p><strong>Triggered by:</strong> Any accessor that requires superclass or interface information:
     * {@link #getGenericsTypes()}, {@link #getInterfaces()}, {@link #getAnnotations()}, etc.
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

    /**
     * Returns all constructors declared by this class.
     * Triggers lazy initialization of class members.
     *
     * @return list of {@link ConstructorNode} objects
     */
    @Override
    public List<ConstructorNode> getDeclaredConstructors() {
        lazyInitMembers();
        return super.getDeclaredConstructors();
    }

    /**
     * Returns a field declared by this class with the given name.
     * Triggers lazy initialization of class members.
     *
     * @param name the field name
     * @return the {@link FieldNode} if found, or {@code null} if no such field exists
     */
    @Override
    public FieldNode getDeclaredField(final String name) {
        lazyInitMembers();
        return super.getDeclaredField(name);
    }

    /**
     * Returns all methods declared by this class with the given name.
     * Triggers lazy initialization of class members.
     *
     * @param name the method name
     * @return list of {@link MethodNode} objects with the specified name
     */
    @Override
    public List<MethodNode> getDeclaredMethods(final String name) {
        lazyInitMembers();
        return super.getDeclaredMethods(name);
    }

    /**
     * Returns all fields declared by this class (excluding inherited fields).
     * Triggers lazy initialization of class members.
     *
     * @return list of {@link FieldNode} objects
     */
    @Override
    public List<FieldNode> getFields() {
        lazyInitMembers();
        return super.getFields();
    }

    /**
     * Returns all methods and constructors declared by this class (excluding inherited methods).
     * Triggers lazy initialization of class members.
     *
     * @return list of {@link MethodNode} objects
     */
    @Override
    public List<MethodNode> getMethods() {
        lazyInitMembers();
        return super.getMethods();
    }

    private volatile boolean membersInitialized;

    /**
     * Lazily initializes all class members (fields, methods, constructors) on first access.
     *
     * <p>This method reconstructs AST nodes from bytecode stubs by delegating to helper methods
     * {@link #createFieldNode(FieldStub)}, {@link #createMethodNode(MethodStub)}, and
     * {@link #createConstructor(MethodStub)}. Each method adds annotations and resolves types
     * via {@link MemberSignatureParser} and {@link Annotations}.
     *
     * <p><strong>Lazy Proxy Pattern:</strong> Private members are wrapped in lazy proxies
     * ({@link LazyFieldNode}, {@link LazyMethodNode}, {@link LazyConstructorNode}) that defer
     * full initialization until the member is accessed. Public/protected members are fully
     * initialized immediately. This optimization reduces startup time and memory footprint
     * for large classes with many private members.
     *
     * <p><strong>Thread Safety:</strong> Like {@link #lazyInitSupers()}, uses double-checked locking
     * with the {@code membersInitialized} flag and {@code lazyInitLock} to ensure single
     * initialization across concurrent threads.
     *
     * <p><strong>Triggered by:</strong> Any accessor that requires member information:
     * {@link #getMethods()}, {@link #getFields()}, {@link #getDeclaredConstructors()},
     * {@link #getDeclaredMethods(String)}, {@link #getDeclaredField(String)}.
     *
     * @see DecompiledClassNode#createMethodNode(MethodStub)
     * @see DecompiledClassNode#createConstructor(MethodStub)
     * @see DecompiledClassNode#createFieldNode(FieldStub)
     * @see LazyFieldNode
     * @see LazyMethodNode
     * @see LazyConstructorNode
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
