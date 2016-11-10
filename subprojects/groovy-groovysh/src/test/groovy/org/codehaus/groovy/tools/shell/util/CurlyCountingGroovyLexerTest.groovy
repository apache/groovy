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
package org.codehaus.groovy.tools.shell.util

/**
 * Unit tests for the {@link CurlyCountingGroovyLexer} class.
 */
class CurlyCountingGroovyLexerTest
    extends GroovyTestCase
{
   void testLexerEmpty() {
       CurlyCountingGroovyLexer it = CurlyCountingGroovyLexer.createGroovyLexer('')
       assert 0 == it.parenLevel
       assert [''] == it.toList()*.text
       assert 0 == it.parenLevel
   }

    void testLexerText() {
        CurlyCountingGroovyLexer it = CurlyCountingGroovyLexer.createGroovyLexer('foo bar baz')
        assert 0 == it.parenLevel
        assert ['foo', 'bar', 'baz', ''] == it.toList()*.text
        assert 0 == it.parenLevel
    }

    void testLexerCurly() {
        CurlyCountingGroovyLexer it = CurlyCountingGroovyLexer.createGroovyLexer('Foo{')
        assert 0 == it.parenLevel
        assert ['Foo', '{', ''] == it.toList()*.text
        assert 1 == it.parenLevel
    }

    void testLexerCurlyMore() {
        CurlyCountingGroovyLexer it = CurlyCountingGroovyLexer.createGroovyLexer('Foo{Baz{Bar{')
        assert 0 == it.parenLevel
        assert ['Foo', '{', 'Baz', '{', 'Bar', '{', ''] == it.toList()*.text
        assert 3 == it.parenLevel
    }

    void testLexerCurlyMany() {
        CurlyCountingGroovyLexer it = CurlyCountingGroovyLexer.createGroovyLexer('Foo{Bar{}}{')
        assert 0 == it.parenLevel
        assert ['Foo', '{', 'Bar', '{', '}', '}', '{',''] == it.toList()*.text
        assert 1 == it.parenLevel
    }
}
