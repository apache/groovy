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
package org.apache.groovy.json.internal;

import java.util.ArrayList;
import java.util.List;

public class JsonParserLax extends JsonParserCharArray {

    private final boolean useValues;
    private final boolean chop;
    private final boolean lazyChop;
    private final boolean defaultCheckDates;

    public JsonParserLax() {
        this(true);
    }

    public JsonParserLax(boolean useValues) {
        this(useValues, false);
    }

    public JsonParserLax(boolean useValues, boolean chop) {
        this(useValues, chop, !chop);
    }

    public JsonParserLax(boolean useValues, boolean chop, boolean lazyChop) {
        this(useValues, chop, lazyChop, true);
    }

    public JsonParserLax(boolean useValues, boolean chop, boolean lazyChop, boolean defaultCheckDates) {
        this.useValues = useValues;
        this.chop = chop;
        this.lazyChop = lazyChop;
        this.defaultCheckDates = defaultCheckDates;
    }

    private Value decodeJsonObjectLax() {
        if (__currentChar == '{')
            this.nextChar();

        ValueMap map = useValues ? new ValueMapImpl() : new LazyValueMap(lazyChop);
        Value value = new ValueContainer(map);

        skipWhiteSpace();
        int startIndexOfKey = __index;
        Value key;
        MapItemValue miv;
        Value item;

        done:
        for (; __index < this.charArray.length; __index++) {
            skipWhiteSpace();

            switch (__currentChar) {
                case ':':
                    char startChar = charArray[startIndexOfKey];
                    if (startChar == ',') {
                        startIndexOfKey++;
                    }

                    key = extractLaxString(startIndexOfKey, __index - 1, false, false);
                    __index++; //skip :

                    item = decodeValueInternal();
                    skipWhiteSpace();

                    miv = new MapItemValue(key, item);

                    map.add(miv);

                    startIndexOfKey = __index;
                    if (__currentChar == '}') {
                        __index++;
                        break done;
                    }

                    break;

                case '\'':
                    key = decodeStringSingle();

                    //puts ("key with quote", key);

                    skipWhiteSpace();

                    if (__currentChar != ':') {
                        complain("expecting current character to be ':' but got " + charDescription(__currentChar) + "\n");
                    }
                    __index++;
                    item = decodeValueInternal();

                    //puts ("key", "#" + key + "#", value);

                    skipWhiteSpace();

                    miv = new MapItemValue(key, item);

                    map.add(miv);
                    startIndexOfKey = __index;
                    if (__currentChar == '}') {
                        __index++;
                        break done;
                    }

                    break;

                case '"':
                    key = decodeStringDouble();

                    //puts ("key with quote", key);

                    skipWhiteSpace();

                    if (__currentChar != ':') {
                        complain("expecting current character to be ':' but got " + charDescription(__currentChar) + "\n");
                    }
                    __index++;
                    item = decodeValueInternal();

                    //puts ("key", "#" + key + "#", value);

                    skipWhiteSpace();

                    miv = new MapItemValue(key, item);

                    map.add(miv);
                    startIndexOfKey = __index;
                    if (__currentChar == '}') {
                        __index++;
                        break done;
                    }

                    break;
            }

            switch (__currentChar) {
                case '}':
                    __index++;
                    break done;

                case '/': /* */ //
                    handleComment();
                    startIndexOfKey = __index;
                    break;

                case '#':
                    handleBashComment();
                    startIndexOfKey = __index;
                    break;
            }
        }

        return value;
    }

    private Value extractLaxString(int startIndexOfKey, int end, boolean encoded, boolean checkDate) {
        char startChar;
        startIndexLookup:
        for (; startIndexOfKey < __index && startIndexOfKey < charArray.length; startIndexOfKey++) {
            startChar = charArray[startIndexOfKey];
            switch (startChar) {
                case ' ':
                case '\n':
                case '\t':
                    continue;

                default:
                    break startIndexLookup;
            }
        }

        char endChar;
        int endIndex = end >= charArray.length ? charArray.length - 1 : end;
        endIndexLookup:
        for (; endIndex >= startIndexOfKey + 1 && endIndex >= 0; endIndex--) {
            endChar = charArray[endIndex];
            switch (endChar) {
                case ' ':
                case '\n':
                case '\t':
                case '}':
                    continue;
                case ',':
                case ';':
                    continue;

                case ']':
                    continue;
                default:
                    break endIndexLookup;
            }
        }
        return new CharSequenceValue(chop, Type.STRING, startIndexOfKey, endIndex + 1, this.charArray, encoded, checkDate);
    }

    @Override
    protected final Object decodeValue() {
        return this.decodeValueInternal();
    }

