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

import groovy.mock.interceptor.MockFor
import org.codehaus.groovy.antlr.parser.GroovyLexer
import org.codehaus.groovy.tools.shell.commands.ImportCommand


class GroovyshCompletorTest extends GroovyTestCase {

    void testLiveClass() {
        /* This test setup looks weird, but it is the only I found that can reproduce this behavior:
groovy:000> class Foo extends HashSet implements Comparable {int compareTo(Object) {0}}
===> true
groovy:000> Foo.
__$stMC              __$swapInit()            __timeStamp
super$1$getClass()   super$1$notify()         super$1$notifyAll()
*/
        IO testio
        ByteArrayOutputStream mockOut
        ByteArrayOutputStream mockErr
        mockOut = new ByteArrayOutputStream();
        mockErr = new ByteArrayOutputStream();
        testio = new IO(
                new ByteArrayInputStream(),
                mockOut,
                mockErr)
        Groovysh groovysh = new Groovysh(testio)
        groovysh.run("import org.codehaus.groovy.tools.shell.ReflectionCompletor")
        groovysh.run("""class Foo extends HashSet implements Comparable {
int compareTo(Object) {0}; int foo; static int bar; int foom(){1}; static int barm(){2}}""")
        groovysh.run("ReflectionCompletor.getPublicFieldsAndMethods(Foo, \"\")")
        String rawout = mockOut.toString()
        List<String> findResult = rawout.split('\\[')[-1].split()[0..-2].collect({ it -> it.trim()[0..-2] })
        assertEquals([], findResult.findAll({ it.startsWith("_") }))
        assertEquals([], findResult.findAll({ it.startsWith("super\$") }))
        assertEquals([], findResult.findAll({ it.startsWith("this\$") }))
        assertEquals(rawout, 99, findResult.size())
    }

    void testLiveInstance() {
        /* This test setup looks weird, but it is the only I found that can reproduce this behavior:
groovy:000> class Foo extends HashSet implements Comparable {int compareTo(Object) {0}}
===> true
groovy:000> Foo.
__$stMC              __$swapInit()            __timeStamp
super$1$getClass()   super$1$notify()         super$1$notifyAll()
*/
        IO testio
        ByteArrayOutputStream mockOut
        ByteArrayOutputStream mockErr
        mockOut = new ByteArrayOutputStream();
        mockErr = new ByteArrayOutputStream();
        testio = new IO(
                new ByteArrayInputStream(),
                mockOut,
                mockErr)
        Groovysh groovysh = new Groovysh(testio)
        groovysh.run("import org.codehaus.groovy.tools.shell.ReflectionCompletor")
        groovysh.run("""class Foo extends HashSet implements Comparable {
int compareTo(Object) {0}; int foo; static int bar; int foom(){1}; static int barm(){2}}""")
        groovysh.run("ReflectionCompletor.getPublicFieldsAndMethods(new Foo(), \"\")")
        String rawout = mockOut.toString()
        List<String> findResult = rawout.split('\\[')[-1].split()[0..-2].collect({ it -> it.trim()[0..-2] })
        assertEquals([], findResult.findAll({ it.startsWith("_") }))
        assertEquals([], findResult.findAll({ it.startsWith("super\$") }))
        assertEquals([], findResult.findAll({ it.startsWith("this\$") }))
        assertEquals(rawout, 121, findResult.size())
    }
}

class ReflectionCompletorUnitTest extends GroovyTestCase {

    void testGetFieldsAndMethodsArray() {
        Collection<String> result = ReflectionCompletor.getPublicFieldsAndMethods([] as String[], "")
        assertEquals(result.toString(), 91, result.size())
        result = ReflectionCompletor.getPublicFieldsAndMethods([] as String[], "size")
        assertEquals(["size()"], result)
    }

