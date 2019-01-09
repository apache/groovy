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
package groovy.lang;

import groovy.util.GroovyTestCase;
import org.codehaus.groovy.runtime.InvokerHelper;

/**
 * Tests the use of the structured Attribute type
 */
public class GStringTest extends GroovyTestCase {

    public void testIterateOverText() {
        DummyGString compString = new DummyGString(new Object[]{"James"});
        assertArrayEquals(new String[]{"Hello ", "!"}, compString.getStrings());
        assertArrayEquals(new Object[]{"James"}, compString.getValues());
        assertEquals("Hello James!", compString.toString());
    }

    public void testAppendString() {
        DummyGString a = new DummyGString(new Object[]{"James"});
        GString result = a.plus(" how are you?");
        assertEquals("Hello James! how are you?", result.toString());
        assertEquals('J', a.charAt(6));
        assertEquals("o J", a.subSequence(4, 7));
    }

    public void testAppendString2() {
        DummyGString a = new DummyGString(new Object[]{"James"}, new String[]{"Hello "});
        GString result = a.plus(" how are you?");
        System.out.println("Strings: " + InvokerHelper.toString(result.getStrings()));
        System.out.println("Values: " + InvokerHelper.toString(result.getValues()));
        assertEquals("Hello James how are you?", result.toString());
    }

    public void testAppendGString() {
        DummyGString a = new DummyGString(new Object[]{"James"});
        DummyGString b = new DummyGString(new Object[]{"Bob"});
        GString result = a.plus(b);
        assertEquals("Hello James!Hello Bob!", result.toString());
    }

    public void testAppendGString2() {
        DummyGString a = new DummyGString(new Object[]{"James"}, new String[]{"Hello "});
        DummyGString b = new DummyGString(new Object[]{"Bob"}, new String[]{"Hello "});
        GString result = a.plus(b);
        assertEquals("Hello JamesHello Bob", result.toString());
    }

    public void testEqualsAndHashCode() {
        DummyGString a = new DummyGString(new Object[]{new Integer(1)});
        DummyGString b = new DummyGString(new Object[]{new Long(1)});
        Comparable c = new DummyGString(new Object[]{new Double(2.3)});

        assertTrue("a == b", a.equals(b));
        assertEquals("hashcode a == b", a.hashCode(), b.hashCode());
        assertFalse("a != c", a.equals(c));
        assertTrue("hashcode a != c", a.hashCode() != c.hashCode());
        assertEquals("a <=> b", 0, a.compareTo(b));
        assertEquals("a <=> b", -1, a.compareTo(c));
    }
}
