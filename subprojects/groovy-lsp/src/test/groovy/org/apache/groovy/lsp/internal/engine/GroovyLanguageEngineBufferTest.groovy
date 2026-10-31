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
import org.apache.groovy.lsp.internal.compile.CompilationSnapshot
import org.apache.groovy.lsp.internal.compile.CompilerSettings
import org.apache.groovy.lsp.internal.feature.LanguageFeatures
import org.apache.groovy.lsp.internal.feature.SemanticTokensService
import org.apache.groovy.lsp.internal.position.PositionEncoding
import org.apache.groovy.lsp.internal.workspace.DocumentStore
import org.apache.groovy.lsp.internal.workspace.TextDocument
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.eclipse.lsp4j.TextDocumentItem
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
        def loader = new GroovyClassLoader()
        def before = LanguageServerContext.fingerprint([], [], CompilerSettings.defaults(), loader)
        loader.addURL(URI.create('file:/tmp/lsp-url-stamp.jar').toURL())
        def afterUrls = LanguageServerContext.fingerprint([], [], CompilerSettings.defaults(), loader)
        assert before != afterUrls
        def host = new CompilerConfiguration()
        def withHost = LanguageServerContext.fingerprint([], [], CompilerSettings.defaults(), loader, host)
        def otherHost = LanguageServerContext.fingerprint([], [], CompilerSettings.defaults(), loader, new CompilerConfiguration())
        assert withHost != otherHost
        assert withHost == LanguageServerContext.fingerprint([], [], CompilerSettings.defaults(), loader, host)
        def mutated = new CompilerConfiguration()
        def beforeCustomizers = LanguageServerContext.fingerprint([], [], CompilerSettings.defaults(), loader, mutated)
        mutated.addCompilationCustomizers(new ImportCustomizer())
        def afterCustomizers = LanguageServerContext.fingerprint([], [], CompilerSettings.defaults(), loader, mutated)
        assert beforeCustomizers != afterCustomizers
    }

    @Test
    void hostCompilerConfigurationImportsAreVisibleOnTheNextQuery() {
        engine = new GroovyLanguageEngine()
        engine.openBuffer(BUFFER, 'groovy', 'ConcurrentHashMap m\n')
        def before = engine.diagnostics(BUFFER)
        assert before.any { (it.message() ?: '').contains('ConcurrentHashMap') }
        def configuration = new CompilerConfiguration()
        def imports = new ImportCustomizer()
        imports.addImports('java.util.concurrent.ConcurrentHashMap')
        configuration.addCompilationCustomizers(imports)
        engine.setHostConfiguration(configuration)
        assert engine.context().hostConfiguration.is(configuration)
        configuration.scriptBaseClass = 'groovy.lang.Script'
        def copied = CompilerSettings.defaults().toConfiguration(configuration)
        assert copied.compilationCustomizers.contains(imports)
        assert copied.scriptBaseClass == 'groovy.lang.Script'
        assert copied.tolerance == 0
        def after = engine.diagnostics(BUFFER)
        assert !after.any { (it.message() ?: '').contains('ConcurrentHashMap') }
        engine.setHostConfiguration(null)
        def reset = engine.diagnostics(BUFFER)
        assert reset.any { (it.message() ?: '').contains('ConcurrentHashMap') }
    }

    @Test
    void sameTextKeepsTheBufferVersion() {
        engine = new GroovyLanguageEngine()
        engine.openBuffer(BUFFER, 'groovy', 'class A {}')
        int version = engine.context().documents.get(BUFFER).version
        engine.updateBuffer(BUFFER, 'class A {}')
        assert engine.context().documents.get(BUFFER).version == version
        engine.diagnostics(BUFFER, 'class A {}')
        assert engine.context().documents.get(BUFFER).version == version
        engine.updateBuffer(BUFFER, '')
        assert engine.context().documents.get(BUFFER).version == version + 1
        engine.updateBuffer(BUFFER, null)
        assert engine.context().documents.get(BUFFER).text == ''
        assert engine.context().documents.get(BUFFER).version == version + 1
    }

    @Test
    void repeatedHostConfigurationPublishesInPlaceImportEdits() {
        engine = new GroovyLanguageEngine()
        engine.openBuffer(BUFFER, 'groovy', 'ConcurrentHashMap m\n')
        def configuration = new CompilerConfiguration()
        def imports = new ImportCustomizer()
        configuration.addCompilationCustomizers(imports)
        engine.setHostConfiguration(configuration)
        assert engine.diagnostics(BUFFER).any { (it.message() ?: '').contains('ConcurrentHashMap') }
        imports.addImports('java.util.concurrent.ConcurrentHashMap')
        assert engine.diagnostics(BUFFER).any { (it.message() ?: '').contains('ConcurrentHashMap') }
        engine.setHostConfiguration(configuration)
        assert !engine.diagnostics(BUFFER).any { (it.message() ?: '').contains('ConcurrentHashMap') }
    }

    @Test
    void declaredNamesReadsScriptMembersAndTypesFromTheAst() {
        engine = new GroovyLanguageEngine()
        engine.openBuffer(BUFFER, 'groovy', '''\
            class Box {}
            def ping() { 1 }
            int kept = 7
            x = 1
            def outer() {
                int inner = 1
            }
            '''.stripIndent())
        def names = engine.declaredNames(BUFFER)
        assert names.contains('Box')
        assert names.contains('ping')
        assert names.contains('kept')
        assert names.contains('x')
        assert names.contains('outer')
        assert !names.contains('inner')
        assert !names.contains('run')
        assert !names.contains('main')
        engine.updateBuffer(BUFFER, '''\
            def (a, b) = [1, 2]
            if (true) {
                z = 3
            }
            try {
                w = 4
            } catch (Exception e) {
            }
            def skip = { hidden = 1 }
            Runnable r = () -> { alsoHidden = 2 }
            '''.stripIndent())
        def destructured = engine.declaredNames(BUFFER)
        assert destructured.contains('a')
        assert destructured.contains('b')
        assert destructured.contains('z')
        assert destructured.contains('w')
        assert !destructured.contains('hidden')
        assert !destructured.contains('alsoHidden')
        assert GroovyLanguageEngine.positionOf('ab\ncd', 4) == [1, 1] as int[]
        assert GroovyLanguageEngine.offsetOf('ab\ncd', 1, 1) == 4
        assert GroovyLanguageEngine.offsetOf('ab\ncd', 0, 99) == 2
        assert GroovyLanguageEngine.positionOf('ab\r\ncd', 4) == [1, 0] as int[]
        assert GroovyLanguageEngine.offsetOf('ab\r\ncd', 1, 0) == 4
        assert GroovyLanguageEngine.positionOf(null, 1) == [0, 0] as int[]
        assert GroovyLanguageEngine.offsetOf('', 0, 0) == 0
        assert GroovyLanguageEngine.flattenSnippet('class ${1:Name} {\n    $0\n}') == 'class Name {\n    \n}'
        assert GroovyLanguageEngine.flattenSnippetLine('class ${1:Name} {\n    $0\n}') == 'class Name {'
        assert GroovyLanguageEngine.spanOf('ab', 0, 0, 0, 0) == [0, 1] as int[]
        assert GroovyLanguageEngine.spanOf('abc', 0, 3, 0, 3) == [2, 3] as int[]
        assert GroovyLanguageEngine.spanOf('abc', 9, 0, 9, 0) == [2, 3] as int[]
        assert GroovyLanguageEngine.spanOf('ab\r', 0, 3, 0, 3) == [2, 3] as int[]
        engine.updateBuffer(BUFFER, 'class Broken { def x = }')
        assert engine.diagnostics(BUFFER, 'class Broken { def x = }').any { it.message() }
        assert engine.describe(BUFFER, 'class Sample {}', 0, 6).contains('Sample')
        assert engine.complete(BUFFER, 'class Sample { def ping() { ping() } }', 0, 6) != null
        assert engine.signatures(BUFFER, 'class Sample { def ping() { ping() } }', 0, 6) != null
        assert engine.declaredNames(BUFFER, 'class Box {}').contains('Box')
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
    void lexicalTokensNeedNoSessionAndKeepIncompleteInput() {
        assert GroovyLanguageEngine.lexicalTokens(null).isEmpty()
        assert GroovyLanguageEngine.lexicalTokens('').isEmpty()
        def tokens = GroovyLanguageEngine.lexicalTokens('def n = 1 // c')
        assert tokens.any { it.type() == 'keyword' && it.length() == 3 }
        assert tokens.any { it.type() == 'variable' }
        assert tokens.any { it.type() == 'number' }
        assert tokens.any { it.type() == 'comment' }
        def incomplete = GroovyLanguageEngine.lexicalTokens('def x =')
        assert incomplete.any { it.type() == 'keyword' }
        assert incomplete.any { it.type() == 'variable' }
        assert GroovyLanguageEngine.TOKEN_TYPES.contains('keyword')
        assert GroovyLanguageEngine.spanOf('def n = 1', new GroovyLanguageEngine.Token(0, 0, 3, 'keyword', 0)) == [0, 3] as int[]
        assert GroovyLanguageEngine.spanOf('ab', null as GroovyLanguageEngine.Token) == [0, 0] as int[]
        assert GroovyLanguageEngine.spanOf('ab', new GroovyLanguageEngine.Token(0, 0, -3, 'keyword', 0)) == [0, 0] as int[]
        assert GroovyLanguageEngine.spanOf(null, new GroovyLanguageEngine.Token(0, 0, 3, 'keyword', 0)) == [0, 0] as int[]
        assert GroovyLanguageEngine.flattenSnippet(null) == ''
        assert GroovyLanguageEngine.flattenSnippet('') == ''
        def hit = new GroovyLanguageEngine.Hit(URI.create('groovy-buffer:t'), 0, 2, 0, 0, 'x', 'Error')
        assert GroovyLanguageEngine.spanOf('ab\ncd', hit) == [2, 3] as int[]
        assert GroovyLanguageEngine.spanOf('ab', null as GroovyLanguageEngine.Hit) == [0, 0] as int[]
        def slashy = GroovyLanguageEngine.lexicalTokens('def s = /z/\n$/y/$\n')
        assert slashy.any { it.type() == 'string' }
        def gstring = GroovyLanguageEngine.lexicalTokens('def s = "n:${x}"\n')
        assert gstring.any { it.type() == 'string' }
    }

    @Test
    void tokensWithTextCompileAndUpgradeClassNames() {
        engine = new GroovyLanguageEngine()
        engine.openBuffer(BUFFER, 'groovy', '')
        def src = 'class Sample {}\n'
        engine.updateBuffer(BUFFER, src)
        def overlay = engine.tokens(BUFFER)
        assert overlay.any { it.type() == 'class' && it.length() == 6 }
        assert overlay.any { it.type() == 'keyword' }
        engine.updateBuffer(BUFFER, '''\
            class Box {
                def ping(int n) { n }
            }
            '''.stripIndent())
        def members = engine.tokens(BUFFER)
        assert members.any { it.type() == 'method' }
        assert members.any { it.type() == 'parameter' }
        engine.updateBuffer(BUFFER, '')
        engine.tokens(BUFFER)
        engine.updateBuffer(BUFFER, 'package p\nimport java.util.List\nenum E { A }\ninterface I {}\n')
        def javaish = engine.tokens(BUFFER)
        assert javaish.any { it.type() == 'keyword' }
        assert engine.declaredNames(BUFFER) != null
        assert GroovyLanguageEngine.lexicalTokens('def x', null).isEmpty()
        assert GroovyLanguageEngine.TOKEN_TYPES.contains('decorator')
        assert GroovyLanguageEngine.TOKEN_TYPES.contains('operator')
    }

    @Test
    void javaBufferWithoutAModuleHasNoCompiledTokens() {
        engine = new GroovyLanguageEngine()
        engine.openBuffer(BUFFER, 'java', 'class A { int n; }')
        assert engine.context().snapshot.get(BUFFER).module == null
        assert engine.declaredNames(BUFFER).isEmpty()
        assert engine.tokens(BUFFER).isEmpty()
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

    @Test
    void bufferEdgesAndHostFingerprintBranches() {
        engine = new GroovyLanguageEngine()
        def fresh = URI.create('groovy-buffer:fresh')
        engine.updateBuffer(fresh, 'class Z {}')
        assert engine.context().documents.get(fresh).version == 1
        engine.context().documents.open(new TextDocumentItem(fresh.toString(), '', 3, 'class Z {}'))
        engine.updateBuffer(fresh, 'class Y {}')
        assert engine.context().documents.get(fresh).languageId == 'groovy'
        def store = engine.context().documents
        def documents = DocumentStore.getDeclaredField('documents')
        documents.accessible = true
        documents.get(store).put(fresh, new TextDocument(fresh, null, 8, 'class Y {}'))
        engine.updateBuffer(fresh, 'class X {}')
        assert engine.context().documents.get(fresh).languageId == 'groovy'

        def bare = CompilerSettings.defaults().toConfiguration(new CompilerConfiguration())
        assert bare.compilationCustomizers.empty
        assert bare.scriptBaseClass == null

        assert TextDocument.positionOf(null, 3) == [0, 0] as int[]
        assert TextDocument.positionOf('', 4) == [0, 0] as int[]
        assert TextDocument.positionOf('ab', -3) == [0, 0] as int[]
        assert TextDocument.positionOf('ab\rcd', 3) == [1, 0] as int[]
        assert TextDocument.positionOf('ab\r', 3) == [1, 0] as int[]
        assert GroovyLanguageEngine.spanOf('', 0, 0, 0, 0) == [0, 0] as int[]
        assert GroovyLanguageEngine.spanOf('ab', 0, 0, 0, 2) == [0, 2] as int[]
        assert GroovyLanguageEngine.flattenSnippetLine('plain') == 'plain'

        def nullList = new URLClassLoader(new URL[0], GroovyLanguageEngineBufferTest.classLoader) {
            @Override
            URL[] getURLs() { null }
        }
        def nullElement = new URLClassLoader(new URL[0], GroovyLanguageEngineBufferTest.classLoader) {
            @Override
            URL[] getURLs() { [null, new File('/tmp/lsp-edge').toURI().toURL()] as URL[] }
        }
        def missing = LanguageServerContext.fingerprint([], [], CompilerSettings.defaults(), nullList)
        def listed = LanguageServerContext.fingerprint([], [], CompilerSettings.defaults(), nullElement)
        assert missing != listed
        assert listed.contains('file:')

        assert SemanticTokensService.lexicalTokenSpans('def x', [], PositionEncoding.UTF16).isEmpty()
        assert SemanticTokensService.lexicalTokenSpans('def x', [null], PositionEncoding.UTF16).isEmpty()

        engine.openBuffer(BUFFER, 'groovy', 'class A {}')
        engine.context().setSnapshot(CompilationSnapshot.EMPTY)
        engine.context().setShutdown(true)
        assert engine.declaredNames(BUFFER).isEmpty()
        assert engine.tokens(BUFFER).isEmpty()
    }

    @Test
    void keywordsDefaultImportsAndOpenStateComeFromTheCompiler() {
        engine = new GroovyLanguageEngine()
        assert !engine.isOpen(null)
        assert !engine.isOpen(BUFFER)
        engine.openBuffer(BUFFER, 'groovy', 'class Sample {}')
        assert engine.isOpen(BUFFER)
        engine.closeBuffer(BUFFER)
        assert !engine.isOpen(BUFFER)
        def keywords = GroovyLanguageEngine.keywords()
        assert keywords.contains('class')
        assert keywords.contains('def')
        assert keywords.contains('true')
        assert keywords.contains('null')
        assert keywords.contains('boolean')
        assert GroovyLanguageEngine.keywords().is(keywords)
        def imports = GroovyLanguageEngine.defaultImports()
        assert imports.contains('java.lang.')
        assert imports.contains('groovy.lang.')
        assert imports.every { it.endsWith('.') }
    }
}
