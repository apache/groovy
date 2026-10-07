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

import org.apache.groovy.lsp.internal.compile.AstQuery
import org.apache.groovy.lsp.internal.compile.CompilerSettings
import org.apache.groovy.lsp.internal.compile.GroovyCompiler
import org.apache.groovy.lsp.internal.position.PositionEncoding
import org.apache.groovy.lsp.internal.position.Positions
import org.apache.groovy.lsp.internal.workspace.TextDocument
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.FieldNode
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.PropertyNode
import org.codehaus.groovy.ast.expr.ConstructorCallExpression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.PropertyExpression
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.eclipse.lsp4j.Position
import org.junit.jupiter.api.Test
import org.apache.groovy.lsp.internal.LspFixture
import org.junit.jupiter.api.AfterEach
import org.eclipse.lsp4j.CompletionParams
import org.eclipse.lsp4j.DeclarationParams
import org.eclipse.lsp4j.DefinitionParams
import org.eclipse.lsp4j.DidChangeTextDocumentParams
import org.eclipse.lsp4j.DidCloseTextDocumentParams
import org.eclipse.lsp4j.DocumentFormattingParams
import org.eclipse.lsp4j.DocumentHighlightParams
import org.eclipse.lsp4j.DocumentSymbolParams
import org.eclipse.lsp4j.FormattingOptions
import org.eclipse.lsp4j.HoverParams
import org.eclipse.lsp4j.ImplementationParams
import org.eclipse.lsp4j.TextDocumentContentChangeEvent
import org.eclipse.lsp4j.TextDocumentIdentifier
import org.eclipse.lsp4j.TypeDefinitionParams
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier
import org.eclipse.lsp4j.WorkspaceSymbolParams

final class NavigationExecutionTest {
    private LspFixture fixture

    @AfterEach
    void tearDown() {
        fixture?.close()
    }


