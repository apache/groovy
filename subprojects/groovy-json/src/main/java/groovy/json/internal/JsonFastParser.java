/*
 * Copyright 2003-2014 the original author or authors.
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
 *
 * Derived from Boon all rights granted to Groovy project for this fork.
 */
package groovy.json.internal;

import java.util.ArrayList;
import java.util.List;

/**
 * This works in index overlay mode or chop mode.
 * Chop mode reduces possibility of memory leak but causes a few more buffer copies as it chops up the buffer.
 *
 * @author Rick Hightower
 */
public class JsonFastParser extends JsonParserCharArray {

    private final boolean useValues;
    private final boolean chop;
    private final boolean lazyChop;
    private final boolean checkDates;

    public JsonFastParser() {
        this(true);
    }

    public JsonFastParser(boolean useValues) {
        this(useValues, false);
    }

    public JsonFastParser(boolean useValues, boolean chop) {
        this(useValues, chop, !chop);
    }

    public JsonFastParser(boolean useValues, boolean chop, boolean lazyChop) {
        this(useValues, chop, lazyChop, true);
    }

    public JsonFastParser(boolean useValues, boolean chop, boolean lazyChop, boolean checkDates) {
        this.useValues = useValues;
        this.chop = chop;
        this.lazyChop = lazyChop;
        this.checkDates = checkDates;
    }

    protected final Value decodeJsonObjectLazyFinalParse() {
        char[] array = charArray;

        if (__currentChar == '{')
            __index++;

        ValueMap map = useValues ? new ValueMapImpl() : new LazyValueMap(lazyChop);
        Value value = new ValueContainer(map);

        objectLoop:
        for (; __index < array.length; __index++) {
            skipWhiteSpace();
            switch (__currentChar) {

                case '"':
                    Value key = decodeStringOverlay();
                    skipWhiteSpace();

                    if (__currentChar != ':') {

                        complain("expecting current character to be " + charDescription(__currentChar) + "\n");
                    }
                    __index++;

                    Value item = decodeValueOverlay();

                    skipWhiteSpace();

                    MapItemValue miv = new MapItemValue(key, item);

                    map.add(miv);
            }

            switch (__currentChar) {
                case '}':
                    __index++;
                    break objectLoop;

                case ',':
                    continue;

                default:

                    complain(
                            "expecting '}' or ',' but got current char " + charDescription(__currentChar));
            }
        }
        return value;
    }

    protected Value decodeValue() {
        return decodeValueOverlay();
    }

    private Value decodeValueOverlay() {
        skipWhiteSpace();

        switch (__currentChar) {
            case '"':
                return decodeStringOverlay();

            case '{':
                return decodeJsonObjectLazyFinalParse();

            case 't':
                return decodeTrue() == true ? ValueContainer.TRUE : ValueContainer.FALSE;

            case 'f':
                return decodeFalse() == false ? ValueContainer.FALSE : ValueContainer.TRUE;

            case 'n':
                return decodeNull() == null ? ValueContainer.NULL : ValueContainer.NULL;

            case '[':
                return decodeJsonArrayOverlay();

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
                return decodeNumberOverlay(false);

            case '-':
                return decodeNumberOverlay(true);

            default:
                complain("Unable to determine the " +
                        "current character, it is not a string, number, array, or object");
                return null;
        }
    }

    private final Value decodeNumberOverlay(final boolean minus) {
        char[] array = charArray;

        final int startIndex = __index;
        int index = __index;
        char currentChar;
        boolean doubleFloat = false;
        boolean foundDot = false;

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
            } else if (currentChar == '.') {
                if (foundDot) {
                    complain("unexpected character " + currentChar);
                }
                foundDot = true;
                doubleFloat = true;
            } else if (isDecimalChar(currentChar)) {
                doubleFloat = true;
            }
            index++;
            if (index >= array.length) break;
        }

        __index = index;
        __currentChar = currentChar;

        Type type = doubleFloat ? Type.DOUBLE : Type.INTEGER;

        NumberValue value = new NumberValue(chop, type, startIndex, __index, this.charArray);

        return value;
    }

    private Value decodeStringOverlay() {
        char[] array = charArray;
        int index = __index;
        char currentChar = charArray[index];

        if (index < array.length && currentChar == '"') {
            index++;
        }

        final int startIndex = index;

        boolean encoded = hasEscapeChar(array, index, indexHolder);
        index = indexHolder[0];

        if (encoded) {
            index = findEndQuote(array, index);
        }

        Value value = new CharSequenceValue(chop, Type.STRING, startIndex, index, array, encoded, checkDates);

        if (index < array.length) {
            index++;
        }

        __index = index;
        return value;
    }

    private Value decodeJsonArrayOverlay() {
        char[] array = charArray;
        if (__currentChar == '[') {
            __index++;
        }

        skipWhiteSpace();

        /* the list might be empty  */
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

        Value item;
        char c;
        int lastIndex;
        boolean foundEnd = false;

        arrayLoop:
        for (; __index < array.length; __index++) {
            item = decodeValueOverlay();

            list.add(item);
            c = currentChar();
            switch (c) {
                case ',':
                    continue;
                case ']':
                    __index++;
                    foundEnd = true;
                    break arrayLoop;
            }

            lastIndex = __index;
            skipWhiteSpace();
            c = currentChar();

            switch (c) {
                case ',':
                    continue;
                case ']':
                    if (__index == lastIndex) {
                        complain("missing ]");
                    }
                    foundEnd = true;
                    __index++;
                    break arrayLoop;
                default:
                    complain(
                            String.format("expecting a ',' or a ']', " +
                                    " but got \nthe current character of  %s " +
                                    " on array size of %s \n", charDescription(__currentChar), list.size())
                    );
            }
        }

        if (!foundEnd) {
            complain("Did not find end of Json Array");
        }
        return value;
    }

    protected final Object decodeFromChars(char[] cs) {
        Value value = ((Value) super.decodeFromChars(cs));
        if (value.isContainer()) {
            return value.toValue();
        } else {
            return value;
        }
    }
}
