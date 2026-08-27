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
package org.codehaus.groovy.control

import org.codehaus.groovy.control.messages.Message
import org.codehaus.groovy.control.messages.SimpleMessage
import org.codehaus.groovy.control.messages.WarningMessage
import org.codehaus.groovy.syntax.Token
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertFalse
import static org.junit.jupiter.api.Assertions.assertTrue

/**
 * GROOVY-12312: tests {@link CompilerConfiguration#setErrorFormat(ErrorFormat)}, which selects
 * how diagnostics are rendered.
 */
final class ErrorFormatTest {

    private static final String TWO_ERRORS = '''\
@groovy.transform.CompileStatic
class Subject {
    def a() { new Object().nope1() }
    def b() { new Object().nope2() }
}
'''

    private static String compileAndReport(String source, ErrorFormat format = null) {
        def config = new CompilerConfiguration()
        if (format != null) config.errorFormat = format
        def unit = new CompilationUnit(config, null, new GroovyClassLoader(ErrorFormatTest.class.classLoader))
        unit.addSource('Subject.groovy', source)
        try {
            unit.compile()
            ''
        } catch (MultipleCompilationErrorsException e) {
            e.message
        }
    }

    @Test
    void testShortFormatEmitsOneLinePerDiagnostic() {
        def lines = compileAndReport(TWO_ERRORS, ErrorFormat.SHORT).readLines()
                .findAll { it.startsWith('Subject.groovy') }
        assertEquals(2, lines.size(), "expected one line per error but got: $lines")
        lines.eachWithIndex { line, i ->
            assert line ==~ /Subject\.groovy:\d+:\d+: error: .*nope${i + 1}.*/ : "unexpected line: $line"
        }
    }

    @Test
    void testShortFormatOmitsTheSourceSnippetAndCaret() {
        def report = compileAndReport(TWO_ERRORS, ErrorFormat.SHORT)
        assertFalse(report.contains('^'), "short format must not draw a caret: $report")
        assertFalse(report.contains(' @ line '), "position belongs in the prefix, not appended: $report")
        assertFalse(report.contains('new Object().nope1()'), "source line must not be echoed: $report")
    }

    // the position is structural in the short form, so it is reported once, with the column
    // the current format only ever gives on its second line
    @Test
    void testShortFormatCarriesLineAndColumn() {
        def line = compileAndReport(TWO_ERRORS, ErrorFormat.SHORT).readLines()
                .find { it.startsWith('Subject.groovy') }
        assertTrue(line.startsWith('Subject.groovy:3:'), "expected file:line:column but got: $line")
    }

    @Test
    void testFullFormatIsTheDefaultAndIsUnchanged() {
        assertEquals(ErrorFormat.FULL, new CompilerConfiguration().errorFormat)
        def byDefault = compileAndReport(TWO_ERRORS)
        assertEquals(byDefault, compileAndReport(TWO_ERRORS, ErrorFormat.FULL))
        assertTrue(byDefault.contains('^'), "full format keeps the caret")
        assertTrue(byDefault.contains(' @ line '), "full format keeps the appended position")
    }

    @Test
    void testShortFormatJoinsMultiLineMessages() {
        def message = new SimpleMessage("first line\n  second line", null)
        assertEquals(['error: first line second line', '1 error'], render(ErrorFormat.SHORT, [], [message]))
    }

    @Test
    void testShortFormatLabelsWarningsAsSuch() {
        def source = SourceUnit.create('W.groovy', 'x', 0)
        def warning = new WarningMessage(WarningMessage.LIKELY_ERRORS, 'suspicious', Token.newString('x', 4, 7), source)
        assertEquals(['W.groovy:4:7: warning: suspicious', '1 warning'], render(ErrorFormat.SHORT, [warning], []))
    }

    // a message with no source unit or position drops those parts rather than inventing them
    @Test
    void testShortFormatOmitsPartsItDoesNotHave() {
        def message = new SimpleMessage('no source unit here', null)
        assertEquals(['error: no source unit here', '1 error'], render(ErrorFormat.SHORT, [], [message]))
    }

    // a Message subclass which knows nothing of formats still renders: its full text is the diagnostic
    @Test
    void testShortFormatFallsBackToTheFullTextOfOtherMessages() {
        def custom = new Message() {
            @Override
            void write(PrintWriter writer, Janitor janitor) {
                writer.println('custom')
                writer.println('  message')
            }
        }
        assertEquals(['error: custom message', '1 error'], render(ErrorFormat.SHORT, [], [custom]))
    }

    @Test
    void testCopyConstructorCarriesTheErrorFormat() {
        def original = new CompilerConfiguration(errorFormat: ErrorFormat.SHORT)
        assertEquals(ErrorFormat.SHORT, new CompilerConfiguration(original).errorFormat)
    }

    @Test
    void testNullSelectsTheDefault() {
        def config = new CompilerConfiguration(errorFormat: ErrorFormat.SHORT)
        config.errorFormat = null
        assertEquals(ErrorFormat.FULL, config.errorFormat)
    }

    private static List<String> render(ErrorFormat format, List<Message> warnings, List<Message> errors) {
        def writer = new StringWriter()
        format.write(new PrintWriter(writer, true), null, warnings, errors, false)
        writer.toString().readLines()
    }
}
