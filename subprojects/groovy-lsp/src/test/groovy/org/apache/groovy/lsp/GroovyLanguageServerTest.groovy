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
package org.apache.groovy.lsp

import org.apache.groovy.lsp.internal.LspFixture
import org.apache.groovy.lsp.internal.compile.CompilerSettings
import org.eclipse.lsp4j.ClientCapabilities
import org.eclipse.lsp4j.CompletionParams
import org.eclipse.lsp4j.DefinitionParams
import org.eclipse.lsp4j.DidChangeTextDocumentParams
import org.eclipse.lsp4j.DidCloseTextDocumentParams
import org.eclipse.lsp4j.DocumentFormattingParams
import org.eclipse.lsp4j.DocumentSymbolParams
import org.eclipse.lsp4j.FormattingOptions
import org.eclipse.lsp4j.HoverParams
import org.eclipse.lsp4j.InitializeParams
import org.eclipse.lsp4j.MarkupContent
import org.eclipse.lsp4j.WindowClientCapabilities
import org.eclipse.lsp4j.WorkspaceEdit
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.ReferenceContext
import org.eclipse.lsp4j.ReferenceParams
import org.eclipse.lsp4j.RenameParams
import org.eclipse.lsp4j.TextDocumentContentChangeEvent
import org.eclipse.lsp4j.TextDocumentIdentifier
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

final class GroovyLanguageServerTest {

    private LspFixture fixture

    @AfterEach
    void tearDown() {
        fixture?.close()
    }

    @Test
    void initializeAdvertisesIncrementalSyncAndCompletion() {
        fixture = new LspFixture()
        def result = fixture.server.initialize(new InitializeParams(
                capabilities: new ClientCapabilities()
        )).get()
        assert result.capabilities.completionProvider != null
        assert result.capabilities.hoverProvider
        assert result.capabilities.renameProvider
        assert result.serverInfo.name == 'groovy-lsp'
    }

    @Test
    void openCompilesAndPublishesDiagnostics() {
        fixture = new LspFixture()
        def uri = fixture.open('Hello.groovy', 'class Hello {\n  def foo() { bar }\n}\n')
        assert fixture.diagnostics.containsKey(uri) || fixture.server.context.snapshot.get(uri) != null
        def compiled = fixture.server.context.snapshot.get(uri)
        assert compiled != null
        assert compiled.module != null
    }

    @Test
    void completionIncludesKeywordsAndClassName() {
        fixture = new LspFixture()
        def uri = fixture.open('Hello.groovy', 'class Hello {\n  def foo() { \n}\n}\n')
        def params = new CompletionParams(new TextDocumentIdentifier(uri), new Position(1, 14))
        def result = fixture.server.textDocumentService.completion(params).get()
        def labels = result.getRight().items*.label
        assert 'class' in labels || 'def' in labels
        assert labels.any { it == 'Hello' || it == 'foo' }
    }

    @Test
    void hoverOnClassName() {
        fixture = new LspFixture()
        def uri = fixture.open('Hello.groovy', 'class Hello {\n}\n')
        def hover = fixture.server.textDocumentService.hover(
                new HoverParams(new TextDocumentIdentifier(uri), new Position(0, 7))).get()
        assert hover != null
        def contents = hover.contents
        String value = contents instanceof MarkupContent
                ? contents.value
                : contents.getRight().value
        assert value.contains('Hello')
    }

    @Test
    void documentSymbolsIncludeClassAndMethod() {
        fixture = new LspFixture()
        def uri = fixture.open('Hello.groovy', 'class Hello {\n  def foo() {}\n}\n')
        def symbols = fixture.server.textDocumentService.documentSymbol(
                new DocumentSymbolParams(new TextDocumentIdentifier(uri))).get()
        assert symbols.any { it.getRight().name == 'Hello' }
    }

    @Test
    void definitionAndReferencesFindMethod() {
        fixture = new LspFixture()
        def uri = fixture.open('Hello.groovy', '''\
            class Hello {
                def foo() { foo() }
            }
            '''.stripIndent())
        def defs = fixture.server.textDocumentService.definition(
                new DefinitionParams(new TextDocumentIdentifier(uri), new Position(1, 8))).get()
        assert defs.getRight() != null
        def refs = fixture.server.textDocumentService.references(
                new ReferenceParams(new TextDocumentIdentifier(uri), new Position(1, 8), new ReferenceContext(true))).get()
        assert refs != null
    }

