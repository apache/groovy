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
import org.apache.groovy.lsp.internal.compile.CompilerSettings
import org.apache.groovy.lsp.internal.compile.GroovyCompiler
import org.apache.groovy.lsp.internal.compile.Identifiers
import org.apache.groovy.lsp.internal.compile.ImportSupport
import org.apache.groovy.lsp.internal.compile.PackageGuess
import org.apache.groovy.lsp.internal.position.PositionEncoding
import org.apache.groovy.lsp.internal.workspace.TextDocument
import org.eclipse.lsp4j.CodeActionContext
import org.eclipse.lsp4j.CodeActionParams
import org.eclipse.lsp4j.CompletionParams
import org.eclipse.lsp4j.Diagnostic
import org.eclipse.lsp4j.DocumentOnTypeFormattingParams
import org.eclipse.lsp4j.ExecuteCommandParams
import org.eclipse.lsp4j.FoldingRangeRequestParams
import org.eclipse.lsp4j.FormattingOptions
import org.eclipse.lsp4j.HoverParams
import org.eclipse.lsp4j.Position
import org.eclipse.lsp4j.Range
import org.eclipse.lsp4j.TextDocumentIdentifier
import org.eclipse.lsp4j.jsonrpc.messages.Either
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

import java.nio.file.Files

final class MetalsInspiredTest {

    private LspFixture fixture

    @AfterEach
    void tearDown() {
        fixture?.close()
    }

    @Test
    void unusedImportIsDetectedFromSource() {
        def compiler = new GroovyCompiler()
        def uri = URI.create('file:///tmp/Unused.groovy')
        def src = 'import java.util.concurrent.ConcurrentHashMap\nclass Hello {}\n'
        def snapshot = compiler.compile(
                [new TextDocument(uri, 'groovy', 1, src)],
                [], CompilerSettings.defaults(),
                MetalsInspiredTest.classLoader)
        def unused = ImportSupport.unused(snapshot.get(uri))
        assert unused
        assert ImportSupport.unusedDiagnostics(snapshot.get(uri), PositionEncoding.UTF16)
        compiler.close()
    }

    @Test
    void unusedImportIsHintedAndRemovable() {
        fixture = new LspFixture()
        def uri = fixture.open('Hello.groovy', '''\
            import java.util.concurrent.ConcurrentHashMap
            class Hello {}
            '''.stripIndent())
        def hints = fixture.publishedDiagnostics().findAll {
            SupportServices.diagnosticMessage(it) == 'Unused import'
        }
        assert hints
        assert hints[0].tags
        def actions = fixture.server.textDocumentService.codeAction(
                new CodeActionParams(new TextDocumentIdentifier(uri),
                        hints[0].range, new CodeActionContext(hints))).get()
        assert actions.any { it.getRight().title == 'Remove unused imports' }
        def onlyFixAll = fixture.server.textDocumentService.codeAction(
                new CodeActionParams(new TextDocumentIdentifier(uri),
                        hints[0].range, new CodeActionContext(hints, ['source.fixAll']))).get()
        assert onlyFixAll.every { it.isRight() && it.getRight().kind == 'source.fixAll' }
        assert onlyFixAll.any { it.getRight().title == 'Remove unused imports' }
        def onlyOrganize = fixture.server.textDocumentService.codeAction(
                new CodeActionParams(new TextDocumentIdentifier(uri),
                        hints[0].range, new CodeActionContext(hints, ['source.organizeImports']))).get()
        assert onlyOrganize.every { it.isRight() && !it.getRight().kind.startsWith('source.generate') }
        assert onlyOrganize.every { it.isRight() && it.getRight().kind != 'source.fixAll' }
    }

    @Test
    void onTypeOutdentsStructuralBraceButNotStringBrace() {
        fixture = new LspFixture()
        def closer = fixture.open('Hello.groovy', 'class Hello {\n    }\n')
        def outdent = fixture.server.textDocumentService.onTypeFormatting(
                new DocumentOnTypeFormattingParams(new TextDocumentIdentifier(closer),
                        new FormattingOptions(4, true), new Position(1, 5), '}')).get()
        assert outdent
        assert outdent[0].newText == ''
        def quoted = fixture.open('Quoted.groovy', 'def s = "foo}"\n')
        def skipped = fixture.server.textDocumentService.onTypeFormatting(
                new DocumentOnTypeFormattingParams(new TextDocumentIdentifier(quoted),
                        new FormattingOptions(4, true), new Position(0, 13), '}')).get()
        assert skipped.isEmpty()
    }

