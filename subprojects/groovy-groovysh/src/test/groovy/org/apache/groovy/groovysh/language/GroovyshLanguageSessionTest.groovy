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
package org.apache.groovy.groovysh.language

import org.apache.groovy.lsp.GroovyLanguageClient
import org.apache.groovy.groovysh.jline.GroovyEngine
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.eclipse.lsp4j.CompletionItem
import org.eclipse.lsp4j.CompletionItemKind
import org.eclipse.lsp4j.SignatureInformation
import org.jline.console.CmdDesc
import org.jline.console.CmdLine
import org.jline.reader.LineReader
import org.jline.reader.ParsedLine
import org.jline.terminal.Size
import org.jline.terminal.impl.DumbTerminal
import org.jline.utils.AttributedString

import java.lang.reflect.Array
import java.nio.charset.StandardCharsets
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout

import java.util.concurrent.TimeUnit

final class GroovyshLanguageSessionTest {

    private GroovyEngine groovy
    private GroovyshLanguageSession session

    @AfterEach
    void tearDown() {
        session?.close()
    }

    @Test
    void diagnosticsForBrokenInputReturnHits() {
        groovy = new GroovyEngine()
        session = new GroovyshLanguageSession(groovy)
        def hits = session.diagnosticsFor('class Broken { def x = }')
        assert hits.any { GroovyLanguageClient.diagnosticText(it) }
    }

    @Test
    void diagnosticsHonourEngineCompilerConfiguration() {
        def configuration = new CompilerConfiguration()
        def imports = new ImportCustomizer()
        imports.addImports('java.util.concurrent.ConcurrentHashMap')
        configuration.addCompilationCustomizers(imports)
        groovy = new GroovyEngine(configuration)
        session = new GroovyshLanguageSession(groovy)
        def hits = session.diagnosticsFor('ConcurrentHashMap m')
        assert !hits.any { GroovyLanguageClient.diagnosticText(it).contains('ConcurrentHashMap') }
        session.close()
        groovy = new GroovyEngine()
        session = new GroovyshLanguageSession(groovy)
        def without = session.diagnosticsFor('ConcurrentHashMap m')
        assert without.any { GroovyLanguageClient.diagnosticText(it).contains('ConcurrentHashMap') }
    }

    @Test
    void composeDeclaresBindingVariablesAndSkipsInternals() {
        groovy = new GroovyEngine()
        groovy.put('answer', 42)
        groovy.put('PWD', '/tmp')
        groovy.put('FOO', 'keep')
        groovy.put('PI', 3.14d)
        groovy.put('in', 1)
        groovy.put('class', 2)
        groovy.put('1bad', 'skip')
        groovy.put('GROOVY_OPTIONS', [:])
        groovy.put('CONSOLE_OPTIONS', [:])
        groovy.put('ROOT', '/')
        groovy.put('NANORC', 'x')
        groovy.put('_args', [])
        groovy.put('gone', null)
        groovy.put('nums', [1, 2] as int[])
        groovy.put('run', new Runnable() {
            @Override
            void run() {
            }
        })
        session = new GroovyshLanguageSession(groovy)
        String script = session.compose('answer')
        assert script.contains('java.lang.Integer answer = null')
        assert script.contains('java.lang.String FOO = null')
        assert script.contains('java.lang.Double PI = null')
        assert !script.contains(' in = null')
        assert !script.contains('class class')
        assert !script.contains('PWD')
        assert !script.contains('1bad')
        assert !script.contains('GROOVY_OPTIONS')
        assert !script.contains('CONSOLE_OPTIONS')
        assert !script.contains('ROOT')
        assert !script.contains('NANORC')
        assert !script.contains('_args')
        assert !script.contains('gone')
        assert script.contains('int[] nums = null')
        assert script.contains('java.lang.Object run = null')
    }

