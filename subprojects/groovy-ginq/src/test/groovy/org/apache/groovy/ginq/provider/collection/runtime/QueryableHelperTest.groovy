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

import static org.apache.groovy.ginq.provider.collection.runtime.QueryableHelper.isEqual

class QueryableHelperTest {
    @Test
    void testIsEqual() {
        def n1 = 1
        def n2 = 2
        def n3 = 3
        assert isEqual(n1, n1)
        assert isEqual(n1, new Integer(1))
        assert isEqual(new Integer(1), n1)
        assert isEqual(new NamedTuple([n1, n2], ['n1', 'n2']), new NamedTuple([n1, n2], ['n1', 'n2']))
        assert isEqual(new NamedTuple([new Integer(1), n2], ['n1', 'n2']), new NamedTuple([n1, n2], ['n1', 'n2']))
        assert isEqual(new NamedTuple([new NamedTuple([n1, n2], ['n1', 'n2']), n3], ['(n1, n2)', 'n3']), new NamedTuple([new NamedTuple([n1, n2], ['n1', 'n2']), n3], ['(n1, n2)', 'n3']))
        assert isEqual(new NamedTuple([new NamedTuple([new Integer(1), n2], ['n1', 'n2']), n3], ['(n1, n2)', 'n3']), new NamedTuple([new NamedTuple([n1, n2], ['n1', 'n2']), n3], ['(n1, n2)', 'n3']))
    }
}
