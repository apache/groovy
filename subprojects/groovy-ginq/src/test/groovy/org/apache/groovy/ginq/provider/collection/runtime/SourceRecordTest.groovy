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
package org.apache.groovy.ginq.provider.collection.runtime


import org.junit.Test

import static groovy.lang.Tuple.tuple

class SourceRecordTest {
    @Test
    void testGet() {
        def r = tuple(tuple(1, 2), 3)
        def sr = new SourceRecord(r, ['a', 'b', 'c'])
        assert r === sr.'*'
        assert 1 == sr.a
        assert 2 == sr.b
        assert 3 == sr.c
    }
}
