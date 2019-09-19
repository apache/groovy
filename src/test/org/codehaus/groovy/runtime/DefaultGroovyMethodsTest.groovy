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
package org.codehaus.groovy.runtime

import groovy.test.GroovyTestCase

/**
 * Tests for DGM methods
 */
class DefaultGroovyMethodsTest extends GroovyTestCase {

    void testPrint() throws Exception {
        Map<String, String> map = ['bob': 'drools', 'james': 'geronimo']
        List<Map<String, String>> list = [map]
        assertEquals("[[bob:drools, james:geronimo]]", InvokerHelper.toString(list))
    }

    void testFloatRounding() throws Exception {
        Float f = 1000.123456f

        assertEquals(DefaultGroovyMethods.round(f), 1000)
        assertEquals(DefaultGroovyMethods.round(f, 0), 1000.0f)
        assertEquals(DefaultGroovyMethods.round(f, 1), 1000.1f)
        assertEquals(DefaultGroovyMethods.round(f, 2), 1000.12f)
        assertEquals(DefaultGroovyMethods.round(f, 3), 1000.123f)
        assertEquals(DefaultGroovyMethods.round(f, 4), 1000.1235f)
        assertEquals(DefaultGroovyMethods.round(f, 5), 1000.12346f)
        assertEquals(DefaultGroovyMethods.round(f, 6), 1000.123456f)
    }

    void testDoubleRounding() throws Exception {
        Double d = 1000.123456

        assertEquals(DefaultGroovyMethods.round(d), 1000L)
        assertEquals(DefaultGroovyMethods.round(d, 0), 1000.0)
        assertEquals(DefaultGroovyMethods.round(d, 1), 1000.1)
        assertEquals(DefaultGroovyMethods.round(d, 2), 1000.12)
        assertEquals(DefaultGroovyMethods.round(d, 3), 1000.123)
        assertEquals(DefaultGroovyMethods.round(d, 4), 1000.1235)
        assertEquals(DefaultGroovyMethods.round(d, 5), 1000.12346)
        assertEquals(DefaultGroovyMethods.round(d, 6), 1000.123456)
    }

    void testBigDecimalRounding() throws Exception {
        BigDecimal bd = new BigDecimal('1000.123456')

        assertEquals(DefaultGroovyMethods.round(bd), new BigDecimal('1000'))
        assertEquals(DefaultGroovyMethods.round(bd, 0), new BigDecimal('1000.0'))
        assertEquals(DefaultGroovyMethods.round(bd, 1), new BigDecimal('1000.1'))
        assertEquals(DefaultGroovyMethods.round(bd, 2), new BigDecimal('1000.12'))
        assertEquals(DefaultGroovyMethods.round(bd, 3), new BigDecimal('1000.123'))
        assertEquals(DefaultGroovyMethods.round(bd, 4), new BigDecimal('1000.1235'))
        assertEquals(DefaultGroovyMethods.round(bd, 5), new BigDecimal('1000.12346'))
        assertEquals(DefaultGroovyMethods.round(bd, 6), new BigDecimal('1000.123456'))

        BigDecimal bd2 = new BigDecimal('-123.739')

        assertEquals(DefaultGroovyMethods.round(bd2), new BigDecimal('-124'))
        assertEquals(DefaultGroovyMethods.round(bd2, 0), new BigDecimal('-124.0'))
        assertEquals(DefaultGroovyMethods.round(bd2, 1), new BigDecimal('-123.7'))
        assertEquals(DefaultGroovyMethods.round(bd2, 2), new BigDecimal('-123.74'))
        assertEquals(DefaultGroovyMethods.round(bd2, 3), new BigDecimal('-123.739'))
    }

    void testFloatTruncate() throws Exception {
        Float f = 1000.123456f

        assertEquals(DefaultGroovyMethods.trunc(f), 1000.0f)
        assertEquals(DefaultGroovyMethods.trunc(f, 0), 1000.0f)
        assertEquals(DefaultGroovyMethods.trunc(f, 1), 1000.1f)
        assertEquals(DefaultGroovyMethods.trunc(f, 2), 1000.12f)
        assertEquals(DefaultGroovyMethods.trunc(f, 3), 1000.123f)
        assertEquals(DefaultGroovyMethods.trunc(f, 4), 1000.1234f)
        assertEquals(DefaultGroovyMethods.trunc(f, 5), 1000.12345f)
        assertEquals(DefaultGroovyMethods.trunc(f, 6), 1000.123456f)

        Float f2 = -123.739f

        assertEquals(DefaultGroovyMethods.trunc(f2), -123.0f)
        assertEquals(DefaultGroovyMethods.trunc(f2, 0), -123.0f)
        assertEquals(DefaultGroovyMethods.trunc(f2, 1), -123.7f)
        assertEquals(DefaultGroovyMethods.trunc(f2, 2), -123.73f)
    }

