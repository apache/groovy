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
package org.apache.groovy.groovysh.jline

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

import java.nio.file.Files
import java.nio.file.Path

/**
 * Unit tests for {@link GroovyPrinter}'s case-insensitive style resolution.
 * Pure logic — no system/printer fixture needed.
 */
class GroovyPrinterTest {

    // names as a user might actually have them (mixed case, incl. from a
    // pre-existing ~/.groovy copy): JSON, Groovy, sql, GRON
    private static final GroovyPrinter.Names NAMES =
        GroovyPrinter.buildNames(['JSON', 'Groovy', 'sql', 'GRON'])

    @Test
    void exactMatchIsLeftUnchanged() {
        // user-config exact name must keep working (the regression Copilot flagged)
        assert GroovyPrinter.resolveStyle('Groovy', NAMES) == 'Groovy'
        assert GroovyPrinter.resolveStyle('JSON', NAMES) == 'JSON'
        assert GroovyPrinter.resolveStyle('sql', NAMES) == 'sql'
    }

    @Test
    void caseInsensitiveHitResolvesToActualName() {
        assert GroovyPrinter.resolveStyle('groovy', NAMES) == 'Groovy'
        assert GroovyPrinter.resolveStyle('GROOVY', NAMES) == 'Groovy'
        assert GroovyPrinter.resolveStyle('gRoOvY', NAMES) == 'Groovy'
        assert GroovyPrinter.resolveStyle('json', NAMES) == 'JSON'
        assert GroovyPrinter.resolveStyle('Json', NAMES) == 'JSON'
        assert GroovyPrinter.resolveStyle('SQL', NAMES) == 'sql'
        assert GroovyPrinter.resolveStyle('gron', NAMES) == 'GRON'   // DEFAULT_NANORC_VALUE
    }

    @Test
    void unknownOrEmptyNameIsPassedThrough() {
        assert GroovyPrinter.resolveStyle('does-not-exist', NAMES) == 'does-not-exist'
        assert GroovyPrinter.resolveStyle('', NAMES) == ''
        assert GroovyPrinter.resolveStyle(null, NAMES) == null
    }

    @Test
    void caseOnlyCollisionPreservesExactAndDoesNotRewriteAmbiguous() {
        // both "JSON" and "json" configured (user misconfig / overlay)
        def names = GroovyPrinter.buildNames(['JSON', 'json', 'Groovy'])
        assert GroovyPrinter.resolveStyle('json', names) == 'json'  // exact wins, not rewritten to JSON
        assert GroovyPrinter.resolveStyle('JSON', names) == 'JSON'  // exact wins
        assert GroovyPrinter.resolveStyle('Json', names) == 'Json'  // ambiguous -> passthrough (JLine decides)
        assert GroovyPrinter.resolveStyle('groovy', names) == 'Groovy' // unaffected sibling still resolves
    }

    @Test
    void collectSyntaxNamesSpansJnanorcAndIncludes(@TempDir Path dir) {
        Files.writeString(dir.resolve('jnanorc'),
            'theme dark.nanorctheme\nsyntax "DIRECT" "\\.x$"\ninclude *.nanorc\n')
        Files.writeString(dir.resolve('groovy.nanorc'), 'syntax "Groovy" "\\.groovy$"\nKEYWORD: "def"\n')
        Files.writeString(dir.resolve('json.nanorc'), 'syntax "JSON" "\\.json$"\n')

        Map<String, Set<String>> raw = [:]
        GroovyPrinter.collectSyntaxNames(dir.resolve('jnanorc'), raw)
        def names = GroovyPrinter.buildNames(raw)

        assert GroovyPrinter.resolveStyle('direct', names) == 'DIRECT' // declared directly in jnanorc
        assert GroovyPrinter.resolveStyle('GROOVY', names) == 'Groovy' // via include, original case
        assert GroovyPrinter.resolveStyle('json', names) == 'JSON'
    }

    @Test
    void oneBadNanorcDoesNotBreakTheOthers(@TempDir Path dir) {
        Files.writeString(dir.resolve('jnanorc'), 'include *.nanorc\n')
        Files.writeString(dir.resolve('good.nanorc'), 'syntax "Groovy" "\\.groovy$"\n')
        Files.writeString(dir.resolve('other.nanorc'), 'syntax "JSON" "\\.json$"\n')
        // a directory entry matching the include glob: readString throws,
        // exercising the per-file failsafe without aborting discovery
        Files.createDirectory(dir.resolve('weird.nanorc'))

        Map<String, Set<String>> raw = [:]
        GroovyPrinter.collectSyntaxNames(dir.resolve('jnanorc'), raw)
        def names = GroovyPrinter.buildNames(raw)

        assert GroovyPrinter.resolveStyle('groovy', names) == 'Groovy'
        assert GroovyPrinter.resolveStyle('json', names) == 'JSON'
        assert names.actual as Set == ['Groovy', 'JSON'] as Set // bad entry skipped, not fatal
    }
}
