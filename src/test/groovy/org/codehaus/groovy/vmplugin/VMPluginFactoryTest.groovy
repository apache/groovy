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
package org.codehaus.groovy.vmplugin

import org.codehaus.groovy.control.CompilerConfiguration
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertNotNull
import static org.junit.jupiter.api.Assertions.assertSame

/**
 * GROOVY-12382: the feature version must be available on runtimes without
 * {@code Runtime.version()}, such as Android's ART, where the specification
 * version property is the only clue and may itself be meaningless ("0.9").
 */
final class VMPluginFactoryTest {

    @Test
    void featureVersionMatchesTheRuntimeOnAJvm() {
        assertEquals(Runtime.version().feature(), VMPluginFactory.featureVersion())
        assertEquals(Runtime.version().feature(), JavaFeatureVersion.current())
        assertEquals(17, JavaFeatureVersion.MINIMUM)
        assertEquals(Integer.toString(Runtime.version().feature()), CompilerConfiguration.DEFAULT_TARGET_BYTECODE)
    }

    @Test
    void parsesModernAndLegacySpecificationVersions() {
        assertEquals(17, JavaFeatureVersion.parse('17', 17))
        assertEquals(21, JavaFeatureVersion.parse('21', 17))
        assertEquals(25, JavaFeatureVersion.parse(' 25 ', 17))
        assertEquals(19, JavaFeatureVersion.parse('19.0.1', 17))
        assertEquals(8, JavaFeatureVersion.parse('1.8', 8))
    }

    @Test
    void neverReportsBelowTheLowestSupportedRelease() {
        assertEquals(17, JavaFeatureVersion.parse('1.8', 17))
        assertEquals(17, JavaFeatureVersion.parse('0.9', 17))   // Android
        assertEquals(17, JavaFeatureVersion.parse('', 17))
        assertEquals(17, JavaFeatureVersion.parse('banana', 17))
        assertEquals(17, JavaFeatureVersion.parse(null, 17))
    }

    @Test
    void pluginIsSelected() {
        VMPlugin plugin = VMPluginFactory.getPlugin()
        assertNotNull(plugin)
        assertSame(plugin, VMPluginFactory.getPlugin())
    }
}
