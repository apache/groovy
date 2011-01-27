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

/**
 * A JSON token, with a type, line / column information, and the text of that token.
 *
 * @author Guillaume Laforge
 * @since 1.8.0
 */
class JsonToken {
    private static final BigInteger MAX_LONG    = BigInteger.valueOf(Long.MAX_VALUE)
    private static final BigInteger MIN_LONG    = BigInteger.valueOf(Long.MIN_VALUE)
    private static final BigInteger MAX_INTEGER = BigInteger.valueOf(Integer.MAX_VALUE);
    private static final BigInteger MIN_INTEGER = BigInteger.valueOf(Integer.MIN_VALUE)
    private static final BigDecimal MAX_DOUBLE  = new BigDecimal(String.valueOf(Double.MAX_VALUE))
    private static final BigDecimal MIN_DOUBLE  = MAX_DOUBLE.negate()
    private static final BigDecimal MAX_FLOAT   = new BigDecimal(String.valueOf(Float.MAX_VALUE))
    private static final BigDecimal MIN_FLOAT   = MAX_FLOAT.negate()

    /** Start line position */
    int startLine
    /** End line position */
    int endLine
    /** Start column position */
    int startColumn
    /** End column position */
    int endColumn

    /** The type of the token */
    JsonTokenType type

    /** The text of that token */
    String text

    /**
     * Return the value represented by this token (ie. a number, a string, a boolean or null).
     * For numbers, the most appropriate type is returned (Float, Double, BigDecimal for decimal numbers,
     * and Integer, Long and BigInteger for integral numbers).
     *
     * @return the represented value
     */
    Object getValue() {
        switch (type) {
            case STRING:
                if (text.size() == 2) {
                    return ""
                } else {
                    return text[1..-2]
                }
            case NUMBER:
                if (text.contains('.') || text.contains('e') || text.contains('E')) {
                    // a decimal number
                    BigDecimal v = new BigDecimal(text)
                    if(v.compareTo(MAX_FLOAT) <= 0 && v.compareTo(MIN_FLOAT) >= 0) {
                        return new Float(text)
                    } else if(v.compareTo(MAX_DOUBLE) <= 0 && v.compareTo(MIN_DOUBLE) >= 0) {
                        return new Double(text)
                    } else {
                        return v
                    }
                } else {
                    // an integer number
                    BigInteger v = new BigInteger(text)
                    if(v.compareTo(MAX_INTEGER) <= 0 && v.compareTo(MIN_INTEGER) >= 0 ) {
                        return Integer.valueOf(v.intValue())
                    } else if (v.compareTo(MAX_LONG) <= 0 && v.compareTo(MIN_LONG) >= 0 ) {
                        return new Long(v.longValue())
                    } else {
                        return v
                    }
                }
            case BOOL_TRUE:
                return true
            case BOOL_FALSE:
                return false
            case NULL:
                return null
            default:
                throw new JsonException("No appropriate value represented by '$text' on line: ${startLine}, column: ${startColumn}")
        }
    }

    String toString() {
        "$text ($type) [$startLine:$startColumn-$endLine:$endColumn]"
    }
}
