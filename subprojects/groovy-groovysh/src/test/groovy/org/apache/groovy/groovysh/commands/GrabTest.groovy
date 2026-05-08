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
package org.apache.groovy.groovysh.commands

import groovy.junit6.plugin.ForkedJvm
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfSystemProperty

/**
 * Tests for the {@code /grab} command — Maven-coordinate dependency
 * resolution via Grape. The actual artifact-fetching test is forked and
 * network-gated; the no-arg test runs always to lock in the documented
 * "no args is a no-op" behaviour that {@code GroovyCommands.grab} relies
 * on for {@code grab(input)} when no xargs are supplied.
 */
class GrabTest extends SystemTestSupport {

    @Test
    void grabWithNoArgsIsNoOp() {
        // grab() returns null when input.xargs() is empty; this should
        // succeed silently rather than throw.
        system.execute('/grab')
    }

    @Test
    @ForkedJvm
    @EnabledIfSystemProperty(named = 'junit.network', matches = 'true')
    void grabFetchesArtifactAndMakesItLoadable() {
        // commons-lang3 is small, stable, and uses well-known coordinates.
        // After the grab, the artifact's classes should resolve through
        // the engine's classloader.
        system.execute('/grab org.apache.commons:commons-lang3:3.14.0')
        def cls = engine.execute("Class.forName('org.apache.commons.lang3.StringUtils')")
        assert cls != null
        assert cls.name == 'org.apache.commons.lang3.StringUtils'
    }
}
