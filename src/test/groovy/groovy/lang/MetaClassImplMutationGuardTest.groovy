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
package groovy.lang

import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertThrows
import static org.junit.jupiter.api.Assertions.assertTrue

/**
 * GROOVY-12308: an initialized {@link MetaClassImpl} rejects in-place
 * additions. Such mutations bump no version and fire no invalidation, so
 * call-site caches would keep serving the pre-change state; the supported
 * routes are build-then-initialize and {@link ExpandoMetaClass}.
 */
final class MetaClassImplMutationGuardTest {

    static class Dummy {
        String field = 'field'
    }

    @Test
    void addMetaBeanPropertyRejectedAfterInitialization() {
        def mc = new MetaClassImpl(Dummy)
        mc.initialize()
        def mbp = new MetaBeanProperty('extra', String, null, null)
        def e = assertThrows(RuntimeException) { mc.addMetaBeanProperty(mbp) }
        assertTrue(e.message.contains('Already initialized'), e.message)
    }

    @Test
    void addNewInstanceMethodRejectedAfterInitialization() {
        def mc = new MetaClassImpl(Dummy)
        mc.initialize()
        def method = DummyMethods.getDeclaredMethod('instanceExtra', Dummy)
        def e = assertThrows(RuntimeException) { mc.addNewInstanceMethod(method) }
        assertTrue(e.message.contains('Already initialized'), e.message)
    }

    @Test
    void addNewStaticMethodRejectedAfterInitialization() {
        def mc = new MetaClassImpl(Dummy)
        mc.initialize()
        def method = DummyMethods.getDeclaredMethod('staticExtra', Dummy)
        def e = assertThrows(RuntimeException) { mc.addNewStaticMethod(method) }
        assertTrue(e.message.contains('Already initialized'), e.message)
    }

    static class DummyMethods {
        static String instanceExtra(Dummy self) { 'instanceExtra' }
        static String staticExtra(Dummy self) { 'staticExtra' }
    }

    @Test
    void buildThenInitializeStillWorks() {
        def helper = new MetaClassImpl(Dummy)
        helper.initialize()
        def getter = helper.getMetaMethod('getField', new Object[0])

        def mc = new MetaClassImpl(Dummy)
        mc.addMetaBeanProperty(new MetaBeanProperty('alias', String, getter, null))
        mc.initialize()

        def dummy = new Dummy()
        assertEquals('field', mc.getProperty(dummy, 'alias'))
    }

    @Test
    void expandoMetaClassMutationUnaffected() {
        try {
            Dummy.metaClass.getExtra = { -> 'v1' }
            assertEquals('v1', new Dummy().extra)
            // second registration mutates the same EMC in place
            Dummy.metaClass.getExtra = { -> 'v2' }
            assertEquals('v2', new Dummy().extra)
        } finally {
            GroovySystem.metaClassRegistry.removeMetaClass(Dummy)
        }
    }
}
