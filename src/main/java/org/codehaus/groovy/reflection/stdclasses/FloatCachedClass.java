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

public class FloatCachedClass extends NumberCachedClass {
    private final boolean allowNull;

    public FloatCachedClass(Class klazz, ClassInfo classInfo, boolean allowNull) {
        super(klazz, classInfo);
        this.allowNull = allowNull;
    }

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

    @Override
    public boolean isDirectlyAssignable(Object argument) {
        return (allowNull && argument == null) || argument instanceof Float;
    }

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
