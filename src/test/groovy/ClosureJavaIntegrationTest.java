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
package groovy;

import groovy.lang.Closure;
import groovy.lang.Reference;
import junit.framework.TestCase;
import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.codehaus.groovy.runtime.DefaultGroovyMethods.any;
import static org.codehaus.groovy.runtime.DefaultGroovyMethods.collect;
import static org.codehaus.groovy.runtime.DefaultGroovyMethods.each;
import static org.codehaus.groovy.runtime.DefaultGroovyMethods.every;
import static org.codehaus.groovy.runtime.DefaultGroovyMethods.findAll;
import static org.codehaus.groovy.runtime.DefaultGroovyMethods.inject;
import static org.codehaus.groovy.runtime.DefaultGroovyMethods.join;
import static org.codehaus.groovy.runtime.DefaultGroovyMethods.max;
import static org.codehaus.groovy.runtime.DefaultGroovyMethods.sort;

/**
 * Groovy's Closure class isn't specifically designed with Java integration in
 * mind, but these tests illustrate some of the possible ways to use them from Java.
 */
public class ClosureJavaIntegrationTest extends TestCase {
    Map<String, Integer> zoo = new LinkedHashMap<>();
    List<String> animals = Arrays.asList("ant", "bear", "camel");

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        zoo.put("Monkeys", 3);
        zoo.put("Giraffe", 2);
        zoo.put("Lions", 5);
    }

    public void testJoinListNonClosureCase() {
        assertEquals(join(animals, ", "), "ant, bear, camel");
    }

    public void testEachList() {
        final List<Integer> result = new ArrayList<>();
        each(animals, new Closure(null) {
            public void doCall(String arg) {
                result.add(arg.length());
            }
        });
        assertEquals(Arrays.asList(3, 4, 5), result);
    }

    public void testEachMap() {
        final List<String> result = new ArrayList<>();
        each(zoo, new Closure(null) {
            public void doCall(String k, Integer v) {
                result.add("k=" + k + ",v=" + v);
            }
        });
        assertEquals(Arrays.asList("k=Monkeys,v=3", "k=Giraffe,v=2", "k=Lions,v=5" ), result);
    }

    public void testCollectList() {
        assertEquals(Arrays.asList(3, 4, 5), collect(animals, new Closure<Integer>(null) {
            public Integer doCall(String it) {
                return it.length();
            }
        }));
    }

    public void testMaxMap() {
        Map.Entry<String, Integer> lionEntry = null;
        for (Map.Entry<String, Integer> entry : zoo.entrySet()) {
            if (entry.getKey().equals("Lions")) lionEntry = entry;
        }
        assertEquals(lionEntry, max(zoo.entrySet(), new Closure<Integer>(null) {
            public Integer doCall(Map.Entry<String, Integer> e) {
                return e.getKey().length() * e.getValue();
            }
        }));
    }

    public void testSortMapKeys() {
        assertEquals(Arrays.asList("Monkeys", "Lions", "Giraffe"), sort(zoo.keySet(), new Closure<Integer>(null) {
            public Integer doCall(String a, String b) {
                return -a.compareTo(b);
            }
        }));
        assertEquals(Arrays.asList("Giraffe", "Lions", "Monkeys"), sort(zoo.keySet(), new Closure<Integer>(null) {
            public Integer doCall(String a, String b) {
                return a.compareTo(b);
            }
        }));
    }

    public void testAnyMap() {
        assertTrue(any(zoo, new Closure<Boolean>(null) {
            public Boolean doCall(String k, Integer v) {
                return k.equals("Lions") && v == 5;
            }
        }));
    }

    public void testFindAllAndCurry() {
        Map<String, Integer> expected = new HashMap<>(zoo);
        expected.remove("Lions");
        Closure<Boolean> keyBiggerThan = new Closure<Boolean>(null) {
            public Boolean doCall(Map.Entry<String, Integer> e, Integer size) {
                return e.getKey().length() > size;
            }
        };
        Closure<Boolean> keyBiggerThan6 = keyBiggerThan.rcurry(6);
        assertEquals(expected, findAll(zoo, keyBiggerThan6));
    }

    public void testListArithmetic() {
        List<List> numLists = new ArrayList<>();
        numLists.add(Arrays.asList(1, 2, 3));
        numLists.add(Arrays.asList(10, 20, 30));
        assertEquals(Arrays.asList(6, 60), collect(numLists, new Closure<Integer>(null) {
            public Integer doCall(Integer a, Integer b, Integer c) {
                return a + b + c;
            }
        }));
        Closure<Integer> arithmeticClosure = new Closure<Integer>(null) {
            public Integer doCall(Integer a, Integer b, Integer c) {
                return a * b + c;
            }
        };
        Closure<Integer> tensAndUnits = arithmeticClosure.curry(10);
        assertEquals(35, (int) tensAndUnits.call(3, 5));
        tensAndUnits = arithmeticClosure.ncurry(0, 10);
        assertEquals(35, (int) tensAndUnits.call(3, 5));
        tensAndUnits = arithmeticClosure.ncurry(1, 10);
        assertEquals(35, (int) tensAndUnits.call(3, 5));
        Closure<Integer> timesPlus5 = arithmeticClosure.rcurry(5);
        assertEquals(35, (int) timesPlus5.call(15, 2));
        timesPlus5 = arithmeticClosure.ncurry(2, 5);
        assertEquals(35, (int) timesPlus5.call(15, 2));
    }

    public void testComposition() {
        Closure<String> toUpperCase = new Closure<String>(null) {
            public String doCall(String s) {
                return s.toUpperCase();
            }
        };
        Closure<Boolean> hasCapitalA = new Closure<Boolean>(null) {
            public Boolean doCall(String s) {
                return s.contains("A");
            }
        };
        Closure<Boolean> hasA = toUpperCase.rightShift(hasCapitalA);
        assertTrue(every(animals, hasA));
        Closure<Boolean> alsoHasA = hasCapitalA.leftShift(toUpperCase);
        assertTrue(every(animals, alsoHasA));
    }

    public void testTrampoline() {
        final Reference<Closure<BigInteger>> ref = new Reference<>();
        ref.set(new Closure<BigInteger>(null) {
            public Object doCall(Integer n, BigInteger total) {
                return n > 1 ? ref.get().trampoline(n - 1, total.multiply(BigInteger.valueOf(n))) : total;
            }
        }.trampoline());
        Closure<BigInteger> factorial = new Closure<BigInteger>(null) {
            public BigInteger doCall(Integer n) {
                return ref.get().call(n, BigInteger.ONE);
            }
        };
        assertEquals(BigInteger.valueOf(479001600), factorial.call(12));
    }

    public void testInject() {
        Collection<Integer> c = Arrays.asList(2, 4, 5, 20);
        Number initial = BigDecimal.ZERO;
        Closure<? extends Number> closure = new Closure<BigDecimal>(c) {
            BigDecimal doCall(BigDecimal total, Integer next) {
                return total.add(BigDecimal.ONE.divide(new BigDecimal(next)));
            }
        };
        assertTrue(DefaultTypeTransformation.compareEqual(BigDecimal.ONE, inject(c, initial, closure)));
    }
}
