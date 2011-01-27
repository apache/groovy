package groovy.json

import static JsonTokenType.*
import static Matching.*

/**
 * @author Guillaume Laforge
 */
class JsonTokenTypeTest extends GroovyTestCase {

    void testMatchingConstants() {
        assert BOOL_TRUE.matching('xyz')        == NO
        assert BOOL_TRUE.matching('t')          == POSSIBLE
        assert BOOL_TRUE.matching('tr')         == POSSIBLE
        assert BOOL_TRUE.matching('tru')        == POSSIBLE
        assert BOOL_TRUE.matching('true')       == YES

        assert BOOL_FALSE.matching('xyz')       == NO
        assert BOOL_FALSE.matching('f')         == POSSIBLE
        assert BOOL_FALSE.matching('fa')        == POSSIBLE
        assert BOOL_FALSE.matching('fal')       == POSSIBLE
        assert BOOL_FALSE.matching('fals')      == POSSIBLE
        assert BOOL_FALSE.matching('false')     == YES

        assert NULL.matching('xyz')             == NO
        assert NULL.matching('n')               == POSSIBLE
        assert NULL.matching('nu')              == POSSIBLE
        assert NULL.matching('nul')             == POSSIBLE
        assert NULL.matching('null')            == YES
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
}
