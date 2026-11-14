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
package org.codehaus.groovy.antlr;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.MixinNode;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.objectweb.asm.Opcodes;

/**
 * Builds enum class nodes and their constant fields.
 */
public class EnumHelper {

    /**
     * Creates an enum class named {@code name}. When {@code outerClass} is
     * null the class is top-level; otherwise it is a static inner class
     * whose binary name is the outer class name, {@code $}, and
     * {@code name}. The JVM enum flag is set. The superclass
     * is {@code Enum} parameterized by the new class.
     *
     * @param name simple name of the enum
     * @param modifiers access flags combined with the enum flags
     * @param interfaces interfaces the enum implements
     * @param outerClass enclosing class, or null for a top-level enum
     * @return the new enum class node
     */
    public static ClassNode makeEnumNode(final String name, final int modifiers, final ClassNode[] interfaces, final ClassNode outerClass) {
        ClassNode enumClass;
        if (outerClass == null) {
            enumClass = new ClassNode(name, modifiers | Opcodes.ACC_ENUM, null, interfaces, MixinNode.EMPTY_ARRAY);
        } else {
            enumClass = new InnerClassNode(outerClass, outerClass.getName() + "$" + name, modifiers | Opcodes.ACC_ENUM | Opcodes.ACC_STATIC, null, interfaces, MixinNode.EMPTY_ARRAY);
        }

        // enum E extends java.lang.Enum<E>
        ClassNode superClass = ClassHelper.Enum_Type.getPlainNodeReference();
        superClass.setGenericsTypes(new GenericsType[]{new GenericsType(enumClass)});
        enumClass.setSuperClass(superClass);

        return enumClass;
    }

    /**
     * Adds a public static final enum constant named {@code name}. A
     * non-null initializer that is not already a {@link ListExpression} is
     * wrapped in a one-element list.
     *
     * @param enumClass enum that receives the constant
     * @param name constant name
     * @param init constructor arguments, a list of them, or null
     * @return the field added to {@code enumClass}
     */
    public static FieldNode addEnumConstant(final ClassNode enumClass, final String name, Expression init) {
        if (init != null && !(init instanceof ListExpression)) {
            ListExpression list = new ListExpression();
            list.addExpression(init);
            init = list;
        }
        final int modifiers = Opcodes.ACC_ENUM | Opcodes.ACC_FINAL | Opcodes.ACC_STATIC | Opcodes.ACC_PUBLIC;
        FieldNode fn = new FieldNode(name, modifiers, enumClass.getPlainNodeReference(), enumClass, init);
        enumClass.addField(fn);
        return fn;
    }
}
