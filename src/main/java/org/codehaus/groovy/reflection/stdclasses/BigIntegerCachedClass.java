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

import java.math.BigInteger;

/**
 * Provides optimized reflection caching for {@link java.math.BigInteger}.
 * Coerces integral and big numeric types to {@link BigInteger} for type-safe method invocation.
 */
public class BigIntegerCachedClass extends NumberCachedClass {
    /**
     * Constructs a cached class representation for {@link BigInteger}.
     *
     * @param klazz the {@link BigInteger} class to cache
     * @param classInfo the class information associated with this cached class
     */
    public BigIntegerCachedClass(Class klazz, ClassInfo classInfo) {
        super(klazz, classInfo);
    }

    /**
     * Checks if the given argument is directly assignable to {@link BigInteger}.
     *
     * @param argument the argument to check
     * @return {@code true} if the argument is an instance of {@link BigInteger}, {@code false} otherwise
     */
    @Override
    public boolean isDirectlyAssignable(Object argument) {
        return argument instanceof BigInteger;
    }

    /**
     * Determines if the given class can be transformed to {@link BigInteger}.
     * Accepts integral types, boxed integral types, and other big numeric types.
     *
     * @param classToTransformFrom the source class to check
     * @return {@code true} if the class can be transformed to {@link BigInteger}, {@code false} otherwise
     */
    @Override
    public boolean isAssignableFrom(Class classToTransformFrom) {
        return classToTransformFrom == null
            || classToTransformFrom == Integer.class
            || classToTransformFrom == Short.class
            || classToTransformFrom == Byte.class
            || classToTransformFrom == BigInteger.class
            || classToTransformFrom == Long.class
            || classToTransformFrom == Integer.TYPE
            || classToTransformFrom == Short.TYPE
            || classToTransformFrom == Byte.TYPE
            || classToTransformFrom == Long.TYPE
            || BigInteger.class.isAssignableFrom(classToTransformFrom);
    }
}
