/*
 * Copyright 2003-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.groovy.tools.shell.util

/**
 * Unit tests for the {@link CurlyCountingGroovyLexer} class.
 */
class CurlyCountingGroovyLexerTest
    extends GroovyTestCase
{
   void testLexerEmpty() {
       CurlyCountingGroovyLexer it = CurlyCountingGroovyLexer.createGroovyLexer("")
       assertEquals(0, it.getParenLevel())
       assertEquals([''], it.toList().collect {it.getText()})
       assertEquals(0, it.getParenLevel())
   }

    void testLexerText() {
        CurlyCountingGroovyLexer it = CurlyCountingGroovyLexer.createGroovyLexer("foo bar baz")
        assertEquals(0, it.getParenLevel())
        assertEquals(['foo', 'bar', 'baz', ''], it.toList().collect {it.getText()})
        assertEquals(0, it.getParenLevel())
    }

    void testLexerCurly() {
        CurlyCountingGroovyLexer it = CurlyCountingGroovyLexer.createGroovyLexer("Foo{")
        assertEquals(0, it.getParenLevel())
        assertEquals(['Foo', '{', ''], it.toList().collect {it.getText()})
        assertEquals(1, it.getParenLevel())
    }

    void testLexerCurlyMore() {
        CurlyCountingGroovyLexer it = CurlyCountingGroovyLexer.createGroovyLexer("Foo{Baz{Bar{")
        assertEquals(0, it.getParenLevel())
        assertEquals(['Foo', '{', 'Baz', '{', 'Bar', '{', ''], it.toList().collect {it.getText()})
        assertEquals(3, it.getParenLevel())
    }

    void testLexerCurlyMany() {
        CurlyCountingGroovyLexer it = CurlyCountingGroovyLexer.createGroovyLexer("Foo{Bar{}}{")
        assertEquals(0, it.getParenLevel())
        assertEquals(['Foo', '{', 'Bar', '{', '}', '}', '{',''], it.toList().collect {it.getText()})
        assertEquals(1, it.getParenLevel())
    }
}