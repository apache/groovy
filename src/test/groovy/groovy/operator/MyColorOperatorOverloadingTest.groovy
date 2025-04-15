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
package groovy.operator

import groovy.test.GroovyTestCase

import static java.awt.Color.*

class MyColorOperatorOverloadingTest extends GroovyTestCase {
    void testAll() {
        if (HeadlessTestSupport.headless) return

        def c = new MyColor(128, 128, 128)
        assert c.delegate == GRAY
        def c2 = -c
        assert c2.delegate == DARK_GRAY
        assert (+c).delegate == WHITE
        use(MyColorCategory) {
            assert (~c2).delegate == LIGHT_GRAY
        }
    }
}
