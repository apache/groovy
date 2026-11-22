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

/**
 * Represents the anonymous inner class for an enum constant. This subtype is
 * needed so that EnumVisitor can differentiate between the scenarios when an
 * {@link InnerClassNode} represents anonymous inner class for an enum constant
 * versus an enum class defined within another class.
 */
public class EnumConstantClassNode extends InnerClassNode {

    /**
     * Creates the anonymous class of an enum constant. The class is
     * {@code ACC_ENUM | ACC_FINAL} and extends {@code superClass}.
     *
     * @param outerClass the enum that declares the constant
     * @param name the binary name of the anonymous class
     * @param superClass the enum type this constant class extends
     */
    public EnumConstantClassNode(ClassNode outerClass, String name, ClassNode superClass) {
        super(outerClass, name, Opcodes.ACC_ENUM | Opcodes.ACC_FINAL, superClass);
    }

    /**
     * Creates the anonymous class of an enum constant with caller-supplied modifiers.
     * Unlike {@link #EnumConstantClassNode(ClassNode, String, ClassNode)}, the
     * modifiers are passed through unchanged.
     *
     * @param outerClass the enum that declares the constant
     * @param name the binary name of the anonymous class
     * @param modifiers the access flags stored on the class
     * @param superClass the enum type this constant class extends
     * @deprecated since 4.0.0, for removal
     */
    @Deprecated(forRemoval = true, since = "4.0.0")
    public EnumConstantClassNode(ClassNode outerClass, String name, int modifiers, ClassNode superClass) {
        super(outerClass, name, modifiers, superClass);
    }
}
