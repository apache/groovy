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
package org.codehaus.groovy.classgen.asm.sc

import groovy.transform.stc.ClosuresSTCTest
import org.junit.jupiter.api.Test

/**
 * Unit tests for static compilation: closures.
 */
final class ClosuresStaticCompileTest extends ClosuresSTCTest implements StaticCompilationTestSupport {

    // GROOVY-12678: a statically compiled lambda is owned by the enclosing instance
    @Override @Test
    void testNestedLambdaDelegateOwner() {
        for (prop in ['delegate', 'owner', 'getDelegate()', 'getOwner()']) {
            assertScript """
                import java.util.function.Supplier
                class C {
                    String name = 'n'
                    void m() {
                        def c = { ->
                            Supplier<Object> s = () -> {
                                name
                                @ASTTest(phase=INSTRUCTION_SELECTION, value={
                                    def type = node.getNodeMetaData(INFERRED_TYPE)?.toString(false)
                                    assert type/*of $prop*/ == 'C'
                                })
                                Object ref = $prop
                                assert ref.is(this)
                            }
                            s.get()
                        }
                        c.call()
                        Supplier<Object> t = () -> {
                            name
                            Supplier<Object> s = () -> {
                                name
                                @ASTTest(phase=INSTRUCTION_SELECTION, value={
                                    def type = node.getNodeMetaData(INFERRED_TYPE)?.toString(false)
                                    assert type/*of $prop*/ == 'C'
                                })
                                Object ref = $prop
                                assert ref.is(this)
                            }
                            s.get()
                        }
                        t.get()
                    }
                }
                new C().m()
            """
        }
    }

    // GROOVY-5584
    @Test
    void testEachOnMapClosure() {
        assertScript '''
            def test() {
                def result = ""
                [a:1, b:3].each { key, value -> result += "$key$value" }
                assert result == "a1b3"
            }
            test()
        '''
    }

    // GROOVY-12576
    @Test
    void testObjectMethodsInClosureWithoutThisObject() {
        assertScript '''
            class C {
                String toString() { 'C' }
                List<Closure> closures() {
                    [{ -> toString() }, { -> hashCode() }, { -> equals(this) }, { -> getClass() }]
                }
            }

            C c = new C()
            List<Closure> closures = c.closures()
            Closure copy = closures[0].dehydrate()
            assert copy() == copy.toString()
            copy = closures[1].dehydrate()
            assert copy() == copy.hashCode()
            assert closures[2].dehydrate()() == false
            copy = closures[3].dehydrate()
            assert copy() == copy.getClass()

            // without a this object, the owner's method is called
            assert closures[0].rehydrate(null, c, null)() == 'C'
            assert closures[1].rehydrate(null, c, null)() == c.hashCode()
            assert closures[2].rehydrate(null, c, null)() == false
            assert closures[3].rehydrate(null, c, null)() == C

            assert closures[0]() == 'C'
            assert closures[1]() == c.hashCode()
            assert closures[2]() == true
            assert closures[3]() == C
        '''
    }

    // GROOVY-12576
    @Test
    void testObjectMethodsInLambdaInsideClosure() {
        assertScript '''
            import java.util.function.Supplier

            class C {
                String toString() { 'C' }
                int hashCode() { 42 }
                boolean equals(Object o) { o instanceof C }
                List<Object> m() {
                    C that = new C()
                    List<Object> result = []
                    'x'.with {
                        Supplier<String> s = () -> toString()
                        Supplier<Integer> h = () -> hashCode()
                        Supplier<Boolean> e = () -> equals(that)
                        Supplier<Class> k = () -> getClass()
                        Supplier<String> n = () -> { { -> toString() }() }
                        result << s.get() << h.get() << e.get() << k.get() << n.get()
                    }
                    result
                }
                static String sm() {
                    'x'.with { Supplier<String> s = () -> toString(); s.get() }
                }
            }

            assert new C().m() == ['C', 42, true, C, 'C']
            assert C.sm() == 'class C'
        '''
    }

