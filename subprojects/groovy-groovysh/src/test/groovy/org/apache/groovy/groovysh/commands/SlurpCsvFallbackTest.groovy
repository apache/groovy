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

import groovy.grape.Grape
import groovy.junit6.plugin.ForkedJvm
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfSystemProperty
import org.junit.jupiter.api.io.TempDir

import java.nio.file.Files
import java.nio.file.Path

import static groovy.test.GroovyAssert.shouldFail

/**
 * Variant tests for the {@code /slurp} CSV path that exercise classpath
 * configurations the default test classpath can't represent. Each test runs
 * in a freshly forked JVM with {@code groovy-csv} filtered off the
 * classpath, so the engine genuinely cannot resolve {@code groovy.csv.CsvSlurper}.
 *
 * The "happy path" (groovy-csv available, preferred over commons-csv) is
 * covered in-process by {@link SlurpTest#slurpCsvProducesListOfMaps}.
 */
class SlurpCsvFallbackTest extends SystemTestSupport {

    @TempDir
    Path tmp

    @Test
    @ForkedJvm(excludeFromClasspath = ['groovy-csv'])
    void slurpCsvErrorsWhenNoCsvLibraryAvailable() {
        Path file = Files.writeString(tmp.resolve('whiskey.csv'), "name,region\nLagavulin,Islay\n")
        // parseCsv throws IllegalArgumentException when neither
        // groovy.csv.CsvSlurper nor org.apache.commons.csv.CSVFormat is on
        // the classpath; slurpcmd's outer catch re-throws so the user sees
        // a clear error message rather than a silently null result.
        def thrown = shouldFail(IllegalArgumentException) {
            system.execute("data = /slurp ${forwardSlashes(file)}")
        }
        assert thrown.message.contains('CSV format requires')
        assert thrown.message.contains('groovy.csv.CsvSlurper')
        assert thrown.message.contains('org.apache.commons.csv.CSVFormat')
    }

    @Test
    @ForkedJvm(excludeFromClasspath = ['groovy-csv'])
    @EnabledIfSystemProperty(named = 'junit.network', matches = 'true')
    void slurpCsvUsesCommonsCsvFallback() {
        // groovy-csv is filtered off the classpath; pull commons-csv via
        // Grape so parseCsv's second branch is the one that fires.
        Grape.grab(group: 'org.apache.commons', module: 'commons-csv', version: '1.14.1',
                classLoader: engine.classLoader, transitive: false)
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
