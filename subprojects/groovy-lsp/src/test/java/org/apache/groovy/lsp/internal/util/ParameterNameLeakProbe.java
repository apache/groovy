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
package org.apache.groovy.lsp.internal.util;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;

/**
 * Defines {@code cov.Gone} and asks for its parameter names without
 * letting Groovy's runtime see the class. The caller must drop every
 * strong reference before checking the returned weak references.
 */
public final class ParameterNameLeakProbe {

    private ParameterNameLeakProbe() {
    }

    public static WeakReference<?>[] defineAndName(final byte[] bytes) throws Exception {
        Isolated loader = new Isolated(ParameterNameLeakProbe.class.getClassLoader());
        Class<?> type = loader.define(bytes);
        Method method = type.getDeclaredMethod("ping", int.class);
        ReflectionParameterNames.names(method);
        return new WeakReference<?>[] {new WeakReference<>(loader), new WeakReference<>(type)};
    }

    private static final class Isolated extends ClassLoader {
        private Isolated(final ClassLoader parent) {
            super(parent);
        }

        private Class<?> define(final byte[] bytes) {
            return defineClass("cov.Gone", bytes, 0, bytes.length);
        }
    }
}
