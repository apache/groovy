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
import org.codehaus.groovy.tools.shell.CommandRegistry
import org.codehaus.groovy.tools.shell.CompletorTestSupport
import org.codehaus.groovy.tools.shell.Groovysh
import org.codehaus.groovy.tools.shell.commands.ImportCommand

class GroovySyntaxCompletorTest extends CompletorTestSupport {

    void testEmpty() {
        IdentifierCompletor mockIdCompletor = idCompletorMocker.proxyDelegateInstance()
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            ReflectionCompletor mockReflComp = reflectionCompletorMocker.proxyInstance(groovyshMock)
            GroovySyntaxCompletor completor = new GroovySyntaxCompletor(groovyshMock, mockReflComp, mockIdCompletor, [mockIdCompletor], null)
            assert -1 == completor.complete('', 0, [])
        }
    }

    void testIdentifier() {
        idCompletorMocker.demand.complete(1) { tokens, candidates ->
            assert(tokens*.text == ['jav']); candidates << 'javup'; candidates << 'java.lang.String' ; true}
        IdentifierCompletor mockIdCompletor = idCompletorMocker.proxyDelegateInstance()
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            ReflectionCompletor mockReflComp = reflectionCompletorMocker.proxyInstance(groovyshMock)
            GroovySyntaxCompletor completor = new GroovySyntaxCompletor(groovyshMock, mockReflComp, mockIdCompletor, [mockIdCompletor], null)
            def candidates = []
            String buffer = 'jav'
            // in the shell, only Classes in the default package occur,but well...
            assert 0 == completor.complete(buffer, buffer.length(), candidates)
            assert ['javup', 'java.lang.String'] == candidates
        }
    }

    void testIdentifierAfterLCurly() {
        idCompletorMocker.demand.complete(1) { tokens, candidates ->
            assert(tokens*.text == ['{', 'jav']); candidates << 'javup'; candidates << 'java.lang.String' ; true}
        IdentifierCompletor mockIdCompletor = idCompletorMocker.proxyDelegateInstance()
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            ReflectionCompletor mockReflComp = reflectionCompletorMocker.proxyInstance(groovyshMock)
            GroovySyntaxCompletor completor = new GroovySyntaxCompletor(groovyshMock, mockReflComp, mockIdCompletor, [mockIdCompletor], null)
            def candidates = []
            String buffer = '{jav'
            // in the shell, only Classes in the default package occur,but well...
            assert 1 == completor.complete(buffer, buffer.length(), candidates)
            assert ['javup', 'java.lang.String'] == candidates
        }
    }

    void testMember() {
        reflectionCompletorMocker.demand.complete(1) { tokens, candidates ->
            assert(tokens*.text == ['Math', '.', 'ma']); candidates << 'max('; 5}
        IdentifierCompletor mockIdCompletor = idCompletorMocker.proxyDelegateInstance()
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            ReflectionCompletor mockReflComp = reflectionCompletorMocker.proxyInstance(groovyshMock)
            GroovySyntaxCompletor completor = new GroovySyntaxCompletor(groovyshMock, mockReflComp, mockIdCompletor, [mockIdCompletor], null)
            def candidates = []
            String buffer = 'Math.ma'
            assert 5 == completor.complete(buffer, buffer.length(), candidates)
            assert ['max('] == candidates
        }
    }

    void testMemberOptionalDot() {
        reflectionCompletorMocker.demand.complete(1) { tokens, candidates ->
            assert(tokens*.text == ['Math', '?.', 'ma']); candidates << 'max('; 6}
        IdentifierCompletor mockIdCompletor = idCompletorMocker.proxyDelegateInstance()
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            ReflectionCompletor mockReflComp = reflectionCompletorMocker.proxyInstance(groovyshMock)
            GroovySyntaxCompletor completor = new GroovySyntaxCompletor(groovyshMock, mockReflComp, mockIdCompletor, [mockIdCompletor], null)
            def candidates = []
            String buffer = 'Math?.ma'
            assert 6 == completor.complete(buffer, buffer.length(), candidates)
            assert ['max('] == candidates
        }
    }

    void testMemberSpreadDot() {
        reflectionCompletorMocker.demand.complete(1) { tokens, candidates ->
            assert(tokens*.text == ['[', 'foo', ']', '*.', 'len']); candidates << 'length()'; 9}
        IdentifierCompletor mockIdCompletor = idCompletorMocker.proxyDelegateInstance()
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            ReflectionCompletor mockReflComp = reflectionCompletorMocker.proxyInstance(groovyshMock)
            GroovySyntaxCompletor completor = new GroovySyntaxCompletor(groovyshMock, mockReflComp, mockIdCompletor, [mockIdCompletor], null)
            def candidates = []
            String buffer = '[\'foo\']*.len'
            assert 9 == completor.complete(buffer, buffer.length(), candidates)
            assert ['length()'] == candidates
        }
    }

    void testMemberAfterMethod() {
        reflectionCompletorMocker.demand.complete(1) { tokens, candidates ->
            assert(tokens*.text == ['Fo', '.', 'ba', '(', ')', '.', 'xyz']); candidates << 'xyzabc'; 0}
        IdentifierCompletor mockIdCompletor = idCompletorMocker.proxyDelegateInstance()
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            ReflectionCompletor mockReflComp = reflectionCompletorMocker.proxyInstance(groovyshMock)
            GroovySyntaxCompletor completor = new GroovySyntaxCompletor(groovyshMock, mockReflComp, mockIdCompletor, [mockIdCompletor], null)
            def candidates = []
            String buffer = 'Fo.ba().xyz'
            // xyz cannot be not a var here
            assert 0 == completor.complete(buffer, buffer.length(), candidates)
            assert ['xyzabc'] == candidates
        }
    }

    void testIdentfierAfterDotAfterParens() {
        idCompletorMocker.demand.complete(1) { tokens, candidates ->
            assert(tokens*.text == ['Foo', '.', 'bar', '(', 'xyz']); candidates << 'xyzabc'; true}
        IdentifierCompletor mockIdCompletor = idCompletorMocker.proxyDelegateInstance()
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            ReflectionCompletor mockReflComp = reflectionCompletorMocker.proxyInstance(groovyshMock)
            GroovySyntaxCompletor completor = new GroovySyntaxCompletor(groovyshMock, mockReflComp, mockIdCompletor, [mockIdCompletor], null)
            def candidates = []
            String buffer = 'Foo.bar(xyz'
            assert 8 == completor.complete(buffer, buffer.length(), candidates)
            assert ['xyzabc'] == candidates
        }
    }

    void testIndentifierAfterParensBeforeDot() {
        idCompletorMocker.demand.complete(1) { tokens, candidates ->
            assert(tokens*.text == ['Foo', '.', 'bar', '(', 'xyz']); candidates << 'xyzabc'; true}
        IdentifierCompletor mockIdCompletor = idCompletorMocker.proxyDelegateInstance()
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            ReflectionCompletor mockReflComp = reflectionCompletorMocker.proxyInstance(groovyshMock)
            GroovySyntaxCompletor completor = new GroovySyntaxCompletor(groovyshMock, mockReflComp, mockIdCompletor, [mockIdCompletor], null)
            def candidates = []
            // cursor is BEFORE dot
            assert 8 == completor.complete('Foo.bar(xyz.', 'Foo.bar(xyz'.length(), candidates)
            assert ['xyzabc'] == candidates
        }
    }

    void testDoubleIdentifier() {
        IdentifierCompletor mockIdCompletor = idCompletorMocker.proxyDelegateInstance()
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            ReflectionCompletor mockReflComp = reflectionCompletorMocker.proxyInstance(groovyshMock)
            GroovySyntaxCompletor completor = new GroovySyntaxCompletor(groovyshMock, mockReflComp, mockIdCompletor, [mockIdCompletor], null)
            def candidates = []
            String buffer = 'String jav'
            assert -1 == completor.complete(buffer, buffer.length(), candidates)
            assert [] == candidates
        }
    }

    void testInfixKeyword() {
        IdentifierCompletor mockIdCompletor = idCompletorMocker.proxyDelegateInstance()
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            ReflectionCompletor mockReflComp = reflectionCompletorMocker.proxyInstance(groovyshMock)
            GroovySyntaxCompletor completor = new GroovySyntaxCompletor(groovyshMock, mockReflComp, mockIdCompletor, [mockIdCompletor], null)
            def candidates = []
            String buffer = 'class Foo ext'
            assert 10 == completor.complete(buffer, buffer.length(), candidates)
            assert ['extends'] == candidates
        }
    }

    void testInstanceof() {
        idCompletorMocker.demand.complete(1) { tokens, candidates ->
            assert(tokens*.text == ['x', 'instanceof', 'P']); candidates << 'Property'; 13}
        IdentifierCompletor mockIdCompletor = idCompletorMocker.proxyDelegateInstance()

        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            ReflectionCompletor mockReflComp = reflectionCompletorMocker.proxyInstance(groovyshMock)
            GroovySyntaxCompletor completor = new GroovySyntaxCompletor(groovyshMock, mockReflComp, mockIdCompletor, [mockIdCompletor], null)
            def candidates = []
            String buffer = 'x instanceof P'
            assert 13 == completor.complete(buffer, buffer.length(), candidates)
            assert 'Property' in candidates
        }
    }


    void testAfterSemi() {
        // evaluation of all is dangerous, but the reflectionCompleter has to deal with this
        reflectionCompletorMocker.demand.complete(1) { tokens, candidates ->
            assert(tokens*.text == ['deletehardDisk', '(', ')', ';', 'foo', '.', 'subs']); candidates << 'substring('; 22}

        IdentifierCompletor mockIdCompletor = idCompletorMocker.proxyDelegateInstance()
        // mock doing the right thing
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            ReflectionCompletor mockReflComp = reflectionCompletorMocker.proxyInstance(groovyshMock)
            GroovySyntaxCompletor completor = new GroovySyntaxCompletor(groovyshMock, mockReflComp, mockIdCompletor, [mockIdCompletor], null)
            def candidates = []
            String buffer = 'deletehardDisk(); foo.subs'
            assert 22 == completor.complete(buffer, buffer.length(), candidates)
            assert ['substring('] == candidates
        }
    }

    void testAfterOperator() {
        // evaluation of all is dangerous, but the reflectionCompleter has to deal with this
        reflectionCompletorMocker.demand.complete(1) { tokens, candidates ->
            assert(tokens*.text == ['a', '=', 'foo', '.', 'subs']); candidates << 'substring('; 9}

        IdentifierCompletor mockIdCompletor = idCompletorMocker.proxyDelegateInstance()
        // mock doing the right thing
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            ReflectionCompletor mockReflComp = reflectionCompletorMocker.proxyInstance(groovyshMock)
            GroovySyntaxCompletor completor = new GroovySyntaxCompletor(groovyshMock, mockReflComp, mockIdCompletor, [mockIdCompletor], null)
            def candidates = []
            String buffer = 'a = foo.subs'
            assert 9 == completor.complete(buffer, buffer.length(), candidates)
            assert ['substring('] == candidates
        }
    }

    void testDontEvaluateAfterCommand() {
        CommandRegistry registry = new CommandRegistry()
        IdentifierCompletor mockIdCompletor = idCompletorMocker.proxyDelegateInstance()
        // mock asserting nothing gets evaluated
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            ReflectionCompletor mockReflComp = reflectionCompletorMocker.proxyInstance(groovyshMock)
            // import command prevents reflection completion
            registry.register(new ImportCommand(groovyshMock))
            GroovySyntaxCompletor completor = new GroovySyntaxCompletor(groovyshMock, mockReflComp, mockIdCompletor, [mockIdCompletor], null)
            def candidates = []
            String buffer = 'import foo'
            assert -1 == completor.complete(buffer, buffer.length(), candidates)
            assert [] == candidates
        }
    }

    void _disabled_testAfterGString() { // should we prohibit this?
        IdentifierCompletor mockIdCompletor = idCompletorMocker.proxyDelegateInstance()
        // mock asserting GString is not evaluated
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            ReflectionCompletor mockReflComp = reflectionCompletorMocker.proxyInstance(groovyshMock)
            GroovySyntaxCompletor completor = new GroovySyntaxCompletor(groovyshMock, mockReflComp, mockIdCompletor, [mockIdCompletor], null)
            def candidates = []
            String buffer = '"\${foo.delete()}".subs'
            assert candidates == [] && -1 == completor.complete(buffer, buffer.length(), candidates)
        }
    }

    void testInStringFilename() {
        IdentifierCompletor mockIdCompletor = idCompletorMocker.proxyDelegateInstance()
        MockFor filenameCompletorMocker = new MockFor(FileNameCompleter)
        String linestart = /foo('/ // ends with single hyphen
        String pathstart = '/usr/foobar'
        String buffer = linestart + pathstart
        filenameCompletorMocker.demand.complete(1) {bufferline, cursor, candidates ->
            assert(bufferline == pathstart)
            assert(cursor == pathstart.length())
            candidates << 'foobar'; 5}
        groovyshMocker.use { filenameCompletorMocker.use {
            FileNameCompleter mockFileComp = new FileNameCompleter()
            Groovysh groovyshMock = new Groovysh()
            ReflectionCompletor mockReflComp = reflectionCompletorMocker.proxyInstance(groovyshMock)
            GroovySyntaxCompletor completor = new GroovySyntaxCompletor(groovyshMock, mockReflComp, mockIdCompletor, [mockIdCompletor], mockFileComp)
            def candidates = []
            assert 'foo(\'/usr/'.length() == completor.complete(buffer, buffer.length(), candidates)
            assert ['foobar'] == candidates
        }}
    }

    void testInStringFilenameBlanks() {
        // test with blanks (non-tokens) before the first hyphen
        IdentifierCompletor mockIdCompletor = idCompletorMocker.proxyDelegateInstance()
        MockFor filenameCompletorMocker = new MockFor(FileNameCompleter)
        String linestart = 'x = \'' // ends with single hyphen
        String pathstart = '/usr/foobar'
        String buffer = linestart + pathstart
        filenameCompletorMocker.demand.complete(1) {bufferline, cursor, candidates ->
            assert bufferline == pathstart
            assert cursor == pathstart.length()
            candidates << 'foobar'; 5}
        groovyshMocker.use { filenameCompletorMocker.use {
            FileNameCompleter mockFileComp = new FileNameCompleter()
            Groovysh groovyshMock = new Groovysh()
            ReflectionCompletor mockReflComp = reflectionCompletorMocker.proxyInstance(groovyshMock)
            GroovySyntaxCompletor completor = new GroovySyntaxCompletor(groovyshMock, mockReflComp, mockIdCompletor, [mockIdCompletor], mockFileComp)
            def candidates = []
            assert 'x = \'/usr/'.length() == completor.complete(buffer, buffer.length(), candidates)
            assert ['foobar'] == candidates
        }}
    }

    void testInGString() {
        idCompletorMocker.demand.complete(1) { tokens, candidates ->
            assert(tokens*.text == ['', '{', 'foo']); candidates << 'foobar'; true}
        IdentifierCompletor mockIdCompletor = idCompletorMocker.proxyDelegateInstance()
        // mock asserting GString is not evaluated
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            ReflectionCompletor mockReflComp = reflectionCompletorMocker.proxyInstance(groovyshMock)
            GroovySyntaxCompletor completor = new GroovySyntaxCompletor(groovyshMock, mockReflComp, mockIdCompletor, [mockIdCompletor], null)
            def candidates = []
            String buffer = '"\${foo'
            assert 3 == completor.complete(buffer, buffer.length(), candidates)
            assert ['foobar'] == candidates
        }
    }

    void testMultilineComplete() {
        reflectionCompletorMocker.demand.complete(1) { tokens, candidates ->
            assert(tokens*.text == ['xyz\nabc', '.', 'subs']); candidates << 'substring('; 7}
        bufferManager.buffers.add(['"""xyz'])
        bufferManager.setSelected(1)
        IdentifierCompletor mockIdCompletor = idCompletorMocker.proxyDelegateInstance()
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            ReflectionCompletor mockReflComp = reflectionCompletorMocker.proxyInstance(groovyshMock)
            GroovySyntaxCompletor completor = new GroovySyntaxCompletor(groovyshMock, mockReflComp, mockIdCompletor, [mockIdCompletor], null)
            def candidates = []
            String buffer = 'abc""".subs'
            assert 7 == completor.complete(buffer, buffer.length(), candidates)
            assert ['substring('] == candidates
        }
    }
}