    @Test
    void composeDoesNotRedeclareBufferNames() {
        groovy = new GroovyEngine()
        groovy.put('GROOVYSH_OPTIONS', [interpreterMode: true])
        groovy.execute('int kept = 7')
        session = new GroovyshLanguageSession(groovy)
        String script = session.compose('kept')
        assert script.readLines().count { it ==~ /.*\bkept\s*=.*/ } == 1
    }

    @Test
    @Timeout(value = 20, unit = TimeUnit.SECONDS)
    void tabAfterDotListsMembersOfTheListInTheBuffer() {
        groovy = new GroovyEngine()
        groovy.put('GROOVYSH_OPTIONS', [interpreterMode: true])
        groovy.execute('def a = []')
        session = new GroovyshLanguageSession(groovy)
        def items = session.completeLine('a.')
        def labels = items.collect { it.label }
        assert 'add' in labels || 'size' in labels || 'plus' in labels : labels.take(30)
        def candidates = []
        session.complete(null, new Line('a.'), candidates)
        assert candidates.any { it.value().startsWith('a.') && (it.value().contains('add') || it.value().contains('size') || it.value().contains('plus')) }
        assert candidates.every { it.group() != 'Function' }
        assert candidates.any { it.group() == 'Method' && it.displ().startsWith('add') && it.descr() == null }
        assert candidates.findAll { it.group() == 'Method' }.every { !it.value().contains('(') }
    }

    @Test
    void completeLineAndCompleterMapEngineCandidates() {
        groovy = new GroovyEngine()
        groovy.execute('class Box { String name }')
        session = new GroovyshLanguageSession(groovy)
        def items = session.completeLine('new Box().')
        assert items != null
        assert session.completeLine('new Box().', 9) != null
        assert session.completeLine('new Box().', -1) != null
        def candidates = []
        session.complete(null, new Line('Box().'), candidates)
        session.complete(null, new Line('/grab'), candidates)
        session.complete(null, null, candidates)
        session.complete(null, new Line(''), null)
        assert GroovyLanguageClient.flattenSnippetLine('class ${1:Name} {\n    $0\n}') == 'class Name {'
        assert GroovyLanguageClient.flattenSnippetLine(null) == ''
        assert GroovyLanguageClient.flattenSnippetLine('') == ''
        assert GroovyLanguageClient.flattenSnippetLine('$0') == ''
        assert GroovyLanguageClient.flattenSnippetLine('${1:foo}$2bar') == 'foobar'
        assert GroovyLanguageClient.flattenSnippetLine('one\ntwo') == 'one'
        assert GroovyshLanguageSession.BUFFER_URI.toString() == 'groovy-buffer:repl'
    }

    @Test
    void mapEngineCandidatesPrefixesAndSkipsEmptyInsert() {
        def out = []
        def items = [
                item('name', CompletionItemKind.Field, 'String', 'name'),
                item('', CompletionItemKind.Method, null, ''),
                item('ping', CompletionItemKind.Method, null, '${1:ping}'),
        ]
        CompletionLayout.mapEngineCandidates(items, new Line('Box().'), out)
        assert out.size() == 2
        assert out[0].value() == 'Box().name'
        assert out[0].displ() == 'name'
        assert out[0].group() == 'Field'
        assert out[1].value() == 'Box().ping'
        out.clear()
        CompletionLayout.mapEngineCandidates(
                [item('println', CompletionItemKind.Method, null, 'println')],
                new Line('pri', 3), out)
        assert out[0].value() == 'println'
        out.clear()
        CompletionLayout.mapEngineCandidates(
                [item('name', CompletionItemKind.Field, null, 'Box().name')],
                new Line('Box().'), out)
        assert out[0].value() == 'Box().name'
        out.clear()
        CompletionLayout.mapEngineCandidates(
                [item('x', null, null, 'x')],
                new Line('z'), out)
        assert out[0].group() == 'Other'
        CompletionLayout.mapEngineCandidates(null, new Line('x'), out)
        CompletionLayout.mapEngineCandidates(items, null, out)
        CompletionLayout.mapEngineCandidates(items, new Line('x'), null)
        CompletionLayout.mapEngineCandidates(
                [null, item('ok', CompletionItemKind.Method, null, 'ok')],
                new Line('z'), out)
        assert out.any { it.value() == 'ok' || it.value().endsWith('ok') }
    }

