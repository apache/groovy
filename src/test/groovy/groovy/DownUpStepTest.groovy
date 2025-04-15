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
package groovy

import groovy.test.GroovyTestCase

public class DownUpStepTest extends GroovyTestCase {

    void testDownto() {
        def z = []
        (10.5).downto(5.9) { z << it }
        assertEquals( [10.5, 9.5, 8.5, 7.5, 6.5], z)
    }

    void testBigIntegerDowntoBigDecimal() {
        def z = []
        10G.downto(5.9G) { z << it }
        assertEquals( [10G, 9G, 8G, 7G, 6G], z)
    }

    void testUpto() {
        def z = 0.0
        (3.1).upto(7.2) { z += it }
        assert z == 3.1 + 4.1 + 5.1 + 6.1 + 7.1
        assert z == 25.5
    }

    void testStep() {
        def z = 0.0
        (1.2).step(3.9, 0.1) { z += it }
        assert z == 67.5
    }

    void testDownStep() {
        def z = 0.0
        (3.8).step(1.1, -0.1) { z += it }
        assert z == 67.5
    }
}
