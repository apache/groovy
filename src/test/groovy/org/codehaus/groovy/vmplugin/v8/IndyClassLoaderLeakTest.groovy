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
package org.codehaus.groovy.vmplugin.v8

import org.junit.jupiter.api.Test
import java.lang.invoke.MethodType
import static org.junit.jupiter.api.Assertions.*

final class IndyClassLoaderLeakTest {

    @Test
    void testMruLeakAwareness() {
        MethodType type = MethodType.methodType(Object, Object)
        CacheableCallSite callSite = new CacheableCallSite(type, java.lang.invoke.MethodHandles.lookup())

        // 1. Same ClassLoader (Safe)
        def sameLoaderObj = new Object()
        Object key1 = IndyInterface.receiverCacheKey(sameLoaderObj)
        // Simulate a successful lookup that calls updateMRU
        updateMRU(callSite, key1, sameLoaderObj.class, this.class)
        assertNotNull(callSite.mruEntry, "MRU should be populated for same loader")

        // 2. Different (Child) ClassLoader (Unsafe)
        def gcl = new GroovyClassLoader()
        def childClass = gcl.parseClass("class Child { def foo() {} }")
        def childObj = childClass.newInstance()

        // Reset MRU
        callSite.mruEntry = null

        Object key2 = IndyInterface.receiverCacheKey(childObj)
        updateMRU(callSite, key2, childObj.class, this.class)
        assertNull(callSite.mruEntry, "MRU should NOT be populated for child loader to avoid leaks")
    }

    @Test
    void testIdentityKeyIsolation() {
        def gcl1 = new GroovyClassLoader()
        def gcl2 = new GroovyClassLoader()

        String script = "class Target {}"
        Class class1 = gcl1.parseClass(script)
        Class class2 = gcl2.parseClass(script)

        assertNotSame(class1, class2)
        assertEquals(class1.name, class2.name)

        Object key1 = IndyInterface.receiverCacheKey(class1)
        Object key2 = IndyInterface.receiverCacheKey(class2)

        assertNotSame(key1, key2, "Classes from different loaders must have distinct cache keys")
    }

    private static void updateMRU(CacheableCallSite callSite, Object key, Class targetClass, Class sender) {
        // We use a dummy wrapper for testing
        def wrapper = new MethodHandleWrapper(
            java.lang.invoke.MethodHandles.constant(Object, "test"),
            java.lang.invoke.MethodHandles.constant(Object, "test"),
            new org.codehaus.groovy.reflection.CachedMethod(targetClass.getDeclaredMethods().length > 0 ? targetClass.getDeclaredMethods()[0] : Object.class.getMethod("toString")),
            IndyInterface.switchPoint,
            true
        )
        callSite.updateMRU(key, wrapper, sender)
    }

}
