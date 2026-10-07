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
import org.eclipse.lsp4j.CompletionItem
import org.eclipse.lsp4j.CompletionItemKind
import org.eclipse.lsp4j.MarkupContent
import org.eclipse.lsp4j.MessageActionItem
import org.eclipse.lsp4j.MessageParams
import org.eclipse.lsp4j.ParameterInformation
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.PublishDiagnosticsParams
import org.eclipse.lsp4j.Range
import org.eclipse.lsp4j.ShowMessageRequestParams
import org.eclipse.lsp4j.TextEdit
import org.eclipse.lsp4j.SignatureInformation
import org.eclipse.lsp4j.WorkspaceSymbol
import org.eclipse.lsp4j.jsonrpc.messages.Either
import org.eclipse.lsp4j.services.LanguageClient
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.PosixFilePermissions
import java.util.concurrent.CompletableFuture

final class GroovyLanguageEngineTest {

    private static final URI BUFFER = URI.create('groovy-buffer:sample')

    private GroovyLanguageEngine engine

    private static boolean posix() {
        FileSystems.default.supportedFileAttributeViews().contains('posix')
    }

    @AfterEach
    void tearDown() {
        engine?.close()
    }

    @Test
    void diagnosticsDescribeDefinitionAndSymbols() {
        engine = new GroovyLanguageEngine()
        def dir = Files.createTempDirectory('groovy-lsp-engine')
        def src = dir.resolve('AbstractServerFactory.groovy')
        Files.writeString(src, '''\
            /** Factory for {@code Server} instances. @param unused ignored */
            class AbstractServerFactory {
                def ping() { ping() }
            }
            '''.stripIndent())
        def uri = engine.open(src)
        assert uri != null
        assert engine.diagnostics(uri) != null
        def hover = engine.describe(uri, 1, 10)
        assert hover.contains('AbstractServerFactory')
        def defs = engine.definition(uri, 2, 8)
        assert defs.any { it.snippet.contains('ping') }
        def refs = engine.references(uri, 2, 8, 10)
        assert refs.total >= 1
        assert !refs.truncated
        assert engine.symbols('ASF', 10).any { it.name == 'AbstractServerFactory' }
        assert engine.implementations(uri, 1, 10) != null
        def rename = engine.rename(uri, 1, 10, 'AbstractServerFactory')
        assert rename.changeCount >= 0
        assert GroovyLanguageEngine.snippet('', 0, 0) == ''
        assert GroovyLanguageEngine.snippet('a\nb\nc', 1, 1).contains('b')
    }

    @Test
    void openRejectsMissingFile() {
        engine = new GroovyLanguageEngine()
        def missing = false
        try {
            engine.open(Path.of('/no/such/groovy-lsp-engine.groovy'))
        } catch (IllegalArgumentException ignored) {
            missing = true
        }
        assert missing
        def nil = false
        try {
            engine.open(null)
        } catch (IllegalArgumentException ignored) {
            nil = true
        }
        assert nil
    }

    @Test
    void diagnosticsAndImplementationsUseTheSuppliedContext() {
        def context = new LanguageServerContext()
        engine = new GroovyLanguageEngine(context)
        assert engine.context().is(context)
        def dir = Files.createTempDirectory('groovy-lsp-engine')
        def broken = dir.resolve('Broken.groovy')
        Files.writeString(broken, 'class Broken { def x = }\n')
        def brokenUri = engine.open(broken)
        def hits = engine.diagnostics(brokenUri)
        assert !hits.isEmpty()
        assert hits[0].message()
        assert hits[0].severity()
        def explicit = new GroovyLanguageEngine.Hit(hits[0].uri(), 0, 0, 0, 0, 'm', 'Error')
        assert explicit.message() == 'm'
        engine.context().documents.close(hits[0].uri().toString())

        def face = dir.resolve('Face.groovy')
        def source = '''\
            interface Face {
                def run()
            }
            class Impl implements Face {
                def run() {}
            }
            '''.stripIndent()
        Files.writeString(face, source)
        def faceUri = engine.open(face)
        def lines = source.readLines()
        int typeLine = lines.findIndexOf { it.contains('interface Face') }
        assert typeLine >= 0 : lines
        int typeCharacter = lines[typeLine].indexOf('Face')
        assert typeCharacter >= 0
        def typeSites = engine.implementations(faceUri, typeLine, typeCharacter)
        assert !typeSites.isEmpty()
        assert typeSites.any { it.snippet().contains('Impl') || it.snippet().contains('run') }

        int runLine = lines.findIndexOf { it.contains('def run()') }
        assert runLine >= 0 : lines
        int runCharacter = lines[runLine].indexOf('run')
        assert runCharacter >= 0
        def methodSites = engine.implementations(faceUri, runLine, runCharacter)
        assert !methodSites.isEmpty()
        assert methodSites.any { it.snippet().contains('run') }
    }

    @Test
    void openRejectsAnUnreadableFileAndAClearedBuffer() {
        def context = new LanguageServerContext()
        engine = new GroovyLanguageEngine(context)
        if (posix()) {
            def hidden = Files.createTempFile('groovy-lsp-hidden', '.groovy')
            Files.writeString(hidden, 'class Hidden {}\n')
            Files.setPosixFilePermissions(hidden, PosixFilePermissions.fromString('---------'))
            def failed = false
            try {
                engine.open(hidden)
            } catch (IllegalArgumentException ex) {
                failed = ex.message.contains('unreadable') && ex.cause instanceof IOException
            } finally {
                Files.setPosixFilePermissions(hidden, PosixFilePermissions.fromString('rw-r--r--'))
                Files.deleteIfExists(hidden)
            }
            assert failed
        }

        context.client = new ClearingClient(context)
        def path = Files.createTempFile('groovy-lsp-clear', '.groovy')
        Files.writeString(path, 'class Clear {}\n')
        def missing = false
        try {
            def uri = engine.open(path)
            engine.describe(uri, 0, 0)
        } catch (IllegalStateException ex) {
            missing = ex.message.contains('not compiled')
        } finally {
            Files.deleteIfExists(path)
        }
        assert missing
    }

