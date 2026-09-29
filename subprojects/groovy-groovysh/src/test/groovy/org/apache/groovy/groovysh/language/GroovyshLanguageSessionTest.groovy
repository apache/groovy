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

import org.apache.groovy.groovysh.jline.GroovyEngine
import org.apache.groovy.lsp.internal.engine.GroovyLanguageEngine
import org.jline.console.CmdDesc
import org.jline.console.CmdLine
import org.jline.reader.ParsedLine
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

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
        assert hits.any { it.message() }
    }

    @Test
    void composeDeclaresBindingVariablesAndSkipsInternals() {
        groovy = new GroovyEngine()
        groovy.put('answer', 42)
        groovy.put('PWD', '/tmp')
        groovy.put('FOO', 'skip')
        groovy.put('1bad', 'skip')
        groovy.put('nums', [1, 2] as int[])
        groovy.put('run', new Runnable() {
            @Override
            void run() {
            }
        })
        session = new GroovyshLanguageSession(groovy)
        String script = session.compose('answer')
        assert script.contains('java.lang.Integer answer = null')
        assert !script.contains('PWD')
        assert !script.contains('FOO')
        assert !script.contains('1bad')
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
    void completeLineAndCompleterMapEngineCandidates() {
        groovy = new GroovyEngine()
        groovy.execute('class Box { String name }')
        session = new GroovyshLanguageSession(groovy)
        def items = session.completeLine('new Box().')
        assert items != null
        def candidates = []
        session.complete(null, new Line('Box().'), candidates)
        assert candidates.every { it.value().startsWith('Box().') || it.value() }
        session.complete(null, new Line('/grab'), candidates)
        session.complete(null, null, candidates)
        session.complete(null, new Line(''), null)
        assert GroovyshLanguageSession.plainInsert('class ${1:Name} {\n    $0\n}') == 'class Name {'
        assert GroovyshLanguageSession.plainInsert(null) == ''
    }

    @Test
    void scriptDescriptionUsesDiagnosticsThenFallsBack() {
        groovy = new GroovyEngine()
        session = new GroovyshLanguageSession(groovy)
        def syntax = new CmdLine('class Broken { def x = }', 'class Broken { def x = }', '', [],
                CmdLine.DescriptionType.SYNTAX)
        def desc = session.scriptDescription(syntax)
        assert desc?.mainDesc
        def method = new CmdLine('println(', 'println(', '', ['println'], CmdLine.DescriptionType.METHOD)
        session.scriptDescription(method)
        session.scriptDescription(null)
        assert session.signaturesFor('') != null
        groovy.execute('''\
            class Box {
                def ping(int a) { a }
            }
            '''.stripIndent())
        def call = new CmdLine('new Box().ping(', 'new Box().ping(', '', ['new', 'Box'],
                CmdLine.DescriptionType.METHOD)
        session.scriptDescription(call)
    }

    @Test
    void helpersCoverLoaderStampDeclaredNamesAndEmptyCompletions() {
        assert GroovyshLanguageSession.urlStamp(new ClassLoader(null) {}) != 0
        assert GroovyshLanguageSession.typeLiteral(Nested.A) == 'java.lang.Object'
        assert GroovyshLanguageSession.declaredNames('def ping() {\n  1\n}').contains('ping')
        assert GroovyshLanguageSession.declaredNames('@Deprecated int kept = 7').contains('kept')
        assert GroovyshLanguageSession.declaredNames('').isEmpty()
        assert GroovyshLanguageSession.endOf('') == [0, 0] as int[]
        groovy = new GroovyEngine() {
            @Override
            CmdDesc scriptDescription(CmdLine line) {
                throw new RuntimeException('boom')
            }
        }
        session = new GroovyshLanguageSession(groovy)
        def syntax = new CmdLine('1', '1', '', [], CmdLine.DescriptionType.SYNTAX)
        assert session.scriptDescription(syntax) == null
        session.metaClass.completeLine = { String line ->
            [new GroovyLanguageEngine.Candidate('', '', '', '', '', false)]
        }
        def candidates = []
        session.complete(null, new Line('x'), candidates)
        assert candidates.isEmpty()
    }

    @Test
    void bumpGrapeGenerationAndClose() {
        groovy = new GroovyEngine()
        session = new GroovyshLanguageSession(groovy)
        session.bumpGrapeGeneration()
        session.bumpGrapeGeneration()
        assert session.diagnosticsFor('1') != null
        session.close()
        session = null
    }

    enum Nested { A }

    private static final class Line implements ParsedLine {
        private final String text

        Line(String text) {
            this.text = text
        }

        @Override
        String word() { text }

        @Override
        int wordCursor() { text.length() }

        @Override
        int wordIndex() { 0 }

        @Override
        List<String> words() { [text] }

        @Override
        String line() { text }

        @Override
        int cursor() { text.length() }
    }
}
