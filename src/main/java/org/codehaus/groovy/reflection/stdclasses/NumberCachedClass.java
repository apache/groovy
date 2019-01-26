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
 * @author Alex.Tkachman
 */
public class NumberCachedClass extends CachedClass {

    public NumberCachedClass(Class klazz, ClassInfo classInfo) {
        super(klazz, classInfo);
    }

    public Object coerceArgument(Object argument) {
        if (argument instanceof Number) {
            return coerceNumber(argument);
        }
        return argument;

    }

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
