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
package org.codehaus.groovy.runtime;

import groovy.lang.Closure;
import groovy.util.GroovyTestCase;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
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
        byte[] bytes = {42,45,47,14,10,84};
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
        byte[] bytes = {42,45,47,14,10,84};
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
        final int[] count = new int[]{0};
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

    private static class MyList extends ArrayList {
        public MyList() {}
    }
}
