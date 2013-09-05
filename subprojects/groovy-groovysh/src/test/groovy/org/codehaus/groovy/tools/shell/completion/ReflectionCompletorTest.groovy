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

package org.codehaus.groovy.tools.shell.completion

import org.codehaus.groovy.antlr.parser.GroovyLexer
import org.codehaus.groovy.tools.shell.Interpreter

import static org.codehaus.groovy.tools.shell.completion.TokenUtilTest.tokenList
import static org.codehaus.groovy.tools.shell.completion.TokenUtilTest.tokensString

class ReflectionCompletorUnitTest extends GroovyTestCase {

    void testGetFieldsAndMethodsArray() {
        Collection<String> result = ReflectionCompletor.getPublicFieldsAndMethods(([] as String[]), "")
        assertTrue('length' in result)
        assertTrue('clone()' in result)
        assertTrue('size()' in result)
        assertTrue('any()' in result)
        result = ReflectionCompletor.getPublicFieldsAndMethods([] as String[], "size")
        assertEquals(["size()"], result)
        result = ReflectionCompletor.getPublicFieldsAndMethods([] as String[], "le")
        assertEquals(["length"], result)
    }

    void testGetFieldsAndMethodsMap() {
        Collection<String> result = ReflectionCompletor.getPublicFieldsAndMethods(['id': '42'], "")
        assertTrue('clear()' in result)
        assertTrue('containsKey(' in result)
        assertTrue('clear()' in result)
        result = ReflectionCompletor.getPublicFieldsAndMethods(['id': '42'], "size")
        // e.g. don't show non-public inherited size field
        assertEquals(["size()"], result)
    }

    void testGetFieldsAndMethodsString() {
        Collection<String> result = ReflectionCompletor.getPublicFieldsAndMethods("foo", "")
        assertTrue('charAt(' in result)
        assertTrue('normalize()' in result)
        assertTrue('format(' in result)
        int foo = 3
        result = ReflectionCompletor.getPublicFieldsAndMethods("$foo", "")
        assertTrue('build(' in result)
        result = ReflectionCompletor.getPublicFieldsAndMethods("foo", "tok")
        assertEquals(["tokenize(", "tokenize()"], result)
        result = ReflectionCompletor.getPublicFieldsAndMethods(String, "tok")
        assertEquals(["tokenize(", "tokenize()"], result)
    }

    void testGetFieldsAndMethodsPrimitive() {
        Collection<String> result = ReflectionCompletor.getPublicFieldsAndMethods(3, "")
        assertTrue("byteValue()" in result)
        assertTrue(result.toString(), "MAX_VALUE" in result)
        assertTrue("abs()" in result)
        assertTrue(result.toString(), "notify()" in result)
        assertTrue("bitCount(" in result)
        result = ReflectionCompletor.getPublicFieldsAndMethods(3, "una")
        assertEquals(["unaryMinus()", "unaryPlus()"], result)
        result = ReflectionCompletor.getPublicFieldsAndMethods(Integer, "una")
        assertEquals(["unaryMinus()", "unaryPlus()"], result)
        result = ReflectionCompletor.getPublicFieldsAndMethods(Integer, "MA")
        assertEquals(["MAX_VALUE"], result)
        result = ReflectionCompletor.getPublicFieldsAndMethods(Integer, "getI")
        assertEquals(["getInteger("], result)
    }

    interface ForTestInterface extends Comparable {
        static final int forTestField = 1;
        void forTestMethod();
    }

    void testGetFieldsAndMethodsAnonymousClass() {
        Collection<String> result = ReflectionCompletor.getPublicFieldsAndMethods(new ForTestInterface(){
            @Override
            void forTestMethod() {}

            @Override
            int compareTo(Object o) {return 0}
        }, "")
        assertTrue('forTestField' in result)
        assertTrue('forTestMethod()' in result)
        assertTrue('compareTo(' in result)
        GroovyLexer
        result = ReflectionCompletor.getPublicFieldsAndMethods(Set, "toA")
        assertEquals([], result)
    }