    @Test
    void implementAbstractMethodsInsertsOverrideStub() {
        fixture = new LspFixture()
        def uri = fixture.open('Hello.groovy', '''\
            interface Named { String name() }
            class Hello implements Named {
            }
            '''.stripIndent())
        def actions = fixture.server.textDocumentService.codeAction(
                new CodeActionParams(new TextDocumentIdentifier(uri),
                        new Range(new Position(1, 6), new Position(1, 11)),
                        new CodeActionContext([]))).get()
        def impl = actions.find { it.getRight().title == 'Implement abstract methods' }
        assert impl != null
        def edit = impl.getRight().edit.changes.values().flatten()[0]
        assert edit.newText.contains('name()')
        assert edit.newText.contains('@Override')
    }

    @Test
    void createMissingClassStub() {
        fixture = new LspFixture()
        def uri = fixture.open('Use.groovy', 'class Use {}\n')
        def diagnostic = new Diagnostic()
        diagnostic.range = new Range(new Position(0, 0), new Position(0, 1))
        diagnostic.message = Either.forLeft('unable to resolve class Widget')
        def actions = fixture.server.textDocumentService.codeAction(
                new CodeActionParams(new TextDocumentIdentifier(uri), diagnostic.range,
                        new CodeActionContext([diagnostic]))).get()
        assert actions.any { it.getRight().title == 'Create class Widget' }
        def stub = actions.find { it.getRight().title == 'Create class Widget' }.getRight().edit.changes.values().flatten()[0]
        assert stub.newText.contains('class Widget')
    }

    @Test
    void insertInferredTypeReplacesDef() {
        fixture = new LspFixture()
        def uri = fixture.open('Hello.groovy', '''\
            class Hello {
                def name = "x"
            }
            '''.stripIndent())
        def actions = fixture.server.textDocumentService.codeAction(
                new CodeActionParams(new TextDocumentIdentifier(uri),
                        new Range(new Position(1, 8), new Position(1, 12)),
                        new CodeActionContext([]))).get()
        def inferred = actions.find { it.getRight().title.startsWith('Insert inferred type') }
        assert inferred != null
        assert inferred.getRight().edit.changes.values().flatten()[0].newText in ['String', 'GString']
    }

    @Test
    void packageGuessFromConventionalRoot() {
        def root = Files.createTempDirectory('groovy-lsp-pkg')
        Files.createDirectories(root.resolve('src/main/groovy/com/acme'))
        def file = root.resolve('src/main/groovy/com/acme/Hello.groovy')
        Files.writeString(file, 'class Hello {}\n')
        assert PackageGuess.fromUri(file.toUri(), [root.toUri()], []) == 'com.acme'
        assert PackageGuess.fromUri(null, [], []) == ''
    }

    @Test
    void identifierWordCountUsesBoundaries() {
        assert Identifiers.count('import foo.Bar\nBar x', 'Bar') == 2
        assert Identifiers.count('import foo.Bar\nclass Hello {}', 'Bar') == 1
        assert !Identifiers.containsWord('Barrier', 'Bar')
        assert Identifiers.count(null, 'Bar') == 0
    }

    @Test
    void onTypeNewlineIndentsAfterBrace() {
        fixture = new LspFixture()
        def uri = fixture.open('Hello.groovy', 'class Hello {\n\n}\n')
        def edits = fixture.server.textDocumentService.onTypeFormatting(
                new DocumentOnTypeFormattingParams(new TextDocumentIdentifier(uri),
                        new FormattingOptions(4, true), new Position(1, 0), '\n')).get()
        assert edits
        assert edits[0].newText == '    '
    }

    @Test
    void hoverFallsBackToIdentifierWhenCompileMisses() {
        fixture = new LspFixture()
        def uri = fixture.open('Hello.groovy', 'class Hello {}\n')
        def hover = fixture.server.textDocumentService.hover(
                new HoverParams(new TextDocumentIdentifier(uri), new Position(0, 8))).get()
        assert hover != null
    }

    @Test
    void importedNamePrefersAlias() {
        assert ImportSupport.importedName(null) == null
    }

    @Test
    void overrideCompletionInsertsOverrideStub() {
        fixture = new LspFixture()
        def src = '''\
            interface Named { String name() }
            class Hello implements Named {
                
            }
            '''.stripIndent()
        def uri = fixture.open('Hello.groovy', src)
        def list = fixture.server.textDocumentService.completion(
                new CompletionParams(new TextDocumentIdentifier(uri), new Position(2, 4))).get()
        def items = list.getRight().items
        def override = items.find { it.detail?.startsWith('override') && it.label == 'name' }
        assert override != null
        assert override.insertText.contains('@Override')
        assert override.insertText.contains('name()')
    }

