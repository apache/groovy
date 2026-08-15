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
import org.codehaus.groovy.runtime.InvokerHelper;

/**
 * Binary-compatibility facade for class files compiled by Groovy 4.0.5+
 * (GROOVY-12257).
 * <p>
 * The {@code @Immutable} transform in Groovy 4.0.5 through 4.0.x emitted
 * {@code INVOKESTATIC} calls to {@link #cloneObject(Object)} into generated
 * constructors and getters for defensive copies (GROOVY-10747). The original
 * class was removed together with the {@code $getLookup} machinery it relied
 * on (GROOVY-10931), which broke such pre-compiled classes with a
 * {@link NoClassDefFoundError} at first instantiation. This facade restores
 * the call target with equivalent behaviour, minus the lookup dependency:
 * arrays are cloned via {@link ArrayUtil} exactly as before, and other
 * {@link Cloneable}s through their public {@code clone()} via the MOP.
 * <p>
 * Nothing compiled by Groovy 5+ references this class; its transforms emit
 * {@code invokedynamic}-based or {@code InvokerHelper} clone calls instead.
 *
 * @since 4.0.5
 * @deprecated retained only so Groovy 4 compiled bytecode keeps linking;
 * not emitted by any current transform and not intended for direct use
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

        // same NoSuchMethodException as the original lookup-based
        // implementation when clone() is not public
        clazz.getMethod("clone");

        return (T) InvokerHelper.invokeMethod(object, "clone", InvokerHelper.EMPTY_ARGS);
    }

    private ObjectUtil() {}
}
