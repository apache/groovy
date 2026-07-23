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
package org.codehaus.groovy.runtime.callsite

import groovy.lang.MetaClassImpl
import org.codehaus.groovy.reflection.CachedMethod
import org.codehaus.groovy.reflection.ReflectionCache
import org.junit.jupiter.api.Test

/**
 * GROOVY-11158: CachedMethodCallSites (logic moved out of CachedMethod).
 */
final class CachedMethodCallSitesTest {

    @Test
    void testCreatePojoMetaMethodSite() {
        def cachedClass = ReflectionCache.getCachedClass(String)
        CachedMethod method = cachedClass.methods.find { it.name == 'length' && it.paramsCount == 0 }
        assert method != null
        def mc = (MetaClassImpl) groovy.lang.GroovySystem.metaClassRegistry.getMetaClass(String)
        def site0 = new CallSiteArray(CachedMethodCallSitesTest, ['length'] as String[]).array[0]
        def site = CachedMethodCallSites.createPojoMetaMethodSite(method, site0, mc, [] as Class[])
        assert site != null
        assert site.call('abcd', CallSiteArray.NOPARAM) == 4
    }

    @Test
    void testCreateStaticMetaMethodSite() {
        def cachedClass = ReflectionCache.getCachedClass(String)
        CachedMethod method = cachedClass.methods.find {
            it.name == 'valueOf' && it.paramsCount == 1 && it.nativeParameterTypes[0] == Object
        }
        assert method != null
        def mc = (MetaClassImpl) groovy.lang.GroovySystem.metaClassRegistry.getMetaClass(String)
        def site0 = new CallSiteArray(CachedMethodCallSitesTest, ['valueOf'] as String[]).array[0]
        def site = CachedMethodCallSites.createStaticMetaMethodSite(method, site0, mc, [Object] as Class[])
        assert site != null
        assert site.callStatic(String, 99) == '99'
    }

    @Test
    void testIsCompilablePublicMethod() {
        def cachedClass = ReflectionCache.getCachedClass(String)
        CachedMethod method = cachedClass.methods.find { it.name == 'length' && it.paramsCount == 0 }
        assert CallSiteGenerator.isCompilable(method)
    }
}
