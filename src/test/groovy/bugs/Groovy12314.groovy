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

import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail
import static org.junit.jupiter.api.Assumptions.assumeFalse

/**
 * GROOVY-12314: property/attribute access to a field whose reflective access
 * cannot be established (e.g. a non-public field of a strongly encapsulated
 * JDK class, absent {@code --add-opens}) must degrade to the normal missing
 * member handling instead of GroovyBugError or a raw IllegalAccessException.
 * The JDK field tests assume the JVM does not open {@code java.base/java.util}
 * and skip themselves when it does.
 */
final class Groovy12314 {

    private static void assumeJavaUtilIsEncapsulated() {
        assumeFalse(ArrayList.class.getModule().isOpen('java.util', Groovy12314.class.getModule()),
            'the JVM opens java.base/java.util, so JDK fields are reflectively reachable')
    }

    @Test
    void testProtectedJdkFieldPropertyRead() {
        assumeJavaUtilIsEncapsulated()
        def list = [1, 2]
        // was: GroovyBugError "BUG! UNCAUGHT EXCEPTION: member is protected"
        shouldFail(MissingPropertyException) {
            list.modCount
        }
    }

    @Test
    void testPackagePrivateJdkFieldPropertyRead() {
        assumeJavaUtilIsEncapsulated()
        def list = [1, 2]
        shouldFail(MissingPropertyException) {
            list.elementData
        }
    }

    @Test
    void testProtectedJdkFieldAttributeRead() {
        assumeJavaUtilIsEncapsulated()
        def list = [1, 2]
        // was: raw IllegalAccessException escaping from CachedField#getProperty
        shouldFail(MissingFieldException) {
            list.@modCount
        }
    }

    @Test
    void testProtectedJdkFieldPropertyWrite() {
        assumeJavaUtilIsEncapsulated()
        def list = [1, 2]
        // the field exists but cannot be written via property syntax
        shouldFail(ReadOnlyPropertyException) {
            list.modCount = 7
        }
    }

    @Test
    void testProtectedJdkFieldAttributeWrite() {
        assumeJavaUtilIsEncapsulated()
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

    @Test
    void testPermissiveWriteToOpenClassesUnaffected() {
        // a non-public field is never given a direct write handle (only public
        // fields are sender-independent), so the write must actually succeed
        // through the sender-aware adapter path
        assertScript '''
            class Holder {
                private int secret = 42
                protected int shielded = 43
            }
            def h = new Holder()
            h.secret = 52
            assert h.@secret == 52
            h.shielded = 53
            assert h.@shielded == 53
        '''
    }

    @Test
    void testPublicFieldOfPackagePrivateClass() {
        // the call-site lookup cannot see the declaring class, so unreflecting the
        // public field is refused at first; forcing accessibility and retrying
        // (GROOVY-9144, GROOVY-9596) must still yield working read/write handles
        def loader = new GroovyClassLoader()
        loader.parseClass '''
            package p
            @groovy.transform.PackageScope
            class Holder12314 {
                public int value = 42
            }
            class Factory12314 {
                static Object make() { new Holder12314() }
            }
        '''
        new GroovyShell(loader).evaluate '''
            def obj = p.Factory12314.make()
            assert obj.value == 42
            obj.value = 52
            assert obj.value == 52
        '''
    }

    @Test
    void testForbiddenAccessErrorReportsTypeArguments() {
        // the classgen safety-net error must name the receiver's type arguments
        // (Optional<String>), not the declaration's type parameters (Optional<T>)
        def err = shouldFail '''
            @groovy.transform.CompileStatic(extensions='groovy/transform/stc/ParameterizedReceiverTestExtension.groovy')
            class C {
                def foo() {
                    bar.baz
                }
            }
        '''
        assert err.message =~ /Access to java\.util\.Optional<java\.lang\.String>#baz is forbidden/
    }
}
