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
package org.codehaus.groovy.runtime;

import java.util.*;

/**
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @author Marc Guillemot
 * @author Brad Long
 */
public class DefaultGroovyMethodsTest extends GroovyTestCase {

    public void testPrint() throws Exception {
        Map<String, String> map = new LinkedHashMap<String, String>();
        map.put("bob", "drools");
        map.put("james", "geronimo");
        List<Map<String, String>> list = new ArrayList<Map<String, String>>();
        list.add(map);
        assertEquals("[[bob:drools, james:geronimo]]", InvokerHelper.toString(list));
    }

    public void testFloatRounding() throws Exception {
        Float f = 1000.123456f;

        assertEquals(DefaultGroovyMethods.round(f), 1000);
        assertEquals(DefaultGroovyMethods.round(f, 0), 1000.0f);
        assertEquals(DefaultGroovyMethods.round(f, 1), 1000.1f);
        assertEquals(DefaultGroovyMethods.round(f, 2), 1000.12f);
        assertEquals(DefaultGroovyMethods.round(f, 3), 1000.123f);
        assertEquals(DefaultGroovyMethods.round(f, 4), 1000.1235f);
        assertEquals(DefaultGroovyMethods.round(f, 5), 1000.12346f);
        assertEquals(DefaultGroovyMethods.round(f, 6), 1000.123456f);
    }

    public void testDoubleRounding() throws Exception {
        Double d = 1000.123456;

        assertEquals(DefaultGroovyMethods.round(d), 1000L);
        assertEquals(DefaultGroovyMethods.round(d, 0), 1000.0);
        assertEquals(DefaultGroovyMethods.round(d, 1), 1000.1);
        assertEquals(DefaultGroovyMethods.round(d, 2), 1000.12);
        assertEquals(DefaultGroovyMethods.round(d, 3), 1000.123);
        assertEquals(DefaultGroovyMethods.round(d, 4), 1000.1235);
        assertEquals(DefaultGroovyMethods.round(d, 5), 1000.12346);
        assertEquals(DefaultGroovyMethods.round(d, 6), 1000.123456);
    }

    public void testBigDecimalRounding() throws Exception {
        BigDecimal bd = new BigDecimal('1000.123456')

        assertEquals(DefaultGroovyMethods.round(bd), new BigDecimal('1000'));
        assertEquals(DefaultGroovyMethods.round(bd, 0), new BigDecimal('1000.0'));
        assertEquals(DefaultGroovyMethods.round(bd, 1), new BigDecimal('1000.1'));
        assertEquals(DefaultGroovyMethods.round(bd, 2), new BigDecimal('1000.12'));
        assertEquals(DefaultGroovyMethods.round(bd, 3), new BigDecimal('1000.123'));
        assertEquals(DefaultGroovyMethods.round(bd, 4), new BigDecimal('1000.1235'));
        assertEquals(DefaultGroovyMethods.round(bd, 5), new BigDecimal('1000.12346'));
        assertEquals(DefaultGroovyMethods.round(bd, 6), new BigDecimal('1000.123456'));

        BigDecimal bd2 = new BigDecimal('-123.739')

        assertEquals(DefaultGroovyMethods.round(bd2), new BigDecimal('-124'));
        assertEquals(DefaultGroovyMethods.round(bd2, 0), new BigDecimal('-124.0'));
        assertEquals(DefaultGroovyMethods.round(bd2, 1), new BigDecimal('-123.7'));
        assertEquals(DefaultGroovyMethods.round(bd2, 2), new BigDecimal('-123.74'));
        assertEquals(DefaultGroovyMethods.round(bd2, 3), new BigDecimal('-123.739'));
    }