    private Value decodeValueInternal() {
        Value value = null;

        for (; __index < charArray.length; __index++) {
            skipWhiteSpace();

            switch (__currentChar) {
                case '\n':
                    break;

                case '\r':
                    break;

                case ' ':
                    break;

                case '\t':
                    break;

                case '\b':
                    break;

                case '\f':
                    break;

                case '/': /* */ //
                    handleComment();
                    break;

                case '#':
                    handleBashComment();
                    break;

                case '"':
                    value = decodeStringDouble();
                    break;

                case '\'':
                    value = decodeStringSingle();
                    break;

                case 't':
                    if (isTrue()) {
                        return decodeTrue() ? ValueContainer.TRUE : ValueContainer.FALSE;
                    } else {
                        value = decodeStringLax();
                    }
                    break;

                case 'f':
                    if (isFalse()) {
                        return !decodeFalse() ? ValueContainer.FALSE : ValueContainer.TRUE;
                    } else {
                        value = decodeStringLax();
                    }
                    break;

                case 'n':
                    if (isNull()) {
                        return decodeNull() == null ? ValueContainer.NULL : ValueContainer.NULL;
                    } else {
                        value = decodeStringLax();
                    }

                    break;

                case '[':
                    value = decodeJsonArrayLax();
                    break;

                case '{':
                    value = decodeJsonObjectLax();
                    break;

                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                case '0':
                    return decodeNumberLax(false);

                case '-':
                    return decodeNumberLax(true);

                default:
                    value = decodeStringLax();
            }

            if (value != null) {
                return value;
            }
        }

        return null;
    }

    private void handleBashComment() {
        for (; __index < charArray.length; __index++) {
            __currentChar = charArray[__index];

            if (__currentChar == '\n') {
                __index++;
                return;
            }
        }
    }

    private void handleComment() {
        if (hasMore()) {
            __index++;
            __currentChar = charArray[__index];

            switch (__currentChar) {
                case '*':
                    for (; __index < charArray.length; __index++) {
                        __currentChar = charArray[__index];

                        if (__currentChar == '*') {
                            if (hasMore()) {
                                __index++;
                                __currentChar = charArray[__index];
                                if (__currentChar == '/') {
                                    if (hasMore()) {
                                        __index++;
                                        return;
                                    }
                                }
                            } else {
                                complain("missing close of comment");
                            }
                        }
                    }

                case '/':
                    for (; __index < charArray.length; __index++) {
                        __currentChar = charArray[__index];

                        if (__currentChar == '\n') {
                            if (hasMore()) {
                                __index++;
                                return;
                            } else {
                                return;
                            }
                        }
                    }
            }
        }
    }

    /**
     * Decodes a number from a JSON value.  If at any point it is determined that
     * the value is not a valid number the value is treated as a {@code String}.
     *
     * @param minus indicate whether the number is negative
     * @return a number, or {@code String} if not a valid number
     */
    protected final Value decodeNumberLax(boolean minus) {
        char[] array = charArray;

        final int startIndex = __index;
        int index = __index;
        char currentChar;
        boolean doubleFloat = false;
        boolean foundDot = false;
        boolean foundSign = false;
        boolean foundExp = false;

        if (minus && index + 1 < array.length) {
            index++;
        }

        while (true) {
            currentChar = array[index];
            if (isNumberDigit(currentChar)) {
                //noop
            } else if (currentChar <= 32) { //white
                break;
            } else if (isDelimiter(currentChar)) {
                break;
            } else if (isDecimalChar(currentChar)) {
                switch (currentChar) {
                    case DECIMAL_POINT:
                        if (foundDot || foundExp) { return decodeStringLax(); }
                        foundDot = true;
                        break;
                    case LETTER_E:
                    case LETTER_BIG_E:
                        if (foundExp) { return decodeStringLax(); }
                        foundExp = true;
                        break;
                    case MINUS:
                    case PLUS:
                        if (foundSign || !foundExp) { return decodeStringLax(); }
                        if (foundExp && array[index - 1] != LETTER_E && array[index - 1] != LETTER_BIG_E) {
                            return decodeStringLax();
                        }
                        foundSign = true;
                        break;
                }
                doubleFloat = true;
            } else {
                return decodeStringLax();
            }
            index++;
            if (index >= array.length) break;
        }

        // Handle the case where the exponential number ends without the actual exponent
        if (foundExp) {
            char prevChar = array[index - 1];
            if (prevChar == LETTER_E || prevChar == LETTER_BIG_E || prevChar == MINUS || prevChar == PLUS) {
                return decodeStringLax();
            }
        }

        __index = index;
        __currentChar = currentChar;

        Type type = doubleFloat ? Type.DOUBLE : Type.INTEGER;

        return new NumberValue(chop, type, startIndex, __index, this.charArray);
    }

