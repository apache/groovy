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
package org.apache.groovy.groovysh.util

import groovy.test.GroovyTestCase

/**
 * Unit tests for the {@link CurlyLevelCountingGroovyLexer} class.
 */
class CurlyLevelCountingGroovyLexerTest extends GroovyTestCase {
   void testLexerEmpty() {
       CurlyLevelCountingGroovyLexer it = CurlyLevelCountingGroovyLexer.createGroovyLexer('')
       assert 0 == it.curlyLevel
       assert 0 == it.countCurlyLevel()
       assert 0 == it.curlyLevel
   }

    void testLexerText() {
        CurlyLevelCountingGroovyLexer it = CurlyLevelCountingGroovyLexer.createGroovyLexer('foo bar baz')
        assert 0 == it.curlyLevel
        assert 0 == it.countCurlyLevel()
        assert 0 == it.curlyLevel
    }

    void testLexerCurly() {
        CurlyLevelCountingGroovyLexer it = CurlyLevelCountingGroovyLexer.createGroovyLexer('Foo{')
        assert 0 == it.curlyLevel
        assert 1 == it.countCurlyLevel()
        assert 1 == it.curlyLevel
    }

    void testLexerCurlyMore() {
        CurlyLevelCountingGroovyLexer it = CurlyLevelCountingGroovyLexer.createGroovyLexer('Foo{Baz{Bar{')
        assert 0 == it.curlyLevel
        assert 3 == it.countCurlyLevel()
        assert 3 == it.curlyLevel
    }

    void testLexerCurlyMany() {
        CurlyLevelCountingGroovyLexer it = CurlyLevelCountingGroovyLexer.createGroovyLexer('Foo{Bar{}}{')
        assert 0 == it.curlyLevel
        assert 1 == it.countCurlyLevel()
        assert 1 == it.curlyLevel
    }
}
