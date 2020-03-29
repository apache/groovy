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
package org.apache.groovy.groovysh

import groovy.test.GroovyTestCase
import org.apache.groovy.groovysh.completion.antlr4.ReflectionCompleter
import org.apache.groovy.groovysh.completion.ReflectionCompletionCandidate
import org.apache.groovy.groovysh.completion.TokenUtilTest
import org.codehaus.groovy.GroovyException
import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.codehaus.groovy.tools.shell.IO
import org.fusesource.jansi.AnsiOutputStream

class GroovyshTest extends GroovyTestCase {

    protected IO testio
    protected ByteArrayOutputStream mockOut
    protected ByteArrayOutputStream mockErr

    @Override
    void setUp() {
        super.setUp()
        mockOut = new ByteArrayOutputStream()
        mockErr = new ByteArrayOutputStream()
        testio = new IO(new ByteArrayInputStream(), mockOut, mockErr)
        testio.setVerbosity(IO.Verbosity.INFO)
    }

    void testCompleteExpr() {
        Groovysh groovysh = new Groovysh(testio)
        groovysh.execute('x = 3')
        assert mockOut.toString().length() > 0
        assert ' 3\n' == mockOut.toString().normalize()[-3..-1]
    }

    protected Groovysh createGroovysh() {
        return new Groovysh(testio) {
            @Override
            protected String getPreference(String key, String theDefault) {
                if (key == INTERPRETER_MODE_PREFERENCE_KEY) {
                    return 'false'
                }
                return super.getPreference(key, theDefault)
            }
        }
    }

    void testClassDef() {
        Groovysh groovysh = createGroovysh()
        groovysh.execute('class MyFooTestClass{ String foo }')
        assert mockOut.toString().length() > 0
        assert ' true\n' == mockOut.toString().normalize()[-6..-1]
        groovysh.execute('m = new MyFooTestClass()')
        assert mockOut.toString().length() > 0
        // mostly assert no exception
        assert mockOut.toString().normalize().contains('MyFooTestClass@')
    }


    void testmethodDef() {
        Groovysh groovysh = createGroovysh()
        groovysh.execute('int foo() {42}')
        assert mockOut.toString().length() > 0
        assert ' true\n' == mockOut.toString().normalize()[-6..-1]
    }



    void testIncompleteExpr() {
        Groovysh groovysh = createGroovysh()
        groovysh.execute('def x() {')
        assert '' == mockOut.toString()
    }

    void testBadExpr() {
        Groovysh groovysh = createGroovysh()
        try {
            groovysh.execute('x}')
            fail('expected MultipleCompilationErrorsException ')
        } catch (MultipleCompilationErrorsException e) {
            assert '' == mockOut.toString()
        }

        try {
            groovysh.execute('x)')
            fail('expected MultipleCompilationErrorsException ')
        } catch (MultipleCompilationErrorsException e) {
            assert '' == mockOut.toString()
        }

        try {
            groovysh.execute('x]')
            fail('expected MultipleCompilationErrorsException ')
        } catch (MultipleCompilationErrorsException e) {
            assert '' == mockOut.toString()
        }
    }

    void testIncompleteBracketMultilineExpr() {
        Groovysh groovysh = createGroovysh()
        groovysh.execute('a = [')
        groovysh.execute('1,')
        groovysh.execute('2,')
        groovysh.execute('3')
        groovysh.execute(']')
        groovysh.execute('a.size() == 3')
        assert mockOut.toString().contains('true')
    }

    void testIncompleteParenMultilineExpr() {
        Groovysh groovysh = createGroovysh()
        groovysh.execute('mc = { num1, num2 -> num1 + num2 }')
        groovysh.execute('mc(3')
        groovysh.execute(',')
        groovysh.execute('7')
        groovysh.execute(') == 10')
        assert mockOut.toString().contains('true')
    }

    void testIncompleteBraceMultilineExpr() {
        Groovysh groovysh = createGroovysh()
        groovysh.execute('mc = {')
        groovysh.execute('3')
        groovysh.execute('}')
        groovysh.execute('mc() == 3')
        assert mockOut.toString().contains('true')
    }

    void testMissingPropertyExpr() {
        Groovysh groovysh = createGroovysh()
        // this is a special case, e.g. happens for Gradle DefaultExtraPropertiesExtension
        // assert no fail
        groovysh.execute(/x = new Object() {public Object getProperty(String name) {throw new MissingPropertyException('From test', name, null)}}/)
    }

    void testDisplayBuffer() {
        Groovysh groovysh = createGroovysh()
        groovysh.displayBuffer(['foo', 'bar'])
        def out = mockOut.toString().normalize()
        // was 20 on my windows box after normalize()
        // is it relying on other preference settings?
//        assertEquals(34, out.length())
        assert out.contains('foo\n')
        assert out.contains('bar\n')
    }

