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

import org.apache.groovy.util.concurrent.ManagedIdentityConcurrentMap;

/**
 * Map-based {@link GroovyClassValue} used when {@code java.lang.ClassValue}
 * must be avoided: due to JDK-8136353, a ClassValue association on an
 * immortal class (for example a bootstrap class such as {@code String})
 * retains its value — and with it the value's class loader — forever, which
 * leaks every Groovy copy deployed and undeployed by a container
 * (GROOVY-12142). Class keys are held weakly with identity semantics, so
 * associations die with their class and never pin a loader.
 * <p>
 * The trade-off is a hash lookup per access instead of ClassValue's
 * per-{@code Class} fast path; selection is therefore opt-in via
 * {@code -Dgroovy.use.classvalue=false}.
 *
 * @param <T> the value type
 */
class GroovyClassValueMapBased<T> implements GroovyClassValue<T> {

    private final ManagedIdentityConcurrentMap<Class<?>, T> map = new ManagedIdentityConcurrentMap<>();
    private final ComputeValue<T> computeValue;

    GroovyClassValueMapBased(final ComputeValue<T> computeValue) {
        this.computeValue = computeValue;
    }

    @Override
    public T get(final Class<?> type) {
        return map.applyIfAbsent(type, computeValue::computeValue);
    }

    @Override
    public void remove(final Class<?> type) {
        map.remove(type);
    }
}
