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

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.vmplugin.v8.Java8;
import org.objectweb.asm.Type;
import org.objectweb.asm.signature.SignatureVisitor;

import java.util.ArrayList;
import java.util.List;

import static org.codehaus.groovy.control.CompilerConfiguration.ASM_API_VERSION;

/**
 * Parses Java generic type signatures from compiled bytecode according to JVMS &sect;4.7.9.1.
 * <p>
 * This abstract parser extends ASM's {@code SignatureVisitor} to convert JVMS type signature strings
 * into Groovy {@link ClassNode} objects with full generic type information. It handles:
 * <ul>
 *   <li>Primitive types: {@code I} (int), {@code Z} (boolean), etc.</li>
 *   <li>Class types: {@code Ljava/lang/String;}</li>
 *   <li>Generic class types with type arguments: {@code Ljava/util/List<Ljava/lang/String;>;}</li>
 *   <li>Type variables: {@code TT;} (references a type parameter)</li>
 *   <li>Array types: {@code [Ljava/lang/String;} (array of String)</li>
 *   <li>Wildcard types: {@code *} (unbounded), {@code +TT;} (extends), {@code -TT;} (super)</li>
 *   <li>Inner class types: {@code Ljava/util/Map$Entry<TK;TV;>;}</li>
 * </ul>
 * <p>
 * Type signature format examples (JVMS notation):
 * <ul>
 *   <li>{@code Ljava/lang/String;} → {@code String}</li>
 *   <li>{@code Ljava/util/List<Ljava/lang/String;>;} → {@code List<String>}</li>
 *   <li>{@code TT;} → type variable {@code T}</li>
 *   <li>{@code [Ljava/lang/String;} → {@code String[]}</li>
 *   <li>{@code Ljava/util/Map<**>;} → {@code Map<?, ?>}</li>
 * </ul>
 * <p>
 * This parser is abstract and must be subclassed to implement the {@link #finished(ClassNode)} method,
 * which is called when parsing of a single type is complete. This design allows the result to be
 * integrated into the calling context (e.g., stored in a field, parameter, or return type).
 *
 * @see GenericsType for representation of generic type arguments
 * @see ClassNode for representing types and classes
 * @see FormalParameterParser for parsing type parameter bounds
 * @see ClassSignatureParser for parsing class-level type information
 * @see MemberSignatureParser for parsing method and field types
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se19/html/jvms-4.html#jvms-TypeSignature">
 *      JVMS §4.7.9.1 TypeSignature Production</a>
 */
abstract class TypeSignatureParser extends SignatureVisitor {

    private final AsmReferenceResolver resolver;

    /**
     * Constructs a type signature parser with the given type resolver.
     *
     * @param resolver used to resolve class types from internal names and descriptors
     */
    public TypeSignatureParser(final AsmReferenceResolver resolver) {
        super(ASM_API_VERSION);
        this.resolver = resolver;
    }

    /**
     * Applies type erasure to a generic type, linking it to its erasure for runtime compatibility.
     * <p>
     * In the Java type system, generic types are erased to their non-generic equivalents at runtime.
     * This method updates a generic type's redirect to point to its erased form, preserving
     * the generic type information for compile-time use while maintaining runtime compatibility.
     * <p>
     * For example:
     * <ul>
     *   <li>{@code List<String>} is erased to {@code List}</li>
     *   <li>{@code T extends Number} is erased to {@code Number}</li>
     *   <li>{@code String[]} remains {@code String[]}</li>
     * </ul>
     *
     * @param genericType the parameterized type with generic information (may be a type variable)
     * @param erasure the erased (non-generic) type to associate with the generic type
     * @return the genericType with erasure information set
     */
    protected static ClassNode applyErasure(final ClassNode genericType, final ClassNode erasure) {
        if (genericType.isArray() && erasure.isArray() && genericType.getComponentType().isGenericsPlaceHolder()) {
            genericType.setRedirect(erasure);
            genericType.getComponentType().setRedirect(erasure.getComponentType());
        } else if (genericType.isGenericsPlaceHolder()) {
            genericType.setRedirect(erasure);
        }
        return genericType;
    }

