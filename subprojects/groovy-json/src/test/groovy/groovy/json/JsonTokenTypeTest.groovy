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

import static groovy.json.JsonTokenType.CLOSE_BRACKET
import static groovy.json.JsonTokenType.CLOSE_CURLY
import static groovy.json.JsonTokenType.COLON
import static groovy.json.JsonTokenType.COMMA
import static groovy.json.JsonTokenType.FALSE
import static groovy.json.JsonTokenType.NULL
import static groovy.json.JsonTokenType.NUMBER
import static groovy.json.JsonTokenType.OPEN_BRACKET
import static groovy.json.JsonTokenType.OPEN_CURLY
import static groovy.json.JsonTokenType.STRING
import static groovy.json.JsonTokenType.TRUE
import static groovy.json.JsonTokenType.startingWith

class JsonTokenTypeTest extends GroovyTestCase {

    void testMatchingConstants() {
        assert !TRUE.matching('xyz')
        assert !TRUE.matching('t')
        assert !TRUE.matching('tr')
        assert !TRUE.matching('tru')
        assert TRUE.matching('true')

        assert !FALSE.matching('xyz')
        assert !FALSE.matching('f')
        assert !FALSE.matching('fa')
        assert !FALSE.matching('fal')
        assert !FALSE.matching('fals')
        assert FALSE.matching('false')

        assert !NULL.matching('xyz')
        assert !NULL.matching('n')
        assert !NULL.matching('nu')
        assert !NULL.matching('nul')
        assert NULL.matching('null')
    }

    void testMatchingPunctuation() {
        assert OPEN_CURLY.matching('{')
        assert CLOSE_CURLY.matching('}')
        assert OPEN_BRACKET.matching('[')
        assert CLOSE_BRACKET.matching(']')
        assert COLON.matching(':')
        assert COMMA.matching(',')
    }

    void testMatchingNumbers() {
        assert !NUMBER.matching('-')
        assert NUMBER.matching('-1')
        assert NUMBER.matching('-1.2')
        assert !NUMBER.matching('-1.2e')
        assert !NUMBER.matching('-1.2e-')
        assert NUMBER.matching('-1.2e-3')

        assert NUMBER.matching('1')
        assert NUMBER.matching('12')
        assert NUMBER.matching('12.3')
        assert NUMBER.matching('12.34')
        assert !NUMBER.matching('12.34e')
        assert NUMBER.matching('12.34e5')
        assert NUMBER.matching('12.34e56')
    }

    void testMatchingString() {
        assert !STRING.matching('1234')

        assert !STRING.matching('"')
        assert !STRING.matching('"a')
        assert STRING.matching('"a"')
        assert STRING.matching('"aa"')

        assert !STRING.matching('"a\\')
        assert !STRING.matching('"a\\"')
        assert STRING.matching('"a\\""')
    }

    void testMatchingLongStringWithBackslashes() {
        assert STRING.matching('"a' + '\\"' * 10000 + '"')
    }

    void testTokenStartingWithChar() {
        assert startingWith('{' as char) == OPEN_CURLY
        assert startingWith('}' as char) == CLOSE_CURLY
        assert startingWith('[' as char) == OPEN_BRACKET
        assert startingWith(']' as char) == CLOSE_BRACKET
        assert startingWith(',' as char) == COMMA
        assert startingWith(':' as char) == COLON

        assert startingWith('t' as char) == TRUE
        assert startingWith('f' as char) == FALSE
        assert startingWith('n' as char) == NULL

        assert startingWith('"' as char) == STRING

        assert startingWith('-' as char) == NUMBER
        assert startingWith('1' as char) == NUMBER
        assert startingWith('2' as char) == NUMBER
        assert startingWith('3' as char) == NUMBER
        assert startingWith('4' as char) == NUMBER
        assert startingWith('5' as char) == NUMBER
        assert startingWith('6' as char) == NUMBER
        assert startingWith('7' as char) == NUMBER
        assert startingWith('8' as char) == NUMBER
        assert startingWith('9' as char) == NUMBER
        assert startingWith('0' as char) == NUMBER
    }
}
