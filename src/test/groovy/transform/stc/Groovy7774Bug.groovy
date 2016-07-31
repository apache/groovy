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

package groovy.transform.stc

class Groovy7774Bug extends StaticTypeCheckingTestCase {
    void testCollectionAddAllShouldHonorInheritance() {
        assertScript '''
            class X{}
            class Y extends X{}

            def create() {
                Set<X> set = new HashSet<X>()
                List<Y> addIterable = [new Y()]
                Iterator<Y> addIterator = [new Y()].iterator()
                Y[] addArray = [new Y()]
                set.addAll(addIterable)
                set.addAll(addIterator)
                set.addAll(addArray)
                assert set.size() == 3
            }

            create()
        '''
    }

    void testMapPutAllShouldHonorInheritance() {
        assertScript '''
            class X{}
            class Y extends X{}

            def create() {
                Map<X, X> items = new HashMap<X, X>()
                List<Map.Entry<Y, Y>> addEntries = new ArrayList<Map.Entry<Y, Y>>()
                items.putAll(addEntries)
            }
            create()
        '''
    }
}