    /**
     * Abstract method that subclasses must implement to process a parsed type.
     * <p>
     * This method is called when parsing of a single type signature completes. The parsed type
     * (with full generic information) is passed to this method for integration into the calling
     * context. Subclasses implement this to store the result, add it to a list, or perform
     * other context-specific actions.
     * <p>
     * Examples of implementation:
     * <ul>
     *   <li>Store as a field type: {@code fieldNode.setType(result)}</li>
     *   <li>Add to a list of bounds: {@code bounds.add(result)}</li>
     *   <li>Set as superclass: {@code classNode.setSuperClass(result)}</li>
     * </ul>
     *
     * @param result the fully parsed ClassNode with generic type information
     */
    abstract void finished(ClassNode result);

    private String baseName;
    private final List<GenericsType> arguments = new ArrayList<>();

    /**
     * Called when ASM parser encounters a type variable reference in the signature.
     * <p>
     * Type variables are placeholders for generic type parameters, referenced by name in
     * type signatures. The type variable must be declared in the enclosing class or method's
     * formal type parameters.
     * <p>
     * Example: In {@code <T> Ljava/util/List<TT;>;}, the {@code TT;} represents a type
     * variable reference to parameter "T".
     *
     * @param name the type variable name (e.g., "T", "E", "K", "V")
     * @see #finished(ClassNode) is called with a reference to the type variable
     */
    @Override
    @SuppressWarnings("removal")
    public void visitTypeVariable(final String name) {
        finished(Java8.configureTypeVariableReference(name));
    }

    /**
     * Called when ASM parser encounters a primitive type in the signature.
     * <p>
     * Primitive types are encoded as single characters in JVMS signatures:
     * <ul>
     *   <li>{@code V} → void</li>
     *   <li>{@code Z} → boolean</li>
     *   <li>{@code C} → char</li>
     *   <li>{@code B} → byte</li>
     *   <li>{@code S} → short</li>
     *   <li>{@code I} → int</li>
     *   <li>{@code J} → long</li>
     *   <li>{@code F} → float</li>
     *   <li>{@code D} → double</li>
     * </ul>
     *
     * @param descriptor the primitive type descriptor character
     * @see #finished(ClassNode) is called with the resolved primitive type
     */
    @Override
    public void visitBaseType(final char descriptor) {
        finished(resolver.resolveType(Type.getType(String.valueOf(descriptor))));
    }

    /**
     * Called when ASM parser encounters an array type in the signature.
     * <p>
     * Returns a delegating parser that will process the component type and convert the result
     * to an array type. This allows nested array types (e.g., {@code [[Ljava/lang/String;}})
     * to be handled recursively.
     * <p>
     * Example: {@code [Ljava/lang/String;} is parsed as an array of String.
     *
     * @return a TypeSignatureParser that wraps the component type result in an array
     * @see #finished(ClassNode) is called with the array type when component parsing completes
     */
    @Override
    public SignatureVisitor visitArrayType() {
        final TypeSignatureParser outer = this;
        return new TypeSignatureParser(resolver) {
            @Override
            void finished(final ClassNode result) {
                outer.finished(result.makeArray());
            }
        };
    }

    /**
     * Called when ASM parser encounters a class type in the signature.
     * <p>
     * This begins the parsing of a class type, storing its internal name. Type arguments
     * (if any) are parsed via subsequent calls to {@link #visitTypeArgument()} or
     * {@link #visitTypeArgument(char)}. Inner class types are handled by
     * {@link #visitInnerClassType(String)}, and {@link #visitEnd()} completes the parse.
     * <p>
     * Example: When parsing {@code Ljava/util/List<Ljava/lang/String;>;}, this is called
     * with {@code name = "java/util/List"}.
     *
     * @param name the class type's internal name (e.g., "java/util/List", "java/lang/String")
     * @see #visitTypeArgument()
     * @see #visitTypeArgument(char)
     * @see #visitInnerClassType(String)
     * @see #visitEnd()
     */
    @Override
    public void visitClassType(final String name) {
        baseName = AsmDecompiler.fromInternalName(name);
    }