    @Test
    void completionGroupsUseFullKindNamesAndShowParameters() {
        def out = []
        CompletionLayout.mapEngineCandidates([
                signed('add', CompletionItemKind.Method, 'boolean', 'Object e', 'add(${1:e})'),
                signed('clear', CompletionItemKind.Method, 'void', '', 'clear()'),
                signed('of', CompletionItemKind.Function, 'List', 'Object e1, Object e2', 'of(${1:e1}, ${2:e2})'),
                item('size', CompletionItemKind.Field, 'int', 'size'),
                item('name', CompletionItemKind.Property, null, 'name'),
                item('item', CompletionItemKind.Variable, 'String', 'item'),
                item('List', CompletionItemKind.Class, 'java.util.List', 'List'),
                item('Face', CompletionItemKind.Interface, 'java.util.List', 'Face'),
                item('Color', CompletionItemKind.Enum, 'Color', 'Color'),
                item('abstract', CompletionItemKind.Keyword, null, 'abstract'),
                item('iff', CompletionItemKind.Snippet, null, 'if (${1:condition}) {\n    $0\n}'),
                item('each', CompletionItemKind.Method, 'GDK', 'each'),
        ], new Line('a.'), out)
        def add = out.find { it.value() == 'a.add' }
        def clear = out.find { it.value() == 'a.clear' }
        def of = out.find { it.value() == 'a.of' }
        def addShown = CompletionLayout.visible(add.displ())
        def clearShown = CompletionLayout.visible(clear.displ())
        def ofShown = CompletionLayout.visible(of.displ())
        assert addShown.startsWith('add(Object e)')
        assert !addShown.startsWith('add (')
        assert addShown.endsWith('boolean')
        assert addShown.length() == clearShown.length()
        assert clearShown.startsWith('clear()')
        assert clearShown.endsWith('void')
        assert ofShown.startsWith('of(Object e1, Object e2)')
        assert ofShown.endsWith('List')
        assert ofShown.length() == addShown.length()
        assert add.displ().startsWith('add\u001B[90m(Object e)')
        assert add.displ().contains('\u001B[90mboolean')
        assert add.descr() == null
        assert add.group() == 'Method'
        assert add.value() == 'a.add'
        assert clear.value() == 'a.clear'
        assert of.value() == 'a.of'
        assert out.find { it.value().startsWith('a.if (') }
        assert of.group() == 'Method'
        assert out.find { it.value() == 'a.size' }.descr() == null
        assert out.find { it.value() == 'a.size' }.displ() == 'size'
        assert out.find { it.value() == 'a.size' }.group() == 'Field'
        assert out.find { it.value() == 'a.name' }.group() == 'Property'
        assert out.find { it.value() == 'a.item' }.group() == 'Variable'
        assert out.find { it.value() == 'a.List' }.group() == 'Class'
        assert out.find { it.value() == 'a.Face' }.group() == 'Interface'
        assert out.find { it.value() == 'a.Color' }.group() == 'Enum'
        assert out.find { it.value() == 'a.abstract' }.group() == 'Keyword'
        assert out.find { it.value().startsWith('a.if') }.group() == 'Snippet'
        assert out.find { it.value() == 'a.each' }.descr() == null
        assert out.find { it.value() == 'a.each' }.displ() == 'each'
        def withParts = new CompletionItem('add')
        withParts.data = [p: 'Object e', r: 'boolean']
        assert GroovyLanguageClient.methodDetail(withParts).returns == 'boolean'
        assert GroovyLanguageClient.methodDetail(withParts).parameters == 'Object e'
        assert GroovyLanguageClient.methodDetail(null) == null
        assert GroovyLanguageClient.methodDetail(new CompletionItem('plain')) == null
        assert CompletionLayout.columnCount(40) == 1
        assert CompletionLayout.columnCount(80) == 2
        assert CompletionLayout.columnCount(120) == 3
        assert CompletionLayout.columnCount(200) == 4
        assert CompletionLayout.cellWidth(80, 2) == 38
        assert addShown.length() == 38
        def narrow = []
        CompletionLayout.mapEngineCandidates([
                signed('veryLongMethod', CompletionItemKind.Method, 'void',
                        'Map<String, List<Object>> arg0, Collection arg1', 'veryLongMethod'),
        ], new Line('a.'), narrow, 40)
        def cut = CompletionLayout.visible(narrow[0].displ())
        assert cut.length() == CompletionLayout.cellWidth(40, 1)
        assert cut.contains('...')
        assert cut.endsWith('void')
        assert cut.startsWith('veryLongMethod(')
        assert out.every { it.group() != 'Function' }
        assert CompletionLayout.category(null) == 'Other'
        def blankParts = new CompletionItem('clear')
        blankParts.data = [p: '', r: '']
        assert GroovyLanguageClient.methodDetail(blankParts).parameters == ''
        assert GroovyLanguageClient.methodDetail(blankParts).returns == ''
        assert CompletionLayout.insertedText(null) == ''
        assert CompletionLayout.insertedText(item('copyOf', CompletionItemKind.Method,
                'List copyOf(Collection arg0)', 'copyOf(${1:arg0})')) == 'copyOf'
        assert CompletionLayout.cellWidth(1, 100) == 1
        assert CompletionLayout.cellWidth(0, 0) > 0
        assert CompletionLayout.visible(null) == ''
        assert CompletionLayout.visible('') == ''
        def blank = new CompletionItem()
        blank.kind = CompletionItemKind.Snippet
        assert CompletionLayout.insertedText(blank) == ''
        def tiny = []
        CompletionLayout.mapEngineCandidates([
                signed('veryLongMethod', CompletionItemKind.Method, 'void', 'Object arg0', 'veryLongMethod'),
        ], new Line('a.'), tiny, 4)
        assert CompletionLayout.visible(tiny[0].displ())
        assert CompletionLayout.fit(null, 4) == ''
        assert CompletionLayout.fit('abcdef', 0) == ''
        assert CompletionLayout.fit('abcdef', 2) == 'ab'
        assert CompletionLayout.fit('abcdef', 6) == 'abcdef'
        assert CompletionLayout.terminalWidth(null) == 80
    }

