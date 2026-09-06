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

import org.apache.groovy.runtime.indy.AotDispatch
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.ResourceLock
import org.junit.jupiter.api.parallel.Resources

import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType

import static org.junit.jupiter.api.Assertions.assertFalse
import static org.junit.jupiter.api.Assertions.assertTrue
import static org.junit.jupiter.api.Assumptions.assumeTrue

/**
 * GROOVY-12354: with {@code groovy.indy.cold.reflection} unset, the reflective
 * cold tier is used only by AOT-linked sites. On a JVM it would leave
 * reflection and Groovy frames between caller and target while a site is
 * cold, misreporting the caller to loggers and stack walkers. The explicit
 * on/off settings are covered by the fresh-JVM
 * {@code ColdReflectionParityTest} in the performance subproject, since the
 * property is read once at {@code IndyInterface} class init.
 */
@ResourceLock(Resources.SYSTEM_PROPERTIES)
final class ColdReflectionDefaultTest {

    private static CacheableCallSite siteLinkedWithAot(boolean aot) {
        String previous = System.getProperty(AotDispatch.FORCE_PROPERTY)
        if (aot) System.setProperty(AotDispatch.FORCE_PROPERTY, 'true')
        else System.clearProperty(AotDispatch.FORCE_PROPERTY)
        try {
            new CacheableCallSite(MethodType.methodType(Object, Object[]), MethodHandles.lookup())
        } finally {
            if (previous != null) {
                System.setProperty(AotDispatch.FORCE_PROPERTY, previous)
            } else {
                System.clearProperty(AotDispatch.FORCE_PROPERTY)
            }
        }
    }

    @Test
    void 'with the property unset only AOT-linked sites use the reflective cold tier'() {
        assumeTrue(System.getProperty('groovy.indy.cold.reflection') == null,
                'groovy.indy.cold.reflection is set for this JVM; the default cannot be observed')
        assertFalse(IndyInterface.coldReflectionEnabled(siteLinkedWithAot(false)), 'JVM site must not use the tier by default')
        assertTrue(IndyInterface.coldReflectionEnabled(siteLinkedWithAot(true)), 'AOT-linked site must use the tier by default')
    }
}
