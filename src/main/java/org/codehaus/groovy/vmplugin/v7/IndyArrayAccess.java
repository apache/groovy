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
package org.codehaus.groovy.vmplugin.v7;

import org.codehaus.groovy.GroovyBugError;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Array;
import java.util.HashMap;

/**
 * Class for handling array access through invokedynamic using static callsite information
 *
 * @since 2.5.0
 */
public class IndyArrayAccess {

    private static final MethodHandle notNegative, normalizeIndex;

    static {
        try {
            notNegative = MethodHandles.lookup().findStatic(IndyArrayAccess.class, "notNegative", MethodType.methodType(boolean.class, int.class));
            normalizeIndex = MethodHandles.lookup().findStatic(IndyArrayAccess.class, "normalizeIndex", MethodType.methodType(int.class, Object.class, int.class));
        } catch (ReflectiveOperationException e) {
            throw new GroovyBugError(e);
        }
    }

    private static final HashMap<Class<?>, MethodHandle> getterMap, setterMap;

    static {
        getterMap = new HashMap<>();
        Class<?>[] classes = new Class<?>[]{
                int[].class, byte[].class, short[].class, long[].class,
                double[].class, float[].class,
                boolean[].class, char[].class, Object[].class};
        for (Class<?> arrayClass : classes) {
            MethodHandle handle = buildGetter(arrayClass);
            getterMap.put(arrayClass, handle);
        }
        setterMap = new HashMap<>();
        for (Class<?> arrayClass : classes) {
            MethodHandle handle = buildSetter(arrayClass);
            setterMap.put(arrayClass, handle);
        }
    }

    private static MethodHandle buildGetter(Class<?> arrayClass) {
        MethodHandle get = MethodHandles.arrayElementGetter(arrayClass);
        MethodHandle fallback = MethodHandles.explicitCastArguments(get, get.type().changeParameterType(0, Object.class));

        fallback = MethodHandles.dropArguments(fallback, 2, int.class);
        MethodType reorderType = fallback.type().
                insertParameterTypes(0, int.class).
                dropParameterTypes(2, 3);
        fallback = MethodHandles.permuteArguments(fallback, reorderType, 1, 0, 0);

        fallback = MethodHandles.foldArguments(fallback, normalizeIndex);
        fallback = MethodHandles.explicitCastArguments(fallback, get.type());

        MethodHandle guard = MethodHandles.dropArguments(notNegative, 0, arrayClass);
        MethodHandle handle = MethodHandles.guardWithTest(guard, get, fallback);
        return handle;
    }

    private static MethodHandle buildSetter(Class<?> arrayClass) {
        MethodHandle set = MethodHandles.arrayElementSetter(arrayClass);
        MethodHandle fallback = MethodHandles.explicitCastArguments(set, set.type().changeParameterType(0, Object.class));

        fallback = MethodHandles.dropArguments(fallback, 3, int.class);
        MethodType reorderType = fallback.type().
                insertParameterTypes(0, int.class).
                dropParameterTypes(4, 5);
        fallback = MethodHandles.permuteArguments(fallback, reorderType, 1, 0, 3, 0);

        fallback = MethodHandles.foldArguments(fallback, normalizeIndex);
        fallback = MethodHandles.explicitCastArguments(fallback, set.type());

        MethodHandle guard = MethodHandles.dropArguments(notNegative, 0, arrayClass);
        MethodHandle handle = MethodHandles.guardWithTest(guard, set, fallback);
        return handle;
    }

    private static int getLength(Object array) {
        if (null == array || !array.getClass().isArray()) {
            return 0;
        }

        return Array.getLength(array);
    }

    private static int normalizeIndex(Object array, int i) {
        int temp = i;
        int size = getLength(array);
        i += size;
        if (i < 0) {
            throw new ArrayIndexOutOfBoundsException("Negative array index [" + temp + "] too large for array size " + size);
        }
        return i;
    }

    public static boolean notNegative(int index) {
        return index >= 0;
    }

    public static MethodHandle arrayGet(MethodType type) {
        Class<?> key = type.parameterType(0);
        MethodHandle res = getterMap.get(key);
        if (res != null) return res;
        res = buildGetter(key);
        res = MethodHandles.explicitCastArguments(res, type);
        return res;
    }

    public static MethodHandle arraySet(MethodType type) {
        Class<?> key = type.parameterType(0);
        MethodHandle res = setterMap.get(key);
        if (res != null) return res;
        res = buildSetter(key);
        res = MethodHandles.explicitCastArguments(res, type);
        return res;
    }
}
