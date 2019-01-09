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
package org.codehaus.groovy.runtime.memoize

class LRUProtectionStorageTest extends GroovyTestCase {
    void testLRUStrategyWithOneElement() {
        def storage = new LRUProtectionStorage(1)
        assert storage.size() == 0
        storage['key1'] = 1
        assert storage.size() == 1
        storage['key2'] = 2
        assert storage.size() == 1
        assertEquals 2, storage['key2']
        storage['key1'] = 1
        assert storage.size() == 1
        assertNull storage['key2']
        assertEquals 1, storage['key1']

    }

    void testLRUStrategy() {
        def storage = new LRUProtectionStorage(3)
        assert storage.size() == 0
        storage['key1'] = 1
        assert storage.size() == 1
        storage['key2'] = 2
        storage['key3'] = 3
        assert storage.size() == 3
        assertEquals 1, storage['key1']
        assertEquals 2, storage['key2']
        assertEquals 3, storage['key3']
        storage['key4'] = 4
        assert storage.size() == 3
        assertNull storage['key1']
        assertEquals 2, storage['key2']
        assertEquals 3, storage['key3']
        assertEquals 4, storage['key4']
        storage['key4']
        storage['key2']
        storage['key5'] = 5
        assert storage.size() == 3
        assertNull storage['key3']
        assertEquals 2, storage['key2']
        assertEquals 4, storage['key4']
        assertEquals 5, storage['key5']
    }

    void testTouch() {
        def storage = new LRUProtectionStorage(3)
        storage['key1'] = 1
        storage['key2'] = 2
        storage['key3'] = 3
        storage.touch('key1', 11)
        storage['key4'] = 4
        assert storage.size() == 3
        assertEquals 11, storage['key1']
        assertEquals 4, storage['key4']
        assertEquals 3, storage['key3']
        assertNull storage['key2']
    }
}