    @Test
    void zeroWidthTerminalFallsBackToTheDefaultColumns() {
        groovy = new GroovyEngine()
        session = new GroovyshLanguageSession(groovy)
        def terminal = new DumbTerminal('groovysh-test', 'dumb',
                new ByteArrayInputStream(new byte[0]), new ByteArrayOutputStream(), StandardCharsets.UTF_8)
        terminal.size = new Size(0, 0)
        def reader = [getTerminal: { -> terminal }] as LineReader
        def candidates = []
        session.complete(reader, new Line('a.'), candidates)
        assert candidates.any { it.group() == 'Method' }
    }

    @Test
    void engineCompleterRecognizesReceiversAndSkipsPackages() {
        groovy = new GroovyEngine()
        def type = Class.forName('org.apache.groovy.groovysh.jline.GroovyEngine$MethodCompleter')
        def ctor = type.getDeclaredConstructor(GroovyEngine)
        ctor.accessible = true
        def completer = ctor.newInstance(groovy)
        def candidates = []
        completer.complete(null, new Line('notARealName.'), candidates)
        completer.complete(null, new Line('java.util.'), candidates)
        groovy.put('kept', 'x')
        completer.complete(null, new Line('kept.'), candidates)
        def known = type.getDeclaredMethod('knownReceiver', String)
        known.accessible = true
        assert known.invoke(completer, [null] as Object[]) == false
        assert known.invoke(completer, ['.x'] as Object[]) == false
        assert known.invoke(completer, ['java.util.'] as Object[]) == false
    }

