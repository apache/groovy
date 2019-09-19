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

class ClosureDefaultParameterTest extends GroovyTestCase {

    void testClosureWithDefaultParams() {

        def block = {a = 123, b = 456 -> "$a $b".toString() }

        assert block.call(456, "def") == "456 def"
        assert block.call() == "123 456"
        assert block(456) == "456 456"
        assert block(456, "def") == "456 def"

        def block2 = { Integer a = 123, String b = "abc" -> "$a $b".toString() }

        assert block2.call(456, "def") == "456 def"
        assert block2.call() == "123 abc"
        assert block2(456) == "456 abc"
        assert block2(456, "def") == "456 def"
    }
    
    void testClosureWithDefaultParamFromOuterScope() {
        def y = 555
        def boo = {x = y -> x}
        assert boo() == y
        assert boo(1) == 1
    }

}

