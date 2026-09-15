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
import org.eclipse.lsp4j.Diagnostic
import org.eclipse.lsp4j.DocumentOnTypeFormattingParams
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
}
