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
package org.codehaus.groovy.reflection

import groovy.lang.GroovySystem
import groovy.lang.MetaClassImpl
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertNotNull
import static org.junit.jupiter.api.Assertions.assertNull
import static org.junit.jupiter.api.Assertions.assertSame
import static org.junit.jupiter.api.Assertions.assertTrue

/**
 * ClassInfo MetaClass lookup / creation paths used by GROOVY-12191 and
 * Sonar javabugs:S2259 (null MetaClassRegistry on bootstrap).
 * <p>
 * The true bootstrap window (registry not yet published) cannot be re-entered
 * once {@link GroovySystem} has finished class initialization; these tests
 * lock the post-init contracts that the null-registry guards preserve:
 * {@link ClassInfo#getMetaClassForClass()} never forces creation, and
 * {@link ClassInfo#getMetaClass()} creates under lock when the registry is live.
 */
final class ClassInfoMetaClassBootstrapTest {

    @Test
    void getMetaClassForClass_doesNotCreate_whenAbsent() {
        def type = FreshMetaClassHost
        GroovySystem.metaClassRegistry.removeMetaClass(type)
        def info = ClassInfo.getClassInfo(type)
        assertNull(info.metaClassForClass)
        // Second look still does not install a MetaClass.
        assertNull(info.metaClassForClass)
        GroovySystem.metaClassRegistry.removeMetaClass(type)
    }

    @Test
    void getMetaClass_createsWhenAbsent() {
        def type = CreateMetaClassHost
        GroovySystem.metaClassRegistry.removeMetaClass(type)
        def info = ClassInfo.getClassInfo(type)
        assertNull(info.metaClassForClass)
        def mc = info.metaClass
        assertNotNull(mc)
        assertTrue(mc instanceof MetaClassImpl)
        assertSame(mc, info.metaClassForClass)
        GroovySystem.metaClassRegistry.removeMetaClass(type)
    }

    @Test
    void getMetaClass_returnsStrongWhenPresent() {
        def type = StrongMetaClassHost
        def info = ClassInfo.getClassInfo(type)
        def mc = new MetaClassImpl(type)
        mc.initialize()
        info.strongMetaClass = mc
        try {
            assertSame(mc, info.metaClassForClass)
            assertSame(mc, info.metaClass)
        } finally {
            info.strongMetaClass = null
            GroovySystem.metaClassRegistry.removeMetaClass(type)
        }
    }

    @Test
    void getMetaClassUnderLock_path_createsViaRegistryWhenLive() {
        // Exercises getMetaClass → getMetaClassUnderLock with a live registry
        // (the path Sonar flagged for missing null-check). Must not NPE.
        def type = UnderLockHost
        GroovySystem.metaClassRegistry.removeMetaClass(type)
        assertNotNull(GroovySystem.metaClassRegistry,
                'registry is live after GroovySystem init')
        def mc = ClassInfo.getClassInfo(type).metaClass
        assertNotNull(mc)
        assertSame(type, mc.theClass)
        GroovySystem.metaClassRegistry.removeMetaClass(type)
    }

    private static final class FreshMetaClassHost {}
    private static final class CreateMetaClassHost {}
    private static final class StrongMetaClassHost {}
    private static final class UnderLockHost {}
}