    private boolean isNull() {
        if (__index + NULL.length <= charArray.length) {
            if (charArray[__index] == 'n' &&
                    charArray[__index + 1] == 'u' &&
                    charArray[__index + 2] == 'l' &&
                    charArray[__index + 3] == 'l') {
                return true;
            }
        }
        return false;
    }

    private boolean isTrue() {
        if (__index + TRUE.length <= charArray.length) {
            if (charArray[__index] == 't' &&
                    charArray[__index + 1] == 'r' &&
                    charArray[__index + 2] == 'u' &&
                    charArray[__index + 3] == 'e') {
                return true;

            }
        }
        return false;
    }

    private boolean isFalse() {
        if (__index + FALSE.length <= charArray.length) {
            if (charArray[__index] == 'f' &&
                    charArray[__index + 1] == 'a' &&
                    charArray[__index + 2] == 'l' &&
                    charArray[__index + 3] == 's' &&
                    charArray[__index + 4] == 'e') {
                return true;
            }
        }
        return false;
    }

    private Value decodeStringLax() {
        int index = __index;
        char currentChar = charArray[__index];
        final int startIndex = __index;
        boolean encoded = false;
        char[] charArray = this.charArray;

        for (; index < charArray.length; index++) {
            currentChar = charArray[index];

            if (isDelimiter(currentChar)) break;
            else if (currentChar == '\\') break;
        }

        Value value = this.extractLaxString(startIndex, index, encoded, defaultCheckDates);

        __index = index;
        return value;
    }

    private Value decodeStringDouble() {
        __currentChar = charArray[__index];

        if (__index < charArray.length && __currentChar == '"') {
            __index++;
        }

        final int startIndex = __index;

        boolean escape = false;
        boolean encoded = false;

        done:
        for (; __index < this.charArray.length; __index++) {
            __currentChar = charArray[__index];
            switch (__currentChar) {

                case '"':
                    if (!escape) {
                        break done;
                    } else {
                        escape = false;
                        continue;
                    }

                case '\\':
                    escape = !escape;
                    encoded = true;
                    continue;
            }
            escape = false;
        }

        Value value = new CharSequenceValue(chop, Type.STRING, startIndex, __index, this.charArray, encoded, defaultCheckDates);

        if (__index < charArray.length) {
            __index++;
        }

        return value;
    }

    private Value decodeStringSingle() {
        __currentChar = charArray[__index];

        if (__index < charArray.length && __currentChar == '\'') {
            __index++;
        }

        final int startIndex = __index;

        boolean escape = false;
        boolean encoded = false;
        int minusCount = 0;
        int colonCount = 0;

        done:
        for (; __index < this.charArray.length; __index++) {
            __currentChar = charArray[__index];
            switch (__currentChar) {
                case '\'':
                    if (!escape) {
                        break done;
                    } else {
                        escape = false;
                        continue;
                    }

                case '\\':
                    encoded = true;
                    escape = true;
                    continue;

                case '-':
                    minusCount++;
                    break;

                case ':':
                    colonCount++;
                    break;
            }
            escape = false;
        }

        boolean checkDates = defaultCheckDates && !encoded && minusCount >= 2 && colonCount >= 2;

        Value value = new CharSequenceValue(chop, Type.STRING, startIndex, __index, this.charArray, encoded, checkDates);

        if (__index < charArray.length) {
            __index++;
        }

        return value;
    }

    private Value decodeJsonArrayLax() {
        if (__currentChar == '[') {
            __index++;
        }

        skipWhiteSpace();

        if (__currentChar == ']') {
            __index++;
            return new ValueContainer(new ArrayList());
        }

        List<Object> list;

        if (useValues) {
            list = new ArrayList<Object>();
        } else {
            list = new ValueList(lazyChop);
        }

        Value value = new ValueContainer(list);

        do {
            skipWhiteSpace();

            Object arrayItem = decodeValueInternal();

            list.add(arrayItem);

            boolean doStop = false;

            done:
            do { // Find either next array element or end of array while ignoring comments
                skipWhiteSpace();

                switch (__currentChar) {
                    case '/':
                        handleComment();
                        continue;
                    case '#':
                        handleBashComment();
                        continue;
                    case ',':
                        __index++;
                        break done;
                    case ']':
                        __index++;
                        doStop = true;
                        break done;
                    default:
                        String charString = charDescription(__currentChar);

                        complain(
                                String.format("expecting a ',' or a ']', " +
                                        " but got \nthe current character of  %s " +
                                        " on array index of %s \n", charString, list.size())
                        );
                }
            } while (this.hasMore());

            if (doStop) break;

        } while (this.hasMore());

        return value;
    }

    @Override
    protected final Object decodeFromChars(char[] cs) {
        Value value = ((Value) super.decodeFromChars(cs));
        if (value.isContainer()) {
            return value.toValue();
        } else {
            return value;
        }
    }
}