    void testDefaultErrorHook() {
        Groovysh groovysh = createGroovysh()
        groovysh.defaultErrorHook(new Throwable() {
            StackTraceElement[] stackTrace = [
                    new StackTraceElement('fooClass', 'fooMethod', 'fooFile', 42),
                    new StackTraceElement(Interpreter.SCRIPT_FILENAME, 'run', 'scriptFile', 42)]
        })
        assert '' == mockOut.toString()
        assert mockErr.toString().contains('org.apache.groovy.groovysh.GroovyshTest$')
        assert mockErr.toString().contains('fooClass')
        assert mockErr.toString().contains('foo')
        assert ! mockErr.toString().contains(Interpreter.SCRIPT_FILENAME)
        assert ! mockErr.toString().contains('...')
    }

    void testDefaultResultHookStringArray() {
        Groovysh groovysh = createGroovysh()
        groovysh.defaultResultHook('foo bar'.split())
        assert mockOut.toString().trim().endsWith('[foo, bar]')
        assert '' == mockErr.toString()
    }

    void testDefaultResultHookObjectArray() {
        Groovysh groovysh = createGroovysh()
        groovysh.defaultResultHook(Object.fields)
        assert mockOut.toString().trim().endsWith('[]')
        assert '' == mockErr.toString()
    }

    void testDefaultResultPrimitive() {
        Groovysh groovysh = createGroovysh()
        groovysh.defaultResultHook(3)
        assert mockOut.toString().trim().endsWith('3')
        assert '' == mockErr.toString()
    }

    void testDefaultResultNull() {
        Groovysh groovysh = createGroovysh()
        groovysh.defaultResultHook(null)
        assert mockOut.toString().trim().endsWith('null')
        assert '' == mockErr.toString()
    }

    void testDefaultResultList() {
        Groovysh groovysh = createGroovysh()
        groovysh.defaultResultHook([])
        assert mockOut.toString().trim().endsWith('[]')
        assert '' == mockErr.toString()
    }

    void testDefaultResultSet() {
        Groovysh groovysh = createGroovysh()
        groovysh.defaultResultHook([42] as Set)
        assert mockOut.toString().trim().endsWith('[42]')
        assert '' == mockErr.toString()
    }

    void testDefaultResultArray() {
        Groovysh groovysh = createGroovysh()
        groovysh.defaultResultHook([42] as int[])
        assert mockOut.toString().trim().endsWith('[42]')
        assert '' == mockErr.toString()
    }

    void testDefaultResultMapEmpty() {
        Groovysh groovysh = createGroovysh()
        groovysh.defaultResultHook([:])
        assert mockOut.toString().trim().endsWith('[:]')
        assert '' == mockErr.toString()
    }

    void testDefaultResultMap() {
        Groovysh groovysh = createGroovysh()
        groovysh.defaultResultHook(['class': 'foo'])
        assert mockOut.toString().trim().endsWith('[class:foo]')
        assert '' == mockErr.toString()
    }

    void testDefaultResultConfigObject() {
        // ConfigObject are like maps
        Groovysh groovysh = createGroovysh()
        ConfigObject co = new ConfigObject()
        co.put('class', 'foo')
        groovysh.defaultResultHook(co)
        assert mockOut.toString().trim().endsWith('[class:foo]')
        assert '' == mockErr.toString()
    }

