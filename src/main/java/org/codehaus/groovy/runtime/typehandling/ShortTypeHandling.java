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
package org.codehaus.groovy.runtime.typehandling;

import groovy.lang.GString;

import java.util.Arrays;

/**
 * High-performance type casting and conversion utilities.
 * <p>
 * Provides fast-path type conversions for common cases. These methods are designed
 * to avoid dependency on the Groovy runtime and should be used directly only when
 * the type constraints documented in each method are met.
 * <p>
 * For general-purpose type coercion, use {@link DefaultTypeTransformation} instead.
 */
public class ShortTypeHandling {

    /**
     * Casts an object to a Class object.
     * <p>
     * Accepts Class objects directly, or a string representation of a class name
     * which is loaded via {@code Class.forName}.
     *
     * @param object a Class object or string class name
     * @return the Class object, or null if input is null
     * @throws GroovyCastException if the object is not a Class or valid class name
     */
    public static Class castToClass(Object object) {
        if (object==null) return null;
        if (object instanceof Class) return (Class) object;
        try {
            return Class.forName(object.toString());
        } catch (Exception e) {
            throw new GroovyCastException(object, Class.class, e);
        }
    }

    /**
     * Casts an object to a String representation.
     * <p>
     * Arrays are converted using {@code Arrays.toString} format.
     * Primitive arrays receive special handling to show element values.
     * Other objects use their standard {@code toString()} method.
     *
     * @param object any object (may be null)
     * @return string representation, or null if input is null
     */
    public static String castToString(Object object) {
        if (object==null) return null;
        if (object.getClass().isArray()) {
            if (object instanceof boolean[]) return Arrays.toString((boolean[])object);
            if (object instanceof byte[]) return Arrays.toString((byte[])object);
            if (object instanceof char[]) return new String((char[])object);
            if (object instanceof double[]) return Arrays.toString((double[])object);
            if (object instanceof float[]) return Arrays.toString((float[])object);
            if (object instanceof int[]) return Arrays.toString((int[])object);
            if (object instanceof long[]) return Arrays.toString((long[])object);
            if (object instanceof short[]) return Arrays.toString((short[])object);
            return Arrays.toString((Object[])object);
        }
        return object.toString();
    }

    /**
     * Casts an object to an Enum value of the specified type.
     * <p>
     * <strong>Note:</strong> This method requires that the supplied enum
     * is not fitting a Collection case for casting.
     * <p>
     * Accepts Enum objects already of the correct type, or a String/GString
     * representation of the enum constant name.
     *
     * @param object an enum value, or a string enum constant name
     * @param type the target enum class
     * @return the Enum value, or null if input is null
     * @throws GroovyCastException if the object cannot be converted to the enum type
     */
    public static Enum castToEnum(Object object, Class<? extends Enum> type) {
        if (object==null) return null;
        if (type.isInstance(object)) return (Enum) object;
        if (object instanceof String || object instanceof GString) {
            return Enum.valueOf(type, object.toString());
        }
        throw new GroovyCastException(object, type);
    }

    /**
     * Casts an object to a char value (Character wrapper).
     * <p>
     * Accepts Character objects, Numbers (converted via intValue), or Strings of length 1.
     *
     * @param object a Character, Number, or single-character String
     * @return a Character with the coerced value, or null if input is null
     * @throws GroovyCastException if the object cannot be converted to char
     */
    public static Character castToChar(Object object) {
        if (object==null) return null;
        if (object instanceof Character) {
            return (Character) object;
        } else if (object instanceof Number value) {
            return (char) value.intValue();
        }
        String text = object.toString();
        if (text.length() == 1) {
            return text.charAt(0);
        } else {
            throw new GroovyCastException(text,char.class);
        }
    }
}
