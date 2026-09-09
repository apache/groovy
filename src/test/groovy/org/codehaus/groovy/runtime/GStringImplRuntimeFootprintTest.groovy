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
package org.codehaus.groovy.runtime

import groovy.transform.KnownImmutable
import org.apache.groovy.ast.tools.ImmutablePropertyUtils
import org.codehaus.groovy.ast.ClassHelper
import org.apache.groovy.internal.util.ImmutableTypes
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertFalse
import static org.junit.jupiter.api.Assertions.assertTrue

/**
 * GROOVY-12390: building a GString must not initialise the compiler's AST
 * model; the immutable-type check it needs lives in a runtime utility.
 */
final class GStringImplRuntimeFootprintTest {

    private static final class RecordingLoader extends URLClassLoader {
        final List<String> loaded = []
        final Map<String, String> firstUse = [:]
        RecordingLoader(URL[] urls) { super(urls, ClassLoader.platformClassLoader) }
        @Override
        protected Class<?> loadClass(String name, boolean resolve) {
            synchronized (getClassLoadingLock(name)) {
                if (name.startsWith('org.codehaus.groovy.') || name.startsWith('org.apache.groovy.') || name.startsWith('groovy.')) loaded << name
                if (name in WATCHED && !firstUse.containsKey(name)) {
                    firstUse[name] = new Throwable().stackTrace.findAll { !it.className.startsWith('java.') && !it.className.contains('RecordingLoader') }.take(12).join('\n')
                }
                super.loadClass(name, resolve)
            }
        }
    }

    // the compiler-side utility GStringImpl used to consult, and the class-model root it initialises
    private static final List<String> WATCHED = [ImmutablePropertyUtils.name, ClassHelper.name]

    /**
     * Note: rendering the GString creates metaclasses, and the metaclass machinery loads the VM
     * plugin, whose interface names AST types; those types get loaded but not initialised. The
     * claim here is narrower: the compiler-side utility and ClassHelper stay out of it entirely.
     */
    @Test
    void aGStringWithANonTrivialValueDoesNotReachTheCompilerUtilities() {
        URL classes = GStringImpl.protectionDomain.codeSource.location
        new RecordingLoader([classes] as URL[]).withCloseable { loader ->
            Class<?> gstring = Class.forName(GStringImpl.name, true, loader)
            def values = [new Object(), new Date(), 42, 'text'] as Object[]
            def strings = ['a', 'b', 'c', 'd', 'e'] as String[]
            def instance = gstring.getConstructor(Object[], String[]).newInstance(values, strings)
            assertTrue(gstring.getMethod('toString').invoke(instance).toString().startsWith('a'))
            WATCHED.each { name ->
                assertFalse(loader.loaded.contains(name), name + ' was loaded via:\n' + loader.firstUse[name])
            }
            assertTrue(loader.loaded.contains(ImmutableTypes.name))
        }
    }

    @KnownImmutable
    static class Marked {}

    @Test
    void theRuntimeUtilityAndTheAstUtilityAgree() {
        [String, Integer, Marked, Object, Date, GStringImplRuntimeFootprintTest].each { Class type ->
            assertEquals(ImmutablePropertyUtils.builtinOrMarkedImmutableClass(type), ImmutableTypes.builtinOrMarkedImmutableClass(type), type.name)
        }
        assertTrue(ImmutableTypes.builtinOrMarkedImmutableClass(Marked))
        assertTrue(ImmutableTypes.isBuiltinImmutable('java.time.Instant'))
        assertFalse(ImmutableTypes.builtinOrMarkedImmutableClass(Date))
        assertTrue(ImmutableTypes.isImmutableMarker('groovy.transform.Immutable'))
        assertFalse(ImmutableTypes.isImmutableMarker('java.lang.Deprecated'))
    }
}
