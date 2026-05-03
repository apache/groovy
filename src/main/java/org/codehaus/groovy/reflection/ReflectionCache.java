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
package org.codehaus.groovy.reflection;

/**
 * Central registry for obtaining cached class information.
 * <p>
 * Provides factory methods for retrieving {@link CachedClass} instances for a given Java class.
 * Maintains static references to commonly-used classes for efficient access.
 */
public class ReflectionCache {

    /**
     * The cached representation of {@code Object.class}.
     * Frequently accessed during type compatibility checks.
     */
    public static final CachedClass OBJECT_CLASS = getCachedClass(Object.class);

    /**
     * The cached representation of {@code Object[].class}.
     * Used as the superclass for non-primitive array types.
     */
    public static final CachedClass OBJECT_ARRAY_CLASS = getCachedClass(Object[].class);

    /**
     * Retrieves the {@code CachedClass} for the given Java class.
     * Returns {@code null} if the class is {@code null}.
     *
     * @param klazz the Java class for which to obtain cache information
     * @return the cached class information, or {@code null} if {@code klazz} is {@code null}
     */
    public static CachedClass getCachedClass(Class klazz) {
        if (klazz == null)
          return null;

        return ClassInfo.getClassInfo(klazz).getCachedClass();
    }
}