    public void testFloatTruncate() throws Exception {
        Float f = 1000.123456f;

        assertEquals(DefaultGroovyMethods.trunc(f), 1000.0f);
        assertEquals(DefaultGroovyMethods.trunc(f, 0), 1000.0f);
        assertEquals(DefaultGroovyMethods.trunc(f, 1), 1000.1f);
        assertEquals(DefaultGroovyMethods.trunc(f, 2), 1000.12f);
        assertEquals(DefaultGroovyMethods.trunc(f, 3), 1000.123f);
        assertEquals(DefaultGroovyMethods.trunc(f, 4), 1000.1234f);
        assertEquals(DefaultGroovyMethods.trunc(f, 5), 1000.12345f);
        assertEquals(DefaultGroovyMethods.trunc(f, 6), 1000.123456f);

        Float f2 = -123.739f

        assertEquals(DefaultGroovyMethods.trunc(f2), -123.0f)
        assertEquals(DefaultGroovyMethods.trunc(f2, 0), -123.0f)
        assertEquals(DefaultGroovyMethods.trunc(f2, 1), -123.7f)
        assertEquals(DefaultGroovyMethods.trunc(f2, 2), -123.73f)
    }

    public void testDoubleTruncate() throws Exception {
        Double d = 1000.123456;

        assertEquals(DefaultGroovyMethods.trunc(d), 1000.0);
        assertEquals(DefaultGroovyMethods.trunc(d, 0), 1000.0);
        assertEquals(DefaultGroovyMethods.trunc(d, 1), 1000.1);
        assertEquals(DefaultGroovyMethods.trunc(d, 2), 1000.12);
        assertEquals(DefaultGroovyMethods.trunc(d, 3), 1000.123);
        assertEquals(DefaultGroovyMethods.trunc(d, 4), 1000.1234);
        assertEquals(DefaultGroovyMethods.trunc(d, 5), 1000.12345);
        assertEquals(DefaultGroovyMethods.trunc(d, 6), 1000.123456);

        Double d2 = -123.739d

        assertEquals(DefaultGroovyMethods.trunc(d2), -123.0d)
        assertEquals(DefaultGroovyMethods.trunc(d2, 0), -123.0d)
        assertEquals(DefaultGroovyMethods.trunc(d2, 1), -123.7d)
        assertEquals(DefaultGroovyMethods.trunc(d2, 2), -123.73d)
    }

    public void testBigDecimalTruncate() throws Exception {
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

    public void testToMethods() throws Exception {
        Number n = 7L;
        assertEquals(DefaultGroovyMethods.toInteger(n), new Integer(7));
        assertEquals(DefaultGroovyMethods.toLong(n), new Long(7));
        assertEquals(DefaultGroovyMethods.toFloat(n), new Float(7));
        assertEquals(DefaultGroovyMethods.toDouble(n), new Double(7));
        assertEquals(DefaultGroovyMethods.toBigInteger(n), new BigInteger("7"));
        assertEquals(DefaultGroovyMethods.toBigDecimal(n), new BigDecimal("7"));
        // The following is true starting with 1.6 (GROOVY-3171):
        assertEquals(new BigDecimal("0.1"), DefaultGroovyMethods.toBigDecimal(0.1));
        assertEquals(ResourceGroovyMethods.toURL("http://example.org/"), new URL("http://example.org/"));
        assertEquals(ResourceGroovyMethods.toURI("http://example.org/"), new URI("http://example.org/"));
        assertEquals(DefaultGroovyMethods.toBoolean(Boolean.FALSE), Boolean.FALSE);
        assertEquals(DefaultGroovyMethods.toBoolean(Boolean.TRUE), Boolean.TRUE);
    }

    public void testGetBytes() {
        byte[] bytes = [42,45,47,14,10,84] as byte[]
        ByteArrayInputStream is = new ByteArrayInputStream(bytes);
        try {
            byte[] answer = IOGroovyMethods.getBytes(is);
            assertEquals(bytes.length, answer.length);
            for (int i = 0; i < bytes.length; i++) {
                assertEquals(bytes[i], answer[i]);       
            }
        } catch (IOException e) {
            fail();
        }
    }

    public void testSetBytes() {
        byte[] bytes = [42,45,47,14,10,84] as byte[]
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            IOGroovyMethods.setBytes(os, bytes);
            byte[] answer = os.toByteArray();
            assertEquals(bytes.length, answer.length);
            for (int i = 0; i < bytes.length; i++) {
                assertEquals(bytes[i], answer[i]);
            }
        } catch (IOException e) {
            fail();
        }
    }

