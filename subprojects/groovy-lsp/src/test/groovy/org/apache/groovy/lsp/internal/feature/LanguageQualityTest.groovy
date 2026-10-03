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

import groovy.lang.groovydoc.Groovydoc
import groovy.lang.groovydoc.GroovydocHolder
import java.lang.reflect.Modifier
import java.nio.file.Path
import org.apache.groovy.lsp.internal.compile.AstQuery
import org.apache.groovy.lsp.internal.compile.CompilationSnapshot
import org.apache.groovy.lsp.internal.compile.CompiledDocument
import org.apache.groovy.lsp.internal.compile.GroovyCompiler
import org.apache.groovy.lsp.internal.position.PositionEncoding
import org.apache.groovy.lsp.internal.util.Uris
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.ModuleNode
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.PropertyNode
import org.codehaus.groovy.ast.expr.MapExpression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression
import org.codehaus.groovy.ast.expr.TupleExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.stmt.EmptyStatement
import org.codehaus.groovy.control.SourceUnit
import org.eclipse.lsp4j.CompletionItemKind
import static org.objectweb.asm.Opcodes.ACC_PUBLIC

import org.apache.groovy.lsp.internal.LspFixture
import org.apache.groovy.lsp.internal.LanguageServerContext
import org.apache.groovy.lsp.internal.compile.CompilerSettings
import org.apache.groovy.lsp.internal.compile.WorkspaceLayout
import org.apache.groovy.lsp.internal.workspace.TextDocument
import org.eclipse.lsp4j.CodeActionContext
import org.eclipse.lsp4j.CodeActionParams
import org.eclipse.lsp4j.CompletionItem
import org.eclipse.lsp4j.CompletionParams
import org.eclipse.lsp4j.Diagnostic
import org.eclipse.lsp4j.DiagnosticSeverity
import org.eclipse.lsp4j.HoverParams
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.Range
import org.eclipse.lsp4j.SelectionRangeParams
import org.eclipse.lsp4j.SignatureHelpParams
import org.eclipse.lsp4j.TextDocumentIdentifier
import org.eclipse.lsp4j.jsonrpc.messages.Either
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

final class LanguageQualityTest {
    private static final PositionEncoding UTF16 = PositionEncoding.UTF16

    private static final PositionEncoding ENC = PositionEncoding.UTF16


    private LspFixture fixture

    @AfterEach
    void tearDown() {
        fixture?.close()
    }

    @Test
    void completionOffersWorkspaceTypeAndImportEdit() {
        fixture = new LspFixture()
        fixture.open('Widget.groovy', 'package demo\nclass Widget {}\n')
        def uri = fixture.open('Use.groovy', 'class Use {\n  def x = Wid\n}\n')
        def result = fixture.server.textDocumentService.completion(
                new CompletionParams(new TextDocumentIdentifier(uri), new Position(1, 13))).get()
        def items = result.getRight().items
        def widget = items.find { it.label == 'Widget' }
        assert widget != null
        assert widget.detail == 'demo.Widget'
        assert widget.additionalTextEdits
        assert widget.additionalTextEdits[0].newText.contains('import demo.Widget')
    }

    @Test
    void hoverRendersGroovydocAsMarkdown() {
        fixture = new LspFixture()
        def uri = fixture.open('Hello.groovy', '''\
            /** Greets {@code name}. @param name the person */
            class Hello {}
            '''.stripIndent())
        def hover = fixture.server.textDocumentService.hover(
                new HoverParams(new TextDocumentIdentifier(uri), new Position(1, 8))).get()
        assert hover != null
        def value = hover.contents.getRight().value
        assert value.contains('`name`')
        assert value.contains('**@param** `name` the person')
    }

