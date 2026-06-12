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

import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.shouldFail

/**
 * Tests for the {@code /doc} command. The command opens documentation in
 * a browser via {@link java.awt.Desktop} when configured, but the early
 * exit and error paths (no args, missing config, headless JVM) are
 * deterministic and cheap to cover.
 */
class DocTest extends SystemTestSupport {

    @Test
    void docWithNoArgsIsNoOp() {
        // doc() returns early when xargs is empty; no exception, no output.
        system.execute('/doc')
    }

    @Test
    void docForUnknownTargetSurfacesAClearError() {
        // Without a CONSOLE_OPTIONS map, /doc throws IllegalStateException.
        // The exact message varies by environment:
        //   - headless JVM: "Desktop is not supported!"
        //   - desktop dev box: "No documents configuration!"
        // Don't pin to either; just lock in that the failure is targeted
        // and not, e.g., an NPE walking through xargs.
        def thrown = shouldFail(IllegalStateException) {
            system.execute('/doc List')
        }
        assert thrown.message != null
        assert !thrown.message.empty
    }
}
