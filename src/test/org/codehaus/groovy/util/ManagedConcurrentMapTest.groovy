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

import groovy.test.GroovyTestCase

class ManagedConcurrentMapTest extends GroovyTestCase {

    ManagedConcurrentMap<Object, String> map =
            new ManagedConcurrentMap<Object, String>(ReferenceBundle.getHardBundle())

    void testEntriesRemoveSelfFromMapWhenFinalized() {
        List<ManagedReference<Object>> entries = []
        for (int i = 0; i < 5; i++) {
            entries << map.getOrPut(new Object(), "Object ${i}")
        }

        assert map.size() == 5
        assert map.fullSize() == 5

        entries*.finalizeReference()

        assert map.size() == 0
        assert map.fullSize() == 0
    }

}