    void testDoubleTruncate() throws Exception {
        Double d = 1000.123456

        assertEquals(DefaultGroovyMethods.trunc(d), 1000.0)
        assertEquals(DefaultGroovyMethods.trunc(d, 0), 1000.0)
        assertEquals(DefaultGroovyMethods.trunc(d, 1), 1000.1)
        assertEquals(DefaultGroovyMethods.trunc(d, 2), 1000.12)
        assertEquals(DefaultGroovyMethods.trunc(d, 3), 1000.123)
        assertEquals(DefaultGroovyMethods.trunc(d, 4), 1000.1234)
        assertEquals(DefaultGroovyMethods.trunc(d, 5), 1000.12345)
        assertEquals(DefaultGroovyMethods.trunc(d, 6), 1000.123456)

        Double d2 = -123.739d

        assertEquals(DefaultGroovyMethods.trunc(d2), -123.0d)
        assertEquals(DefaultGroovyMethods.trunc(d2, 0), -123.0d)
        assertEquals(DefaultGroovyMethods.trunc(d2, 1), -123.7d)
        assertEquals(DefaultGroovyMethods.trunc(d2, 2), -123.73d)
    }

    void testBigDecimalTruncate() throws Exception {
        BigDecimal bd = new BigDecimal('1000.123456')

        assertEquals(DefaultGroovyMethods.trunc(bd), new BigDecimal('1000.0'))
        assertEquals(DefaultGroovyMethods.trunc(bd, 0), new BigDecimal('1000.0'))
        assertEquals(DefaultGroovyMethods.trunc(bd, 1), new BigDecimal('1000.1'))
        assertEquals(DefaultGroovyMethods.trunc(bd, 2), new BigDecimal('1000.12'))
        assertEquals(DefaultGroovyMethods.trunc(bd, 3), new BigDecimal('1000.123'))
        assertEquals(DefaultGroovyMethods.trunc(bd, 4), new BigDecimal('1000.1234'))
        assertEquals(DefaultGroovyMethods.trunc(bd, 5), new BigDecimal('1000.12345'))
        assertEquals(DefaultGroovyMethods.trunc(bd, 6), new BigDecimal('1000.123456'))

        BigDecimal bd2 = new BigDecimal('-123.739')

        assertEquals(DefaultGroovyMethods.trunc(bd2), new BigDecimal('-123.0'))
        assertEquals(DefaultGroovyMethods.trunc(bd2, 0), new BigDecimal('-123.0'))
        assertEquals(DefaultGroovyMethods.trunc(bd2, 1), new BigDecimal('-123.7'))
        assertEquals(DefaultGroovyMethods.trunc(bd2, 2), new BigDecimal('-123.73'))
    }

    // GROOVY-6626
    void testBigIntegerPower() {
        assert DefaultGroovyMethods.power(2G, 63G) == DefaultGroovyMethods.power(2G, 63)
        assert DefaultGroovyMethods.power(13G, 15G) == DefaultGroovyMethods.power(13G, 15)
    }

    void testToMethods() throws Exception {
        Number n = 7L
        assertEquals(DefaultGroovyMethods.toInteger(n), new Integer(7))
        assertEquals(DefaultGroovyMethods.toLong(n), new Long(7))
        assertEquals(DefaultGroovyMethods.toFloat(n), new Float(7))
        assertEquals(DefaultGroovyMethods.toDouble(n), new Double(7))
        assertEquals(DefaultGroovyMethods.toBigInteger(n), new BigInteger("7"))
        assertEquals(DefaultGroovyMethods.toBigDecimal(n), new BigDecimal("7"))
        // The following is true starting with 1.6 (GROOVY-3171):
        assertEquals(new BigDecimal("0.1"), DefaultGroovyMethods.toBigDecimal(0.1))
        assertEquals(ResourceGroovyMethods.toURL("http://example.org/"), new URL("http://example.org/"))
        assertEquals(ResourceGroovyMethods.toURI("http://example.org/"), new URI("http://example.org/"))
        assertEquals(DefaultGroovyMethods.toBoolean(Boolean.FALSE), Boolean.FALSE)
        assertEquals(DefaultGroovyMethods.toBoolean(Boolean.TRUE), Boolean.TRUE)
    }

    void testGetBytes() {
        byte[] bytes = [42,45,47,14,10,84] as byte[]
        ByteArrayInputStream is = new ByteArrayInputStream(bytes)
        try {
            byte[] answer = IOGroovyMethods.getBytes(is)
            assert bytes.length == answer.length
            (0..<bytes.length).each{
                assert answer[it] == answer[it]
            }
        } catch (IOException e) {
            fail()
        }
    }

    void testSetBytes() {
        byte[] bytes = [42,45,47,14,10,84] as byte[]
        ByteArrayOutputStream os = new ByteArrayOutputStream()
        try {
            IOGroovyMethods.setBytes(os, bytes)
            byte[] answer = os.toByteArray()
            assert bytes.length == answer.length
            (0..<bytes.length).each{
                assert answer[it] == answer[it]
            }
        } catch (IOException e) {
            fail()
        }
    }

