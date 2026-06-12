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

import org.codehaus.groovy.reflection.CachedClass;
import org.codehaus.groovy.reflection.ClassInfo;

import java.math.BigInteger;

/**
 * Base class for optimized reflection caching of numeric types.
 * Provides common type coercion logic for primitive and boxed numeric types,
 * as well as {@link java.math.BigInteger} and {@link java.math.BigDecimal}.
 */
public class NumberCachedClass extends CachedClass {

    /**
     * Constructs a cached class representation for a numeric class.
     *
     * @param klazz the numeric class to cache
     * @param classInfo the class information associated with this cached class
     */
    public NumberCachedClass(Class klazz, ClassInfo classInfo) {
        super(klazz, classInfo);
    }

    /**
     * Coerces the given argument if it is a {@link Number}.
     * Delegates to {@link #coerceNumber} for specialized conversion logic.
     *
     * @param argument the argument to coerce
     * @return the coerced number or the original argument if not a {@link Number}
     */
    @Override
    public Object coerceArgument(Object argument) {
        if (argument instanceof Number) {
            return coerceNumber(argument);
        }
        return argument;

    }

    /**
     * Determines if the given class can be transformed to this numeric type.
     * Accepts {@code null}, {@link Number} subclasses, and primitive numeric types.
     *
     * @param classToTransformFrom the source class to check
     * @return {@code true} if the class can be transformed to this numeric type, {@code false} otherwise
     */
    @Override
    public boolean isAssignableFrom(Class classToTransformFrom) {
        return classToTransformFrom == null
            || Number.class.isAssignableFrom(classToTransformFrom)
            || classToTransformFrom == Byte.TYPE
            || classToTransformFrom == Short.TYPE
            || classToTransformFrom == Integer.TYPE
            || classToTransformFrom == Long.TYPE
            || classToTransformFrom == Float.TYPE
            || classToTransformFrom == Double.TYPE
                ;
    }

    /**
     * Performs specialized numeric coercion for the target type.
     * Subclasses override this to provide type-specific conversion logic.
     *
     * @param argument the numeric argument to coerce
     * @return the coerced value
     */
    private Object coerceNumber(Object argument) {
        Class param = getTheClass();
        if (param == Byte.class /*|| param == Byte.TYPE*/) {
            argument = ((Number) argument).byteValue();
        } else if (param == BigInteger.class) {
            argument = new BigInteger(String.valueOf((Number) argument));
        }

        return argument;
    }
}
