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

import org.codehaus.groovy.ast.stmt.Statement;

import java.util.LinkedList;

/**
 * Represents an inner class declaration
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 */
public class InnerClassNode extends ClassNode {

    private final ClassNode outerClass;
    private VariableScope scope;
    private boolean anonymous;

    /**
     * @param name is the full name of the class
     * @param modifiers the modifiers, @see org.objectweb.asm.Opcodes
     * @param superClass the base class name - use "java.lang.Object" if no direct base class
     */
    public InnerClassNode(ClassNode outerClass, String name, int modifiers, ClassNode superClass) {
        this(outerClass, name, modifiers, superClass, ClassHelper.EMPTY_TYPE_ARRAY, MixinNode.EMPTY_ARRAY);
    }

    /**
     * @param name is the full name of the class
     * @param modifiers the modifiers, @see org.objectweb.asm.Opcodes
     * @param superClass the base class name - use "java.lang.Object" if no direct base class
     */
    public InnerClassNode(ClassNode outerClass, String name, int modifiers, ClassNode superClass, ClassNode[] interfaces, MixinNode[] mixins) {
        super(name, modifiers, superClass, interfaces, mixins);
        this.outerClass = outerClass;

        if (outerClass.innerClasses == null)
            outerClass.innerClasses = new LinkedList<>();
        outerClass.innerClasses.add(this);
    }

    public ClassNode getOuterClass() {
        return outerClass;
    }

    public ClassNode getOuterMostClass()  {
        ClassNode outerClass = getOuterClass();
        while (outerClass instanceof InnerClassNode)  {
            outerClass = outerClass.getOuterClass();
        }
        return outerClass;
    }

    /**
     * @return the field node on the outer class or null if this is not an inner class
     */
    public FieldNode getOuterField(String name) {
        return outerClass.getDeclaredField(name);
    }
    
    public VariableScope getVariableScope() {
        return scope;        
    }
    
    public void setVariableScope(VariableScope scope) {
        this.scope = scope;
    }

    public boolean isAnonymous() {
        return anonymous;
    }

    public void setAnonymous(boolean anonymous) {
        this.anonymous = anonymous;
    }

    @Override
    public void addConstructor(final ConstructorNode node) {
        super.addConstructor(node);
    }

    @Override
    public ConstructorNode addConstructor(final int modifiers, final Parameter[] parameters, final ClassNode[] exceptions, final Statement code) {
        return super.addConstructor(modifiers, parameters, exceptions, code);
    }
}
