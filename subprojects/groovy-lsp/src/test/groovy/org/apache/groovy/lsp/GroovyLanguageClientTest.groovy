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
package org.apache.groovy.lsp

import org.eclipse.lsp4j.CompletionItem
import org.eclipse.lsp4j.CompletionList
import org.eclipse.lsp4j.Diagnostic
import org.eclipse.lsp4j.Hover
import org.eclipse.lsp4j.MarkupContent
import org.eclipse.lsp4j.MarkupKind
import org.eclipse.lsp4j.ExecuteCommandParams
import org.eclipse.lsp4j.PublishDiagnosticsParams
import org.eclipse.lsp4j.SemanticTokens
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.codehaus.groovy.control.customizers.SourceAwareCustomizer
import org.eclipse.lsp4j.jsonrpc.messages.Either
import org.junit.jupiter.api.Test

final class GroovyLanguageClientTest {

    @Test
    void protocolEitherShapes() {
        assert GroovyLanguageClient.completionItems(null).empty
        assert GroovyLanguageClient.completionItems('nope').empty
        assert GroovyLanguageClient.completionItems(Either.forLeft([])).empty
        assert GroovyLanguageClient.completionItems(Either.forRight(null)).empty
        assert GroovyLanguageClient.completionItems(Either.forRight('nope')).empty
        def items = new CompletionList(false, [new CompletionItem('ping')])
        assert GroovyLanguageClient.completionItems(Either.forRight(items))*.label == ['ping']
        def bareList = new CompletionList()
        assert GroovyLanguageClient.completionItems(Either.forRight(bareList)).empty

        assert GroovyLanguageClient.hoverText(null) == ''
        assert GroovyLanguageClient.hoverText(new Hover()) == ''
        assert GroovyLanguageClient.hoverText(new Hover(Either.forRight(
                new MarkupContent(MarkupKind.MARKDOWN, 'Sample')))) == 'Sample'
        assert GroovyLanguageClient.hoverText(new Hover(Either.forRight(
                new MarkupContent(MarkupKind.MARKDOWN, '')))) == ''
        assert GroovyLanguageClient.hoverText(new Hover(Either.forRight(null))) == ''
        def leftNull = new Hover()
        leftNull.contents = Either.forLeft(null)
        assert GroovyLanguageClient.hoverText(leftNull) == ''
        def mixed = GroovyLanguageClient.hoverText(new Hover(Either.forLeft([
                Either.forLeft('plain'),
                Either.forRight(new MarkupContent(MarkupKind.MARKDOWN, 'md')),
                Either.forRight(new MarkupContent(MarkupKind.MARKDOWN, '')),
                Either.forRight(null),
                Either.forLeft(null),
                null
        ])))
        assert mixed.contains('plain')
        assert mixed.contains('md')

        assert GroovyLanguageClient.scriptNameList(null).empty
        assert GroovyLanguageClient.scriptNameList('x').empty
        assert GroovyLanguageClient.scriptNameList([null, 'ping']) == ['', 'ping']

        assert GroovyLanguageClient.tokenData(null).empty
        assert GroovyLanguageClient.tokenData(new SemanticTokens([1, 0, 1, 8, 0])) == [1, 0, 1, 8, 0]

        assert GroovyLanguageClient.diagnosticText(null) == ''
        assert GroovyLanguageClient.diagnosticText(new Diagnostic()) == ''
        def left = new Diagnostic()
        left.message = Either.forLeft('plain')
        assert GroovyLanguageClient.diagnosticText(left) == 'plain'
        def leftEmpty = new Diagnostic()
        leftEmpty.message = Either.forLeft(null)
        assert GroovyLanguageClient.diagnosticText(leftEmpty) == ''
        def right = new Diagnostic()
        right.message = Either.forRight(new MarkupContent(MarkupKind.MARKDOWN, 'md'))
        assert GroovyLanguageClient.diagnosticText(right) == 'md'
        def rightEmpty = new Diagnostic()
        rightEmpty.message = Either.forRight(new MarkupContent(MarkupKind.MARKDOWN, ''))
        assert GroovyLanguageClient.diagnosticText(rightEmpty) == ''
        def rightNull = new Diagnostic()
        rightNull.message = Either.forRight(null)
        assert GroovyLanguageClient.diagnosticText(rightNull) == ''
        assert GroovyLanguageClient.keywordList().contains('class')
        assert GroovyLanguageClient.keywordList().contains('int')
        assert GroovyLanguageClient.keywordList().contains('true')
        assert GroovyLanguageClient.keywordList().is(GroovyLanguageClient.keywordList())
    }

    @Test
    void inProcessSessionSpeaksTheSameMessagesAsAnEditor() {
        def dogfood = GroovyLanguageClient.open('groovy-buffer:dogfood', 'class Broken { def x = }', false)
        try {
            assert dogfood.publishedOrNull() == null
            assert dogfood.semanticTokens('class Broken { def x = }')
            assert dogfood.semanticTokens('class Broken { def x = }')
            assert dogfood.compile('class Broken { def x = }')
            assert dogfood.publishedOrNull() != null

            dogfood.settings([compile: 'true', imports: '', starImports: '', scriptBaseClass: ''])
            dogfood.parentLoader(null)
            dogfood.parentLoader(GroovyLanguageClientTest.classLoader)
            assert dogfood.hover('class Sample {\n    def ping() { 1 }\n}', 0, 6).contains('Sample')
            assert dogfood.complete('class Sample {\n    def ping() { ping() }\n}', 1, 16) != null
            assert dogfood.signatures('println(', 0, 8) != null
            assert dogfood.scriptNames('int kept = 7\ndef ping() {}\n').contains('ping')

            dogfood.telemetryEvent('t')
            dogfood.showMessage(null)
            assert dogfood.showMessageRequest(null).get() == null
            dogfood.logMessage(null)
            dogfood.publishDiagnostics(null)
            dogfood.publishDiagnostics(new PublishDiagnosticsParams())

            def tokens = dogfood.tokens('def x = 1')
            assert tokens.any { it.name == 'DEF' }
            assert dogfood.tokens('').empty
            def tree = dogfood.cst('class C { }')
            assert tree.name == 'compilationUnit'
            assert tree.children
            def broken = dogfood.cst('class {')
            assert broken.name
            assert dogfood.server.workspaceService.executeCommand(
                    new ExecuteCommandParams('groovy.tokens', ['groovy-buffer:missing'])).get() == []
            def missingTree = dogfood.server.workspaceService.executeCommand(
                    new ExecuteCommandParams('groovy.cst', ['groovy-buffer:missing'])).get()
            assert missingTree.name == 'compilationUnit' || missingTree.name == 'EOF' || missingTree.children != null
        } finally {
            dogfood.close()
        }
    }

