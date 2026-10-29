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
package bugs

import org.codehaus.groovy.control.CompilationFailedException
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.Phases
import org.codehaus.groovy.control.messages.SyntaxErrorMessage
import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.shouldFail

final class Groovy12532 {

    private static final int SOURCES = 64

    @Test
    void testParallelParseReportsEveryFailedSourceBeforeThrowing() {
        20.times { run ->
            def unit = compileBrokenSources(run, false)
            assertEverySourceReportedOnce(unit, run)
        }
    }

    @Test
    void testParallelParseWithErrorRecoveryKeepsEveryError() {
        20.times { run ->
            def unit = compileBrokenSources(run, true)
            assertEverySourceReportedOnce(unit, run)
        }
    }

    private static CompilationUnit compileBrokenSources(int run, boolean errorRecovery) {
        def config = new CompilerConfiguration()
        config.optimizationOptions.put(CompilerConfiguration.PARALLEL_PARSE, true)
        config.optimizationOptions.put(CompilerConfiguration.ERROR_RECOVERY, errorRecovery)
        def unit = new CompilationUnit(config)
        SOURCES.times { i -> unit.addSource(sourceName(run, i), "class C${run}_${i} { def m() { foo( } }") }
        shouldFail(CompilationFailedException) {
            unit.compile(Phases.CONVERSION)
        }
        unit
    }

    private static void assertEverySourceReportedOnce(CompilationUnit unit, int run) {
        def errors = unit.errorCollector.errors
        int size = errors.size()
        def reported = []
        for (error in errors) reported << ((SyntaxErrorMessage) error).cause.sourceLocator
        assert reported.size() == size
        assert size == SOURCES
        assert reported.toSet() == (0..<SOURCES).collect { sourceName(run, it) }.toSet()
        assert unit.errorCollector.errorCount == size
    }

    private static String sourceName(int run, int index) {
        "C${run}_${index}.groovy"
    }
}
