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

package org.codehaus.groovy.tools.shell.completion

import groovy.mock.interceptor.MockFor
import org.codehaus.groovy.tools.shell.CommandRegistry
import org.codehaus.groovy.tools.shell.CompletorTestSupport
import org.codehaus.groovy.tools.shell.Groovysh
import org.codehaus.groovy.tools.shell.commands.ImportCommand

class GroovySyntaxCompletorTest extends CompletorTestSupport {

    void testEmpty() {
        IdentifierCompletor mockIdCompletor = idCompletorMocker.proxyDelegateInstance()
        ReflectionCompletor mockReflComp = reflectionCompletorMocker.proxyInstance()
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            GroovySyntaxCompletor completor = new GroovySyntaxCompletor(groovyshMock, mockReflComp, [mockIdCompletor], null)
            assertEquals(-1, completor.complete("", 0, []))
        }
    }

    void testIdentifier() {
        idCompletorMocker.demand.complete(1) { tokens, candidates ->
            assert(tokens.collect{it.getText()} == ["jav"]); candidates << "javup"; candidates << "java.lang.String" ; true}
        IdentifierCompletor mockIdCompletor = idCompletorMocker.proxyDelegateInstance()
        ReflectionCompletor mockReflComp = reflectionCompletorMocker.proxyInstance()
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            GroovySyntaxCompletor completor = new GroovySyntaxCompletor(groovyshMock, mockReflComp, [mockIdCompletor], null)
            def candidates = []
            String buffer = "jav"
            // in the shell, only Classes in the default package occur,but well...
            assertEquals(0, completor.complete(buffer, buffer.length(), candidates))
            assertEquals(["javup", "java.lang.String"], candidates)
        }
    }

    void testIdentifierAfterLCurly() {
        idCompletorMocker.demand.complete(1) { tokens, candidates ->
            assert(tokens.collect{it.getText()} == ["{", "jav"]); candidates << "javup"; candidates << "java.lang.String" ; true}
        IdentifierCompletor mockIdCompletor = idCompletorMocker.proxyDelegateInstance()
        ReflectionCompletor mockReflComp = reflectionCompletorMocker.proxyInstance()
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            GroovySyntaxCompletor completor = new GroovySyntaxCompletor(groovyshMock, mockReflComp, [mockIdCompletor], null)
            def candidates = []
            String buffer = "{jav"
            // in the shell, only Classes in the default package occur,but well...
            assertEquals(1, completor.complete(buffer, buffer.length(), candidates))
            assertEquals(["javup", "java.lang.String"], candidates)
        }
    }

    void testMember() {
        reflectionCompletorMocker.demand.complete(1) { tokens, candidates ->
            assert(tokens.collect{it.getText()} == ["Math", ".", "ma"]); candidates << "max("; 5}
        IdentifierCompletor mockIdCompletor = idCompletorMocker.proxyDelegateInstance()
        ReflectionCompletor mockReflComp = reflectionCompletorMocker.proxyInstance()
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            GroovySyntaxCompletor completor = new GroovySyntaxCompletor(groovyshMock, mockReflComp, [mockIdCompletor], null)
            def candidates = []
            String buffer = "Math.ma"
            assertEquals(5, completor.complete(buffer, buffer.length(), candidates))
            assertEquals(["max("], candidates)
        }
    }

    void testMemberAfterMethod() {
        reflectionCompletorMocker.demand.complete(1) { tokens, candidates ->
            assert(tokens.collect{it.getText()} == ["Fo", ".", "ba", "(", ")", ".", "xyz"]); candidates << "xyzabc"; 0}
        IdentifierCompletor mockIdCompletor = idCompletorMocker.proxyDelegateInstance()
        ReflectionCompletor mockReflComp = reflectionCompletorMocker.proxyInstance()
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            GroovySyntaxCompletor completor = new GroovySyntaxCompletor(groovyshMock, mockReflComp, [mockIdCompletor], null)
            def candidates = []
            String buffer = "Fo.ba().xyz"
            // xyz cannot be not a var here
            assertEquals(0, completor.complete(buffer, buffer.length(), candidates))
            assertEquals(["xyzabc"], candidates)
        }
    }

    void testIdentfierAfterDotAfterParens() {
        idCompletorMocker.demand.complete(1) { tokens, candidates ->
            assert(tokens.collect{it.getText()} == ["Foo", ".", "bar", "(", "xyz"]); candidates << "xyzabc"; true}
        IdentifierCompletor mockIdCompletor = idCompletorMocker.proxyDelegateInstance()
        ReflectionCompletor mockReflComp = reflectionCompletorMocker.proxyInstance()
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            GroovySyntaxCompletor completor = new GroovySyntaxCompletor(groovyshMock, mockReflComp, [mockIdCompletor], null)
            def candidates = []
            String buffer = "Foo.bar(xyz"
            assertEquals(8, completor.complete(buffer, buffer.length(), candidates))
            assertEquals(["xyzabc"], candidates)
        }
    }

    void testIndentifierAfterParensBeforeDot() {
        idCompletorMocker.demand.complete(1) { tokens, candidates ->
            assert(tokens.collect{it.getText()} == ["Foo", ".", "bar", "(", "xyz"]); candidates << "xyzabc"; true}
        IdentifierCompletor mockIdCompletor = idCompletorMocker.proxyDelegateInstance()
        ReflectionCompletor mockReflComp = reflectionCompletorMocker.proxyInstance()
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            GroovySyntaxCompletor completor = new GroovySyntaxCompletor(groovyshMock, mockReflComp, [mockIdCompletor], null)
            def candidates = []
            // cursor is BEFORE dot
            assertEquals(8, completor.complete("Foo.bar(xyz.", "Foo.bar(xyz".length(), candidates))
            assertEquals(["xyzabc"], candidates)
        }
    }

    void testDoubleIdentifier() {
        IdentifierCompletor mockIdCompletor = idCompletorMocker.proxyDelegateInstance()
        ReflectionCompletor mockReflComp = reflectionCompletorMocker.proxyInstance()
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            GroovySyntaxCompletor completor = new GroovySyntaxCompletor(groovyshMock, mockReflComp, [mockIdCompletor], null)
            def candidates = []
            String buffer = "String jav"
            assertEquals(-1, completor.complete(buffer, buffer.length(), candidates))
            assertEquals([], candidates)
        }
    }

    void testAfterSemi() {
        // evaluation of all is dangerous, but the reflectionCompletor has to deal with this
        reflectionCompletorMocker.demand.complete(1) { tokens, candidates ->
            assert(tokens.collect{it.getText()} == ["deletehardDisk", "(", ")", ";", "foo", ".", "subs"]); candidates << "substring("; 22}

        IdentifierCompletor mockIdCompletor = idCompletorMocker.proxyDelegateInstance()
        ReflectionCompletor mockReflComp = reflectionCompletorMocker.proxyInstance()
        // mock doing the right thing
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            GroovySyntaxCompletor completor = new GroovySyntaxCompletor(groovyshMock, mockReflComp, [mockIdCompletor], null)
            def candidates = []
            String buffer = "deletehardDisk(); foo.subs"
            assertEquals(22, completor.complete(buffer, buffer.length(), candidates))
            assertEquals(["substring("], candidates)
        }
    }

    void testAfterOperator() {
        // evaluation of all is dangerous, but the reflectionCompletor has to deal with this
        reflectionCompletorMocker.demand.complete(1) { tokens, candidates ->
            assert(tokens.collect{it.getText()} == ["a", "=", "foo", ".", "subs"]); candidates << "substring("; 9}

        IdentifierCompletor mockIdCompletor = idCompletorMocker.proxyDelegateInstance()
        ReflectionCompletor mockReflComp = reflectionCompletorMocker.proxyInstance()
        // mock doing the right thing
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            GroovySyntaxCompletor completor = new GroovySyntaxCompletor(groovyshMock, mockReflComp, [mockIdCompletor], null)
            def candidates = []
            String buffer = "a = foo.subs"
            assertEquals(9, completor.complete(buffer, buffer.length(), candidates))
            assertEquals(["substring("], candidates)
        }
    }

    void testDontEvaluateAfterCommand() {
        CommandRegistry registry = new CommandRegistry()
        IdentifierCompletor mockIdCompletor = idCompletorMocker.proxyDelegateInstance()
        ReflectionCompletor mockReflComp = reflectionCompletorMocker.proxyInstance()
        // mock asserting nothing gets evaluated
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            // import command prevents reflection completion
            registry << new ImportCommand(groovyshMock)
            GroovySyntaxCompletor completor = new GroovySyntaxCompletor(groovyshMock, mockReflComp, [mockIdCompletor], null)
            def candidates = []
            String buffer = "import foo"
            assertEquals(-1, completor.complete(buffer, buffer.length(), candidates))
            assertEquals([], candidates)
        }
    }

    void testAfterGString() {
        IdentifierCompletor mockIdCompletor = idCompletorMocker.proxyDelegateInstance()
        ReflectionCompletor mockReflComp = reflectionCompletorMocker.proxyInstance()
        // mock asserting GString is not evaluated
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            GroovySyntaxCompletor completor = new GroovySyntaxCompletor(groovyshMock, mockReflComp, [mockIdCompletor], null)
            def candidates = []
            String buffer = "\"\${foo.delete()}\".subs"
            assertEquals(-1, completor.complete(buffer, buffer.length(), candidates))
            assertEquals([], candidates)
        }
    }

    void testInStringFilename() {
        IdentifierCompletor mockIdCompletor = idCompletorMocker.proxyDelegateInstance()
        ReflectionCompletor mockReflComp = reflectionCompletorMocker.proxyInstance()
        MockFor filenameCompletorMocker = new MockFor(FileNameCompleter)
        String linestart = "foo('" // ends with single hyphen
        String pathstart = "/usr/foobar"
        String buffer = linestart + pathstart
        filenameCompletorMocker.demand.complete(1) {bufferline, cursor, candidates ->
            assert(bufferline == pathstart);
            assert(cursor == pathstart.length());
            candidates << "foobar"; 5}
        groovyshMocker.use { filenameCompletorMocker.use {
            FileNameCompleter mockFileComp = new FileNameCompleter()
            Groovysh groovyshMock = new Groovysh()
            GroovySyntaxCompletor completor = new GroovySyntaxCompletor(groovyshMock, mockReflComp, [mockIdCompletor], mockFileComp)
            def candidates = []
            assertEquals("foo('/usr/".length(), completor.complete(buffer, buffer.length(), candidates))
            assertEquals(["foobar"], candidates)
        }}
    }

    void testInStringFilenameBlanks() {
        // test with blanks (non-tokens) before the first hyphen
        IdentifierCompletor mockIdCompletor = idCompletorMocker.proxyDelegateInstance()
        ReflectionCompletor mockReflComp = reflectionCompletorMocker.proxyInstance()
        MockFor filenameCompletorMocker = new MockFor(FileNameCompleter)
        String linestart = "x = '" // ends with single hyphen
        String pathstart = "/usr/foobar"
        String buffer = linestart + pathstart
        filenameCompletorMocker.demand.complete(1) {bufferline, cursor, candidates ->
            assert(bufferline == pathstart);
            assert(cursor == pathstart.length());
            candidates << "foobar"; 5}
        groovyshMocker.use { filenameCompletorMocker.use {
            FileNameCompleter mockFileComp = new FileNameCompleter()
            Groovysh groovyshMock = new Groovysh()
            GroovySyntaxCompletor completor = new GroovySyntaxCompletor(groovyshMock, mockReflComp, [mockIdCompletor], mockFileComp)
            def candidates = []
            assertEquals("x = '/usr/".length(), completor.complete(buffer, buffer.length(), candidates))
            assertEquals(["foobar"], candidates)
        }}
    }

    void testInGString() {
        idCompletorMocker.demand.complete(1) { tokens, candidates ->
            assert(tokens.collect{it.getText()} == ["", "{", "foo"]); candidates << "foobar"; true}
        IdentifierCompletor mockIdCompletor = idCompletorMocker.proxyDelegateInstance()
        ReflectionCompletor mockReflComp = reflectionCompletorMocker.proxyInstance()
        // mock asserting GString is not evaluated
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            GroovySyntaxCompletor completor = new GroovySyntaxCompletor(groovyshMock, mockReflComp, [mockIdCompletor], null)
            def candidates = []
            String buffer = "\"\${foo"
            assertEquals(3, completor.complete(buffer, buffer.length(), candidates))
            assertEquals(["foobar"], candidates)
        }
    }

    void testMultilineComplete() {
        reflectionCompletorMocker.demand.complete(1) { tokens, candidates ->
            assert(tokens.collect{it.getText()} == ["xyz\nabc", ".", "subs"]); candidates << "substring("; 7}
        bufferManager.buffers.add(["\"\"\"xyz"])
        bufferManager.setSelected(1)
        IdentifierCompletor mockIdCompletor = idCompletorMocker.proxyDelegateInstance()
        ReflectionCompletor mockReflComp = reflectionCompletorMocker.proxyInstance()
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            GroovySyntaxCompletor completor = new GroovySyntaxCompletor(groovyshMock, mockReflComp, [mockIdCompletor], null)
            def candidates = []
            String buffer = "abc\"\"\".subs"
            assertEquals(7, completor.complete(buffer, buffer.length(), candidates))
            assertEquals(["substring("], candidates)
        }
    }
}

