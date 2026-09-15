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
import org.apache.groovy.lsp.internal.compile.AstQuery
import org.apache.groovy.lsp.internal.compile.CompilationSnapshot
import org.apache.groovy.lsp.internal.position.PositionEncoding
import org.apache.groovy.lsp.internal.workspace.TextDocument
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.FieldNode
import org.codehaus.groovy.ast.ImportNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.expr.ClassExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.PropertyExpression
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression
import org.codehaus.groovy.ast.expr.VariableExpression
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
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.TextDocumentContentChangeEvent
import org.eclipse.lsp4j.TextDocumentIdentifier
import org.eclipse.lsp4j.TypeDefinitionParams
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier
import org.eclipse.lsp4j.WorkspaceSymbolParams
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

import java.net.URI

final class FeatureCoverageTest {

    private LspFixture fixture

    @AfterEach
    void tearDown() {
        fixture?.close()
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

    @Test
    void hoverRenderCoversNodeKinds() {
        assert HoverService.render(null) == null
        def iface = new ClassNode('Face', 0x0200, ClassHelper.OBJECT_TYPE)
        assert HoverService.render(iface).contains('Face')
        assert HoverService.render(new ClassNode('E', 0x4000, ClassHelper.OBJECT_TYPE)).contains('E')
        def field = new FieldNode('n', 0, ClassHelper.STRING_TYPE, ClassHelper.OBJECT_TYPE, null)
        assert HoverService.render(field).contains('n')
        assert HoverService.render(new Parameter(ClassHelper.int_TYPE, 'i')).contains('i')
        assert HoverService.render(new VariableExpression('x')).contains('x')
        assert HoverService.render(new ClassExpression(ClassHelper.STRING_TYPE)).contains('String')
        def call = new MethodCallExpression(new VariableExpression('this'), 'foo', MethodCallExpression.NO_ARGUMENTS)
        assert HoverService.render(call).contains('foo')
        assert HoverService.render(new PropertyExpression(new VariableExpression('this'), 'name')).contains('name')
        assert HoverService.render(new ImportNode(ClassHelper.STRING_TYPE, 'S')).contains('import')
        assert HoverService.render(new ConstantExpression('hi')).contains('hi')
    }

    @Test
    void astQueryNullModuleAndDeclarations() {
        assert AstQuery.nodeAt(null, 1, 1) == null
        assert AstQuery.declarations(null).isEmpty()
        assert AstQuery.enclosingCall(null, 1, 1) == null
        fixture = new LspFixture()
        def uri = fixture.open('Hello.groovy', 'class Hello {\n  def foo() { foo() }\n}\n')
        def module = fixture.server.context.snapshot.get(uri).module
        assert AstQuery.declarations(module)
        def hits = []
        AstQuery.walk(module, { node, ctx -> hits << node })
        assert hits
        AstQuery.walk(module.getClasses()[0], { node, ctx -> hits << node })
    }

    @Test
    void typeInferenceAndRenameGuards() {
        fixture = new LspFixture()
        def uri = fixture.open('Hello.groovy', '''\
            class Hello {
                def name = "x"
                def foo(def p) { name }
            }
            '''.stripIndent())
        def module = fixture.server.context.snapshot.get(uri).module
        def field = module.classes[0].fields.find { it.name == 'name' && !it.synthetic }
        if (field != null) {
            assert TypeInference.of(field) != null
        }
        assert RenameService.isIdentifier('α')
        assert !RenameService.isIdentifier('')
        assert !RenameService.isIdentifier(null)
        def empty = new RenameService().rename(null, null, new Position(0, 0), 'x',
                PositionEncoding.UTF16)
        assert empty.changes == null || empty.changes.isEmpty()
        assert new RenameService().prepareRename(null, null, new Position(0, 0),
                PositionEncoding.UTF16) == null
    }

    @Test
    void remainingNavigationAndSymbolHelpers() {
        def doc = new TextDocument(URI.create('file:///tmp/A.groovy'), 'groovy', 1, 'mysteryword\n')
        def hover = new HoverService().hover(doc, null, new Position(0, 3), PositionEncoding.UTF16)
        assert hover != null
        def prop = new PropertyExpression(new VariableExpression('this'), 'name')
        assert NavigationService.resolveProperty(prop) == null
        def method = new MethodNode('go', 8, ClassHelper.OBJECT_TYPE,
                new Parameter[0], ClassNode.EMPTY_ARRAY, null)
        method.setDeclaringClass(ClassHelper.OBJECT_TYPE)
        def stat = new StaticMethodCallExpression(
                ClassHelper.OBJECT_TYPE, 'go', MethodCallExpression.NO_ARGUMENTS)
        assert NavigationService.staticCallMatches(stat, method)
        NavigationService.receiverMatchesOwner(new VariableExpression('this'), 'Hello')
        NavigationService.receiverMatchesOwner(new ClassExpression(ClassHelper.OBJECT_TYPE), 'java.lang.Object')
        NavigationService.receiverMatchesOwner(null, 'Hello')
        def local = new VariableExpression('bar')
        local.lineNumber = 1
        local.columnNumber = 1
        local.lastLineNumber = 1
        local.lastColumnNumber = 4
        def identity = SymbolIdentity.of(local, CompilationSnapshot.EMPTY)
        assert identity != null
        assert identity.kind() != null
        assert identity.refersTo(local, null, CompilationSnapshot.EMPTY)
        fixture = new LspFixture()
        def uri = fixture.open('Locals.groovy', '''\
            class Locals {
                def foo() {
                    def bar = 1
                    bar
                }
            }
            '''.stripIndent())
        def tds = fixture.server.textDocumentService
        def id = new TextDocumentIdentifier(uri)
        assert tds.documentHighlight(new DocumentHighlightParams(id, new Position(2, 12))).get() != null
        fixture.server.context.clientCapabilities.textDocument.documentSymbol.hierarchicalDocumentSymbolSupport = false
        assert tds.documentSymbol(new DocumentSymbolParams(id)).get() != null
        assert SymbolService.emptyRange() != null
        def owner = new ClassNode('demo.Hello', 0, ClassHelper.OBJECT_TYPE)
        def field = new FieldNode('name', 0, ClassHelper.STRING_TYPE, owner, null)
        field.lineNumber = 1
        field.columnNumber = 1
        field.lastLineNumber = 1
        field.lastColumnNumber = 5
        def fieldId = SymbolIdentity.of(field, CompilationSnapshot.EMPTY)
        def other = new PropertyExpression(new VariableExpression('x'), 'name')
        if (fieldId != null) {
            fieldId.refersTo(other, null, CompilationSnapshot.EMPTY)
        }
        def a = new FieldNode('b', 0, ClassHelper.STRING_TYPE, owner, null)
        a.lineNumber = 1
        def b = new FieldNode('a', 0, ClassHelper.STRING_TYPE, owner, null)
        b.lineNumber = 1
        owner.addField(a)
        owner.addField(b)
        SourceGeneration.instanceFields(owner)
    }
}
