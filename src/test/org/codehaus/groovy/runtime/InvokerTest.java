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

import groovy.lang.GString;
import groovy.lang.GroovyRuntimeException;
import groovy.test.GroovyTestCase;
import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * Test the Invoker class
 */
public class InvokerTest extends GroovyTestCase {

    public void testAsCollectionWithArray() {
        Object[] array = {"A", "B", "C"};
        assertAsCollection(array, 3);
    }

    public void testAsCollectionWithMap() {
        Map map = new HashMap();
        map.put("A", "abc");
        map.put("B", "def");
        map.put("C", "xyz");
        assertAsCollection(map, 3);
    }

    public void testAsCollectionWithList() {
        List list = new ArrayList();
        list.add("A");
        list.add("B");
        list.add("C");
        assertAsCollection(list, 3);
    }

    public void testInvokerException() throws Throwable {
        try {
            throw new GroovyRuntimeException("message", new NullPointerException());
        }
        catch (GroovyRuntimeException e) {
            // worked
            assertEquals("message", e.getMessage());
            assertTrue(e.getCause() instanceof NullPointerException);
        }
    }

    public void testAsBoolean() {
        assertAsBoolean(true, Boolean.TRUE);
        assertAsBoolean(true, "true");
        assertAsBoolean(true, "TRUE");
        assertAsBoolean(true, "false");
        assertAsBoolean(false, Boolean.FALSE);
        assertAsBoolean(false, (String) null);
        assertAsBoolean(false, "");
        GString emptyGString = new GString(new Object[]{""}, new String[]{""});
        assertAsBoolean(false, emptyGString);
        GString nonEmptyGString = new GString(new Object[]{"x"}, new String[]{"x"});
        assertAsBoolean(true, nonEmptyGString);
        assertAsBoolean(true, Integer.valueOf(1234));
        assertAsBoolean(false, Integer.valueOf(0));
        assertAsBoolean(true, new Float(0.3f));
        assertAsBoolean(true, new Double(3.0f));
        assertAsBoolean(false, new Float(0.0f));
        assertAsBoolean(true, new Character((char) 1));
        assertAsBoolean(false, new Character((char) 0));
        assertAsBoolean(false, Collections.EMPTY_LIST);
        assertAsBoolean(true, Arrays.asList(new Integer[]{Integer.valueOf(1)}));
    }

    public void testLessThan() {
        assertTrue(ScriptBytecodeAdapter.compareLessThan(Integer.valueOf(1), Integer.valueOf(2)));
        assertTrue(ScriptBytecodeAdapter.compareLessThanEqual(Integer.valueOf(2), Integer.valueOf(2)));
    }

    public void testGreaterThan() {
        assertTrue(ScriptBytecodeAdapter.compareGreaterThan(Integer.valueOf(3), Integer.valueOf(2)));
        assertTrue(ScriptBytecodeAdapter.compareGreaterThanEqual(Integer.valueOf(2), Integer.valueOf(2)));
    }

    public void testCompareTo() {
        assertTrue(DefaultTypeTransformation.compareEqual("x", Integer.valueOf('x')));
    }

    // Implementation methods
    //-------------------------------------------------------------------------

    /**
     * Asserts the asBoolean method returns the given flag
     */
    protected void assertAsBoolean(boolean expected, Object value) {
        boolean answer = DefaultTypeTransformation.castToBoolean(value);
        assertEquals("value: " + value + " asBoolean()", expected, answer);
    }

    /**
     * Asserts that the given object can be converted into a collection and iterator
     * of the given size
     */
    protected void assertAsCollection(Object collectionObject, int count) {
        Collection collection = DefaultTypeTransformation.asCollection(collectionObject);
        assertTrue("Collection is not null", collection != null);
        assertEquals("Collection size", count, collection.size());

        assertIterator("collections iterator", collection.iterator(), count);
        assertIterator("InvokerHelper.asIterator", InvokerHelper.asIterator(collectionObject), count);
        assertIterator("InvokerHelper.asIterator(InvokerHelper.asCollection)", InvokerHelper.asIterator(collection), count);
        assertIterator("InvokerHelper.asIterator(InvokerHelper.asIterator)", InvokerHelper.asIterator(InvokerHelper.asIterator(collectionObject)), count);
    }

    /**
     * Asserts that the iterator is valid and of the right size
     */
    protected void assertIterator(String message, Iterator iterator, int count) {
        for (int i = 0; i < count; i++) {
            assertTrue(message + ": should have item: " + i, iterator.hasNext());
            assertTrue(message + ": item: " + i + " should not be null", iterator.next() != null);
        }

        assertFalse(
                message + ": should not have item after iterating through: " + count + " items",
                iterator.hasNext());
    }


}
