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

/**
 * Unit tests for static type checking : closure parameter type inference for {@link org.codehaus.groovy.runtime.StringGroovyMethods}.
 */
class StringGMClosureParamTypeInferenceSTCTest extends StaticTypeCheckingTestCase {
    void testCollectReplacements() {
        assertScript '''
            assert "Groovy".collectReplacements { s -> s.equalsIgnoreCase('O') ? s.toUpperCase() : s } == 'GrOOvy'
        '''
    }

    void testDropWhile() {
        assertScript '''
            def text = "Groovy"
            assert text.dropWhile{ it.charAt(0) < (char)'Z' } == 'roovy'
            assert text.dropWhile{ !it.equalsIgnoreCase('V') } == 'vy'
            assert text.dropWhile{ char it -> it < (char)'Z' } == 'roovy'
            assert text.dropWhile{ Character it -> it != (char)'v' } == 'vy'
        '''
    }

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

    void testFind() {
        checkFind("'foobarbaz'")
        checkFind('''"${'foobarbaz'}"''')
        checkFind("new StringBuilder('foobarbaz')")
    }

    private checkFind(CharSequence s) {
        assertScript 'assert ' + s + '.find("b(a)(r)") { full, a, r -> assert "BAR"=="B" + a.toUpperCase() + r.toUpperCase() }'
    }

    void testFindPattern() {
        checkFindPattern("'foobarbaz'")
        checkFindPattern('''"${'foobarbaz'}"''')
        checkFindPattern("new StringBuilder('foobarbaz')")
    }

    private checkFindPattern(CharSequence s) {
        assertScript 'assert ' + s + '.find(~"b(a)(r)") { full, a, r -> assert "BAR"=="B" + a.toUpperCase() + r.toUpperCase() }'
    }

    void testFindAll() {
        checkFindAll("'foobarbaz'")
        checkFindAll('''"${'foobarbaz'}"''')
        checkFindAll("new StringBuilder('foobarbaz')")
    }

    private checkFindAll(CharSequence s) {
        assertScript 'assert ' + s + '.findAll("b(a)([rz])") { full, a, rz -> assert "BA"=="B" + a.toUpperCase() }.size() == 2'
    }

    void testFindAllPattern() {
        checkFindAllPattern("'foobarbaz'")
        checkFindAllPattern('''"${'foobarbaz'}"''')
        checkFindAllPattern("new StringBuilder('foobarbaz')")
    }

    private checkFindAllPattern(CharSequence s) {
        assertScript 'assert ' + s + '.findAll(~"b(a)([rz])") { full, a, rz -> assert "BA"=="B" + a.toUpperCase() }.size() == 2'
    }

    void testReplaceAll() {
        assertScript '''
            assert 'foobarbaz'.replaceAll('b(ar|az)') { List<String> it -> it[1].toUpperCase() } == 'fooARAZ'
            assert 'foobarbaz'.replaceAll('b(ar|az)') { full, sub -> full.toUpperCase() } == 'fooBARBAZ'
        '''
    }
    void testReplaceAllWithPattern() {
        assertScript '''
            assert 'foobarbaz'.replaceAll(~'b(ar|az)') { List<String> it -> it[1].toUpperCase() } == 'fooARAZ\'
            assert 'foobarbaz'.replaceAll(~'b(ar|az)') { full, sub -> full.toUpperCase() } == 'fooBARBAZ\'
        '''
    }

    void testReplaceFirst() {
        assertScript '''
            assert 'foobarbaz'.replaceFirst('b(ar|az)') { List<String> it -> it[1].toUpperCase() } == 'fooARbaz'
            assert 'foobarbaz'.replaceFirst('b(ar|az)') { full, sub -> full.toUpperCase() } == 'fooBARbaz'
        '''
    }
    void testReplaceFirstWithPattern() {
        assertScript '''
            assert 'foobarbaz'.replaceFirst(~'b(ar|az)') { List<String> it -> it[1].toUpperCase() } == 'fooARbaz'
            assert 'foobarbaz'.replaceFirst(~'b(ar|az)') { full, sub -> full.toUpperCase() } == 'fooBARbaz'
        '''
    }

    void testSplitEachLine() {
        assertScript '''
def text="""a,c
d:f
g,i"""
text.splitEachLine('([,:])') { a -> println a[0].toUpperCase() }
'''
    }

    void testTakeWhileOnCharSeq() {
        assertScript '''
            String foo(CharSequence cs) { cs.takeWhile { it.charAt(0) < (char) 'j' }}
            assert foo("abcdefghijklmnopqrstuvwxyz") == 'abcdefghi'
        '''
    }
}
