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

public class DoubleCachedClass extends NumberCachedClass { // Double, double
    private final boolean allowNull;

    public DoubleCachedClass(Class klazz, ClassInfo classInfo, boolean allowNull) {
        super(klazz, classInfo);
        this.allowNull = allowNull;
    }

    @Override
    public boolean isDirectlyAssignable(Object argument) {
        return (allowNull && argument == null) || argument instanceof Double;
    }

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
