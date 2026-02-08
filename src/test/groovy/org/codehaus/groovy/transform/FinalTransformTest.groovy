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
package org.codehaus.groovy.transform

import org.junit.Test
import org.codehaus.groovy.control.MultipleCompilationErrorsException

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

/**
 * Tests for the {@code @Final} transformation
 */
class FinalTransformTest {

    @Test
    void testUsageInAnnoationCollectorForClass() {
        assertScript """
            import groovy.transform.*
            import static java.lang.reflect.Modifier.isFinal

            @AnnotationCollector
            @Canonical
            @Final
            @interface MyCanonical {}

            @MyCanonical class Foo {}

            assert isFinal(Foo.modifiers)
        """
    }

    @Test
    void testUsageDirectDisabled() {
        assertScript """
            import groovy.transform.*
            import static java.lang.reflect.Modifier.isFinal

            @Final(enabled=false)
            class Foo {}

            assert !isFinal(Foo.modifiers)
        """
    }

    @Test
    void testUsageInAnnoationCollectorForClassDisabled() {
        assertScript """
            import groovy.transform.*
            import static java.lang.reflect.Modifier.isFinal

            @AnnotationCollector(mode=AnnotationCollectorMode.PREFER_EXPLICIT_MERGED)
            @Canonical
            @Final
            @interface MyCanonical {}

            @MyCanonical
            @Final(enabled=false)
            class Foo {}

            assert !isFinal(Foo.modifiers)
        """
    }

    @Test
    void testUsageInAnnoationCollectorForMethod() {
        assertScript """
            import groovy.transform.*
            import static groovy.test.GroovyAssert.shouldFail
            import static java.lang.reflect.Modifier.isFinal

            @AnnotationCollector
            @NullCheck
            @Final
            @interface MyNullCheck {}

            class Foo {
                @MyNullCheck upper(String s) { s.toUpperCase() }
            }

            assert isFinal(Foo.getMethod('upper', String).modifiers)
            def foo = new Foo()
            assert foo.upper('foo') == 'FOO'
            shouldFail(IllegalArgumentException) {
                foo.upper(null)
            }
        """
    }

    @Test
    void testDirectClassUsage() {
        shouldFail MultipleCompilationErrorsException, '''
            @Final class Foo {}
            class Bar extends Foo {}
        '''
    }
}