    @Test
    void sitesWithoutBuffersAndSymbolsWithoutLocations() {
        engine = new GroovyLanguageEngine()
        def toSite = GroovyLanguageEngine.getDeclaredMethod('toSite', URI, Range)
        toSite.accessible = true
        def site = toSite.invoke(engine, URI.create('file:///coverage/missing.groovy'), null) as GroovyLanguageEngine.Site
        assert site.snippet() == ''

        def toSymbol = GroovyLanguageEngine.getDeclaredMethod('toSymbol', WorkspaceSymbol)
        toSymbol.accessible = true
        def symbol = toSymbol.invoke(engine, new WorkspaceSymbol()) as GroovyLanguageEngine.Symbol
        assert symbol.location().uri() == URI.create('file:///')
        assert symbol.kind() == 'Object'
    }

    @Test
    void candidateSignatureAndTokenHelpersCoverMarkupAndDeltas() {
        engine = new GroovyLanguageEngine()
        def toCandidate = GroovyLanguageEngine.getDeclaredMethod('toCandidate', CompletionItem)
        toCandidate.accessible = true
        def item = new CompletionItem('n')
        item.kind = CompletionItemKind.Method
        item.detail = 'int'
        item.insertText = null
        item.documentation = Either.forLeft('_inferred_ x')
        def candidate = toCandidate.invoke(null, item) as GroovyLanguageEngine.Candidate
        assert candidate.documentation() == '_inferred_ x'
        assert candidate.insertText() == 'n'

        item.documentation = Either.forRight(new MarkupContent('markdown', 'docs'))
        def marked = toCandidate.invoke(null, item) as GroovyLanguageEngine.Candidate
        assert marked.documentation() == 'docs'
        item.documentation = Either.forLeft(null)
        assert (toCandidate.invoke(null, item) as GroovyLanguageEngine.Candidate).documentation() == ''
        item.kind = null
        item.detail = null
        def empty = toCandidate.invoke(null, item) as GroovyLanguageEngine.Candidate
        assert empty.documentation() == ''
        assert empty.kind() == ''
        assert empty.detail() == ''

        def markupOrString = GroovyLanguageEngine.getDeclaredMethod('markupOrString', Either)
        markupOrString.accessible = true
        assert markupOrString.invoke(null, [null] as Object[]) == ''
        assert markupOrString.invoke(null, Either.forLeft(null)) == ''
        assert markupOrString.invoke(null, Either.forRight(new MarkupContent('markdown', ''))) == ''

        def parameterLabel = GroovyLanguageEngine.getDeclaredMethod('parameterLabel', ParameterInformation)
        parameterLabel.accessible = true
        assert parameterLabel.invoke(null, new ParameterInformation()) == ''
        def param = new ParameterInformation()
        param.label = Either.forLeft('p')
        assert parameterLabel.invoke(null, param) == 'p'
        param.label = Either.forRight(null)
        assert parameterLabel.invoke(null, param) == ''

        def toSignature = GroovyLanguageEngine.getDeclaredMethod('toSignature', SignatureInformation)
        toSignature.accessible = true
        def info = new SignatureInformation()
        info.parameters = [param, null]
        def signature = toSignature.invoke(null, info) as GroovyLanguageEngine.Signature
        assert signature.parameters().size() == 2
        info.documentation = Either.forRight(new MarkupContent('markdown', 'sig'))
        assert (toSignature.invoke(null, info) as GroovyLanguageEngine.Signature).documentation() == 'sig'

        def toChanges = GroovyLanguageEngine.getDeclaredMethod('toChanges', URI, List)
        toChanges.accessible = true
        assert (toChanges.invoke(engine, BUFFER, null) as List).isEmpty()
        assert (toChanges.invoke(engine, BUFFER, [null]) as List).isEmpty()
        def rangeless = new TextEdit()
        rangeless.newText = 'x'
        assert (toChanges.invoke(engine, BUFFER, [rangeless]) as List).isEmpty()
        def withRange = new TextEdit()
        withRange.range = new Range(new Position(0, 0), new Position(0, 1))
        withRange.newText = ''
        def changes = toChanges.invoke(engine, BUFFER, [withRange]) as List
        assert changes.size() == 1
        assert changes[0].newText() == ''
    }

    private static final class ClearingClient implements LanguageClient {

        private final LanguageServerContext context

        private ClearingClient(LanguageServerContext context) {
            this.context = context
        }

        @Override
        void telemetryEvent(Object object) {
        }

        @Override
        void publishDiagnostics(PublishDiagnosticsParams diagnostics) {
            context.documents.clear()
            context.snapshot = CompilationSnapshot.EMPTY
        }

        @Override
        void showMessage(MessageParams messageParams) {
        }

        @Override
        CompletableFuture<MessageActionItem> showMessageRequest(ShowMessageRequestParams requestParams) {
            return CompletableFuture.completedFuture(null)
        }

        @Override
        void logMessage(MessageParams message) {
        }
    }
}