    @Test
    void completionResolveFillsDocumentationFromData() {
        fixture = new LspFixture()
        def uri = fixture.open('Hello.groovy', '''\
            class Hello {
                def foo() {}
            }
            '''.stripIndent())
        def result = fixture.server.textDocumentService.completion(
                new CompletionParams(new TextDocumentIdentifier(uri), new Position(1, 8))).get()
        def foo = result.getRight().items.find { it.label == 'foo' }
        assert foo != null
        assert foo.data != null
        def resolved = fixture.server.textDocumentService.resolveCompletionItem(foo).get()
        assert resolved != null
    }

    @Test
    void methodSnippetIncludesParameterPlaceholders() {
        fixture = new LspFixture()
        def uri = fixture.open('Hello.groovy', '''\
            class Hello {
                def foo(String bar) {}
                def go() { f }
            }
            '''.stripIndent())
        def result = fixture.server.textDocumentService.completion(
                new CompletionParams(new TextDocumentIdentifier(uri), new Position(2, 15))).get()
        def foo = result.getRight().items.find { it.label == 'foo' }
        assert foo.insertText.contains('${1:bar}')
    }

    @Test
    void signatureHelpListsOverloads() {
        fixture = new LspFixture()
        def uri = fixture.open('Hello.groovy', '''\
            class Hello {
                def print(int i) {}
                def print(String s) {}
                def go() { print("x") }
            }
            '''.stripIndent())
        def help = fixture.server.textDocumentService.signatureHelp(
                new SignatureHelpParams(new TextDocumentIdentifier(uri), new Position(3, 20))).get()
        assert help.signatures.size() >= 2
        assert help.signatures.any { it.label.contains('String') }
        assert help.signatures.any { it.label.contains('int') }
    }

    @Test
    void selectionRangeChainsContainingNodes() {
        fixture = new LspFixture()
        def uri = fixture.open('Hello.groovy', '''\
            class Hello {
                def foo() { bar }
            }
            '''.stripIndent())
        def ranges = fixture.server.textDocumentService.selectionRange(
                new SelectionRangeParams(new TextDocumentIdentifier(uri), [new Position(1, 18)])).get()
        assert ranges.size() == 1
        int depth = 0
        def current = ranges[0]
        while (current != null) {
            depth += 1
            current = current.parent
        }
        assert depth >= 2
    }

    @Test
    void addImportCodeActionUsesTypeIndex() {
        fixture = new LspFixture()
        fixture.open('Widget.groovy', 'package demo\nclass Widget {}\n')
        def uri = fixture.open('Use.groovy', 'class Use {\n  Widget x\n}\n')
        def diagnostic = new Diagnostic()
        diagnostic.range = new Range(new Position(1, 2), new Position(1, 8))
        diagnostic.message = Either.forLeft('unable to resolve class Widget')
        diagnostic.severity = DiagnosticSeverity.Error
        def actions = fixture.server.textDocumentService.codeAction(
                new CodeActionParams(new TextDocumentIdentifier(uri),
                        diagnostic.range, new CodeActionContext([diagnostic]))).get()
        assert actions.any { it.getRight().title == 'Add import for demo.Widget' }
    }

    @Test
    void unchangedRecompileReusesTheSnapshot() {
        fixture = new LspFixture()
        fixture.open('Hello.groovy', 'class Hello {}\n')
        def first = fixture.server.context.snapshot
        def second = fixture.server.context.recompile()
        assert second.is(first)
    }

    @Test
    void fingerprintChangesWithDocumentText() {
        def a = new TextDocument(URI.create('file:///tmp/A.groovy'), 'groovy', 1, 'class A {}')
        def b = new TextDocument(URI.create('file:///tmp/A.groovy'), 'groovy', 2, 'class B {}')
        def left = LanguageServerContext.fingerprint([a], [], CompilerSettings.defaults())
        def right = LanguageServerContext.fingerprint([b], [], CompilerSettings.defaults())
        assert left != right
        def extra = LanguageServerContext.fingerprint([a], [],
                new CompilerSettings([], [], CompilerSettings.defaults().throughPhase, false, false, [gdsl: 'x']))
        assert extra != left
    }

