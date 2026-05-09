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
import org.junit.jupiter.api.io.TempDir

import java.nio.file.Files
import java.nio.file.Path

/**
 * Tests for the {@code /slurp} command, registered by JLine's
 * {@code ConsoleEngineImpl}. Confirms format detection by file extension
 * for the formats most groovysh users reach for, and verifies the result
 * is bound to a Groovy variable when assigned with {@code = /slurp ...}.
 *
 * Uses {@link TempDir} for fixture lifecycle so the support class doesn't
 * grow a dedicated tempDir field for every file-touching test.
 */
class SlurpTest extends SystemTestSupport {

    @TempDir
    Path tmp

    @Test
    void slurpJsonProducesMap() {
        Path file = Files.writeString(tmp.resolve('answer.json'), '{"answer":42,"name":"groovysh"}')
        system.execute("data = /slurp ${forwardSlashes(file)}")
        def value = console.getVariable('data')
        assert value != null
        assert value.answer == 42
        assert value.name == 'groovysh'
    }

    @Test
    void slurpPropertiesProducesMap() {
        Path file = Files.writeString(tmp.resolve('app.properties'), "name=groovysh\nversion=4.x\n")
        system.execute("config = /slurp ${forwardSlashes(file)}")
        def value = console.getVariable('config')
        assert value != null
        assert value.name == 'groovysh'
        assert value.version == '4.x'
    }

    @Test
    void slurpMarkdownProducesIterableOfElements() {
        // Requires groovy.markdown.MarkdownSlurper; supplied via the
        // testImplementation projects.groovyMarkdown dependency.
        Path file = Files.writeString(tmp.resolve('notes.md'),
                "# Title\n\nA paragraph.\n\n- item one\n- item two\n")
        system.execute("doc = /slurp ${forwardSlashes(file)}")
        def doc = console.getVariable('doc')
        assert doc != null
        // MarkdownDocument is Iterable<Map<String,Object>>; toList lets us
        // assert without pinning to the exact element-type names (those
        // belong to groovy-markdown's API and may evolve).
        def elements = doc.toList()
        assert elements.size() >= 2
        def joined = elements.collect { it.values().toString() }.join(' ')
        assert joined.contains('Title')
        assert joined.contains('A paragraph.')
        assert joined.contains('item one')
    }

    @Test
    void slurpCsvProducesListOfMaps() {
        // Requires groovy.csv.CsvSlurper on the classpath; supplied here via
        // the testImplementation projects.groovyCsv dependency.
        Path file = Files.writeString(tmp.resolve('whiskey.csv'),
                "name,region\nLagavulin,Islay\nMacallan,Speyside\n")
        system.execute("rows = /slurp ${forwardSlashes(file)}")
        def rows = console.getVariable('rows')
        assert rows instanceof List
        assert rows.size() == 2
        assert rows[0].name == 'Lagavulin' && rows[0].region == 'Islay'
        assert rows[1].name == 'Macallan' && rows[1].region == 'Speyside'
    }
}
