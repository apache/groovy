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

import java.nio.file.Path
import org.apache.groovy.lsp.internal.compile.AstQuery
import org.apache.groovy.lsp.internal.compile.CompilationSnapshot
import org.apache.groovy.lsp.internal.compile.CompilerSettings
import org.apache.groovy.lsp.internal.compile.GroovyCompiler
import org.apache.groovy.lsp.internal.util.Uris
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.CompileUnit
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.ModuleNode
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.PropertyExpression
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression
import org.codehaus.groovy.ast.expr.TupleExpression
import org.codehaus.groovy.control.SourceUnit
import org.eclipse.lsp4j.FoldingRangeKind
import static org.objectweb.asm.Opcodes.ACC_PUBLIC

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
    private static final PositionEncoding UTF16 = PositionEncoding.UTF16

    private static final PositionEncoding ENC = PositionEncoding.UTF16


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
        fixture.close()
        fixture = new LspFixture()
        fixture.open('AbstractServerFactory.groovy', 'class AbstractServerFactory {}\n')
        def humps = fixture.server.workspaceService.symbol(new WorkspaceSymbolParams('ASF')).get()
        assert humps.getRight().any { it.name == 'AbstractServerFactory' }
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
        assert new FoldingService().folding(null).isEmpty()
        assert new InlayHintService().inlayHints(null, null, PositionEncoding.UTF16).isEmpty()
        assert new DocumentLinkService().documentLinks(null, null, PositionEncoding.UTF16).isEmpty()
        assert new CodeLensService().codeLenses(null, null, PositionEncoding.UTF16).isEmpty()
        assert new SemanticTokensService().semanticTokens(null, PositionEncoding.UTF16).data.isEmpty()
        def emptyDoc = new TextDocument(URI.create('file:///tmp/X.groovy'), 'groovy', 1, '')
        assert new FormattingService().onTypeFormat(emptyDoc, new Position(0, 0), '}', 4, true).isEmpty()
    }

    @Test
    void semanticTokensEmitLexerStringNumberCommentWithoutAst() {
        def src = 'def n = 1 // hi\nString s = "x"\n'
        def compiled = new CompiledDocument(URI.create('file:///tmp/T.groovy'), 1, src, null, null, null)
        def data = new SemanticTokensService().semanticTokens(compiled, PositionEncoding.UTF16).data
        assert data.size() % 5 == 0
        def decoded = decodeTokens(data)
        assert decoded.any { it.type == SemanticTokensService.TOKEN_TYPES.indexOf('keyword') }
        assert decoded.any { it.type == SemanticTokensService.TOKEN_TYPES.indexOf('number') }
        assert decoded.any { it.type == SemanticTokensService.TOKEN_TYPES.indexOf('comment') }
        assert decoded.any { it.type == SemanticTokensService.TOKEN_TYPES.indexOf('string') }
    }

    @Test
    void semanticTokensSplitMultilineComment() {
        def src = '/*\n block\n*/\nclass C {}\n'
        def compiled = new CompiledDocument(URI.create('file:///tmp/T.groovy'), 1, src, null, null, null)
        def decoded = decodeTokens(new SemanticTokensService().semanticTokens(compiled, PositionEncoding.UTF16).data)
        def comments = decoded.findAll { it.type == SemanticTokensService.TOKEN_TYPES.indexOf('comment') }
        assert comments.size() >= 2
        assert comments*.line.toSet().size() >= 2
    }

    @Test
    void rangeFormatEditsOnlySelectedLines() {
        def src = 'class C {\ndef x\n}\n'
        def document = new TextDocument(URI.create('file:///tmp/C.groovy'), 'groovy', 1, src)
        def edits = new FormattingService().format(document, new Range(new Position(1, 0), new Position(1, 5)),
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
        def decoded = decodeTokens(new SemanticTokensService().semanticTokens(compiled, PositionEncoding.UTF16).data)
        def strings = decoded.findAll { it.type == SemanticTokensService.TOKEN_TYPES.indexOf('string') }
        assert strings.size() >= 2
        int stringChars = strings.sum { it.length } as int
        assert stringChars < src.length()
    }

    @Test
    void formatDoesNotReindentInsideMultilineString() {
        def src = 'def s = """\n  keep\n"""\n'
        def document = new TextDocument(URI.create('file:///tmp/S.groovy'), 'groovy', 1, src)
        def edits = new FormattingService().format(document, null, 4, true, PositionEncoding.UTF16)
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
        assert new HighlightService().highlights(null, null, new Position(0, 0), PositionEncoding.UTF16).isEmpty()
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
    @Test
    void semanticTokensCoverKindsModifiersAndRanges() {
        def uri = fileUri('file:///tmp/cov-tokens.groovy')
        def src = '''\
            package demo
            import java.util.List
            @java.lang.SuppressWarnings('unused')
            interface Face {
                void run()
            }
            @java.lang.Deprecated
            enum Hue {
                RED
            }
            class Hello {
                @java.lang.Deprecated
                static String label
                def foo(String item) {
                    this.label
                    go('x')
                }
                static void go(String item) {}
            }
            '''.stripIndent()
        def snap = compileOf(uri, src)
        def module = snap.get(uri).module
        def face = module.classes.find { it.nameWithoutPackage == 'Face' }
        def hue = module.classes.find { it.nameWithoutPackage == 'Hue' }
        assert face.annotations.any { it.classNode?.name == 'java.lang.SuppressWarnings' }
        assert hue.annotations.any { it.classNode?.name == 'java.lang.Deprecated' }
        assert walked(module, StaticMethodCallExpression) { it.method == 'go' }
        assert walked(module, PropertyExpression) { it.propertyAsString == 'label' }
        def hello = module.classes.find { it.nameWithoutPackage == 'Hello' }
        hello.addSyntheticMethod('hidden', ACC_PUBLIC, ClassHelper.OBJECT_TYPE,
                Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, null)

        def service = new SemanticTokensService()
        def full = decode(service.semanticTokens(snap.get(uri), UTF16).data)
        assert full.any { it.type == token('interface') }
        assert full.any { it.type == token('enum') && (it.mod & 4) != 0 }
        assert full.any { it.type == token('property') && (it.mod & 2) != 0 }
        assert full.any { it.type == token('method') }
        def ranged = decode(service.semanticTokens(snap.get(uri), UTF16,
                new Range(new Position(0, 0), new Position(0, 4))).data)
        assert ranged
        assert ranged.every { it.line == 0 }
        assert full.any { it.line > 0 }

        def bare = new NullAnnotations()
        bare.lineNumber = 1
        bare.columnNumber = 7
        bare.lastLineNumber = 1
        bare.lastColumnNumber = 12
        def bareModule = new ModuleNode((SourceUnit) null)
        bareModule.addClass(bare)
        def bareDoc = new CompiledDocument(fileUri('file:///tmp/cov-bare-token.groovy'), 1, 'class Hello {}',
                bareModule, null, null)
        def bareTokens = decode(service.semanticTokens(bareDoc, UTF16).data)
        assert bareTokens.any { it.type == token('class') && it.mod == 1 }

        def one = new CompiledDocument(fileUri('file:///tmp/cov-one.groovy'), 1, 'def', null, null, null)
        assert service.semanticTokens(one, UTF16).data.size() == 5
        def empty = new CompiledDocument(fileUri('file:///tmp/cov-empty-text.groovy'), 1, '', null, null, null)
        assert service.semanticTokens(empty, UTF16).data.isEmpty()
    }

    private CompilationSnapshot compileOf(URI uri, String text) {
        compileOf([new TextDocument(uri, 'groovy', 1, text)])
    }

    private CompilationSnapshot compileOf(List<TextDocument> documents) {
        def compiler = new GroovyCompiler()
        try {
            return compiler.compile(documents, [], CompilerSettings.defaults(), getClass().classLoader)
        } finally {
            compiler.close()
        }
    }

    private static URI fileUri(String path) {
        Uris.normalize(URI.create(path))
    }

    private static URI fileUri(Path path) {
        Uris.normalize(path.toUri())
    }

    private static boolean walked(module, Class type, Closure predicate) {
        boolean[] found = [false]
        AstQuery.walk(module) { node, ctx ->
            if (type.isInstance(node) && predicate(node)) {
                found[0] = true
            }
        }
        found[0]
    }

    private static int token(String name) {
        SemanticTokensService.TOKEN_TYPES.indexOf(name)
    }

    private static List<Map> decode(List<Integer> data) {
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

    private static final class NullAnnotations extends ClassNode {
        private boolean reported

        NullAnnotations() {
            super('Hello', ACC_PUBLIC, ClassHelper.OBJECT_TYPE)
        }

        @Override
        List<AnnotationNode> getAnnotations() {
            if (!reported) {
                reported = true
                return null
            }
            Collections.emptyList()
        }
    }

    @Test
    void signatureHelpActiveParameterAndEmptyOverloads() {
        def src = '''\
            class Sig {
                void zero() {}
                void two(int a, int b) {}
                void show() {
                    missing(1)
                    zero( )
                    two(1, 2 )
                }
            }
            '''.stripIndent()
        def uri = open('Sig.groovy', src)
        def doc = compiled(uri)
        def buffer = doc.toTextDocument()
        def service = new SignatureHelpService()
        def orphan = new TextDocument(URI.create('file:///missing.groovy'), 'groovy', 1, doc.text)
        assert service.signatureHelp(orphan, snapshot(), at(doc.text, 'class'), ENC).signatures.isEmpty()
        assert service.signatureHelp(buffer, snapshot(), at(doc.text, 'missing('), ENC).signatures.isEmpty()
        assert service.signatureHelp(buffer, snapshot(), at(doc.text, 'class'), ENC).signatures.isEmpty()

        def zeroPos = at(doc.text, 'zero( )')
        zeroPos.character += 'zero('.length()
        def zeroHelp = service.signatureHelp(buffer, snapshot(), zeroPos, ENC)
        assert zeroHelp.signatures*.label.any { it.contains('zero()') }
        assert zeroHelp.activeParameter == 0

        def twoCall = callsNamed(typeNamed(doc, 'Sig').methods.find { it.name == 'show' }, 'two')[0]
        def args = twoCall.arguments
        assert args instanceof TupleExpression
        def first = args.expressions[0]
        int savedLine = first.lineNumber
        def mid = new Position(first.lastLineNumber - 1, first.lastColumnNumber)
        assert AstQuery.enclosingCall(doc.module, buffer, mid, ENC).is(twoCall)
        assert service.signatureHelp(buffer, snapshot(), mid, ENC).activeParameter == 1
        first.lineNumber = -1
        assert service.signatureHelp(buffer, snapshot(), mid, ENC).activeParameter == 0
        first.lineNumber = savedLine

        def last = args.expressions[-1]
        def afterLast = new Position(last.lastLineNumber - 1, last.lastColumnNumber)
        assert AstQuery.enclosingCall(doc.module, buffer, afterLast, ENC).is(twoCall)
        def afterHelp = service.signatureHelp(buffer, snapshot(), afterLast, ENC)
        assert afterHelp.activeParameter == 1
        assert afterHelp.signatures[afterHelp.activeSignature].label.contains('int a, int b')
    }

    @Test
    void formattingRangeNewlineAndOutdentGuards() {
        def formatting = new FormattingService()
        def ready = 'class C {\n    def ready\n}\n'
        def readyDoc = new TextDocument(URI.create('file:///C.groovy'), 'groovy', 1, ready)
        assert formatting.format(readyDoc, new Range(new Position(5, 0), new Position(1, 0)), 4, true, ENC).isEmpty()
        assert formatting.format(readyDoc, new Range(new Position(1, 0), new Position(1, 12)), 4, true, ENC).isEmpty()

        def bare = new TextDocument(URI.create('file:///C.groovy'), 'groovy', 1, 'class C {\n}')
        def full = formatting.format(bare, null, 4, true, ENC)
        assert full.size() == 1
        assert full[0].newText.endsWith('\n')
        assert full[0].newText.contains('class C')

        def ended = new TextDocument(URI.create('file:///C.groovy'), 'groovy', 1, 'class C {\n}')
        assert formatting.onTypeFormat(ended, new Position(5, 0), '}', 4, true, ENC).isEmpty()
        def identifier = new TextDocument(URI.create('file:///C.groovy'), 'groovy', 1, 'class C {\n    x}\n')
        int brace = identifier.text.readLines()[1].indexOf('}') + 1
        assert formatting.onTypeFormat(identifier, new Position(1, brace), '}', 4, true, ENC).isEmpty()
        assert formatting.onTypeFormat(null, new Position(0, 0), '\n', 4, true, ENC).isEmpty()
        assert formatting.onTypeFormat(readyDoc, new Position(0, 0), null, 4, true, ENC).isEmpty()
        assert formatting.onTypeFormat(readyDoc, new Position(-1, 0), '\n', 4, true, ENC).isEmpty()
    }

    @Test
    void foldingRegionsImportsAndEmptyText() {
        def src = '''\
            import java.util.List
            import java.util.Map
            // #region basket
            class Folded {
                def foo() { 1 }
            }
            // #endregion
            // plain note
            '''.stripIndent()
        def uri = open('Folded.groovy', src)
        def doc = compiled(uri)
        doc.module.addStarImport('ghost.')
        def ghost = doc.module.starImports[-1]
        ghost.lineNumber = -1
        def folds = new FoldingService().folding(doc)
        def regions = folds.findAll { it.kind == FoldingRangeKind.Region }
        assert regions.size() == 1
        assert regions[0].startLine == doc.text.readLines().findIndexOf { it.contains('#region') }
        def imports = folds.findAll { it.kind == FoldingRangeKind.Imports }
        assert imports.size() == 1
        assert imports[0].startLine == 0

        def emptyModule = new ModuleNode((CompileUnit) null)
        def empty = new CompiledDocument(URI.create('file:///empty.groovy'), 1, '', emptyModule, null, null)
        assert new FoldingService().folding(empty).isEmpty()
    }

    private String open(String path, String text) {
        if (fixture == null) {
            fixture = new LspFixture()
        }
        fixture.open(path, text)
    }

    private CompilationSnapshot snapshot() {
        fixture.server.context.snapshot
    }

    private CompiledDocument compiled(String uri) {
        def doc = snapshot().get(uri)
        assert doc?.module != null
        doc
    }

    private static ClassNode typeNamed(CompiledDocument doc, String simple) {
        def found = doc.module.classes.find { it.nameWithoutPackage == simple && !it.name.contains('$') }
        assert found != null : doc.module.classes*.name
        found
    }

    private static List<MethodCallExpression> callsNamed(MethodNode method, String name) {
        def found = []
        AstQuery.walk(method.code) { node, ctx ->
            if (node instanceof MethodCallExpression && node.methodAsString == name) {
                found << node
            }
        }
        assert found : name
        found
    }

    private static Position at(String text, String token) {
        int index = text.indexOf(token)
        assert index >= 0 : token
        int line = 0
        int column = 0
        for (int i = 0; i < index; i++) {
            if (text.charAt(i) == '\n') {
                line += 1
                column = 0
            } else {
                column += 1
            }
        }
        new Position(line, column)
    }

    @Test
    void signatureInlaySemanticFormatAndLinks() {
        fixture = new LspFixture()
        def text = '''\
            package demo
            import java.util.Map
            import static java.lang.Math.PI
            import java.util.List
            import java.util.*
            @Deprecated
            class Box {
                @Deprecated
                static String CONST = 'x'
                String name
                static void ping(String name, int count) {}
                Box(String name, int count) { this.name = name }
                void use(def label) {
                    def item = 'z'
                    def name = 'a'
                    ping(name, 1)
                    ping(name: 'a', count: 1)
                    Box.ping('b', 2)
                    new Box('a', 1)
                    ping('a', 1)
                    List
                    PI
                    CONST.length()
                }
            }
            interface Named { String name() }
            enum Hue { RED }
            '''.stripIndent()
        def uri = fixture.open('Box.groovy', text)
        def snapshot = fixture.server.context.snapshot
        def compiled = snapshot.get(uri)
        def document = compiled.toTextDocument()
        def help = new SignatureHelpService()
        assert help.signatureHelp(null, snapshot, new Position(0, 0), UTF16).signatures.isEmpty()
        def outside = help.signatureHelp(document, snapshot, caret(text, 'class'), UTF16)
        assert outside.signatures.isEmpty()
        def staticHelp = help.signatureHelp(document, snapshot, after(text, 'Box.ping('), UTF16)
        assert staticHelp.signatures.any { it.label.contains('ping') }
        def ctorHelp = help.signatureHelp(document, snapshot, after(text, 'new Box('), UTF16)
        assert ctorHelp.signatures.any { it.label.contains('<init>') }
        def tooMany = help.signatureHelp(document, snapshot, after(text, "ping('a', 1"), UTF16)
        assert tooMany.signatures.any { it.label.startsWith('ping') }
        assert tooMany.activeParameter >= 0

        def inlays = new InlayHintService()
        assert inlays.inlayHints(null, null, UTF16).isEmpty()
        def hints = inlays.inlayHints(compiled, null, UTF16, snapshot)
        assert hints.any { it.kind.toString().contains('Parameter') || (it.label.isLeft() && it.label.getLeft().contains(':')) }
        def skipped = inlays.inlayHints(compiled, new Range(new Position(200, 0), new Position(200, 1)), UTF16, snapshot)
        assert skipped.size() <= hints.size()

        def tokens = new SemanticTokensService().semanticTokens(compiled, UTF16)
        def kinds = tokenTypes(tokens.data)
        assert SemanticTokensService.TOKEN_TYPES.indexOf('interface') in kinds
        assert SemanticTokensService.TOKEN_TYPES.indexOf('enum') in kinds
        assert SemanticTokensService.TOKEN_TYPES.indexOf('method') in kinds
        assert tokenModifiers(tokens.data).any { (it & 2) != 0 }
        assert tokenModifiers(tokens.data).any { (it & 4) != 0 }
        def ranged = new SemanticTokensService().semanticTokens(compiled, UTF16,
                new Range(new Position(0, 0), new Position(0, 1)))
        assert ranged.data != null

        def formatting = new FormattingService()
        def messy = new TextDocument(URI.create('file:///T.groovy'), 'groovy', 1, 'class T {\n    def a=1\n}')
        def formatted = formatting.format(messy, null, 4, true, UTF16)
        assert formatted[0].newText.endsWith('\n')
        def tidy = new TextDocument(messy.uri, 'groovy', 2, formatted[0].newText)
        assert formatting.format(tidy, null, 4, true, UTF16).isEmpty()
        assert formatting.format(messy, new Range(new Position(5, 0), new Position(1, 0)), 4, true, UTF16).isEmpty()
        assert formatting.onTypeFormat(null, new Position(0, 0), '\n', 4, true).isEmpty()
        assert formatting.onTypeFormat(messy, new Position(0, 0), null, 4, true).isEmpty()
        assert formatting.onTypeFormat(messy, new Position(-1, 0), '\n', 4, true).isEmpty()
        assert formatting.onTypeFormat(messy, new Position(0, 0), 'x', 4, true).isEmpty()
        def tabbed = new TextDocument(URI.create('file:///Tab.groovy'), 'groovy', 1, 'class T {\n\tdef a = 1\n}\n')
        assert formatting.format(tabbed, null, 4, false, UTF16) != null
        def closer = new TextDocument(URI.create('file:///C.groovy'), 'groovy', 1, 'class T {\n}')
        assert formatting.onTypeFormat(closer, new Position(20, 1), '}', 4, true).isEmpty()
        def frozen = new TextDocument(URI.create('file:///F.groovy'), 'groovy', 1, "class F {\n    def s = '''\n        keep\n'''\n}\n")
        def frozenEdits = formatting.format(frozen, null, 4, true, UTF16)
        if (frozenEdits) {
            assert frozenEdits[0].newText.contains('keep')
        }

        def folds = new FoldingService().folding(compiled)
        assert folds.any { it.kind == 'imports' }
        def regions = '''\
            // #region demo
            class Regioned {
            }
            // #endregion
            /* one */
            /*
             block
            */
            '''.stripIndent()
        def regionUri = fixture.open('Regioned.groovy', regions)
        def regionDoc = snapshotOf(regionUri)
        assert new FoldingService().folding(regionDoc).any { it.kind == 'region' }
        assert new FoldingService().folding(new CompiledDocument(URI.create('file:///e.groovy'), 1, '', null, null, null)).isEmpty()

        def lenses = new CodeLensService().codeLenses(compiled, snapshot, UTF16)
        assert lenses.any { it.command.title.contains('reference') }
        assert new CodeLensService().codeLenses(null, snapshot, UTF16).isEmpty()

        def selection = new SelectionRangeService().selectionRanges(document, snapshot, [new Position(400, 0)], UTF16)
        assert selection[0].range.start.line == 400

        def links = new DocumentLinkService().documentLinks(compiled, snapshot, UTF16)
        assert links != null
        assert new DocumentLinkService().documentLinks(null, snapshot, UTF16).isEmpty()
    }

    private CompiledDocument snapshotOf(String uri) {
        fixture.server.context.snapshot.get(uri)
    }

    private static List<Integer> tokenTypes(List<Integer> data) {
        def types = []
        for (int i = 0; i + 4 < data.size(); i += 5) {
            types << data[i + 3]
        }
        types
    }

    private static List<Integer> tokenModifiers(List<Integer> data) {
        def modifiers = []
        for (int i = 0; i + 4 < data.size(); i += 5) {
            modifiers << data[i + 4]
        }
        modifiers
    }

    private static Position caret(String text, String token) {
        int idx = text.indexOf(token)
        assert idx >= 0 : token
        int line = 0
        int character = 0
        for (int i = 0; i < idx; i++) {
            if (text.charAt(i) == '\n') {
                line++
                character = 0
            } else {
                character++
            }
        }
        new Position(line, character)
    }

    private static Position after(String text, String token) {
        def position = caret(text, token)
        new Position(position.line, position.character + token.length())
    }

}
