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

import java.lang.reflect.Modifier
import java.nio.file.Files
import java.nio.file.Path
import javax.tools.ToolProvider
import org.apache.groovy.lsp.internal.compile.AstQuery
import org.apache.groovy.lsp.internal.compile.CompilationSnapshot
import org.apache.groovy.lsp.internal.compile.CompiledDocument
import org.apache.groovy.lsp.internal.compile.CompilerSettings
import org.apache.groovy.lsp.internal.compile.GroovyCompiler
import org.apache.groovy.lsp.internal.position.PositionEncoding
import org.apache.groovy.lsp.internal.position.Positions
import org.apache.groovy.lsp.internal.util.Uris
import org.apache.groovy.lsp.internal.workspace.TextDocument
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.CompileUnit
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.ModuleNode
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.expr.ClassExpression
import org.codehaus.groovy.ast.expr.ConstructorCallExpression
import org.codehaus.groovy.ast.expr.DeclarationExpression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.PropertyExpression
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.stmt.EmptyStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.SourceUnit
import org.eclipse.lsp4j.CallHierarchyItem
import org.eclipse.lsp4j.CompletionItem
import org.eclipse.lsp4j.FileRename
import org.eclipse.lsp4j.Range
import org.eclipse.lsp4j.RenameFile
import org.eclipse.lsp4j.SymbolKind
import org.eclipse.lsp4j.TypeHierarchyItem
import org.junit.jupiter.api.Assumptions
import org.junit.jupiter.api.io.TempDir
import static org.objectweb.asm.Opcodes.ACC_PUBLIC

import org.apache.groovy.lsp.internal.LspFixture
import org.eclipse.lsp4j.CallHierarchyIncomingCallsParams
import org.eclipse.lsp4j.CallHierarchyOutgoingCallsParams
import org.eclipse.lsp4j.CallHierarchyPrepareParams
import org.eclipse.lsp4j.DefinitionParams
import org.eclipse.lsp4j.DocumentLinkParams
import org.eclipse.lsp4j.ExecuteCommandParams
import org.eclipse.lsp4j.HoverParams
import org.eclipse.lsp4j.MarkupContent
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.PrepareRenameParams
import org.eclipse.lsp4j.ReferenceContext
import org.eclipse.lsp4j.ReferenceParams
import org.eclipse.lsp4j.RenameParams
import org.eclipse.lsp4j.SignatureHelpParams
import org.eclipse.lsp4j.TextDocumentIdentifier
import org.eclipse.lsp4j.TypeHierarchyPrepareParams
import org.eclipse.lsp4j.TypeHierarchySubtypesParams
import org.eclipse.lsp4j.TypeHierarchySupertypesParams
import org.eclipse.lsp4j.WorkspaceEdit
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

final class NavigationIdentityTest {
    private static final PositionEncoding UTF16 = PositionEncoding.UTF16

    private static final PositionEncoding ENC = PositionEncoding.UTF16


    private LspFixture fixture

    @AfterEach
    void tearDown() {
        fixture?.close()
    }

    @Test
    void hoverAndDefinitionOnCallNameUseTheMethod() {
        fixture = new LspFixture()
        def src = '''\
            class Hello {
                def foo(String bar) { foo(bar) }
            }
            '''.stripIndent()
        def uri = fixture.open('Hello.groovy', src)
        def id = new TextDocumentIdentifier(uri)
        def hover = fixture.server.textDocumentService.hover(new HoverParams(id, new Position(1, 27))).get()
        assert hover != null
        def value = hover.contents instanceof MarkupContent
                ? hover.contents.value
                : hover.contents.getRight().value
        assert value.contains('foo')
        assert !value.contains('```groovy\nfoo\n```') || value.contains('(')
        def defs = fixture.server.textDocumentService.definition(new DefinitionParams(id, new Position(1, 27))).get()
        assert defs.isRight()
        assert defs.getRight().any { it.targetSelectionRange.start.line == 1 && it.targetSelectionRange.start.character == 8 }
    }

    @Test
    void prepareRenameOnParameterSelectsTheParameter() {
        fixture = new LspFixture()
        def uri = fixture.open('Hello.groovy', '''\
            class Hello {
                def foo(String bar) { foo(bar) }
            }
            '''.stripIndent())
        def prepare = fixture.server.textDocumentService.prepareRename(
                new PrepareRenameParams(new TextDocumentIdentifier(uri), new Position(1, 20))).get()
        assert prepare != null
        def result = prepare.getSecond()
        assert result.placeholder == 'bar'
        assert result.range.start.character == 19
    }

