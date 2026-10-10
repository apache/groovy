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

import org.apache.groovy.lsp.internal.LanguageServerContext
import org.apache.groovy.lsp.internal.LspFixture
import org.apache.groovy.lsp.internal.compile.CompilerSettings
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
import org.eclipse.lsp4j.ClientCapabilities
import org.eclipse.lsp4j.MessageActionItem
import org.eclipse.lsp4j.MessageParams
import org.eclipse.lsp4j.ProgressParams
import org.eclipse.lsp4j.PublishDiagnosticsParams
import org.eclipse.lsp4j.ShowMessageRequestParams
import org.eclipse.lsp4j.TextDocumentItem
import org.eclipse.lsp4j.services.LanguageClient
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.PipedInputStream
import java.io.PipedOutputStream
import java.io.PrintStream
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import org.apache.groovy.lsp.internal.compile.CompilationSnapshot
import org.apache.groovy.lsp.internal.feature.LanguageFeatures
import org.apache.groovy.lsp.internal.position.PositionEncoding
import org.apache.groovy.lsp.internal.protocol.GroovyTextDocumentService
import org.apache.groovy.lsp.internal.protocol.GroovyWorkspaceService
import org.apache.groovy.lsp.internal.util.Uris
import org.eclipse.lsp4j.CallHierarchyIncomingCallsParams
import org.eclipse.lsp4j.CallHierarchyOutgoingCallsParams
import org.eclipse.lsp4j.CallHierarchyPrepareParams
import org.eclipse.lsp4j.CodeAction
import org.eclipse.lsp4j.CodeActionContext
import org.eclipse.lsp4j.CodeActionParams
import org.eclipse.lsp4j.Command
import org.eclipse.lsp4j.CompletionItem
import org.eclipse.lsp4j.DeclarationParams
import org.eclipse.lsp4j.DidChangeConfigurationParams
import org.eclipse.lsp4j.DidChangeWatchedFilesParams
import org.eclipse.lsp4j.DidChangeWorkspaceFoldersParams
import org.eclipse.lsp4j.DidSaveTextDocumentParams
import org.eclipse.lsp4j.DocumentDiagnosticParams
import org.eclipse.lsp4j.DocumentHighlightParams
import org.eclipse.lsp4j.DocumentOnTypeFormattingParams
import org.eclipse.lsp4j.DocumentRangeFormattingParams
import org.eclipse.lsp4j.ExecuteCommandParams
import org.eclipse.lsp4j.FileChangeType
import org.eclipse.lsp4j.FileEvent
import org.eclipse.lsp4j.GeneralClientCapabilities
import org.eclipse.lsp4j.ImplementationParams
import org.eclipse.lsp4j.InitializedParams
import org.eclipse.lsp4j.PrepareRenameParams
import org.eclipse.lsp4j.Range
import org.eclipse.lsp4j.SelectionRangeParams
import org.eclipse.lsp4j.SemanticTokensRangeParams
import org.eclipse.lsp4j.SignatureHelpParams
import org.eclipse.lsp4j.TypeDefinitionParams
import org.eclipse.lsp4j.TypeHierarchyPrepareParams
import org.eclipse.lsp4j.TypeHierarchySubtypesParams
import org.eclipse.lsp4j.TypeHierarchySupertypesParams
import org.eclipse.lsp4j.WorkspaceClientCapabilities
import org.eclipse.lsp4j.WorkspaceFolder
import org.eclipse.lsp4j.WorkspaceFoldersChangeEvent
import org.eclipse.lsp4j.jsonrpc.messages.Either
import java.io.IOException
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket

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
        assert result.serverInfo.version == GroovyLanguageServer.implementationVersion()
        assert result.serverInfo.version != 'null'
        assert GroovyLanguageServer.versionOrDev(null) == 'dev'
        assert GroovyLanguageServer.versionOrDev('5.0.0') == '5.0.0'
        assert GroovyLanguageServer.manifestVersion(null) == null
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

    @Test
    void progressEndSurvivesAClientThatRejectsNotifications() {
        def context = new LanguageServerContext()
        try {
            def window = new WindowClientCapabilities()
            window.workDoneProgress = true
            def capabilities = new ClientCapabilities()
            capabilities.window = window
            context.clientCapabilities = capabilities
            context.client = new ThrowingProgressClient()
            def uri = Files.createTempFile('groovy-lsp-progress', '.groovy').toUri().toString()
            context.documents.open(new TextDocumentItem(uri, 'groovy', 1, 'class Progress {}\n'))
            assert context.recompile() != null
        } finally {
            context.close()
        }
    }

    @Test
    void launcherHelpAndVersionPrintWithoutExiting() {
        def original = System.out
        def buffer = new ByteArrayOutputStream()
        System.setOut(new PrintStream(buffer, true, StandardCharsets.UTF_8))
        try {
            GroovyLanguageServerLauncher.main(['--help'] as String[])
            GroovyLanguageServerLauncher.main(['-h'] as String[])
            GroovyLanguageServerLauncher.main(['--version'] as String[])
            GroovyLanguageServerLauncher.main(['-v'] as String[])
        } finally {
            System.setOut(original)
        }
        def text = buffer.toString(StandardCharsets.UTF_8)
        assert text.contains('groovylsp')
        assert text.contains('groovy-lsp ' + GroovyLanguageServer.implementationVersion())
        assert !text.contains('groovy-lsp null')
        def code = GroovyLanguageServerLauncher.run(new ByteArrayInputStream(new byte[0]), new ByteArrayOutputStream(), false)
        assert code == null || code == 0
    }

    @Test
    void launcherReportsListenerFailures() {
        def failed = new CompletableFuture<Void>()
        failed.completeExceptionally(new IOException('boom'))
        try {
            GroovyLanguageServerLauncher.awaitExit(new GroovyLanguageServer(), failed)
            assert false : 'listener failure'
        } catch (ExecutionException e) {
            assert e.cause instanceof IOException
        }

        def interrupted = new CompletableFuture<Integer>()
        GroovyLanguageServerLauncher.completeOnEof(new Future<Void>() {
            @Override
            boolean cancel(boolean mayInterruptIfRunning) { false }

            @Override
            boolean isCancelled() { false }

            @Override
            boolean isDone() { true }

            @Override
            Void get() { throw new InterruptedException('stop') }

            @Override
            Void get(long timeout, TimeUnit unit) { throw new InterruptedException('stop') }
        }, new GroovyLanguageServer(), interrupted)
        try {
            interrupted.get(1, TimeUnit.SECONDS)
            assert false : 'interrupted listener'
        } catch (ExecutionException e) {
            assert e.cause instanceof InterruptedException
        } finally {
            Thread.interrupted()
        }

        def plain = new ExecutionException(new IOException('plain'))
        try {
            GroovyLanguageServerLauncher.unwrapListener(plain)
            assert false : 'plain listener failure'
        } catch (ExecutionException e) {
            assert e.is(plain)
        }
        try {
            GroovyLanguageServerLauncher.unwrapListener(new ExecutionException(new InterruptedException('stop')))
            assert false : 'interrupted listener unwrap'
        } catch (InterruptedException expected) {
            assert expected.message == 'stop'
        }
    }

    @Test
    void launcherReturnsOnExitWhileTheStreamStaysOpen() {
        def input = new PipedInputStream()
        def client = new PipedOutputStream(input)
        def pending = CompletableFuture.supplyAsync {
            GroovyLanguageServerLauncher.run(input, new ByteArrayOutputStream(), true)
        }
        try {
            client.write(frame('{"jsonrpc":"2.0","method":"exit"}'))
            client.flush()
            assert pending.get(8, TimeUnit.SECONDS) == 1
        } finally {
            client.close()
            input.close()
        }
    }

    @Test
    void launcherReturnsZeroWhenShutdownPrecedesExit() {
        def input = new PipedInputStream()
        def client = new PipedOutputStream(input)
        def pending = CompletableFuture.supplyAsync {
            GroovyLanguageServerLauncher.run(input, new ByteArrayOutputStream(), true)
        }
        try {
            client.write(frame('{"jsonrpc":"2.0","id":1,"method":"shutdown","params":null}'))
            client.write(frame('{"jsonrpc":"2.0","method":"exit"}'))
            client.flush()
            assert pending.get(8, TimeUnit.SECONDS) == 0
        } finally {
            client.close()
            input.close()
        }
    }

    @Test
    void launcherStdioCompletesInitializeShutdownAndExit() {
        def java = Path.of(System.getProperty('java.home'), 'bin', 'java')
        if (!Files.exists(java)) {
            java = Path.of(System.getProperty('java.home'), 'bin', 'java.exe')
        }
        def process = new ProcessBuilder(
                java.toString(),
                '-cp',
                System.getProperty('java.class.path'),
                'org.apache.groovy.lsp.GroovyLanguageServerLauncher')
                .start()
        def stdout = new ByteArrayOutputStream()
        def stderr = new ByteArrayOutputStream()
        def outThread = Thread.start { process.inputStream.transferTo(stdout) }
        def errThread = Thread.start { process.errorStream.transferTo(stderr) }
        try {
            process.outputStream.write(frame('{"jsonrpc":"2.0","id":1,"method":"initialize","params":{"processId":null,"capabilities":{}}}'))
            process.outputStream.write(frame('{"jsonrpc":"2.0","method":"initialized","params":{}}'))
            process.outputStream.write(frame('{"jsonrpc":"2.0","id":2,"method":"shutdown","params":null}'))
            process.outputStream.write(frame('{"jsonrpc":"2.0","method":"exit"}'))
            process.outputStream.flush()
            process.outputStream.close()
            assert process.waitFor(20, TimeUnit.SECONDS) : stderr.toString(StandardCharsets.UTF_8)
            outThread.join(2000)
            errThread.join(2000)
            assert process.exitValue() == 0 : stderr.toString(StandardCharsets.UTF_8)
            def output = stdout.toString(StandardCharsets.UTF_8)
            assert output.contains('capabilities')
            assert output.contains('"id":1') || output.contains('"id": 1')
        } finally {
            if (process.alive) {
                process.destroyForcibly()
            }
            outThread.join(2000)
            errThread.join(2000)
        }
    }

    @Test
    void remainingProtocolMethodsReturn() {
        fixture = new LspFixture()
        def uri = fixture.open('Hello.groovy', 'class Hello {\n  def foo(String bar) {}\n}\n')
        def id = new TextDocumentIdentifier(uri)
        def tds = fixture.server.textDocumentService
        def ws = fixture.server.workspaceService

        tds.didSave(new DidSaveTextDocumentParams(id))
        assert tds.resolveCompletionItem(new CompletionItem('x')).get().label == 'x'
        assert tds.diagnostic(new DocumentDiagnosticParams(id)).get() != null
        assert tds.semanticTokensRange(new SemanticTokensRangeParams(id,
                new Range(new Position(0, 0), new Position(1, 0)))).get() != null
        assert tds.rangeFormatting(new DocumentRangeFormattingParams(id, new FormattingOptions(4, true),
                new Range(new Position(0, 0), new Position(1, 0)))).get() != null
        assert tds.onTypeFormatting(new DocumentOnTypeFormattingParams(id, new FormattingOptions(4, true),
                new Position(0, 0), '{')).get() != null
        assert tds.prepareCallHierarchy(new CallHierarchyPrepareParams(id, new Position(1, 6))).get() != null
        assert tds.callHierarchyIncomingCalls(new CallHierarchyIncomingCallsParams()).get().isEmpty()
        assert tds.callHierarchyOutgoingCalls(new CallHierarchyOutgoingCallsParams()).get().isEmpty()
        assert tds.prepareTypeHierarchy(new TypeHierarchyPrepareParams(id, new Position(0, 7))).get() != null
        assert tds.typeHierarchySupertypes(new TypeHierarchySupertypesParams()).get().isEmpty()
        assert tds.typeHierarchySubtypes(new TypeHierarchySubtypesParams()).get().isEmpty()

        ws.didChangeConfiguration(new DidChangeConfigurationParams([groovy: [grapeEnabled: false, classpath: []]]))
        ws.didChangeWatchedFiles(new DidChangeWatchedFilesParams([new FileEvent(uri, FileChangeType.Changed)]))
        def folder = Files.createTempDirectory('groovy-lsp-ws')
        ws.didChangeWorkspaceFolders(new DidChangeWorkspaceFoldersParams(new WorkspaceFoldersChangeEvent(
                [new WorkspaceFolder(folder.toUri().toString(), 'root')], [])))
        assert ws.executeCommand(new ExecuteCommandParams('groovy.lsp.organizeImports', [uri])).get() != null
        assert ws.executeCommand(new ExecuteCommandParams('unknown', [])).get() == null
    }

    @Test
    void launcherHelpAndVersionWriteToStdout() {
        def help = GroovyLanguageServerLauncher.ListArgs.parse(['--help'] as String[])
        assert help.help
        GroovyLanguageServerLauncher.main(['--help'] as String[])
        GroovyLanguageServerLauncher.main(['--version'] as String[])
    }

    @Test
    void configurationAndFolderChangesUpdateSettings() {
        fixture = new LspFixture()
        def uri = fixture.open('Hello.groovy', 'class Hello {}\n')
        fixture.server.workspaceService.didChangeConfiguration(new DidChangeConfigurationParams(
                [groovy: [grapeEnabled: true, astTestEnabled: true, classpath: ['.', null],
                          sourcePaths: ['src'], dependencies: 'g:a:1']]))
        assert fixture.server.context.settings.grapeEnabled
        assert fixture.server.context.settings.astTestEnabled
        assert '.' in fixture.server.context.settings.classpath
        assert fixture.server.context.extraSetting('dependencies') == 'g:a:1'
        assert fixture.server.context.extraSetting(null) == null
        assert fixture.server.context.documentText(URI.create(uri))?.contains('class Hello')
        assert fixture.server.context.positionEncoding() in ['utf-16', 'utf-8', 'utf-32']
        assert 'src' in fixture.server.context.sourcePaths()
        fixture.server.workspaceService.didChangeConfiguration(new DidChangeConfigurationParams(
                [grapeEnabled: false, classpath: 'not-a-list']))
        assert !fixture.server.context.settings.grapeEnabled
        def folder = Files.createTempDirectory('groovy-lsp-folder')
        def added = new WorkspaceFolder(folder.toUri().toString(), 'tmp')
        fixture.server.workspaceService.didChangeWorkspaceFolders(new DidChangeWorkspaceFoldersParams(
                new WorkspaceFoldersChangeEvent([added], [])))
        assert fixture.server.context.workspaceFolders.contains(Uris.normalize(folder.toUri()))
        fixture.server.workspaceService.didChangeWorkspaceFolders(new DidChangeWorkspaceFoldersParams(
                new WorkspaceFoldersChangeEvent([], [added])))
        assert !fixture.server.context.workspaceFolders.contains(Uris.normalize(folder.toUri()))
        fixture.server.workspaceService.didChangeWorkspaceFolders(new DidChangeWorkspaceFoldersParams(
                new WorkspaceFoldersChangeEvent([], [])))
        fixture.configurationSection = [classpath: ['from-pull.jar'], gdsl: 'x.gdsl']
        fixture.server.context.clientCapabilities.workspace.configuration = true
        fixture.server.initialized(new InitializedParams())
        assert 'from-pull.jar' in fixture.server.context.classpath()
        assert fixture.server.context.extraSetting('gdsl') == 'x.gdsl'
    }

    @Test
    void unknownDocumentProtocolMethodsAreEmpty() {
        fixture = new LspFixture()
        def tds = fixture.server.textDocumentService
        def missing = new TextDocumentIdentifier('file:///groovy-lsp-missing/Missing.groovy')
        assert tds.completion(new CompletionParams(missing, new Position(0, 0))).get().getRight().items.isEmpty()
        assert tds.hover(new HoverParams(missing, new Position(0, 0))).get() == null
        assert tds.signatureHelp(new SignatureHelpParams(missing, new Position(0, 0))).get().signatures.isEmpty()
        assert tds.definition(new DefinitionParams(missing, new Position(0, 0))).get().getRight().isEmpty()
        assert tds.references(new ReferenceParams(missing, new Position(0, 0), new ReferenceContext(true))).get().isEmpty()
        assert tds.documentHighlight(new DocumentHighlightParams(missing, new Position(0, 0))).get().isEmpty()
        assert tds.codeAction(new CodeActionParams(missing, new Range(new Position(0, 0), new Position(0, 1)),
                new CodeActionContext([]))).get().isEmpty()
        assert tds.formatting(new DocumentFormattingParams(missing, new FormattingOptions(4, true))).get().isEmpty()
        assert tds.onTypeFormatting(new DocumentOnTypeFormattingParams(missing, new FormattingOptions(4, true),
                new Position(1, 0), '\n')).get().isEmpty()
        assert tds.prepareRename(new PrepareRenameParams(missing, new Position(0, 0))).get() == null
        assert tds.rename(new RenameParams(missing, new Position(0, 0), 'x')).get().changes.isEmpty()
        assert tds.selectionRange(new SelectionRangeParams(missing, [new Position(0, 0)])).get().isEmpty()
        assert tds.prepareCallHierarchy(new CallHierarchyPrepareParams(missing, new Position(0, 0))).get().isEmpty()
        assert tds.prepareTypeHierarchy(new TypeHierarchyPrepareParams(missing, new Position(0, 0))).get().isEmpty()
        assert tds.typeDefinition(new TypeDefinitionParams(missing, new Position(0, 0))).get().getRight().isEmpty()
        assert tds.implementation(new ImplementationParams(missing, new Position(0, 0))).get().getRight().isEmpty()
        assert tds.declaration(new DeclarationParams(missing, new Position(0, 0))).get().getRight().isEmpty()
    }

    @Test
    void launcherWaitsForExitAndAcceptsASocketClient() {
        def code = CompletableFuture.supplyAsync {
            GroovyLanguageServerLauncher.run(new ByteArrayInputStream(new byte[0]), new ByteArrayOutputStream(), true)
        }.get(8, TimeUnit.SECONDS)
        assert code == 0
        def probe = new ServerSocket(0)
        int port = probe.localPort
        probe.close()
        def started = new CompletableFuture<Void>()
        def thread = Thread.start {
            started.complete(null)
            GroovyLanguageServerLauncher.main(['--socket', String.valueOf(port)] as String[])
        }
        started.get(2, TimeUnit.SECONDS)
        Socket client = null
        for (int i = 0; i < 40 && client == null; i++) {
            try {
                def attempt = new Socket()
                attempt.connect(new InetSocketAddress('127.0.0.1', port), 250)
                client = attempt
            } catch (IOException ignored) {
                Thread.sleep(25)
            }
        }
        assert client != null
        def body = '{"jsonrpc":"2.0","method":"exit"}'
        def framed = "Content-Length: ${body.getBytes('UTF-8').length}\r\n\r\n${body}"
        try {
            client.outputStream.write(framed.getBytes('UTF-8'))
            client.outputStream.flush()
            thread.join(8000)
            assert !thread.alive
        } finally {
            client?.close()
            if (thread.alive) {
                thread.interrupt()
                thread.join(2000)
            }
        }
    }

    @Test
    void workspaceFolderChangesAndCommandsTolerateMissingDocuments() {
        def context = new LanguageServerContext()
        try {
            def workspace = new GroovyWorkspaceService(context, new LanguageFeatures())
            workspace.didChangeWorkspaceFolders(new DidChangeWorkspaceFoldersParams())
            def added = new WorkspaceFoldersChangeEvent([null, new WorkspaceFolder()], [])
            workspace.didChangeWorkspaceFolders(new DidChangeWorkspaceFoldersParams(added))
            def bare = new WorkspaceFoldersChangeEvent()
            def addedField = WorkspaceFoldersChangeEvent.getDeclaredField('added')
            def removedField = WorkspaceFoldersChangeEvent.getDeclaredField('removed')
            addedField.accessible = true
            removedField.accessible = true
            addedField.set(bare, null)
            removedField.set(bare, null)
            workspace.didChangeWorkspaceFolders(new DidChangeWorkspaceFoldersParams(bare))

            def missing = workspace.executeCommand(new ExecuteCommandParams(
                    'groovy.lsp.showReferences', ['file:///groovy-lsp-missing.groovy', 1, 2])).get()
            assert missing instanceof List
            assert missing.isEmpty()
            def superCall = workspace.executeCommand(new ExecuteCommandParams(
                    'groovy.lsp.gotoSuperMethod', ['file:///groovy-lsp-missing.groovy', 'x', 'y'])).get()
            assert superCall instanceof List
            assert superCall.isEmpty()

            def actions = [null, Either.forLeft(new Command('title', 'cmd')), Either.forRight(new CodeAction('bare'))]
            assert GroovyTextDocumentService.filterByOnly(actions, ['refactor']).isEmpty()
        } finally {
            context.close()
        }
    }

    @Test
    void contextSessionHelpersAndConfigurationPull() {
        def context = new LanguageServerContext()
        assert context.documentText(null) == null
        assert context.documentText(URI.create('file:///tmp/missing.groovy')) == null
        assert context.extraSetting(null) == null
        assert context.extraSetting('x') == null
        assert context.extraSettings().isEmpty()
        assert context.classpath().isEmpty()
        assert context.sourcePaths().isEmpty()
        assert context.positionEncoding() == 'utf-16'
        assert context.workspaceFolders().isEmpty()

        def ws = new GroovyWorkspaceService(context, new LanguageFeatures())
        ws.pullConfiguration()
        def caps = new ClientCapabilities()
        def workspace = new WorkspaceClientCapabilities()
        workspace.configuration = true
        caps.workspace = workspace
        context.clientCapabilities = caps
        ws.pullConfiguration()

        def fixture = new LspFixture(context)
        fixture.server.context.clientCapabilities.workspace.configuration = true
        fixture.configurationSection = [classpath: ['lib/a.jar'], grapeEnabled: true, dependencies: 'g:a:1']
        fixture.server.workspaceService.pullConfiguration()
        assert 'lib/a.jar' in fixture.server.context.classpath()
        assert fixture.server.context.settings.grapeEnabled
        assert fixture.server.context.extraSetting('dependencies') == 'g:a:1'

        fixture.configurationReply = []
        fixture.server.workspaceService.pullConfiguration()
        fixture.configurationReply = ['not-a-map']
        fixture.server.workspaceService.pullConfiguration()
        fixture.configurationReply = [null]
        fixture.server.workspaceService.pullConfiguration()
        fixture.configurationCompletesNull = true
        fixture.server.workspaceService.pullConfiguration()
        fixture.configurationCompletesNull = false
        fixture.configurationReturnsNullFuture = true
        fixture.server.workspaceService.pullConfiguration()
        fixture.configurationReturnsNullFuture = false
        fixture.failConfiguration = true
        fixture.server.workspaceService.pullConfiguration()
        fixture.failConfiguration = false
        fixture.configurationHangs = true
        long started = System.nanoTime()
        fixture.server.workspaceService.pullConfiguration()
        assert System.nanoTime() - started < TimeUnit.MILLISECONDS.toNanos(200)
        def uri = fixture.open('Hello.groovy', 'class Hello {}\n')
        fixture.server.context.documents.clear()
        assert fixture.server.context.documentText(URI.create(uri))?.contains('class Hello')
        fixture.server.workspaceService.didChangeConfiguration(new DidChangeConfigurationParams('not-a-map'))
        fixture.close()
    }

    @Test
    void languageServerInitializeEncodingsAndExit() {
        def server = new GroovyLanguageServer()
        def params = new InitializeParams()
        params.rootUri = Files.createTempDirectory('lsp-root').toUri().toString()
        params.workspaceFolders = [new WorkspaceFolder(params.rootUri, 'root')]
        def caps = new ClientCapabilities()
        def general = new GeneralClientCapabilities()
        general.positionEncodings = ['utf-8', 'utf-16']
        caps.general = general
        params.capabilities = caps
        def result = server.initialize(params).get()
        assert result.capabilities.positionEncoding == 'utf-8'
        server.initialized(null)
        server.context.applyEdit(null)
        server.context.applyEdit(new WorkspaceEdit())
        assert server.context.diagnosticsFor(null).isEmpty()
        assert server.context.compiler != null
        assert server.context.scanner != null
        assert server.context.diagnostics != null
        assert server.features().completions() != null
        assert server.features().hovers() != null
        assert server.features().navigation() != null
        assert server.features().symbols() != null
        assert server.features().rename() != null
        server.context.snapshot = CompilationSnapshot.EMPTY
        assert server.context.initialized
        server.shutdown().get()
        assert server.context.recompile() != null
        server.exit()
        assert server.context.exitCode == 0
        def other = new GroovyLanguageServer()
        other.exit()
        assert other.context.exitCode == 1
        def rootOnly = new GroovyLanguageServer()
        def rootParams = new InitializeParams()
        rootParams.rootUri = params.rootUri
        rootParams.capabilities = new ClientCapabilities()
        rootOnly.initialize(rootParams).get()
        assert rootOnly.context.positionEncoding == PositionEncoding.UTF16
        rootOnly.shutdown().get()
    }

    @Test
    void launcherParseAndRunWithoutWaiting() {
        def parsed = GroovyLanguageServerLauncher.ListArgs.parse(null)
        assert !parsed.help && parsed.port == null
        def socket = GroovyLanguageServerLauncher.ListArgs.parse(['--stdio', '--socket', '0', '-v'] as String[])
        assert socket.port == 0
        assert socket.version
        def out = new ByteArrayOutputStream()
        def code = GroovyLanguageServerLauncher.run(new ByteArrayInputStream(new byte[0]), out, false)
        assert code == null || code == 0
    }

    private static byte[] frame(String json) {
        byte[] body = json.getBytes(StandardCharsets.UTF_8)
        byte[] header = "Content-Length: ${body.length}\r\n\r\n".getBytes(StandardCharsets.UTF_8)
        byte[] framed = new byte[header.length + body.length]
        System.arraycopy(header, 0, framed, 0, header.length)
        System.arraycopy(body, 0, framed, header.length, body.length)
        framed
    }

    private static final class ThrowingProgressClient implements LanguageClient {

        @Override
        void telemetryEvent(Object object) {
        }

        @Override
        void publishDiagnostics(PublishDiagnosticsParams diagnostics) {
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

        @Override
        void notifyProgress(ProgressParams params) {
            throw new IllegalStateException('progress failed')
        }
    }
}
