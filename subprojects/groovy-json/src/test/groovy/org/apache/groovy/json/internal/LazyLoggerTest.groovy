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
package org.apache.groovy.json.internal

import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertNotNull
import static org.junit.jupiter.api.Assertions.assertTrue
import static org.junit.jupiter.api.Assumptions.assumeTrue

/**
 * GROOVY-12386: the internal classes create their {@code System.Logger} lazily,
 * in a nested holder, so that loading them does not require the Java 9 logging
 * API, which Android's runtime lacks.
 */
class LazyLoggerTest {

    @Test
    void loggersLiveInNestedHoldersNotInTheClassesThemselves() {
        [Sys, Exceptions].each { owner ->
            assertTrue(owner.declaredFields.every { it.type != System.Logger }, owner.name)
            def holder = owner.declaredClasses.find { it.simpleName == 'Log' }
            assertNotNull(holder, owner.name)
            def field = holder.getDeclaredField('LOGGER')
            assumeTrue(field.trySetAccessible(), 'holder field accessible from the test')
            assertNotNull(field.get(null), 'the logger resolves on a JVM')
        }
    }

    @Test
    void wrappedExceptionStillPrintsItsStackTrace() {
        def err = new ByteArrayOutputStream()
        def saved = System.err
        System.err = new PrintStream(err, true)
        try {
            new Exceptions.JsonInternalException('boom').printStackTrace()
        } finally {
            System.err = saved
        }
        // without a cause the exception delegates to Throwable.printStackTrace after logging
        assertTrue(err.toString().contains('boom'), err.toString())
    }
}
