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
import groovy.transform.CompileStatic

@CompileStatic
final class Groovy8609Bug extends GroovyTestCase {

    void testUpperBoundWithGenerics() {
        assertScript '''
        @groovy.transform.CompileStatic
        public class A<T extends List<E>, E extends Map<String, Integer>> {
            E getFirstRecord(T recordList) {
                return recordList.get(0)
            }
            
            static void main(args) {
                def list = new ArrayList<HashMap<String, Integer>>()
                def record = new HashMap<String, Integer>()
                list.add(record)
                def a = new A<ArrayList<HashMap<String, Integer>>, HashMap<String, Integer>>()
                assert record.is(a.getFirstRecord(list))
            }
        }
        '''
    }

    void testUpperBoundWithoutGenerics() {
        assertScript '''
        @groovy.transform.CompileStatic
        public class A<T extends List<E>, E extends Map> {
            E getFirstRecord(T recordList) {
                return recordList.get(0);
            }
            
            static void main(args) {
                def list = new ArrayList<HashMap<String, Integer>>()
                def record = new HashMap<String, Integer>()
                list.add(record)
                def a = new A<ArrayList<HashMap<String, Integer>>, HashMap<String, Integer>>()
                assert record.is(a.getFirstRecord(list))
            }
        }
        '''
    }

    void testNoUpperBound() {
        assertScript '''
        @groovy.transform.CompileStatic
        public class A<T extends List<E>, E> {
            E getFirstRecord(T recordList) {
                return recordList.get(0);
            }
            
            static void main(args) {
                def list = new ArrayList<HashMap<String, Integer>>()
                def record = new HashMap<String, Integer>()
                list.add(record)
                def a = new A<ArrayList<HashMap<String, Integer>>, HashMap<String, Integer>>()
                assert record.is(a.getFirstRecord(list))
            }
        }
        '''
    }

    void testUpperBoundWithGenericsThroughWrongType() {
        def errMsg = shouldFail '''
        @groovy.transform.CompileStatic
        public class A<T extends List<E>, E extends Map<String, Integer>> {
            E getFirstRecord(T recordList) {
                return recordList.get(0)
            }
            
            static void main(args) {
                def list = new ArrayList<TreeMap<String, Integer>>()
                def record = new TreeMap<String, Integer>()
                list.add(record)
                def a = new A<ArrayList<HashMap<String, Integer>>, HashMap<String, Integer>>()
                assert record.is(a.getFirstRecord(list))
            }
        }
        '''

        assert errMsg.contains('[Static type checking] - Cannot find matching method A#getFirstRecord(java.util.ArrayList <TreeMap>)')
    }

    void testUpperBoundWithGenericsThroughWrongType2() {
        def errMsg = shouldFail '''
        @groovy.transform.CompileStatic
        public class A<T extends List<E>, E extends Map<String, Integer>> {
            E getFirstRecord(T recordList) {
                return recordList.get(0)
            }
            
            static void main(args) {
                def list = new ArrayList<HashMap<String, Long>>()
                def record = new HashMap<String, Long>()
                list.add(record)
                def a = new A<ArrayList<HashMap<String, Integer>>, HashMap<String, Integer>>()
                assert record.is(a.getFirstRecord(list))
            }
        }
        '''

        assert errMsg.contains('[Static type checking] - Cannot find matching method A#getFirstRecord(java.util.ArrayList <HashMap>)')
    }

    void testUpperBoundWithGenericsThroughWrongType3() {
        def errMsg = shouldFail '''
        @groovy.transform.CompileStatic
        public class A<T extends List<E>, E extends Map<String, Integer>> {
            E getFirstRecord(T recordList) {
                return recordList.get(0)
            }
            
            static void main(args) {
                def list = new ArrayList<HashMap<StringBuffer, Integer>>()
                def record = new HashMap<StringBuffer, Integer>()
                list.add(record)
                def a = new A<ArrayList<HashMap<String, Integer>>, HashMap<String, Integer>>()
                assert record.is(a.getFirstRecord(list))
            }
        }
        '''

        assert errMsg.contains('[Static type checking] - Cannot find matching method A#getFirstRecord(java.util.ArrayList <HashMap>)')
    }
}