    void testGetFieldsAndMethodsMap() {
        Collection<String> result = ReflectionCompletor.getPublicFieldsAndMethods(['id': '42'], "")
        assertEquals(96, result.size())
        result = ReflectionCompletor.getPublicFieldsAndMethods(['id': '42'], "size")
        // e.g. don't show non-public inherited size field
        assertEquals(["size()"], result)
    }

    void testGetFieldsAndMethodsString() {
        Collection<String> result = ReflectionCompletor.getPublicFieldsAndMethods("foo", "")
        assertEquals(159, result.size())
        result = ReflectionCompletor.getPublicFieldsAndMethods("foo", "tok")
        assertEquals(["tokenize(", "tokenize()"], result)
        result = ReflectionCompletor.getPublicFieldsAndMethods(String, "tok")
        assertEquals(["tokenize(", "tokenize()"], result)
    }

    void testGetFieldsAndMethodsPrimitive() {
        Collection<String> result = ReflectionCompletor.getPublicFieldsAndMethods(3, "")
        assertEquals(113, result.size())
        result = ReflectionCompletor.getPublicFieldsAndMethods(3, "una")
        assertEquals(["unaryMinus()"], result)
        result = ReflectionCompletor.getPublicFieldsAndMethods(Integer, "una")
        assertEquals(["unaryMinus()"], result)
        result = ReflectionCompletor.getPublicFieldsAndMethods(Integer, "MA")
        assertEquals(["MAX_VALUE"], result)
        result = ReflectionCompletor.getPublicFieldsAndMethods(Integer, "getI")
        assertEquals(["getInteger("], result)
    }

    void testGetFieldsAndMethodsInterface() {
        Collection<String> result = ReflectionCompletor.getPublicFieldsAndMethods(Set, "")
        assertEquals(96, result.size())
        result = ReflectionCompletor.getPublicFieldsAndMethods(Set, "toA")
        assertEquals([], result)
    }

    void testGetInterfaceFields() {
        Collection<String> result = ReflectionCompletor.getPublicFieldsAndMethods(GroovyLexer, "")
        assertEquals(277, result.size())
        result = ReflectionCompletor.getPublicFieldsAndMethods(GroovyLexer, "LITERAL_as")
        assertEquals(["LITERAL_as", "LITERAL_assert"], result)
        GroovyLexer lexer = new GroovyLexer(new ByteArrayInputStream("".getBytes()))
        result = ReflectionCompletor.getPublicFieldsAndMethods(lexer, "LITERAL_as")
        assertEquals(["LITERAL_as", "LITERAL_assert"], result)
    }

    void testGetFieldsAndMethodsClass() {
        Collection<String> result = ReflectionCompletor.getPublicFieldsAndMethods(HashSet.getClass(), "")
        assertEquals(52, result.size())
        result = ReflectionCompletor.getPublicFieldsAndMethods(HashSet, "pro")
        assertEquals([], result)
        result = ReflectionCompletor.getPublicFieldsAndMethods(HashSet, "toA")
        assertEquals([], result)
        result = ReflectionCompletor.getPublicFieldsAndMethods(new HashSet(), "toA")
        assertEquals(["toArray(", "toArray()"], result)
    }

    void testGetFieldsAndMethodsCustomClass() {
        Interpreter interp = new Interpreter(Thread.currentThread().contextClassLoader, new Binding())
        Object instance = interp.evaluate(["class Foo extends HashSet implements Comparable {int compareTo(Object) {0}}"])
        Collection<String> result = ReflectionCompletor.getPublicFieldsAndMethods(instance.getClass(), "")
        assertEquals(49, result.size)
        result = ReflectionCompletor.getPublicFieldsAndMethods([] as String[], "si")
        assertEquals(["size()"], result)
    }

}

class ReflectionCompletorTest extends CompletorTestSupport {