    // GROOVY-12576
    @Test
    void testInheritedObjectMethodInLambdaInsideClosure() {
        assertScript '''
            import java.util.function.Supplier

            class C {
                String m() {
                    'x'.with { Supplier<String> s = () -> toString(); s.get() }
                }
            }
            trait T {
                String m() {
                    'x'.with { Supplier<String> s = () -> toString(); s.get() }
                }
            }
            class K implements T {
                String toString() { 'K' }
            }

            C c = new C()
            assert c.m() == c.toString()
            assert c.m().startsWith('C@')
            assert new K().m() == 'K'
        '''
    }

    // GROOVY-12576
    @Test
    void testObjectMethodInLambdaInsideDelegateOnlyClosure() {
        assertScript '''
            import java.util.function.Supplier

            class D {
                String toString() { 'D' }
            }
            class C {
                String name
                String toString() { name }
                static <T> T withC(@DelegatesTo(value = C, strategy = Closure.DELEGATE_ONLY) Closure<T> block) {
                    block.delegate = new C(name: 'delegate')
                    block.resolveStrategy = Closure.DELEGATE_ONLY
                    block.call()
                }
                static <T> T withD(@DelegatesTo(value = D, strategy = Closure.DELEGATE_ONLY) Closure<T> block) {
                    block.delegate = new D()
                    block.resolveStrategy = Closure.DELEGATE_ONLY
                    block.call()
                }
                String sameType() {
                    withC { Supplier<String> s = () -> toString(); s.get() }
                }
                String otherType() {
                    withD { Supplier<String> s = () -> toString(); s.get() }
                }
            }

            C c = new C(name: 'this')
            assert c.sameType() == 'this'
            assert c.otherType() == 'this'
        '''
    }

    // GROOVY-12576
    @Test
    void testInheritedObjectMethodInClosureWithoutThisObject() {
        assertScript '''
            class C {
                Closure<String> closure() {
                    return { -> toString() }
                }
            }

            C c = new C()
            Closure<String> closure = c.closure()
            assert closure() == c.toString()

            Closure<String> copy = closure.dehydrate()
            assert copy() == copy.toString()

            // without a this object, the owner's method is called
            assert closure.rehydrate(null, c, null)() == c.toString()
            assert closure.rehydrate('o', 'o', 'o')() == 'o'
        '''
    }

    // GROOVY-12576
    @Test
    void testInstanceExtensionMethodInClosureWithoutThisObject() {
        assertScript '''
            class C {
                String toString() { 'C!' }
                Closure<String> closure() {
                    return { -> inspect() }
                }
            }

            C c = new C()
            Closure<String> closure = c.closure()
            assert closure() == 'C!'

            Closure<String> copy = closure.dehydrate()
            assert copy() == copy.toString()

            // without a this object, the extension method is called on the owner
            assert closure.rehydrate(null, c, null)() == 'C!'
            assert closure.rehydrate('o', 'o', 'o')() == "'o'"
        '''
    }