    enum ForTestEnum {
        val1, val2;
        public static final ForTestEnum val3;
        int enumMethod() {return 0}
        static int staticMethod() {return 1}
    }

    void testEnum() {
        Collection<String> result = ReflectionCompletor.getPublicFieldsAndMethods(ForTestEnum, "")
        assertTrue(result.toString(), 'val1' in result)
        assertFalse(result.toString(), 'enumMethod()' in result)
        assertTrue(result.toString(), 'staticMethod()' in result)
        result = ReflectionCompletor.getPublicFieldsAndMethods(ForTestEnum.val1, "")
        // User will probably not want this
        assertFalse(result.toString(), 'val1' in result)
        assertTrue(result.toString(), 'enumMethod()' in result)
        assertTrue(result.toString(), 'staticMethod()' in result)
    }

    void testGetAbstractClassFields() {
        Collection<String> result = ReflectionCompletor.getPublicFieldsAndMethods(GroovyLexer, "")
        assertTrue(result.toString(), 'ABSTRACT' in result)
        assertTrue(result.toString(), 'isCase(' in result)
        assertTrue(result.toString(), 'tracing' in result)
        result = ReflectionCompletor.getPublicFieldsAndMethods(new GroovyLexer(new ByteArrayInputStream()), "")
        assertTrue(result.toString(), 'ABSTRACT' in result)
        assertTrue(result.toString(), 'isCase(' in result)
        assertTrue(result.toString(), 'tracing' in result)
        result = ReflectionCompletor.getPublicFieldsAndMethods(GroovyLexer, "LITERAL_as")
        assertEquals(["LITERAL_as", "LITERAL_assert"], result)
        GroovyLexer lexer = new GroovyLexer(new ByteArrayInputStream("".getBytes()))
        result = ReflectionCompletor.getPublicFieldsAndMethods(lexer, "LITERAL_as")
        assertEquals(["LITERAL_as", "LITERAL_assert"], result)
    }

    void testGetFieldsAndMethodsClass() {
        Collection<String> result = ReflectionCompletor.getPublicFieldsAndMethods(Arrays, "")
        assertTrue(result.toString(), 'sort(' in result)
        result = ReflectionCompletor.getPublicFieldsAndMethods(HashSet, "pro")
        assertEquals([], result)
        result = ReflectionCompletor.getPublicFieldsAndMethods(HashSet, "toA")
        assertEquals([], result)
        result = ReflectionCompletor.getPublicFieldsAndMethods(new HashSet(), "toA")
        assertEquals(["toArray(", "toArray()"], result)
    }

    void testGetFieldsAndMethodsCustomClass() {
        Interpreter interp = new Interpreter(Thread.currentThread().contextClassLoader, new Binding())
        Object instance = interp.evaluate(["class Foo extends HashSet implements Comparable {int compareTo(Object) {0}}; Foo"])
        Collection<String> result = ReflectionCompletor.getPublicFieldsAndMethods(instance, "")
        assertFalse('compareTo(' in result)
        instance = interp.evaluate(["class Foo extends HashSet implements Comparable {int compareTo(Object) {0}}; new Foo()"])
        result = ReflectionCompletor.getPublicFieldsAndMethods(instance, "")
        assertTrue(result.toString(), 'compareTo(' in result)
    }
}


class InvokerParsingTest extends GroovyTestCase {

    void testTokenListToEvalString() {
        assertEquals('', ReflectionCompletor.tokenListToEvalString(tokenList("")))
        assertEquals('1', ReflectionCompletor.tokenListToEvalString(tokenList("1")))
        assertEquals('1.', ReflectionCompletor.tokenListToEvalString(tokenList("1.")))
        assertEquals('foo', ReflectionCompletor.tokenListToEvalString(tokenList("foo")))
        assertEquals('"foo"', ReflectionCompletor.tokenListToEvalString(tokenList("'foo'")))
    }

