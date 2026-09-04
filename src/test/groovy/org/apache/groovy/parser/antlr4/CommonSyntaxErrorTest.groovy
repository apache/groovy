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
package org.apache.groovy.parser.antlr4

import groovy.transform.AutoFinal
import org.junit.jupiter.api.Test

import static org.apache.groovy.parser.antlr4.TestUtils.expectContains
import static org.apache.groovy.parser.antlr4.TestUtils.expectContainsOnce
import static org.apache.groovy.parser.antlr4.TestUtils.expectParseError

/**
 * Message-quality catalog for the javac-aligned diagnostics added around
 * unclosed literals, escaped unexpected characters (Unicode spaces, curly
 * quotes), missing punctuation, and reserved keywords.
 * <p>
 * Caret-level contracts for missing {@code ) } / {@code ] } / {@code } },
 * GString {@code $}, unexpected-character glyphs, and declaration-shape
 * errors stay in {@link SyntaxErrorTest}. This class locks the <em>new</em>
 * sentences that are not already dumped there.
 * </p>
 */
@AutoFinal
final class CommonSyntaxErrorTest {

    @Test
    void 'unclosed single-quoted string'() {
        expectParseError '''\
            |println 'Hello
            |'''.stripMargin(), '''\
            |Unclosed string literal @ line 1, column 9.
            |   println 'Hello
            |           ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'unclosed double-quoted string'() {
        expectParseError '''\
            |println "Hello
            |'''.stripMargin(), '''\
            |Unclosed string literal @ line 1, column 9.
            |   println "Hello
            |           ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'unclosed triple-quoted string'() {
        expectContains "s = '''hello", 'Unclosed string literal'
    }

    @Test
    void 'unclosed comment'() {
        expectParseError '''\
            |/* comment
            |'''.stripMargin(), '''\
            |Unclosed comment @ line 1, column 1.
            |   /* comment
            |   ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'unclosed comment after a statement'() {
        expectParseError '''\
            |def x = 1
            |/* still open
            |'''.stripMargin(), '''\
            |Unclosed comment @ line 2, column 1.
            |   /* still open
            |   ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'zero-width space is shown as a unicode escape'() {
        expectParseError "def \u200Bname = null\n", '''\
            |Unexpected character: '\\u200b' @ line 1, column 5.
            |   def \u200Bname = null
            |       ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'form feed is shown as \\f'() {
        expectParseError "def na\u000Cme = null\n", '''\
            |Unexpected character: '\\f' @ line 1, column 7.
            |   def na\u000Cme = null
            |         ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'NUL is shown as a unicode escape'() {
        expectContains 'def x\u0000 = 1', "Unexpected character: '\\u0000'"
    }

    @Test
    void 'BOM is shown as a unicode escape'() {
        expectContains "\uFEFFdef x = 1", "Unexpected character: '\\ufeff'"
    }

    @Test
    void 'printable unexpected character is shown as itself'() {
        expectContains 'def `name` = 1', "Unexpected character: '`'"
    }

    @Test
    void 'no-break space is shown as a unicode escape'() {
        expectContains "def \u00A0x = 1", "Unexpected character: '\\u00a0'"
    }

    @Test
    void 'curly quote is shown as a unicode escape'() {
        expectContains "println \u2018hello", "Unexpected character: '\\u2018'"
    }

    @Test
    void 'em dash is shown as a unicode escape'() {
        expectContains "def x = 1\u20142", "Unexpected character: '\\u2014'"
    }

    @Test
    void 'if without parentheses'() {
        expectParseError '''\
            |if true { x = 1 }
            |'''.stripMargin(), '''\
            |Missing '(' @ line 1, column 4.
            |   if true { x = 1 }
            |      ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'while without parentheses'() {
        expectContains 'while true { x = 1 }', "Missing '('"
    }

    @Test
    void 'for without parentheses'() {
        expectContains 'for int i in 1..2 {}', "Missing '('"
    }

    @Test
    void 'incomplete ternary missing colon'() {
        expectParseError 'x ? y', '''\
            |Missing ':' @ line 1, column 6.
            |   x ? y
            |        ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'generic type missing closing angle'() {
        expectContains 'def list1 = new ArrayList<Integer()', "Missing '>'"
    }

    @Test
    void 'const is not supported'() {
        expectParseError '''\
            |const x = 1
            |'''.stripMargin(), '''\
            |'const' is not supported; use 'val' or 'static final' instead @ line 1, column 1.
            |   const x = 1
            |   ^
            |
            |1 error
            |'''.stripMargin()
        // Same sentence in class and method bodies: the token has no parse
        // context cheap enough to pick one replacement, so both are named.
        expectContains 'class C { const x = 1 }', "'const' is not supported; use 'val' or 'static final' instead"
        expectContains 'def m() { const x = 1 }', "'const' is not supported; use 'val' or 'static final' instead"
    }

    @Test
    void 'goto is not supported'() {
        expectParseError '''\
            |goto label
            |'''.stripMargin(), '''\
            |'goto' is not supported @ line 1, column 1.
            |   goto label
            |   ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'else after if with no then-branch still names else'() {
        expectContains 'if (x) else { }', "'else' without 'if'"
    }

    @Test
    void 'else without if'() {
        expectContains 'else { x = 1 }', "'else' without 'if'"
    }

    @Test
    void 'catch without try'() {
        expectContains 'catch (e) { }', "'catch' without 'try'"
    }

    @Test
    void 'finally without try'() {
        expectContains 'finally { }', "'finally' without 'try'"
    }

    @Test
    void 'case outside of switch'() {
        expectContains 'case 1: x = 1', "'case' outside of switch"
    }

    @Test
    void 'default colon outside of switch'() {
        expectContains 'default: x = 1', "'default' outside of switch"
    }

    @Test
    void 'default arrow outside of switch'() {
        expectContains 'default -> x', "'default' outside of switch"
    }

    @Test
    void 'incomplete interface default method is not labelled as outside of switch'() {
        String msg = TestUtils.compileMessage('interface I { default }')
        assert !msg.contains("'default' outside of switch"), msg
    }

    @Test
    void 'threadsafe is not supported'() {
        expectContains 'threadsafe foo() {}', "'threadsafe' is not supported"
        expectContains 'threadsafe', "'threadsafe' is not supported"
    }

    @Test
    void 'throw with nothing after it'() {
        expectContains 'throw', 'Unexpected end of input'
    }

    @Test
    void 'import static with nothing after it'() {
        expectContains 'import static', 'Unexpected end of input'
    }

    @Test
    void 'number ending with underscore'() {
        expectContainsOnce 'def n = 1_', 'Number ending with underscores is invalid'
    }

    @Test
    void 'invalid octal number'() {
        expectContainsOnce 'def n = 08', 'Invalid octal number'
    }

    @Test
    void 'shebang not on the first line'() {
        expectContainsOnce 'x = 1\n#!/usr/bin/env groovy', 'Shebang comment should appear at the first line'
    }

    @Test
    void 'varargs must be the last parameter and names it'() {
        expectContains 'def m(int... a, int b) {}', 'The var-arg parameter a must be the last parameter'
    }

    // Groovy 4 safe index: '?[' is one token and pairs with ']'

    @Test
    void 'unclosed empty safe index'() {
        expectParseError '''\
            |a?[
            |'''.stripMargin(), '''\
            |Missing ']' @ line 1, column 4.
            |   a?[
            |      ^
            |
            |1 error
            |'''.stripMargin()
    }

    @Test
    void 'safe index mismatched closer is still missing bracket'() {
        expectContains 'a?[0)', "Missing ']'"
        expectContains 'a?[0}', "Missing ']'"
    }

    @Test
    void 'nested and chained unclosed safe index name the inner bracket'() {
        expectContains 'a?[b?[0', "Missing ']'"
        expectContains 'a?[0]?[1', "Missing ']'"
        expectContains 'a[b?[0', "Missing ']'"
    }

    @Test
    void 'safe index inside parens still names the missing bracket'() {
        expectContains '(a?[0', "Missing ']'"
        expectContains '(a[0', "Missing ']'"
        expectContains 'foo(a?[0', "Missing ']'"
    }

    @Test
    void 'safe index without a receiver'() {
        expectContains '?[0]', "'?[' requires an expression before it"
    }

    @Test
    void 'safe index after open paren is not a receiver'() {
        expectContains 'foo(?[0])', "'?[' requires an expression before it"
    }

    @Test
    void 'safe index after assignment is not a receiver'() {
        expectContains 'x = ?[0]', "'?[' requires an expression before it"
    }

    @Test
    void 'safe index after a lone dot is not a receiver'() {
        expectContains 'foo.?[0]', "'?[' requires an expression before it"
    }

    @Test
    void 'safe index after a newline is not a path continuation'() {
        expectContains 'a\n?[0]', "'?[' requires an expression before it"
    }

    @Test
    void 'question then safe index is not a receiver'() {
        expectContains 'a??[0]', "'?[' requires an expression before it"
    }

    @Test
    void 'keyword after a dot is still a safe-index receiver'() {
        expectContains 'foo.if?[0)', "Missing ']'"
    }

    @Test
    void 'space between question and bracket is ternary not safe index'() {
        // '?[' is one token; `a? [0]` tokenises as `a` `?` `[0]` (incomplete ternary)
        expectContains 'a? [0]', "Missing ':'"
    }
}