    @Test
    void scriptDescriptionUsesDiagnosticsThenFallsBack() {
        groovy = new GroovyEngine()
        session = new GroovyshLanguageSession(groovy)
        def syntax = new CmdLine('class Broken { def x = }', 'class Broken { def x = }', '', [],
                CmdLine.DescriptionType.SYNTAX)
        def desc = session.scriptDescription(syntax)
        assert desc?.mainDesc
        def clean = new CmdLine('1', '1', '', [], CmdLine.DescriptionType.SYNTAX)
        assert session.scriptDescription(clean) == null
        def method = new CmdLine('println(', 'println', '', ['println'], CmdLine.DescriptionType.METHOD)
        session.scriptDescription(method)
        session.scriptDescription(null)
        assert session.signaturesFor('') != null
    }

    @Test
    void scriptDescriptionUsesEngineSignaturesOnTheComposedLine() {
        groovy = new GroovyEngine()
        groovy.execute('''\
            class Box {
                static void ping(String name) {}
                static void ping(String name, int count) {}
            }
            '''.stripIndent())
        session = new GroovyshLanguageSession(groovy)
        assert session.compose('Box.ping(').contains('class Box')
        def described = session.scriptDescription(
                new CmdLine('Box.ping(', 'Box.ping', '', ['Box'], CmdLine.DescriptionType.METHOD))
        if (described?.mainDesc) {
            assert described.mainDesc.any { it.toString().contains('ping') || it.toString().contains('Box') }
        }
        session.close()
        groovy = new GroovyEngine()
        session = new GroovyshLanguageSession(groovy)
        def src = '''\
            class Box {
                static void ping(String name) {}
                static void ping(String name, int count) {}
                static void use() { Box.ping(1) }
            }
            '''.stripIndent()
        int cursor = src.indexOf('Box.ping(') + 'Box.ping('.length()
        def withArgs = session.signaturesFor(src, cursor)
        assert withArgs.any { it?.label?.contains('ping') }
        assert GroovyshLanguageSession.describeSignatures(null) == null
        assert GroovyshLanguageSession.describeSignatures([]) == null
        def labeled = GroovyshLanguageSession.describeSignatures([
                new SignatureInformation('ping(String name)'),
                null,
                new SignatureInformation('')
        ])
        assert labeled.mainDesc.size() == 3
        assert labeled.mainDesc[0].toString().contains('ping')
        assert session.signaturesFor('Box.ping(', -1) != null
        assert session.signaturesFor('Box.ping(', 99) != null
        assert session.signaturesFor('a\r\nb', 4) != null
        assert session.signaturesFor('a\rb', 3) != null
    }

    @Test
    void helpersCoverEmptyCompletionsAndInspectorFallback() {
        groovy = new GroovyEngine() {
            @Override
            CmdDesc scriptDescription(CmdLine line) {
                throw new RuntimeException('boom')
            }
        }
        session = new GroovyshLanguageSession(groovy)
        def syntax = new CmdLine('1', '1', '', [], CmdLine.DescriptionType.SYNTAX)
        assert session.scriptDescription(syntax) == null
        def method = new CmdLine('zzz(', 'zzz', '', ['zzz'], CmdLine.DescriptionType.METHOD)
        assert session.scriptDescription(method) == null
    }

    @Test
    void typeLiteralMatrix() {
        assert GroovyshLanguageSession.typeLiteral('x') == 'java.lang.String'
        assert GroovyshLanguageSession.typeLiteral(1L) == 'java.lang.Long'
        assert GroovyshLanguageSession.typeLiteral(new Object()) == 'java.lang.Object'
        assert GroovyshLanguageSession.typeLiteral(new int[0]) == 'int[]'
        assert GroovyshLanguageSession.typeLiteral(new String[0]) == 'java.lang.String[]'
        assert GroovyshLanguageSession.typeLiteral(new Runnable() {
            @Override
            void run() {
            }
        }) == 'java.lang.Object'
        assert GroovyshLanguageSession.typeLiteral(Nested.A) == Nested.canonicalName
        Runnable lambda = () -> {}
        assert GroovyshLanguageSession.typeLiteral(lambda) == 'java.lang.Object'
        assert GroovyshLanguageSession.typeLiteral(new Hidden()) == 'java.lang.Object'
        def script = new GroovyShell().parse('1')
        assert GroovyshLanguageSession.typeLiteral(script) == 'java.lang.Object'
        def row = Array.newInstance(script.getClass(), 1)
        def grid = Array.newInstance(script.getClass(), 1, 2)
        assert GroovyshLanguageSession.typeLiteral(row) == 'java.lang.Object[]'
        assert GroovyshLanguageSession.typeLiteral(grid) == 'java.lang.Object[][]'
        assert GroovyshLanguageSession.typeLiteral(new int[1][2]) == 'int[][]'
    }

