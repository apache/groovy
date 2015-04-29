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

import junit.framework.TestCase;

import java.util.List;

/**
 * @author James Strachan
 */
public class TupleTest extends TestCase {

    final Object[] data = {"a", "b", "c"};
    final Tuple t = new Tuple(data);

    public void testSize() {
        assertEquals("Size of " + t, 3, t.size());

        assertEquals("get(0)", "a", t.get(0));
        assertEquals("get(1)", "b", t.get(1));
    }

    public void testGetOutOfTuple() {
        try {
            t.get(-1);
            fail("Should have thrown IndexOut");
        }
        catch (IndexOutOfBoundsException e) {
            // worked
        }
        try {
            t.get(10);
            fail("Should have thrown IndexOut");
        }
        catch (IndexOutOfBoundsException e) {
            // worked
        }

    }

    public void testContains() {
        assertTrue("contains a", t.contains("a"));
        assertTrue("contains b", t.contains("b"));
    }

    public void testSubList() {
        List s = t.subList(1, 2);

        assertTrue("is a Tuple", s instanceof Tuple);

        assertEquals("size", 1, s.size());
    }

    public void testHashCodeAndEquals() {
        Tuple a = new Tuple(new Object[]{"a", "b", "c"});
        Tuple b = new Tuple(new Object[]{"a", "b", "c"});
        Tuple c = new Tuple(new Object[]{"d", "b", "c"});
        Tuple d = new Tuple(new Object[]{"a", "b"});
        Tuple2<String, String> e = new Tuple2<String, String>("a", "b");
        Tuple2<String, String> f = new Tuple2<String, String>("a", "c");

        assertEquals("hashcode", a.hashCode(), b.hashCode());
        assertTrue("hashcode", a.hashCode() != c.hashCode());

        assertEquals("a and b", a, b);
        assertFalse("a != c", a.equals(c));

        assertFalse("!a.equals(null)", a.equals(null));

        assertTrue("d.equals(e)", d.equals(e));
        assertTrue("e.equals(d)", e.equals(d));
        assertFalse("!e.equals(f)", e.equals(f));
        assertFalse("!f.equals(e)", f.equals(e));
    }

    public void testIterator() {
    }

}