    /**
     * Called when ASM parser encounters an unbounded type argument (wildcard {@code ?}).
     * <p>
     * This represents a wildcard type with no bounds, matching any type. In Groovy, this is
     * represented as a wildcard GenericsType with no upper or lower bounds.
     * <p>
     * Example: In {@code List<?>;}, this method is called to add the unbounded wildcard argument.
     *
     * @see #visitTypeArgument(char) for bounded wildcards
     * @see #finished(ClassNode) is called at visitEnd() with all type arguments collected
     */
    @Override
    public void visitTypeArgument() {
        arguments.add(createWildcard(null, null));
    }

    /**
     * Called when ASM parser encounters a bounded type argument in the signature.
     * <p>
     * Returns a delegating parser that will parse the type argument and add it to the
     * type arguments list with appropriate bounds. The wildcard character indicates the kind of bound:
     * <ul>
     *   <li>{@code INSTANCEOF} ('+' in bytecode) → exact type, no bounds</li>
     *   <li>{@code EXTENDS} ('+' in signature) → upper bound (covariant: {@code ? extends T})</li>
     *   <li>{@code SUPER} ('-' in signature) → lower bound (contravariant: {@code ? super T})</li>
     * </ul>
     *
     * @param wildcard the kind of bound: INSTANCEOF (exact), EXTENDS (upper), or SUPER (lower)
     * @return a TypeSignatureParser that collects the bounded type argument
     * @see #visitTypeArgument() for unbounded wildcards
     * @see #finished(ClassNode) is called at visitEnd() with all type arguments collected
     */
    @Override
    public SignatureVisitor visitTypeArgument(final char wildcard) {
        return new TypeSignatureParser(resolver) {
            @Override
            void finished(ClassNode result) {
                if (wildcard == INSTANCEOF) {
                    arguments.add(new GenericsType(result));
                    return;
                }

                ClassNode[] upper = wildcard == EXTENDS ? new ClassNode[]{result} : null;
                ClassNode lower = wildcard == SUPER ? result : null;
                arguments.add(createWildcard(upper, lower));
            }
        };
    }

    /**
     * Called when ASM parser encounters an inner class type in the signature.
     * <p>
     * Inner class types are nested classes, represented with a {@code $} separator in the class name.
     * This method appends the inner class name to the base class name and clears the type arguments
     * list to prepare for parsing type arguments specific to the inner class.
     * <p>
     * Example: When parsing {@code Ljava/util/Map$Entry<TK;TV;>;}, after parsing "java/util/Map",
     * this method is called with {@code name = "Entry"}, updating the base name to "java/util/Map$Entry"
     * and resetting the arguments list for the inner class's type parameters.
     *
     * @param name the inner class's simple name (without package or outer class prefix)
     * @see #visitClassType(String) for the outer class type
     * @see #visitEnd() which completes parsing of the inner class type
     */
    @Override
    public void visitInnerClassType(final String name) {
        baseName += "$" + name;
        arguments.clear();
    }

