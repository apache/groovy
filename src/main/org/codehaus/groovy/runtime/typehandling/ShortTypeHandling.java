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
 * Class providing various short paths for type conversions. Read the comments
 * to what conditions have to be met to get valid results!
 * Any method here must not depend on the groovy runtime.
 */
public class ShortTypeHandling {

    public static Class castToClass(Object object) {
        if (object==null) return null;
        if (object instanceof Class) return (Class) object;
        try {
            return Class.forName(object.toString());
        } catch (Exception e) {
            throw new GroovyCastException(object, Class.class, e);
        }
    }

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
     * this class requires that the supplied enum is not fitting a 
     * Collection case for casting
     */
    public static Enum castToEnum(Object object, Class<? extends Enum> type) {
        if (object==null) return null;
        if (type.isInstance(object)) return (Enum) object;
        if (object instanceof String || object instanceof GString) {
            return Enum.valueOf(type, object.toString());
        }
        throw new GroovyCastException(object, type);
    }

    public static Character castToChar(Object object) {
        if (object==null) return null;
        if (object instanceof Character) {
            return (Character) object;
        } else if (object instanceof Number) {
            Number value = (Number) object;
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
