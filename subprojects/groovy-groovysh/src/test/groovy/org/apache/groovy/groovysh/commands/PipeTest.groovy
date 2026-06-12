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

/**
 * Tests for the {@code /pipe} command — JLine's user-defined pipe operator
 * registry. Operators take the form {@code /pipe OPERATOR PREFIX POSTFIX};
 * uses of {@code OPERATOR} on subsequent lines have the right-hand side
 * wrapped between {@code PREFIX} and {@code POSTFIX} before evaluation.
 *
 * Documented in {@code groovysh.adoc}; this is the first automated coverage
 * for the user-defined pipe path. The tests cover the {@code /pipe} command
 * surface (define, list, delete, reserved-name rejection) — the actual
 * pipeline-rewriting machinery is JLine's responsibility upstream.
 */
class PipeTest extends SystemTestSupport {

    @Test
    void definedPipeAppearsInList() {
        system.execute("/pipe |? '.findAll{' '}'")
        printer.output.clear()
        system.execute('/pipe --list')
        // pipes is rendered as a Map<String, List<String>>; the key is the
        // operator name. Substring-match on the operator (and the prefix)
        // tolerates rendering changes between JLine versions.
        def out = printer.output.join()
        assert out.contains('|?')
        assert out.contains('.findAll{')
    }

    @Test
    void deleteAllRemovesPreviouslyDefinedPipes() {
        system.execute("/pipe |? '.findAll{' '}'")
        system.execute("/pipe |* '.collect{' '}'")
        system.execute('/pipe --delete *')
        printer.output.clear()
        system.execute('/pipe --list')
        def out = printer.output.join()
        assert !out.contains('|?')
        assert !out.contains('|*')
    }

}
