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
package org.apache.groovy.runtime;

import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.runtime.ArrayUtil;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;

/**
 * Util for object's operations with checks
 * @since 4.0.5
 */
@Deprecated
public class ObjectUtil {
    /**
     * Clone the specified object
     *
     * @param object the object to clone
     * @return the cloned object
     * @param <T> the object type
     * @throws Throwable some exception or error
     * @since 4.0.5
     */
    @SuppressWarnings("unchecked")
    public static <T> T cloneObject(T object) throws Throwable {
        if (null == object) return null;

        final Class<?> clazz = object.getClass();
        if (!(object instanceof Cloneable)) throw new CloneNotSupportedException(clazz.getName());

        if (clazz.isArray()) {
            if (clazz.getComponentType().isPrimitive()) {
                if (byte[].class == clazz) {
                    return (T) ArrayUtil.cloneArray((byte[]) object);
                } else if (short[].class == clazz) {
                    return (T) ArrayUtil.cloneArray((short[]) object);
                } else if (int[].class == clazz) {
                    return (T) ArrayUtil.cloneArray((int[]) object);
                } else if (char[].class == clazz) {
                    return (T) ArrayUtil.cloneArray((char[]) object);
                } else if (long[].class == clazz) {
                    return (T) ArrayUtil.cloneArray((long[]) object);
                } else if (float[].class == clazz) {
                    return (T) ArrayUtil.cloneArray((float[]) object);
                } else if (double[].class == clazz) {
                    return (T) ArrayUtil.cloneArray((double[]) object);
                } else if (boolean[].class == clazz) {
                    return (T) ArrayUtil.cloneArray((boolean[]) object);
                }

                throw new GroovyBugError(clazz.getName() + " is not an array of primitive type"); // should never happen
            }
            return (T) ArrayUtil.cloneArray((Object[]) object);
        }

        final Method cloneMethod = clazz.getMethod("clone");
        final MethodHandle cloneMethodHandle = LOOKUP.in(clazz).unreflect(cloneMethod);

        return (T) cloneMethodHandle.invokeWithArguments(object);
    }

    /**
     * Returns the method handle of {@link ObjectUtil#cloneObject(Object)}
     *
     * @return the method handle
     * @since 4.0.5
     */
    public static MethodHandle getCloneObjectMethodHandle() {
        return MethodHandleHolder.CLONE_OBJECT_METHOD_HANDLE;
    }

    private static class MethodHandleHolder {
        private static final MethodHandle CLONE_OBJECT_METHOD_HANDLE;
        static {
            final Class<ObjectUtil> objectUtilClass = ObjectUtil.class;
            Method cloneObjectMethod;
            try {
                cloneObjectMethod = objectUtilClass.getDeclaredMethod("cloneObject", Object.class);
            } catch (NoSuchMethodException e) {
                throw new GroovyBugError("Failed to find `cloneObject` method in class `" + objectUtilClass.getName() + "`", e);
            }

            try {
                CLONE_OBJECT_METHOD_HANDLE = LOOKUP.in(objectUtilClass).unreflect(cloneObjectMethod);
            } catch (IllegalAccessException e) {
                throw new GroovyBugError("Failed to create method handle for " + cloneObjectMethod);
            }
        }
        private MethodHandleHolder() {}
    }

    private ObjectUtil() {}

    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
}
