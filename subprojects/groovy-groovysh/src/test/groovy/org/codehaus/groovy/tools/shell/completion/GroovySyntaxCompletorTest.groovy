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
package org.codehaus.groovy.tools.shell.completion

import groovy.mock.interceptor.MockFor
import org.codehaus.groovy.tools.shell.BufferManager
import org.codehaus.groovy.tools.shell.CommandRegistry
import org.codehaus.groovy.tools.shell.CompletorTestSupport
import org.codehaus.groovy.tools.shell.Groovysh

final class GroovySyntaxCompletorTest extends CompletorTestSupport {

    private final BufferManager bufferManager = new BufferManager()
    private final List<CharSequence> candidates = []

    private int runTest(String buffer, int cursor = buffer.length(), FileNameCompleter fileNameCompleter = null) {
        if (buffer) {
            def registry = new CommandRegistry()
            groovyshMocker.demand.getRegistry(1) { registry }
            groovyshMocker.demand.getBuffers(0..1) { bufferManager }
        }

        int result = Integer.MIN_VALUE
        groovyshMocker.use {
            Groovysh groovysh = new Groovysh()
            IdentifierCompletor identifierCompletor = idCompletorMocker.proxyDelegateInstance()
            ReflectionCompletor reflectionCompletor = reflectionCompletorMocker.proxyInstance(groovysh)
            GroovySyntaxCompletor completor = new GroovySyntaxCompletor(groovysh, reflectionCompletor, identifierCompletor, [identifierCompletor], fileNameCompleter)

            result = completor.complete(buffer, cursor, candidates)
        }
        return result
    }

    void testEmpty() {
        int result = runTest('')

        assert result == -1
        assert candidates.isEmpty()
    }

    void testIdentifier() {
        idCompletorMocker.demand.complete(1) { tokens, candidates ->
            assert(tokens*.text == ['jav']); candidates << 'javup'; candidates << 'java.lang.String' ; true}

        int result = runTest('jav')

        assert result == 0
        assert candidates == ['javup', 'java.lang.String']
    }

    void testIdentifierAfterLCurly() {
        idCompletorMocker.demand.complete(1) { tokens, candidates ->
            assert(tokens*.text == ['{', 'jav']); candidates << 'javup'; candidates << 'java.lang.String' ; true}

        int result = runTest('{jav')

        assert result == 1
        assert candidates == ['javup', 'java.lang.String']
    }

    void testMember() {
        reflectionCompletorMocker.demand.complete(1) { tokens, candidates ->
            assert(tokens*.text == ['Math', '.', 'ma']); candidates << 'max('; 5}

        int result = runTest('Math.ma')

        assert result == 5
        assert candidates == ['max(']
    }

    void testMemberOptionalDot() {
        reflectionCompletorMocker.demand.complete(1) { tokens, candidates ->
            assert(tokens*.text == ['Math', '?.', 'ma']); candidates << 'max('; 6}

        int result = runTest('Math?.ma')

        assert result == 6
        assert candidates == ['max(']
    }

    void testMemberSpreadDot() {
        reflectionCompletorMocker.demand.complete(1) { tokens, candidates ->
            assert(tokens*.text == ['[', 'foo', ']', '*.', 'len']); candidates << 'length()'; 9}

        int result = runTest('[\'foo\']*.len')

        assert result == 9
        assert candidates == ['length()']
    }

    void testMemberAfterMethod() {
        reflectionCompletorMocker.demand.complete(1) { tokens, candidates ->
            assert(tokens*.text == ['Fo', '.', 'ba', '(', ')', '.', 'xyz']); candidates << 'xyzabc'; 0}

        // xyz cannot be not a var here
        int result = runTest('Fo.ba().xyz')

        assert result == 0
        assert candidates == ['xyzabc']
    }

    void testIdentfierAfterDotAfterParens() {
        idCompletorMocker.demand.complete(1) { tokens, candidates ->
            assert(tokens*.text == ['Foo', '.', 'bar', '(', 'xyz']); candidates << 'xyzabc'; true}

        int result = runTest('Foo.bar(xyz')

        assert result == 8
        assert candidates == ['xyzabc']
    }