    void testGetInvokerTokens() {
        // must make sure no token list is returned that could be evaluated with side effects.
        assertEquals(null, tokensString(ReflectionCompletor.getInvokerTokens(tokenList(''))))
        assertEquals('foo', tokensString(ReflectionCompletor.getInvokerTokens(tokenList('foo'))))
        assertEquals('bar.foo', tokensString(ReflectionCompletor.getInvokerTokens(tokenList('bar.foo'))))
        assertEquals('bar.&foo', tokensString(ReflectionCompletor.getInvokerTokens(tokenList('bar.&foo'))))
        // literal (simple join of token text forgets hyphens)
        assertEquals('foo', tokensString(ReflectionCompletor.getInvokerTokens(tokenList('"foo"'))))
        assertEquals('1', tokensString(ReflectionCompletor.getInvokerTokens(tokenList('1'))))
        // operator
        assertEquals('foo', tokensString(ReflectionCompletor.getInvokerTokens(tokenList('1+"foo"'))))
        assertEquals('foo', tokensString(ReflectionCompletor.getInvokerTokens(tokenList('bar == foo'))))
        assertEquals('foo', tokensString(ReflectionCompletor.getInvokerTokens(tokenList('bar = foo'))))
        assertEquals('foo', tokensString(ReflectionCompletor.getInvokerTokens(tokenList('1+foo'))))
        // begin
        assertEquals('foo', tokensString(ReflectionCompletor.getInvokerTokens(tokenList(';foo'))))
        assertEquals('foo', tokensString(ReflectionCompletor.getInvokerTokens(tokenList('(foo'))))
        assertEquals('[foo[][2]].bar', tokensString(ReflectionCompletor.getInvokerTokens(tokenList('[[foo[][2]].bar'))))
        assertEquals('foo', tokensString(ReflectionCompletor.getInvokerTokens(tokenList('${foo'))))
        assertEquals('foo', tokensString(ReflectionCompletor.getInvokerTokens(tokenList('"$foo'))))
        assertEquals('foo', tokensString(ReflectionCompletor.getInvokerTokens(tokenList('[1,foo'))))
        assertEquals('foo', tokensString(ReflectionCompletor.getInvokerTokens(tokenList('[1: foo'))))
        // Collections
        assertEquals('[1+2]', tokensString(ReflectionCompletor.getInvokerTokens(tokenList('[1+2]'))))
        assertEquals('[1..2]', tokensString(ReflectionCompletor.getInvokerTokens(tokenList('[1..2]'))))
        assertEquals('[1,2]', tokensString(ReflectionCompletor.getInvokerTokens(tokenList('[1, 2]'))))
        assertEquals('[1:2]', tokensString(ReflectionCompletor.getInvokerTokens(tokenList('[1: 2]'))))
        // allowed Parens
        assertEquals('((Foo)foo).', tokensString(ReflectionCompletor.getInvokerTokens(tokenList('((Foo) foo).'))))
        assertEquals('((1+2>4)==(a&&b)).', tokensString(ReflectionCompletor.getInvokerTokens(tokenList('((1 + 2 > 4) == (a && b)).'))))
        // not allowed
        assertEquals(null, tokensString(ReflectionCompletor.getInvokerTokens(tokenList('foo()'))))
        assertEquals(null, tokensString(ReflectionCompletor.getInvokerTokens(tokenList('foo each'))))
        assertEquals(null, tokensString(ReflectionCompletor.getInvokerTokens(tokenList('New Foo().'))))
        assertEquals(null, tokensString(ReflectionCompletor.getInvokerTokens(tokenList('foo.each bar'))))
        assertEquals(null, tokensString(ReflectionCompletor.getInvokerTokens(tokenList('foo++'))))
    }
}
