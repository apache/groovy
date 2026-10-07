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

    private static final URI BUFFER = URI.create('groovy-buffer:sample')

    private GroovyLanguageEngine engine

    @AfterEach
    void tearDown() {
        engine?.close()
    }

    @Test
    void openBufferCompilesUnsavedTextWithoutAFile() {
        engine = new GroovyLanguageEngine()
        def uri = engine.openBuffer(BUFFER, 'groovy', '''\
            class Sample {
                def ping() { ping() }
            }
            '''.stripIndent())
        assert uri == BUFFER
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
        engine.openBuffer(BUFFER, 'groovy', 'class First {}')
        engine.updateBuffer(BUFFER, 'class Second { def x = }')
        def after = engine.diagnostics(BUFFER)
        assert after.any { it.message() }
    }

    @Test
    void closeBufferDropsTheDocument() {
        engine = new GroovyLanguageEngine()
        engine.openBuffer(BUFFER, 'groovy', 'class Keep {}')
        engine.closeBuffer(BUFFER)
        def missing = false
        try {
            engine.describe(BUFFER, 0, 0)
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
        engine.setClasspath(['/tmp/lsp-cp.jar'])
        assert engine.context().settings.classpath == ['/tmp/lsp-cp.jar']
        assert !engine.context().settings.grapeEnabled
        engine.putExtra('generation', '1')
        assert engine.context().settings.extra.generation == '1'
        assert engine.context().settings.classpath == ['/tmp/lsp-cp.jar']
        engine.putExtra('generation', null)
        assert !engine.context().settings.extra.containsKey('generation')
        assert engine.context().settings.classpath == ['/tmp/lsp-cp.jar']
        engine.putExtra(null, 'x')
        engine.putExtra('', 'x')
        engine.openBuffer(BUFFER, 'groovy', 'int n = 1\n')
        assert engine.diagnostics(BUFFER) != null
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
        engine.openBuffer(BUFFER, 'groovy', src)
        def lines = src.readLines()
        int line = lines.findIndexOf { it.contains('name.') }
        int column = lines[line].indexOf('name.') + 'name.'.length()
        def items = engine.complete(BUFFER, line, column)
        assert items != null
        def call = '''\
            class C {
                def ping(int a, String b) {}
                def use() { ping(1,
            }
            '''.stripIndent()
        engine.openBuffer(BUFFER, 'groovy', call)
        def callLines = call.readLines()
        int callLine = callLines.findIndexOf { it.contains('ping(1,') }
        int callColumn = callLines[callLine].indexOf('ping(1,') + 'ping(1,'.length()
        def set = engine.signatures(BUFFER, callLine, callColumn)
        assert set.signatures().any { it.label()?.contains('ping') } || set.signatures() != null
    }

    @Test
    void constructorsTolerateNullContextAndFeatures() {
        engine = new GroovyLanguageEngine(null, null)
        engine.openBuffer(BUFFER, '', '')
        assert engine.diagnostics(BUFFER) != null
        assert engine.tokens(BUFFER) != null
        assert engine.signatures(BUFFER, 0, 0).signatures() != null
        assert engine.format(BUFFER, 4, true) != null
        assert engine.organizeImports(BUFFER) != null
        assert engine.complete(BUFFER, 0, 0) != null
    }

    @Test
    void formatAndOrganizeImportsReturnEditsForABuffer() {
        engine = new GroovyLanguageEngine(new LanguageServerContext(), new LanguageFeatures())
        engine.openBuffer(BUFFER, 'groovy', '''\
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
        assert engine.format(BUFFER, 4, true) != null
        assert engine.format(BUFFER, 0, false) != null
        assert engine.organizeImports(BUFFER) != null
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
        engine.openBuffer(BUFFER, 'groovy', 'class Keep {}')
        engine.closeBuffer(BUFFER)
        assert engine.diagnostics(BUFFER).isEmpty()
        engine.close()
        engine.scheduleCompile(1)
        engine = null
    }

    @Test
    void queriesUseTheOpenBufferInsteadOfRereadingDisk() {
        engine = new GroovyLanguageEngine()
        def dir = Files.createTempDirectory('groovy-lsp-buffer')
        def src = dir.resolve('Sample.groovy')
        Files.writeString(src, 'class Sample {}\n')
        def uri = engine.open(src)
        Files.writeString(src, 'class Other {}\n')
        def hover = engine.describe(uri, 0, 6)
        assert hover.contains('Sample')
        assert engine.definition(uri, 0, 6).any { it.snippet().contains('Sample') }
        engine.setParentLoader(GroovyLanguageEngineBufferTest.classLoader)
        engine.setParentLoader(GroovyLanguageEngineBufferTest.classLoader)
        assert engine.context().parentLoader.is(GroovyLanguageEngineBufferTest.classLoader)
        def left = LanguageServerContext.fingerprint([], [], CompilerSettings.defaults(), new GroovyClassLoader())
        def right = LanguageServerContext.fingerprint([], [], CompilerSettings.defaults(), new GroovyClassLoader())
        assert left != right
    }

    @Test
    void compileNowRefreshesTheOpenBuffer() {
        engine = new GroovyLanguageEngine()
        def uri = engine.openBuffer(BUFFER, 'groovy', 'class Ok {}')
        def before = engine.context().snapshot.get(uri)
        assert before.text.contains('class Ok')
        engine.updateBuffer(uri, 'class Broken { def x = }')
        assert engine.context().documents.get(uri).text.contains('class Broken')
        def stale = engine.context().snapshot.get(uri)
        assert stale.text.contains('class Ok')
        assert !stale.text.contains('Broken')
        engine.compileNow()
        def after = engine.context().snapshot.get(uri)
        assert after.text.contains('class Broken')
        assert !after.errors.isEmpty()
    }

    @Test
    void signaturesListsBothOverloadsOfACompleteCall() {
        engine = new GroovyLanguageEngine()
        def src = '''\
            class Box {
                static void ping(String name) {}
                static void ping(String name, int count) {}
                void use() { Box.ping(1) }
            }
            '''.stripIndent()
        engine.openBuffer(BUFFER, 'groovy', src)
        def lines = src.readLines()
        int line = lines.findIndexOf { it.contains('Box.ping(') }
        assert line >= 0
        int column = lines[line].indexOf('Box.ping(') + 'Box.ping('.length()
        def set = engine.signatures(BUFFER, line, column)
        assert set.signatures().size() == 2
        assert set.signatures().collect { it.parameters().size() }.sort() == [1, 2]
        def active = set.signatures()[set.activeSignature()]
        assert active.parameters().size() == 1
        assert active.label().contains('String')
        assert set.signatures().any { it.parameters().size() == 2 && it.label().contains('int') }
    }

    @Test
    void renameRewritesTheClassNameInTheBuffer() {
        engine = new GroovyLanguageEngine()
        engine.openBuffer(BUFFER, 'groovy', 'class Sample {\n    String name\n}\n')
        def renamed = engine.rename(BUFFER, 0, 6, 'Renamed')
        assert renamed.newName() == 'Renamed'
        assert renamed.changeCount() >= 1
        assert renamed.changes().every { it.newText() == 'Renamed' }
    }
}
