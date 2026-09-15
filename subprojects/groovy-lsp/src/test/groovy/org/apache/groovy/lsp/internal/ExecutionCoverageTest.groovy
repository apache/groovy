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
package org.apache.groovy.lsp.internal

import org.apache.groovy.lsp.GroovyLanguageServerLauncher
import org.apache.groovy.lsp.internal.compile.AstQuery
import org.apache.groovy.lsp.internal.compile.CompilationSnapshot
import org.apache.groovy.lsp.internal.compile.CompiledDocument
import org.apache.groovy.lsp.internal.compile.CompilerSettings
import org.apache.groovy.lsp.internal.compile.GroovyCompiler
import org.apache.groovy.lsp.internal.compile.TypeIndex
import org.apache.groovy.lsp.internal.feature.CodeActionService
import org.apache.groovy.lsp.internal.feature.HoverService
import org.apache.groovy.lsp.internal.feature.NavigationService
import org.apache.groovy.lsp.internal.feature.RenameService
import org.apache.groovy.lsp.internal.feature.SourceGeneration
import org.apache.groovy.lsp.internal.feature.SupportServices
import org.apache.groovy.lsp.internal.feature.SymbolIdentity
import org.apache.groovy.lsp.internal.feature.TypeInference
import org.apache.groovy.lsp.internal.position.PositionEncoding
import org.apache.groovy.lsp.internal.position.Positions
import org.apache.groovy.lsp.internal.util.Uris
import org.apache.groovy.lsp.internal.workspace.TextDocument
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.FieldNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.expr.AttributeExpression
import org.codehaus.groovy.ast.expr.ClassExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.ConstructorCallExpression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.PropertyExpression
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.eclipse.lsp4j.CallHierarchyIncomingCallsParams
import org.eclipse.lsp4j.CallHierarchyOutgoingCallsParams
import org.eclipse.lsp4j.CallHierarchyPrepareParams
import org.eclipse.lsp4j.CodeActionContext
import org.eclipse.lsp4j.CodeActionParams
import org.eclipse.lsp4j.CompletionParams
import org.eclipse.lsp4j.DeclarationParams
import org.eclipse.lsp4j.DefinitionParams
import org.eclipse.lsp4j.Diagnostic
import org.eclipse.lsp4j.DidChangeConfigurationParams
import org.eclipse.lsp4j.DidChangeWorkspaceFoldersParams
import org.eclipse.lsp4j.DidOpenTextDocumentParams
import org.eclipse.lsp4j.DocumentFormattingParams
import org.eclipse.lsp4j.DocumentHighlightParams
import org.eclipse.lsp4j.DocumentLinkParams
import org.eclipse.lsp4j.DocumentOnTypeFormattingParams
import org.eclipse.lsp4j.DocumentRangeFormattingParams
import org.eclipse.lsp4j.ExecuteCommandParams
import org.eclipse.lsp4j.FoldingRangeRequestParams
import org.eclipse.lsp4j.FormattingOptions
import org.eclipse.lsp4j.HoverParams
import org.eclipse.lsp4j.ImplementationParams
import org.eclipse.lsp4j.InlayHintParams
import org.eclipse.lsp4j.MarkupContent
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.PrepareRenameParams
import org.eclipse.lsp4j.Range
import org.eclipse.lsp4j.ReferenceContext
import org.eclipse.lsp4j.ReferenceParams
import org.eclipse.lsp4j.RenameParams
import org.eclipse.lsp4j.SelectionRangeParams
import org.eclipse.lsp4j.SemanticTokensParams
import org.eclipse.lsp4j.SignatureHelpParams
import org.eclipse.lsp4j.TextDocumentIdentifier
import org.eclipse.lsp4j.TextDocumentItem
import org.eclipse.lsp4j.TypeDefinitionParams
import org.eclipse.lsp4j.TypeHierarchyPrepareParams
import org.eclipse.lsp4j.TypeHierarchySubtypesParams
import org.eclipse.lsp4j.TypeHierarchySupertypesParams
import org.eclipse.lsp4j.WindowClientCapabilities
import org.eclipse.lsp4j.WorkspaceEdit
import org.eclipse.lsp4j.WorkspaceFolder
import org.eclipse.lsp4j.WorkspaceFoldersChangeEvent
import org.eclipse.lsp4j.jsonrpc.messages.Either
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