    @Test
    void renameDoesNotTouchADifferentMethodOfTheSameName() {
        fixture = new LspFixture()
        def uri = fixture.open('Hello.groovy', '''\
            class Hello {
                def foo(String bar) { foo(bar) }
            }
            class Other {
                def foo() {}
            }
            '''.stripIndent())
        def edit = fixture.server.textDocumentService.rename(
                new RenameParams(new TextDocumentIdentifier(uri), new Position(1, 9), 'renamed')).get()
        def text = edit.changes.values().flatten()*.newText
        assert text.every { it == 'renamed' }
        def ranges = edit.changes.values().flatten()*.range
        assert ranges.every { it.start.line != 4 }
        def refs = fixture.server.textDocumentService.references(
                new ReferenceParams(new TextDocumentIdentifier(uri), new Position(1, 9), new ReferenceContext(true))).get()
        assert refs.every { it.range.start.line != 4 }
        assert refs.size() >= 2
    }

    @Test
    void signatureHelpUsesEnclosingCallAndActiveArgument() {
        fixture = new LspFixture()
        def uri = fixture.open('Hello.groovy', '''\
            class Hello {
                def foo(String bar, int n) {
                    foo(bar, 1)
                }
            }
            '''.stripIndent())
        // caret after the comma of foo(bar, 1)
        def help = fixture.server.textDocumentService.signatureHelp(
                new SignatureHelpParams(new TextDocumentIdentifier(uri), new Position(2, 16))).get()
        assert help.signatures
        assert help.signatures[0].label.contains('foo')
        assert help.activeParameter == 1
    }

    @Test
    void organizeImportsKeepsStarStaticAndAlias() {
        fixture = new LspFixture()
        def uri = fixture.open('Hello.groovy', '''\
            import java.util.LinkedList as LL
            import java.util.regex.Pattern
            import static java.util.Collections.emptyList
            import java.io.*
            class Hello {}
            '''.stripIndent())
        def edit = fixture.server.workspaceService.executeCommand(
                new ExecuteCommandParams('groovy.lsp.organizeImports', [uri])).get()
        assert edit instanceof WorkspaceEdit
        def replacement = edit.changes.values().flatten()[0].newText
        assert replacement.contains('import java.io.*')
        assert replacement.contains('import java.util.LinkedList as LL')
        assert replacement.contains('import static java.util.Collections.emptyList')
        assert fixture.appliedEdits
    }

    @Test
    void callAndTypeHierarchyWalkTheSnapshot() {
        fixture = new LspFixture()
        def uri = fixture.open('Hello.groovy', '''\
            interface Named {}
            class Hello implements Named {
                def foo() { bar() }
                def bar() {}
            }
            '''.stripIndent())
        def id = new TextDocumentIdentifier(uri)
        def tds = fixture.server.textDocumentService
        def prepared = tds.prepareCallHierarchy(new CallHierarchyPrepareParams(id, new Position(2, 8))).get()
        assert prepared.size() == 1
        assert prepared[0].name == 'foo'
        def outgoing = tds.callHierarchyOutgoingCalls(new CallHierarchyOutgoingCallsParams(prepared[0])).get()
        assert outgoing.any { it.to.name == 'bar' }
        def bar = tds.prepareCallHierarchy(new CallHierarchyPrepareParams(id, new Position(3, 8))).get()
        def incoming = tds.callHierarchyIncomingCalls(new CallHierarchyIncomingCallsParams(bar[0])).get()
        assert incoming.any { it.from.name == 'foo' }

        def named = tds.prepareTypeHierarchy(new TypeHierarchyPrepareParams(id, new Position(0, 10))).get()
        assert named[0].name == 'Named'
        def subtypes = tds.typeHierarchySubtypes(new TypeHierarchySubtypesParams(named[0])).get()
        assert subtypes.any { it.name == 'Hello' }
        def hello = tds.prepareTypeHierarchy(new TypeHierarchyPrepareParams(id, new Position(1, 6))).get()
        def supers = tds.typeHierarchySupertypes(new TypeHierarchySupertypesParams(hello[0])).get()
        assert supers.any { it.name == 'Named' }
    }

    @Test
    void renameDoesNotTouchAFieldOfTheSameNameOnAnotherClass() {
        fixture = new LspFixture()
        def uri = fixture.open('Hello.groovy', '''\
            class Hello {
                String name
                def go() { name }
            }
            class Other {
                String name
            }
            '''.stripIndent())
        def refs = fixture.server.textDocumentService.references(
                new ReferenceParams(new TextDocumentIdentifier(uri), new Position(1, 11), new ReferenceContext(true))).get()
        assert refs.every { it.range.start.line != 5 }
        assert refs.size() >= 2
    }

    @Test
    void documentLinkTargetIsAFileUri() {
        fixture = new LspFixture()
        def uri = fixture.open('Hello.groovy', '''\
            import java.util.LinkedList
            class Hello {}
            '''.stripIndent())
        def links = fixture.server.textDocumentService.documentLink(
                new DocumentLinkParams(new TextDocumentIdentifier(uri))).get()
        assert links.every { it.target == null || it.target.contains('://') }
    }

