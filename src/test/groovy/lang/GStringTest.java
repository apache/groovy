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

import groovy.test.GroovyTestCase;
import org.codehaus.groovy.runtime.InvokerHelper;

/**
 * Tests the use of the structured Attribute type
 */
public class GStringTest extends GroovyTestCase {

    public void testIterateOverText() {
        GString compString = new GString(new Object[]{"James"}, new String[]{"Hello ", "!"});
        assertArrayEquals(new String[]{"Hello ", "!"}, compString.getStrings());
        assertArrayEquals(new Object[]{"James"}, compString.getValues());
        assertEquals("Hello James!", compString.toString());
    }

    public void testAppendString() {
        GString a = new GString(new Object[]{"James"}, new String[]{"Hello ", "!"});
        GString result = a.plus(" how are you?");
        assertEquals("Hello James! how are you?", result.toString());
        assertEquals('J', a.charAt(6));
        assertEquals("o J", a.subSequence(4, 7));
    }

    public void testAppendString2() {
        GString a = new GString(new Object[]{"James"}, new String[]{"Hello "});
        GString result = a.plus(" how are you?");
        System.out.println("Strings: " + InvokerHelper.toString(result.getStrings()));
        System.out.println("Values: " + InvokerHelper.toString(result.getValues()));
        assertEquals("Hello James how are you?", result.toString());
    }

    public void testAppendGString() {
        GString a = new GString(new Object[]{"James"}, new String[]{"Hello ", "!"});
        GString b = new GString(new Object[]{"Bob"}, new String[]{"Hello ", "!"});
        GString result = a.plus(b);
        assertEquals("Hello James!Hello Bob!", result.toString());
    }

    public void testAppendGString2() {
        GString a = new GString(new Object[]{"James"}, new String[]{"Hello "});
        GString b = new GString(new Object[]{"Bob"}, new String[]{"Hello "});
        GString result = a.plus(b);
        assertEquals("Hello JamesHello Bob", result.toString());
    }

    public void testEqualsAndHashCode() {
        GString a = new GString(new Object[]{Integer.valueOf(1)}, new String[]{"Hello ", "!"});
        GString b = new GString(new Object[]{Long.valueOf(1)}, new String[]{"Hello ", "!"});
        Comparable c = new GString(new Object[]{new Double(2.3)}, new String[]{"Hello ", "!"});

        assertTrue("a == b", a.equals(b));
        assertEquals("hashcode a == b", a.hashCode(), b.hashCode());
        assertFalse("a != c", a.equals(c));
        assertTrue("hashcode a != c", a.hashCode() != c.hashCode());
        assertEquals("a <=> b", 0, a.compareTo(b));
        assertEquals("a <=> b", -1, a.compareTo(c));
    }
}
