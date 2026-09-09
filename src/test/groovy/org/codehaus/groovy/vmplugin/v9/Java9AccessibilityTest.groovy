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
package org.codehaus.groovy.vmplugin.v9

import org.codehaus.groovy.vmplugin.VMPlugin
import org.codehaus.groovy.vmplugin.VMPluginFactory
import org.junit.jupiter.api.Test

import java.lang.reflect.Modifier

import static org.junit.jupiter.api.Assertions.assertFalse
import static org.junit.jupiter.api.Assertions.assertTrue

/**
 * GROOVY-12383: the module-system accessibility rules apply on a JVM, and the
 * plugin initialises with them; where the module system is absent the Java 8
 * rules take over, which only a runtime such as Android's ART can exercise.
 */
final class Java9AccessibilityTest {

    private final VMPlugin plugin = VMPluginFactory.plugin

    @Test
    void moduleSystemIsDetectedOnAJvm() {
        assertTrue(Java9.modulesAvailable())
        assertTrue(plugin instanceof Java9)
    }

    @Test
    void publicMemberOfAnExportedJdkPackageIsAccessible() {
        def method = ArrayList.getMethod('size')
        assertTrue(plugin.checkCanSetAccessible(method, Java9AccessibilityTest))
        assertTrue(plugin.checkAccessible(Java9AccessibilityTest, ArrayList, method.modifiers, false))
    }

    @Test
    void concealedJdkMemberIsNotAccessibleWithoutIllegalAccess() {
        // jdk.internal.misc is in java.base and not exported to the unnamed module on any release since 9
        def concealed = Class.forName('jdk.internal.misc.Unsafe')
        assertFalse(plugin.checkAccessible(Java9AccessibilityTest, concealed, Modifier.PUBLIC | Modifier.STATIC, false))
    }

    @Test
    void classConstructorIsNeverMadeAccessible() {
        def constructors = Class.declaredConstructors
        assertTrue(constructors.length > 0)
        constructors.each { ctor ->
            assertFalse(plugin.checkCanSetAccessible(ctor, Java9AccessibilityTest), ctor.toString())
        }
    }
}
