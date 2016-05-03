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
package org.codehaus.groovy.util

class ManagedDoubleKeyMapTest extends GroovyTestCase {

    ManagedDoubleKeyMap<Object, Object, String> map =
            new ManagedDoubleKeyMap<Object, Object, String>(ReferenceBundle.getHardBundle())

    void testEntriesRemoveSelfFromMapWhenFinalized() {
        def entries = []
        for (int i = 0; i < 5; i++) {
            entries << map.getOrPut(new Object(), new Object(), "Value${i}")
        }

        assert map.size() == 5
        assert map.fullSize() == 5

        entries*.clean()

        assert map.size() == 0
        assert map.fullSize() == 0
    }

    void testPutForSameHashBucket() {
        Object obj1 = new Object() { @Override public int hashCode() { return 42; } }
        Object obj2 = new Object() { @Override public int hashCode() { return 42; } }
        Object obj3 = new Object() { @Override public int hashCode() { return 42; } }

        map.put(obj1, obj2, 'obj1')
        map.put(obj2, obj1, 'obj2')
        map.put(obj3, obj2, 'obj3')

        assert map.size() == 3
        assert map.fullSize() == 3

        assert map.get(obj1, obj2) == 'obj1'
        assert map.get(obj2, obj1) == 'obj2'
        assert map.get(obj3, obj2) == 'obj3'
    }

}