    @Test
    void gdkAppearsOnceAPrefixIsTyped() {
        fixture = new LspFixture()
        def uri = fixture.open('Hello.groovy', 'class Hello {\n  def x = "".ea\n}\n')
        def result = fixture.server.textDocumentService.completion(
                new CompletionParams(new TextDocumentIdentifier(uri), new Position(1, 15))).get()
        assert result.getRight().items.any { it.label == 'each' && it.detail == 'GDK' }
    }

    @Test
    void resolveCompletionItemWithoutDataIsUnchanged() {
        fixture = new LspFixture()
        fixture.open('Hello.groovy', 'class Hello {}\n')
        def item = new CompletionItem('x')
        def resolved = fixture.server.textDocumentService.resolveCompletionItem(item).get()
        assert resolved.label == 'x'
        assert resolved.documentation == null
    }

    @Test
    void withInferredKeepsExplicitClasspath() {
        def settings = new CompilerSettings(['/tmp/explicit.jar'], ['custom'], 3, false, false, [gdsl: 'p'])
        def inferred = WorkspaceLayout.withInferred(settings, [])
        assert inferred.classpath == ['/tmp/explicit.jar']
        assert inferred.sourcePaths == ['custom']
        assert inferred.extra.gdsl == 'p'
    }

    @Test
    void completionIncludesEnclosingParameter() {
        fixture = new LspFixture()
        def uri = fixture.open('Hello.groovy', '''\
            class Hello {
                def foo(String bar) { ba }
            }
            '''.stripIndent())
        def items = fixture.server.textDocumentService.completion(
                new CompletionParams(new TextDocumentIdentifier(uri), new Position(1, 28))).get().getRight().items
        assert items.any { it.label == 'bar' }
    }

    @Test
    void gdkIsOmittedWithoutAPrefix() {
        fixture = new LspFixture()
        def uri = fixture.open('Hello.groovy', 'class Hello {\n  def x = "".\n}\n')
        def result = fixture.server.textDocumentService.completion(
                new CompletionParams(new TextDocumentIdentifier(uri), new Position(1, 13))).get()
        def labels = result.getRight().items*.label
        assert !('each' in labels)
    }
    @Test
    void completionResolvesDocsAndGdkWithoutModule() {
        def gdk = new TextDocument(fileUri('file:///tmp/cov-gdk.groovy'), 'groovy', 1, 'foo.ea')
        def gdkItems = new CompletionService().complete(gdk, null, new Position(0, 6), UTF16).items
        assert gdkItems.any { it.label == 'each' && it.detail == 'GDK' }

        def uri = fileUri('file:///tmp/cov-docs.groovy')
        def snap = compileOf(uri, 'class Hello {}\n')
        def hello = snap.get(uri).module.classes.find { it.nameWithoutPackage == 'Hello' }
        hello.putNodeMetaData(GroovydocHolder.DOC_COMMENT, new Groovydoc('Hello type docs.', hello))
        def typeItem = new CompletionItem('Hello')
        typeItem.data = [k: 't', n: 'Hello']
        def typeResolved = new CompletionService().resolve(typeItem, snap)
        assert typeResolved.documentation.getRight().value.contains('Hello type docs')

        def owner = new ClassNode('demo.Hello', ACC_PUBLIC, ClassHelper.OBJECT_TYPE)
        def method = owner.addMethod('foo', ACC_PUBLIC, ClassHelper.OBJECT_TYPE,
                Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, null)
        method.lineNumber = 2
        method.putNodeMetaData(GroovydocHolder.DOC_COMMENT, new Groovydoc('Does the thing.', method))
        def module = new ModuleNode((SourceUnit) null)
        module.addClass(owner)
        def methodUri = fileUri('file:///tmp/cov-method-doc.groovy')
        def methodSnap = snapshot(new CompiledDocument(methodUri, 1, 'class Hello { def foo() {} }\n', module, null, null))
        def methodItem = new CompletionItem('foo')
        methodItem.data = [k: 'm', n: 'foo', o: 'demo.Hello', d: '']
        def methodResolved = new CompletionService().resolve(methodItem, methodSnap)
        assert methodResolved.documentation.getRight().value.contains('Does the thing')

        def blank = new CompletionItem('x')
        blank.data = 'nope'
        assert new CompletionService().resolve(blank, methodSnap).documentation == null
        def emptyData = new CompletionItem('x')
        emptyData.data = [k: null]
        assert new CompletionService().resolve(emptyData, methodSnap).documentation == null
    }

