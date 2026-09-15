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
import org.apache.groovy.lsp.internal.compile.CompiledDocument
import org.apache.groovy.lsp.internal.compile.WorkspaceScanner
import org.apache.groovy.lsp.internal.position.PositionEncoding
import org.apache.groovy.lsp.internal.protocol.ServerCapabilityFactory
import org.apache.groovy.lsp.internal.workspace.TextDocument
import org.eclipse.lsp4j.CodeActionContext
import org.eclipse.lsp4j.CodeActionParams
import org.eclipse.lsp4j.CodeLensParams
import org.eclipse.lsp4j.Diagnostic
import org.eclipse.lsp4j.DocumentOnTypeFormattingParams
import org.eclipse.lsp4j.ExecuteCommandParams
import org.eclipse.lsp4j.DiagnosticSeverity
import org.eclipse.lsp4j.DocumentLinkParams
import org.eclipse.lsp4j.FoldingRangeRequestParams
import org.eclipse.lsp4j.FormattingOptions
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
        assert caps.executeCommandProvider.commands == [
                'groovy.lsp.organizeImports', 'groovy.lsp.showReferences', 'groovy.lsp.gotoSuperMethod']
        assert caps.completionProvider.resolveProvider == true
        assert caps.codeLensProvider != null
        assert 'source.fixAll' in caps.codeActionProvider.getRight().codeActionKinds
        assert 'refactor.rewrite' in caps.codeActionProvider.getRight().codeActionKinds
        assert caps.documentOnTypeFormattingProvider.moreTriggerCharacter == ['}']
        assert caps.workspace.fileOperations.willRename != null
    }

    @Test
    void classFoldsAreNotRegionsAndCommentRegionsAre() {
        fixture = new LspFixture()
        def uri = fixture.open('Hello.groovy', '''\
            class Hello {
                def foo() { 1 }
            }
            // region
            def x = 1
            // endregion
            '''.stripIndent())
        def folds = fixture.server.textDocumentService.foldingRange(
                new FoldingRangeRequestParams(new TextDocumentIdentifier(uri))).get()
        assert folds.any { it.kind == 'region' }
        assert folds.findAll { it.kind == 'region' }.size() == 1
        assert folds.any { it.kind == null || it.kind == '' }
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
        def support = new SupportServices()
        assert support.folding(null).isEmpty()
        assert support.inlayHints(null, null, PositionEncoding.UTF16).isEmpty()
        assert support.documentLinks(null, null, PositionEncoding.UTF16).isEmpty()
        assert support.codeLenses(null, null, PositionEncoding.UTF16).isEmpty()
        assert support.semanticTokens(null, PositionEncoding.UTF16).data.isEmpty()
        def emptyDoc = new TextDocument(URI.create('file:///tmp/X.groovy'), 'groovy', 1, '')
        assert support.onTypeFormat(emptyDoc, new Position(0, 0), '}', 4, true).isEmpty()
    }

    @Test
    void semanticTokensEmitLexerStringNumberCommentWithoutAst() {
        def src = 'def n = 1 // hi\nString s = "x"\n'
        def compiled = new CompiledDocument(URI.create('file:///tmp/T.groovy'), 1, src, null, null, null)
        def data = new SupportServices().semanticTokens(compiled, PositionEncoding.UTF16).data
        assert data.size() % 5 == 0
        def decoded = decodeTokens(data)
        assert decoded.any { it.type == SupportServices.TOKEN_TYPES.indexOf('keyword') }
        assert decoded.any { it.type == SupportServices.TOKEN_TYPES.indexOf('number') }
        assert decoded.any { it.type == SupportServices.TOKEN_TYPES.indexOf('comment') }
        assert decoded.any { it.type == SupportServices.TOKEN_TYPES.indexOf('string') }
    }

    @Test
    void semanticTokensSplitMultilineComment() {
        def src = '/*\n block\n*/\nclass C {}\n'
        def compiled = new CompiledDocument(URI.create('file:///tmp/T.groovy'), 1, src, null, null, null)
        def decoded = decodeTokens(new SupportServices().semanticTokens(compiled, PositionEncoding.UTF16).data)
        def comments = decoded.findAll { it.type == SupportServices.TOKEN_TYPES.indexOf('comment') }
        assert comments.size() >= 2
        assert comments*.line.toSet().size() >= 2
    }

    @Test
    void rangeFormatEditsOnlySelectedLines() {
        def src = 'class C {\ndef x\n}\n'
        def document = new TextDocument(URI.create('file:///tmp/C.groovy'), 'groovy', 1, src)
        def edits = new SupportServices().format(document, new Range(new Position(1, 0), new Position(1, 5)),
                4, true, PositionEncoding.UTF16)
        assert edits.size() == 1
        assert edits[0].range.start.line == 1
        assert edits[0].range.end.line == 1
        assert edits[0].newText.contains('def x')
        assert !edits[0].newText.contains('class C')
    }

    @Test
    void semanticTokensDoNotPaintGStringInterpolationAsString() {
        def src = 'def s = "hello ${foo} world"\n'
        def compiled = new CompiledDocument(URI.create('file:///tmp/G.groovy'), 1, src, null, null, null)
        def decoded = decodeTokens(new SupportServices().semanticTokens(compiled, PositionEncoding.UTF16).data)
        def strings = decoded.findAll { it.type == SupportServices.TOKEN_TYPES.indexOf('string') }
        assert strings.size() >= 2
        int stringChars = strings.sum { it.length } as int
        assert stringChars < src.length()
    }

    @Test
    void formatDoesNotReindentInsideMultilineString() {
        def src = 'def s = """\n  keep\n"""\n'
        def document = new TextDocument(URI.create('file:///tmp/S.groovy'), 'groovy', 1, src)
        def edits = new SupportServices().format(document, null, 4, true, PositionEncoding.UTF16)
        def result = edits ? edits[0].newText : src
        assert result.contains('  keep')
    }

    private static List<Map> decodeTokens(List<Integer> data) {
        int line = 0
        int col = 0
        def out = []
        for (int i = 0; i < data.size(); i += 5) {
            line += data[i]
            col = data[i] == 0 ? col + data[i + 1] : data[i + 1]
            out << [line: line, start: col, length: data[i + 2], type: data[i + 3], mod: data[i + 4]]
        }
        out
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
        assert CompletionService.prefixAt(null, new Position(0, 0), PositionEncoding.UTF16) == ''
        assert !CompletionService.isMemberAccess(null, new Position(0, 0), PositionEncoding.UTF16)
        assert new SupportServices().highlights(null, null, new Position(0, 0), PositionEncoding.UTF16).isEmpty()
    }

    @Test
    void commentFoldOnTypeTabsAndOrganizeImportsApply() {
        fixture = new LspFixture()
        def uri = fixture.open('Hello.groovy', '''\
            import java.util.concurrent.ConcurrentHashMap
            import java.util.ArrayList
            class Hello {
            /*
             * docs
             */
                ArrayList x
            }'''.stripIndent())
        def folds = fixture.server.textDocumentService.foldingRange(
                new FoldingRangeRequestParams(new TextDocumentIdentifier(uri))).get()
        assert folds.any { it.kind == 'comment' }
        def edit = fixture.server.workspaceService.executeCommand(
                new ExecuteCommandParams('groovy.lsp.organizeImports', [uri])).get()
        assert edit.changes[uri]
        assert fixture.appliedEdits
        def tabbed = fixture.open('Tabs.groovy', 'class Tabs {\n\n}\n')
        def edits = fixture.server.textDocumentService.onTypeFormatting(
                new DocumentOnTypeFormattingParams(new TextDocumentIdentifier(tabbed),
                        new FormattingOptions(4, false), new Position(1, 0), '\n')).get()
        assert edits[0].newText.contains('\t') || edits[0].newText.isEmpty()
    }
}