    void testFindCommandDuplicate() {
        Groovysh groovysh = createGroovysh()
        CommandSupport command = new CommandSupport(groovysh, 'import', 'imp') {
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
        Groovysh groovysh = createGroovysh()
        assertNull(groovysh.findCommand(' foo import '))
        assert !groovysh.isExecutable(' foo import ')
        CommandSupport command2 = new CommandSupport(groovysh, 'foo', '/foo') {
            @Override
            Object execute(List args) {
                return null
            }
        }
        groovysh.register(command2)
        assert command2 == groovysh.findCommand(' foo bar ')
        assert groovysh.isExecutable(' foo import ')
        List accumulateArgs = []
        assert command2 == groovysh.findCommand(' foo bar ', accumulateArgs)
        assert accumulateArgs == ['bar']
        accumulateArgs = []
        assert command2 == groovysh.findCommand(' foo bar baz', accumulateArgs)
        assert accumulateArgs == ['bar', 'baz']
        assert groovysh.isExecutable(' foo import ')
        assert command2 == groovysh.findCommand(' /foo bar ')
        assertNull(groovysh.findCommand(' bar foo '))
    }

    void testExecuteCommandFoo() {
        Groovysh groovysh = createGroovysh()
        assertNull(groovysh.findCommand(' foo import '))
        assert ! groovysh.isExecutable(' foo import ')
        CommandSupport command2 = new CommandSupport(groovysh, 'foo', '/foo') {
            @Override
            Object execute(List args) {
                throw new CommandException(this, 'Test Command failure')
            }
        }
        groovysh.register(command2)
        // also assert CommandException caught
        assertNull(groovysh.execute(' foo import '))
        assert mockErr.toString().contains('Test Command failure')
        assert 1 == mockErr.toString().count('\n')
        assert '' == mockOut.toString()
    }

    void testGetIndentLevel() {
        Groovysh groovysh = createGroovysh()
        assert '' == groovysh.indentPrefix
        groovysh.buffers.buffers.add(['Foo {'])
        groovysh.buffers.select(1)
        assert ' ' * groovysh.indentSize == groovysh.indentPrefix
        groovysh.buffers.buffers.add(['Foo {{'])
        groovysh.buffers.select(2)
        assert ' ' * groovysh.indentSize * 2 == groovysh.indentPrefix
    }

    void testLoadUserScript() {
        final File file = createTemporaryGroovyScriptFile('1 / 0')
        Groovysh groovysh = new Groovysh() {
            @Override
            File getUserStateDirectory() {
                return file.parentFile
            }
        }
        try {
            groovysh.loadUserScript(file.name)
            fail('Expected ArithmeticException')
        } catch (ArithmeticException e) {}
    }

    void testImports() {
        Groovysh groovysh = createGroovysh()
        groovysh.execute('import java.rmi.Remote ')
        assert mockOut.toString().length() > 0
        assert 'java.rmi.Remote\n' == mockOut.toString().normalize()[-('java.rmi.Remote\n'.length())..-1]
        groovysh.execute('Remote r')
        assert mockOut.toString().length() > 0
        // mostly assert no exception
        assert 'null\n' == mockOut.toString().normalize()[-5..-1]
    }

    static File createTemporaryGroovyScriptFile(content) {
        String testName = 'GroovyshTest' + System.currentTimeMillis()
        File groovyCode = new File(System.getProperty('java.io.tmpdir'), testName)
        groovyCode.write(content)
        groovyCode.deleteOnExit()
        return groovyCode
    }
}

/**
 * Runs all tests of groovyshTests with interpreter mode enabled
 */
class GroovyshInterpreterModeTest extends GroovyshTest {

    protected Groovysh createGroovysh() {
        return new Groovysh(testio) {
            @Override
            protected String getPreference(String key, String theDefault) {
                if (key == INTERPRETER_MODE_PREFERENCE_KEY) {
                    return 'true'
                }
                return super.getPreference(key, theDefault)
            }
        }
    }

    void testBoundVar() {
        Groovysh groovysh = createGroovysh()

        groovysh.execute('int x = 3')
        assert mockOut.toString().length() > 0
        assert ' 3\n' == mockOut.toString().normalize()[-3..-1]
        groovysh.execute('x')
        assert mockOut.toString().length() > 0
        assert ' 3\n' == mockOut.toString().normalize()[-3..-1]
    }

    void testBoundVarmultiple() {
        Groovysh groovysh = createGroovysh()
        groovysh.execute('int x, y, z')
        assert mockOut.toString().length() > 0
        assert ' 0\n' == mockOut.toString().normalize()[-3..-1]
        groovysh.execute('y')
        assert mockOut.toString().length() > 0
        assert ' 0\n' == mockOut.toString().normalize()[-3..-1]
    }

}


class GroovyshCompleterTest extends GroovyTestCase {

