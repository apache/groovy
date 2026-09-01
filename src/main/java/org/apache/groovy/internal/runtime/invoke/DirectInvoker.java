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
package org.apache.groovy.internal.runtime.invoke;

import groovy.transform.Internal;

/**
 * JIT-constant trampoline for a single {@link java.lang.reflect.Method}.
 *
 * <p>Not user API. Not an invokedynamic target. Valid for the {@code Method}
 * lifetime, not across {@code MetaMethod} wrappers.
 *
 * <p>Public because generated nestmates of a foreign host (other package,
 * other loader) must {@code implements} this interface. The containing
 * {@code internal} package is japicmp-excluded.
 *
 * @since 6.0.0
 */
@Internal
@FunctionalInterface
public interface DirectInvoker {

    /**
     * Invokes the bound method on {@code receiver} with {@code arguments}.
     *
     * <p>Arguments are assumed already coerced (see
     * {@code MetaMethod.doMethodInvoke}) except when a
     * {@code TransformMetaMethod} skipped coercion. Generated trampolines
     * require a non-null {@code arguments} array (use
     * {@code MetaClassHelper.EMPTY_ARRAY} for no args);
     * {@code CachedMethod.invoke} performs that substitution. Primitive
     * returns are boxed; the caller runs {@code normalizeBoxedReturn}.
     *
     * @param receiver  the receiver, ignored for static methods
     * @param arguments the positional arguments (non-null; empty if none)
     * @return the boxed result, or {@code null} for {@code void}
     * @throws Throwable the target's thrown exception, unwrapped
     */
    Object invoke(Object receiver, Object[] arguments) throws Throwable;
}
