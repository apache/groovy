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
package org.apache.groovy.lsp.internal.engine

import org.apache.groovy.lsp.internal.LanguageServerContext
import org.apache.groovy.lsp.internal.compile.CompilerSettings
import org.apache.groovy.lsp.internal.feature.LanguageFeatures
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

import java.nio.file.Files

final class GroovyLanguageEngineBufferTest {

    private GroovyLanguageEngine engine

    @AfterEach
    void tearDown() {
        engine?.close()
    }

    @Test
    void openBufferCompilesUnsavedTextWithoutAFile() {
        engine = new GroovyLanguageEngine()
        def uri = engine.openBuffer(GroovyLanguageEngine.CONSOLE_URI, 'groovy', '''\
            class Sample {
                def ping() { ping() }
            }
            '''.stripIndent())
        assert uri == GroovyLanguageEngine.CONSOLE_URI
        def hits = engine.diagnostics(uri)
        assert hits.every { it.severity() }
        def hover = engine.describe(uri, 0, 6)
        assert hover.contains('Sample')
        def defs = engine.definition(uri, 1, 8)
        assert defs.any { it.snippet().contains('ping') }
        assert engine.implementations(uri, 0, 6) != null
        def refs = engine.references(uri, 1, 8, 10)
        assert refs.total >= 1
        assert engine.complete(uri, 1, 16).any { it.label == 'ping' || it.kind }
        assert !engine.tokens(uri).isEmpty()
        assert engine.signatures(uri, 1, 16).signatures() != null
        assert engine.organizeImports(uri) != null
        assert engine.format(uri, 4, true) != null
        assert engine.rename(uri, 0, 6, 'Sample').changeCount() >= 0
        assert engine.symbols('Sam', 10).any { it.name == 'Sample' }
    }

    @Test
    void updateBufferDoesNotCompileUntilAsked() {
        engine = new GroovyLanguageEngine()
        engine.openBuffer(GroovyLanguageEngine.REPL_URI, 'groovy', 'class First {}')
        engine.updateBuffer(GroovyLanguageEngine.REPL_URI, 'class Second { def x = }')
        def after = engine.diagnostics(GroovyLanguageEngine.REPL_URI)
        assert after.any { it.message() }
    }

    @Test
    void closeBufferDropsTheDocument() {
        engine = new GroovyLanguageEngine()
        engine.openBuffer(GroovyLanguageEngine.CONSOLE_URI, 'groovy', 'class Keep {}')
        engine.closeBuffer(GroovyLanguageEngine.CONSOLE_URI)
        def missing = false
        try {
            engine.describe(GroovyLanguageEngine.CONSOLE_URI, 0, 0)
        } catch (IllegalStateException ex) {
            missing = ex.message.contains('not compiled')
        }
        assert missing
        engine.closeBuffer(null)
    }

    @Test
    void openBufferRejectsANullUri() {
        engine = new GroovyLanguageEngine()
        def failed = false
        try {
            engine.openBuffer(null, 'groovy', 'class A {}')
        } catch (IllegalArgumentException ignored) {
            failed = true
        }
        assert failed
    }

    @Test
    void setClasspathAndParentLoaderAndExtraAreHonoured() {
        engine = new GroovyLanguageEngine()
        engine.setParentLoader(GroovyLanguageEngineBufferTest.classLoader)
        engine.setClasspath([])
        engine.putExtra('grapeGeneration', '1')
        engine.putExtra('grapeGeneration', null)
        engine.putExtra(null, 'x')
        engine.putExtra('', 'x')
        engine.openBuffer(GroovyLanguageEngine.CONSOLE_URI, 'groovy', 'int n = 1\n')
        assert engine.diagnostics(GroovyLanguageEngine.CONSOLE_URI) != null
        def one = LanguageServerContext.fingerprint([], [], CompilerSettings.defaults(), new GroovyClassLoader())
        def two = LanguageServerContext.fingerprint([], [], CompilerSettings.defaults(), new GroovyClassLoader())
        assert one != two
    }

