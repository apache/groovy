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
package org.apache.groovy.xml.util;

import groovy.xml.XmlRuntimeException;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * Internal helper for optional Jackson databinding support.
 * Uses reflection to avoid a compile-time dependency on jackson-databind.
 */
public class JacksonHelper {

    private static final String OBJECT_MAPPER_CLASS = "com.fasterxml.jackson.databind.ObjectMapper";

    // Lazily cached mapper and method — initialized on first use, thread-safe via volatile + holder
    private static volatile Object cachedMapper;
    private static volatile Method cachedConvertValue;

    /**
     * Converts a Map to a typed object using Jackson's ObjectMapper.convertValue.
     * Requires jackson-databind on the classpath.
     *
     * @param map  the source map
     * @param type the target type
     * @param <T>  the target type
     * @return the converted object
     * @throws XmlRuntimeException if jackson-databind is not available or conversion fails
     */
    public static <T> T convertMapToType(Map<String, Object> map, Class<T> type) {
        try {
            if (cachedMapper == null) {
                initMapper();
            }
            return type.cast(cachedConvertValue.invoke(cachedMapper, map, type));
        } catch (XmlRuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new XmlRuntimeException("Failed to convert XML map to " + type.getName(), e);
        }
    }

    private static synchronized void initMapper() {
        if (cachedMapper != null) return;
        Class<?> omClass = loadOptionalClass(OBJECT_MAPPER_CLASS);
        if (omClass == null) {
            throw new XmlRuntimeException(
                    "Typed XML parsing requires jackson-databind on the classpath. "
                            + "Add com.fasterxml.jackson.core:jackson-databind to your dependencies.");
        }
        try {
            cachedMapper = omClass.getDeclaredConstructor().newInstance();
            cachedConvertValue = omClass.getMethod("convertValue", Object.class, Class.class);
        } catch (Exception e) {
            throw new XmlRuntimeException("Failed to initialize Jackson ObjectMapper", e);
        }
    }

    private static Class<?> loadOptionalClass(String className) {
        ClassLoader[] loaders = {
                Thread.currentThread().getContextClassLoader(),
                JacksonHelper.class.getClassLoader(),
                ClassLoader.getSystemClassLoader()
        };
        for (ClassLoader loader : loaders) {
            if (loader == null) continue;
            try {
                return Class.forName(className, false, loader);
            } catch (ClassNotFoundException ignore) {
                // try next class loader
            }
        }
        return null;
    }
}
