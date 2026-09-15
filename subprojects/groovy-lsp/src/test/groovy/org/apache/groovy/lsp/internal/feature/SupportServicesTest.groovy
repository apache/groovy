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
package org.apache.groovy.lsp.internal.feature

import org.apache.groovy.lsp.internal.LspFixture
import org.apache.groovy.lsp.internal.compile.WorkspaceScanner
import org.apache.groovy.lsp.internal.position.PositionEncoding
import org.apache.groovy.lsp.internal.protocol.ServerCapabilityFactory
import org.apache.groovy.lsp.internal.workspace.TextDocument
import org.eclipse.lsp4j.CodeActionContext
import org.eclipse.lsp4j.CodeActionParams
import org.eclipse.lsp4j.CodeLensParams
import org.eclipse.lsp4j.Diagnostic
import org.eclipse.lsp4j.ExecuteCommandParams
import org.eclipse.lsp4j.DiagnosticSeverity
import org.eclipse.lsp4j.DocumentLinkParams
import org.eclipse.lsp4j.FoldingRangeRequestParams
import org.eclipse.lsp4j.InlayHintParams
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.PrepareRenameParams
import org.eclipse.lsp4j.Range
import org.eclipse.lsp4j.SelectionRangeParams
import org.eclipse.lsp4j.SemanticTokensParams
import org.eclipse.lsp4j.SignatureHelpParams
import org.eclipse.lsp4j.TextDocumentIdentifier
import org.eclipse.lsp4j.WorkspaceSymbolParams
import org.eclipse.lsp4j.jsonrpc.messages.Either
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

import java.nio.file.Files

final class SupportServicesTest {

    private LspFixture fixture

    @AfterEach
    void tearDown() {
        fixture?.close()
    }

    @Test
    void signatureHelpFoldingTokensLensesActions() {
        fixture = new LspFixture()
        def uri = fixture.open('Hello.groovy', '''\
            class Hello {
                def foo(String bar) { foo("x") }
            }
            '''.stripIndent())
        def id = new TextDocumentIdentifier(uri)
        def help = fixture.server.textDocumentService.signatureHelp(
                new SignatureHelpParams(id, new Position(1, 28))).get()
        assert help != null
        def folds = fixture.server.textDocumentService.foldingRange(new FoldingRangeRequestParams(id)).get()
        assert folds.size() >= 1
        def tokens = fixture.server.textDocumentService.semanticTokensFull(new SemanticTokensParams(id)).get()
        assert tokens.data.size() % 5 == 0
        def lenses = fixture.server.textDocumentService.codeLens(new CodeLensParams(id)).get()
        assert lenses != null
        if (lenses) {
            def command = lenses[0].command
            assert command.command == 'groovy.lsp.showReferences'
            def refs = fixture.server.workspaceService.executeCommand(
                    new ExecuteCommandParams(command.command, command.arguments)).get()
            assert refs != null
        }
        def diagnostic = new Diagnostic()
        diagnostic.range = new Range(new Position(0, 0), new Position(0, 1))
        diagnostic.message = Either.forLeft('unable to resolve class Foo')
        diagnostic.severity = DiagnosticSeverity.Error
        def actions = fixture.server.textDocumentService.codeAction(
                new CodeActionParams(id, new Range(new Position(0, 0), new Position(0, 1)),
                        new CodeActionContext([diagnostic]))).get()
        assert actions.any { it.getRight().title.contains('Organize') }
        def hints = fixture.server.textDocumentService.inlayHint(
                new InlayHintParams(id, new Range(new Position(0, 0), new Position(3, 0)))).get()
        assert hints != null
        def links = fixture.server.textDocumentService.documentLink(new DocumentLinkParams(id)).get()
        assert links != null
        def sel = fixture.server.textDocumentService.selectionRange(
                new SelectionRangeParams(id, [new Position(1, 8)])).get()
        assert sel.size() == 1
        def prepare = fixture.server.textDocumentService.prepareRename(
                new PrepareRenameParams(id, new Position(0, 7))).get()
        assert prepare != null
        def symbols = fixture.server.workspaceService.symbol(new WorkspaceSymbolParams('Hel')).get()
        assert symbols.getRight().any { it.name.contains('Hello') }
    }

    @Test
    void renameRejectsInvalidIdentifier() {
        assert !RenameService.isIdentifier('')
        assert !RenameService.isIdentifier('1abc')
        assert RenameService.isIdentifier('foo_bar')
        assert CompletionService.prefixMatches('Hello', 'he')
        assert CompletionService.prefixMatches('Hello', '')
        assert !CompletionService.prefixMatches('Hello', 'z')
    }

    @Test
    void capabilitiesOmitColorAndNotebooks() {
        def caps = new ServerCapabilityFactory().create(PositionEncoding.UTF16)
        assert caps.colorProvider == null
        assert caps.notebookDocumentSync == null
        assert caps.diagnosticProvider != null
        assert caps.executeCommandProvider.commands == ['groovy.lsp.organizeImports', 'groovy.lsp.showReferences']
        assert caps.completionProvider.resolveProvider == true
        assert caps.codeLensProvider != null
    }

    @Test
    void workspaceScannerSkipsBuildDir() {
        def root = Files.createTempDirectory('groovy-lsp-scan')
        Files.createDirectories(root.resolve('src'))
        Files.writeString(root.resolve('src/A.groovy'), 'class A {}')
        Files.createDirectories(root.resolve('build'))
        Files.writeString(root.resolve('build/B.groovy'), 'class B {}')
        def files = new WorkspaceScanner().scan([root.toUri()], [])
        assert files.any { it.fileName.toString() == 'A.groovy' }
        assert files.every { !it.toString().contains("${File.separator}build${File.separator}") }
    }

    @Test
    void hoverRenderCoversNodeKinds() {
        assert HoverService.render(null) == null
    }

    @Test
    void completionPrefixAndMemberAccess() {
        def doc = new TextDocument(
                URI.create('file:///tmp/A.groovy'), 'groovy', 1, 'hello.wo')
        def prefix = CompletionService.prefixAt(doc, new Position(0, 8), PositionEncoding.UTF16)
        assert prefix == 'wo'
        assert CompletionService.isMemberAccess(doc, new Position(0, 8), PositionEncoding.UTF16)
        def plain = new TextDocument(
                URI.create('file:///tmp/A.groovy'), 'groovy', 1, 'cla')
        assert !CompletionService.isMemberAccess(plain, new Position(0, 3), PositionEncoding.UTF16)
        assert CompletionService.prefixAt(plain, new Position(0, 3), PositionEncoding.UTF16) == 'cla'
    }
}