    void testDownto() {
        final int[] count = [0] as int[]
        final Closure closure = new Closure(null) {
            Object doCall(final Object params) {
                count[0]++
                return null
            }
        }

        DefaultGroovyMethods.downto(new BigInteger("1"), new BigDecimal("0"), closure)
        assertEquals(count[0], 2)

        count[0] = 0

        DefaultGroovyMethods.downto(new BigInteger("1"), new BigDecimal("0.123"), closure)
        assertEquals(count[0], 1)
    }

    void testBulkCollectionForArrayOperations() {
        List<String> list = []
        assertTrue(DefaultGroovyMethods.addAll(list, "abcd".split("")))
        assertTrue(DefaultGroovyMethods.removeAll(list, "def".split("")))
        assertTrue(DefaultGroovyMethods.retainAll(list, "bcd".split("")))
        List<String> bAndC = ['b', 'c']
        assertTrue(DefaultGroovyMethods.containsAll((Iterable)list, bAndC.toArray(new String[2])))
        assertEquals(list, bAndC)
        assertTrue(DefaultGroovyMethods.addAll(list, 1, ['a', 's', 'i'].toArray(new String[3])))
        assertEquals(list, ['b', 'a', 's', 'i', 'c'])
    }

    /**
     * Tests that a List subclass without a constructor for Collections is still coerced
     * into the correct list type. 
     */
    void testCollectionTypeConstructors() {
        MyList list = DefaultGroovyMethods.asType([1, 2, 3], MyList.class)
        assertEquals(3, list.size())
        assertEquals(1, list.get(0))
        assertEquals(2, list.get(1))
        assertEquals(3, list.get(2))
    }

    // GROOVY-7654
    void testIterableAsList() {
        def list = [1, 2, 3]
        def iterable = new IterableWrapper(delegate: list)

        def iterableAsIterable = iterable as Iterable
        assertTrue(iterableAsIterable.is(iterable))

        def iterableAsIterableWrapper = iterable as IterableWrapper
        assertTrue(iterableAsIterableWrapper.is(iterable))

        def iterableAsList = iterable.asList()
        def iterableAsType = iterable as List

        assertEquals(iterableAsList, iterableAsType)
        assertEquals(1, iterableAsList[0])
        assertEquals(1, iterableAsType[0])
    }

    // GROOVY-8271
    void testTake() {
        int hasNextCount = 0
        int nextCount = 0
        def iterator = [
            hasNext: { -> hasNextCount++; true },
            next: { -> nextCount++ }
        ] as Iterator<Integer>
        iterator.take(3).toList()
        assertEquals(3, hasNextCount)
        assertEquals(3, nextCount)
    }

    private static class MyList extends ArrayList {
        MyList() {}
    }

    private static class IterableWrapper implements Iterable {
        Iterable delegate

        Iterator iterator() {
            delegate.iterator()
        }
    }

    void testBooleanOr() {
        assertTrue(DefaultGroovyMethods.or(true, true))
        assertTrue(DefaultGroovyMethods.or(true, false))
        assertTrue(DefaultGroovyMethods.or(false, true))
        assertFalse(DefaultGroovyMethods.or(false, false))
        assertFalse(DefaultGroovyMethods.or(false, null))
        assertTrue(DefaultGroovyMethods.or(true, null))
    }

    void testBooleanAnd() {
        assertTrue(DefaultGroovyMethods.and(true, true))
        assertFalse(DefaultGroovyMethods.and(true, false))
        assertFalse(DefaultGroovyMethods.and(false, true))
        assertFalse(DefaultGroovyMethods.and(false, false))
        assertFalse(DefaultGroovyMethods.and(false, null))
        assertFalse(DefaultGroovyMethods.and(true, null))
    }

    void testBooleanXor() {
        assertFalse(DefaultGroovyMethods.xor(true, true))
        assertTrue(DefaultGroovyMethods.xor(true, false))
        assertTrue(DefaultGroovyMethods.xor(false, true))
        assertFalse(DefaultGroovyMethods.xor(false, false))
        assertFalse(DefaultGroovyMethods.xor(false, null))
        assertTrue(DefaultGroovyMethods.xor(true, null))
    }

    void testBooleanImplication() {
        assertTrue(DefaultGroovyMethods.implies(true, true))
        assertFalse(DefaultGroovyMethods.implies(true, false))
        assertTrue(DefaultGroovyMethods.implies(false, true))
        assertTrue(DefaultGroovyMethods.implies(false, false))
        assertTrue(DefaultGroovyMethods.implies(false, null))
        assertFalse(DefaultGroovyMethods.implies(true, null))
    }

    void testWhichJar() {
        assert DefaultGroovyMethods.getLocation(org.objectweb.asm.Opcodes).getFile().matches(/(.+\/)?asm[-].+\.jar/)
        assert null == DefaultGroovyMethods.getLocation(String)
    }

    void testThrowableAsString() {
        def result = new Exception("this is an exception").asString()

        assert result.contains("this is an exception")
        assert result.contains("at org.codehaus.groovy.runtime.DefaultGroovyMethodsTest.testThrowableAsString(DefaultGroovyMethodsTest.groovy:")
    }
}