    @Test
    void openDoesNotCompileTheWorkingTree() {
        fixture = new LspFixture()
        def uri = fixture.open('Only.groovy', 'class Only {}\n')
        assert fixture.server.context.snapshot.documents().size() == 1
        assert fixture.server.context.snapshot.get(uri) != null
    }
    @Test
    void navigationUsesClassExpressionSuperAndBadUris() {
        def text = '''\
            class Hello implements Runnable {
                void run() {}
                def values() {
                    def (a, b) = [1, 2]
                    String
                }
            }
            class Blank {
            }
            '''.stripIndent() + '\n'
        def uri = fileUri('file:///tmp/cov-nav.groovy')
        def snap = compileOf(uri, text)
        def compiled = snap.get(uri)
        def doc = compiled.toTextDocument()
        def nav = new NavigationService()
        def module = compiled.module
        def hello = module.classes.find { it.nameWithoutPackage == 'Hello' }
        def run = hello.methods.find { it.name == 'run' && !it.synthetic && it.lineNumber > 0 }
        def values = hello.methods.find { it.name == 'values' && !it.synthetic && it.lineNumber > 0 }
        assert MethodBinding.superOf(run)?.name == 'run'
        def runPos = caretOn(text, run.lineNumber, 'run')
        assert AstQuery.nodeAt(module, doc, runPos, UTF16) instanceof MethodNode
        assert nav.superMethod(doc, snap, runPos, UTF16).isEmpty()
        assert MethodBinding.superOf(values) == null
        assert nav.superMethod(doc, snap, caretOn(text, values.lineNumber, 'values'), UTF16).isEmpty()

        def classExprs = []
        def decls = []
        AstQuery.walk(module) { node, ctx ->
            if (node instanceof ClassExpression) {
                classExprs << node
            }
            if (node instanceof DeclarationExpression && node.multipleAssignmentDeclaration) {
                decls << node
            }
        }
        assert classExprs
        def classPos = positionOf(text, classExprs[0])
        assert AstQuery.nodeAt(module, doc, classPos, UTF16) instanceof ClassExpression
        assert nav.definition(doc, snap, classPos, UTF16).isEmpty()
        assert decls
        def declPos = positionOf(text, decls[0])
        assert AstQuery.nodeAt(module, doc, declPos, UTF16) instanceof DeclarationExpression
        assert nav.definition(doc, snap, declPos, UTF16).isEmpty()

        def trailing = new Position(text.readLines().size() - 1, 0)
        assert AstQuery.nodeAt(module, doc, trailing, UTF16) == null
        assert nav.definition(doc, snap, trailing, UTF16).isEmpty()
        assert nav.definition(null, null, null, UTF16).isEmpty()
        assert nav.superMethod(null, CompilationSnapshot.EMPTY, new Position(0, 0), UTF16).isEmpty()

        def config = new CompilerConfiguration()
        def bad = new SourceUnit('::::', 'class A {}', config, null, null)
        def badModule = new ModuleNode(bad)
        def type = new ClassNode('A', ACC_PUBLIC, ClassHelper.OBJECT_TYPE)
        badModule.addClass(type)
        def fallback = URI.create('file:///fallback.groovy')
        assert NavigationService.uriOf(type, fallback, CompilationSnapshot.EMPTY, null) == fallback
        def method = new MethodNode('m', ACC_PUBLIC, ClassHelper.OBJECT_TYPE,
                Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, null)
        assert NavigationService.uriOf(method, fallback, null, new ModuleNode(bad)) == fallback

        def other = new ClassNode('Other', ACC_PUBLIC, ClassHelper.OBJECT_TYPE)
        other.lineNumber = 1
        def otherModule = new ModuleNode((SourceUnit) null)
        otherModule.addClass(other)
        def wanted = new ClassNode('Wanted', ACC_PUBLIC, ClassHelper.OBJECT_TYPE)
        def located = NavigationService.uriOf(wanted, fallback, snapshot(
                new CompiledDocument(fileUri('file:///tmp/cov-nav-empty.groovy'), 1, '', null, null, null),
                new CompiledDocument(fileUri('file:///tmp/cov-nav-other.groovy'), 1, 'class Other {}', otherModule, null, null)
        ), null)
        assert located == fallback
    }

