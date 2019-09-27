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

import groovy.test.GroovyTestCase
import org.apache.groovy.groovysh.completion.antlr4.ReflectionCompleter
import org.codehaus.groovy.antlr.parser.GroovyLexer
import org.apache.groovy.groovysh.Interpreter

import static org.apache.groovy.groovysh.completion.TokenUtilTest.tokenList
import static org.apache.groovy.groovysh.completion.TokenUtilTest.tokensString

class ReflectionCompleterTest extends GroovyTestCase {

    void testBeanAccessorPattern() {
        assert 'getX'.matches(ReflectionCompleter.BEAN_ACCESSOR_PATTERN)
        assert 'setX'.matches(ReflectionCompleter.BEAN_ACCESSOR_PATTERN)
        assert 'isX'.matches(ReflectionCompleter.BEAN_ACCESSOR_PATTERN)
        assert !('get'.matches(ReflectionCompleter.BEAN_ACCESSOR_PATTERN))
        assert !('getx'.matches(ReflectionCompleter.BEAN_ACCESSOR_PATTERN))
        assert !('foo'.matches(ReflectionCompleter.BEAN_ACCESSOR_PATTERN))
    }

    void testAddDefaultMethods() {
        List<String> result = ReflectionCompleter.getDefaultMethods(3, '')
        assert 'abs()' in result
        assert 'times(' in result

        result = ReflectionCompleter.getDefaultMethods([1, 2, 3], '')
        assert 'any(' in result
        assert 'count(' in result
        assert 'take(' in result
        assert 'unique()' in result

        result = ReflectionCompleter.getDefaultMethods(new String[2], '')
        assert 'any(' in result
        assert 'collect(' in result
        assert 'count(' in result
        assert 'take(' in result

        result = ReflectionCompleter.getDefaultMethods(['a': 1, 'b': 2], '')
        assert 'any(' in result
        assert 'spread()' in result
    }


    void testGetFieldsAndMethodsArray() {
        Collection<String> result = ReflectionCompleter.getPublicFieldsAndMethods(([] as String[]), '')*.value
        assert 'length' in result
        assert 'clone()' in result
        result = ReflectionCompleter.getMetaclassMethods(([] as String[]), '', true)
        assert 'size()' in result
        assert 'any()' in result
        assert 'take(' in result
        result = ReflectionCompleter.getMetaclassMethods([] as String[], 'size', true)
        assert ['size()'] == result
        result = ReflectionCompleter.getPublicFieldsAndMethods([] as String[], 'le')*.value
        assert ['length'] == result
    }

    void testGetFieldsAndMethodsMap() {
        Collection<String> result = ReflectionCompleter.getPublicFieldsAndMethods(['id': '42'], '')*.value
        assert 'clear()' in result
        assert 'containsKey(' in result
        assert 'clear()' in result
        // 'class' as key can cause bugs where .class is used instead of getClass()
        result = ReflectionCompleter.getPublicFieldsAndMethods(['class': '42', 'club': 53], '')*.value
        assert 'clear()' in result
        assert 'containsKey(' in result
        assert 'class' in result
        assert 'club' in result
        result = ReflectionCompleter.getPublicFieldsAndMethods(['id': '42'], 'size')*.value
        // e.g. don't show non-public inherited size field
        assert ['size()'] == result
    }

    void testGetFieldsAndMethodsString() {
        Collection<String> result = ReflectionCompleter.getPublicFieldsAndMethods('foo', '')*.value
        assert 'charAt(' in result
        assert 'contains(' in result
        assert ! ('format(' in result)
        result = ReflectionCompleter.getMetaclassMethods('foo', '', true)
        assert 'normalize()' in result
        int foo = 3
        result = ReflectionCompleter.getPublicFieldsAndMethods("$foo", '')*.value
        assert 'build(' in result
        result = ReflectionCompleter.getMetaclassMethods('foo', 'tok', true)
        assert ['tokenize(', 'tokenize()'] == result
        result = ReflectionCompleter.getMetaclassMethods(String, 'tok', true)
        assert ['tokenize(', 'tokenize()'] == result
    }

    void testGetFieldsAndMethodsPrimitive() {
        Collection<String> result = ReflectionCompleter.getPublicFieldsAndMethods(3, '')*.value
        assert 'byteValue()' in result
        assert ! ('MAX_VALUE' in result)
        assert ! ('valueOf(' in result)
        assert ! ('bitCount(' in result)
        result = ReflectionCompleter.getMetaclassMethods(3, '', true)
        assert 'abs()' in result
        result = ReflectionCompleter.getMetaclassMethods(3, 'una', true)
        assert ['unaryMinus()', 'unaryPlus()'] == result
        result = ReflectionCompleter.getMetaclassMethods(Integer, 'una', true)
        assert ['unaryMinus()', 'unaryPlus()'] == result
        result = ReflectionCompleter.getPublicFieldsAndMethods(Integer, 'MA')*.value
        assert ['MAX_VALUE'] == result
        result = ReflectionCompleter.getPublicFieldsAndMethods(Integer, 'getI')*.value
        assert ['getInteger('] == result
    }

