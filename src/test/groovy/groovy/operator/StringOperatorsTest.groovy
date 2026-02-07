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

import org.junit.jupiter.api.Test


class StringOperatorsTest {

    def x
    def y

    @Test
    void testPlus() {
        x = "hello " + "there"
        assert x == "hello there"

        x = "hello " + 2
        assert x == "hello 2"

        x = "hello " + 1.2
        assert x == "hello 1.2"

        y = x + 1
        assert y == "hello 1.21"
    }

    @Test
    void testLongPlus() {
        x = "hello" + " " + "there" + " nice" + " day"

        assert x == "hello there nice day"
    }

    @Test
    void testMinus() {
        x = "the quick brown fox" - "quick "

        assert x == "the brown fox"

        y = x - "brown "

        assert y == "the fox"
    }

    @Test
    void testOperationsOnConstantString() {
        assert "hello".size() == 5

        assert "the quick brown".substring(4).substring(0, 5) == "quick"
    }

    @Test
    void testBitwiseNegate() {
        String value="test"
        String s = "^\\S+$value\$"
        def p = ~s
        assert p instanceof java.util.regex.Pattern
    }
}
