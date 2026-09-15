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
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.Range
import org.eclipse.lsp4j.SelectionRangeParams
import org.eclipse.lsp4j.SignatureHelpParams
import org.eclipse.lsp4j.TextDocumentIdentifier
import org.eclipse.lsp4j.jsonrpc.messages.Either
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

final class LanguageQualityTest {

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
}
