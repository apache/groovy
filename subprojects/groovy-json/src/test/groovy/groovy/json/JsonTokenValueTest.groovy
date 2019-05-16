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
package groovy.json

import groovy.test.GroovyTestCase

import static groovy.json.JsonTokenType.FALSE
import static groovy.json.JsonTokenType.NULL
import static groovy.json.JsonTokenType.NUMBER
import static groovy.json.JsonTokenType.OPEN_CURLY
import static groovy.json.JsonTokenType.STRING
import static groovy.json.JsonTokenType.TRUE

class JsonTokenValueTest extends GroovyTestCase {

    void testValues() {
        assert new JsonToken(type: STRING, text: '""').value == ""
        assert new JsonToken(type: STRING, text: '"abc"').value == "abc"
        assert new JsonToken(type: STRING, text: '"abc\""').value == 'abc"'

        assert new JsonToken(type: NULL, text: 'null').value == null

        assert new JsonToken(type: TRUE, text: 'true').value
        assert !new JsonToken(type: FALSE, text: 'false').value

        assert new JsonToken(type: NUMBER, text: '0').value == 0
        assert new JsonToken(type: NUMBER, text: '1000').value == 1000
        assert new JsonToken(type: NUMBER, text: '-1000').value == -1000

        assert new JsonToken(type: NUMBER, text: '0').value instanceof Integer
        assert new JsonToken(type: NUMBER, text: '1000').value instanceof Integer
        assert new JsonToken(type: NUMBER, text: '-1000').value instanceof Integer

        assert new JsonToken(type: NUMBER, text: '10000000000').value instanceof Long

        assert new JsonToken(type: NUMBER, text: '100000000000000000000000').value instanceof BigInteger

        assert new JsonToken(type: NUMBER, text: '1.234').value instanceof BigDecimal
        assert new JsonToken(type: NUMBER, text: '1.234e13').value instanceof BigDecimal
        assert new JsonToken(type: NUMBER, text: '1E+13').value instanceof BigDecimal
        assert new JsonToken(type: NUMBER, text: '-1E-13').value instanceof BigDecimal
        assert new JsonToken(type: NUMBER, text: '1.234e135').value instanceof BigDecimal
        assert new JsonToken(type: NUMBER, text: '1.234e1357').value instanceof BigDecimal

        assert new JsonToken(type: NUMBER, text: '123456.123456789').value instanceof BigDecimal
        assert new JsonToken(type: NUMBER, text: '123456.123456789').value == 123456.123456789
    }

    void testWeirdTheoricalValue() {
        shouldFail { assert new JsonToken(type: NUMBER, text: '1234xyz').value }
        shouldFail { new JsonToken(type: OPEN_CURLY, text: '{').value }
    }
}
