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
package org.apache.groovy.lsp.internal.protocol

import org.apache.groovy.lsp.GroovyLanguageServerLauncher
import org.apache.groovy.lsp.internal.LspFixture
import org.apache.groovy.lsp.internal.util.Uris
import org.eclipse.lsp4j.CodeActionContext
import org.eclipse.lsp4j.CodeActionParams
import org.eclipse.lsp4j.CompletionParams
import org.eclipse.lsp4j.DeclarationParams
import org.eclipse.lsp4j.DefinitionParams
import org.eclipse.lsp4j.DocumentFormattingParams
import org.eclipse.lsp4j.DocumentHighlightParams
import org.eclipse.lsp4j.HoverParams
import org.eclipse.lsp4j.ImplementationParams
import org.eclipse.lsp4j.PrepareRenameParams
import org.eclipse.lsp4j.ReferenceContext
import org.eclipse.lsp4j.ReferenceParams
import org.eclipse.lsp4j.RenameParams
import org.eclipse.lsp4j.SelectionRangeParams
import org.eclipse.lsp4j.SignatureHelpParams
import org.eclipse.lsp4j.TypeDefinitionParams
import org.eclipse.lsp4j.CallHierarchyIncomingCallsParams
import org.eclipse.lsp4j.CallHierarchyOutgoingCallsParams
import org.eclipse.lsp4j.CallHierarchyPrepareParams
import org.eclipse.lsp4j.CompletionItem
import org.eclipse.lsp4j.DidChangeConfigurationParams
import org.eclipse.lsp4j.DidChangeWatchedFilesParams
import org.eclipse.lsp4j.DidChangeWorkspaceFoldersParams
import org.eclipse.lsp4j.DidSaveTextDocumentParams
import org.eclipse.lsp4j.DocumentDiagnosticParams
import org.eclipse.lsp4j.DocumentOnTypeFormattingParams
import org.eclipse.lsp4j.DocumentRangeFormattingParams
import org.eclipse.lsp4j.ExecuteCommandParams
import org.eclipse.lsp4j.FileChangeType
import org.eclipse.lsp4j.FileEvent
import org.eclipse.lsp4j.FormattingOptions
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.Range
import org.eclipse.lsp4j.SemanticTokensRangeParams
import org.eclipse.lsp4j.TextDocumentIdentifier
import org.eclipse.lsp4j.TypeHierarchyPrepareParams
import org.eclipse.lsp4j.TypeHierarchySubtypesParams
import org.eclipse.lsp4j.TypeHierarchySupertypesParams
import org.eclipse.lsp4j.WorkspaceFolder
import org.eclipse.lsp4j.WorkspaceFoldersChangeEvent
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.nio.file.Files
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

final class ProtocolCoverageTest {

    private LspFixture fixture

    @AfterEach
    void tearDown() {
        fixture?.close()
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
        fixture.server.workspaceService.didChangeConfiguration(new DidChangeConfigurationParams(
                [groovy: [grapeEnabled: true, astTestEnabled: true, classpath: ['.', null], sourcePaths: ['src']]]))
        assert fixture.server.context.settings.grapeEnabled
        assert fixture.server.context.settings.astTestEnabled
        assert '.' in fixture.server.context.settings.classpath
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
        client.outputStream.write(framed.getBytes('UTF-8'))
        client.outputStream.flush()
        client.close()
        thread.join(8000)
        if (thread.alive) {
            thread.interrupt()
            thread.join(2000)
        }
    }
}
