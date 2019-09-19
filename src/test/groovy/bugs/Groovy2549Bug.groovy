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

class Groovy2549Bug extends GroovyTestCase {
    void test2549() {
        def c = 2
        def x = 1 + (c as int)
        assert x == 3

        x = (c as int) + 1
        assert x == 3

        def y = 1 + (2 as Integer)
        assert y == 3

        def z = 1 + (2 as long)
        assert z == 3

        def zzz = 1 + (2 as float)
        assert zzz == 3
    }
}
