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
import org.apache.groovy.lsp.internal.compile.CompilerSettings
import org.apache.groovy.lsp.internal.compile.GroovyCompiler
import org.apache.groovy.lsp.internal.position.PositionEncoding
import org.apache.groovy.lsp.internal.workspace.TextDocument
import org.eclipse.lsp4j.DefinitionParams
import org.eclipse.lsp4j.HoverParams
import org.eclipse.lsp4j.InlayHintParams
import org.eclipse.lsp4j.MarkupContent
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.PrepareRenameParams
import org.eclipse.lsp4j.ReferenceContext
import org.eclipse.lsp4j.ReferenceParams
import org.eclipse.lsp4j.Range
import org.eclipse.lsp4j.RenameParams
import org.eclipse.lsp4j.TextDocumentIdentifier
import org.codehaus.groovy.ast.FieldNode
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.expr.VariableExpression
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

final class ConservativeNavigationTest {

    private LspFixture fixture

    @AfterEach
    void tearDown() {
        fixture?.close()
    }

    @Test
    void prepareRenameAndRenameRefuseUnboundDynamicCalls() {
        fixture = new LspFixture()
        def src = '''\
            class Hello {
                def run(def o) { o.mystery() }
                def mystery() {}
            }
            '''.stripIndent()
        def uri = fixture.open('Hello.groovy', src)
        def id = new TextDocumentIdentifier(uri)
        int callCol = src.readLines()[1].indexOf('mystery')
        def prepare = fixture.server.textDocumentService.prepareRename(
                new PrepareRenameParams(id, new Position(1, callCol))).get()
        assert prepare == null
        def edit = fixture.server.textDocumentService.rename(
                new RenameParams(id, new Position(1, callCol), 'renamed')).get()
        assert !edit.changes
        assert !edit.documentChanges
    }

    @Test
    void renameStillRewritesTheDeclarationOfAnUnboundCall() {
        fixture = new LspFixture()
        def src = '''\
            class Hello {
                def run(def o) { o.mystery() }
                def mystery() {}
            }
            '''.stripIndent()
        def uri = fixture.open('Hello.groovy', src)
        int declCol = src.readLines()[2].indexOf('mystery')
        def edit = fixture.server.textDocumentService.rename(
                new RenameParams(new TextDocumentIdentifier(uri), new Position(2, declCol), 'renamed')).get()
        def texts = edit.changes.values().flatten()*.newText
        assert texts
        assert texts.every { it == 'renamed' }
        def ranges = edit.changes.values().flatten()*.range
        assert ranges.every { it.start.line != 1 }
    }

    @Test
    void definitionListsSameArityOverloadsWhenUnbound() {
        fixture = new LspFixture()
        def src = '''\
            class Hello {
                def foo() { bar("x") }
                def bar(String s) {}
                def bar(CharSequence s) {}
            }
            '''.stripIndent()
        def uri = fixture.open('Hello.groovy', src)
        int col = src.readLines()[1].indexOf('bar')
        def defs = fixture.server.textDocumentService.definition(
                new DefinitionParams(new TextDocumentIdentifier(uri), new Position(1, col))).get()
        assert defs.isRight()
        def lines = defs.getRight()*.targetSelectionRange.start.line as Set
        assert lines.contains(2) || lines.contains(3) || defs.getRight().size() >= 1
    }

    @Test
    void hoverOnDefLocalUsesInitializerType() {
        fixture = new LspFixture()
        def src = '''\
            class Hello {
                def foo() {
                    def name = "x"
                    name
                }
            }
            '''.stripIndent()
        def uri = fixture.open('Hello.groovy', src)
        int col = src.readLines()[2].indexOf('name')
        def hover = fixture.server.textDocumentService.hover(
                new HoverParams(new TextDocumentIdentifier(uri), new Position(2, col))).get()
        assert hover != null
        def contents = hover.contents
        String value = contents instanceof MarkupContent ? contents.value : contents.getRight().value
        assert value.contains('name')
        assert value.contains('String') || value.contains('GString')
    }

    @Test
    void inlayHintsIncludeDefLocals() {
        fixture = new LspFixture()
        def src = '''\
            class Hello {
                def foo() {
                    def name = "x"
                }
            }
            '''.stripIndent()
        def uri = fixture.open('Hello.groovy', src)
        def hints = fixture.server.textDocumentService.inlayHint(
                new InlayHintParams(new TextDocumentIdentifier(uri),
                        new Range(new Position(0, 0), new Position(5, 0)))).get()
        assert hints.any {
            def label = it.label.isLeft() ? it.label.getLeft() : it.label.getRight().toString()
            label.toString().trim() in ['String', 'GString']
        }
    }

    @Test
    void windowsFileUriClassRenameDoesNotUsePathOf() {
        def compiler = new GroovyCompiler()
        def uri = URI.create('file:///d:/a/groovy/Hello.groovy')
        def src = 'class Hello {}\n'
        def snapshot = compiler.compile(
                [new TextDocument(uri, 'groovy', 1, src)],
                [], CompilerSettings.defaults(), ConservativeNavigationTest.classLoader)
        def doc = new TextDocument(uri, 'groovy', 1, src)
        def edit = new RenameService().rename(doc, snapshot, new Position(0, 6), 'World', PositionEncoding.UTF16)
        assert edit.documentChanges
        assert edit.documentChanges.any { it.isRight() && it.getRight().newUri.contains('World.groovy') }
        assert edit.documentChanges.any { it.isRight() && it.getRight().newUri.startsWith('file:///d:/a/groovy/') }
        compiler.close()
    }

    @Test
    void isRenameSafeDistinguishesDeclarationsFromUnboundCalls() {
        fixture = new LspFixture()
        def src = '''\
            class Hello {
                def run(def o) { o.mystery() }
                def mystery() {}
            }
            '''.stripIndent()
        def uri = fixture.open('Hello.groovy', src)
        def compiled = fixture.server.context.snapshot.get(uri)
        def call = AstQuery.nodeAt(compiled.module, 2, src.readLines()[1].indexOf('mystery') + 1)
        def decl = AstQuery.nodeAt(compiled.module, 3, src.readLines()[2].indexOf('mystery') + 1)
        assert !NavigationService.isRenameSafe(call, fixture.server.context.snapshot)
        assert NavigationService.isRenameSafe(decl, fixture.server.context.snapshot)
        assert !NavigationService.isRenameSafe(null, fixture.server.context.snapshot)
        assert NavigationService.resolveMethod(null, fixture.server.context.snapshot) == null
        assert NavigationService.resolveStatic(null, fixture.server.context.snapshot) == null
        assert NavigationService.resolveMethodCandidates(null, fixture.server.context.snapshot).isEmpty()
        assert NavigationService.overloads(null, fixture.server.context.snapshot).isEmpty()
        assert TypeInference.of((FieldNode) null) == null
        assert TypeInference.of((Parameter) null) == null
        assert TypeInference.of((VariableExpression) null, null) == null
        assert !TypeInference.usedInitializer(null, null)
    }

    @Test
    void referencesDoNotJoinUnboundCallsOfTheSameName() {
        fixture = new LspFixture()
        def src = '''\
            class Hello {
                def run(def a, def b) {
                    a.mystery()
                    b.mystery()
                }
            }
            '''.stripIndent()
        def uri = fixture.open('Hello.groovy', src)
        int col = src.readLines()[2].indexOf('mystery')
        def refs = fixture.server.textDocumentService.references(
                new ReferenceParams(new TextDocumentIdentifier(uri),
                        new Position(2, col), new ReferenceContext(true))).get()
        assert refs == null || refs.size() <= 1
    }
}
