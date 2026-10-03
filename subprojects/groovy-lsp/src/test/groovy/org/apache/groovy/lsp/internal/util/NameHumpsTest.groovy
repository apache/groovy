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
package org.apache.groovy.lsp.internal.util

import org.junit.jupiter.api.Test

final class NameHumpsTest {

    @Test
    void camelHumpsMatchAbstractServerFactory() {
        assert NameHumps.matches('ASF', 'AbstractServerFactory')
        assert NameHumps.matches('ServerFactory', 'AbstractServerFactory')
        assert NameHumps.matches('TaskMgr', 'TaskManager')
        assert NameHumps.matches('factory', 'AbstractServerFactory')
        assert !NameHumps.matches('XYZ', 'AbstractServerFactory')
        assert !NameHumps.matches('ASF', null)
        assert NameHumps.matches('', 'Hello')
        assert NameHumps.matches(null, 'Hello')
        assert NameHumps.splitHumps('AbstractServerFactory') == ['Abstract', 'Server', 'Factory']
        assert NameHumps.splitHumps('ASF') == ['A', 'S', 'F']
        assert NameHumps.splitHumps('http2Client') == ['http', '2', 'Client']
        assert NameHumps.splitHumps('') == []
        assert NameHumps.splitHumps(null) == []
    }

    @Test
    void mismatchedHumpDoesNotMatch() {
        assert !NameHumps.matches('Aqz', 'Abstract')
        def hump = NameHumps.getDeclaredMethod('humpMatches', String, String)
        hump.accessible = true
        assert hump.invoke(null, '', 'Abstract') == false
    }
}
