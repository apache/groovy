package groovy.json

import static JsonTokenType.*

/**
 * @author Guillaume Laforge
 */
class JsonTokenValueTest extends GroovyTestCase {

    void testValues() {
        assert new JsonToken(type: STRING, text: '""').value == ""
        assert new JsonToken(type: STRING, text: '"abc"').value == "abc"
        assert new JsonToken(type: STRING, text: '"abc\""').value == 'abc"'

        assert new JsonToken(type: NULL, text: 'null').value == null

        assert new JsonToken(type: BOOL_TRUE, text: 'true').value
        assert !new JsonToken(type: BOOL_FALSE, text: 'false').value

        assert new JsonToken(type: NUMBER, text: '0').value == 0
        assert new JsonToken(type: NUMBER, text: '1000').value == 1000
        assert new JsonToken(type: NUMBER, text: '-1000').value == -1000

        assert new JsonToken(type: NUMBER, text: '0').value instanceof Integer
        assert new JsonToken(type: NUMBER, text: '1000').value instanceof Integer
        assert new JsonToken(type: NUMBER, text: '-1000').value instanceof Integer

        assert new JsonToken(type: NUMBER, text: '10000000000').value instanceof Long

        assert new JsonToken(type: NUMBER, text: '100000000000000000000000').value instanceof BigInteger

        assert new JsonToken(type: NUMBER, text: '1.234').value instanceof Float
        assert new JsonToken(type: NUMBER, text: '1.234e13').value instanceof Float
        assert new JsonToken(type: NUMBER, text: '1E+13').value instanceof Float
        assert new JsonToken(type: NUMBER, text: '-1E-13').value instanceof Float

        assert new JsonToken(type: NUMBER, text: '1.234e135').value instanceof Double

        assert new JsonToken(type: NUMBER, text: '1.234e1357').value instanceof BigDecimal
    }

    void testWeirdTheoricalValue() {
        shouldFail { assert new JsonToken(type: NUMBER, text: '1234xyz').value }
        shouldFail { new JsonToken(type: OPEN_CURLY, text: '{').value }
    }
}
