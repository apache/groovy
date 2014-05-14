/*
 * Copyright 2003-2013 the original author or authors.
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

class GroovyshTest extends GroovyTestCase {

    IO testio
    ByteArrayOutputStream mockOut
    ByteArrayOutputStream mockErr

    void setUp() {
        super.setUp()
        mockOut = new ByteArrayOutputStream();
        mockErr = new ByteArrayOutputStream();
        testio = new IO(new ByteArrayInputStream(), mockOut, mockErr)
        testio.setVerbosity(IO.Verbosity.INFO)
    }

    void testCompleteExpr() {
        Groovysh groovysh = new Groovysh(testio)
        groovysh.execute("x = 3")
        assert mockOut.toString().length() > 0
        assert " 3\n" == mockOut.toString().normalize()[-3..-1]
    }

    void testIncompleteExpr() {
        Groovysh groovysh = new Groovysh(testio)
        groovysh.execute("def x() {")
        assert "" == mockOut.toString()
    }

    void testBadExpr() {
        Groovysh groovysh = new Groovysh(testio)
        try {
            groovysh.execute("x}")
            fail()
        } catch (MultipleCompilationErrorsException e) {
            assert "" == mockOut.toString()
        }
    }

    void testMissingPropertyExpr() {
        Groovysh groovysh = new Groovysh(testio)
        // this is a special case, e.g. happens for Gradle DefaultExtraPropertiesExtension
        // assert no fail
        groovysh.execute("x = new Object() {public Object getProperty(String name) {throw new MissingPropertyException('From test', name, null)}}")
    }

    void testDisplayBuffer() {
        Groovysh groovysh = new Groovysh(testio)
        groovysh.displayBuffer(["foo", "bar"])
        def out = mockOut.toString().normalize()
        // was 20 on my windows box after normalize()
        // is it relying on other preference settings?
//        assertEquals(34, out.length())
        assert out.contains("foo\n")
        assert out.contains("bar\n")
    }

    void testDefaultErrorHook() {
        Groovysh groovysh = new Groovysh(testio)
        groovysh.defaultErrorHook(new Throwable() {
            StackTraceElement[] stackTrace = [
                    new StackTraceElement("fooClass", "fooMethod", "fooFile", 42),
                    new StackTraceElement(Interpreter.SCRIPT_FILENAME, "run", "scriptFile", 42)]
        })
        assert "" == mockOut.toString()
        assert mockErr.toString().contains("foo")
        assert ! mockErr.toString().contains(Interpreter.SCRIPT_FILENAME)
        assert ! mockErr.toString().contains("...")
    }

    void testDefaultResultHookStringArray() {
        Groovysh groovysh = new Groovysh(testio)
        groovysh.defaultResultHook("foo bar".split())
        assert mockOut.toString().trim().endsWith("[foo, bar]")
        assert "" == mockErr.toString()
    }

    void testDefaultResultHookObjectArray() {
        Groovysh groovysh = new Groovysh(testio)
        groovysh.defaultResultHook(Object.fields)
        assert mockOut.toString().trim().endsWith("[]")
        assert "" == mockErr.toString()
    }

    void testDefaultResultPrimitive() {
        Groovysh groovysh = new Groovysh(testio)
        groovysh.defaultResultHook(3)
        assert mockOut.toString().trim().endsWith("3")
        assert "" == mockErr.toString()
    }

    void testDefaultResultNull() {
        Groovysh groovysh = new Groovysh(testio)
        groovysh.defaultResultHook(null)
        assert mockOut.toString().trim().endsWith("null")
        assert "" == mockErr.toString()
    }

    void testDefaultResultList() {
        Groovysh groovysh = new Groovysh(testio)
        groovysh.defaultResultHook([])
        assert mockOut.toString().trim().endsWith("[]")
        assert "" == mockErr.toString()
    }

    void testDefaultResultSet() {
        Groovysh groovysh = new Groovysh(testio)
        groovysh.defaultResultHook([42] as Set)
        assert mockOut.toString().trim().endsWith("[42]")
        assert "" == mockErr.toString()
    }

    void testDefaultResultArray() {
        Groovysh groovysh = new Groovysh(testio)
        groovysh.defaultResultHook([42] as int[])
        assert mockOut.toString().trim().endsWith("[42]")
        assert "" == mockErr.toString()
    }

    void testDefaultResultMapEmpty() {
        Groovysh groovysh = new Groovysh(testio)
        groovysh.defaultResultHook([:])
        assert mockOut.toString().trim().endsWith("[:]")
        assert "" == mockErr.toString()
    }

    void testDefaultResultMap() {
        Groovysh groovysh = new Groovysh(testio)
        groovysh.defaultResultHook(['class': 'foo'])
        assert mockOut.toString().trim().endsWith("[class:foo]")
        assert "" == mockErr.toString()
    }

    void testDefaultResultConfigObject() {
        // ConfigObject are like maps
        Groovysh groovysh = new Groovysh(testio)
        ConfigObject co = new ConfigObject()
        co.put("class", "foo")
        groovysh.defaultResultHook(co)
        assert mockOut.toString().trim().endsWith("[class:foo]")
        assert "" == mockErr.toString()
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
        assertNull(groovysh.findCommand(" foo import "))
        assert !groovysh.isExecutable(" foo import ")
        CommandSupport command2 = new CommandSupport(groovysh, "foo", "/foo") {
            @Override
            Object execute(List args) {
                return null
            }
        }
        groovysh.register(command2)
        assert command2 == groovysh.findCommand(" foo bar ")
        assert groovysh.isExecutable(" foo import ")
        assert command2 == groovysh.findCommand(" /foo bar ")
        assertNull(groovysh.findCommand(" bar foo "))
    }

    void testExecuteCommandFoo() {
        Groovysh groovysh = new Groovysh(testio)
        assertNull(groovysh.findCommand(" foo import "))
        assert ! groovysh.isExecutable(" foo import ")
        CommandSupport command2 = new CommandSupport(groovysh, "foo", "/foo") {
            @Override
            Object execute(List args) {
                throw new CommandException(this, "Test Command failure")
            }
        }
        groovysh.register(command2)
        // also assert CommandException caught
        assertNull(groovysh.execute(" foo import "))
        assert mockErr.toString().contains("Test Command failure")
        assert 1 == mockErr.toString().count("\n")
        assert "" == mockOut.toString()
    }

    void testGetIndentLevel() {
        Groovysh groovysh = new Groovysh(testio)
        assert "" == groovysh.getIndentPrefix()
        groovysh.buffers.buffers.add(["Foo {"])
        groovysh.buffers.select(1)
        assert " " * groovysh.indentSize == groovysh.getIndentPrefix()
        groovysh.buffers.buffers.add(["Foo {{"])
        groovysh.buffers.select(2)
        assert " " * groovysh.indentSize * 2 == groovysh.getIndentPrefix()
    }

    void testLoadUserScript() {
        final File file = createTemporaryGroovyScriptFile('1 / 0')
        Groovysh groovysh = new Groovysh(testio) {
            @Override
            File getUserStateDirectory() {
                return file.getParentFile()
            }
        }
        try {
            groovysh.loadUserScript(file.getName())
            fail('Expected ArithmeticException')
        } catch (ArithmeticException e) {}
    }

    File createTemporaryGroovyScriptFile(content) {
        String testName = "GroovyshTest" + System.currentTimeMillis()
        File groovyCode = new File(System.getProperty("java.io.tmpdir"), testName)
        groovyCode.write(content)
        groovyCode.deleteOnExit()
        return groovyCode
    }
}


class GroovyshCompletorTest extends GroovyTestCase {

    void testIOMock() {
        IO testio
        ByteArrayOutputStream mockOut
        ByteArrayOutputStream mockErr
        mockOut = new ByteArrayOutputStream();
        mockErr = new ByteArrayOutputStream();
        testio = new IO(
                new ByteArrayInputStream(),
                mockOut,
                mockErr)
        testio.out.println("mockResult")
        assertTrue("stdout=" + mockOut.toString() + "\nstderr=" + mockErr.toString(), mockOut.toString().contains('mockResult'))
        testio.err.println("mockErrResult")
        assertTrue("stdout=" + mockOut.toString() + "\nstderr=" + mockErr.toString(), mockErr.toString().contains('mockErrResult'))
    }

    void testLiveClass() {
        /* This test setup looks weird, but it is the only I found that can reproduce this behavior:
-groovy:000> class Foo extends HashSet implements Comparable {int compareTo(Object) {0}}
-===> true
-groovy:000> Foo.
-__$stMC              __$swapInit()            __timeStamp
-super$1$getClass()   super$1$notify()         super$1$notifyAll()
-*/
        IO testio = new IO()
        Groovysh groovysh = new Groovysh(testio)
        Object result = groovysh.interp.evaluate(["import " + ReflectionCompletor.getCanonicalName(), """class Foo extends HashSet implements Comparable {
int compareTo(Object) {0}; int priv; static int priv2; public int foo; public static int bar; int foom(){1}; static int barm(){2}}""", "ReflectionCompletor.getPublicFieldsAndMethods(Foo, \"\")"])
        assert result
        assert result.size() > 0
        assert [] == result.findAll({it.startsWith("_")})
        assert [] == result.findAll({it.startsWith("super\$")})
        assert [] == result.findAll({it.startsWith("this\$")})
        assert ! ('foo' in result)
        assert ! ('priv' in result)
        assert ! ('priv2' in result)
        assert 'barm()' in result
        assert ! ('foom()' in result)

    }

    void testLiveInstance() {
        /* This test setup looks weird, but it is the only I found that can reproduce this behavior:
-groovy:000> class Foo extends HashSet implements Comparable {int compareTo(Object) {0}}
-===> true
-groovy:000> Foo.
-__$stMC              __$swapInit()            __timeStamp
-super$1$getClass()   super$1$notify()         super$1$notifyAll()
-*/
        IO testio = new IO()
        Groovysh groovysh = new Groovysh(testio)
        Object result = groovysh.interp.evaluate(["import " + ReflectionCompletor.getCanonicalName(), """class Foo extends HashSet implements Comparable {
int compareTo(Object) {0}; int priv; static int priv2; public int foo; public static int bar; int foom(){1}; static int barm(){2}}""",
                "ReflectionCompletor.getPublicFieldsAndMethods(new Foo(), \"\")"])
        assertNotNull(result)
        assert result.size() > 0
        assert [] == result.findAll({it.startsWith("_")})
        assert [] == result.findAll({it.startsWith("super\$")})
        assert [] == result.findAll({it.startsWith("this\$")})
        assert 'bar' in result
        assert ! ('priv' in result)
        assert ! ('priv2' in result)
        assert 'foom()' in result
        assert 'barm()' in result
    }

    void testImportedClassStaticMember() {
        // tests that import are taken into account when evaluating for completion
        IO testio = new IO()
        Groovysh groovysh = new Groovysh(new URLClassLoader(), new Binding(), testio)
        groovysh.run("import " + GroovyException.name)
        ReflectionCompletor compl = new ReflectionCompletor(groovysh, 0)
        def candidates = []
        compl.complete(TokenUtilTest.tokenList("GroovyException."), candidates)
        assert candidates.size() > 0
    }
}
