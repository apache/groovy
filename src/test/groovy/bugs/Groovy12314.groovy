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
package bugs

import groovy.test.GroovyAssert
import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

/**
 * GROOVY-12314: property/attribute access to a field whose reflective access
 * cannot be established (e.g. a non-public field of a strongly encapsulated
 * JDK class, absent {@code --add-opens}) must degrade to the normal missing
 * member handling instead of GroovyBugError or a raw IllegalAccessException.
 * These tests assume the JVM does not open {@code java.base/java.util}.
 */
final class Groovy12314 {

    @Test
    void testProtectedJdkFieldPropertyRead() {
        def list = [1, 2]
        // was: GroovyBugError "BUG! UNCAUGHT EXCEPTION: member is protected"
        shouldFail(MissingPropertyException) {
            list.modCount
        }
    }

    @Test
    void testPackagePrivateJdkFieldPropertyRead() {
        def list = [1, 2]
        shouldFail(MissingPropertyException) {
            list.elementData
        }
    }

    @Test
    void testProtectedJdkFieldAttributeRead() {
        def list = [1, 2]
        // was: raw IllegalAccessException escaping from CachedField#getProperty
        shouldFail(MissingFieldException) {
            list.@modCount
        }
    }

    @Test
    void testProtectedJdkFieldPropertyWrite() {
        def list = [1, 2]
        // the field exists but cannot be written via property syntax
        shouldFail(ReadOnlyPropertyException) {
            list.modCount = 7
        }
    }

    @Test
    void testProtectedJdkFieldAttributeWrite() {
        def list = [1, 2]
        shouldFail(MissingFieldException) {
            list.@modCount = 7
        }
    }

    @Test
    void testPermissiveAccessToOpenClassesUnaffected() {
        // classes on the class path live in an unnamed (open) module: dynamic
        // Groovy's permissive field access there must keep working
        assertScript '''
            class Holder {
                private int secret = 42
                protected int shielded = 43
            }
            def h = new Holder()
            assert h.secret == 42
            assert h.@secret == 42
            assert h.shielded == 43
            assert h.@shielded == 43
        '''
    }
}
