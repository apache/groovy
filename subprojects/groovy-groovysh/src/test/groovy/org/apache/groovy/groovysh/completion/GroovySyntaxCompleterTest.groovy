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
package org.apache.groovy.groovysh.completion

import groovy.mock.interceptor.MockFor
import org.apache.groovy.groovysh.CommandRegistry
import org.apache.groovy.groovysh.CompleterTestSupport
import org.apache.groovy.groovysh.Groovysh
import org.apache.groovy.groovysh.commands.ImportCommand

class GroovySyntaxCompleterTest extends CompleterTestSupport {

    void testEmpty() {
        IdentifierCompleter mockIdCompleter = idCompleterMocker.proxyDelegateInstance()
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            ReflectionCompleter mockReflComp = reflectionCompleterMocker.proxyInstance(groovyshMock)
            GroovySyntaxCompleter completer = new GroovySyntaxCompleter(groovyshMock, mockReflComp, mockIdCompleter, [mockIdCompleter], null)
            assert -1 == completer.complete('', 0, [])
        }
    }

    void testIdentifier() {
        idCompleterMocker.demand.complete(1) { tokens, candidates ->
            assert(tokens*.text == ['jav']); candidates << 'javup'; candidates << 'java.lang.String' ; true}
        IdentifierCompleter mockIdCompleter = idCompleterMocker.proxyDelegateInstance()
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            ReflectionCompleter mockReflComp = reflectionCompleterMocker.proxyInstance(groovyshMock)
            GroovySyntaxCompleter completer = new GroovySyntaxCompleter(groovyshMock, mockReflComp, mockIdCompleter, [mockIdCompleter], null)
            def candidates = []
            String buffer = 'jav'
            // in the shell, only Classes in the default package occur,but well...
            assert 0 == completer.complete(buffer, buffer.length(), candidates)
            assert ['javup', 'java.lang.String'] == candidates
        }
    }

    void testIdentifierAfterLCurly() {
        idCompleterMocker.demand.complete(1) { tokens, candidates ->
            assert(tokens*.text == ['{', 'jav']); candidates << 'javup'; candidates << 'java.lang.String' ; true}
        IdentifierCompleter mockIdCompleter = idCompleterMocker.proxyDelegateInstance()
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            ReflectionCompleter mockReflComp = reflectionCompleterMocker.proxyInstance(groovyshMock)
            GroovySyntaxCompleter completer = new GroovySyntaxCompleter(groovyshMock, mockReflComp, mockIdCompleter, [mockIdCompleter], null)
            def candidates = []
            String buffer = '{jav'
            // in the shell, only Classes in the default package occur,but well...
            assert 1 == completer.complete(buffer, buffer.length(), candidates)
            assert ['javup', 'java.lang.String'] == candidates
        }
    }

    void testMember() {
        reflectionCompleterMocker.demand.complete(1) { tokens, candidates ->
            assert(tokens*.text == ['Math', '.', 'ma']); candidates << 'max('; 5}
        IdentifierCompleter mockIdCompleter = idCompleterMocker.proxyDelegateInstance()
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            ReflectionCompleter mockReflComp = reflectionCompleterMocker.proxyInstance(groovyshMock)
            GroovySyntaxCompleter completer = new GroovySyntaxCompleter(groovyshMock, mockReflComp, mockIdCompleter, [mockIdCompleter], null)
            def candidates = []
            String buffer = 'Math.ma'
            assert 5 == completer.complete(buffer, buffer.length(), candidates)
            assert ['max('] == candidates
        }
    }

    void testMemberOptionalDot() {
        reflectionCompleterMocker.demand.complete(1) { tokens, candidates ->
            assert(tokens*.text == ['Math', '?.', 'ma']); candidates << 'max('; 6}
        IdentifierCompleter mockIdCompleter = idCompleterMocker.proxyDelegateInstance()
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            ReflectionCompleter mockReflComp = reflectionCompleterMocker.proxyInstance(groovyshMock)
            GroovySyntaxCompleter completer = new GroovySyntaxCompleter(groovyshMock, mockReflComp, mockIdCompleter, [mockIdCompleter], null)
            def candidates = []
            String buffer = 'Math?.ma'
            assert 6 == completer.complete(buffer, buffer.length(), candidates)
            assert ['max('] == candidates
        }
    }

    void testMemberSpreadDot() {
        reflectionCompleterMocker.demand.complete(1) { tokens, candidates ->
            assert(tokens*.text == ['[', 'foo', ']', '*.', 'len']); candidates << 'length()'; 9}
        IdentifierCompleter mockIdCompleter = idCompleterMocker.proxyDelegateInstance()
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            ReflectionCompleter mockReflComp = reflectionCompleterMocker.proxyInstance(groovyshMock)
            GroovySyntaxCompleter completer = new GroovySyntaxCompleter(groovyshMock, mockReflComp, mockIdCompleter, [mockIdCompleter], null)
            def candidates = []
            String buffer = '[\'foo\']*.len'
            assert 9 == completer.complete(buffer, buffer.length(), candidates)
            assert ['length()'] == candidates
        }
    }

    void testMemberAfterMethod() {
        reflectionCompleterMocker.demand.complete(1) { tokens, candidates ->
            assert(tokens*.text == ['Fo', '.', 'ba', '(', ')', '.', 'xyz']); candidates << 'xyzabc'; 0}
        IdentifierCompleter mockIdCompleter = idCompleterMocker.proxyDelegateInstance()
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            ReflectionCompleter mockReflComp = reflectionCompleterMocker.proxyInstance(groovyshMock)
            GroovySyntaxCompleter completer = new GroovySyntaxCompleter(groovyshMock, mockReflComp, mockIdCompleter, [mockIdCompleter], null)
            def candidates = []
            String buffer = 'Fo.ba().xyz'
            // xyz cannot be not a var here
            assert 0 == completer.complete(buffer, buffer.length(), candidates)
            assert ['xyzabc'] == candidates
        }
    }

    void testIdentfierAfterDotAfterParens() {
        idCompleterMocker.demand.complete(1) { tokens, candidates ->
            assert(tokens*.text == ['Foo', '.', 'bar', '(', 'xyz']); candidates << 'xyzabc'; true}
        IdentifierCompleter mockIdCompleter = idCompleterMocker.proxyDelegateInstance()
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            ReflectionCompleter mockReflComp = reflectionCompleterMocker.proxyInstance(groovyshMock)
            GroovySyntaxCompleter completer = new GroovySyntaxCompleter(groovyshMock, mockReflComp, mockIdCompleter, [mockIdCompleter], null)
            def candidates = []
            String buffer = 'Foo.bar(xyz'
            assert 8 == completer.complete(buffer, buffer.length(), candidates)
            assert ['xyzabc'] == candidates
        }
    }

    void testIndentifierAfterParensBeforeDot() {
        idCompleterMocker.demand.complete(1) { tokens, candidates ->
            assert(tokens*.text == ['Foo', '.', 'bar', '(', 'xyz']); candidates << 'xyzabc'; true}
        IdentifierCompleter mockIdCompleter = idCompleterMocker.proxyDelegateInstance()
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            ReflectionCompleter mockReflComp = reflectionCompleterMocker.proxyInstance(groovyshMock)
            GroovySyntaxCompleter completer = new GroovySyntaxCompleter(groovyshMock, mockReflComp, mockIdCompleter, [mockIdCompleter], null)
            def candidates = []
            // cursor is BEFORE dot
            assert 8 == completer.complete('Foo.bar(xyz.', 'Foo.bar(xyz'.length(), candidates)
            assert ['xyzabc'] == candidates
        }
    }

    void testDoubleIdentifier() {
        IdentifierCompleter mockIdCompleter = idCompleterMocker.proxyDelegateInstance()
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            ReflectionCompleter mockReflComp = reflectionCompleterMocker.proxyInstance(groovyshMock)
            GroovySyntaxCompleter completer = new GroovySyntaxCompleter(groovyshMock, mockReflComp, mockIdCompleter, [mockIdCompleter], null)
            def candidates = []
            String buffer = 'String jav'
            assert -1 == completer.complete(buffer, buffer.length(), candidates)
            assert [] == candidates
        }
    }

    void testInfixKeyword() {
        IdentifierCompleter mockIdCompleter = idCompleterMocker.proxyDelegateInstance()
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            ReflectionCompleter mockReflComp = reflectionCompleterMocker.proxyInstance(groovyshMock)
            GroovySyntaxCompleter completer = new GroovySyntaxCompleter(groovyshMock, mockReflComp, mockIdCompleter, [mockIdCompleter], null)
            def candidates = []
            String buffer = 'class Foo ext'
            assert 10 == completer.complete(buffer, buffer.length(), candidates)
            assert ['extends'] == candidates
        }
    }

    void testInstanceof() {
        idCompleterMocker.demand.complete(1) { tokens, candidates ->
            assert(tokens*.text == ['x', 'instanceof', 'P']); candidates << 'Property'; 13}
        IdentifierCompleter mockIdCompleter = idCompleterMocker.proxyDelegateInstance()

        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            ReflectionCompleter mockReflComp = reflectionCompleterMocker.proxyInstance(groovyshMock)
            GroovySyntaxCompleter completer = new GroovySyntaxCompleter(groovyshMock, mockReflComp, mockIdCompleter, [mockIdCompleter], null)
            def candidates = []
            String buffer = 'x instanceof P'
            assert 13 == completer.complete(buffer, buffer.length(), candidates)
            assert 'Property' in candidates
        }
    }


    void testAfterSemi() {
        // evaluation of all is dangerous, but the reflectionCompleter has to deal with this
        reflectionCompleterMocker.demand.complete(1) { tokens, candidates ->
            assert(tokens*.text == ['deletehardDisk', '(', ')', ';', 'foo', '.', 'subs']); candidates << 'substring('; 22}

        IdentifierCompleter mockIdCompleter = idCompleterMocker.proxyDelegateInstance()
        // mock doing the right thing
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            ReflectionCompleter mockReflComp = reflectionCompleterMocker.proxyInstance(groovyshMock)
            GroovySyntaxCompleter completer = new GroovySyntaxCompleter(groovyshMock, mockReflComp, mockIdCompleter, [mockIdCompleter], null)
            def candidates = []
            String buffer = 'deletehardDisk(); foo.subs'
            assert 22 == completer.complete(buffer, buffer.length(), candidates)
            assert ['substring('] == candidates
        }
    }

    void testAfterOperator() {
        // evaluation of all is dangerous, but the reflectionCompleter has to deal with this
        reflectionCompleterMocker.demand.complete(1) { tokens, candidates ->
            assert(tokens*.text == ['a', '=', 'foo', '.', 'subs']); candidates << 'substring('; 9}

        IdentifierCompleter mockIdCompleter = idCompleterMocker.proxyDelegateInstance()
        // mock doing the right thing
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            ReflectionCompleter mockReflComp = reflectionCompleterMocker.proxyInstance(groovyshMock)
            GroovySyntaxCompleter completer = new GroovySyntaxCompleter(groovyshMock, mockReflComp, mockIdCompleter, [mockIdCompleter], null)
            def candidates = []
            String buffer = 'a = foo.subs'
            assert 9 == completer.complete(buffer, buffer.length(), candidates)
            assert ['substring('] == candidates
        }
    }

    void testDontEvaluateAfterCommand() {
        CommandRegistry registry = new CommandRegistry()
        IdentifierCompleter mockIdCompleter = idCompleterMocker.proxyDelegateInstance()
        // mock asserting nothing gets evaluated
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            ReflectionCompleter mockReflComp = reflectionCompleterMocker.proxyInstance(groovyshMock)
            // import command prevents reflection completion
            registry.register(new ImportCommand(groovyshMock))
            GroovySyntaxCompleter completer = new GroovySyntaxCompleter(groovyshMock, mockReflComp, mockIdCompleter, [mockIdCompleter], null)
            def candidates = []
            String buffer = 'import foo'
            assert -1 == completer.complete(buffer, buffer.length(), candidates)
            assert [] == candidates
        }
    }

    void _disabled_testAfterGString() { // should we prohibit this?
        IdentifierCompleter mockIdCompleter = idCompleterMocker.proxyDelegateInstance()
        // mock asserting GString is not evaluated
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            ReflectionCompleter mockReflComp = reflectionCompleterMocker.proxyInstance(groovyshMock)
            GroovySyntaxCompleter completer = new GroovySyntaxCompleter(groovyshMock, mockReflComp, mockIdCompleter, [mockIdCompleter], null)
            def candidates = []
            String buffer = '"\${foo.delete()}".subs'
            assert candidates == [] && -1 == completer.complete(buffer, buffer.length(), candidates)
        }
    }

    void testInStringFilename() {
        IdentifierCompleter mockIdCompleter = idCompleterMocker.proxyDelegateInstance()
        MockFor filenameCompleterMocker = new MockFor(FileNameCompleter)
        String linestart = /foo('/ // ends with single hyphen
        String pathstart = '/usr/foobar'
        String buffer = linestart + pathstart
        filenameCompleterMocker.demand.complete(1) {bufferline, cursor, candidates ->
            assert(bufferline == pathstart)
            assert(cursor == pathstart.length())
            candidates << 'foobar'; 5}
        groovyshMocker.use { filenameCompleterMocker.use {
            FileNameCompleter mockFileComp = new FileNameCompleter()
            Groovysh groovyshMock = new Groovysh()
            ReflectionCompleter mockReflComp = reflectionCompleterMocker.proxyInstance(groovyshMock)
            GroovySyntaxCompleter completer = new GroovySyntaxCompleter(groovyshMock, mockReflComp, mockIdCompleter, [mockIdCompleter], mockFileComp)
            def candidates = []
            assert 'foo(\'/usr/'.length() == completer.complete(buffer, buffer.length(), candidates)
            assert ['foobar'] == candidates
        }}
    }

    void testInStringFilenameBlanks() {
        // test with blanks (non-tokens) before the first hyphen
        IdentifierCompleter mockIdCompleter = idCompleterMocker.proxyDelegateInstance()
        MockFor filenameCompleterMocker = new MockFor(FileNameCompleter)
        String linestart = 'x = \'' // ends with single hyphen
        String pathstart = '/usr/foobar'
        String buffer = linestart + pathstart
        filenameCompleterMocker.demand.complete(1) {bufferline, cursor, candidates ->
            assert bufferline == pathstart
            assert cursor == pathstart.length()
            candidates << 'foobar'; 5}
        groovyshMocker.use { filenameCompleterMocker.use {
            FileNameCompleter mockFileComp = new FileNameCompleter()
            Groovysh groovyshMock = new Groovysh()
            ReflectionCompleter mockReflComp = reflectionCompleterMocker.proxyInstance(groovyshMock)
            GroovySyntaxCompleter completer = new GroovySyntaxCompleter(groovyshMock, mockReflComp, mockIdCompleter, [mockIdCompleter], mockFileComp)
            def candidates = []
            assert 'x = \'/usr/'.length() == completer.complete(buffer, buffer.length(), candidates)
            assert ['foobar'] == candidates
        }}
    }

    void testInGString() {
        idCompleterMocker.demand.complete(1) { tokens, candidates ->
            assert(tokens*.text == ['', '{', 'foo']); candidates << 'foobar'; true}
        IdentifierCompleter mockIdCompleter = idCompleterMocker.proxyDelegateInstance()
        // mock asserting GString is not evaluated
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            ReflectionCompleter mockReflComp = reflectionCompleterMocker.proxyInstance(groovyshMock)
            GroovySyntaxCompleter completer = new GroovySyntaxCompleter(groovyshMock, mockReflComp, mockIdCompleter, [mockIdCompleter], null)
            def candidates = []
            String buffer = '"\${foo'
            assert 3 == completer.complete(buffer, buffer.length(), candidates)
            assert ['foobar'] == candidates
        }
    }

    void testMultilineComplete() {
        reflectionCompleterMocker.demand.complete(1) { tokens, candidates ->
            assert(tokens*.text == ['xyz\nabc', '.', 'subs']); candidates << 'substring('; 7}
        bufferManager.buffers.add(['"""xyz'])
        bufferManager.setSelected(1)
        IdentifierCompleter mockIdCompleter = idCompleterMocker.proxyDelegateInstance()
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            ReflectionCompleter mockReflComp = reflectionCompleterMocker.proxyInstance(groovyshMock)
            GroovySyntaxCompleter completer = new GroovySyntaxCompleter(groovyshMock, mockReflComp, mockIdCompleter, [mockIdCompleter], null)
            def candidates = []
            String buffer = 'abc""".subs'
            assert 7 == completer.complete(buffer, buffer.length(), candidates)
            assert ['substring('] == candidates
        }
    }
}