    @Test
    void navigationJumpsToJavaFieldAndComment(@TempDir Path folder) {
        Assumptions.assumeTrue(ToolProvider.systemJavaCompiler != null, 'jdk.compiler is required')
        fixture = new LspFixture()
        fixture.open(folder.resolve('Widget.java').toString(), '''\
            public class Widget {
                public String id;
                public String name() { return id; }
            }
            '''.stripIndent(), 'java')
        def uri = fixture.open(folder.resolve('Use.groovy').toString(), '''\
            class Use {
                def run(Widget widget) {
                    widget.id
                }
            }
            // Widget
            '''.stripIndent())
        def doc = fixture.server.context.documentFor(uri)
        def snap = fixture.server.context.snapshot
        def widgetTypes = snap.javaSymbols().typesNamed('Widget')
        assert widgetTypes
        def nav = fixture.server.features().navigation()
        def lines = doc.text.readLines()
        def idLine = lines.findIndexOf { it.contains('widget.id') }
        def idColumn = lines[idLine].indexOf('.id') + 1
        PropertyExpression idProperty = null
        AstQuery.walk(snap.get(uri).module) { node, ctx ->
            if (node instanceof PropertyExpression && node.propertyAsString == 'id') {
                idProperty = node
            }
        }
        assert idProperty != null
        def widgetType = new ClassNode('Widget', ACC_PUBLIC, ClassHelper.OBJECT_TYPE)
        def idField = widgetType.addField('id', ACC_PUBLIC, ClassHelper.STRING_TYPE, null)
        def object = new VariableExpression('widget', widgetType)
        object.setSourcePosition(idProperty.objectExpression)
        idProperty.objectExpression = object
        assert MethodBinding.resolveProperty(idProperty).is(idField)
        assert idField.lineNumber <= 0
        def fieldLinks = nav.definition(doc, snap, new Position(idLine, idColumn), UTF16)
        assert fieldLinks.isEmpty() || fieldLinks.every { it.targetUri.endsWith('Widget.java') }
        def commentLine = lines.findIndexOf { it.contains('// Widget') }
        def commentColumn = lines[commentLine].indexOf('Widget')
        def commentPos = new Position(commentLine, commentColumn)
        assert AstQuery.nodeAt(snap.get(uri).module, doc, commentPos, UTF16) == null
        def commentLinks = nav.definition(doc, snap, commentPos, UTF16)
        assert commentLinks
        assert commentLinks[0].targetUri.endsWith('Widget.java')
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

    private static Position caretOn(String text, int groovyLine, String token) {
        def line = text.readLines()[groovyLine - 1]
        new Position(groovyLine - 1, line.indexOf(token))
    }

    private static Position positionOf(String text, node) {
        def line = Positions.lineText(text, node.lineNumber)
        Positions.toLsp(node.lineNumber, Math.max(node.columnNumber, 1), line, UTF16)
    }

    @Test
    void hierarchyCallsTypesAndGuards() {
        def src = '''\
            interface Face {
                String tag()
            }
            class Base {
                String tag() { 'base' }
            }
            class Alpha {
            }
            class Beta extends Alpha {
            }
            class Child extends Base implements Face {
                static String stat(String value) { value }
                Child() {}
                String tag() { 'child' }
                String onlyPing() { stat('x') }
                String use() {
                    helper()
                    stat('x')
                    onlyPing()
                    new Child()
                }
            }
            '''.stripIndent()
        def uri = open('Hierarchy.groovy', src)
        def doc = compiled(uri)
        def snap = snapshot()
        def buffer = doc.toTextDocument()
        def hierarchy = new HierarchyService()
        def child = typeNamed(doc, 'Child')
        def onlyPing = child.methods.find { it.name == 'onlyPing' && !it.synthetic }
        def stat = child.methods.find { it.name == 'stat' && !it.synthetic }
        def use = child.methods.find { it.name == 'use' && !it.synthetic }
        def text = doc.text

        assert hierarchy.prepareTypeHierarchy(buffer, snap, at(text, 'onlyPing'), ENC).isEmpty()
        def childItem = hierarchy.prepareTypeHierarchy(buffer, snap, nameIn(text, 'class Child'), ENC)
        assert childItem*.name == ['Child']
        def supers = hierarchy.supertypes(childItem[0], snap, ENC)*.name
        assert supers.contains('Base')
        assert supers.contains('Face')
        assert !supers.contains('Object')

        def betaItem = hierarchy.prepareTypeHierarchy(buffer, snap, nameIn(text, 'class Beta'), ENC)[0]
        assert hierarchy.supertypes(betaItem, snap, ENC)*.name == ['Alpha']

        def missing = new TypeHierarchyItem('Missing', SymbolKind.Class, uri, SymbolService.emptyRange(), SymbolService.emptyRange())
        missing.data = [name: 'missing.Missing']
        assert hierarchy.supertypes(missing, snap, ENC).isEmpty()
        def nullOnly = new CompiledDocument(URI.create('file:///null-module.groovy'), 1, '', null, null, null)
        def nullSnap = new CompilationSnapshot([(nullOnly.uri): nullOnly])
        assert hierarchy.supertypes(childItem[0], nullSnap, ENC).isEmpty()

        def pingItem = hierarchy.prepareCallHierarchy(buffer, snap, at(text, 'onlyPing'), ENC)
        assert pingItem*.name == ['onlyPing']
        def statItem = hierarchy.prepareCallHierarchy(buffer, snap, at(text, 'stat'), ENC)
        assert statItem*.name == ['stat']
        def useItem = hierarchy.prepareCallHierarchy(buffer, snap, at(text, 'use()'), ENC)[0]
        assert hierarchy.incomingCalls(pingItem[0], snap, ENC)*.from.name == ['use']
        assert hierarchy.incomingCalls(statItem[0], snap, ENC)*.from.name.contains('use')
        def staticCalls = []
        AstQuery.walk(use) { node, ctx ->
            if (node instanceof StaticMethodCallExpression) {
                staticCalls << node.method
            }
        }
        assert staticCalls.contains('stat')

        def helper = new MethodNode('helper', Modifier.PUBLIC, ClassHelper.OBJECT_TYPE, Parameter.EMPTY_ARRAY,
                ClassNode.EMPTY_ARRAY, new EmptyStatement())
        def external = new ClassNode('ext.Util', Modifier.PUBLIC, ClassHelper.OBJECT_TYPE)
        external.addMethod(helper)
        helper.lineNumber = 4
        helper.columnNumber = 5
        helper.lastLineNumber = 4
        helper.lastColumnNumber = 11
        def helperCall = callsNamed(use, 'helper')[0]
        helperCall.methodTarget = helper
        def ctorCalls = []
        AstQuery.walk(use) { node, ctx ->
            if (node instanceof ConstructorCallExpression) {
                ctorCalls << node
            }
        }
        assert ctorCalls
        def userCtor = child.declaredConstructors.find { !it.synthetic && it.lineNumber > 0 }
        if (userCtor != null) {
            child.declaredConstructors.remove(userCtor)
            child.declaredConstructors.add(0, userCtor)
        }

        def outgoing = hierarchy.outgoingCalls(useItem, snap, ENC)*.to.name
        assert outgoing.contains('stat')
        assert outgoing.contains('onlyPing')
        assert !outgoing.contains('helper')
        assert !outgoing.contains('<init>')

        def bare = new CallHierarchyItem(pingItem[0].name, pingItem[0].kind, pingItem[0].uri, pingItem[0].range, pingItem[0].selectionRange)
        assert hierarchy.incomingCalls(bare, snap, ENC)*.from.name.contains('use')
        def broken = new CallHierarchyItem(useItem.name, useItem.kind, useItem.uri, useItem.range, useItem.selectionRange)
        broken.data = useItem.data
        broken.uri = '%'
        assert hierarchy.outgoingCalls(broken, snap, ENC)*.to.name.contains('stat')

        def caller = callerOf(onlyPing)
        def extra = plus(snap, nullOnly, caller)
        def withCaller = hierarchy.incomingCalls(pingItem[0], extra, ENC)
        assert withCaller*.from.name.contains('use')
        assert withCaller.every { it.from.uri }

        def pingCall = callsNamed(use, 'onlyPing')[0]
        pingCall.lineNumber = -1
        pingCall.lastLineNumber = -1
        pingCall.method.lineNumber = -1
        pingCall.method.lastLineNumber = -1
        def after = hierarchy.outgoingCalls(useItem, snap, ENC)*.to.name
        assert after.contains('stat')
        assert !after.contains('onlyPing')
        assert hierarchy.incomingCalls(pingItem[0], snap, ENC).isEmpty()

        child.interfaces?.each { it.lineNumber = 0 }
        def withoutFace = hierarchy.supertypes(childItem[0], snap, ENC)*.name
        assert withoutFace.contains('Base')
        assert !withoutFace.contains('Face') : child.interfaces.collect { [it.name, it.lineNumber] }
        def base = child.superClass
        base.setName('NotInSource')
        def hiddenBase = hierarchy.supertypes(childItem[0], snap, ENC)*.name
        assert !hiddenBase.contains('Base')
        assert !hiddenBase.contains('NotInSource')
        def externalType = new ClassNode('ext.External', Modifier.PUBLIC, ClassHelper.OBJECT_TYPE)
        externalType.lineNumber = 1
        externalType.columnNumber = 1
        externalType.lastLineNumber = 1
        externalType.lastColumnNumber = 8
        child.superClass = externalType
        assert hierarchy.supertypes(childItem[0], snap, ENC).isEmpty()
    }

    @Test
    void documentLinksWorkspaceStarAndNonFileTargets() {
        def widgetUri = open('demo/Widget.groovy', '''\
            package demo
            class Widget {}
            '''.stripIndent())
        def useUri = open('demo/Use.groovy', '''\
            package demo
            import demo.Widget
            import java.util.*
            class Use {
                Widget item
            }
            '''.stripIndent())
        def use = compiled(useUri)
        def widget = compiled(widgetUri)
        def links = new DocumentLinkService().documentLinks(use, snapshot(), ENC)
        assert links*.target == [widget.uri.toString()]

        use.module.addImport('Ghost', ClassHelper.make('demo.Ghost'))
        assert use.module.imports[-1].lineNumber <= 0
        assert new DocumentLinkService().documentLinks(use, snapshot(), ENC)*.target == [widget.uri.toString()]

        def nullModule = new CompiledDocument(URI.create('file:///null-module.groovy'), 1, '', null, null, null)
        def skipped = new DocumentLinkService().documentLinks(use, new CompilationSnapshot([(nullModule.uri): nullModule]), ENC)
        assert skipped.size() == 1
        assert skipped[0].target.endsWith('demo/Widget.groovy')
        assert skipped[0].target != widget.uri.toString()

        def unresolved = new DocumentLinkService().documentLinks(use, null, ENC)
        assert unresolved.size() == 1
        assert unresolved[0].target.endsWith('demo/Widget.groovy')

        def http = importDocument(URI.create('http://example.com/A.groovy'), 'import demo.Missing\n', 'demo.Missing')
        assert new DocumentLinkService().documentLinks(http, CompilationSnapshot.EMPTY, ENC).isEmpty()
        def root = importDocument(URI.create('file:///'), 'import demo.Missing\n', 'demo.Missing')
        assert new DocumentLinkService().documentLinks(root, CompilationSnapshot.EMPTY, ENC).isEmpty()
    }

    @Test
    void symbolIdentityClassExpressionAndNullNode() {
        def expr = new ClassExpression(ClassHelper.STRING_TYPE)
        def identity = SymbolIdentity.of(expr, CompilationSnapshot.EMPTY)
        assert identity.kind() == SymbolIdentity.Kind.TYPE
        assert identity.name() == 'String'
        assert identity.refersTo(new ClassExpression(ClassHelper.STRING_TYPE), null)
        assert !identity.refersTo(new ClassExpression(ClassHelper.Integer_TYPE), null)
        assert !identity.refersTo(null, null)
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

    private static CompiledDocument importDocument(URI uri, String text, String className) {
        def module = new ModuleNode((CompileUnit) null)
        module.addImport(className.substring(className.lastIndexOf('.') + 1), ClassHelper.make(className))
        def imp = module.imports[0]
        place(imp, 1, 1, text.length())
        new CompiledDocument(uri, 1, text, module, null, null)
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

    private static CompiledDocument callerOf(MethodNode target) {
        def text = target.name + '()\n'
        def call = new MethodCallExpression(new VariableExpression('this'), target.name, MethodCallExpression.NO_ARGUMENTS)
        call.methodTarget = target
        call.implicitThis = true
        place(call, 1, 1, 1 + target.name.length())
        place(call.method, 1, 1, 1 + target.name.length())
        def caller = new MethodNode('caller', Modifier.PUBLIC, ClassHelper.OBJECT_TYPE, Parameter.EMPTY_ARRAY,
                ClassNode.EMPTY_ARRAY, new ExpressionStatement(call))
        place(caller, 1, 1, text.length())
        def type = new ClassNode('Caller', Modifier.PUBLIC, ClassHelper.OBJECT_TYPE)
        place(type, 1, 1, text.length())
        type.addMethod(caller)
        def module = new ModuleNode((CompileUnit) null)
        module.addClass(type)
        new CompiledDocument(null, 1, text, module, null, null)
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

    private static Position nameIn(String text, String declaration) {
        def position = at(text, declaration)
        int space = declaration.lastIndexOf(' ')
        position.character += space + 1
        position
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
    void completionNavigationRenameAndHierarchy(@TempDir Path folder) {
        fixture = new LspFixture()
        def widgetA = folder.resolve('a')
        def widgetB = folder.resolve('b')
        Files.createDirectories(widgetA)
        Files.createDirectories(widgetB)
        def alpha = '''\
            package a
            class Alpha {}
            '''.stripIndent()
        def beta = '''\
            package b
            class Beta {}
            '''.stripIndent()
        def widget1 = '''\
            package a
            class Widget {}
            '''.stripIndent()
        def widget2 = '''\
            package b
            class Widget {}
            '''.stripIndent()
        fixture.open(widgetA.resolve('Alpha.groovy').toString(), alpha)
        fixture.open(widgetB.resolve('Beta.groovy').toString(), beta)
        fixture.open(widgetA.resolve('Widget.groovy').toString(), widget1)
        fixture.open(widgetB.resolve('Widget.groovy').toString(), widget2)
        def use = '''\
            class Use {
                Alpha a
                Beta b
                Widget w
            }
            '''.stripIndent()
        def useUri = fixture.open(folder.resolve('Use.groovy').toString(), use)
        def snapshot = fixture.server.context.snapshot
        def useDoc = snapshot.get(useUri)
        def diagnostics = fixture.publishedDiagnostics().findAll {
            it.message != null && it.message.isLeft() && it.message.getLeft().toLowerCase().contains('unable to resolve class')
        }
        def actions = new CodeActionService().collect(useDoc.toTextDocument(), snapshot, diagnostics,
                new Range(new Position(0, 0), new Position(5, 0)), UTF16, [], [])
        assert actions.any { it.isRight() && it.getRight().title.startsWith('Add import for a.Alpha') }
        assert actions.any { it.isRight() && it.getRight().title == 'Add all unambiguous imports' }
        assert actions.any { it.isRight() && (it.getRight().title.startsWith('Add import for a.Widget') || it.getRight().title.startsWith('Add import for b.Widget')) }

        def completion = new CompletionService()
        def gdkDoc = new TextDocument(URI.create('file:///gdk.groovy'), 'groovy', 1, 'foo.each')
        def gdk = completion.complete(gdkDoc, CompilationSnapshot.EMPTY, new Position(0, 8), UTF16)
        assert gdk.items.any { it.detail == 'GDK' }
        assert completion.complete(null, snapshot, new Position(0, 0), UTF16).items.any { it.kind.toString() == 'Keyword' }
        assert CompletionService.prefixAt(null, null, UTF16) == ''
        assert !CompletionService.isMemberAccess(null, null, UTF16)
        assert CompletionService.prefixAt(gdkDoc, new Position(0, 80), UTF16).contains('each')
        assert CompletionService.stringMap('nope').isEmpty()
        assert CompletionService.stringMap(null).isEmpty()
        assert CompletionService.stringMap([k: null, n: 'A']) == [n: 'A']
        assert completion.resolve(null, snapshot) == null
        def bare = new CompletionItem('x')
        assert completion.resolve(bare, null).is(bare)

        def boxText = '''\
            /**
             * A box.
             */
            class Box {
                String name
                static void ping(String name, int count) {}
                /**
                 * Builds a box.
                 */
                Box(String name, int count) { this.name = name }
                void use() {
                    ping(name: 'a', )
                    Box.ping( )
                    new Box( )
                }
            }
            interface Work { void run(String name, int count) }
            class Job implements Work {
                void run(String name, int count) {}
            }
            enum Hue { RED }
            '''.stripIndent()
        def boxUri = fixture.open('Box.groovy', boxText)
        def boxCompiled = fixture.server.context.snapshot.get(boxUri)
        assert boxCompiled.module != null : boxCompiled.errors
        def boxDoc = boxCompiled.toTextDocument()
        def member = afterLast(boxText, 'Box.')
        def inside = completion.complete(boxDoc, fixture.server.context.snapshot, member, UTF16)
        assert inside.items != null
        assert CompletionService.isMemberAccess(boxDoc, member, UTF16)
        def named = completion.complete(boxDoc, fixture.server.context.snapshot, after(boxText, "ping(name: 'a', "), UTF16)
        assert named.items.any { it.kind.toString() == 'Keyword' }
        def hueLine = boxText.readLines().findIndexOf { it.contains('enum Hue') }
        def hueColumn = boxText.readLines()[hueLine].indexOf('Hue') + 2
        def typeItems = completion.complete(boxDoc, fixture.server.context.snapshot, new Position(hueLine, hueColumn), UTF16)
        assert typeItems.items.any { it.label == 'Hue' && it.kind.toString() == 'Enum' }
        def boxType = boxCompiled.module.classes.find { it.name == 'Box' }
        def ping = boxType.methods.find { it.name == 'ping' && it.lineNumber > 0 }
        def item = new CompletionItem('ping')
        item.data = [k: 'm', n: 'ping', o: ping.declaringClass.name, d: ping.typeDescriptor]
        def resolved = completion.resolve(item, fixture.server.context.snapshot)
        assert resolved.documentation == null || resolved.documentation.getRight().value != null
        def typeItem = new CompletionItem('Box')
        typeItem.data = [k: 't', n: boxType.name]
        def resolvedType = completion.resolve(typeItem, fixture.server.context.snapshot)
        assert resolvedType.documentation.getRight().value.contains('A box')

        def hello = folder.resolve('Hello.groovy')
        def helloText = '''\
            class Hello {
                Hello field
                void run() { new Hello() }
                static void util(int n) { util(1) }
            }
            '''.stripIndent()
        def helloUri = fixture.open(hello.toString(), helloText)
        def helloSnapshot = fixture.server.context.snapshot
        def helloDoc = helloSnapshot.get(helloUri).toTextDocument()
        def navigation = new NavigationService()
        def classPos = caret(helloText, 'class Hello')
        assert navigation.declaration(helloDoc, helloSnapshot, classPos, UTF16)
        assert navigation.implementation(helloDoc, helloSnapshot, classPos, UTF16) != null
        assert navigation.superMethod(helloDoc, helloSnapshot, caret(helloText, 'void run'), UTF16).isEmpty()
        assert navigation.references(null, helloSnapshot, classPos, UTF16, true).isEmpty()
        def multi = '''\
            class Multi {
                def split() {
                    def (left, right) = [1, 2]
                    left
                }
            }
            '''.stripIndent()
        def multiUri = fixture.open('Multi.groovy', multi)
        def multiDoc = fixture.server.context.snapshot.get(multiUri).toTextDocument()
        assert navigation.definition(multiDoc, fixture.server.context.snapshot, caret(multi, 'def (left'), UTF16) != null

        def rename = new RenameService()
        assert rename.prepareRename(null, helloSnapshot, classPos, UTF16) == null
        assert rename.rename(helloDoc, helloSnapshot, classPos, ' ', UTF16).changes.isEmpty()
        def renamed = rename.rename(helloDoc, helloSnapshot, caret(helloText, 'class Hello'), 'Greeting', UTF16)
        assert renamed.documentChanges.any { it.isRight() && it.getRight() instanceof RenameFile }
        assert rename.willRenameFiles(null, helloSnapshot, UTF16).changes != null
        assert rename.willRenameFiles([null], helloSnapshot, UTF16).changes.isEmpty()
        def javaRename = new FileRename(helloUri, helloUri.replace('.groovy', '.java'))
        assert rename.willRenameFiles([javaRename], helloSnapshot, UTF16).changes.isEmpty()
        def otherDir = new FileRename(helloUri, URI.create('file:///elsewhere/Greeting.groovy').toString())
        assert rename.willRenameFiles([otherDir], helloSnapshot, UTF16).changes.isEmpty()
        def badName = new FileRename(helloUri, helloUri.replace('Hello.groovy', '1Bad.groovy'))
        assert rename.willRenameFiles([badName], helloSnapshot, UTF16).changes.isEmpty()
        def good = new FileRename(helloUri, helloUri.replace('Hello.groovy', 'Greeting.groovy'))
        def moved = rename.willRenameFiles([good], helloSnapshot, UTF16)
        assert moved.changes.values().flatten().any { it.newText == 'Greeting' }
        assert RenameService.isIdentifier('Name')
        assert !RenameService.isIdentifier(null)
        assert !RenameService.isIdentifier('')
        assert !RenameService.isIdentifier('1a')
        assert !RenameService.isIdentifier('a-b')

        def hierarchy = new HierarchyService()
        def prepared = hierarchy.prepareTypeHierarchy(helloDoc, helloSnapshot, caret(helloText, 'class Hello'), UTF16)
        assert prepared
        assert hierarchy.supertypes(prepared[0], helloSnapshot, UTF16) != null
        assert hierarchy.subtypes(prepared[0], helloSnapshot, UTF16) != null
        assert hierarchy.incomingCalls(null, helloSnapshot, UTF16).isEmpty()
        assert hierarchy.prepareTypeHierarchy(null, helloSnapshot, classPos, UTF16).isEmpty()
        def callItems = fixture.server.textDocumentService.prepareCallHierarchy(
                new CallHierarchyPrepareParams(new TextDocumentIdentifier(helloUri),
                        caret(helloText, 'void util'))).get()
        if (callItems) {
            assert hierarchy.incomingCalls(callItems[0], helloSnapshot, UTF16) != null
            assert hierarchy.outgoingCalls(callItems[0], helloSnapshot, UTF16) != null
        }
        def bad = new CallHierarchyItem('util', SymbolKind.Method, ':::',
                new Range(new Position(0, 0), new Position(0, 1)),
                new Range(new Position(0, 0), new Position(0, 1)))
        bad.data = [:]
        assert hierarchy.outgoingCalls(bad, helloSnapshot, UTF16)*.to.name.contains('util')

        AstQuery.walk(helloSnapshot.get(helloUri).module) { node, ctx ->
            def identity = SymbolIdentity.of(node, helloSnapshot)
            if (identity != null) {
                assert identity.refersTo(node, ctx) || !identity.refersTo(null, ctx)
            }
            SymbolIdentity.keyOf(node)
        }
        assert !SymbolIdentity.isResolvedType(null)
        assert !SymbolIdentity.isResolvedType(ClassHelper.OBJECT_TYPE)
        assert SymbolIdentity.isResolvedType(ClassHelper.STRING_TYPE)
        assert SymbolService.kindOf(new Parameter(ClassHelper.STRING_TYPE, 'p')) == SymbolKind.Variable
        assert SymbolService.kindOf(hueNode(boxCompiled)) == SymbolKind.Enum
        assert new SymbolService().workspaceSymbols(null, 'Box', UTF16).isEmpty()
        assert new SymbolService().documentSymbols(null, UTF16).isEmpty()
        assert new SymbolService().documentSymbolInformation(boxCompiled, UTF16).any { it.name == 'Box' }
    }

    private static ClassNode hueNode(CompiledDocument document) {
        document.module.classes.find { it.name == 'Hue' }
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

    private static Position afterLast(String text, String token) {
        int idx = text.lastIndexOf(token)
        assert idx >= 0 : token
        int line = 0
        int character = 0
        int end = idx + token.length()
        for (int i = 0; i < end; i++) {
            if (text.charAt(i) == '\n') {
                line++
                character = 0
            } else {
                character++
            }
        }
        new Position(line, character)
    }

    private static void place(ASTNode node, int line, int column, int lastColumn) {
        node.lineNumber = line
        node.columnNumber = column
        node.lastLineNumber = line
        node.lastColumnNumber = lastColumn
    }

}