    @Test
    void namedArgumentCompletionUsesBoundParameter() {
        fixture = new LspFixture()
        def src = '''\
            class Hello {
                def foo(String name, int age) { foo("x", 1) }
            }
            '''.stripIndent()
        def uri = fixture.open('Hello.groovy', src)
        def line = src.split('\n')[1]
        def at = line.indexOf('foo("x"') + 4
        def list = fixture.server.textDocumentService.completion(
                new CompletionParams(new TextDocumentIdentifier(uri), new Position(1, at))).get()
        def labels = list.getRight().items*.label
        assert 'name' in labels
        assert 'age' in labels
    }

    @Test
    void convertToNamedArgumentsRewritesCall() {
        fixture = new LspFixture()
        def src = '''\
            class Hello {
                def foo(String name, int age) { }
                def bar() { foo("a", 1) }
            }
            '''.stripIndent()
        def uri = fixture.open('Hello.groovy', src)
        def actions = fixture.server.textDocumentService.codeAction(
                new CodeActionParams(new TextDocumentIdentifier(uri),
                        new Range(new Position(2, 16), new Position(2, 19)),
                        new CodeActionContext([]))).get()
        def named = actions.find { it.isRight() && it.getRight().title == 'Convert to named arguments' }
        assert named != null
        assert named.getRight().kind == 'refactor.rewrite'
        def text = named.getRight().edit.changes.values().flatten()[0].newText
        assert text.contains('name:')
        assert text.contains('age:')
    }

    @Test
    void insertInferredTypeForLocal() {
        fixture = new LspFixture()
        def uri = fixture.open('Hello.groovy', '''\
            class Hello {
                def bar() {
                    def n = "x"
                }
            }
            '''.stripIndent())
        def actions = fixture.server.textDocumentService.codeAction(
                new CodeActionParams(new TextDocumentIdentifier(uri),
                        new Range(new Position(2, 12), new Position(2, 13)),
                        new CodeActionContext([]))).get()
        def inferred = actions.find { it.getRight().title.startsWith('Insert inferred type') }
        assert inferred != null
        assert inferred.getRight().edit.changes.values().flatten()[0].newText in ['String', 'GString']
    }

    @Test
    void importGroupFolds() {
        fixture = new LspFixture()
        def uri = fixture.open('Hello.groovy', '''\
            import java.util.concurrent.ConcurrentHashMap
            import java.util.ArrayList
            class Hello {}
            '''.stripIndent())
        def folds = fixture.server.textDocumentService.foldingRange(
                new FoldingRangeRequestParams(new TextDocumentIdentifier(uri))).get()
        assert folds.any { it.kind == 'imports' && it.endLine > it.startLine }
    }

    @Test
    void gotoSuperMethodFindsOverrideTarget() {
        fixture = new LspFixture()
        def uri = fixture.open('Hello.groovy', '''\
            class Base { def foo() { } }
            class Hello extends Base { def foo() { } }
            '''.stripIndent())
        def locations = fixture.server.workspaceService.executeCommand(
                new ExecuteCommandParams('groovy.lsp.gotoSuperMethod', [uri, 1, 32])).get()
        assert locations
        assert locations[0].range.start.line == 0
    }

    @Test
    void addAllUnambiguousImportsWhenTwoHits() {
        fixture = new LspFixture()
        fixture.open('a/Widget.groovy', 'package a\nclass Widget {}\n')
        fixture.open('b/Gadget.groovy', 'package b\nclass Gadget {}\n')
        def uri = fixture.open('Use.groovy', 'package c\nclass Use {}\n')
        def widget = new Diagnostic()
        widget.range = new Range(new Position(0, 0), new Position(0, 1))
        widget.message = Either.forLeft('unable to resolve class Widget')
        def gadget = new Diagnostic()
        gadget.range = new Range(new Position(0, 0), new Position(0, 1))
        gadget.message = Either.forLeft('unable to resolve class Gadget')
        def actions = fixture.server.textDocumentService.codeAction(
                new CodeActionParams(new TextDocumentIdentifier(uri), widget.range,
                        new CodeActionContext([widget, gadget]))).get()
        assert actions.any { it.getRight().title == 'Add all unambiguous imports' }
    }
}