    @Test
    void bindingDeclarationsToleratesFindFailure() {
        groovy = new GroovyEngine() {
            @Override
            Map<String, Object> find(String name) {
                throw new IllegalStateException('boom')
            }
        }
        session = new GroovyshLanguageSession(groovy)
        assert session.compose('1').trim() == '1'
        session.close()
        groovy = new GroovyEngine() {
            @Override
            Map<String, Object> find(String name) {
                null
            }
        }
        session = new GroovyshLanguageSession(groovy)
        assert session.compose('1').trim() == '1'
    }

    @Test
    void completeSwallowsEngineFailureAfterClose() {
        groovy = new GroovyEngine()
        session = new GroovyshLanguageSession(groovy)
        session.close()
        def candidates = []
        session.complete(null, new Line('x'), candidates)
        assert candidates.isEmpty()
        session = null
    }

    @Test
    void closeAfterDiagnostics() {
        groovy = new GroovyEngine()
        session = new GroovyshLanguageSession(groovy)
        assert session.diagnosticsFor('1') instanceof List
        session.close()
        session.close()
        session = null
    }

    @Test
    void scriptDescriptionFallsBackWhenMethodHasNoSignatures() {
        groovy = new GroovyEngine() {
            @Override
            CmdDesc scriptDescription(CmdLine line) {
                def d = new CmdDesc()
                d.setMainDesc([new AttributedString('fallback')])
                return d
            }
        }
        session = new GroovyshLanguageSession(groovy)
        def method = new CmdLine('zzz(', 'zzz', '', ['zzz'], CmdLine.DescriptionType.METHOD)
        def desc = session.scriptDescription(method)
        assert desc?.mainDesc[0].toString().contains('fallback')
        assert session.diagnosticsFor(null) != null
        assert session.completeLine(null) != null
        assert session.scriptDescription(new CmdLine(null, '', null, [], CmdLine.DescriptionType.SYNTAX)) == null
        session.scriptDescription(new CmdLine(null, null, null, [], CmdLine.DescriptionType.METHOD))
        def candidates = []
        session.complete(null, new Line(null), candidates)
        session.compose(null)
        groovy.put('again', 'x')
        assert session.compose('1').contains('again')
    }

    enum Nested { A }

    private static final class Hidden {}

    private static CompletionItem signed(String label, CompletionItemKind kind, String returns,
                                         String parameters, String insert) {
        def item = item(label, kind, returns + ' ' + label + '(' + parameters + ')', insert)
        item.data = [p: parameters, r: returns]
        item
    }

    private static CompletionItem item(String label, CompletionItemKind kind, String detail, String insert) {
        def item = new CompletionItem(label)
        item.kind = kind
        item.detail = detail
        item.insertText = insert
        item
    }

    private static final class Line implements ParsedLine {
        private final String text
        private final int cursor

        Line(String text) {
            this(text, text == null ? 0 : text.length())
        }

        Line(String text, int cursor) {
            this.text = text
            this.cursor = cursor
        }

        @Override
        String word() { text }

        @Override
        int wordCursor() { text == null ? 0 : Math.min(cursor, text.length()) }

        @Override
        int wordIndex() { 0 }

        @Override
        List<String> words() { [text] }

        @Override
        String line() { text }

        @Override
        int cursor() { cursor }
    }
}
