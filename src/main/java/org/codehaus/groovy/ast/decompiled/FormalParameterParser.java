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

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.vmplugin.v8.Java8;
import org.objectweb.asm.signature.SignatureVisitor;

import java.util.ArrayList;
import java.util.List;

import static org.codehaus.groovy.control.CompilerConfiguration.ASM_API_VERSION;

/**
 * Parses Java formal type parameters from generic signatures according to JVMS &sect;4.7.9.1.
 * <p>
 * This abstract parser extends ASM's {@code SignatureVisitor} to process type parameter declarations
 * within class and method generic signatures. It accumulates type parameter names and their bounds,
 * converting them to Groovy {@link GenericsType} objects.
 * <p>
 * Type parameters are specified in angle brackets with optional bounds:
 * <ul>
 *   <li>{@code <T>} - unbounded type parameter</li>
 *   <li>{@code <T extends Number>} - single upper bound</li>
 *   <li>{@code <T extends Number & Comparable<T>>} - multiple upper bounds (intersection type)</li>
 *   <li>{@code <T extends ? super Serializable>} - lower bounds (less common)</li>
 * </ul>
 * <p>
 * This parser is abstract and must be subclassed to implement how parsed type parameters are used.
 * For example, {@code ClassSignatureParser} extends this to process class type parameters,
 * while method signatures create anonymous subclasses to handle method type parameters separately.
 *
 * @see GenericsType for the representation of type parameters with bounds
 * @see TypeSignatureParser for parsing individual type expressions and bounds
 * @see ClassSignatureParser for parsing class-level type parameters
 * @see MemberSignatureParser for parsing method-level type parameters
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se19/html/jvms-4.html#jvms-4.7.9.1">
 *      JVMS §4.7.9.1 Signatures</a>
 */
abstract class FormalParameterParser extends SignatureVisitor {

    private String currentTypeParameter;
    private final List<ClassNode> parameterBounds = new ArrayList<>();
    private final List<GenericsType> typeParameters = new ArrayList<>();

    private final AsmReferenceResolver resolver;

    /**
     * Constructs a formal parameter parser with the given type resolver.
     *
     * @param resolver used to resolve class types from internal names and descriptors
     */
    public FormalParameterParser(final AsmReferenceResolver resolver) {
        super(ASM_API_VERSION);
        this.resolver = resolver;
    }

    /**
     * Flushes any pending type parameter being accumulated.
     * <p>
     * When parsing transitions between type parameters or completes, this method converts
     * the accumulated type parameter name and bounds into a {@code GenericsType} object
     * and adds it to the collected type parameters. This method is called:
     * <ul>
     *   <li>When a new type parameter is encountered</li>
     *   <li>When transitioning to non-type-parameter elements (superclass, interfaces, return type)</li>
     *   <li>When parsing is complete</li>
     * </ul>
     *
     * @see #visitFormalTypeParameter(String) for when this is called during parsing
     */
    @SuppressWarnings("removal")
    protected void flushTypeParameter() {
        if (currentTypeParameter != null) {
            ClassNode ref = Java8.configureTypeVariableReference(currentTypeParameter);
            ClassNode[] theBoundTypes = parameterBounds.toArray(ClassNode.EMPTY_ARRAY);
            typeParameters.add(Java8.configureTypeVariableDefinition(ref, theBoundTypes));

            parameterBounds.clear();
            currentTypeParameter = null;
        }
    }

    /**
     * Returns the parsed type parameters as an array of {@code GenericsType} objects.
     * <p>
     * This method flushes any pending type parameter and returns all accumulated type parameters
     * with their bounds. Each type parameter is represented as a {@code GenericsType} with:
     * <ul>
     *   <li>The type parameter name (e.g., "T")</li>
     *   <li>Upper bound constraints (or empty for unbounded parameters)</li>
     *   <li>Lower bound (typically null)</li>
     * </ul>
     *
     * @return array of parsed type parameters, or empty array if none were declared
     */
    public GenericsType[] getTypeParameters() {
        flushTypeParameter();
        return typeParameters.toArray(GenericsType.EMPTY_ARRAY);
    }

    /**
     * Called when ASM parser encounters a new formal type parameter in the signature.
     * <p>
     * This method is invoked once per type parameter declaration. The parameter name is stored
     * and any previously accumulated bounds are flushed into the type parameters list.
     * Bounds for this parameter will be collected via subsequent calls to {@link #visitClassBound()}
     * and {@link #visitInterfaceBound()}.
     * <p>
     * Example: When parsing {@code <T extends Number & Serializable>}, this method is called once
     * with {@code name = "T"}, followed by two bound visits.
     *
     * @param name the type parameter name (e.g., "T", "E", "K", "V")
     */
    @Override
    public void visitFormalTypeParameter(final String name) {
        flushTypeParameter();
        currentTypeParameter = name;
    }

    /**
     * Called when ASM parser encounters a class bound for the current type parameter.
     * <p>
     * Type parameters may have an upper bound that extends a class or interface.
     * The first bound (if present) is typically a class; additional bounds are interfaces.
     * Returns a {@code TypeSignatureParser} that will parse the bound type and add it to
     * the bounds list for the current type parameter.
     * <p>
     * Example: In {@code <T extends Number>}, this is called with a parser that will resolve "Number".
     *
     * @return a TypeSignatureParser that collects the class bound for the current type parameter
     * @see #visitInterfaceBound() for interface bounds
     */
    @Override
    public SignatureVisitor visitClassBound() {
        return new TypeSignatureParser(resolver) {
            @Override
            void finished(ClassNode result) {
                parameterBounds.add(result);
            }
        };
    }

    /**
     * Called when ASM parser encounters an interface bound for the current type parameter.
     * <p>
     * Interface bounds are additional bounds beyond the class bound, used for intersection types.
     * The first bound in an intersection type is typically the class bound (handled by
     * {@link #visitClassBound()}), and subsequent bounds are interface bounds.
     * <p>
     * Example: In {@code <T extends Number & Comparable<T>>}, {@code visitClassBound()} is called
     * for "Number", and {@code visitInterfaceBound()} is called for "Comparable&lt;T&gt;".
     *
     * @return a TypeSignatureParser that collects the interface bound for the current type parameter
     * @see #visitClassBound() for the primary class bound
     */
    @Override
    public SignatureVisitor visitInterfaceBound() {
        return visitClassBound();
    }
}
