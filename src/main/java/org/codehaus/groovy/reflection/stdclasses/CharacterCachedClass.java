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

/**
 * Provides optimized reflection caching for {@code char} and {@link java.lang.Character}.
 * Optionally allows {@code null} values for the boxed {@link Character} class variant.
 */
public class CharacterCachedClass extends CachedClass {
    private final boolean allowNull;

    /**
     * Constructs a cached class representation for the given character class.
     *
     * @param klazz the character class to cache (either {@code char.class} or {@link Character}.class)
     * @param classInfo the class information associated with this cached class
     * @param allowNull {@code true} to allow {@code null} values (for {@link Character}.class), {@code false} for primitive {@code char}
     */
    public CharacterCachedClass(Class klazz, ClassInfo classInfo, boolean allowNull) {
        super(klazz, classInfo);
        this.allowNull = allowNull;
    }

    /**
     * Checks if the given argument is directly assignable without type conversion.
     *
     * @param argument the argument to check
     * @return {@code true} if the argument is a {@link Character} instance, or {@code null} is allowed, {@code false} otherwise
     */
    @Override
    public boolean isDirectlyAssignable(Object argument) {
        return (allowNull && argument == null) || argument instanceof Character;
    }

    /**
     * Determines if the given class can be transformed to char/Character.
     *
     * @param classToTransformFrom the source class to check
     * @return {@code true} if the class can be transformed to char, {@code false} otherwise
     */
    @Override
    public boolean isAssignableFrom(Class classToTransformFrom) {
        return (allowNull && classToTransformFrom == null)
                || classToTransformFrom == Character.class
                || classToTransformFrom == Character.TYPE;
    }
}
