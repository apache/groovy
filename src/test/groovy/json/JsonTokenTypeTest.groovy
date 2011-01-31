/*
 * Copyright 2003-2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package groovy.json

import static JsonTokenType.*
import static Matching.*

/**
 * @author Guillaume Laforge
 */
class JsonTokenTypeTest extends GroovyTestCase {

    void testMatchingConstants() {
        assert TRUE.matching('xyz')        == NO
        assert TRUE.matching('t')          == POSSIBLE
        assert TRUE.matching('tr')         == POSSIBLE
        assert TRUE.matching('tru')        == POSSIBLE
        assert TRUE.matching('true')       == YES

        assert FALSE.matching('xyz')       == NO
        assert FALSE.matching('f')         == POSSIBLE
        assert FALSE.matching('fa')        == POSSIBLE
        assert FALSE.matching('fal')       == POSSIBLE
        assert FALSE.matching('fals')      == POSSIBLE
        assert FALSE.matching('false')     == YES

        assert NULL.matching('xyz')        == NO
        assert NULL.matching('n')          == POSSIBLE
        assert NULL.matching('nu')         == POSSIBLE
        assert NULL.matching('nul')        == POSSIBLE
        assert NULL.matching('null')       == YES
    }

    void testMatchingPunctuation() {
        assert OPEN_CURLY.matching('{')         == YES
        assert CLOSE_CURLY.matching('}')        == YES
        assert OPEN_BRACKET.matching('[')       == YES
        assert CLOSE_BRACKET.matching(']')      == YES
        assert COLON.matching(':')              == YES
        assert COMMA.matching(',')              == YES
    }

    void testMatchingNumbers() {
        assert NUMBER.matching('-')             == POSSIBLE
        assert NUMBER.matching('-1')            == YES
        assert NUMBER.matching('-1.2')          == YES
        assert NUMBER.matching('-1.2e')         == POSSIBLE
        assert NUMBER.matching('-1.2e-')        == POSSIBLE
        assert NUMBER.matching('-1.2e-3')       == YES

        assert NUMBER.matching('1')             == YES
        assert NUMBER.matching('12')            == YES
        assert NUMBER.matching('12.3')          == YES
        assert NUMBER.matching('12.34')         == YES
        assert NUMBER.matching('12.34e')        == POSSIBLE
        assert NUMBER.matching('12.34e5')       == YES
        assert NUMBER.matching('12.34e56')      == YES
    }

    void testMatchingString() {
        assert STRING.matching('1234')          == NO

        assert STRING.matching('"')             == POSSIBLE
        assert STRING.matching('"a')            == POSSIBLE
        assert STRING.matching('"a"')           == YES
        assert STRING.matching('"aa"')          == YES

        assert STRING.matching('"a\\')          == POSSIBLE
        assert STRING.matching('"a\\"')         == POSSIBLE
        assert STRING.matching('"a\\""')        == YES
    }

    void testTokenStartingWithChar() {
        assert startingWith('{' as char)      == OPEN_CURLY
        assert startingWith('}' as char)      == CLOSE_CURLY
        assert startingWith('[' as char)      == OPEN_BRACKET
        assert startingWith(']' as char)      == CLOSE_BRACKET
        assert startingWith(',' as char)      == COMMA
        assert startingWith(':' as char)      == COLON

        assert startingWith('t' as char)      == TRUE
        assert startingWith('f' as char)      == FALSE
        assert startingWith('n' as char)      == NULL

        assert startingWith('"' as char)      == STRING

        assert startingWith('-' as char)      == NUMBER
        assert startingWith('1' as char)      == NUMBER
        assert startingWith('2' as char)      == NUMBER
        assert startingWith('3' as char)      == NUMBER
        assert startingWith('4' as char)      == NUMBER
        assert startingWith('5' as char)      == NUMBER
        assert startingWith('6' as char)      == NUMBER
        assert startingWith('7' as char)      == NUMBER
        assert startingWith('8' as char)      == NUMBER
        assert startingWith('9' as char)      == NUMBER
        assert startingWith('0' as char)      == NUMBER
    }
}
