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

package org.codehaus.groovy.tools.shell

import org.codehaus.groovy.GroovyException
import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.codehaus.groovy.tools.shell.completion.ReflectionCompletor
import org.codehaus.groovy.tools.shell.completion.TokenUtilTest

class GroovyshTest
extends GroovyTestCase {

    IO testio
    ByteArrayOutputStream mockOut
    ByteArrayOutputStream mockErr

    void setUp() {
        super.setUp()

        mockOut = new ByteArrayOutputStream();

        mockErr = new ByteArrayOutputStream();

        testio = new IO(
                new ByteArrayInputStream(),
                mockOut,
                mockErr)
    }

    void testCompleteExpr() {
        Groovysh groovysh = new Groovysh(testio)
        groovysh.execute("x = 3")
        assertEquals(" 3\n", mockOut.toString()[-3..-1])
    }

    void testIncompleteExpr() {
        Groovysh groovysh = new Groovysh(testio)
        groovysh.execute("def x() {")
        assertEquals("", mockOut.toString())
    }

    void testBadExpr() {
        Groovysh groovysh = new Groovysh(testio)
        try {
            groovysh.execute("x}")
            fail()
        } catch (MultipleCompilationErrorsException e) {
            assertEquals("", mockOut.toString())
        }
    }

    void testDisplayBuffer() {
        Groovysh groovysh = new Groovysh(testio)
        groovysh.displayBuffer(["foo", "bar"])
        assertEquals(34, mockOut.toString().length())
        assertTrue(mockOut.toString().contains("foo\n"))
        assertTrue(mockOut.toString().contains("bar\n"))
    }

    void testDefaultErrorHook() {
        Groovysh groovysh = new Groovysh(testio)
        groovysh.defaultErrorHook(new Throwable() {
            StackTraceElement[] stackTrace = [
                    new StackTraceElement("fooClass", "fooMethod", "fooFile", 42),
                    new StackTraceElement(Interpreter.SCRIPT_FILENAME, "run", "scriptFile", 42)]
        })
        assertEquals("", mockOut.toString())
        assertTrue(mockErr.toString(), mockErr.toString().contains("foo"))
        assertFalse(mockErr.toString(), mockErr.toString().contains(Interpreter.SCRIPT_FILENAME))
        assertFalse(mockErr.toString(), mockErr.toString().contains("..."))
    }

    void testDefaultResultHookString() {
        Groovysh groovysh = new Groovysh(testio)
        groovysh.defaultResultHook("foo bar".split())
        assertTrue(mockOut.toString(), mockOut.toString().trim().endsWith("[foo, bar] (class java.lang.String)"))
        assertEquals("", mockErr.toString())
    }

    void testDefaultResultHookObject() {
        Groovysh groovysh = new Groovysh(testio)
        groovysh.defaultResultHook(Object.fields)
        assertTrue(mockOut.toString(), mockOut.toString().trim().endsWith("[] (class java.lang.reflect.Field)"))
        assertEquals("", mockErr.toString())
    }

    void testDefaultResultPrimitive() {
        Groovysh groovysh = new Groovysh(testio)
        groovysh.defaultResultHook(3)
        assertTrue(mockOut.toString(), mockOut.toString().trim().endsWith("3"))
        assertEquals("", mockErr.toString())
    }

    void testDefaultResultNull() {
        Groovysh groovysh = new Groovysh(testio)
        groovysh.defaultResultHook(null)
        assertTrue(mockOut.toString(), mockOut.toString().trim().endsWith("null"))
        assertEquals("", mockErr.toString())
    }

    void testDefaultResultList() {
        Groovysh groovysh = new Groovysh(testio)
        groovysh.defaultResultHook([])
        assertTrue(mockOut.toString(), mockOut.toString().trim().endsWith("[]"))
        assertEquals("", mockErr.toString())
    }

    void testFindCommandDuplicate() {
        Groovysh groovysh = new Groovysh(testio)
        CommandSupport command = new CommandSupport(groovysh, "import", "imp") {
            @Override
            Object execute(List args) {
                return null
            }
        }
        try {
            groovysh.register(command)
            fail()
        } catch (AssertionError e) {
            // pass
        }
    }

    void testFindCommandFoo() {
        Groovysh groovysh = new Groovysh(testio)
        assertEquals(null, groovysh.findCommand(" foo import "))
        assertFalse(groovysh.isExecutable(" foo import "))
        CommandSupport command2 = new CommandSupport(groovysh, "foo", "/foo") {
            @Override
            Object execute(List args) {
                return null
            }
        }
        groovysh.register(command2)
        assertEquals(command2, groovysh.findCommand(" foo bar "))
        assertTrue(groovysh.isExecutable(" foo import "))
        assertEquals(command2, groovysh.findCommand(" /foo bar "))
        assertEquals(null, groovysh.findCommand(" bar foo "))
    }

    void testExecuteCommandFoo() {
        Groovysh groovysh = new Groovysh(testio)
        assertEquals(null, groovysh.findCommand(" foo import "))
        assertFalse(groovysh.isExecutable(" foo import "))
        CommandSupport command2 = new CommandSupport(groovysh, "foo", "/foo") {
            @Override
            Object execute(List args) {
                throw new CommandException(this, "Test Command failure")
            }
        }
        groovysh.register(command2)
        // also assert CommandException caught
        assertNull(groovysh.execute(" foo import "))
        assertTrue(mockErr.toString(), mockErr.toString().contains("Test Command failure"))
        assertEquals(1, mockErr.toString().count("\n"))
        assertEquals("", mockOut.toString())
    }

    void testGetIndentLevel() {
        Groovysh groovysh = new Groovysh(testio)
        assertEquals("", groovysh.getIndentPrefix())
        groovysh.buffers.buffers.add(["Foo {"])
        groovysh.buffers.select(1)
        assertEquals(" " * groovysh.indentSize, groovysh.getIndentPrefix())
        groovysh.buffers.buffers.add(["Foo {{"])
        groovysh.buffers.select(2)
        assertEquals(" " * groovysh.indentSize * 2, groovysh.getIndentPrefix())

    }
}


