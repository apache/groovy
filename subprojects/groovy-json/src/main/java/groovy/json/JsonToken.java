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
package groovy.json;

import java.math.BigDecimal;
import java.math.BigInteger;

import static groovy.json.JsonTokenType.FALSE;
import static groovy.json.JsonTokenType.NULL;
import static groovy.json.JsonTokenType.NUMBER;
import static groovy.json.JsonTokenType.STRING;
import static groovy.json.JsonTokenType.TRUE;

/**
 * A JSON token, with a type, line / column information, and the text of that token.
 *
 * @since 1.8.0
 */
public class JsonToken {

    private static final BigInteger MAX_LONG    = BigInteger.valueOf(Long.MAX_VALUE);
    private static final BigInteger MIN_LONG    = BigInteger.valueOf(Long.MIN_VALUE);
    private static final BigInteger MAX_INTEGER = BigInteger.valueOf(Integer.MAX_VALUE);
    private static final BigInteger MIN_INTEGER = BigInteger.valueOf(Integer.MIN_VALUE);

    /** Start line position */
    private long startLine;
    /** End line position */
    private long endLine;
    /** Start column position */
    private long startColumn;
    /** End column position */
    private long endColumn;

    /** The type of the token */
    private JsonTokenType type;

    /** The text of that token */
    private String text;

    /**
     * Return the value represented by this token (i.e. a number, a string, a boolean or null).
     * For numbers, BigDecimal is returned for decimals and Integer, Long or BigInteger for
     * integral numbers.
     *
     * @return the represented value
     */
    public Object getValue() {
        if (type == STRING) {
            if (text.length() == 2) {
                return "";
            } else {
                return text.substring(1, text.length() - 1);
            }
        } else if (type == NUMBER) {
            if (text.contains(".") || text.contains("e") || text.contains("E")) {
                // a decimal number
                return new BigDecimal(text);
            } else {
                // an integer number
                BigInteger v = new BigInteger(text);
                if (v.compareTo(MAX_INTEGER) <= 0 && v.compareTo(MIN_INTEGER) >= 0) {
                    return v.intValue();
                } else if (v.compareTo(MAX_LONG) <= 0 && v.compareTo(MIN_LONG) >= 0) {
                    return v.longValue();
                } else {
                    return v;
                }
            }
        } else if (type == TRUE) {
            return true;
        } else if (type == FALSE) {
            return false;
        } else if (type == NULL) {
            return null;
        } else {
            throw new JsonException(
                    "No appropriate value represented by '" + text +
                    "' on line: " + startLine + ", column: " + startColumn
            );
        }
    }

    /**
     * Returns a debug string containing the token text, type, and source positions.
     *
     * @return a string representation of this token
     */
    @Override
    public String toString() {
        return text + " (" + type + ") [" + startLine + ":" + startColumn + "-" + endLine + ":" + endColumn + "]";
    }

    /**
     * Returns the starting line of this token.
     *
     * @return the starting line number
     */
    public long getStartLine() {
        return startLine;
    }

    /**
     * Sets the starting line of this token.
     *
     * @param startLine the starting line number
     */
    public void setStartLine(long startLine) {
        this.startLine = startLine;
    }

    /**
     * Returns the ending line of this token.
     *
     * @return the ending line number
     */
    public long getEndLine() {
        return endLine;
    }

    /**
     * Sets the ending line of this token.
     *
     * @param endLine the ending line number
     */
    public void setEndLine(long endLine) {
        this.endLine = endLine;
    }

    /**
     * Returns the starting column of this token.
     *
     * @return the starting column number
     */
    public long getStartColumn() {
        return startColumn;
    }

    /**
     * Sets the starting column of this token.
     *
     * @param startColumn the starting column number
     */
    public void setStartColumn(long startColumn) {
        this.startColumn = startColumn;
    }

    /**
     * Returns the ending column of this token.
     *
     * @return the ending column number
     */
    public long getEndColumn() {
        return endColumn;
    }

    /**
     * Sets the ending column of this token.
     *
     * @param endColumn the ending column number
     */
    public void setEndColumn(long endColumn) {
        this.endColumn = endColumn;
    }

    /**
     * Returns the token type.
     *
     * @return the token type
     */
    public JsonTokenType getType() {
        return this.type;
    }

    /**
     * Sets the token type.
     *
     * @param type the token type
     */
    public void setType(JsonTokenType type) {
        this.type = type;
    }

    /**
     * Sets the token text.
     *
     * @param text the token text
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * Returns the token text.
     *
     * @return the token text
     */
    public String getText() {
        return this.text;
    }
}
