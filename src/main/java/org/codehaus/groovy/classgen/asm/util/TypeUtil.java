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

package org.codehaus.groovy.classgen.asm.util;

import groovy.lang.Tuple2;
import org.apache.groovy.util.Maps;
import org.codehaus.groovy.ast.ClassNode;
import org.objectweb.asm.Type;

import java.util.Map;

import static org.codehaus.groovy.ast.ClassHelper.BigDecimal_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.BigInteger_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.Boolean_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.Byte_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.CLASS_Type;
import static org.codehaus.groovy.ast.ClassHelper.Character_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.DYNAMIC_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.Double_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.Float_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.GROOVY_OBJECT_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.GSTRING_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.Integer_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.Long_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.OBJECT_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.STRING_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.Short_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.VOID_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.boolean_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.byte_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.char_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.double_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.float_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.int_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.long_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.short_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.void_WRAPPER_TYPE;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.DLOAD;
import static org.objectweb.asm.Opcodes.DRETURN;
import static org.objectweb.asm.Opcodes.FLOAD;
import static org.objectweb.asm.Opcodes.FRETURN;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.IRETURN;
import static org.objectweb.asm.Opcodes.LLOAD;
import static org.objectweb.asm.Opcodes.LRETURN;

/**
 * A utility for getting information of types
 *
 * @since 2.5.0
 */
public abstract class TypeUtil {
    public static Class autoboxType(Class type) {
        final Class res = PRIMITIVE_TYPE_TO_WRAPPED_CLASS_MAP.get(type);
        return res == null ? type : res;
    }

    public static int getLoadInsnByType(Type type) {
        Integer insn = PRIMITIVE_TYPE_TO_LOAD_INSN_MAP.get(type);

        if (null != insn) {
            return insn;
        }

        return ALOAD;
    }

    public static int getReturnInsnByType(Type type) {
        Integer insn = PRIMITIVE_TYPE_TO_RETURN_INSN_MAP.get(type);

        if (null != insn) {
            return insn;
        }

        return ARETURN;
    }

    public static String getWrappedClassDescriptor(Type type) {
        String desc = PRIMITIVE_TYPE_TO_WRAPPED_CLASS_DESCRIPTOR_MAP.get(type);

        if (null != desc) {
            return desc;
        }

        throw new IllegalArgumentException("Unexpected type class [" + type + "]");
    }

    public static boolean isPrimitiveType(Type type) {
        return PRIMITIVE_TYPE_TO_LOAD_INSN_MAP.containsKey(type);
    }

    public static boolean isPrimitiveType(String name) {
        return NAME_TO_PRIMITIVE_TYPE_MAP.containsKey(name);
    }

    public static boolean isPrimitiveType(ClassNode type) {
        return PRIMITIVE_TYPE_TO_DESCRIPTION_MAP.containsKey(type.redirect());
    }

    public static boolean isDynamicTyped(ClassNode type) {
        return type != null && DYNAMIC_TYPE == type.redirect();
    }

    public static boolean isPrimitiveBoolean(ClassNode type) {
        return type.redirect() == boolean_TYPE;
    }

    public static boolean isPrimitiveChar(ClassNode type) {
        return type.redirect() == char_TYPE;
    }

    public static boolean isPrimitiveByte(ClassNode type) {
        return type.redirect() == byte_TYPE;
    }

    public static boolean isPrimitiveInt(ClassNode type) {
        return type.redirect() == int_TYPE;
    }

    public static boolean isPrimitiveLong(ClassNode type) {
        return type.redirect() == long_TYPE;
    }

    public static boolean isPrimitiveShort(ClassNode type) {
        return type.redirect() == short_TYPE;
    }

    public static boolean isPrimitiveDouble(ClassNode type) {
        return type.redirect() == double_TYPE;
    }

    public static boolean isPrimitiveFloat(ClassNode type) {
        return type.redirect() == float_TYPE;
    }

    public static boolean isPrimitiveVoid(ClassNode type) {
        return type.redirect() == VOID_TYPE;
    }

    public static boolean isWrapperBoolean(ClassNode type) {
        return type != null && type.redirect() == Boolean_TYPE;
    }

    public static boolean isWrapperCharacter(ClassNode type) {
        return type != null && type.redirect() == Character_TYPE;
    }

    public static boolean isWrapperByte(ClassNode type) {
        return type != null && type.redirect() == Byte_TYPE;
    }

    public static boolean isWrapperInteger(ClassNode type) {
        return type != null && type.redirect() == Integer_TYPE;
    }

    public static boolean isWrapperLong(ClassNode type) {
        return type != null && type.redirect() == Long_TYPE;
    }

    public static boolean isWrapperShort(ClassNode type) {
        return type != null && type.redirect() == Short_TYPE;
    }

    public static boolean isWrapperDouble(ClassNode type) {
        return type != null && type.redirect() == Double_TYPE;
    }

    public static boolean isWrapperFloat(ClassNode type) {
        return type != null && type.redirect() == Float_TYPE;
    }

    public static boolean isWrapperVoid(ClassNode type) {
        return type != null && type.redirect() == void_WRAPPER_TYPE;
    }