    // GROOVY-12576
    @Test
    void testObjectExtensionMethodInLambdaInsideClosure() {
        assertScript '''
            import java.util.function.Supplier

            class D {
                String toString() { 'D' }
            }
            class C {
                String toString() { 'C' }
                List<Object> m() {
                    List<Object> result = []
                    new D().with {
                        Supplier<Object> s1 = () -> { println('a'); print('b'); printf('%s', 'c'); null }
                        Supplier<Object> s2 = () -> sprintf('%s', 'd')
                        Supplier<Object> s3 = () -> inspect()
                        Supplier<Object> s4 = () -> dump()
                        Supplier<Object> s5 = () -> with { it.toString() }
                        result << s1.get() << s2.get() << s3.get() << s4.get() << s5.get()
                    }
                    result
                }
                static List<Object> sm() {
                    List<Object> result = []
                    new D().with {
                        Supplier<Object> s1 = () -> { println('a'); print('b'); printf('%s', 'c'); null }
                        Supplier<Object> s2 = () -> sprintf('%s', 'd')
                        Supplier<Object> s3 = () -> inspect()
                        Supplier<Object> s4 = () -> dump()
                        Supplier<Object> s5 = () -> with { it.toString() }
                        result << s1.get() << s2.get() << s3.get() << s4.get() << s5.get()
                    }
                    result
                }
            }

            def printed = new ByteArrayOutputStream()
            PrintStream out = System.out
            System.setOut(new PrintStream(printed))
            try {
                List<Object> result = new C().m()
                assert result[0..1] == [null, 'd']
                assert result[2] == 'C'
                assert result[3].toString().startsWith('<C@')
                assert result[4] == 'C'

                result = C.sm()
                assert result[0..1] == [null, 'd']
                assert result[2] == 'class C'
                assert result[3].toString().startsWith('<java.lang.Class@')
                assert result[4] == 'class C'
            } finally {
                System.setOut(out)
            }
            assert printed.toString().readLines() == ['a', 'bca', 'bc']
        '''
    }

    // GROOVY-12576
    @Test
    void testObjectExtensionMethodInLambdaInsideClosureInTrait() {
        assertScript '''
            import java.util.function.Supplier

            class D {
                String toString() { 'D' }
            }
            trait T {
                List<Object> m() {
                    List<Object> result = []
                    new D().with {
                        Supplier<Object> s1 = () -> { println('a'); print('b'); printf('%s', 'c'); null }
                        Supplier<Object> s2 = () -> sprintf('%s', 'd')
                        Supplier<Object> s3 = () -> inspect()
                        Supplier<Object> s4 = () -> dump()
                        Supplier<Object> s5 = () -> with { it.toString() }
                        result << s1.get() << s2.get() << s3.get() << s4.get() << s5.get()
                    }
                    result
                }
            }
            class K implements T {
                String toString() { 'K' }
            }

            def printed = new ByteArrayOutputStream()
            PrintStream out = System.out
            System.setOut(new PrintStream(printed))
            try {
                List<Object> result = new K().m()
                assert result[0..1] == [null, 'd']
                assert result[2] == 'K'
                assert result[3].toString().startsWith('<K@')
                assert result[4] == 'K'
            } finally {
                System.setOut(out)
            }
            assert printed.toString().readLines() == ['a', 'bc']
        '''
    }

    // GROOVY-12576
    @Test
    void testObjectExtensionMethodInLambdaInsideClosureInScript() {
        assertScript '''
            import java.util.function.Supplier

            class D {
                String toString() { 'D' }
            }

            String toString() { 'script' }

            def printed = new ByteArrayOutputStream()
            PrintStream out = System.out
            System.setOut(new PrintStream(printed))
            try {
                List<Object> result = []
                new D().with {
                    Supplier<Object> s1 = () -> { println('a'); print('b'); printf('%s', 'c'); null }
                    Supplier<Object> s2 = () -> sprintf('%s', 'd')
                    Supplier<Object> s3 = () -> inspect()
                    Supplier<Object> s4 = () -> with { it.toString() }
                    result << s1.get() << s2.get() << s3.get() << s4.get()
                }
                assert result == [null, 'd', 'script', 'script']
            } finally {
                System.setOut(out)
            }
            assert printed.toString().readLines() == ['a', 'bc']
        '''
    }