    void testIndentifierAfterParensBeforeDot() {
        idCompletorMocker.demand.complete(1) { tokens, candidates ->
            assert(tokens*.text == ['Foo', '.', 'bar', '(', 'xyz']); candidates << 'xyzabc'; true}

        int result = runTest('Foo.bar(xyz.', 'Foo.bar(xyz'.length()) // cursor is BEFORE dot

        assert result == 8
        assert candidates == ['xyzabc']
    }

    void testDoubleIdentifier() {
        int result = runTest('String jav')

        assert result == -1
        assert candidates == []
    }

    void testInfixKeyword() {
        int result = runTest('class Foo ext')

        assert result == 10
        assert candidates == ['extends']
    }

    void testInstanceof() {
        idCompletorMocker.demand.complete(1) { tokens, candidates ->
            assert(tokens*.text == ['x', 'instanceof', 'P']); candidates << 'Property'; 13}

        int result = runTest('x instanceof P')

        assert result == 13
        assert candidates.contains('Property')
    }

    void testAfterSemi() {
        // evaluation of all is dangerous, but the reflectionCompletor has to deal with this
        reflectionCompletorMocker.demand.complete(1) { tokens, candidates ->
            assert(tokens*.text == ['deletehardDisk', '(', ')', ';', 'foo', '.', 'subs']); candidates << 'substring('; 22}

        int result = runTest('deletehardDisk(); foo.subs')

        assert result == 22
        assert candidates == ['substring(']
    }

    void testAfterOperator() {
        // evaluation of all is dangerous, but the reflectionCompletor has to deal with this
        reflectionCompletorMocker.demand.complete(1) { tokens, candidates ->
            assert(tokens*.text == ['a', '=', 'foo', '.', 'subs']); candidates << 'substring('; 9}

        int result = runTest('a = foo.subs')

        assert result == 9
        assert candidates == ['substring(']
    }

    void testDontEvaluateAfterCommand() {
        // import command prevents reflection completion
        int result = runTest('import foo')

        assert result == -1
        assert candidates == []
    }

    void _disabled_testAfterGString() { // should we prohibit this?
        int result = runTest('"\${foo.delete()}".subs') // GString not evaluated

        assert result == -1
        assert candidates == []
    }

    void testInStringFilename() {
        def filenameCompleterMocker = new MockFor(FileNameCompleter)
        String linestart = "foo('" // ends with apostrophe
        String pathstart = '/usr/foobar'

        filenameCompleterMocker.demand.complete(1) { bufferline, cursor, candidates ->
            assert bufferline == pathstart
            assert cursor == pathstart.length()
            candidates << 'foobar'
            5
        }
        filenameCompleterMocker.use {
            String buffer = linestart + pathstart
            int result = runTest(buffer, buffer.length(), new FileNameCompleter())

            assert result == "foo('/usr/".length()
            assert candidates == ['foobar']
        }
    }

    void testInStringFilenameBlanks() {
        // test with blanks (non-tokens) before the apostrophe
        def filenameCompleterMocker = new MockFor(FileNameCompleter)
        String linestart = 'x = \'' // ends with apostrophe
        String pathstart = '/usr/foobar'

        filenameCompleterMocker.demand.complete(1) {bufferline, cursor, candidates ->
            assert bufferline == pathstart
            assert cursor == pathstart.length()
            candidates << 'foobar'
            5
        }
        filenameCompleterMocker.use {
            String buffer = linestart + pathstart
            int result = runTest(buffer, buffer.length(), new FileNameCompleter())

            assert result == "x = '/usr/".length()
            assert candidates == ['foobar']
        }
    }

    void testInGString() {
        idCompletorMocker.demand.complete(1) { tokens, candidates ->
            assert(tokens*.text == ['', '{', 'foo']); candidates << 'foobar'; true}

        int result = runTest('"\${foo')

        assert result == 3
        assert candidates == ['foobar']
    }

    void testMultilineComplete() {
        reflectionCompletorMocker.demand.complete(1) { tokens, candidates ->
            assert(tokens*.text == ['xyz\nabc', '.', 'subs']); candidates << 'substring('; 7}
        bufferManager.buffers.add(['"""xyz'])
        bufferManager.setSelected(1)

        int result = runTest('abc""".subs')

        assert result == 7
        assert candidates == ['substring(']
    }
}