    /**
     * Called when ASM parser completes parsing of a class type (with or without type arguments).
     * <p>
     * This method is called after parsing all components of a class type including the class name,
     * all type arguments, and any inner classes. It creates a ClassNode for the base type and applies
     * the collected type arguments (if any), then calls {@link #finished(ClassNode)} with the result.
     * <p>
     * Handles several cases:
     * <ul>
     *   <li>Non-generic class: creates a plain ClassNode</li>
     *   <li>Parameterized generic class: creates a ClassNode and sets type arguments</li>
     *   <li>Raw type (generic class used without type arguments): creates a plain reference</li>
     *   <li>Implicit type bounds: applies default bounds for unbounded wildcards from the class definition</li>
     * </ul>
     * <p>
     * Example signature transformations:
     * <ul>
     *   <li>{@code Ljava/lang/String;} → plain String ClassNode</li>
     *   <li>{@code Ljava/util/List<Ljava/lang/String;>;} → List ClassNode with String type argument</li>
     *   <li>{@code Ljava/util/List;} (raw) → List ClassNode without type arguments (erased)</li>
     * </ul>
     *
     * @see #visitClassType(String) to start parsing a class type
     * @see #visitTypeArgument() and {@link #visitTypeArgument(char)} for parsing type arguments
     * @see #finished(ClassNode) called with the fully constructed ClassNode
     */
    @Override
    public void visitEnd() {
        ClassNode baseType = resolver.resolveClass(baseName);
        if (arguments.isEmpty() && isNotParameterized(baseType)) {
            finished(baseType);
        } else {
            ClassNode parameterizedType = baseType.getPlainNodeReference();
            if (arguments.isEmpty()) {
                // GROOVY-10234: no type arguments -> raw type
            } else {
                try {
                    // GROOVY-10153, GROOVY-10651, GROOVY-10671: "?" or "? super T" (see ResolveVisitor#resolveWildcardBounding)
                    for (int i = 0, n = arguments.size(); i < n; i += 1) { GenericsType argument = arguments.get(i);
                        if (!argument.isWildcard() || argument.getUpperBounds() != null) continue; //
                        ClassNode[] implicitBounds = baseType.getGenericsTypes()[i].getUpperBounds();
                        if (implicitBounds != null && !ClassHelper.isObjectType(implicitBounds[0])) {
                            argument.getType().setRedirect(implicitBounds[0]); // bound is not Object
                        }
                    }
                } catch (StackOverflowError ignore) {
                    // TODO: self-referential type parameter
                }
                parameterizedType.setGenericsTypes(arguments.toArray(GenericsType.EMPTY_ARRAY));
            }
            finished(parameterizedType);
        }
    }

    //--------------------------------------------------------------------------

    /**
     * Creates a wildcard GenericsType with the specified bounds.
     * <p>
     * Wildcard types in Java generics represent unknown types with optional constraints.
     * This method constructs a GenericsType representing a wildcard (?) with upper and/or lower bounds.
     * <p>
     * Bound combinations:
     * <ul>
     *   <li>{@code upper=null, lower=null} → unbounded wildcard: {@code ?}</li>
     *   <li>{@code upper=[Number], lower=null} → upper bound: {@code ? extends Number}</li>
     *   <li>{@code upper=null, lower=String} → lower bound: {@code ? super String}</li>
     * </ul>
     *
     * @param upper array of upper bound ClassNodes (for covariant wildcards), or null for unbounded
     * @param lower a lower bound ClassNode (for contravariant wildcards), or null for unbounded
     * @return a GenericsType configured as a wildcard with the specified bounds
     * @see GenericsType#setWildcard(boolean) to mark this as a wildcard type
     */
    private static GenericsType createWildcard(final ClassNode[] upper, final ClassNode lower) {
        ClassNode base = ClassHelper.makeWithoutCaching("?");
        base.setRedirect(ClassHelper.OBJECT_TYPE);
        GenericsType t = new GenericsType(base, upper, lower);
        t.setWildcard(true);
        return t;
    }

    /**
     * Determines whether a class type is not parameterized (i.e., not a generic class or a raw type).
     * <p>
     * A non-parameterized class is one that either:
     * <ul>
     *   <li>Is not a generic class (has no type parameters defined)</li>
     *   <li>Is a generic class but hasn't been parsed for generic type information yet</li>
     * </ul>
     * <p>
     * This check is important when deciding whether to apply collected type arguments to a class.
     * Decompiled classes may have lazy initialization of generic type information, so this method
     * handles both fully-initialized ClassNodes and DecompiledClassNodes.
     *
     * @param cn the ClassNode to check
     * @return true if the class is not parameterized (has no generic type parameters)
     * @see DecompiledClassNode#isParameterized() for decompiled class handling
     */
    private static boolean isNotParameterized(final ClassNode cn) {
        // DecompiledClassNode may not have generics initialized
        if (cn instanceof DecompiledClassNode) {
            return !((DecompiledClassNode) cn).isParameterized();
        }
        return (cn.getGenericsTypes() == null);
    }
}
