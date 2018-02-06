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

import java.util.Map;

import static org.codehaus.groovy.ast.ClassHelper.VOID_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.boolean_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.byte_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.char_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.double_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.float_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.int_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.long_TYPE;
import static org.codehaus.groovy.ast.ClassHelper.short_TYPE;

/**
 * A utility for extracting type description
 */
public class TypeDescriptionUtil {
    private static final String REF_DESCRIPTION = "L";
    private static final Map<ClassNode, String> TYPE_TO_DESCRIPTION_MAP = Maps.of(
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

    private static final Map<String, ClassNode> NAME_TO_TYPE_MAP = Maps.of(
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

    public static boolean isPrimitiveType(String name) {
        return NAME_TO_TYPE_MAP.containsKey(name);
    }

    public static boolean isPrimitiveType(ClassNode type) {
        return TYPE_TO_DESCRIPTION_MAP.containsKey(type);
    }

    public static String getDescriptionByType(ClassNode type) {
        String desc = TYPE_TO_DESCRIPTION_MAP.get(type);

        if (null == desc) { // reference type
            if (!type.isArray()) {
                return makeRefDescription(type.getName());
            }

            StringBuilder arrayDescription = new StringBuilder(32);
            Tuple2<ClassNode, Integer> arrayInfo = extractArrayInfo(type);

            for (int i = 0, dimension = arrayInfo.getSecond(); i < dimension; i++) {
                arrayDescription.append("[");
            }

            ClassNode componentType = arrayInfo.getFirst();
            return arrayDescription.append(getDescriptionByType(componentType)).toString();
        }

        return desc;
    }

    public static String getDescriptionByName(String name) {
        ClassNode type = NAME_TO_TYPE_MAP.get(name);

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
}
