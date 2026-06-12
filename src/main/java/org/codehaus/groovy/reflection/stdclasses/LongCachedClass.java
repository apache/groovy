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
package org.codehaus.groovy.reflection.stdclasses;

import org.codehaus.groovy.reflection.ClassInfo;

/**
 * Provides optimized reflection caching for {@code long} and {@link java.lang.Long}.
 * Coerces numeric arguments to long values for type-safe method invocation.
 * Optionally allows {@code null} values for the boxed {@link Long} class variant.
 */
public class LongCachedClass extends NumberCachedClass {
    private final boolean allowNull;

    /**
     * Constructs a cached class representation for the given long class.
     *
     * @param klazz the long class to cache (either {@code long.class} or {@link Long}.class)
     * @param classInfo the class information associated with this cached class
     * @param allowNull {@code true} to allow {@code null} values (for {@link Long}.class), {@code false} for primitive {@code long}
     */
    public LongCachedClass(Class klazz, ClassInfo classInfo, boolean allowNull) {
        super(klazz, classInfo);
        this.allowNull = allowNull;
    }

    /**
     * Coerces the given numeric argument to a long value.
     *
     * @param argument the argument to coerce
     * @return the argument as a {@code long}, or the original argument if not a number
     */
    @Override
    public Object coerceArgument(Object argument) {
        if (argument instanceof Long) {
            return argument;
        }

        if (argument instanceof Number) {
            return ((Number) argument).longValue();
        }
        return argument;
    }

    /**
     * Checks if the given argument is directly assignable without type conversion.
     *
     * @param argument the argument to check
     * @return {@code true} if the argument is a {@link Long} instance, or {@code null} is allowed, {@code false} otherwise
     */
    @Override
    public boolean isDirectlyAssignable(Object argument) {
        return (allowNull && argument == null) || argument instanceof Long;
    }

    /**
     * Determines if the given class can be transformed to long/Long.
     * Accepts integral types and boxed integral types.
     *
     * @param classToTransformFrom the source class to check
     * @return {@code true} if the class can be transformed to long, {@code false} otherwise
     */
    @Override
    public boolean isAssignableFrom(Class classToTransformFrom) {
        return (allowNull && classToTransformFrom == null)
                || classToTransformFrom == Integer.class
                || classToTransformFrom == Long.class
                || classToTransformFrom == Short.class
                || classToTransformFrom == Byte.class
                || classToTransformFrom == Integer.TYPE
                || classToTransformFrom == Long.TYPE
                || classToTransformFrom == Short.TYPE
                || classToTransformFrom == Byte.TYPE;
    }
}
