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
 * Provides optimized reflection caching for {@link java.lang.Object}.
 * The root cached class that accepts all argument types for assignment,
 * making it a universal fallback for method invocation.
 */
public class ObjectCachedClass extends CachedClass {
    /**
     * Constructs a cached class representation for {@link Object}.
     *
     * @param classInfo the class information associated with {@link Object}
     */
    public ObjectCachedClass(ClassInfo classInfo) {
        super(Object.class, classInfo);
    }

    /**
     * Returns {@code null} since {@link Object} has no superclass in the type hierarchy.
     *
     * @return {@code null} always
     */
    @Override
    public synchronized CachedClass getCachedSuperClass() {
        return null;
    }

    /**
     * Accepts any class as assignable to {@link Object}.
     * This is always true since all Java classes inherit from {@link Object}.
     *
     * @param argument the class to check
     * @return {@code true} always
     */
    @Override
    public boolean isAssignableFrom(Class argument) {
        return true;
    }
}
