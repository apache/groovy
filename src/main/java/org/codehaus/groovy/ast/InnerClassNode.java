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
 * Represents an inner class definition.
 */
public class InnerClassNode extends ClassNode {

    private final ClassNode outerClass;
    private VariableScope scope;
    private boolean anonymous;

    /**
     * @param name is the full name of the class
     * @param modifiers the modifiers, @see org.objectweb.asm.Opcodes
     * @param superClass the base class name; use "java.lang.Object" if no direct base class
     */
    public InnerClassNode(ClassNode outerClass, String name, int modifiers, ClassNode superClass) {
        this(outerClass, name, modifiers, superClass, ClassNode.EMPTY_ARRAY, MixinNode.EMPTY_ARRAY);
    }

    /**
     * @param name is the full name of the class
     * @param modifiers the modifiers, @see org.objectweb.asm.Opcodes
     * @param superClass the base class name; use "java.lang.Object" if no direct base class
     */
    public InnerClassNode(ClassNode outerClass, String name, int modifiers, ClassNode superClass, ClassNode[] interfaces, MixinNode[] mixins) {
        super(name, modifiers | (outerClass != null && outerClass.isInterface() ? Opcodes.ACC_STATIC : 0), superClass, interfaces, mixins);
        if (outerClass != null) outerClass.addInnerClass(this);
        this.outerClass = outerClass;
    }

    @Override
    public ClassNode getOuterClass() {
        return outerClass;
    }

    public ClassNode getOuterMostClass() {
        ClassNode outerClass = getOuterClass();
        while (outerClass instanceof InnerClassNode)  {
            outerClass = outerClass.getOuterClass();
        }
        return outerClass;
    }

    @Override
    public FieldNode getOuterField(String name) {
        return outerClass.getDeclaredField(name);
    }

    public VariableScope getVariableScope() {
        return scope;
    }

    public void setVariableScope(VariableScope scope) {
        this.scope = scope;
    }

    @Override
    public boolean isSealed() {
        return !isAnonymous() && super.isSealed(); // JLS 15.9.5
    }

    public boolean isAnonymous() {
        return anonymous;
    }

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
