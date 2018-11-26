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
import groovy.util.function.Consumer0;
import groovy.util.function.Consumer1;
import groovy.util.function.Consumer2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static groovy.lang.Tuple.collectors;
import static groovy.lang.Tuple.tuple;
import static java.util.stream.Collectors.averagingInt;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.mapping;

/**
 * @author James Strachan
 */
public class TupleTest extends GroovyTestCase {

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
        } catch (IndexOutOfBoundsException e) {
            // worked
        }
        try {
            t.get(10);
            fail("Should have thrown IndexOut");
        } catch (IndexOutOfBoundsException e) {
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

    public void testSubTuple() {
        Tuple s = t.subTuple(1, 2);

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

    public void testTuple1() {
        Tuple1<Integer> t = new Tuple1<>(1);

        assertEquals(1, t.size());

        assertEquals(new Integer(1), t.getFirst());
        assertEquals(1, t.get(0));

        assertEquals(t, t.subTuple(0, t.size()));
    }

    public void testTuple2() {
        Tuple2<Integer, Integer> t = new Tuple2<>(1, 2);

        assertEquals(2, t.size());

        assertEquals(new Integer(1), t.getFirst());
        assertEquals(1, t.get(0));

        assertEquals(new Integer(2), t.getSecond());
        assertEquals(2, t.get(1));

        assertEquals(t, t.subTuple(0, t.size()));
    }

    public void testTuple3() {
        Tuple3<Integer, Integer, Integer> t = new Tuple3<>(1, 2, 3);

        assertEquals(3, t.size());

        assertEquals(new Integer(1), t.getFirst());
        assertEquals(1, t.get(0));

        assertEquals(new Integer(2), t.getSecond());
        assertEquals(2, t.get(1));

        assertEquals(new Integer(3), t.getThird());
        assertEquals(3, t.get(2));

        assertEquals(t, t.subTuple(0, t.size()));
    }

    public void testTuple4() {
        Tuple4<Integer, Integer, Integer, Integer> t = new Tuple4<>(1, 2, 3, 4);

        assertEquals(4, t.size());

        assertEquals(new Integer(1), t.getFirst());
        assertEquals(1, t.get(0));

        assertEquals(new Integer(2), t.getSecond());
        assertEquals(2, t.get(1));

        assertEquals(new Integer(3), t.getThird());
        assertEquals(3, t.get(2));

        assertEquals(new Integer(4), t.getFourth());
        assertEquals(4, t.get(3));

        assertEquals(t, t.subTuple(0, t.size()));
    }

    public void testTuple5() {
        Tuple5<Integer, Integer, Integer, Integer, Integer> t = new Tuple5<>(1, 2, 3, 4, 5);

        assertEquals(5, t.size());

        assertEquals(new Integer(1), t.getFirst());
        assertEquals(1, t.get(0));

        assertEquals(new Integer(2), t.getSecond());
        assertEquals(2, t.get(1));

        assertEquals(new Integer(3), t.getThird());
        assertEquals(3, t.get(2));

        assertEquals(new Integer(4), t.getFourth());
        assertEquals(4, t.get(3));

        assertEquals(new Integer(5), t.getFifth());
        assertEquals(5, t.get(4));

        assertEquals(t, t.subTuple(0, t.size()));
    }

    public void testTuple6() {
        Tuple6<Integer, Integer, Integer, Integer, Integer, Integer> t = new Tuple6<>(1, 2, 3, 4, 5, 6);

        assertEquals(6, t.size());

        assertEquals(new Integer(1), t.getFirst());
        assertEquals(1, t.get(0));

        assertEquals(new Integer(2), t.getSecond());
        assertEquals(2, t.get(1));

        assertEquals(new Integer(3), t.getThird());
        assertEquals(3, t.get(2));

        assertEquals(new Integer(4), t.getFourth());
        assertEquals(4, t.get(3));

        assertEquals(new Integer(5), t.getFifth());
        assertEquals(5, t.get(4));

        assertEquals(new Integer(6), t.getSixth());
        assertEquals(6, t.get(5));

        assertEquals(t, t.subTuple(0, t.size()));
    }

    public void testTuple7() {
        Tuple7<Integer, Integer, Integer, Integer, Integer, Integer, Integer> t = new Tuple7<>(1, 2, 3, 4, 5, 6, 7);

        assertEquals(7, t.size());

        assertEquals(new Integer(1), t.getFirst());
        assertEquals(1, t.get(0));

        assertEquals(new Integer(2), t.getSecond());
        assertEquals(2, t.get(1));

        assertEquals(new Integer(3), t.getThird());
        assertEquals(3, t.get(2));

        assertEquals(new Integer(4), t.getFourth());
        assertEquals(4, t.get(3));

        assertEquals(new Integer(5), t.getFifth());
        assertEquals(5, t.get(4));

        assertEquals(new Integer(6), t.getSixth());
        assertEquals(6, t.get(5));

        assertEquals(new Integer(7), t.getSeventh());
        assertEquals(7, t.get(6));

        assertEquals(t, t.subTuple(0, t.size()));
    }

    public void testTuple8() {
        Tuple8<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer> t = new Tuple8<>(1, 2, 3, 4, 5, 6, 7, 8);

        assertEquals(8, t.size());

        assertEquals(new Integer(1), t.getFirst());
        assertEquals(1, t.get(0));

        assertEquals(new Integer(2), t.getSecond());
        assertEquals(2, t.get(1));

        assertEquals(new Integer(3), t.getThird());
        assertEquals(3, t.get(2));

        assertEquals(new Integer(4), t.getFourth());
        assertEquals(4, t.get(3));

        assertEquals(new Integer(5), t.getFifth());
        assertEquals(5, t.get(4));

        assertEquals(new Integer(6), t.getSixth());
        assertEquals(6, t.get(5));

        assertEquals(new Integer(7), t.getSeventh());
        assertEquals(7, t.get(6));

        assertEquals(new Integer(8), t.getEighth());
        assertEquals(8, t.get(7));

        assertEquals(t, t.subTuple(0, t.size()));
    }

    public void testTuple9() {
        Tuple9<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer> t = new Tuple9<>(1, 2, 3, 4, 5, 6, 7, 8, 9);

        assertEquals(9, t.size());

        assertEquals(new Integer(1), t.getFirst());
        assertEquals(1, t.get(0));

        assertEquals(new Integer(2), t.getSecond());
        assertEquals(2, t.get(1));

        assertEquals(new Integer(3), t.getThird());
        assertEquals(3, t.get(2));

        assertEquals(new Integer(4), t.getFourth());
        assertEquals(4, t.get(3));

        assertEquals(new Integer(5), t.getFifth());
        assertEquals(5, t.get(4));

        assertEquals(new Integer(6), t.getSixth());
        assertEquals(6, t.get(5));

        assertEquals(new Integer(7), t.getSeventh());
        assertEquals(7, t.get(6));

        assertEquals(new Integer(8), t.getEighth());
        assertEquals(8, t.get(7));

        assertEquals(new Integer(9), t.getNinth());
        assertEquals(9, t.get(8));

        assertEquals(t, t.subTuple(0, t.size()));
    }

    public void testEqualsHashCode() {
        Set<Tuple2<Integer, String>> set = new HashSet<>();

        set.add(tuple(1, "abc"));
        assertEquals(1, set.size());
        set.add(tuple(1, "abc"));
        assertEquals(1, set.size());
        set.add(tuple(null, null));
        assertEquals(2, set.size());
        set.add(tuple(null, null));
        assertEquals(2, set.size());
        set.add(tuple(1, null));
        assertEquals(3, set.size());
        set.add(tuple(1, null));
        assertEquals(3, set.size());
    }

    public void testEqualsNull() {
        assertFalse(tuple(1).equals(null));
        assertFalse(tuple(1, 2).equals(null));
        assertFalse(tuple(1, 2, 3).equals(null));
    }

    public void testToMap() {
        Map<Integer, Object> m = new LinkedHashMap<>();
        m.put(0, 1);
        m.put(1, "a");
        m.put(2, null);
        assertEquals(m, tuple(1, "a", null).toMap(i -> i));
    }

    public void testSwap() {
        assertEquals(tuple(1, "a"), tuple("a", 1).swap());
        assertEquals(tuple(1, "a"), tuple(1, "a").swap().swap());
    }

    public void testConcat() {
        assertEquals(tuple(1, "a"), tuple(1).concat("a"));
        assertEquals(tuple(1, "a", 2), tuple(1).concat("a").concat(2));
        assertEquals(tuple(1, "a", 2, "b"), tuple(1).concat("a").concat(2).concat("b"));
        assertEquals(tuple(1, "a", 2, "b", 3), tuple(1).concat("a").concat(2).concat("b").concat(3));
        assertEquals(tuple(1, "a", 2, "b", 3, "c"), tuple(1).concat("a").concat(2).concat("b").concat(3).concat("c"));
        assertEquals(tuple(1, "a", 2, "b", 3, "c", 4), tuple(1).concat("a").concat(2).concat("b").concat(3).concat("c").concat(4));
        assertEquals(tuple(1, "a", 2, "b", 3, "c", 4, "d"), tuple(1).concat("a").concat(2).concat("b").concat(3).concat("c").concat(4).concat("d"));
        assertEquals(tuple(1, "a", 2, "b", 3, "c", 4, "d", 5), tuple(1).concat("a").concat(2).concat("b").concat(3).concat("c").concat(4).concat("d").concat(5));
        assertEquals(tuple(1, "a", 2, "b", 3, "c", 4, "d", 5, "e"), tuple(1).concat("a").concat(2).concat("b").concat(3).concat("c").concat(4).concat("d").concat(5).concat("e"));
        assertEquals(tuple(1, "a", 2, "b", 3, "c", 4, "d", 5, "e", 6), tuple(1).concat("a").concat(2).concat("b").concat(3).concat("c").concat(4).concat("d").concat(5).concat("e").concat(6));
        assertEquals(tuple(1, "a", 2, "b", 3, "c", 4, "d", 5, "e", 6, "f"), tuple(1).concat("a").concat(2).concat("b").concat(3).concat("c").concat(4).concat("d").concat(5).concat("e").concat(6).concat("f"));
        assertEquals(tuple(1, "a", 2, "b", 3, "c", 4, "d", 5, "e", 6, "f", 7), tuple(1).concat("a").concat(2).concat("b").concat(3).concat("c").concat(4).concat("d").concat(5).concat("e").concat(6).concat("f").concat(7));
        assertEquals(tuple(1, "a", 2, "b", 3, "c", 4, "d", 5, "e", 6, "f", 7, "g"), tuple(1).concat("a").concat(2).concat("b").concat(3).concat("c").concat(4).concat("d").concat(5).concat("e").concat(6).concat("f").concat(7).concat("g"));
        assertEquals(tuple(1, "a", 2, "b", 3, "c", 4, "d", 5, "e", 6, "f", 7, "g", 8), tuple(1).concat("a").concat(2).concat("b").concat(3).concat("c").concat(4).concat("d").concat(5).concat("e").concat(6).concat("f").concat(7).concat("g").concat(8));
        assertEquals(tuple(1, "a", 2, "b", 3, "c", 4, "d", 5, "e", 6, "f", 7, "g", 8, "h"), tuple(1).concat("a").concat(2).concat("b").concat(3).concat("c").concat(4).concat("d").concat(5).concat("e").concat(6).concat("f").concat(7).concat("g").concat(8).concat("h"));

        assertEquals(tuple(1, "a"), tuple(1).concat(tuple("a")));
        assertEquals(tuple(1, "a", 2, "b", 3, "c", 4, "d"), tuple(1).concat(tuple("a", 2, "b").concat(tuple(3).concat(tuple("c", 4, "d")))));

        assertEquals(new Integer(136), tuple().concat(tuple(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16)).map((v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16) -> v1 + v2 + v3 + v4 + v5 + v6 + v7 + v8 + v9 + v10 + v11 + v12 + v13 + v14 + v15 + v16));
        assertEquals(new Integer(136), tuple(1).concat(tuple(2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16)).map((v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16) -> v1 + v2 + v3 + v4 + v5 + v6 + v7 + v8 + v9 + v10 + v11 + v12 + v13 + v14 + v15 + v16));
        assertEquals(new Integer(136), tuple(1, 2).concat(tuple(3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16)).map((v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16) -> v1 + v2 + v3 + v4 + v5 + v6 + v7 + v8 + v9 + v10 + v11 + v12 + v13 + v14 + v15 + v16));
        assertEquals(new Integer(136), tuple(1, 2, 3).concat(tuple(4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16)).map((v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16) -> v1 + v2 + v3 + v4 + v5 + v6 + v7 + v8 + v9 + v10 + v11 + v12 + v13 + v14 + v15 + v16));
        assertEquals(new Integer(136), tuple(1, 2, 3, 4).concat(tuple(5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16)).map((v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16) -> v1 + v2 + v3 + v4 + v5 + v6 + v7 + v8 + v9 + v10 + v11 + v12 + v13 + v14 + v15 + v16));
        assertEquals(new Integer(136), tuple(1, 2, 3, 4, 5).concat(tuple(6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16)).map((v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16) -> v1 + v2 + v3 + v4 + v5 + v6 + v7 + v8 + v9 + v10 + v11 + v12 + v13 + v14 + v15 + v16));
        assertEquals(new Integer(136), tuple(1, 2, 3, 4, 5, 6).concat(tuple(7, 8, 9, 10, 11, 12, 13, 14, 15, 16)).map((v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16) -> v1 + v2 + v3 + v4 + v5 + v6 + v7 + v8 + v9 + v10 + v11 + v12 + v13 + v14 + v15 + v16));
        assertEquals(new Integer(136), tuple(1, 2, 3, 4, 5, 6, 7).concat(tuple(8, 9, 10, 11, 12, 13, 14, 15, 16)).map((v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16) -> v1 + v2 + v3 + v4 + v5 + v6 + v7 + v8 + v9 + v10 + v11 + v12 + v13 + v14 + v15 + v16));
        assertEquals(new Integer(136), tuple(1, 2, 3, 4, 5, 6, 7, 8).concat(tuple(9, 10, 11, 12, 13, 14, 15, 16)).map((v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16) -> v1 + v2 + v3 + v4 + v5 + v6 + v7 + v8 + v9 + v10 + v11 + v12 + v13 + v14 + v15 + v16));
        assertEquals(new Integer(136), tuple(1, 2, 3, 4, 5, 6, 7, 8, 9).concat(tuple(10, 11, 12, 13, 14, 15, 16)).map((v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16) -> v1 + v2 + v3 + v4 + v5 + v6 + v7 + v8 + v9 + v10 + v11 + v12 + v13 + v14 + v15 + v16));
        assertEquals(new Integer(136), tuple(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).concat(tuple(11, 12, 13, 14, 15, 16)).map((v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16) -> v1 + v2 + v3 + v4 + v5 + v6 + v7 + v8 + v9 + v10 + v11 + v12 + v13 + v14 + v15 + v16));
        assertEquals(new Integer(136), tuple(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11).concat(tuple(12, 13, 14, 15, 16)).map((v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16) -> v1 + v2 + v3 + v4 + v5 + v6 + v7 + v8 + v9 + v10 + v11 + v12 + v13 + v14 + v15 + v16));
        assertEquals(new Integer(136), tuple(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12).concat(tuple(13, 14, 15, 16)).map((v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16) -> v1 + v2 + v3 + v4 + v5 + v6 + v7 + v8 + v9 + v10 + v11 + v12 + v13 + v14 + v15 + v16));
        assertEquals(new Integer(136), tuple(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13).concat(tuple(14, 15, 16)).map((v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16) -> v1 + v2 + v3 + v4 + v5 + v6 + v7 + v8 + v9 + v10 + v11 + v12 + v13 + v14 + v15 + v16));
        assertEquals(new Integer(136), tuple(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14).concat(tuple(15, 16)).map((v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16) -> v1 + v2 + v3 + v4 + v5 + v6 + v7 + v8 + v9 + v10 + v11 + v12 + v13 + v14 + v15 + v16));
        assertEquals(new Integer(136), tuple(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15).concat(tuple(16)).map((v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16) -> v1 + v2 + v3 + v4 + v5 + v6 + v7 + v8 + v9 + v10 + v11 + v12 + v13 + v14 + v15 + v16));
        assertEquals(new Integer(136), tuple(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16).concat(tuple()).map((v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16) -> v1 + v2 + v3 + v4 + v5 + v6 + v7 + v8 + v9 + v10 + v11 + v12 + v13 + v14 + v15 + v16));

        assertEquals(tuple(), tuple().concat(tuple()));
        assertEquals(tuple(1), tuple(1).concat(tuple()));
        assertEquals(tuple(1, 2), tuple(1, 2).concat(tuple()));
        assertEquals(tuple(1, 2, 3), tuple(1, 2, 3).concat(tuple()));
        assertEquals(tuple(1, 2, 3, 4), tuple(1, 2, 3, 4).concat(tuple()));
        assertEquals(tuple(1, 2, 3, 4, 5), tuple(1, 2, 3, 4, 5).concat(tuple()));
        assertEquals(tuple(1, 2, 3, 4, 5, 6), tuple(1, 2, 3, 4, 5, 6).concat(tuple()));
        assertEquals(tuple(1, 2, 3, 4, 5, 6, 7), tuple(1, 2, 3, 4, 5, 6, 7).concat(tuple()));
        assertEquals(tuple(1, 2, 3, 4, 5, 6, 7, 8), tuple(1, 2, 3, 4, 5, 6, 7, 8).concat(tuple()));
        assertEquals(tuple(1, 2, 3, 4, 5, 6, 7, 8, 9), tuple(1, 2, 3, 4, 5, 6, 7, 8, 9).concat(tuple()));
        assertEquals(tuple(1, 2, 3, 4, 5, 6, 7, 8, 9, 10), tuple(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).concat(tuple()));
        assertEquals(tuple(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11), tuple(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11).concat(tuple()));
        assertEquals(tuple(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12), tuple(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12).concat(tuple()));
        assertEquals(tuple(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13), tuple(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13).concat(tuple()));
        assertEquals(tuple(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14), tuple(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14).concat(tuple()));
        assertEquals(tuple(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15), tuple(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15).concat(tuple()));
        assertEquals(tuple(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16), tuple(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16).concat(tuple()));

    }

    public void testCompareTo() {
        Set<Tuple2<Integer, String>> set = new TreeSet<>();

        set.add(tuple(2, "a"));
        set.add(tuple(1, "b"));
        set.add(tuple(1, "a"));
        set.add(tuple(2, "a"));

        assertEquals(3, set.size());
        assertEquals(Arrays.asList(tuple(1, "a"), tuple(1, "b"), tuple(2, "a")), new ArrayList<>(set));
    }

    public void testCompareToWithNulls() {
        Set<Tuple2<Integer, String>> set = new TreeSet<>();

        set.add(tuple(2, "a"));
        set.add(tuple(1, "b"));
        set.add(tuple(1, null));
        set.add(tuple(null, "a"));
        set.add(tuple(null, "b"));
        set.add(tuple(null, null));

        assertEquals(6, set.size());
        assertEquals(Arrays.asList(tuple(1, "b"), tuple(1, null), tuple(2, "a"), tuple(null, "a"), tuple(null, "b"), tuple(null, null)), new ArrayList<>(set));
    }

    public void testIterable() {
        LinkedList<Object> list = new LinkedList<>(tuple(1, "b", null));
        for (Object o : tuple(1, "b", null)) {
            assertEquals(list.poll(), o);
        }
    }

    public void testFunctions() {
        assertEquals("[1, b, null]", tuple(1, "b", null).map((v1, v2, v3) -> tuple(v1, v2, v3).toString()));
        assertEquals("1-b", tuple(1, "b", null).map((v1, v2, v3) -> v1 + "-" + v2));
    }

    public void testMapN() {
        assertEquals(tuple(1, "a", 2, "b"), tuple(1, null, 2, null).map2(v -> "a").map4(v -> "b"));
    }

    public void testOverlaps() {
        assertTrue(Tuple2.overlaps(tuple(1, 3), tuple(1, 3)));
        assertTrue(Tuple2.overlaps(tuple(1, 3), tuple(2, 3)));
        assertTrue(Tuple2.overlaps(tuple(1, 3), tuple(2, 4)));
        assertTrue(Tuple2.overlaps(tuple(1, 3), tuple(3, 4)));
        assertFalse(Tuple2.overlaps(tuple(1, 3), tuple(4, 5)));
        assertFalse(Tuple2.overlaps(tuple(1, 1), tuple(2, 2)));
    }

    public void testIntersect() {
        assertEquals(Optional.of(tuple(2, 3)), Tuple2.intersect(tuple(1, 3), tuple(2, 4)));
        assertEquals(Optional.of(tuple(3, 3)), Tuple2.intersect(tuple(1, 3), tuple(3, 5)));
        assertEquals(Optional.empty(), Tuple2.intersect(tuple(1, 3), tuple(4, 5)));
    }

    public void testCollectors() {
        assertEquals(
                tuple(3L),
                Stream.of(1, 2, 3)
                        .collect(collectors(counting()))
        );

        assertEquals(
                tuple(3L, "1, 2, 3"),
                Stream.of(1, 2, 3)
                        .collect(collectors(
                                counting(),
                                mapping(Object::toString, joining(", "))
                        ))
        );

        assertEquals(
                tuple(3L, "1, 2, 3", 2.0),
                Stream.of(1, 2, 3)
                        .collect(collectors(
                                counting(),
                                mapping(Object::toString, joining(", ")),
                                averagingInt(Integer::intValue)
                        ))
        );
    }

    public void testLimit() {
        assertEquals(
                tuple(),
                tuple(1, "A", 2, "B").limit0()
        );
        assertEquals(
                tuple(1),
                tuple(1, "A", 2, "B").limit1()
        );
        assertEquals(
                tuple(1, "A"),
                tuple(1, "A", 2, "B").limit2()
        );
        assertEquals(
                tuple(1, "A", 2),
                tuple(1, "A", 2, "B").limit3()
        );
        assertEquals(
                tuple(1, "A", 2, "B"),
                tuple(1, "A", 2, "B").limit4()
        );
    }

    public void testSkip() {
        assertEquals(
                tuple(),
                tuple(1, "A", 2, "B").skip4()
        );
        assertEquals(
                tuple("B"),
                tuple(1, "A", 2, "B").skip3()
        );
        assertEquals(
                tuple(2, "B"),
                tuple(1, "A", 2, "B").skip2()
        );
        assertEquals(
                tuple("A", 2, "B"),
                tuple(1, "A", 2, "B").skip1()
        );
        assertEquals(
                tuple(1, "A", 2, "B"),
                tuple(1, "A", 2, "B").skip0()
        );
    }

    public void testSplit() {
        assertEquals(
                tuple(
                        tuple(),
                        tuple(1, "A", 2, "B")
                ),
                tuple(1, "A", 2, "B").split0()
        );
        assertEquals(
                tuple(
                        tuple(1),
                        tuple("A", 2, "B")
                ),
                tuple(1, "A", 2, "B").split1()
        );
        assertEquals(
                tuple(
                        tuple(1, "A"),
                        new Tuple2<>(2, "B")
                ),
                tuple(1, "A", 2, "B").split2()
        );
        assertEquals(
                tuple(
                        tuple(1, "A", 2),
                        tuple("B")
                ),
                tuple(1, "A", 2, "B").split3()
        );
        assertEquals(
                tuple(
                        tuple(1, "A", 2, "B"),
                        tuple()
                ),
                tuple(1, "A", 2, "B").split4()
        );
    }

    int result;
    public void testConsumers() {
        Consumer0 c0 = () -> { result = 1; };
        Runnable r = c0.toRunnable();
        Consumer0 c0a = Consumer0.from(r);

        result = 0;
        c0.accept();
        assertEquals(1, result);

        result = 0;
        c0.accept(Tuple.tuple());
        assertEquals(1, result);

        result = 0;
        r.run();
        assertEquals(1, result);

        result = 0;
        c0a.accept();
        assertEquals(1, result);

        Consumer1<Integer> c1 = i -> { result = i; };
        Consumer<Integer> c1a = c1.toConsumer();
        Consumer1<Integer> c1b = Consumer1.from(c1a);

        result = 0;
        c1.accept(1);
        assertEquals(1, result);

        result = 0;
        c1.accept(Tuple.tuple(1));
        assertEquals(1, result);

        result = 0;
        c1a.accept(1);
        assertEquals(1, result);

        result = 0;
        c1b.accept(1);
        assertEquals(1, result);

        Consumer2<Integer, Integer> c2 = (i, j) -> { result = i + j; };
        BiConsumer<Integer, Integer> c2a = c2.toBiConsumer();
        Consumer2<Integer, Integer> c2b = Consumer2.from(c2a);

        result = 0;
        c2.accept(1, 2);
        assertEquals(3, result);

        result = 0;
        c2.accept(Tuple.tuple(1, 2));
        assertEquals(3, result);

        result = 0;
        c2a.accept(1, 2);
        assertEquals(3, result);

        result = 0;
        c2b.accept(1, 2);
        assertEquals(3, result);
    }

    public void testGroovyStyleAccessor() {
        try {
            assertScript("def t = new Tuple1<String>('Daniel'); assert 'Daniel' == t.v1");
        } catch (Exception e) {
            assert false: e.getMessage();
        }
    }

    public void testMapAll() {
        assertEquals(tuple(), tuple().mapAll(() -> null));
        assertEquals(tuple(2), tuple(1).mapAll((v1) -> tuple(v1 + 1)));

        assertEquals(tuple(2, 3), tuple(1, 2).mapAll((v1, v2) -> tuple(v1 + 1, v2 + 1)));
        assertEquals(tuple(2, 3, 4), tuple(1, 2, 3).mapAll((v1, v2, v3) -> tuple(v1 + 1, v2 + 1, v3 + 1)));
        assertEquals(tuple(2, 3, 4, 5), tuple(1, 2, 3, 4).mapAll((v1, v2, v3, v4) -> tuple(v1 + 1, v2 + 1, v3 + 1, v4 + 1)));
        assertEquals(tuple(2, 3, 4, 5, 6), tuple(1, 2, 3, 4, 5).mapAll((v1, v2, v3, v4, v5) -> tuple(v1 + 1, v2 + 1, v3 + 1, v4 + 1, v5 + 1)));
        assertEquals(tuple(2, 3, 4, 5, 6, 7), tuple(1, 2, 3, 4, 5, 6).mapAll((v1, v2, v3, v4, v5, v6) -> tuple(v1 + 1, v2 + 1, v3 + 1, v4 + 1, v5 + 1, v6 + 1)));
        assertEquals(tuple(2, 3, 4, 5, 6, 7, 8), tuple(1, 2, 3, 4, 5, 6, 7).mapAll((v1, v2, v3, v4, v5, v6, v7) -> tuple(v1 + 1, v2 + 1, v3 + 1, v4 + 1, v5 + 1, v6 + 1, v7 + 1)));
        assertEquals(tuple(2, 3, 4, 5, 6, 7, 8, 9), tuple(1, 2, 3, 4, 5, 6, 7, 8).mapAll((v1, v2, v3, v4, v5, v6, v7, v8) -> tuple(v1 + 1, v2 + 1, v3 + 1, v4 + 1, v5 + 1, v6 + 1, v7 + 1, v8 + 1)));
        assertEquals(tuple(2, 3, 4, 5, 6, 7, 8, 9, 10), tuple(1, 2, 3, 4, 5, 6, 7, 8, 9).mapAll((v1, v2, v3, v4, v5, v6, v7, v8, v9) -> tuple(v1 + 1, v2 + 1, v3 + 1, v4 + 1, v5 + 1, v6 + 1, v7 + 1, v8 + 1, v9 + 1)));
        assertEquals(tuple(2, 3, 4, 5, 6, 7, 8, 9, 10, 11), tuple(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).mapAll((v1, v2, v3, v4, v5, v6, v7, v8, v9, v10) -> tuple(v1 + 1, v2 + 1, v3 + 1, v4 + 1, v5 + 1, v6 + 1, v7 + 1, v8 + 1, v9 + 1, v10 + 1)));
        assertEquals(tuple(2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12), tuple(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11).mapAll((v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11) -> tuple(v1 + 1, v2 + 1, v3 + 1, v4 + 1, v5 + 1, v6 + 1, v7 + 1, v8 + 1, v9 + 1, v10 + 1, v11 + 1)));
        assertEquals(tuple(2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13), tuple(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12).mapAll((v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12) -> tuple(v1 + 1, v2 + 1, v3 + 1, v4 + 1, v5 + 1, v6 + 1, v7 + 1, v8 + 1, v9 + 1, v10 + 1, v11 + 1, v12 + 1)));
        assertEquals(tuple(2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14), tuple(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13).mapAll((v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13) -> tuple(v1 + 1, v2 + 1, v3 + 1, v4 + 1, v5 + 1, v6 + 1, v7 + 1, v8 + 1, v9 + 1, v10 + 1, v11 + 1, v12 + 1, v13 + 1)));
        assertEquals(tuple(2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15), tuple(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14).mapAll((v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14) -> tuple(v1 + 1, v2 + 1, v3 + 1, v4 + 1, v5 + 1, v6 + 1, v7 + 1, v8 + 1, v9 + 1, v10 + 1, v11 + 1, v12 + 1, v13 + 1, v14 + 1)));
        assertEquals(tuple(2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16), tuple(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15).mapAll((v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15) -> tuple(v1 + 1, v2 + 1, v3 + 1, v4 + 1, v5 + 1, v6 + 1, v7 + 1, v8 + 1, v9 + 1, v10 + 1, v11 + 1, v12 + 1, v13 + 1, v14 + 1, v15 + 1)));
        assertEquals(tuple(2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17), tuple(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16).mapAll((v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16) -> tuple(v1 + 1, v2 + 1, v3 + 1, v4 + 1, v5 + 1, v6 + 1, v7 + 1, v8 + 1, v9 + 1, v10 + 1, v11 + 1, v12 + 1, v13 + 1, v14 + 1, v15 + 1, v16 + 1)));

        assertEquals(tuple(2,3), tuple(1,2).mapAll(v1 -> v1 + 1, v2 -> v2 + 1));
        assertEquals(tuple(2,3,4), tuple(1,2,3).mapAll(v1 -> v1 + 1, v2 -> v2 + 1, v3 -> v3 + 1));
        assertEquals(tuple(2,3,4,5), tuple(1,2,3,4).mapAll(v1 -> v1 + 1, v2 -> v2 + 1, v3 -> v3 + 1, v4 -> v4 + 1));
        assertEquals(tuple(2,3,4,5,6), tuple(1,2,3,4,5).mapAll(v1 -> v1 + 1, v2 -> v2 + 1, v3 -> v3 + 1, v4 -> v4 + 1, v5 -> v5 + 1));
        assertEquals(tuple(2,3,4,5,6,7), tuple(1,2,3,4,5,6).mapAll(v1 -> v1 + 1, v2 -> v2 + 1, v3 -> v3 + 1, v4 -> v4 + 1, v5 -> v5 + 1, v6 -> v6 + 1));
        assertEquals(tuple(2,3,4,5,6,7,8), tuple(1,2,3,4,5,6,7).mapAll(v1 -> v1 + 1, v2 -> v2 + 1, v3 -> v3 + 1, v4 -> v4 + 1, v5 -> v5 + 1, v6 -> v6 + 1, v7 -> v7 + 1));
        assertEquals(tuple(2,3,4,5,6,7,8,9), tuple(1,2,3,4,5,6,7,8).mapAll(v1 -> v1 + 1, v2 -> v2 + 1, v3 -> v3 + 1, v4 -> v4 + 1, v5 -> v5 + 1, v6 -> v6 + 1, v7 -> v7 + 1, v8 -> v8 + 1));
        assertEquals(tuple(2,3,4,5,6,7,8,9,10), tuple(1,2,3,4,5,6,7,8,9).mapAll(v1 -> v1 + 1, v2 -> v2 + 1, v3 -> v3 + 1, v4 -> v4 + 1, v5 -> v5 + 1, v6 -> v6 + 1, v7 -> v7 + 1, v8 -> v8 + 1, v9 -> v9 + 1));
        assertEquals(tuple(2,3,4,5,6,7,8,9,10,11), tuple(1,2,3,4,5,6,7,8,9,10).mapAll(v1 -> v1 + 1, v2 -> v2 + 1, v3 -> v3 + 1, v4 -> v4 + 1, v5 -> v5 + 1, v6 -> v6 + 1, v7 -> v7 + 1, v8 -> v8 + 1, v9 -> v9 + 1, v10 -> v10 + 1));
        assertEquals(tuple(2,3,4,5,6,7,8,9,10,11,12), tuple(1,2,3,4,5,6,7,8,9,10,11).mapAll(v1 -> v1 + 1, v2 -> v2 + 1, v3 -> v3 + 1, v4 -> v4 + 1, v5 -> v5 + 1, v6 -> v6 + 1, v7 -> v7 + 1, v8 -> v8 + 1, v9 -> v9 + 1, v10 -> v10 + 1, v11 -> v11 + 1));
        assertEquals(tuple(2,3,4,5,6,7,8,9,10,11,12,13), tuple(1,2,3,4,5,6,7,8,9,10,11,12).mapAll(v1 -> v1 + 1, v2 -> v2 + 1, v3 -> v3 + 1, v4 -> v4 + 1, v5 -> v5 + 1, v6 -> v6 + 1, v7 -> v7 + 1, v8 -> v8 + 1, v9 -> v9 + 1, v10 -> v10 + 1, v11 -> v11 + 1, v12 -> v12 + 1));
        assertEquals(tuple(2,3,4,5,6,7,8,9,10,11,12,13,14), tuple(1,2,3,4,5,6,7,8,9,10,11,12,13).mapAll(v1 -> v1 + 1, v2 -> v2 + 1, v3 -> v3 + 1, v4 -> v4 + 1, v5 -> v5 + 1, v6 -> v6 + 1, v7 -> v7 + 1, v8 -> v8 + 1, v9 -> v9 + 1, v10 -> v10 + 1, v11 -> v11 + 1, v12 -> v12 + 1, v13 -> v13 + 1));
        assertEquals(tuple(2,3,4,5,6,7,8,9,10,11,12,13,14,15), tuple(1,2,3,4,5,6,7,8,9,10,11,12,13,14).mapAll(v1 -> v1 + 1, v2 -> v2 + 1, v3 -> v3 + 1, v4 -> v4 + 1, v5 -> v5 + 1, v6 -> v6 + 1, v7 -> v7 + 1, v8 -> v8 + 1, v9 -> v9 + 1, v10 -> v10 + 1, v11 -> v11 + 1, v12 -> v12 + 1, v13 -> v13 + 1, v14 -> v14 + 1));
        assertEquals(tuple(2,3,4,5,6,7,8,9,10,11,12,13,14,15,16), tuple(1,2,3,4,5,6,7,8,9,10,11,12,13,14,15).mapAll(v1 -> v1 + 1, v2 -> v2 + 1, v3 -> v3 + 1, v4 -> v4 + 1, v5 -> v5 + 1, v6 -> v6 + 1, v7 -> v7 + 1, v8 -> v8 + 1, v9 -> v9 + 1, v10 -> v10 + 1, v11 -> v11 + 1, v12 -> v12 + 1, v13 -> v13 + 1, v14 -> v14 + 1, v15 -> v15 + 1));
        assertEquals(tuple(2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17), tuple(1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16).mapAll(v1 -> v1 + 1, v2 -> v2 + 1, v3 -> v3 + 1, v4 -> v4 + 1, v5 -> v5 + 1, v6 -> v6 + 1, v7 -> v7 + 1, v8 -> v8 + 1, v9 -> v9 + 1, v10 -> v10 + 1, v11 -> v11 + 1, v12 -> v12 + 1, v13 -> v13 + 1, v14 -> v14 + 1, v15 -> v15 + 1, v16 -> v16 + 1));

    }

    public void testMap() {
        assertEquals(tuple(2), tuple(1).map1(e -> e + 1));
        assertEquals(tuple(2, 3), tuple(1, 2).map1(e -> e + 1).map2(e -> e + 1));
        assertEquals(tuple(2, 3, 4), tuple(1, 2, 3).map1(e -> e + 1).map2(e -> e + 1).map3(e -> e + 1));
        assertEquals(tuple(2, 3, 4, 5), tuple(1, 2, 3, 4).map1(e -> e + 1).map2(e -> e + 1).map3(e -> e + 1).map4(e -> e + 1));
        assertEquals(tuple(2, 3, 4, 5, 6), tuple(1, 2, 3, 4, 5).map1(e -> e + 1).map2(e -> e + 1).map3(e -> e + 1).map4(e -> e + 1).map5(e -> e + 1));
        assertEquals(tuple(2, 3, 4, 5, 6, 7), tuple(1, 2, 3, 4, 5, 6).map1(e -> e + 1).map2(e -> e + 1).map3(e -> e + 1).map4(e -> e + 1).map5(e -> e + 1).map6(e -> e + 1));
        assertEquals(tuple(2, 3, 4, 5, 6, 7, 8), tuple(1, 2, 3, 4, 5, 6, 7).map1(e -> e + 1).map2(e -> e + 1).map3(e -> e + 1).map4(e -> e + 1).map5(e -> e + 1).map6(e -> e + 1).map7(e -> e + 1));
        assertEquals(tuple(2, 3, 4, 5, 6, 7, 8, 9), tuple(1, 2, 3, 4, 5, 6, 7, 8).map1(e -> e + 1).map2(e -> e + 1).map3(e -> e + 1).map4(e -> e + 1).map5(e -> e + 1).map6(e -> e + 1).map7(e -> e + 1).map8(e -> e + 1));
        assertEquals(tuple(2, 3, 4, 5, 6, 7, 8, 9, 10), tuple(1, 2, 3, 4, 5, 6, 7, 8, 9).map1(e -> e + 1).map2(e -> e + 1).map3(e -> e + 1).map4(e -> e + 1).map5(e -> e + 1).map6(e -> e + 1).map7(e -> e + 1).map8(e -> e + 1).map9(e -> e + 1));
        assertEquals(tuple(2, 3, 4, 5, 6, 7, 8, 9, 10, 11), tuple(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).map1(e -> e + 1).map2(e -> e + 1).map3(e -> e + 1).map4(e -> e + 1).map5(e -> e + 1).map6(e -> e + 1).map7(e -> e + 1).map8(e -> e + 1).map9(e -> e + 1).map10(e -> e + 1));
        assertEquals(tuple(2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12), tuple(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11).map1(e -> e + 1).map2(e -> e + 1).map3(e -> e + 1).map4(e -> e + 1).map5(e -> e + 1).map6(e -> e + 1).map7(e -> e + 1).map8(e -> e + 1).map9(e -> e + 1).map10(e -> e + 1).map11(e -> e + 1));
        assertEquals(tuple(2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13), tuple(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12).map1(e -> e + 1).map2(e -> e + 1).map3(e -> e + 1).map4(e -> e + 1).map5(e -> e + 1).map6(e -> e + 1).map7(e -> e + 1).map8(e -> e + 1).map9(e -> e + 1).map10(e -> e + 1).map11(e -> e + 1).map12(e -> e + 1));
        assertEquals(tuple(2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14), tuple(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13).map1(e -> e + 1).map2(e -> e + 1).map3(e -> e + 1).map4(e -> e + 1).map5(e -> e + 1).map6(e -> e + 1).map7(e -> e + 1).map8(e -> e + 1).map9(e -> e + 1).map10(e -> e + 1).map11(e -> e + 1).map12(e -> e + 1).map13(e -> e + 1));
        assertEquals(tuple(2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15), tuple(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14).map1(e -> e + 1).map2(e -> e + 1).map3(e -> e + 1).map4(e -> e + 1).map5(e -> e + 1).map6(e -> e + 1).map7(e -> e + 1).map8(e -> e + 1).map9(e -> e + 1).map10(e -> e + 1).map11(e -> e + 1).map12(e -> e + 1).map13(e -> e + 1).map14(e -> e + 1));
        assertEquals(tuple(2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16), tuple(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15).map1(e -> e + 1).map2(e -> e + 1).map3(e -> e + 1).map4(e -> e + 1).map5(e -> e + 1).map6(e -> e + 1).map7(e -> e + 1).map8(e -> e + 1).map9(e -> e + 1).map10(e -> e + 1).map11(e -> e + 1).map12(e -> e + 1).map13(e -> e + 1).map14(e -> e + 1).map15(e -> e + 1));
        assertEquals(tuple(2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17), tuple(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16).map1(e -> e + 1).map2(e -> e + 1).map3(e -> e + 1).map4(e -> e + 1).map5(e -> e + 1).map6(e -> e + 1).map7(e -> e + 1).map8(e -> e + 1).map9(e -> e + 1).map10(e -> e + 1).map11(e -> e + 1).map12(e -> e + 1).map13(e -> e + 1).map14(e -> e + 1).map15(e -> e + 1).map16(e -> e + 1));

        assertEquals(new Integer(0), tuple().map(() -> 0));
        assertEquals(new Integer(1), tuple(1).map((v1) -> v1));
        assertEquals(new Integer(3), tuple(1, 2).map((v1, v2) -> v1 + v2));
        assertEquals(new Integer(6), tuple(1, 2, 3).map((v1, v2, v3) -> v1 + v2 + v3));
        assertEquals(new Integer(10), tuple(1, 2, 3, 4).map((v1, v2, v3, v4) -> v1 + v2 + v3 + v4));
        assertEquals(new Integer(15), tuple(1, 2, 3, 4, 5).map((v1, v2, v3, v4, v5) -> v1 + v2 + v3 + v4 + v5));
        assertEquals(new Integer(21), tuple(1, 2, 3, 4, 5, 6).map((v1, v2, v3, v4, v5, v6) -> v1 + v2 + v3 + v4 + v5 + v6));
        assertEquals(new Integer(28), tuple(1, 2, 3, 4, 5, 6, 7).map((v1, v2, v3, v4, v5, v6, v7) -> v1 + v2 + v3 + v4 + v5 + v6 + v7));
        assertEquals(new Integer(36), tuple(1, 2, 3, 4, 5, 6, 7, 8).map((v1, v2, v3, v4, v5, v6, v7, v8) -> v1 + v2 + v3 + v4 + v5 + v6 + v7 + v8));
        assertEquals(new Integer(45), tuple(1, 2, 3, 4, 5, 6, 7, 8, 9).map((v1, v2, v3, v4, v5, v6, v7, v8, v9) -> v1 + v2 + v3 + v4 + v5 + v6 + v7 + v8 + v9));
        assertEquals(new Integer(55), tuple(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).map((v1, v2, v3, v4, v5, v6, v7, v8, v9, v10) -> v1 + v2 + v3 + v4 + v5 + v6 + v7 + v8 + v9 + v10));
        assertEquals(new Integer(66), tuple(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11).map((v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11) -> v1 + v2 + v3 + v4 + v5 + v6 + v7 + v8 + v9 + v10 + v11));
        assertEquals(new Integer(78), tuple(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12).map((v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12) -> v1 + v2 + v3 + v4 + v5 + v6 + v7 + v8 + v9 + v10 + v11 + v12));
        assertEquals(new Integer(91), tuple(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13).map((v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13) -> v1 + v2 + v3 + v4 + v5 + v6 + v7 + v8 + v9 + v10 + v11 + v12 + v13));
        assertEquals(new Integer(105), tuple(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14).map((v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14) -> v1 + v2 + v3 + v4 + v5 + v6 + v7 + v8 + v9 + v10 + v11 + v12 + v13 + v14));
        assertEquals(new Integer(120), tuple(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15).map((v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15) -> v1 + v2 + v3 + v4 + v5 + v6 + v7 + v8 + v9 + v10 + v11 + v12 + v13 + v14 + v15));
        assertEquals(new Integer(136), tuple(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16).map((v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16) -> v1 + v2 + v3 + v4 + v5 + v6 + v7 + v8 + v9 + v10 + v11 + v12 + v13 + v14 + v15 + v16));

    }
}
