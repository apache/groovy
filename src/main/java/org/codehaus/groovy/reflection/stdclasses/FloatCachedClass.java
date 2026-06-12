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

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Provides optimized reflection caching for {@code float} and {@link java.lang.Float}.
 * Coerces numeric arguments to float values, with validation to prevent overflow to infinity.
 * Optionally allows {@code null} values for the boxed {@link Float} class variant.
 */
public class FloatCachedClass extends NumberCachedClass {
    private final boolean allowNull;

    /**
     * Constructs a cached class representation for the given float class.
     *
     * @param klazz the float class to cache (either {@code float.class} or {@link Float}.class)
     * @param classInfo the class information associated with this cached class
     * @param allowNull {@code true} to allow {@code null} values (for {@link Float}.class), {@code false} for primitive {@code float}
     */
    public FloatCachedClass(Class klazz, ClassInfo classInfo, boolean allowNull) {
        super(klazz, classInfo);
        this.allowNull = allowNull;
    }

    /**
     * Coerces the given numeric argument to a float value.
     * Validates that {@link BigDecimal} conversions do not overflow to infinity.
     *
     * @param argument the argument to coerce
     * @return the argument as a {@code float}, or the original argument if not a number
     * @throws IllegalArgumentException if a {@link BigDecimal} conversion results in infinity
     */
    @Override
    public Object coerceArgument(Object argument) {
        if (argument instanceof Float) {
            return argument;
        }

        if (argument instanceof Number) {
            Float res = ((Number) argument).floatValue();
            if (argument instanceof BigDecimal && res.isInfinite()) {
                throw new IllegalArgumentException(Float.class + " out of range while converting from BigDecimal");
            }
            return res;
        }
        return argument;
    }

    /**
     * Checks if the given argument is directly assignable without type conversion.
     *
     * @param argument the argument to check
     * @return {@code true} if the argument is a {@link Float} instance, or {@code null} is allowed, {@code false} otherwise
     */
    @Override
    public boolean isDirectlyAssignable(Object argument) {
        return (allowNull && argument == null) || argument instanceof Float;
    }

    /**
     * Determines if the given class can be transformed to float/Float.
     * Accepts integral and big numeric types.
     *
     * @param classToTransformFrom the source class to check
     * @return {@code true} if the class can be transformed to float, {@code false} otherwise
     */
    @Override
    public boolean isAssignableFrom(Class classToTransformFrom) {
        return (allowNull && classToTransformFrom == null)
                || classToTransformFrom == Float.class
                || classToTransformFrom == Integer.class
                || classToTransformFrom == Long.class
                || classToTransformFrom == Short.class
                || classToTransformFrom == Byte.class
                || classToTransformFrom == Float.TYPE
                || classToTransformFrom == Integer.TYPE
                || classToTransformFrom == Long.TYPE
                || classToTransformFrom == Short.TYPE
                || classToTransformFrom == Byte.TYPE
                || classToTransformFrom == BigDecimal.class
                || classToTransformFrom == BigInteger.class;
    }
}