    void testEmpty() {
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            ReflectionCompletor completor = new ReflectionCompletor(groovyshMock)
            assertEquals(-1, completor.complete("", 0, []))
        }
    }

    void testUnknownVar() {
        groovyshMocker.demand.getInterp(1) { [context: [variables: [:]]] }
        groovyshMocker.demand.getInterp(1) { [classLoader: [loadedClasses: []]] }
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            ReflectionCompletor completor = new ReflectionCompletor(groovyshMock)
            def candidates = []
            assertEquals(-1, completor.complete("xyz", 2, candidates))
            assertEquals([], candidates)
        }
    }

    void testPackageAccess() {
        groovyshMocker.demand.getInterp(1) { [context: [variables: [:]]] }
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            ReflectionCompletor completor = new ReflectionCompletor(groovyshMock)
            def candidates = []
            String prompt = "println(java."
            assertEquals(-1, completor.complete(prompt, prompt.length(), candidates))
            assertEquals([], candidates)
        }
    }

    void testKeywordModifier() {
        groovyshMocker.demand.getInterp(1) { [context: [variables: [:]]] }
        groovyshMocker.demand.getInterp(1) { [classLoader: [loadedClasses: []]] }
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            ReflectionCompletor completor = new ReflectionCompletor(groovyshMock)
            def candidates = []
            String buffer = "pub"
            assertEquals(0, completor.complete(buffer, buffer.length(), candidates))
            assertEquals(['public '], candidates)
        }
    }

    void testKeywordModifierSecond() {
        CommandRegistry registry = new CommandRegistry()
        groovyshMocker.demand.getRegistry(1) { registry }
        groovyshMocker.demand.getInterp(1) { [context: [variables: [:]]] }
        groovyshMocker.demand.getInterp(1) { [classLoader: [loadedClasses: []]] }
        groovyshMocker.demand.getRegistry(1) { registry }
        groovyshMocker.demand.getInterp(1) { [context: [variables: [:]]] }
        groovyshMocker.demand.getInterp(1) { [classLoader: [loadedClasses: []]] }
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            ReflectionCompletor completor = new ReflectionCompletor(groovyshMock)
            def candidates = []
            String buffer = "public sta"
            assertEquals(7, completor.complete(buffer, buffer.length(), candidates))
            assertEquals(['static '], candidates)
            candidates = []
            buffer = "public swi" // don't suggest switch keyword here
            assertEquals(7, completor.complete(buffer, buffer.length(), candidates))
            assertEquals(["switch ("], candidates)
        }
    }

    void testKeywordModifierThird() {
        CommandRegistry registry = new CommandRegistry()
        groovyshMocker.demand.getRegistry(1) { registry }
        groovyshMocker.demand.getInterp(1) { [context: [variables: [:]]] }
        groovyshMocker.demand.getInterp(1) { [classLoader: [loadedClasses: []]] }
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            ReflectionCompletor completor = new ReflectionCompletor(groovyshMock)
            def candidates = []
            String buffer = "public static inter"
            assertEquals(14, completor.complete(buffer, buffer.length(), candidates))
            assertEquals(['interface '], candidates)
        }
    }

    void testKeywordModifierFor() {
        groovyshMocker.demand.getInterp(1) { [context: [variables: [:]]] }
        CommandRegistry registry = new CommandRegistry()
        groovyshMocker.demand.getInterp(1) { [classLoader: [loadedClasses: []]] }
        groovyshMocker.demand.getRegistry(1) { registry }
        groovyshMocker.demand.getInterp(1) { [context: [variables: [:]]] }
        groovyshMocker.demand.getInterp(1) { [classLoader: [loadedClasses: []]] }
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            ReflectionCompletor completor = new ReflectionCompletor(groovyshMock)
            def candidates = []
            String buffer = "fo"
            assertEquals(0, completor.complete(buffer, buffer.length(), candidates))
            assertEquals(['for ('], candidates)
            candidates = []
            buffer = "for (pub" // don't suggest public keyword here
            assertEquals(5, completor.complete(buffer, buffer.length(), candidates))
            assertEquals(["public "], candidates)
        }
    }

    void testKnownVar() {
        groovyshMocker.demand.getInterp(1) { [context: [variables: [xyzabc: ""]]] }
        groovyshMocker.demand.getInterp(1) { [classLoader: [loadedClasses: []]] }
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            ReflectionCompletor completor = new ReflectionCompletor(groovyshMock)
            def candidates = []
            assertEquals(0, completor.complete("xyz", 2, candidates))
            assertEquals(["xyzabc"], candidates)
        }
    }

    void testKnownVarMultiple() {
        groovyshMocker.demand.getInterp(1) { [context: [variables: [bad: "", xyzabc: "", xyzfff: "", nope: ""]]] }
        groovyshMocker.demand.getInterp(1) { [classLoader: [loadedClasses: []]] }
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            ReflectionCompletor completor = new ReflectionCompletor(groovyshMock)
            def candidates = []
            assertEquals(0, completor.complete("xyz", 2, candidates))
            assertEquals(["xyzabc", "xyzfff"], candidates)
        }
    }

    void testKnownField() {
        groovyshMocker.demand.getInterp(1) { [evaluate: { expr -> Math }] }
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            ReflectionCompletor completor = new ReflectionCompletor(groovyshMock)
            def candidates = []
            // xyz cannot be not a var here
            assertEquals(5, completor.complete("Math.P", "Math.P".length(), candidates))
            assertEquals(["PI"], candidates)
        }
    }

    void testKnownVarBeforeDot() {
        groovyshMocker.demand.getInterp(1) { [evaluate: {expr -> assert(expr == "xyz"); "foo"}, context: [variables: [xyzabc: ""]]] }
        groovyshMocker.demand.getInterp(1) { [classLoader: [loadedClasses: []]] }
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            ReflectionCompletor completor = new ReflectionCompletor(groovyshMock)
            def candidates = []
            // cursor is BEFORE dot
            assertEquals(8, completor.complete("Foo.bar(xyz.", "Foo.bar(xyz".length(), candidates))
            assertEquals(["xyzabc"], candidates)
        }
    }

    void testKnownClassMember() {
        groovyshMocker.demand.getInterp(1) { [evaluate: { Math }] }
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            ReflectionCompletor completor = new ReflectionCompletor(groovyshMock)
            def candidates = []
            String buffer = "Math.ma"
            assertEquals(5, completor.complete(buffer, buffer.length(), candidates))
            assertEquals(["max("], candidates)
        }
    }
    
    void testKnownVarAfterDot() {
        groovyshMocker.demand.getInterp(1) { [evaluate: { expr -> assert (expr == ["xyz"]) }, context: [variables: [xyzabc: ""]]] }
        groovyshMocker.demand.getInterp(1) { [classLoader: [loadedClasses: []]] }
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            ReflectionCompletor completor = new ReflectionCompletor(groovyshMock)
            def candidates = []
            assertEquals(8, completor.complete("Foo.bar(xyz", "Foo.bar(xyz".length(), candidates))
            assertEquals(["xyzabc"], candidates)
        }
    }

    void testKnownMethod() {
        groovyshMocker.demand.getInterp(1) { [context: [variables: [javup: String.&toString]]] }
        groovyshMocker.demand.getInterp(1) { [classLoader: [loadedClasses: []]] }
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            ReflectionCompletor completor = new ReflectionCompletor(groovyshMock)
            def candidates = []
            assertEquals(0, completor.complete("jav", 2, candidates))
            assertEquals(["javup()"], candidates)
        }
    }

    void testKnownVarAndClass() {
        groovyshMocker.demand.getInterp(1) { [context: [variables: [javup: ""]]] }
        groovyshMocker.demand.getInterp(1) { [classLoader: [loadedClasses: [String]]] }
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            ReflectionCompletor completor = new ReflectionCompletor(groovyshMock)
            def candidates = []
            // in the shell, only Classes in the default package occur,but well...
            assertEquals(0, completor.complete("jav", 2, candidates))
            assertEquals(["javup", "java.lang.String"], candidates)
        }
    }

    void testKnownClass() {
        groovyshMocker.demand.getInterp(1) { [context: [variables: [:]]] }
        groovyshMocker.demand.getInterp(1) { [classLoader: [loadedClasses: [String]]] }
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            ReflectionCompletor completor = new ReflectionCompletor(groovyshMock)
            def candidates = []
            // in the shell, only Classes in the default package occur,but well...
            assertEquals(0, completor.complete("jav", 2, candidates))
            assertEquals(["java.lang.String"], candidates)
        }
    }

    void testKnownMethodWithArgs() {
        groovyshMocker.demand.getInterp(1) { [context: [variables: [javup: Math.&max]]] }
        groovyshMocker.demand.getInterp(1) { [classLoader: [loadedClasses: []]] }
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            ReflectionCompletor completor = new ReflectionCompletor(groovyshMock)
            def candidates = []
            assertEquals(0, completor.complete("jav", 2, candidates))
            assertEquals(["javup("], candidates)
        }
    }

    void testKnownVarMultipleDots() {
        //mock asserts (flawed) "baz" is not evaluated
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            ReflectionCompletor completor = new ReflectionCompletor(groovyshMock)
            def candidates = []
            String buffer = "foo(Bar.baz.xyz"
            assertEquals(-1, completor.complete(buffer, buffer.length(), candidates))
            assertEquals([], candidates)
        }
    }

    void testKnownVarAfterMethod() {
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            ReflectionCompletor completor = new ReflectionCompletor(groovyshMock)
            def candidates = []
            // xyz cannot be not a var here
            assertEquals(-1, completor.complete("Fo.ba().xyz", "Fo.ba().xyz".length(), candidates))
            assertEquals([], candidates)
        }
    }


    void testLiteralStringMethod() {
        // mock doing the right thing
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            ReflectionCompletor completor = new ReflectionCompletor(groovyshMock)
            def candidates = []
            String buffer = "\"foo\".subs"
            assertEquals(-1, completor.complete(buffer, buffer.length(), candidates))
            assertEquals([], candidates)
        }
    }

    void testDontEvaluateMethod() {
        MockFor registryMocker = new MockFor(CommandRegistry)
        registryMocker.demand.commands(1) { [] }
        registryMocker.use {
            CommandRegistry registry = new CommandRegistry()
            groovyshMocker.demand.getRegistry(1) { registry }
            // mock doing the right thing
            groovyshMocker.demand.getInterp(1) { [evaluate: { expr -> assert (expr == ["foo"]); "foo" }] }
            groovyshMocker.use {
                Groovysh groovyshMock = new Groovysh()
                ReflectionCompletor completor = new ReflectionCompletor(groovyshMock)
                def candidates = []
                String buffer = "deletehardDisk(); foo.subs"
                assertEquals(22, completor.complete(buffer, buffer.length(), candidates))
                assertEquals(["substring("], candidates)
            }
        }
    }

    void testDontEvaluateAfterCommand() {
        CommandRegistry registry = new CommandRegistry()
        groovyshMocker.demand.getRegistry(1) { registry }
        // mock asserting nothing gets evaluated
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            // import command prevents reflection completion
            registry << new ImportCommand(groovyshMock)
            ReflectionCompletor completor = new ReflectionCompletor(groovyshMock)
            def candidates = []
            String buffer = "import foo"
            assertEquals(-1, completor.complete(buffer, buffer.length(), candidates))
            assertEquals([], candidates)
        }
    }

    /**
     * Evaluating Gstrings with $ is dangerous, as this may invoke method
     */
    void testLiteralStringMethodEval() {
        // mock asserting GString is not evaluated
        groovyshMocker.use {
            Groovysh groovyshMock = new Groovysh()
            ReflectionCompletor completor = new ReflectionCompletor(groovyshMock)
            def candidates = []
            String buffer = "\"\${foo.delete()}\".subs"
            assertEquals(-1, completor.complete(buffer, buffer.length(), candidates))
            assertEquals([], candidates)
        }
    }    
}
