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

import groovy.lang.GString;
import org.codehaus.groovy.reflection.CachedClass;
import org.codehaus.groovy.reflection.ClassInfo;

/**
 * Provides optimized reflection caching for {@link java.lang.String}.
 * Coerces {@link GString} arguments to {@link String} for type-safe method invocation.
 */
public class StringCachedClass extends CachedClass {
    private static final Class STRING_CLASS = String.class;
    private static final Class GSTRING_CLASS = GString.class;

    /**
     * Constructs a cached class representation for {@link String}.
     *
     * @param classInfo the class information associated with {@link String}
     */
    public StringCachedClass(ClassInfo classInfo) {
        super(STRING_CLASS, classInfo);
    }

    /**
     * Checks if the given argument is directly assignable to {@link String}.
     *
     * @param argument the argument to check
     * @return {@code true} if the argument is an instance of {@link String}, {@code false} otherwise
     */
    @Override
    public boolean isDirectlyAssignable(Object argument) {
        return argument instanceof String;
    }

    /**
     * Determines if the given class can be transformed to {@link String}.
     * Accepts {@code null}, {@link String}, and {@link GString} subtypes.
     *
     * @param classToTransformFrom the source class to check
     * @return {@code true} if the class can be transformed to {@link String}, {@code false} otherwise
     */
    @Override
    public boolean isAssignableFrom(Class classToTransformFrom) {
        return  classToTransformFrom == null
              || classToTransformFrom == STRING_CLASS
              || GSTRING_CLASS.isAssignableFrom(classToTransformFrom);
    }

    /**
     * Coerces the given argument to {@link String}.
     * Converts {@link GString} arguments to {@link String} by calling {@code toString()}.
     *
     * @param argument the argument to coerce
     * @return the argument as a {@link String}, or the original argument if not a {@link GString}
     */
    @Override
    public Object coerceArgument(Object argument) {
        return argument instanceof GString ? argument.toString() : argument;
    }
}
