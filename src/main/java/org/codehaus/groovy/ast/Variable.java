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
 * interface to mark a AstNode as Variable. Typically these are
 * VariableExpression, FieldNode, PropertyNode and Parameter
 */
public interface Variable {

    /**
     * Returns the name of the variable.
     */
    String getName();

    /**
     * Returns the type of the variable.
     */
    ClassNode getType();

    /**
     * Returns the type before wrapping primitives type of the variable.
     */
    ClassNode getOriginType();

    /**
     * Returns the expression used to initialize the variable or null of there
     * is no initialization.
     */
    Expression getInitialExpression();

    /**
     * Returns true if there is an initialization expression.
     */
    boolean hasInitialExpression();

    default boolean isClosureSharedVariable() {
        return false;
    }
    default void setClosureSharedVariable(boolean inClosure) {
    }

    /**
     * Returns true if this variable is used in a static context.
     * A static context is any static initializer block, when this variable
     * is declared as static or when this variable is used in a static method
     */
    boolean isInStaticContext();

    boolean isDynamicTyped();

    //--------------------------------------------------------------------------

    int getModifiers();

    /**
     * @since 5.0.0
     */
    default boolean isFinal() {
        return (getModifiers() & ACC_FINAL) != 0;
    }

    /**
     * @since 5.0.0
     */
    default boolean isPrivate() {
        return (getModifiers() & ACC_PRIVATE) != 0;
    }

    /**
     * @since 5.0.0
     */
    default boolean isProtected() {
        return (getModifiers() & ACC_PROTECTED) != 0;
    }

    /**
     * @since 5.0.0
     */
    default boolean isPublic() {
        return (getModifiers() & ACC_PUBLIC) != 0;
    }

    /**
     * @since 5.0.0
     */
    default boolean isStatic() {
        return (getModifiers() & ACC_STATIC) != 0;
    }

    /**
     * @since 5.0.0
     */
    default boolean isVolatile() {
        return (getModifiers() & ACC_VOLATILE) != 0;
    }
}