import java.nio.file.Files
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

final class ExecutionCoverageTest {

    private LspFixture fixture

    @AfterEach
    void tearDown() {
        fixture?.close()
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
        assert tds.rangeFormatting(new DocumentRangeFormattingParams(missing,
                new FormattingOptions(4, true), new Range(new Position(0, 0), new Position(0, 1)))).get().isEmpty()
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
    void staticConstructorEnumAndCatchAreNavigable() {
        fixture = new LspFixture()
        def src = '''\
            package demo
            import java.util.concurrent.ConcurrentHashMap
            import java.util.Map as AliasMap
            import static java.lang.Math.abs
            import java.util.*
            /** Hello type. */
            class Hello {
                String name
                Hello(String name) { this.name = name }
                static String go(String x) { x }
                def run() {
                    def local = go("ok")
                    def made = new Hello(local)
                    this.@name
                    for (k, v in [a: 1]) { abs(v) }
                    try { made.name } catch (Exception e) { e }
                    local
                }
            }
            interface Face { def ping() }
            enum Tone { LOW, HIGH }
            class Impl implements Face {
                def ping() { Tone.LOW }
            }
            '''.stripIndent()
        def uri = fixture.open('Hello.groovy', src)
        def id = new TextDocumentIdentifier(uri)
        def tds = fixture.server.textDocumentService
        def lines = src.readLines()
        def goLine = lines.findIndexOf { it.contains('go("ok")') }
        def ctorLine = lines.findIndexOf { it.contains('new Hello') }
        def goCol = lines[goLine].indexOf('go(') + 3
        def ctorCol = lines[ctorLine].indexOf('Hello(') + 6
        def help = tds.signatureHelp(new SignatureHelpParams(id, new Position(goLine, goCol))).get()
        assert help != null
        def ctorHelp = tds.signatureHelp(new SignatureHelpParams(id, new Position(ctorLine, ctorCol))).get()
        assert ctorHelp != null
        def hover = tds.hover(new HoverParams(id, new Position(6, 10))).get()
        assert hover != null
        def defn = tds.definition(new DefinitionParams(id, new Position(goLine, lines[goLine].indexOf('go(')))).get()
        assert defn != null
        def faceLine = lines.findIndexOf { it.contains('interface Face') }
        def impl = tds.implementation(new ImplementationParams(id, new Position(faceLine, lines[faceLine].indexOf('Face')))).get()
        assert impl != null
        def tokens = tds.semanticTokensFull(new SemanticTokensParams(id)).get()
        assert tokens.data.size() >= 5
        def folds = tds.foldingRange(new FoldingRangeRequestParams(id)).get()
        assert folds.size() >= 1
        def hints = tds.inlayHint(new InlayHintParams(id, new Range(new Position(0, 0), new Position(30, 0)))).get()
        assert hints != null
        def module = fixture.server.context.snapshot.get(uri).module
        assert module.packageName.contains('demo')
        assert AstQuery.nodeAt(module, 2, 10) != null
        assert AstQuery.declarations(module).any { it.class.simpleName.contains('Package') || it.lineNumber == 1 }
        def hits = []
        AstQuery.walk(module, { node, ctx -> hits << node })
        assert hits.any { it instanceof Parameter }
    }

    @Test
    void callAndTypeHierarchyFollowCompiledMethods() {
        fixture = new LspFixture()
        def uri = fixture.open('Hello.groovy', '''\
            class Base {}
            class Hello extends Base {
                def foo() { bar() }
                def bar() { 1 }
            }
            '''.stripIndent())
        def id = new TextDocumentIdentifier(uri)
        def tds = fixture.server.textDocumentService
        def prepared = tds.prepareCallHierarchy(new CallHierarchyPrepareParams(id, new Position(3, 8))).get()
        assert prepared
        def incoming = tds.callHierarchyIncomingCalls(new CallHierarchyIncomingCallsParams(prepared[0])).get()
        assert incoming.any { it.from.name == 'foo' }
        def fromFoo = tds.prepareCallHierarchy(new CallHierarchyPrepareParams(id, new Position(2, 8))).get()
        assert fromFoo
        def outgoing = tds.callHierarchyOutgoingCalls(new CallHierarchyOutgoingCallsParams(fromFoo[0])).get()
        assert outgoing.any { it.to.name == 'bar' }
        def types = tds.prepareTypeHierarchy(new TypeHierarchyPrepareParams(id, new Position(1, 6))).get()
        assert types
        assert tds.typeHierarchySupertypes(new TypeHierarchySupertypesParams(types[0])).get()
                .any { it.name == 'Base' }
        def base = tds.prepareTypeHierarchy(new TypeHierarchyPrepareParams(id, new Position(0, 6))).get()
        assert tds.typeHierarchySubtypes(new TypeHierarchySubtypesParams(base[0])).get()
                .any { it.name == 'Hello' }
    }

    @Test
    void documentLinkResolvesImportedWorkspaceType() {
        fixture = new LspFixture()
        fixture.open('A.groovy', 'package demo\nclass Widget {}\n')
        def b = fixture.open('B.groovy', 'package demo\nimport demo.Widget\nclass Use { Widget w }\n')
        def links = fixture.server.textDocumentService.documentLink(new DocumentLinkParams(new TextDocumentIdentifier(b))).get()
        assert links.any { it.target.contains('A.groovy') }
    }

    @Test
    void organizeImportsAppliesWorkspaceEdit() {
        fixture = new LspFixture()
        def uri = fixture.open('Hello.groovy', '''\
            import java.util.concurrent.ConcurrentHashMap
            import java.util.ArrayList
            class Hello { ArrayList x }
            '''.stripIndent())
        def edit = fixture.server.workspaceService.executeCommand(
                new ExecuteCommandParams('groovy.lsp.organizeImports', [uri])).get()
        assert edit.changes[uri]
        assert fixture.appliedEdits
        def refs = fixture.server.workspaceService.executeCommand(
                new ExecuteCommandParams('groovy.lsp.showReferences', [uri, 'not-a-number', 6])).get()
        assert refs != null
        assert fixture.server.workspaceService.executeCommand(
                new ExecuteCommandParams('groovy.lsp.showReferences', ['file:///missing.groovy', 0, 0])).get() == []
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
        assert fixture.server.context.workspaceFolders.contains(folder.toUri())
        fixture.server.workspaceService.didChangeWorkspaceFolders(new DidChangeWorkspaceFoldersParams(
                new WorkspaceFoldersChangeEvent([], [added])))
        assert !fixture.server.context.workspaceFolders.contains(folder.toUri())
        fixture.server.workspaceService.didChangeWorkspaceFolders(new DidChangeWorkspaceFoldersParams(
                new WorkspaceFoldersChangeEvent([], [])))
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
    void commentFoldOnTypeTabsAndTrailingNewlineFormat() {
        fixture = new LspFixture()
        def uri = fixture.open('Hello.groovy', '''\
            class Hello {
            /*
             * docs
             */
                def x
            }'''.stripIndent())
        def folds = fixture.server.textDocumentService.foldingRange(
                new FoldingRangeRequestParams(new TextDocumentIdentifier(uri))).get()
        assert folds.any { it.kind == 'comment' }
        def tabbed = fixture.open('Tabs.groovy', 'class Tabs {\n\n}\n')
        def edits = fixture.server.textDocumentService.onTypeFormatting(
                new DocumentOnTypeFormattingParams(new TextDocumentIdentifier(tabbed),
                        new FormattingOptions(4, false), new Position(1, 0), '\n')).get()
        assert edits[0].newText.contains('\t') || edits[0].newText.isEmpty()
        def formatted = fixture.server.textDocumentService.formatting(
                new DocumentFormattingParams(new TextDocumentIdentifier(uri), new FormattingOptions(4, true))).get()
        assert formatted != null
    }

    @Test
    void completionResolvesWorkspaceTypeAndMembers() {
        fixture = new LspFixture()
        fixture.open('Other.groovy', 'class Other {}\n')
        def src = '''\
            class Hello {
                def x = "".ea
            }
            '''.stripIndent()
        def uri = fixture.open('Hello.groovy', src)
        def tds = fixture.server.textDocumentService
        def memberLine = src.readLines()[1]
        def members = tds.completion(new CompletionParams(new TextDocumentIdentifier(uri),
                new Position(1, memberLine.indexOf('ea') + 2))).get()
        assert members.getRight().items.any { it.label == 'each' }
        def types = tds.completion(new CompletionParams(new TextDocumentIdentifier(uri), new Position(0, 0))).get()
        def other = types.getRight().items.find { it.label == 'Other' }
        if (other != null) {
            def resolved = tds.resolveCompletionItem(other).get()
            assert resolved.label == 'Other'
        }
    }

    @Test
    void addPackageAndCreateClassFromQualifiedDiagnostic() {
        fixture = new LspFixture()
        def root = Files.createTempDirectory('groovy-lsp-pkg-action')
        def dir = root.resolve('src').resolve('main').resolve('groovy').resolve('demo')
        Files.createDirectories(dir)
        def file = dir.resolve('Hello.groovy')
        Files.writeString(file, 'package wrong\nclass Hello {}\n')
        fixture.server.context.workspaceFolders.add(root.toUri())
        def item = new TextDocumentItem(file.toUri().toString(), 'groovy', 1, Files.readString(file))
        fixture.server.textDocumentService.didOpen(new DidOpenTextDocumentParams(item))
        fixture.server.context.recompile()
        def actions = fixture.server.textDocumentService.codeAction(new CodeActionParams(
                new TextDocumentIdentifier(file.toUri().toString()),
                new Range(new Position(1, 6), new Position(1, 11)),
                new CodeActionContext([]))).get()
        assert actions.any { it.isRight() && it.getRight().title.contains('package demo') }
        def diagnostic = new Diagnostic()
        diagnostic.range = new Range(new Position(0, 0), new Position(0, 1))
        diagnostic.message = Either.forLeft('unable to resolve class com.acme.Widget')
        def create = fixture.server.textDocumentService.codeAction(new CodeActionParams(
                new TextDocumentIdentifier(file.toUri().toString()), diagnostic.range,
                new CodeActionContext([diagnostic]))).get()
        assert create.any { it.isRight() && it.getRight().title == 'Create class Widget' }
        diagnostic.message = Either.forRight(new MarkupContent('markdown', 'unable to resolve class skipme'))
        def skipped = fixture.server.textDocumentService.codeAction(new CodeActionParams(
                new TextDocumentIdentifier(file.toUri().toString()), diagnostic.range,
                new CodeActionContext([diagnostic]))).get()
        assert skipped.every { !it.isRight() || it.getRight().title != 'Create class skipme' }
    }

    @Test
    void compilerReadsExtraFilesAndRestoresProcessFlags() {
        def dir = Files.createTempDirectory('groovy-lsp-extra')
        def extra = dir.resolve('Extra.groovy')
        Files.writeString(extra, 'class Extra {}\n')
        def note = dir.resolve('readme.txt')
        Files.writeString(note, 'not groovy')
        def nested = dir.resolve('nested')
        Files.createDirectory(nested)
        System.setProperty('groovy.asttest.enable', 'true')
        System.setProperty('groovy.grape.enable', 'true')
        def compiler = new GroovyCompiler()
        def snapshot
        try {
            snapshot = compiler.compile(
                    [new TextDocument(URI.create('file:///tmp/Open.groovy'), 'groovy', 1, 'class Open {}\n')],
                    [null, nested, note, extra, extra],
                    CompilerSettings.defaults(),
                    ExecutionCoverageTest.classLoader)
        } finally {
            compiler.close()
        }
        assert System.getProperty('groovy.asttest.enable') == 'true'
        assert System.getProperty('groovy.grape.enable') == 'true'
        System.clearProperty('groovy.asttest.enable')
        System.clearProperty('groovy.grape.enable')
        assert snapshot.get(extra.toUri()).module != null
        assert snapshot.get(extra.toUri()).module.classes.any { it.nameWithoutPackage == 'Extra' }
        assert snapshot.types().byName('Extra') != null
        assert snapshot.types().matchingPrefix('E', 1).size() == 1
        def existing = new CompilerSettings([dir.toString()], [], 3, false, false)
        existing.createClassLoader(ExecutionCoverageTest.classLoader).withCloseable { loader ->
            assert loader.URLs.length >= 1
        }
        def missing = Files.createTempDirectory('groovy-lsp-fp').resolve('gone.groovy')
        def live = Files.createTempFile('groovy-lsp-fp', '.groovy')
        Files.writeString(live, 'class Live {}')
        def fingerprint = LanguageServerContext.fingerprint([], [live, missing], CompilerSettings.defaults())
        assert fingerprint.contains(live.fileName.toString())
        assert fingerprint.contains(missing.fileName.toString())
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
        40.times {
            try {
                client = new Socket('127.0.0.1', port)
                return
            } catch (ConnectException ignored) {
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

    @Test
    void helperEdgesForNavigationHoverAndGeneration() {
        assert Uris.fileName(URI.create('foo:bar')) == 'bar'
        assert !RenameService.isIdentifier('ab!')
        def propertyName = new ConstantExpression('name')
        propertyName.lineNumber = 1
        propertyName.columnNumber = 7
        propertyName.lastLineNumber = 1
        propertyName.lastColumnNumber = 11
        def attr = new AttributeExpression(new VariableExpression('this'), propertyName)
        attr.lineNumber = 1
        attr.columnNumber = 1
        attr.lastLineNumber = 1
        attr.lastColumnNumber = 11
        Positions.toIdentifierRange(attr, 'this.@name', PositionEncoding.UTF16)
        def ctor = new ConstructorCallExpression(ClassHelper.OBJECT_TYPE, MethodCallExpression.NO_ARGUMENTS)
        ctor.lineNumber = 1
        assert NavigationService.argumentCount(ctor.arguments) >= 0
        def emptyOwner = new ClassNode('demo.Empty', 0, ClassHelper.OBJECT_TYPE)
        emptyOwner.lineNumber = 1
        emptyOwner.columnNumber = 1
        emptyOwner.lastLineNumber = 3
        emptyOwner.lastColumnNumber = 2
        assert SourceGeneration.equalsAndHashCode(emptyOwner,
                new CompiledDocument(URI.create('file:///tmp/Empty.groovy'), 1, 'class Empty {\n}\n', null, null, null),
                PositionEncoding.UTF16)
        def support = new SupportServices()
        assert support.signatureHelp(new TextDocument(URI.create('file:///tmp/X.groovy'), 'groovy', 1, ''),
                CompilationSnapshot.EMPTY, new Position(0, 0), PositionEncoding.UTF16).signatures.isEmpty()
        assert support.folding(null).isEmpty()
        assert support.inlayHints(null, null, PositionEncoding.UTF16).isEmpty()
        assert support.documentLinks(null, CompilationSnapshot.EMPTY, PositionEncoding.UTF16).isEmpty()
        assert support.codeLenses(null, CompilationSnapshot.EMPTY, PositionEncoding.UTF16).isEmpty()
        assert support.semanticTokens(null, PositionEncoding.UTF16).data.isEmpty()
        def diagnostic = new Diagnostic()
        diagnostic.message = Either.forRight(new MarkupContent('plaintext', 'hello'))
        assert SupportServices.diagnosticMessage(diagnostic) == 'hello'
        def field = new FieldNode('n', 0, ClassHelper.OBJECT_TYPE, ClassHelper.OBJECT_TYPE, new ConstantExpression('x'))
        field.dynamicTyped = true
        assert TypeInference.of(field) != null
        def method = new MethodNode('m', 0, ClassHelper.VOID_TYPE, new Parameter[0], ClassNode.EMPTY_ARRAY, null)
        assert HoverService.render(method).contains('m')
        def call = new MethodCallExpression(new VariableExpression('this'), 'm', MethodCallExpression.NO_ARGUMENTS)
        call.methodTarget = method
        assert HoverService.render(call).contains('m')
        assert SymbolIdentity.of(new ClassExpression(ClassHelper.STRING_TYPE), CompilationSnapshot.EMPTY) != null
        def staticCall = new StaticMethodCallExpression(
                ClassHelper.OBJECT_TYPE, 'hashCode', MethodCallExpression.NO_ARGUMENTS)
        staticCall.lineNumber = 1
        SymbolIdentity.of(staticCall, CompilationSnapshot.EMPTY)
        assert SymbolIdentity.of(method, CompilationSnapshot.EMPTY) != null
        assert CodeActionService.missingAbstracts(ClassHelper.OBJECT_TYPE) != null
        def snapshot = new GroovyCompiler().compile(
                [new TextDocument(URI.create('file:///tmp/Script.groovy'), 'groovy', 1, 'def x = 1\n')],
                [], CompilerSettings.defaults(), ExecutionCoverageTest.classLoader)
        assert snapshot.types() != TypeIndex.EMPTY || snapshot.documents
        def races = (1..8).collect { Thread.start { snapshot.types() } }
        races*.join()
        assert snapshot.types() === snapshot.types()
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
