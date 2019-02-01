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

/**
 * Represents a mixin which can be applied to any ClassNode to implement mixins
 */
public class MixinNode extends ClassNode {

    public static final MixinNode[] EMPTY_ARRAY = {};
    
    /**
     * @param name is the full name of the class
     * @param modifiers the modifiers, @see org.objectweb.asm.Opcodes
     * @param superType the base class name - use "java.lang.Object" if no direct base class
     */
    public MixinNode(String name, int modifiers, ClassNode superType) {
        this(name, modifiers, superType, ClassHelper.EMPTY_TYPE_ARRAY);
    }

    /**
     * @param name is the full name of the class
     * @param modifiers the modifiers, @see org.objectweb.asm.Opcodes
     * @param superType the base class name - use "java.lang.Object" if no direct base class
     */
    public MixinNode(String name, int modifiers, ClassNode superType, ClassNode[] interfaces) {
        super(name, modifiers, superType, interfaces, MixinNode.EMPTY_ARRAY);
    }
}