    interface ForTestInterface extends Comparable<Object> {
        static final int FOR_TEST_FIELD = 1
        void forTestMethod()
    }

    void testGetFieldsAndMethodsAnonymousClass() {
        Collection<String> result = ReflectionCompleter.getPublicFieldsAndMethods(new ForTestInterface() {
            @Override
            void forTestMethod() {}

            @Override
            int compareTo(Object o) {return 0}
        }, '')*.value
        assert ! ('FOR_TEST_FIELD' in result)
        assert 'forTestMethod()' in result
        assert 'compareTo(' in result
        GroovyLexer
        result = ReflectionCompleter.getPublicFieldsAndMethods(Set, 'toA')
        assert []== result
    }

    enum ForTestEnum {
        VAL1, VAL2
        static final ForTestEnum VAL_3
        int enumMethod() {return 0}
        static int staticMethod() {return 1}
    }

    void testEnum() {
        Collection<String> result = ReflectionCompleter.getPublicFieldsAndMethods(ForTestEnum, '')
        result = result*.value
        assert 'VAL1' in result
        assert ! ( 'enumMethod()' in result)
        assert 'staticMethod()' in result
        result = ReflectionCompleter.getPublicFieldsAndMethods(ForTestEnum.VAL1, '')
        result = result*.value
        // User will probably not want this
        assert ! ( 'VAL1' in result)
        assert 'enumMethod()' in result
        assert ! ('staticMethod()' in result)
    }

    void testGetAbstractClassFields() {
        Collection<String> result = ReflectionCompleter.getPublicFieldsAndMethods(GroovyLexer, '')
        result = result*.value
        assert 'ABSTRACT' in result
        assert 'tracing' in result
        result = ReflectionCompleter.getMetaclassMethods(GroovyLexer, '', true)
        assert 'collect()' in result
        result = ReflectionCompleter.getPublicFieldsAndMethods(new GroovyLexer(new ByteArrayInputStream()), '')
        result = result*.value
        assert ! ('ABSTRACT' in result)
        assert ! ('tracing' in result)
        result = ReflectionCompleter.getMetaclassMethods(new GroovyLexer(new ByteArrayInputStream()), '', true)
        assert 'isCase(' in result
        result = ReflectionCompleter.getPublicFieldsAndMethods(GroovyLexer, 'LITERAL_as')
        result = result*.value
        assert ['LITERAL_as', 'LITERAL_assert'] == result
        // static members only shown for prefix of sufficient length
        GroovyLexer lexer = new GroovyLexer(new ByteArrayInputStream(''.bytes))
        result = ReflectionCompleter.getPublicFieldsAndMethods(lexer, 'LI')
        result = result*.value
        assert !('LITERAL_as' in result)
        result = ReflectionCompleter.getPublicFieldsAndMethods(lexer, 'LITERAL_as')
        result = result*.value
        assert ['LITERAL_as', 'LITERAL_assert'] == result
    }

    void testGetFieldsAndMethodsClass() {
        Collection<String> result = ReflectionCompleter.getPublicFieldsAndMethods(Arrays, '')
        result = result*.value
        assert 'sort(' in result
        result = ReflectionCompleter.getPublicFieldsAndMethods(HashSet, 'pro')
        result = result*.value
        assert [] == result
        result = ReflectionCompleter.getPublicFieldsAndMethods(HashSet, 'to')
        result = result*.value
        assert !('toArray(' in result)
        result = ReflectionCompleter.getPublicFieldsAndMethods(new HashSet(), 'toA')
        result = result*.value
        assert ['toArray(', 'toArray()'] == result
    }

    void testSuppressMetaAndDefaultMethods() {
        Collection<String> result = ReflectionCompleter.getMetaclassMethods('foo', '', true)
        assert 'getMetaClass()' in result
        assert 'asBoolean()' in result
        result = ReflectionCompleter.getMetaclassMethods('foo', '', false)
        result = result*.value
        assert ! ('getMetaClass()' in result)
        assert ! ('asBoolean()' in result)
    }

    void testGetFieldsAndMethodsCustomClass() {
        Interpreter interp = new Interpreter(Thread.currentThread().contextClassLoader, new Binding())
        Object instance = interp.evaluate(['class Foo extends HashSet implements Comparable {int compareTo(Object) {0}}; Foo'])
        Collection<String> result = ReflectionCompleter.getPublicFieldsAndMethods(instance, '')*.value
        assertFalse('compareTo(' in result)
        instance = interp.evaluate(['class Foo extends HashSet implements Comparable {int compareTo(Object) {0}}; new Foo()'])
        result = ReflectionCompleter.getPublicFieldsAndMethods(instance, '')*.value
        assert 'compareTo(' in result
    }
}


