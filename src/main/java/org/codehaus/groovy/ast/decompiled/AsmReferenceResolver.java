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

import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.control.ClassNodeResolver;
import org.codehaus.groovy.control.CompilationUnit;
import org.objectweb.asm.Type;

/**
 * Helper class for resolving class references encountered in bytecode stubs decompiled from compiled classes.
 * Bridges between ASM's internal type descriptors/names and Groovy's {@link ClassNode} representation
 * by consulting both classes currently being compiled in the compilation unit and already-loaded classes
 * via the {@link ClassNodeResolver}.
 *
 * <h2>Resolution Strategy</h2>
 * <p>AsmReferenceResolver implements a two-tier lookup strategy:
 * <ol>
 *   <li><strong>Compilation-time classes:</strong> First checks the compilation unit's AST for classes
 *       being compiled in the same batch. This ensures references to companion classes resolve correctly.</li>
 *   <li><strong>Runtime classes:</strong> If not found in the compilation unit, delegates to the
 *       {@link ClassNodeResolver}, which consults already-compiled and third-party classes.</li>
 * </ol>
 *
 * <h2>Type Descriptor Handling</h2>
 * <p>The resolver handles multiple bytecode formats:
 * <ul>
 *   <li><strong>Primitive types:</strong> Resolved via {@link org.codehaus.groovy.ast.ClassHelper#make(String)}
 *       (e.g., {@code "I"} → {@code int.class}).</li>
 *   <li><strong>Object types:</strong> Converted from JVMS internal names (e.g., {@code "java/lang/String"})
 *       to dot notation and resolved via {@link #resolveClass(String)}.</li>
 *   <li><strong>Array types:</strong> Handled recursively by {@link #resolveType(Type)} and marked
 *       via {@link ClassNode#makeArray()}.</li>
 * </ul>
 *
 * <h2>Error Handling</h2>
 * <p>The resolver distinguishes between two resolution modes:
 * <ul>
 *   <li>{@link #resolveClass(String)} - Throws {@link NoClassDefFoundError} if the class cannot be resolved.
 *       Used when a type is required.</li>
 *   <li>{@link #resolveClassNullable(String)} - Returns {@code null} if the class cannot be resolved.
 *       Used for optional type information (e.g., exception types).</li>
 * </ul>
 *
 * <h2>Thread Safety</h2>
 * <p>AsmReferenceResolver instances are typically created once per compilation unit and are
 * thread-safe for concurrent read access. The underlying {@link ClassNodeResolver} and
 * {@link CompilationUnit} manage their own synchronization.
 *
 * @see DecompiledClassNode
 * @see AsmDecompiler
 * @see ClassNodeResolver
 * @see CompilationUnit
 */
public class AsmReferenceResolver {
    /**
     * The resolver responsible for locating already-compiled and third-party classes.
     */
    private final ClassNodeResolver resolver;

    /**
     * The compilation unit containing classes being compiled and compilation context metadata.
     * Consulted first for classes in the current batch before falling back to {@code resolver}.
     */
    private final CompilationUnit unit;

    /**
     * Creates an ASM reference resolver for a compilation unit.
     *
     * @param resolver the {@link ClassNodeResolver} used to resolve class names
     * @param unit the {@link CompilationUnit} containing classes being compiled and metadata
     */
    public AsmReferenceResolver(final ClassNodeResolver resolver, final CompilationUnit unit) {
        this.resolver = resolver;
        this.unit = unit;
    }

    /**
     * Resolves a fully qualified class name to a {@link ClassNode}.
     * First checks classes being compiled in this unit, then consults the resolver for already-loaded classes.
     *
     * @param className the fully qualified class name
     * @return the resolved {@link ClassNode}
     * @throws NoClassDefFoundError if the class cannot be resolved
     */
    public ClassNode resolveClass(final String className) {
        ClassNode classNode = resolveClassNullable(className);
        if (classNode == null) {
            throw new NoClassDefFoundError(className);
        }
        return classNode;
    }

    /**
     * Attempts to resolve a fully qualified class name to a {@link ClassNode}, returning {@code null} if not found.
     * First checks classes being compiled in this unit, then consults the resolver for already-loaded classes.
     *
     * @param className the fully qualified class name
     * @return the resolved {@link ClassNode}, or {@code null} if not resolvable
     */
    public ClassNode resolveClassNullable(final String className) {
        ClassNode beingCompiled = unit.getAST().getClass(className);
        if (beingCompiled != null) {
            return beingCompiled;
        }

        ClassNodeResolver.LookupResult lookupResult = resolver.resolveName(className, unit);
        return lookupResult != null ? lookupResult.getClassNode() : null;
    }

    /**
     * Resolves an ASM {@link Type} to a {@link ClassNode}, handling array types by wrapping element types.
     *
     * @param type the ASM type to resolve
     * @return the corresponding {@link ClassNode}, with dimensions for array types
     * @throws NoClassDefFoundError if an object type cannot be resolved
     */
    public ClassNode resolveType(final Type type) {
        if (type.getSort() == Type.ARRAY) {
            ClassNode result = resolveNonArrayType(type.getElementType());
            for (int n = type.getDimensions(); n > 0; n -= 1) {
                result = result.makeArray();
            }
            return result;
        }

        return resolveNonArrayType(type);
    }

    /**
     * Resolves a non-array ASM type to a {@link ClassNode}.
     * Primitive types are wrapped using {@link ClassHelper#make(String)};
     * object types are resolved via {@link AsmReferenceResolver#resolveClass(String)}.
     *
     * @param type the ASM type (non-array)
     * @return the corresponding {@link ClassNode}
     * @throws NoClassDefFoundError if an object type cannot be resolved
     */
    private ClassNode resolveNonArrayType(final Type type) {
        String className = type.getClassName();
        if (type.getSort() != Type.OBJECT) {
            return ClassHelper.make(className);
        }

        return resolveClass(className);
    }

    /**
     * Resolves a fully qualified class name to a runtime JVM {@link Class} object.
     * Uses the compilation unit's class loader to load the class.
     *
     * @param name the fully qualified class name
     * @return the loaded runtime {@link Class}
     * @throws GroovyBugError if the class cannot be loaded (wrapped ClassNotFoundException)
     */
    public Class resolveJvmClass(final String name) {
        try {
            return unit.getClassLoader().loadClass(name, false, true);
        } catch (ClassNotFoundException e) {
            throw new GroovyBugError("JVM class can't be loaded for " + name, e);
        }
    }
}