class GroovyshCompletorTest extends GroovyTestCase {

    void testLiveClass() {
        /* This test setup looks weird, but it is the only I found that can reproduce this behavior:
groovy:000> class Foo extends HashSet implements Comparable {int compareTo(Object) {0}}
===> true
groovy:000> Foo.
__$stMC              __$swapInit()            __timeStamp
super$1$getClass()   super$1$notify()         super$1$notifyAll()
*/
        ByteArrayOutputStream mockOut = new ByteArrayOutputStream();
        ByteArrayOutputStream mockErr = new ByteArrayOutputStream();
        IO testio = new IO(
                new ByteArrayInputStream(),
                mockOut,
                mockErr)
        Groovysh groovysh = new Groovysh(new URLClassLoader(), new Binding(), testio)
        groovysh.run("import " + ReflectionCompletor.name)
        groovysh.run("""class Foo extends HashSet implements Comparable {
int compareTo(Object) {0}; int priv; static int priv2; public int foo; public static int bar; int foom(){1}; static int barm(){2}}""")
        groovysh.run(ReflectionCompletor.name + ".getPublicFieldsAndMethods(Foo, \"\")")
        //assert mockErr.toString() == ""
        String rawout = mockOut.toString()
        assert rawout
        String rest = rawout.split('\\[')[-1]
        assert rest
        List<String> findResult = rest.split()[0..-2].collect({ it -> it.trim()[0..-2] })
        assertEquals([], findResult.findAll({ it.startsWith("_") }))
        assertEquals([], findResult.findAll({ it.startsWith("super\$") }))
        assertEquals([], findResult.findAll({ it.startsWith("this\$") }))
        assertTrue(findResult.toString(), 'bar' in findResult)
        assertFalse(findResult.toString(), 'foo' in findResult)
        assertFalse(findResult.toString(), 'priv' in findResult)
        assertFalse(findResult.toString(), 'priv2' in findResult)
        assertTrue(findResult.toString(), 'barm()' in findResult)
        assertFalse(findResult.toString(), 'foom()' in findResult)
    }

    void testLiveInstance() {
        /* This test setup looks weird, but it is the only I found that can reproduce this behavior:
groovy:000> class Foo extends HashSet implements Comparable {int compareTo(Object) {0}}
===> true
groovy:000> Foo.
__$stMC              __$swapInit()            __timeStamp
super$1$getClass()   super$1$notify()         super$1$notifyAll()
*/
        ByteArrayOutputStream mockOut = new ByteArrayOutputStream();
        ByteArrayOutputStream mockErr = new ByteArrayOutputStream();
        IO testio = new IO(
                new ByteArrayInputStream(),
                mockOut,
                mockErr)
        Groovysh groovysh = new Groovysh(new URLClassLoader(), new Binding(), testio)
        groovysh.run("import " + ReflectionCompletor.name)
        groovysh.run("""class Foo extends HashSet implements Comparable {
int compareTo(Object) {0}; int priv; static int priv2; public int foo; public static int bar; int foom(){1}; static int barm(){2}}""")
        groovysh.run(ReflectionCompletor.name + ".getPublicFieldsAndMethods(new Foo(), \"\")")
        //assert mockErr.toString() == ""
        String rawout = mockOut.toString()
        assert rawout
        String rest = rawout.split('\\[')[-1]
        List<String> findResult = rest.split()[0..-2].collect({ it -> it.trim()[0..-2] })
        assertEquals([], findResult.findAll({ it.startsWith("_") }))
        assertEquals([], findResult.findAll({ it.startsWith("super\$") }))
        assertEquals([], findResult.findAll({ it.startsWith("this\$") }))
        assertTrue(findResult.toString(), 'foo' in findResult)
        assertTrue(findResult.toString(), 'bar' in findResult)
        assertFalse(findResult.toString(), 'priv' in findResult)
        assertFalse(findResult.toString(), 'priv2' in findResult)
        assertTrue(findResult.toString(), 'foom()' in findResult)
        assertTrue(findResult.toString(), 'barm()' in findResult)
    }

    void testImportedClassStaticMember() {
        // tests that import are taken into account when evaluating for completion
        ByteArrayOutputStream mockOut = new ByteArrayOutputStream();
        ByteArrayOutputStream mockErr = new ByteArrayOutputStream();
        IO testio = new IO(
                new ByteArrayInputStream(),
                mockOut,
                mockErr)
        Groovysh groovysh = new Groovysh(new URLClassLoader(), new Binding(), testio)
        groovysh.run("import " + GroovyException.name)
        ReflectionCompletor compl = new ReflectionCompletor(groovysh)
        def candidates = []
        compl.complete(TokenUtilTest.tokenList("GroovyException."), candidates)
        assertTrue(candidates.size() > 0)
    }
}