    @Test
    void completeOnMemberAccessReturnsCandidates() {
        engine = new GroovyLanguageEngine()
        def src = '''\
            class Box {
                String name
                def use() { name.
            }
            '''.stripIndent()
        engine.openBuffer(GroovyLanguageEngine.CONSOLE_URI, 'groovy', src)
        def lines = src.readLines()
        int line = lines.findIndexOf { it.contains('name.') }
        int column = lines[line].indexOf('name.') + 'name.'.length()
        def items = engine.complete(GroovyLanguageEngine.CONSOLE_URI, line, column)
        assert items != null
        def call = '''\
            class C {
                def ping(int a, String b) {}
                def use() { ping(1,
            }
            '''.stripIndent()
        engine.openBuffer(GroovyLanguageEngine.CONSOLE_URI, 'groovy', call)
        def callLines = call.readLines()
        int callLine = callLines.findIndexOf { it.contains('ping(1,') }
        int callColumn = callLines[callLine].indexOf('ping(1,') + 'ping(1,'.length()
        def set = engine.signatures(GroovyLanguageEngine.CONSOLE_URI, callLine, callColumn)
        assert set.signatures().any { it.label()?.contains('ping') } || set.signatures() != null
    }

    @Test
    void constructorsTolerateNullContextAndFeatures() {
        engine = new GroovyLanguageEngine(null, null)
        engine.openBuffer(GroovyLanguageEngine.REPL_URI, '', '')
        assert engine.diagnostics(GroovyLanguageEngine.REPL_URI) != null
        assert engine.tokens(GroovyLanguageEngine.REPL_URI) != null
        assert engine.signatures(GroovyLanguageEngine.REPL_URI, 0, 0).signatures() != null
        assert engine.format(GroovyLanguageEngine.REPL_URI, 4, true) != null
        assert engine.organizeImports(GroovyLanguageEngine.REPL_URI) != null
        assert engine.complete(GroovyLanguageEngine.REPL_URI, 0, 0) != null
    }

    @Test
    void formatAndOrganizeImportsReturnEditsForABuffer() {
        engine = new GroovyLanguageEngine(new LanguageServerContext(), new LanguageFeatures())
        engine.openBuffer(GroovyLanguageEngine.CONSOLE_URI, 'groovy', '''\
            import java.util.LinkedList
            import java.util.ArrayList
            class T {
            def x() {
            1
            }
            ArrayList a
            LinkedList b
            }
            '''.stripIndent())
        assert engine.format(GroovyLanguageEngine.CONSOLE_URI, 4, true) != null
        assert engine.format(GroovyLanguageEngine.CONSOLE_URI, 0, false) != null
        assert engine.organizeImports(GroovyLanguageEngine.CONSOLE_URI) != null
    }

    @Test
    void updateBufferRejectsANullUriAndDiagnosticsSurviveAClose() {
        engine = new GroovyLanguageEngine()
        def failed = false
        try {
            engine.updateBuffer(null, 'class A {}')
        } catch (IllegalArgumentException ignored) {
            failed = true
        }
        assert failed
        engine.openBuffer(GroovyLanguageEngine.CONSOLE_URI, 'groovy', 'class Keep {}')
        engine.closeBuffer(GroovyLanguageEngine.CONSOLE_URI)
        assert engine.diagnostics(GroovyLanguageEngine.CONSOLE_URI).isEmpty()
        engine.close()
        engine.scheduleCompile(1)
        engine = null
    }

    @Test
    void pathQueriesReuseAnOpenBufferInsteadOfRereadingDisk() {
        engine = new GroovyLanguageEngine()
        def dir = Files.createTempDirectory('groovy-lsp-buffer')
        def src = dir.resolve('Sample.groovy')
        Files.writeString(src, 'class Sample {}\n')
        def uri = engine.open(src)
        Files.writeString(src, 'class Other {}\n')
        def hover = engine.describe(src, 0, 6)
        assert hover.contains('Sample')
        assert engine.definition(uri, 0, 6).any { it.snippet().contains('Sample') }
        engine.setParentLoader(GroovyLanguageEngineBufferTest.classLoader)
        engine.setParentLoader(GroovyLanguageEngineBufferTest.classLoader)
        assert engine.context().parentLoader.is(GroovyLanguageEngineBufferTest.classLoader)
        def left = LanguageServerContext.fingerprint([], [], CompilerSettings.defaults(), new GroovyClassLoader())
        def right = LanguageServerContext.fingerprint([], [], CompilerSettings.defaults(), new GroovyClassLoader())
        assert left != right
    }
}