    public static boolean isBigIntegerType(ClassNode type) {
        return type != null && type.redirect() == BigInteger_TYPE;
    }

    public static boolean isBigDecimalType(ClassNode type) {
        return type != null && type.redirect() == BigDecimal_TYPE;
    }

    public static boolean isStringType(ClassNode type) {
        return type != null && type.redirect() == STRING_TYPE;
    }

    public static boolean isGStringType(ClassNode type) {
        return type != null && type.redirect() == GSTRING_TYPE;
    }

    public static boolean isObjectType(ClassNode type) {
        return OBJECT_TYPE.equals(type);
    }

    public static boolean isGroovyObjectType(ClassNode type) {
        return GROOVY_OBJECT_TYPE.equals(type);
    }

    public static boolean isClassType(ClassNode type) {
        return CLASS_Type.equals(type);
    }

    public static String getDescriptionByType(ClassNode type) {
        String desc = PRIMITIVE_TYPE_TO_DESCRIPTION_MAP.get(type);

        if (null == desc) { // reference type
            if (!type.isArray()) {
                return makeRefDescription(type.getName());
            }

            StringBuilder arrayDescription = new StringBuilder(32);
            Tuple2<ClassNode, Integer> arrayInfo = extractArrayInfo(type);

            for (int i = 0, dimension = arrayInfo.getSecond(); i < dimension; i++) {
                arrayDescription.append("[");
            }

            ClassNode componentType = arrayInfo.getV1();
            return arrayDescription.append(getDescriptionByType(componentType)).toString();
        }

        return desc;
    }

    public static String getDescriptionByName(String name) {
        ClassNode type = NAME_TO_PRIMITIVE_TYPE_MAP.get(name);

        if (null == type) {
            return makeRefDescription(name);
        }

        return getDescriptionByType(type);
    }

    private static String makeRefDescription(String name) {
        return REF_DESCRIPTION + name.replace('.', '/') + ";";
    }

    private static Tuple2<ClassNode, Integer> extractArrayInfo(ClassNode type) {
        int dimension = 0;

        do {
            dimension++;
        } while ((type = type.getComponentType()).isArray());

        return new Tuple2<ClassNode, Integer>(type, dimension);
    }

    private static final String REF_DESCRIPTION = "L";
    private static final Map<ClassNode, String> PRIMITIVE_TYPE_TO_DESCRIPTION_MAP = Maps.of(
            int_TYPE, "I",
            VOID_TYPE,"V",
            boolean_TYPE, "Z",
            byte_TYPE, "B",
            char_TYPE, "C",
            short_TYPE, "S",
            double_TYPE, "D",
            float_TYPE, "F",
            long_TYPE, "J"
    );

    private static final Map<String, ClassNode> NAME_TO_PRIMITIVE_TYPE_MAP = Maps.of(
            "int", int_TYPE,
            "void", VOID_TYPE,
            "boolean", boolean_TYPE,
            "byte", byte_TYPE,
            "char", char_TYPE,
            "short", short_TYPE,
            "double", double_TYPE,
            "float", float_TYPE,
            "long", long_TYPE
    );

    private static final Map<Type, Integer> PRIMITIVE_TYPE_TO_LOAD_INSN_MAP = Maps.of(
            Type.BOOLEAN_TYPE, ILOAD,
            Type.BYTE_TYPE, ILOAD,
            Type.CHAR_TYPE, ILOAD,
            Type.DOUBLE_TYPE, DLOAD,
            Type.FLOAT_TYPE, FLOAD,
            Type.INT_TYPE, ILOAD,
            Type.LONG_TYPE, LLOAD,
            Type.SHORT_TYPE, ILOAD
    );

    private static final Map<Type, Integer> PRIMITIVE_TYPE_TO_RETURN_INSN_MAP = Maps.of(
            Type.BOOLEAN_TYPE, IRETURN,
            Type.BYTE_TYPE, IRETURN,
            Type.CHAR_TYPE, IRETURN,
            Type.DOUBLE_TYPE, DRETURN,
            Type.FLOAT_TYPE, FRETURN,
            Type.INT_TYPE, IRETURN,
            Type.LONG_TYPE, LRETURN,
            Type.SHORT_TYPE, IRETURN
    );

    private static final Map<Type, String> PRIMITIVE_TYPE_TO_WRAPPED_CLASS_DESCRIPTOR_MAP = Maps.of(
            Type.BOOLEAN_TYPE, "java/lang/Boolean",
            Type.BYTE_TYPE, "java/lang/Byte",
            Type.CHAR_TYPE, "java/lang/Character",
            Type.DOUBLE_TYPE, "java/lang/Double",
            Type.FLOAT_TYPE, "java/lang/Float",
            Type.INT_TYPE, "java/lang/Integer",
            Type.LONG_TYPE, "java/lang/Long",
            Type.SHORT_TYPE, "java/lang/Short"
    );

    private static final Map<Class, Class> PRIMITIVE_TYPE_TO_WRAPPED_CLASS_MAP = Maps.of(
        byte.class, Byte.class,
        boolean.class, Boolean.class,
        char.class, Character.class,
        double.class, Double.class,
        float.class, Float.class,
        int.class, Integer.class,
        long.class, Long.class,
        short.class, Short.class
    );
}
