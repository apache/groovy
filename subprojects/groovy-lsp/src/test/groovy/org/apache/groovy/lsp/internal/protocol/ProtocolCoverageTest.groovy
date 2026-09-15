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

import java.nio.file.Files

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
}
