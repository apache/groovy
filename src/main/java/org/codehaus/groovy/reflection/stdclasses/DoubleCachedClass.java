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
 * Provides optimized reflection caching for {@code double} and {@link java.lang.Double}.
 * Coerces numeric arguments to double values, with validation to prevent overflow to infinity.
 * Optionally allows {@code null} values for the boxed {@link Double} class variant.
 */
public class DoubleCachedClass extends NumberCachedClass {
    private final boolean allowNull;

    /**
     * Constructs a cached class representation for the given double class.
     *
     * @param klazz the double class to cache (either {@code double.class} or {@link Double}.class)
     * @param classInfo the class information associated with this cached class
     * @param allowNull {@code true} to allow {@code null} values (for {@link Double}.class), {@code false} for primitive {@code double}
     */
    public DoubleCachedClass(Class klazz, ClassInfo classInfo, boolean allowNull) {
        super(klazz, classInfo);
        this.allowNull = allowNull;
    }

    /**
     * Checks if the given argument is directly assignable without type conversion.
     *
     * @param argument the argument to check
     * @return {@code true} if the argument is a {@link Double} instance, or {@code null} is allowed, {@code false} otherwise
     */
    @Override
    public boolean isDirectlyAssignable(Object argument) {
        return (allowNull && argument == null) || argument instanceof Double;
    }

    /**
     * Coerces the given numeric argument to a double value.
     * Validates that {@link BigDecimal} conversions do not overflow to infinity.
     *
     * @param argument the argument to coerce
     * @return the argument as a {@code double}, or the original argument if not a number
     * @throws IllegalArgumentException if a {@link BigDecimal} conversion results in infinity
     */
    @Override
    public Object coerceArgument(Object argument) {
        if (argument instanceof Double) {
            return argument;
        }

        if (argument instanceof Number) {
            Double res = ((Number) argument).doubleValue();
            if (argument instanceof BigDecimal && res.isInfinite()) {
                throw new IllegalArgumentException(Double.class + " out of range while converting from BigDecimal");
            }
            return res;
        }
        return argument;
    }

    /**
     * Determines if the given class can be transformed to double/Double.
     * Accepts all numeric types, including integral and big numeric types.
     *
     * @param classToTransformFrom the source class to check
     * @return {@code true} if the class can be transformed to double, {@code false} otherwise
     */
    @Override
    public boolean isAssignableFrom(Class classToTransformFrom) {
        return (allowNull && classToTransformFrom == null)
                || classToTransformFrom == Double.class
                || classToTransformFrom == Integer.class
                || classToTransformFrom == Long.class
                || classToTransformFrom == Short.class
                || classToTransformFrom == Byte.class
                || classToTransformFrom == Float.class
                || classToTransformFrom == Double.TYPE
                || classToTransformFrom == Integer.TYPE
                || classToTransformFrom == Long.TYPE
                || classToTransformFrom == Short.TYPE
                || classToTransformFrom == Byte.TYPE
                || classToTransformFrom == Float.TYPE
                || classToTransformFrom == BigDecimal.class
                || classToTransformFrom == BigInteger.class
                || (classToTransformFrom!=null && BigDecimal.class.isAssignableFrom(classToTransformFrom))
                || (classToTransformFrom!=null && BigInteger.class.isAssignableFrom(classToTransformFrom))
                ;
    }
}
