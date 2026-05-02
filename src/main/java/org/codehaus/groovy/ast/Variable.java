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
package org.codehaus.groovy.ast;

import org.codehaus.groovy.ast.expr.Expression;

import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PROTECTED;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACC_VOLATILE;

/**
 * Interface marking an AST node as representing a variable in Groovy/Java scope.
 * Typical implementations include {@link org.codehaus.groovy.ast.expr.VariableExpression},
 * {@link FieldNode}, {@link PropertyNode}, and {@link Parameter}.
 * Provides unified access to variable metadata such as type, scope context, and modifiers.
 *
 * @see org.codehaus.groovy.ast.expr.VariableExpression
 * @see FieldNode
 * @see PropertyNode
 * @see Parameter
 */
public interface Variable {

    /**
     * Returns the name of the variable.
     *
     * @return the variable name
     */
    String getName();

    /**
     * Returns the type of the variable. If the type is not yet determined,
     * implementations may return a dynamic or placeholder type.
     *
     * @return the {@link ClassNode} representing this variable's type
     */
    ClassNode getType();

    /**
     * Returns the original type of this variable before any type wrapping or transformation.
     * For primitive types, this preserves the distinction between boxed and unboxed forms.
     *
     * @return the original {@link ClassNode} before transformations
     */
    ClassNode getOriginType();

    /**
     * Returns the initialization expression for this variable, or null if no initialization is present.
     * For fields and local variables, this may contain the right-hand side of an assignment;
     * for parameters, this represents a default value.
     *
     * @return the initialization {@link Expression}, or null
     */
    Expression getInitialExpression();

    /**
     * Returns true if this variable has an initialization expression.
     *
     * @return true if initialized
     */
    boolean hasInitialExpression();

    /**
     * Returns true if this variable is shared with and accessed by nested closures.
     * Shared variables require special handling in bytecode generation to ensure proper access semantics.
     *
     * @return true if shared with closures
     */
    default boolean isClosureSharedVariable() {
        return false;
    }

    /**
     * Marks this variable as shared with nested closures.
     * This affects code generation and variable access patterns.
     *
     * @param inClosure true if shared with closures
     */
    default void setClosureSharedVariable(boolean inClosure) {
    }

    /**
     * Returns true if this variable is declared or used in a static context.
     * A static context includes static initializers, static field declarations, static method bodies,
     * and class-level code without instance access.
     *
     * @return true if in a static context
     */
    boolean isInStaticContext();

    /**
     * Returns true if this variable has dynamic type information that could not be resolved at compile time.
     *
     * @return true if dynamically typed
     */
    boolean isDynamicTyped();

    /**
     * Returns the modifiers (access flags) for this variable as per {@code org.objectweb.asm.Opcodes}.
     * May include visibility modifiers (public, protected, private), {@code static}, {@code final}, etc.
     *
     * @return the modifier flags
     * @see org.objectweb.asm.Opcodes
     */
    int getModifiers();

    /**
     * Returns true if this variable is declared with the {@code final} modifier.
     *
     * @return true if final
     * @since 5.0.0
     */
    default boolean isFinal() {
        return (getModifiers() & ACC_FINAL) != 0;
    }

    /**
     * Returns true if this variable is declared with the {@code private} modifier.
     *
     * @return true if private
     * @since 5.0.0
     */
    default boolean isPrivate() {
        return (getModifiers() & ACC_PRIVATE) != 0;
    }

    /**
     * Returns true if this variable is declared with the {@code protected} modifier.
     *
     * @return true if protected
     * @since 5.0.0
     */
    default boolean isProtected() {
        return (getModifiers() & ACC_PROTECTED) != 0;
    }

    /**
     * Returns true if this variable is declared with the {@code public} modifier.
     *
     * @return true if public
     * @since 5.0.0
     */
    default boolean isPublic() {
        return (getModifiers() & ACC_PUBLIC) != 0;
    }

    /**
     * Returns true if this variable is declared with the {@code static} modifier.
     *
     * @return true if static
     * @since 5.0.0
     */
    default boolean isStatic() {
        return (getModifiers() & ACC_STATIC) != 0;
    }

    /**
     * Returns true if this variable is declared with the {@code volatile} modifier,
     * indicating that memory visibility is required for multi-threaded access.
     *
     * @return true if volatile
     * @since 5.0.0
     */
    default boolean isVolatile() {
        return (getModifiers() & ACC_VOLATILE) != 0;
    }
}