    void testIOMock() {
        IO testio
        ByteArrayOutputStream mockOut
        ByteArrayOutputStream mockErr
        mockOut = new ByteArrayOutputStream()
        mockErr = new ByteArrayOutputStream()
        testio = new IO(new ByteArrayInputStream(), mockOut, mockErr)
        testio.out.println('mockResult')
        assertTrue('stdout=' + mockOut.toString() + '\nstderr=' + mockErr.toString(), mockOut.toString().contains('mockResult'))
        testio.err.println('mockErrResult')
        assertTrue('stdout=' + mockOut.toString() + '\nstderr=' + mockErr.toString(), mockErr.toString().contains('mockErrResult'))
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
        List<ReflectionCompletionCandidate> candResult = (List<ReflectionCompletionCandidate>) groovysh.interp.evaluate([
                """\
import ${ReflectionCompleter.getCanonicalName()}
class Foo extends HashSet implements Comparable {
  int compareTo(Object) {0};
  int priv;
  static int priv2;
  public int foo;
  public static int bar;
  int foom(){1};
  static int barm(){2};
  static int getPriv3(){3};
  private int getPriv4(){4};
  int getPriv5(){5};
}
ReflectionCompleter.getPublicFieldsAndMethods(Foo, '')
"""])
        assert candResult
        assert candResult.size() > 0
        List<String> result = candResult*.value
        assert [] == result.findAll({ it.startsWith('_') })
        assert [] == result.findAll({ it.startsWith('super$') })
        assert [] == result.findAll({ it.startsWith('this$') })
        assert !('foo' in result)
        assert !('priv' in result)
        assert ('priv2' in result)
        assert ('priv3' in result)
        assert !('priv4' in result)
        assert !('priv5' in result)
        assert 'barm()' in result
        assert !('foom()' in result)

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
        List<ReflectionCompletionCandidate> candResult = (List<ReflectionCompletionCandidate>) groovysh.interp.evaluate([
                """\
import ${ReflectionCompleter.getCanonicalName()}
class Foo extends HashSet implements Comparable {
  int compareTo(Object) {0};
  int priv;
  static int priv2;
  private int foo;
  static int bar;
  int foom(){1};
  static int barm(){2}
  static int getPriv3(){3};
  private int getPriv4(){4};
  int getPriv5(){5};
}
ReflectionCompleter.getPublicFieldsAndMethods(new Foo(), '')
"""])
        assertNotNull(candResult)
        assert candResult.size() > 0
        List<String> result = candResult*.value
        assert [] == result.findAll({ it.startsWith('_') })
        assert [] == result.findAll({ it.startsWith('super$') })
        assert [] == result.findAll({ it.startsWith('this$') })
        assert !('bar' in result)
        assert ('priv' in result)
        assert !('priv2' in result)
        assert !('priv3' in result)
        assert !('priv4' in result)
        assert ('priv5' in result)
        assert 'foom()' in result
        assert !('barm()' in result)
    }

    void testImportedClassStaticMember() {
        // tests that import are taken into account when evaluating for completion
        IO testio = new IO()
        Groovysh groovysh = new Groovysh(new URLClassLoader(), new Binding(), testio)
        def result = groovysh.execute('import ' + GroovyException.name)
        assert result == GroovyException.canonicalName
        ReflectionCompleter compl = new ReflectionCompleter(groovysh)
        def candidates = []

        compl.complete(TokenUtilTest.tokenList('GroovyException.'), candidates)
        assert candidates.size() == 0
        compl.complete(TokenUtilTest.tokenList('GroovyException.find'), candidates)
        assert candidates.size() > 0
    }

    void _fixme_testSortCandidates() {
        // tests that import are taken into account when evaluating for completion
        IO testio = new IO()
        Groovysh groovysh = new Groovysh(new URLClassLoader(), new Binding(), testio)
        ReflectionCompleter compl = new ReflectionCompleter(groovysh)
        def candidates = []
        compl.complete(TokenUtilTest.tokenList(/['a':3, 'b':4]./), candidates)
        assert candidates.size() > 1
        assert candidates.reverse().subList(0, 3).collect({ String it -> stripAnsi(it) }) == ['empty', 'b', 'a']
    }

    /**
    * copied from jline2 ConsoleReader
    */
    private static CharSequence stripAnsi(final CharSequence str) {
        if (str == null) return ''
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream()
            AnsiOutputStream aos = new AnsiOutputStream(baos)
            aos.write(str.toString().bytes)
            aos.flush()
            return baos.toString()
        } catch (IOException e) {
            return str
        }
    }

}



class GroovyshUtilsTest extends GroovyTestCase {

    void testIsTypeOrMethodDeclaration() {
        List<String> buffer = []
        assert !Groovysh.isTypeOrMethodDeclaration(buffer)
        buffer = ['']
        assert !Groovysh.isTypeOrMethodDeclaration(buffer)
        buffer = ['foo']
        assert !Groovysh.isTypeOrMethodDeclaration(buffer)
        buffer = ['foo()']
        assert !Groovysh.isTypeOrMethodDeclaration(buffer)
        buffer = ['123']
        assert !Groovysh.isTypeOrMethodDeclaration(buffer)
        buffer = ['def foo() {}']
        assert Groovysh.isTypeOrMethodDeclaration(buffer)
        buffer = ['public static void bar() {}']
        assert Groovysh.isTypeOrMethodDeclaration(buffer)
        buffer = ['public class Foo {}']
        assert Groovysh.isTypeOrMethodDeclaration(buffer)
        buffer = ['interface Foo {}']
        assert Groovysh.isTypeOrMethodDeclaration(buffer)
        buffer = ['enum Foo {VAL1, VAL2}']
        assert Groovysh.isTypeOrMethodDeclaration(buffer)
    }
}