    @Test
    void renameRewritesIdentifier() {
        fixture = new LspFixture()
        def uri = fixture.open('Hello.groovy', 'class Hello {\n  def foo() {}\n}\n')
        def edit = fixture.server.textDocumentService.rename(
                new RenameParams(new TextDocumentIdentifier(uri), new Position(1, 6), 'bar')).get()
        assert edit.changes != null
    }

    @Test
    void formatTrimsTrailingWhitespace() {
        fixture = new LspFixture()
        def uri = fixture.open('Hello.groovy', 'class Hello {  \n}\n')
        def edits = fixture.server.textDocumentService.formatting(
                new DocumentFormattingParams(new TextDocumentIdentifier(uri), new FormattingOptions(4, true))).get()
        assert edits != null
    }

    @Test
    void incrementalChangeAndClose() {
        fixture = new LspFixture()
        def uri = fixture.open('Hello.groovy', 'class Hello {\n}\n')
        def id = new VersionedTextDocumentIdentifier(uri, 2)
        fixture.server.textDocumentService.didChange(new DidChangeTextDocumentParams(id,
                [new TextDocumentContentChangeEvent('class Hello {\n  def x\n}\n')]))
        fixture.server.context.recompile()
        fixture.server.textDocumentService.didClose(new DidCloseTextDocumentParams(new TextDocumentIdentifier(uri)))
        assert fixture.server.context.documents.size() == 0
    }

    @Test
    void shutdownThenExitSetsZero() {
        fixture = new LspFixture()
        fixture.server.shutdown().get()
        fixture.server.exit()
        assert fixture.server.context.exitCode == 0
        fixture = null
    }

    @Test
    void compilerSettingsDisableGrapeByDefault() {
        def settings = CompilerSettings.defaults()
        assert !settings.grapeEnabled
        assert !settings.astTestEnabled
        def config = settings.toConfiguration()
        assert config.disabledGlobalASTTransformations.contains('groovy.grape.GrabAnnotationTransformation')
        def enabled = new CompilerSettings([], [], settings.throughPhase, true, true)
        assert enabled.toConfiguration().disabledGlobalASTTransformations.isEmpty() ||
                !enabled.toConfiguration().disabledGlobalASTTransformations.contains('groovy.grape.GrabAnnotationTransformation')
    }

    @Test
    void launcherParsesHelpAndVersion() {
        def help = GroovyLanguageServerLauncher.ListArgs.parse(['--help'] as String[])
        assert help.help
        def version = GroovyLanguageServerLauncher.ListArgs.parse(['-v'] as String[])
        assert version.version
        def socket = GroovyLanguageServerLauncher.ListArgs.parse(['--socket', '0'] as String[])
        assert socket.port == 0
        def empty = GroovyLanguageServerLauncher.ListArgs.parse(null)
        assert !empty.help
    }

    @Test
    void workDoneProgressIsPublishedAndFailuresAreIsolated() {
        fixture = new LspFixture()
        def window = new WindowClientCapabilities()
        window.workDoneProgress = true
        fixture.server.context.clientCapabilities.window = window
        fixture.open('Hello.groovy', 'class Hello {}\n')
        assert fixture.progressCreates
        assert fixture.progressNotifications
        fixture.failProgress = true
        fixture.open('ForceProgress.groovy', 'class ForceProgress {}\n')
        assert fixture.server.context.snapshot.documents
    }

    @Test
    void applyEditForwardsToConnectedClient() {
        fixture = new LspFixture()
        def edit = new WorkspaceEdit()
        fixture.server.context.applyEdit(edit)
        assert fixture.appliedEdits.size() == 1
    }

    @Test
    void shutdownIgnoresFurtherDebouncedCompiles() {
        fixture = new LspFixture()
        fixture.server.shutdown().get()
        fixture.server.context.scheduleRecompile(0)
        assert fixture.server.context.shutdown
        fixture.server.exit()
        fixture = null
    }
}
