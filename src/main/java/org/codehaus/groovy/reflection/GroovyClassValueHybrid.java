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

import org.codehaus.groovy.reflection.v7.GroovyClassValueJava7;

/**
 * GROOVY-12281 investigation prototype: routes by key origin. A {@code ClassValue}
 * association lives as long as its key class, so an association on an immortal
 * platform class pins the value's loader forever; platform-loader keys therefore
 * use the weak-key map (owned by Groovy's own loader), while every other key
 * keeps the {@code ClassValue} per-class fast path. Values are strong in both
 * stores — lifetimes are unchanged and nothing is ever recomputed.
 *
 * @param <T> the value type
 */
class GroovyClassValueHybrid<T> implements GroovyClassValue<T> {

    private final GroovyClassValueJava7<T> fastPath;
    private final GroovyClassValueMapBased<T> platformStore;

    GroovyClassValueHybrid(final ComputeValue<T> computeValue) {
        this.fastPath = new GroovyClassValueJava7<>(computeValue);
        this.platformStore = new GroovyClassValueMapBased<>(computeValue);
    }

    private static boolean isPlatformKey(final Class<?> type) {
        ClassLoader loader = type.getClassLoader();
        return loader == null || loader == ClassLoader.getPlatformClassLoader();
    }

    @Override
    public T get(final Class<?> type) {
        return isPlatformKey(type) ? platformStore.get(type) : fastPath.get(type);
    }

    @Override
    public void remove(final Class<?> type) {
        if (isPlatformKey(type)) {
            platformStore.remove(type);
        } else {
            fastPath.remove(type);
        }
    }
}