    @Test
    void completionMembersNamedArgumentsAndOverrides() {
        fixture = new LspFixture()
        def src = '''\
            enum Color { RED }
            interface Face {
                void run()
                // face-caret
            }
            interface Named { String name() }
            class Hello implements Named {
                String name
                int years
                Hello(String name, int age) {}
                static void go(String name, int age) {}
                void foo(String name) {}
                def bar(String left, int right) {
                    zzz
                    ri
                    Co
                    Hello.na
                    go(xx, ag)
                    new Hello(yy, ag)
                    foo(name: 1)
                }
            }
            '''.stripIndent()
        def uri = fixture.open('Hello.groovy', src)
        def doc = fixture.server.context.documentFor(uri)
        def snap = fixture.server.context.snapshot
        def module = snap.get(uri).module
        assert walked(module, StaticMethodCallExpression) { it.method == 'go' }
        def foo = null
        AstQuery.walk(module) { node, ctx ->
            if (node instanceof MethodCallExpression && node.methodAsString == 'foo') {
                foo = node
            }
        }
        assert foo != null
        assert foo.arguments instanceof TupleExpression
        assert foo.arguments.expressions.any { it instanceof MapExpression }
        assert MethodBinding.resolveMethod(foo, snap)?.name == 'foo'

        def service = new CompletionService()
        def faceLine = src.readLines().findIndexOf { it.contains('face-caret') }
        def faceItems = service.complete(doc, snap, new Position(faceLine, 0), UTF16).items
        assert faceItems.every { it.detail == null || !it.detail.startsWith('override') }

        def zzz = itemsAt(service, doc, snap, src, 'zzz')
        assert zzz.every { it.detail == null || !it.detail.startsWith('override') }

        def locals = itemsAt(service, doc, snap, src, 'ri')
        assert locals.any { it.label == 'right' && it.kind == CompletionItemKind.Variable }
        assert locals.every { it.label != 'left' }

        def enums = itemsAt(service, doc, snap, src, 'Co')
        assert enums.any { it.label == 'Color' && it.kind == CompletionItemKind.Enum }

        def helloType = module.classes.find { it.nameWithoutPackage == 'Hello' }
        helloType.addField('other', ACC_PUBLIC, ClassHelper.int_TYPE, null)
        helloType.addField('nameTag', ACC_PUBLIC, ClassHelper.STRING_TYPE, null)
        def fields = itemsAt(service, doc, snap, src, 'Hello.na')
        assert fields.any { it.label == 'nameTag' && it.kind == CompletionItemKind.Field }

        def staticNamed = itemsAt(service, doc, snap, src, 'go(xx, ag')
        assert staticNamed.any { it.label == 'age' && it.detail == 'named argument' }
        assert staticNamed.every { it.detail != 'named argument' || it.label != 'name' }

        def ctorNamed = itemsAt(service, doc, snap, src, 'new Hello(yy, ag')
        assert ctorNamed.any { it.label == 'age' && it.detail == 'named argument' }

        def used = itemsAt(service, doc, snap, src, 'foo(name: 1')
        assert used.every { it.detail != 'named argument' }
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

    private static CompilationSnapshot snapshot(CompiledDocument... documents) {
        def map = [:]
        documents.each { map[it.uri] = it }
        new CompilationSnapshot(map)
    }

    private static URI fileUri(String path) {
        Uris.normalize(URI.create(path))
    }

    private static URI fileUri(Path path) {
        Uris.normalize(path.toUri())
    }

    private static List itemsAt(CompletionService service, TextDocument document, CompilationSnapshot snapshot,
                                String text, String token) {
        def lines = text.readLines()
        int line = -1
        for (int i = lines.size() - 1; i >= 0; i--) {
            if (lines[i].contains(token)) {
                line = i
                break
            }
        }
        assert line >= 0
        int column = lines[line].indexOf(token) + token.length() - 1
        service.complete(document, snapshot, new Position(line, column), UTF16).items
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

    @Test
    void hoverMarkdownForRecordsPropertiesTargetsAndIdentifiers() {
        def hover = new HoverService()
        assert hover.hover(null, null, new Position(0, 0), ENC) == null
        assert hover.markdown(null, null, new Position(0, 0), ENC) == ''

        def blank = new TextDocument(URI.create('file:///blank.groovy'), 'groovy', 1, '   \n')
        def blankDoc = new CompiledDocument(blank.uri, 1, blank.text, null, null, null)
        def blankSnap = new CompilationSnapshot([(blank.uri): blankDoc])
        assert hover.markdown(blank, blankSnap, new Position(0, 1), ENC) == ''
        assert hover.hover(blank, blankSnap, new Position(0, 1), ENC) == null

        def recordType = ClassHelper.make('java.lang.Record')
        def rec = new ClassNode('Point', Modifier.PUBLIC, recordType)
        assert rec.record
        def recordMarkdown = HoverService.render(rec)
        assert recordMarkdown.contains('record')
        assert recordMarkdown.contains('Point')

        def owner = new ClassNode('Box', Modifier.PUBLIC, ClassHelper.OBJECT_TYPE)
        def property = new PropertyNode('title', Modifier.PUBLIC, ClassHelper.STRING_TYPE, owner, null, null, null)
        def propertyMarkdown = HoverService.render(property)
        assert propertyMarkdown.contains('String')
        assert propertyMarkdown.contains('title')

        def params = [new Parameter(ClassHelper.STRING_TYPE, 'left'), new Parameter(ClassHelper.int_TYPE, 'right')] as Parameter[]
        def method = new MethodNode('join', Modifier.PUBLIC, ClassHelper.STRING_TYPE, params, ClassNode.EMPTY_ARRAY, new EmptyStatement())
        method.declaringClass = new ClassNode('Util', Modifier.PUBLIC, ClassHelper.OBJECT_TYPE)
        def call = new MethodCallExpression(new VariableExpression('this'), 'join', MethodCallExpression.NO_ARGUMENTS)
        call.methodTarget = method
        def callMarkdown = HoverService.render(call)
        assert callMarkdown.contains('Util.join')
        assert callMarkdown.contains('String left, int right')

        def widgetUri = open('Widget.groovy', 'class Widget {}\n')
        def note = new TextDocument(URI.create('file:///note.groovy'), 'groovy', 1, 'Widget\n')
        def noteDoc = new CompiledDocument(note.uri, 1, note.text, null, null, null)
        def snap = plus(snapshot(), noteDoc)
        assert snap.types().uniqueBySimpleName('Widget') != null
        def markdown = hover.markdown(note, snap, new Position(0, 'Widget'.length()), ENC)
        assert markdown.startsWith('```groovy')
        assert markdown.contains(snap.types().uniqueBySimpleName('Widget').name())
        assert compiled(widgetUri).module != null
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

    private static CompilationSnapshot plus(CompilationSnapshot base, CompiledDocument... extra) {
        def map = new LinkedHashMap<URI, CompiledDocument>()
        if (base != null) {
            for (CompiledDocument doc : base.documents()) {
                if (doc.uri != null) {
                    map.put(doc.uri, doc)
                }
            }
        }
        for (CompiledDocument doc : extra) {
            def key = doc.uri != null ? doc.uri : URI.create('file:///synthetic-' + System.identityHashCode(doc) + '.groovy')
            map.put(key, doc)
        }
        new CompilationSnapshot(map)
    }

}
