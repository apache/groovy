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
package org.codehaus.groovy.antlr

import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

/**
 * Error diagnostics for GStrings that end with a bare {@code $}
 * (or otherwise place an illegal character immediately after {@code $}).
 * <p>
 * Historical Groovy 2 reported
 * {@code illegal string body character after dollar sign} with a precise
 * caret.  The Antlr4 lexer must preserve that clarity rather than emitting
 * the generic {@code token recognition error at: '"'} that points at the
 * closing quote without mentioning the dollar.
 */
class GStringEndTest {

    private static final String ILLEGAL_AFTER_DOLLAR = 'Illegal string body character after dollar sign'

    @Test
    void testInvalidEndContainsLineNumber() {
        def err = shouldFail(MultipleCompilationErrorsException,
             '''
                def Target = "releases$"
            ''')
        def text = err.toString()
        assert text.contains('line 2, column 40')
        assert text.contains(ILLEGAL_AFTER_DOLLAR)
        // caret sample still points at the illegal character (the closing quote after $)
        assert text.contains('def Target = "releases$"')
        assert !text.contains('token recognition error')
    }

    @Test
    void testInvalidEndInTripleDoubleQuotedString() {
        def err = shouldFail(MultipleCompilationErrorsException,
             '''
                def Target = """releases$"""
            ''')
        def text = err.toString()
        assert text.contains(ILLEGAL_AFTER_DOLLAR)
        assert text.contains('line 2, column 42')
        assert !text.contains('token recognition error')
    }

    @Test
    void testDollarOnlyGString() {
        def err = shouldFail(MultipleCompilationErrorsException,
             'def x = "$"')
        def text = err.toString()
        assert text.contains(ILLEGAL_AFTER_DOLLAR)
        assert text.contains('line 1, column 11')
        assert !text.contains('token recognition error')
    }

    @Test
    void testIllegalCharacterAfterDollarNotClosingQuote() {
        // space after $ is also illegal (not an identifier / not ${)
        def err = shouldFail(MultipleCompilationErrorsException,
             'def x = "a$ b"')
        def text = err.toString()
        assert text.contains(ILLEGAL_AFTER_DOLLAR)
        assert text.contains('line 1, column 12')
        assert !text.contains('token recognition error')
    }

    @Test
    void testMidStringDollarPartWithTrailingDollar() {
        def err = shouldFail(MultipleCompilationErrorsException,
             'def x = "hello $name$"')
        def text = err.toString()
        assert text.contains(ILLEGAL_AFTER_DOLLAR)
        assert !text.contains('token recognition error')
    }

    @Test
    void testEscapedTrailingDollarIsLegal() {
        assertScript '''
            def Target = "releases\\$"
            assert Target == 'releases$'
        '''
    }

    @Test
    void testDollarAtEndOfFile() {
        // GStringBegin consumes up through `$`, then EOF — no character to quote in the message
        def err = shouldFail(MultipleCompilationErrorsException,
             'def x = "hello$')
        def text = err.toString()
        assert text.contains(ILLEGAL_AFTER_DOLLAR)
        assert !text.contains('token recognition error')
        // EOF form has no trailing ": '…'" character display
        assert !(text =~ /Illegal string body character after dollar sign: '/)
    }

    @Test
    void testErrorReportOnStringEndWithOutParser() {
        // GROOVY-6608: the code did throw a NPE
        def err = shouldFail(MultipleCompilationErrorsException,
             '''
            def scanFolders()
            { doThis( ~"(?i)^sometext$",
            ''')
        def text = err.toString()
        assert text.contains('line 3, column 39')
    }
}