    public void testDownto() {
        final int[] count = [0] as int[]
        final Closure closure = new Closure(null) {
            public Object doCall(final Object params) {
                count[0]++;
                return null;
            }
        };

        DefaultGroovyMethods.downto(new BigInteger("1"), new BigDecimal("0"), closure);
        assertEquals(count[0], 2);

        count[0] = 0;

        DefaultGroovyMethods.downto(new BigInteger("1"), new BigDecimal("0.123"), closure);
        assertEquals(count[0], 1);
    }

    public void testBulkCollectionForArrayOperations() {
        List<String> list = new ArrayList<String>();
        assertTrue(DefaultGroovyMethods.addAll(list, "abcd".split("")));
        assertTrue(DefaultGroovyMethods.removeAll(list, "def".split("")));
        assertTrue(DefaultGroovyMethods.retainAll(list, "bcd".split("")));
        List<String> bAndC = Arrays.asList("b", "c");
        assertTrue(DefaultGroovyMethods.containsAll(list, bAndC.toArray(new String[2])));
        assertEquals(list, bAndC);
        assertTrue(DefaultGroovyMethods.addAll(list, 1, Arrays.asList("a", "s", "i").toArray(new String[3])));
        assertEquals(list, Arrays.asList("b", "a", "s", "i", "c"));
    }

    /**
     * Tests that a List subclass without a constructor for Collections is still coerced
     * into the correct list type. 
     */
    public void testCollectionTypeConstructors() {
        MyList list = DefaultGroovyMethods.asType(Arrays.asList(1, 2, 3), MyList.class);
        assertEquals(3, list.size());
        assertEquals(1, list.get(0));
        assertEquals(2, list.get(1));
        assertEquals(3, list.get(2));
    }

    // GROOVY-7654
    public void testIterableAsList() {
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
    public void testTake() {
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
        public MyList() {}
    }

    private static class IterableWrapper implements Iterable {
        Iterable delegate

        Iterator iterator() {
            delegate.iterator()
        }
    }


    @IgnoreDefaultEqualsAndToString
    private static class CustomList extends ArrayList {
        CustomList( Object... items) {
            addAll(items)
        }

        @Override
        boolean equals(Object other) {
            return this.sum() == other.sum()
        }
    }

    @IgnoreDefaultEqualsAndToString
    private static class CustomSet extends HashSet {
        CustomSet( Object... items) {
            addAll(items)
        }

        @Override
        boolean equals(Object other) {
            return this.collect { it.toString().toUpperCase() } == other.collect { it.toString().toUpperCase() }
        }
    }

    @IgnoreDefaultEqualsAndToString
    private static class CustomMap extends HashMap {
        CustomMap(Map params) {
            this.putAll(params)
        }

        boolean equals(Object other) {
            this.values().sum() == other.values().sum()
        }
    }

    public void testCustomEqualsForList() {

        assertTrue(new CustomList(1,2,3).equals(new CustomList(3,3)))
        assertTrue(new CustomList(1,2,3) == new CustomList(3,3) )

        assertTrue(new CustomSet('a','b','c').equals(new CustomSet('A','B','C')))
        assertTrue(new CustomSet('a','b','c') == new CustomSet('A','B','C') )

        assertTrue(new CustomMap(a:10) == new CustomMap(b:3,c:7))
        assertTrue(new CustomMap(a:10).equals(new CustomMap(b:3,c:7)))
        assertFalse(new CustomMap(a:1) == new CustomMap(b:2))

    }


}