    @Test
    void classpathAndImportsComeFromTheHostConfiguration() {
        assert GroovyLanguageClient.classpathOf(ClassLoader.systemClassLoader).empty
        assert GroovyLanguageClient.importsOf(null) == ''
        assert GroovyLanguageClient.starImportsOf(null) == ''
        def configuration = new CompilerConfiguration()
        assert GroovyLanguageClient.importsOf(configuration) == ''
        def imports = new ImportCustomizer()
        imports.addImports('java.util.List')
        imports.addStarImports('java.io')
        configuration.addCompilationCustomizers(imports)
        configuration.addCompilationCustomizers(
                new SourceAwareCustomizer(new ImportCustomizer()))
        imports.addImport('L', 'java.util.List')
        imports.addStaticImport('java.lang.System', 'out')
        imports.addStaticImport('sout', 'java.lang.System', 'out')
        imports.addStaticStars('java.lang.Math')
        assert GroovyLanguageClient.importsOf(configuration).contains('java.util.List')
        assert GroovyLanguageClient.starImportsOf(configuration).contains('java.io')
        assert GroovyLanguageClient.aliasesOf(configuration).contains('L=java.util.List')
        def aliasOnly = new ImportCustomizer()
        aliasOnly.addImport('JL', 'java.util.List')
        def aliasConfig = new CompilerConfiguration()
        aliasConfig.addCompilationCustomizers(aliasOnly)
        assert GroovyLanguageClient.importsOf(aliasConfig) == ''
        assert GroovyLanguageClient.aliasesOf(aliasConfig) == 'JL=java.util.List'
        assert GroovyLanguageClient.staticImportsOf(configuration).contains('java.lang.System.out')
        assert GroovyLanguageClient.staticImportsOf(configuration).contains('sout=java.lang.System.out')
        assert !GroovyLanguageClient.staticImportsOf(configuration).contains('java.lang.Math')
        assert GroovyLanguageClient.staticStarsOf(configuration).contains('java.lang.Math')
        assert GroovyLanguageClient.aliasesOf(null) == ''
        assert GroovyLanguageClient.staticImportsOf(null) == ''
        assert GroovyLanguageClient.staticStarsOf(null) == ''
        def directory = File.createTempDir('lsp-cp', '')
        directory.deleteOnExit()
        def loader = new URLClassLoader([directory.toURI().toURL(), new URL('http://example.com/missing.jar')] as URL[], null)
        def paths = GroovyLanguageClient.classpathOf(loader)
        assert paths.any { it.contains('lsp-cp') }
        assert paths.every { !it.contains('example.com') }
    }

    @Test
    void methodDetailAndDocumentOffsets() {
        assert GroovyLanguageClient.methodDetail(null) == null
        def plain = new CompletionItem('x')
        assert GroovyLanguageClient.methodDetail(plain) == null
        plain.data = 'nope'
        assert GroovyLanguageClient.methodDetail(plain) == null
        plain.data = [k: 'm']
        assert GroovyLanguageClient.methodDetail(plain) == null
        plain.data = [p: null, r: 'int']
        def detail = GroovyLanguageClient.methodDetail(plain)
        assert detail.parameters == ''
        assert detail.returns == 'int'
        plain.data = [p: 'int x', r: null]
        detail = GroovyLanguageClient.methodDetail(plain)
        assert detail.parameters == 'int x'
        assert detail.returns == ''
        plain.data = [p: 'int x', r: 'void']
        detail = GroovyLanguageClient.methodDetail(plain)
        assert detail.parameters == 'int x'
        assert detail.returns == 'void'

        assert GroovyLanguageClient.positionOf('a\r\nb', 3) == [1, 0] as int[]
        assert GroovyLanguageClient.offsetOf('a\r\nb', 1, 0) == 3
        assert GroovyLanguageClient.flattenSnippet(null) == ''
        assert GroovyLanguageClient.flattenSnippet('${1:name}') == 'name'
        assert GroovyLanguageClient.flattenSnippetLine('a\nb') == 'a'
        assert GroovyLanguageClient.spanOf(null, 0, 0, 0, 0, true) == [0, 0] as int[]
        assert GroovyLanguageClient.spanOf('', 0, 0, 0, 0, false) == [0, 0] as int[]
        assert GroovyLanguageClient.spanOf('ab', 0, 0, 0, 0, false) == [0, 1] as int[]
        assert GroovyLanguageClient.spanOf('ab', 0, 0, 0, 2, true) == [0, 2] as int[]
        assert GroovyLanguageClient.spanOf('abc', 0, 3, 0, 3, true) == [2, 3] as int[]
        assert GroovyLanguageClient.spanOf('abc', 0, 3, 0, 3, false) == [3, 3] as int[]
    }
}
