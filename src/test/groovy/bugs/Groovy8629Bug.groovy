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
package groovy.bugs

import groovy.test.GroovyTestCase

public class Groovy8629Bug extends GroovyTestCase {
    void testNestedMethodCallInConstructor() {
        assertScript '''
        import groovy.transform.CompileStatic
        
        /**
         * A utility class for comparing two maps
         */
        @CompileStatic
        class MapComparison implements Iterable<IntegerPair> {
            Map<String, Integer> m1
            Map<String, Integer> m2
            Set<String> unionKeys = null
        
            MapComparison(Map<String, Integer> map1, Map<String, Integer> map2) {
                this.m1 = map1
                this.m2 = map2
            }
        
            @Override
            Iterator<IntegerPair> iterator() {
                if (unionKeys == null) {
                    unionKeys = m1.keySet() + m2.keySet()
                }
                return new IntegerPairIterator(unionKeys.iterator())
            }
        
            class IntegerPairIterator implements Iterator<IntegerPair> {
                private Iterator<String> keyIterator
        
                IntegerPairIterator(Iterator<String> keyIterator) {
                    this.keyIterator = keyIterator
                }
        
                @Override
                boolean hasNext() {
                    return keyIterator.hasNext()
                }
        
                @Override
                IntegerPair next() {
                    String key = keyIterator.next()
                    IntegerPair comp = new IntegerPair(m1[key], m2[key])
                    return comp
                }
        
                @Override
                void remove() {
                    throw new UnsupportedOperationException()
                }
            }
        
            static class IntegerPair  {
                Integer i1;
                Integer i2;
        
                IntegerPair(Integer int1, Integer int2) {
                    i1 = int1;
                    i2 = int2;
                }
            }
        }
        
        def mc = new MapComparison([:],[:])

        '''
    }

}
