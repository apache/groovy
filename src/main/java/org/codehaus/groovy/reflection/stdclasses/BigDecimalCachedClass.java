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
import org.codehaus.groovy.runtime.typehandling.NumberMath;

import java.math.BigDecimal;

/**
 * Provides optimized reflection caching for {@link java.math.BigDecimal}.
 * Coerces numeric arguments to {@link BigDecimal} for type-safe method invocation.
 */
public class BigDecimalCachedClass extends DoubleCachedClass {
    /**
     * Constructs a cached class representation for {@link BigDecimal}.
     *
     * @param klazz the {@link BigDecimal} class to cache
     * @param classInfo the class information associated with this cached class
     */
    public BigDecimalCachedClass(Class klazz, ClassInfo classInfo) {
        super(klazz, classInfo, true);
    }

    /**
     * Checks if the given argument is directly assignable to {@link BigDecimal}.
     *
     * @param argument the argument to check
     * @return {@code true} if the argument is an instance of {@link BigDecimal}, {@code false} otherwise
     */
    @Override
    public boolean isDirectlyAssignable(Object argument) {
        return argument instanceof BigDecimal;
    }

    /**
     * Coerces the given numeric argument to {@link BigDecimal}.
     *
     * @param argument the argument to coerce
     * @return the argument as a {@link BigDecimal}, or the original argument if not a number
     */
    @Override
    public Object coerceArgument(Object argument) {
        if (argument instanceof Number) {
            return NumberMath.toBigDecimal((Number) argument);
        }
        return argument;
    }
}
