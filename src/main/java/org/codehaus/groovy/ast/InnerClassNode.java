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

import org.objectweb.asm.Opcodes;

import java.util.Collections;

/**
 * Represents a nested (inner) class definition within an outer class.
 * Inner classes maintain a reference to their enclosing {@link ClassNode} and support Groovy-specific
 * features such as variable scoping and anonymous class detection. Automatically marks inner classes
 * as static when defined in interfaces per JLS specifications.
 *
 * @see ClassNode
 * @see VariableScope
 */
public class InnerClassNode extends ClassNode {

    private final ClassNode outerClass;
    private VariableScope scope;
    private boolean anonymous;

    /**
     * Creates an inner class with the specified outer class, name, modifiers, and superclass.
     *
     * @param outerClass the enclosing class, or null for top-level classes
     * @param name the fully qualified name of the inner class
     * @param modifiers the {@code org.objectweb.asm.Opcodes} modifiers for this class
     * @param superClass the superclass of this inner class, or {@link ClassNode#EMPTY_ARRAY} for Object
     * @see org.objectweb.asm.Opcodes
     */
    public InnerClassNode(ClassNode outerClass, String name, int modifiers, ClassNode superClass) {
        this(outerClass, name, modifiers, superClass, ClassNode.EMPTY_ARRAY, MixinNode.EMPTY_ARRAY);
    }

    /**
     * Creates an inner class with the specified outer class, name, modifiers, superclass, interfaces, and mixins.
     * If the outer class is an interface, the inner class is automatically marked as static.
     *
     * @param outerClass the enclosing class, or null for top-level classes
     * @param name the fully qualified name of the inner class
     * @param modifiers the {@code org.objectweb.asm.Opcodes} modifiers for this class
     * @param superClass the superclass of this inner class
     * @param interfaces the interfaces implemented by this inner class
     * @param mixins the mixins applied to this inner class
     * @see org.objectweb.asm.Opcodes
     */
    public InnerClassNode(ClassNode outerClass, String name, int modifiers, ClassNode superClass, ClassNode[] interfaces, MixinNode[] mixins) {
        super(name, modifiers | (outerClass != null && outerClass.isInterface() ? Opcodes.ACC_STATIC : 0), superClass, interfaces, mixins);
        if (outerClass != null) outerClass.addInnerClass(this);
        this.outerClass = outerClass;
    }

    /**
     * Returns the class that encloses this inner class, or null if this is a top-level class.
     *
     * @return the enclosing {@link ClassNode}, or null
     */
    @Override
    public ClassNode getOuterClass() {
        return outerClass;
    }

    /**
     * Returns the outermost class in the nesting hierarchy by recursively traversing outer class references
     * until reaching a class that has no outer class.
     *
     * @return the topmost {@link ClassNode} in the nesting chain
     */
    public ClassNode getOuterMostClass() {
        ClassNode outerClass = getOuterClass();
        while (outerClass instanceof InnerClassNode)  {
            outerClass = outerClass.getOuterClass();
        }
        return outerClass;
    }

    /**
     * Retrieves a field from the enclosing class by name, enabling access to outer class fields
     * from within the inner class scope.
     *
     * @param name the field name to retrieve from the outer class
     * @return the {@link FieldNode} from the outer class, or null if not found
     * @see ClassNode#getDeclaredField(String)
     */
    @Override
    public FieldNode getOuterField(String name) {
        return outerClass.getDeclaredField(name);
    }

    /**
     * Returns the variable scope associated with this inner class, tracking declarations and
     * references for closure and method boundary analysis.
     *
     * @return the {@link VariableScope}, or null if not set
     * @see VariableScope
     */
    public VariableScope getVariableScope() {
        return scope;
    }

    /**
     * Sets the variable scope that manages variables declared and referenced within this inner class.
     *
     * @param scope the {@link VariableScope} to associate with this inner class
     */
    public void setVariableScope(VariableScope scope) {
        this.scope = scope;
    }

    /**
     * Checks if this inner class is sealed per JLS 15.9.5, returning false for anonymous classes
     * since they cannot be further subclassed.
     *
     * @return true if this inner class is sealed and not anonymous
     */
    @Override
    public boolean isSealed() {
        return !isAnonymous() && super.isSealed(); // JLS 15.9.5
    }

    /**
     * Returns whether this inner class represents an anonymous class (created with inline expressions).
     *
     * @return true if this is an anonymous inner class
     */
    public boolean isAnonymous() {
        return anonymous;
    }

    /**
     * Marks this inner class as anonymous if not already set. Anonymous classes have restricted modifiers:
     * static and abstract modifiers are removed, and final is removed for enums per JLS 15.9.5.
     * Once set to anonymous, this state cannot be reverted.
     *
     * @param anonymous true to mark this inner class as anonymous
     * @throws IllegalArgumentException if attempting to demote from anonymous to non-anonymous
     */
    public void setAnonymous(final boolean anonymous) {
        if (this.anonymous != anonymous) {
            if (anonymous) { // JLS 15.9.5
                this.anonymous = true;

                // GROOVY-11877
                int modifiers = getModifiers();
                modifiers &= ~Opcodes.ACC_STATIC;
                modifiers &= ~Opcodes.ACC_ABSTRACT;
                if (!isEnum()) modifiers &= ~Opcodes.ACC_FINAL;
                setModifiers(modifiers);

                setPermittedSubclasses(Collections.emptyList());
            } else {
                throw new IllegalArgumentException("cannot demote anon. inner class");
            }
        }
    }
}