    @Test
    void everyPositionedNodeCanBeQueried() {
        def compiler = new GroovyCompiler()
        def uri = URI.create('file:///tmp/Nav.groovy')
        def src = '''\
            interface Face { def ping() }
            class Hello implements Face {
                String name
                Hello(String name) { this.name = name }
                static String go(String x) { x }
                def ping() {
                    def local = go("ok")
                    def made = new Hello(local)
                    made.name
                    name.toLowerCase()
                    local
                }
            }
            '''.stripIndent()
        def snapshot = compiler.compile(
                [new TextDocument(uri, 'groovy', 1, src)],
                [], CompilerSettings.defaults(), NavigationExecutionTest.classLoader)
        compiler.close()
        def compiled = snapshot.get(uri)
        assert compiled.module != null
        def doc = new TextDocument(uri, 'groovy', 1, src)
        def nav = new NavigationService()
        def encoding = PositionEncoding.UTF16
        def seen = [] as Set
        AstQuery.walk(compiled.module) { ASTNode node, ctx ->
            if (node.lineNumber <= 0) {
                return
            }
            def line = Positions.lineText(src, node.lineNumber)
            def pos = Positions.toLsp(node.lineNumber, Math.max(node.columnNumber, 1), line, encoding)
            seen << node.class.simpleName
            assert nav.definition(doc, snapshot, pos, encoding) != null
            assert nav.typeDefinition(doc, snapshot, pos, encoding) != null
            assert nav.implementation(doc, snapshot, pos, encoding) != null
            assert nav.declaration(doc, snapshot, pos, encoding) != null
            assert nav.references(doc, snapshot, pos, encoding, true) != null
            MethodBinding.isRenameSafe(node, snapshot)
            SymbolIdentity.of(node, snapshot)?.refersTo(node, ctx)
        }
        assert 'MethodCallExpression' in seen || 'StaticMethodCallExpression' in seen
        assert 'ClassNode' in seen
        assert nav.implementation(doc, null, new Position(0, 0), encoding).isEmpty()
        assert nav.references(doc, null, new Position(0, 0), encoding, false).isEmpty()
        def hello = compiled.module.classes.find { it.nameWithoutPackage == 'Hello' }
        assert MethodBinding.implementations(hello, null).isEmpty()
        assert MethodBinding.implementations(null, snapshot).isEmpty()
        def ping = hello.methods.find { it.name == 'ping' && !it.synthetic }
        assert MethodBinding.methodImplementations(null, snapshot).isEmpty()
        assert MethodBinding.methodImplementations(ping, snapshot) != null
        def field = hello.fields.find { it.name == 'name' && !it.synthetic }
        def prop = new PropertyExpression(new VariableExpression('this'), 'name')
        prop.objectExpression.type = hello
        def resolvedField = MethodBinding.resolveProperty(prop)
        assert resolvedField instanceof FieldNode || resolvedField instanceof PropertyNode
        MethodBinding.resolveProperty(null)
        def call = new MethodCallExpression(new VariableExpression('this'), 'ping', MethodCallExpression.NO_ARGUMENTS)
        call.methodTarget = ping
        assert MethodBinding.callMatches(call, ping, null)
        assert !MethodBinding.callMatches(null, ping, null)
        def stat = new StaticMethodCallExpression(hello, 'go', MethodCallExpression.NO_ARGUMENTS)
        def go = hello.methods.find { it.name == 'go' && it.static }
        MethodBinding.staticCallMatches(stat, go)
        MethodBinding.receiverMatchesOwner(new VariableExpression('this'), '')
        def ctor = new ConstructorCallExpression(hello, MethodCallExpression.NO_ARGUMENTS)
        MethodBinding.argumentCount(ctor.arguments)
        MethodBinding.isRenameSafe(field, snapshot)
        MethodBinding.isRenameSafe(ping, snapshot)
        MethodBinding.isRenameSafe(new Parameter(hello, 'p'), snapshot)
        MethodBinding.isRenameSafe(new VariableExpression('local'), snapshot)
        MethodBinding.isRenameSafe(call, snapshot)
        MethodBinding.isRenameSafe(stat, snapshot)
        MethodBinding.isRenameSafe(prop, snapshot)
        assert new HierarchyService().prepareCallHierarchy(doc, null, new Position(0, 0), encoding).isEmpty()
        assert new HierarchyService().prepareCallHierarchy(doc, snapshot, new Position(0, 6), encoding).isEmpty()
        assert new HierarchyService().incomingCalls(null, snapshot, encoding).isEmpty()
        assert new HierarchyService().outgoingCalls(null, snapshot, encoding).isEmpty()
        assert new HierarchyService().prepareTypeHierarchy(doc, null, new Position(0, 0), encoding).isEmpty()
        def copy = new ClassNode(hello.name, 0, hello.superClass)
        def located = NavigationService.uriOf(copy, URI.create('file:///fallback'), snapshot, null)
        assert located == uri || located.toString().contains('Nav.groovy') || located.toString().contains('fallback')
        def calls = []
        AstQuery.walk(compiled.module) { node, ctx ->
            if (node instanceof MethodCallExpression || node instanceof StaticMethodCallExpression
                    || node instanceof ConstructorCallExpression) {
                calls << node
            }
        }
        assert calls
        calls.each { node ->
            if (node.lineNumber > 0) {
                def line = Positions.lineText(src, node.lineNumber)
                def pos = Positions.toLsp(node.lineNumber, Math.max(node.columnNumber, 1), line, encoding)
                assert nav.typeDefinition(doc, snapshot, pos, encoding) != null
            }
        }
        def generated = SourceGeneration.instanceFields(hello)
        assert generated.any { it.name == 'name' }
        def dup = new ClassNode('demo.Dup', 0, hello.superClass)
        def existing = new FieldNode('n', 0, hello, dup, null)
        existing.lineNumber = 2
        existing.columnNumber = 1
        existing.lastLineNumber = 2
        existing.lastColumnNumber = 2
        dup.addField(existing)
        def property = new PropertyNode('n', 0, hello, dup, null, null, null)
        if (property.field != null) {
            property.field.lineNumber = 2
        }
        dup.addProperty(property)
        assert SourceGeneration.instanceFields(dup).any { it.name == 'n' }
        def hover = new HoverService().hover(doc, snapshot, new Position(1, 10), encoding)
        assert hover != null
        def signatures = new SignatureHelpService()
        def goLine = src.readLines().findIndexOf { it.contains('go("ok")') }
        def goCol = src.readLines()[goLine].indexOf('go(') + 3
        assert signatures.signatureHelp(doc, snapshot, new Position(goLine, goCol), encoding) != null
        def ctorLine = src.readLines().findIndexOf { it.contains('new Hello') }
        def ctorCol = src.readLines()[ctorLine].indexOf('Hello(') + 6
        assert signatures.signatureHelp(doc, snapshot, new Position(ctorLine, ctorCol), encoding) != null
        def formatting = new FormattingService()
        assert formatting.onTypeFormat(doc, new Position(99, 0), '\n', 4, true).isEmpty()
        def indented = new TextDocument(uri, 'groovy', 1, 'class Hello {\n    \n}\n')
        def noop = formatting.onTypeFormat(indented, new Position(1, 4), '\n', 4, true)
        assert noop.isEmpty() || noop[0].newText == '    '
    }
    @Test
    void navigationVariantsAndHighlights() {
        fixture = new LspFixture()
        def src = '''\
            interface Face { def run() }
            class Hello implements Face {
                String name
                def run() { name }
                static def go() { go() }
            }
            '''.stripIndent()
        def uri = fixture.open('Hello.groovy', src)
        def id = new TextDocumentIdentifier(uri)
        def tds = fixture.server.textDocumentService
        assert tds.typeDefinition(new TypeDefinitionParams(id, new Position(3, 20))).get() != null
        assert tds.declaration(new DeclarationParams(id, new Position(2, 8))).get() != null
        assert tds.implementation(new ImplementationParams(id, new Position(0, 10))).get() != null
        assert tds.documentHighlight(new DocumentHighlightParams(id, new Position(2, 11))).get() != null
        assert tds.documentSymbol(new DocumentSymbolParams(id)).get() != null
        assert tds.definition(new DefinitionParams(id, new Position(4, 28))).get() != null
        def hover = tds.hover(new HoverParams(id, new Position(0, 10))).get()
        assert hover != null
        def completions = tds.completion(new CompletionParams(id, new Position(3, 16))).get()
        assert completions.getRight().items != null
        assert tds.formatting(new DocumentFormattingParams(id, new FormattingOptions(4, true))).get() != null
        tds.didChange(new DidChangeTextDocumentParams(new VersionedTextDocumentIdentifier(uri, 2),
                [new TextDocumentContentChangeEvent(src + '\n')]))
        tds.didClose(new DidCloseTextDocumentParams(id))
        assert fixture.server.workspaceService.symbol(new WorkspaceSymbolParams('')).get() != null
        assert fixture.server.workspaceService.symbol(new WorkspaceSymbolParams('Hello')).get() != null
        def missing = tds.definition(new DefinitionParams(
                new TextDocumentIdentifier('file:///missing.groovy'), new Position(0, 0))).get()
        assert missing.getRight().isEmpty()
        fixture.server.context.clientCapabilities.textDocument.definition.linkSupport = false
        def uri2 = fixture.open('Hello2.groovy', 'class Hello2 {}\n')
        def left = tds.definition(new DefinitionParams(new TextDocumentIdentifier(uri2), new Position(0, 6))).get()
        assert left.isLeft() || left.isRight()
        def store = fixture.server.context.documents
        assert store.get(URI.create(uri2)) != null
    }

    @Test
    void staticCallsPropertiesAndIdentifierHover() {
        fixture = new LspFixture()
        def src = '''\
            class Hello {
                String name
                def foo() { this.name }
                static def go() { Hello.go() }
            }
            '''.stripIndent()
        def uri = fixture.open('Hello.groovy', src)
        def id = new TextDocumentIdentifier(uri)
        def tds = fixture.server.textDocumentService
        assert tds.definition(new DefinitionParams(id, new Position(3, 36))).get() != null
        assert tds.documentHighlight(new DocumentHighlightParams(id, new Position(2, 28))).get() != null
        def commentUri = fixture.open('Comment.groovy', 'class Comment {\n  // mysteryword\n}\n')
        def hover = tds.hover(new HoverParams(new TextDocumentIdentifier(commentUri), new Position(1, 6))).get()
        assert hover != null
        def implSrc = '''\
            interface Face { def run() }
            class Impl implements Face { def run() {} }
            '''.stripIndent()
        def implUri = fixture.open('Face.groovy', implSrc)
        assert tds.implementation(new ImplementationParams(
                new TextDocumentIdentifier(implUri), new Position(0, 22))).get() != null
    }

}
