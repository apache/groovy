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
package org.codehaus.groovy.jsr223

import org.junit.jupiter.api.Test

import javax.script.ScriptEngine
import javax.script.ScriptEngineFactory

/**
 * Tests the code-generating methods of {@link GroovyScriptEngineFactory}.
 */
final class GroovyScriptEngineFactoryTest {

    private final ScriptEngineFactory factory = new GroovyScriptEngineFactory()

    /** Runs a generated statement and returns what it printed. */
    private String outputOf(String statement) {
        ScriptEngine engine = factory.scriptEngine
        StringWriter captured = new StringWriter()
        engine.context.writer = captured
        engine.eval(statement)
        captured.toString()
    }

    @Test
    void testOutputStatementOfPlainText() {
        assert factory.getOutputStatement('context') == 'println("context")'
    }

    @Test
    void testOutputStatementEscapesCharactersThatWouldChangeItsMeaning() {
        // A quote or backslash ends or re-escapes the literal, a dollar turns it into an
        // interpolating GString, and a line terminator ends the line.
        assert factory.getOutputStatement('"') == 'println("\\"")'
        assert factory.getOutputStatement('\\') == 'println("\\\\")'
        assert factory.getOutputStatement('$') == 'println("\\$")'
        assert factory.getOutputStatement('\n') == 'println("\\n")'
        assert factory.getOutputStatement('\r') == 'println("\\r")'
    }

    @Test
    void testOutputStatementLeavesHarmlessControlCharactersAlone() {
        // Legal inside a Groovy string literal, so escaping them would change the emitted
        // text for input that already worked.
        assert factory.getOutputStatement('a\tb') == 'println("a\tb")'
    }

    @Test
    void testGeneratedStatementDisplaysTheTextVerbatim() {
        // The contract is what the statement displays, not how it is spelled, so evaluate it.
        ['context',
         'plain text',
         'quotes " and \\ backslashes',
         'a $ dollar',
         'interpolation ${1 + 1} stays literal',
         'a $name that names no variable',
         'two\nlines',
         'carriage\rreturn',
         'tabs\tand\tmore',
         'everything: "\\ $ ${x} \n \r \t',
         'nul\u0000bell\u0007esc\u001b',
         'literal backslash-u: \\u0041',
         'line sep\u2028para sep\u2029',
         'unicode \u00e9 \u4e2d\u6587 \ud83d\ude00'].each { String text ->
            assert outputOf(factory.getOutputStatement(text)) == text + System.lineSeparator()
        }
    }

    @Test
    void testProgramAndMethodCallSyntaxTakeCode() {
        // Both take code by contract, so they concatenate rather than escape.
        assert factory.getProgram('println("hello")', 'println("world")') ==
                'println("hello")\nprintln("world")\n'
        assert factory.getMethodCallSyntax('obj', 'foo', 'x') == 'obj.foo(x)'
        assert factory.getMethodCallSyntax('obj', 'foo') == 'obj.foo()'
    }
}