    // GROOVY-12576
    @Test
    void testObjectExtensionMethodInLambdaInsideDelegateOnlyClosure() {
        assertScript '''
            import java.util.function.Supplier

            class D {
                String toString() { 'D' }
            }
            class C {
                String toString() { 'C' }
                static <T> T withD(@DelegatesTo(value = D, strategy = Closure.DELEGATE_ONLY) Closure<T> block) {
                    block.delegate = new D()
                    block.resolveStrategy = Closure.DELEGATE_ONLY
                    block.call()
                }
                List<Object> m() {
                    List<Object> result = []
                    withD {
                        Supplier<Object> s1 = () -> { println('a'); print('b'); printf('%s', 'c'); null }
                        Supplier<Object> s2 = () -> sprintf('%s', 'd')
                        Supplier<Object> s3 = () -> inspect()
                        Supplier<Object> s4 = () -> dump()
                        Supplier<Object> s5 = () -> with { it.toString() }
                        result << s1.get() << s2.get() << s3.get() << s4.get() << s5.get()
                    }
                    result
                }
                void uncalled() {
                    withD {
                        Supplier<Object> s1 = () -> { println('a'); print('b'); printf('%s', 'c'); null }
                        Supplier<Object> s2 = () -> sprintf('%s', 'd')
                        Supplier<Object> s3 = () -> inspect()
                        Supplier<Object> s4 = () -> dump()
                        Supplier<Object> s5 = () -> with { it.toString() }
                    }
                }
            }

            def printed = new ByteArrayOutputStream()
            PrintStream out = System.out
            System.setOut(new PrintStream(printed))
            try {
                List<Object> result = new C().m()
                assert result[0..1] == [null, 'd']
                assert result[2] == 'C'
                assert result[3].toString().startsWith('<C@')
                assert result[4] == 'C'
            } finally {
                System.setOut(out)
            }
            assert printed.toString().readLines() == ['a', 'bc']
        '''
    }

    // GROOVY-5811
    @Test
    void testClosureExceptionUnwrap() {
        assertScript '''
            @groovy.transform.InheritConstructors
            public class MyException extends Exception {}

            void foo() {
                throw new MyException()
            }

            void bar() {
                boolean caught = false
                try {
                    def cl = { foo() }
                    cl()
                } catch (MyException e) {
                    caught = true
                } finally {
                    assert caught
                }
            }

            bar()
        '''
    }

    // GROOVY-6522
    @Test
    void testShouldCallClosure() {
        assertScript '''
            class Sample {
                Closure formatFirstName, formatLastName

                void doStuff (String fname, String lname, Closure callback) {
                    formatFirstName(fname) { String fnameError, String formattedFname ->
                        formatLastName(lname) { String lnameError, String formattedLname ->
                            String errors = "${fnameError ? "${fnameError}, " : ''}${lnameError ?: ''}"
                            callback(errors, formattedFname, formattedLname)
                        }
                    }
                }
            }

            Closure ffn = { String fname, Closure callback ->
                String firstInitial = fname?.substring(0,1)
                if (!firstInitial)
                    callback('invalid first name', null)
                else
                    callback(null, firstInitial.toLowerCase())
            }

            Closure fln = { String lname, Closure callback ->
                String lastPrefix = lname?.size() > 2 ? lname.substring(0,3) : null
                if (!lastPrefix)
                    callback('invalid last name', null)
                else
                    callback(null, lastPrefix.toLowerCase())
            }

            Sample sample = new Sample(formatFirstName: ffn, formatLastName: fln)

            sample.doStuff('John', 'Doe') { String errors, String formattedFname, String formattedLname ->
                if (errors)
                    println errors
                else
                    println "${formattedFname}.${formattedLname}"
            }
        '''
    }

    @Test
    void testMethodVersusPropertyOnCompoundAssignmentTarget() {
        assertScript '''
            import org.codehaus.groovy.ast.expr.*
            import org.codehaus.groovy.ast.stmt.*

            class Pogo {
                String string = ''
                void setString(String string) {
                    this.string = string
                }
            }

            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                node = node.code.statements[0].expression
                assert node instanceof MethodCallExpression

                node = node.arguments.expressions[0].code.statements[0].expression
                assert node instanceof BinaryExpression

                assert node.leftExpression instanceof PropertyExpression
                def target = node.leftExpression.getNodeMetaData(DIRECT_METHOD_CALL_TARGET)

                assert target != null && target.name == 'setString'
            })
            void test(Pogo pogo) {
                pogo.with {
                    string += 'x'
                }
            }

            test(new Pogo())
        '''
    }
}
