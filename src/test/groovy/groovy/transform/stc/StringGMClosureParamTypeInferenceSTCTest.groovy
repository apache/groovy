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
package groovy.transform.stc

import org.junit.jupiter.api.Test

/**
 * Unit tests for static type checking : closure parameter type inference for {@link org.codehaus.groovy.runtime.StringGroovyMethods}.
 */
class StringGMClosureParamTypeInferenceSTCTest extends StaticTypeCheckingTestCase {

    @Test
    void testCollectReplacements() {
        assertScript '''
            assert "Groovy".collectReplacements { s -> s.equalsIgnoreCase('O') ? s.toUpperCase() : s } == 'GrOOvy'
        '''
    }

    @Test
    void testDropWhile() {
        assertScript '''
            def text = "Groovy"
            assert text.dropWhile{ it.charAt(0) < (char)'Z' } == 'roovy'
            assert text.dropWhile{ !it.equalsIgnoreCase('V') } == 'vy'
            assert text.dropWhile{ char it -> it < (char)'Z' } == 'roovy'
            assert text.dropWhile{ Character it -> it != (char)'v' } == 'vy'
        '''
    }

    @Test
    void testEachLine() {
        assertScript '''
        def text = """abc
def
ghi"""
        text.eachLine { line -> assert line.length() == 3 }
        text.eachLine { assert it.length() == 3 }
        text.eachLine { line, nb -> assert line.length() <= 3+nb }
'''
    }

    @Test
    void testEachLineWithStartValue() {
        assertScript '''
        def text = """abc
def
ghi"""
        text.eachLine(3) { line -> assert line.length() == 3 }
        text.eachLine(3) { assert it.length() == 3 }
        text.eachLine(3) { line, nb -> assert line.length() <= nb }
'''
    }

    @Test
    void testEachMatch() {
        shouldFailWithMessages '''
        'groovy'.eachMatch('(groo)(vy)') { groups ->
        }
        ''', 'More than one target method matches'
        assertScript '''
        'groovy'.eachMatch('(groo)(vy)') { List groups ->
            assert groups.size()==3
            assert groups[0] == 'groovy'
            assert groups[1] == 'groo'
        }
        '''
        assertScript '''
        'groovy'.eachMatch('(groo)(vy)') { full, a, b ->
            assert full == 'groovy'
            assert a.toUpperCase() == 'GROO'
        }
        '''
    }

    @Test
    void testEachMatchWithPattern() {
        shouldFailWithMessages '''
        'groovy'.eachMatch(~'(groo)(vy)') { groups ->
        }
        ''', 'More than one target method matches'
        assertScript '''
        'groovy'.eachMatch(~'(groo)(vy)') { List groups ->
            assert groups.size()==3
            assert groups[0] == 'groovy'
            assert groups[1] == 'groo'
        }
        '''
        assertScript '''
        'groovy'.eachMatch(~'(groo)(vy)') { full, a, b ->
            assert full == 'groovy'
            assert a.toUpperCase() == 'GROO'
        }
        '''
    }

    @Test
    void testFind() {
        checkFind("'foobarbaz'")
        checkFind('''"${'foobarbaz'}"''')
        checkFind("new StringBuilder('foobarbaz')")
    }

    private checkFind(CharSequence s) {
        assertScript 'assert ' + s + '.find("b(a)(r)") { full, a, r -> assert "BAR"=="B" + a.toUpperCase() + r.toUpperCase() }'
    }

    @Test
    void testFindPattern() {
        checkFindPattern("'foobarbaz'")
        checkFindPattern('''"${'foobarbaz'}"''')
        checkFindPattern("new StringBuilder('foobarbaz')")
    }

    private checkFindPattern(CharSequence s) {
        assertScript 'assert ' + s + '.find(~"b(a)(r)") { full, a, r -> assert "BAR"=="B" + a.toUpperCase() + r.toUpperCase() }'
    }

    @Test
    void testFindAll() {
        checkFindAll("'foobarbaz'")
        checkFindAll('''"${'foobarbaz'}"''')
        checkFindAll("new StringBuilder('foobarbaz')")
    }

    private checkFindAll(CharSequence s) {
        assertScript 'assert ' + s + '.findAll("b(a)([rz])") { full, a, rz -> assert "BA"=="B" + a.toUpperCase() }.size() == 2'
    }

    @Test
    void testFindAllPattern() {
        checkFindAllPattern("'foobarbaz'")
        checkFindAllPattern('''"${'foobarbaz'}"''')
        checkFindAllPattern("new StringBuilder('foobarbaz')")
    }

    private checkFindAllPattern(CharSequence s) {
        assertScript 'assert ' + s + '.findAll(~"b(a)([rz])") { full, a, rz -> assert "BA"=="B" + a.toUpperCase() }.size() == 2'
    }

    @Test
    void testReplaceAll() {
        assertScript '''
            assert 'foobarbaz'.replaceAll('b(ar|az)') { List<String> it -> it[1].toUpperCase() } == 'fooARAZ'
            assert 'foobarbaz'.replaceAll('b(ar|az)') { full, sub -> full.toUpperCase() } == 'fooBARBAZ'
        '''
    }
    @Test
    void testReplaceAllWithPattern() {
        assertScript '''
            assert 'foobarbaz'.replaceAll(~'b(ar|az)') { List<String> it -> it[1].toUpperCase() } == 'fooARAZ\'
            assert 'foobarbaz'.replaceAll(~'b(ar|az)') { full, sub -> full.toUpperCase() } == 'fooBARBAZ\'
        '''
    }

    @Test
    void testReplaceFirst() {
        assertScript '''
            assert 'foobarbaz'.replaceFirst('b(ar|az)') { List<String> it -> it[1].toUpperCase() } == 'fooARbaz'
            assert 'foobarbaz'.replaceFirst('b(ar|az)') { full, sub -> full.toUpperCase() } == 'fooBARbaz'
        '''
    }
    @Test
    void testReplaceFirstWithPattern() {
        assertScript '''
            assert 'foobarbaz'.replaceFirst(~'b(ar|az)') { List<String> it -> it[1].toUpperCase() } == 'fooARbaz'
            assert 'foobarbaz'.replaceFirst(~'b(ar|az)') { full, sub -> full.toUpperCase() } == 'fooBARbaz'
        '''
    }

    @Test
    void testSplitEachLine() {
        assertScript '''
def text="""a,c
d:f
g,i"""
text.splitEachLine('([,:])') { a -> println a[0].toUpperCase() }
'''
    }

    @Test
    void testTakeWhileOnCharSeq() {
        assertScript '''
            String foo(CharSequence cs) { cs.takeWhile { it.charAt(0) < (char) 'j' }}
            assert foo("abcdefghijklmnopqrstuvwxyz") == 'abcdefghi'
        '''
    }
}
