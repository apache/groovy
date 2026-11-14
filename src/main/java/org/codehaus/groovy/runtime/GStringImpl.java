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
package org.codehaus.groovy.runtime;

import groovy.lang.GString;
import groovy.lang.GroovyObject;
import groovy.transform.Pure;
import org.apache.groovy.internal.util.ImmutableTypes;
import org.apache.groovy.lang.annotation.GroovyABI;

import java.io.IOException;
import java.io.Serial;
import java.io.Writer;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.Locale;
import java.util.stream.Stream;

/**
 * Default implementation of a GString used by the compiler. A GString consists
 * of a list of values and strings which can be combined to create a new String.
 *
 * @see groovy.lang.GString
 */
public class GStringImpl extends GString {
    @Serial private static final long serialVersionUID = 3581289038662723858L;
    private final String[] strings;
    private final boolean frozen;
    private boolean cacheable;
    private String cachedStringLiteral;

    /**
     * Create a new GString with values and strings.
     * <p>
     * Each value is prefixed by a string, after the last value
     * an additional String might be used, hence the following constraint is expected to hold:
     * <code>
     * strings.length == values.length  ||  strings.length == values.length + 1
     * </code>.
     * <p>
     * <strong>NOTE:</strong> The lengths are <strong>not</strong> checked but using arrays with
     * lengths which violate the above constraint could result in unpredictable behaviour.
     *
     * @param values  the value parts
     * @param strings the string parts
     */
    @GroovyABI(since = "1.0.0")
    public GStringImpl(Object[] values, String[] strings) {
        this(values, strings, checkValuesStringConstant(values), null, false);
    }

    /**
     * Create a new GString with values and strings.
     * <p>
     * Each value is prefixed by a string, after the last value
     * an additional String might be used, hence the following constraint is expected to hold:
     * <code>
     * strings.length == values.length  ||  strings.length == values.length + 1
     * </code>.
     * <p>
     * <strong>NOTE:</strong> The lengths are <strong>not</strong> checked but using arrays with
     * lengths which violate the above constraint could result in unpredictable behaviour.
     *
     * @param values  the value parts
     * @param strings the string parts
     * @param frozen  creates a GStringImpl which is not subject to mutation and hence more amenable to caching
     */
    protected GStringImpl(Object[] values, String[] strings, boolean cacheable, String cachedStringLiteral, boolean frozen) {
        super(frozen ? values.clone() : values);
        this.strings = frozen ? strings.clone() : strings;
        this.cacheable = cacheable;
        this.cachedStringLiteral = cachedStringLiteral;
        this.frozen = frozen;
    }

    @Override
    public GString plus(GString that) {
        GString thatFrozen = that instanceof GStringImpl ? ((GStringImpl) that).freeze() : that;
        return GStringUtil.plusImpl(super.getValues(), thatFrozen.getValues(), strings, thatFrozen.getStrings());
    }

    @Override
    public Writer writeTo(Writer out) throws IOException {
        return GStringUtil.writeToImpl(out, super.getValues(), strings);
    }

    /* (non-Javadoc)
     * @see groovy.lang.Buildable#build(groovy.lang.GroovyObject)
     */
    @Override
    public void build(final GroovyObject builder) {
        GStringUtil.buildImpl(builder, super.getValues(), strings);
    }

    @Override
    protected int calcInitialCapacity() {
        return GStringUtil.calcInitialCapacityImpl(super.getValues(), strings);
    }

    /**
     * @return returns an equivalent optimised but less mutable version of this GString
     */
    public GString freeze() {
        return new GStringImpl(super.getValues(), strings, cacheable, cachedStringLiteral, true);
    }

    /**
     * Get the strings of this GString.
     * <p>
     * This method returns the same array as used in the constructor.
     * Changing the values will result in changes of the GString.
     * It is generally not recommended to do so.
     */
    @Override
    public String[] getStrings() {
        if (frozen) {
            return strings.clone();
        }
        cacheable = false;
        cachedStringLiteral = null;
        return strings;
    }

    @Override
    public Object[] getValues() {
        if (frozen) {
            return super.getValues().clone();
        }
        cacheable = false;
        cachedStringLiteral = null;
        return super.getValues();
    }

    /**
     * Returns the rendered text with every leading and trailing UTF-16
     * code unit whose value is at most {@code U+0020} removed. When neither
     * end has a code unit at or below that bound, the rendered string is
     * returned unchanged.
     * <p>
     * The text comes from {@link #toString()}. When this instance may cache
     * its rendering, the stored text is reused; otherwise the literal segments
     * and values are interpolated on every call.
     *
     * @return the rendered text without those leading and trailing code units
     */
    public String trim() {
        return toString().trim();
    }

    /**
     * Reports whether the rendered text contains no code units.
     * <p>
     * The text comes from {@link #toString()}. When this instance may cache
     * its rendering, the stored text is reused; otherwise the literal segments
     * and values are interpolated on every call.
     *
     * @return {@code true} when the rendered length is zero
     */
    public boolean isEmpty() {
        return toString().isEmpty();
    }

    /**
     * Returns a stream of lines extracted from this string,
     * separated by line terminators.
     * <p>
     * A <i>line terminator</i> is one of the following:
     * a line feed character {@code "\n"} (U+000A),
     * a carriage return character {@code "\r"} (U+000D),
     * or a carriage return followed immediately by a line feed
     * {@code "\r\n"} (U+000D U+000A).
     * <p>
     * A <i>line</i> is either a sequence of zero or more characters
     * followed by a line terminator, or it is a sequence of one or
     * more characters followed by the end of the string. A
     * line does not include the line terminator.
     * <p>
     * The stream returned by this method contains the lines from
     * this string in the order in which they occur.
     * <p>
     * <b>API Note:</b> This definition of <i>line</i> implies that an empty
     * string has zero lines and that there is no empty line
     * following a line terminator at the end of a string.
     * <p>
     * <b>Implementation Note:</b> This method provides better performance than
     * {@code split("\R")} by supplying elements lazily and
     * by faster search of new line terminators.
     *
     * @return  the stream of lines extracted from this string
     *
     * @since 5.0.0
     */
    public Stream<String> lines() {
        return toString().lines();
    }

    /**
     * Returns {@code true} if the string is empty or contains only
     * {@linkplain Character#isWhitespace(int) white space} codepoints,
     * otherwise {@code false}.
     *
     * @return {@code true} if the string is empty or contains only
     *         {@linkplain Character#isWhitespace(int) white space} codepoints,
     *         otherwise {@code false}
     *
     * @see Character#isWhitespace(int)
     *
     * @since 5.0.0
     */
    public boolean isBlank() {
        return toString().isBlank();
    }

    /**
     * Returns a string whose value is the concatenation of this
     * string repeated {@code count} times.
     * <p>
     * If this string is empty or count is zero then the empty
     * string is returned.
     *
     * @param   count number of times to repeat
     *
     * @return  A string composed of this string repeated
     *          {@code count} times or the empty string if this
     *          string is empty or count is zero
     *
     * @throws  IllegalArgumentException if the {@code count} is
     *          negative.
     *
     * @since 5.0.0
     */
    public String repeat(int count) {
        return toString().repeat(count);
    }

    /**
     * Returns a string whose value is this string, with all leading
     * {@linkplain Character#isWhitespace(int) white space} removed.
     * <p>
     * If this {@code String} object represents an empty string,
     * or if all code points in this string are
     * {@linkplain Character#isWhitespace(int) white space}, then an empty string
     * is returned.
     * <p>
     * Otherwise, returns a substring of this string beginning with the first
     * code point that is not a {@linkplain Character#isWhitespace(int) white space}
     * up to and including the last code point of this string.
     * <p>
     * This method may be used to trim
     * {@linkplain Character#isWhitespace(int) white space} from
     * the beginning of a string.
     *
     * @return  a string whose value is this string, with all leading white
     *          space removed
     *
     * @see Character#isWhitespace(int)
     *
     * @since 5.0.0
     */
    public String stripLeading() {
        return toString().stripLeading();
    }

    /**
     * Returns a string whose value is this string, with all trailing
     * {@linkplain Character#isWhitespace(int) white space} removed.
     * <p>
     * If this {@code String} object represents an empty string,
     * or if all characters in this string are
     * {@linkplain Character#isWhitespace(int) white space}, then an empty string
     * is returned.
     * <p>
     * Otherwise, returns a substring of this string beginning with the first
     * code point of this string up to and including the last code point
     * that is not a {@linkplain Character#isWhitespace(int) white space}.
     * <p>
     * This method may be used to trim
     * {@linkplain Character#isWhitespace(int) white space} from
     * the end of a string.
     *
     * @return  a string whose value is this string, with all trailing white
     *          space removed
     *
     * @see Character#isWhitespace(int)
     *
     * @since 5.0.0
     */
    public String stripTrailing() {
        return toString().stripTrailing();
    }

    /**
     * Returns a string whose value is this string, with all leading
     * and trailing {@linkplain Character#isWhitespace(int) white space}
     * removed.
     * <p>
     * If this {@code String} object represents an empty string,
     * or if all code points in this string are
     * {@linkplain Character#isWhitespace(int) white space}, then an empty string
     * is returned.
     * <p>
     * Otherwise, returns a substring of this string beginning with the first
     * code point that is not a {@linkplain Character#isWhitespace(int) white space}
     * up to and including the last code point that is not a
     * {@linkplain Character#isWhitespace(int) white space}.
     * <p>
     * This method may be used to strip
     * {@linkplain Character#isWhitespace(int) white space} from
     * the beginning and end of a string.
     *
     * @return  a string whose value is this string, with all leading
     *          and trailing white space removed
     *
     * @see Character#isWhitespace(int)
     *
     * @since 5.0.0
     */
    public String strip() {
        return toString().strip();
    }

    /**
     * Returns the Unicode code point at UTF-16 index {@code index} in the
     * rendered text. A high surrogate at that index followed by a low
     * surrogate is returned as one supplementary code point; any other
     * code unit, including an unpaired surrogate, is returned as its own
     * value.
     * <p>
     * The text comes from {@link #toString()}. When this instance may cache
     * its rendering, the stored text is reused; otherwise the literal segments
     * and values are interpolated on every call.
     *
     * @param index UTF-16 index of the code unit to read, counting from zero
     * @return the code point that starts at {@code index}
     * @throws StringIndexOutOfBoundsException if {@code index} is negative
     *     or not less than the rendered length
     */
    public int codePointAt(int index) {
        return toString().codePointAt(index);
    }

    /**
     * Returns the Unicode code point that ends just before UTF-16 index
     * {@code index}. When the code unit at {@code index - 1} is a low
     * surrogate and the unit before it is a high surrogate, the
     * supplementary code point of that pair is returned; otherwise the
     * single code unit at {@code index - 1} is returned.
     * <p>
     * The text comes from {@link #toString()}. When this instance may cache
     * its rendering, the stored text is reused; otherwise the literal segments
     * and values are interpolated on every call.
     *
     * @param index UTF-16 index just after the code point to read
     * @return the code point that ends at {@code index}
     * @throws StringIndexOutOfBoundsException if {@code index} is less
     *     than 1 or greater than the rendered length
     */
    public int codePointBefore(int index) {
        return toString().codePointBefore(index);
    }

    /**
     * Returns how many Unicode code points lie in the rendered text from
     * {@code beginIndex} inclusive to {@code endIndex} exclusive. An
     * unpaired surrogate counts as one code point.
     * <p>
     * The text comes from {@link #toString()}. When this instance may cache
     * its rendering, the stored text is reused; otherwise the literal segments
     * and values are interpolated on every call.
     *
     * @param beginIndex first UTF-16 index of the range, inclusive
     * @param endIndex UTF-16 index at which the range stops, exclusive
     * @return the number of code points in that range
     * @throws IndexOutOfBoundsException if {@code beginIndex} is negative,
     *     {@code endIndex} is greater than the rendered length, or
     *     {@code beginIndex} is greater than {@code endIndex}
     */
    public int codePointCount(int beginIndex, int endIndex) {
        return toString().codePointCount(beginIndex, endIndex);
    }

    /**
     * Returns the UTF-16 index reached by moving {@code codePointOffset}
     * code points from {@code index}. An unpaired surrogate counts as one
     * code point. {@code index} may equal the rendered length, and a zero
     * offset then stays on that length.
     * <p>
     * The text comes from {@link #toString()}. When this instance may cache
     * its rendering, the stored text is reused; otherwise the literal segments
     * and values are interpolated on every call.
     *
     * @param index UTF-16 index at which the walk starts, from zero through
     *     the rendered length inclusive
     * @param codePointOffset number of code points to move. Positive moves
     *     toward the end and negative moves toward the start
     * @return the UTF-16 index where the walk stops
     * @throws IndexOutOfBoundsException if {@code index} is negative or
     *     greater than the rendered length, or if the walk would pass
     *     either end of the text
     */
    public int offsetByCodePoints(int index, int codePointOffset) {
        return toString().offsetByCodePoints(index, codePointOffset);
    }

    /**
     * Copies UTF-16 code units from the rendered text into {@code dst}.
     * The copied range starts at {@code srcBegin} inclusive and stops at
     * {@code srcEnd} exclusive, and the first copied unit is written at
     * {@code dstBegin}.
     * <p>
     * The text comes from {@link #toString()}. When this instance may cache
     * its rendering, the stored text is reused; otherwise the literal segments
     * and values are interpolated on every call.
     *
     * @param srcBegin first rendered index to copy, inclusive
     * @param srcEnd rendered index at which copying stops, exclusive
     * @param dst array that receives the copied code units
     * @param dstBegin index in {@code dst} of the first copied unit
     * @throws StringIndexOutOfBoundsException if the source range falls
     *     outside the rendered text, or if {@code dst} has no room for
     *     the copied units starting at {@code dstBegin}
     * @throws NullPointerException if {@code dst} is null
     */
    public void getChars(int srcBegin, int srcEnd, char[] dst, int dstBegin) {
        toString().getChars(srcBegin, srcEnd, dst, dstBegin);
    }

    /**
     * Encodes the rendered text with {@code charset} into a new byte array.
     * <p>
     * The text comes from {@link #toString()}. When this instance may cache
     * its rendering, the stored text is reused; otherwise the literal segments
     * and values are interpolated on every call.
     *
     * @param charset encoding used to turn the rendered text into bytes
     * @return a new array holding that encoding
     * @throws NullPointerException if {@code charset} is null
     */
    public byte[] getBytes(Charset charset) {
        return toString().getBytes(charset);
    }

    /**
     * Reports whether the rendered text and {@code sb} have the same
     * length and the same characters. The buffer is locked while its
     * length and characters are read.
     * <p>
     * The text comes from {@link #toString()}. When this instance may cache
     * its rendering, the stored text is reused; otherwise the literal segments
     * and values are interpolated on every call.
     *
     * @param sb buffer whose current contents are compared
     * @return {@code true} when both sides have the same length and characters
     * @throws NullPointerException if {@code sb} is null
     */
    public boolean contentEquals(StringBuffer sb) {
        return toString().contentEquals(sb);
    }

    /**
     * Reports whether the rendered text and {@code cs} have the same
     * length and the same characters. A {@link String} is compared with
     * {@link String#equals(Object)}. A {@link StringBuffer} is locked
     * for the read. A {@link StringBuilder} is compared without a lock.
     * Any other sequence is walked with its length and {@code charAt}.
     * <p>
     * The text comes from {@link #toString()}. When this instance may cache
     * its rendering, the stored text is reused; otherwise the literal segments
     * and values are interpolated on every call.
     *
     * @param cs sequence compared with the rendered text
     * @return {@code true} when both sides have the same length and characters
     * @throws NullPointerException if {@code cs} is null
     */
    public boolean contentEquals(CharSequence cs) {
        return toString().contentEquals(cs);
    }

    /**
     * Reports whether the rendered text and {@code anotherString} are
     * equal ignoring case. The lengths must already match, and each
     * UTF-16 code unit is then folded with
     * {@link Character#toUpperCase(char)} and, when those differ,
     * {@link Character#toLowerCase(char)}. No locale is consulted, so
     * the result can disagree with folding both sides through
     * {@link String#toLowerCase()}. A null argument yields {@code false}.
     * <p>
     * The text comes from {@link #toString()}. When this instance may cache
     * its rendering, the stored text is reused; otherwise the literal segments
     * and values are interpolated on every call.
     *
     * @param anotherString string compared with the rendered text
     * @return {@code true} when the two texts match under that case folding
     */
    public boolean equalsIgnoreCase(String anotherString) {
        return toString().equalsIgnoreCase(anotherString);
    }

    /**
     * Orders the rendered text against {@code anotherString} by UTF-16
     * code unit, using the same ordering as {@link String#compareTo(String)}.
     * <p>
     * The text comes from {@link #toString()}. When this instance may cache
     * its rendering, the stored text is reused; otherwise the literal segments
     * and values are interpolated on every call.
     *
     * @param anotherString string the rendered text is ordered against
     * @return a negative integer, zero, or a positive integer when the
     *     rendered text is less than, equal to, or greater than
     *     {@code anotherString}
     * @throws NullPointerException if {@code anotherString} is null
     */
    public int compareTo(String anotherString) {
        return toString().compareTo(anotherString);
    }

    /**
     * Orders the rendered text against {@code str} by UTF-16 code unit
     * after case folding. Each unit is folded with
     * {@link Character#toUpperCase(char)} and then
     * {@link Character#toLowerCase(char)}. No locale is consulted. When
     * one folded text is a prefix of the other, the shorter one sorts first.
     * <p>
     * The text comes from {@link #toString()}. When this instance may cache
     * its rendering, the stored text is reused; otherwise the literal segments
     * and values are interpolated on every call.
     *
     * @param str string the rendered text is ordered against
     * @return a negative integer, zero, or a positive integer when the
     *     folded rendered text is less than, equal to, or greater than
     *     the folded {@code str}
     * @throws NullPointerException if {@code str} is null
     */
    public int compareToIgnoreCase(String str) {
        return toString().compareToIgnoreCase(str);
    }

    /**
     * Compares {@code len} UTF-16 code units of the rendered text starting
     * at {@code toffset} with {@code len} code units of {@code other}
     * starting at {@code ooffset}. A negative offset, or a region that
     * does not fit in either text, yields {@code false}. A negative
     * {@code len} compares no units and yields {@code true} when both
     * offsets are non-negative.
     * <p>
     * The text comes from {@link #toString()}. When this instance may cache
     * its rendering, the stored text is reused; otherwise the literal segments
     * and values are interpolated on every call.
     *
     * @param toffset first rendered index of the region
     * @param other string that supplies the region to compare against
     * @param ooffset first index of the region in {@code other}
     * @param len number of UTF-16 code units to compare
     * @return {@code true} when the compared units match, or when {@code len}
     *     is negative and both offsets are non-negative
     * @throws NullPointerException if {@code other} is null
     */
    public boolean regionMatches(int toffset, String other, int ooffset, int len) {
        return toString().regionMatches(toffset, other, ooffset, len);
    }

    /**
     * Compares {@code len} UTF-16 code units of the rendered text starting
     * at {@code toffset} with the same number of units of {@code other}
     * starting at {@code ooffset}. When {@code ignoreCase} is true, each
     * unit is folded with {@link Character#toUpperCase(char)} and, when
     * those differ, {@link Character#toLowerCase(char)}; otherwise the
     * units are compared unchanged. A negative offset, or a region that
     * does not fit in either text, yields {@code false}. A negative
     * {@code len} compares no units and yields {@code true} when both
     * offsets are non-negative.
     * <p>
     * The text comes from {@link #toString()}. When this instance may cache
     * its rendering, the stored text is reused; otherwise the literal segments
     * and values are interpolated on every call.
     *
     * @param ignoreCase {@code true} to fold case before comparing units
     * @param toffset first rendered index of the region
     * @param other string that supplies the region to compare against
     * @param ooffset first index of the region in {@code other}
     * @param len number of UTF-16 code units to compare
     * @return {@code true} when the regions match under that comparison, or
     *     when {@code len} is negative and both offsets are non-negative
     * @throws NullPointerException if {@code other} is null
     */
    public boolean regionMatches(boolean ignoreCase, int toffset, String other, int ooffset, int len) {
        return toString().regionMatches(ignoreCase, toffset, other, ooffset, len);
    }

    /**
     * Reports whether {@code prefix} occurs in the rendered text at
     * UTF-16 index {@code toffset}. An empty prefix matches when
     * {@code toffset} lies between zero and the rendered length,
     * inclusive. A negative offset, or a prefix that would extend past
     * the end, yields {@code false}.
     * <p>
     * The text comes from {@link #toString()}. When this instance may cache
     * its rendering, the stored text is reused; otherwise the literal segments
     * and values are interpolated on every call.
     *
     * @param prefix sequence that must occur at {@code toffset}
     * @param toffset UTF-16 index at which {@code prefix} must start
     * @return {@code true} when the rendered text has {@code prefix} there
     * @throws NullPointerException if {@code prefix} is null
     */
    public boolean startsWith(String prefix, int toffset) {
        return toString().startsWith(prefix, toffset);
    }

    /**
     * Reports whether the rendered text begins with {@code prefix}.
     * An empty prefix matches. The comparison starts at the first code unit.
     * <p>
     * The text comes from {@link #toString()}. When this instance may cache
     * its rendering, the stored text is reused; otherwise the literal segments
     * and values are interpolated on every call.
     *
     * @param prefix sequence the rendered text must begin with
     * @return {@code true} when {@code prefix} is a prefix of the rendered text
     * @throws NullPointerException if {@code prefix} is null
     */
    public boolean startsWith(String prefix) {
        return toString().startsWith(prefix);
    }

    /**
     * Reports whether the rendered text ends with {@code suffix}.
     * An empty suffix matches.
     * <p>
     * The text comes from {@link #toString()}. When this instance may cache
     * its rendering, the stored text is reused; otherwise the literal segments
     * and values are interpolated on every call.
     *
     * @param suffix sequence the rendered text must end with
     * @return {@code true} when {@code suffix} is a suffix of the rendered text
     * @throws NullPointerException if {@code suffix} is null
     */
    public boolean endsWith(String suffix) {
        return toString().endsWith(suffix);
    }

    /**
     * Returns the UTF-16 index of the first occurrence of code point
     * {@code ch} in the rendered text, or {@code -1} when it does not
     * occur. A basic-plane value matches one code unit, a supplementary
     * code point matches its surrogate pair, and any other {@code int}
     * does not occur. The search starts at the first code unit.
     * <p>
     * The text comes from {@link #toString()}. When this instance may cache
     * its rendering, the stored text is reused; otherwise the literal segments
     * and values are interpolated on every call.
     *
     * @param ch code point to search for
     * @return the index of the first match, or {@code -1} when there is none
     */
    public int indexOf(int ch) {
        return toString().indexOf(ch);
    }

    /**
     * Returns the UTF-16 index of the first occurrence of code point
     * {@code ch} at or after {@code fromIndex}, or {@code -1} when it
     * does not occur. A negative {@code fromIndex} starts at the
     * beginning, and a {@code fromIndex} at or beyond the rendered
     * length yields {@code -1}. A basic-plane value matches one code
     * unit, a supplementary code point matches its surrogate pair, and
     * any other {@code int} does not occur.
     * <p>
     * The text comes from {@link #toString()}. When this instance may cache
     * its rendering, the stored text is reused; otherwise the literal segments
     * and values are interpolated on every call.
     *
     * @param ch code point to search for
     * @param fromIndex UTF-16 index at which the search starts
     * @return the index of the first match at or after {@code fromIndex},
     *     or {@code -1} when there is none
     */
    public int indexOf(int ch, int fromIndex) {
        return toString().indexOf(ch, fromIndex);
    }

    /**
     * Returns the UTF-16 index of the last occurrence of code point
     * {@code ch} in the rendered text, or {@code -1} when it does not
     * occur. A basic-plane value matches one code unit, a supplementary
     * code point matches its surrogate pair, and any other {@code int}
     * does not occur. The search starts at the last code unit.
     * <p>
     * The text comes from {@link #toString()}. When this instance may cache
     * its rendering, the stored text is reused; otherwise the literal segments
     * and values are interpolated on every call.
     *
     * @param ch code point to search for
     * @return the index of the last match, or {@code -1} when there is none
     */
    public int lastIndexOf(int ch) {
        return toString().lastIndexOf(ch);
    }

    /**
     * Returns the UTF-16 index of the last occurrence of code point
     * {@code ch} at or before {@code fromIndex}, or {@code -1} when it
     * does not occur. A negative {@code fromIndex} yields {@code -1}.
     * A {@code fromIndex} past the end searches the whole rendered text.
     * A basic-plane value matches one code unit, a supplementary code
     * point matches its surrogate pair, and any other {@code int} does
     * not occur.
     * <p>
     * The text comes from {@link #toString()}. When this instance may cache
     * its rendering, the stored text is reused; otherwise the literal segments
     * and values are interpolated on every call.
     *
     * @param ch code point to search for
     * @param fromIndex UTF-16 index at which the backward search starts
     * @return the index of the last match at or before {@code fromIndex},
     *     or {@code -1} when there is none
     */
    public int lastIndexOf(int ch, int fromIndex) {
        return toString().lastIndexOf(ch, fromIndex);
    }

    /**
     * Returns the UTF-16 index of the first occurrence of {@code str}
     * in the rendered text, or {@code -1} when it does not occur. The
     * empty string occurs at index zero.
     * <p>
     * The text comes from {@link #toString()}. When this instance may cache
     * its rendering, the stored text is reused; otherwise the literal segments
     * and values are interpolated on every call.
     *
     * @param str exact sequence to search for
     * @return the index of the first match, or {@code -1} when there is none
     * @throws NullPointerException if {@code str} is null
     */
    public int indexOf(String str) {
        return toString().indexOf(str);
    }

    /**
     * Returns the UTF-16 index of the first occurrence of {@code str}
     * at or after {@code fromIndex}, or {@code -1} when it does not
     * occur. A negative {@code fromIndex} starts at the beginning. The
     * empty string occurs at {@code fromIndex} clamped into the range
     * from zero through the rendered length.
     * <p>
     * The text comes from {@link #toString()}. When this instance may cache
     * its rendering, the stored text is reused; otherwise the literal segments
     * and values are interpolated on every call.
     *
     * @param str exact sequence to search for
     * @param fromIndex UTF-16 index at which the search starts
     * @return the index of the first match at or after {@code fromIndex},
     *     or {@code -1} when there is none
     * @throws NullPointerException if {@code str} is null
     */
    public int indexOf(String str, int fromIndex) {
        return toString().indexOf(str, fromIndex);
    }

    /**
     * Returns the UTF-16 index of the last occurrence of {@code str}
     * in the rendered text, or {@code -1} when it does not occur. The
     * empty string occurs at the rendered length.
     * <p>
     * The text comes from {@link #toString()}. When this instance may cache
     * its rendering, the stored text is reused; otherwise the literal segments
     * and values are interpolated on every call.
     *
     * @param str exact sequence to search for
     * @return the index of the last match, or {@code -1} when there is none
     * @throws NullPointerException if {@code str} is null
     */
    public int lastIndexOf(String str) {
        return toString().lastIndexOf(str);
    }

    /**
     * Returns the UTF-16 index of the last occurrence of {@code str}
     * at or before {@code fromIndex}, or {@code -1} when it does not
     * occur. A negative {@code fromIndex} yields {@code -1}, and a
     * {@code fromIndex} past the end searches the whole rendered text.
     * The empty string occurs at {@code fromIndex} when that index is
     * in range, and at the rendered length when {@code fromIndex} is
     * past the end.
     * <p>
     * The text comes from {@link #toString()}. When this instance may cache
     * its rendering, the stored text is reused; otherwise the literal segments
     * and values are interpolated on every call.
     *
     * @param str exact sequence to search for
     * @param fromIndex UTF-16 index at which the backward search starts
     * @return the index of the last match at or before {@code fromIndex},
     *     or {@code -1} when there is none
     * @throws NullPointerException if {@code str} is null
     */
    public int lastIndexOf(String str, int fromIndex) {
        return toString().lastIndexOf(str, fromIndex);
    }

    /**
     * Returns the tail of the rendered text from {@code beginIndex}
     * inclusive through the last code unit. An index of zero returns the
     * rendered string itself, and an index equal to the length returns
     * an empty string.
     * <p>
     * The text comes from {@link #toString()}. When this instance may cache
     * its rendering, the stored text is reused; otherwise the literal segments
     * and values are interpolated on every call.
     *
     * @param beginIndex first rendered index to keep, inclusive
     * @return the tail of the rendered text, or the rendered string when
     *     {@code beginIndex} is zero
     * @throws StringIndexOutOfBoundsException if {@code beginIndex} is
     *     negative or greater than the rendered length
     */
    public String substring(int beginIndex) {
        return toString().substring(beginIndex);
    }

    /**
     * Returns the slice of the rendered text from {@code beginIndex}
     * inclusive to {@code endIndex} exclusive. A range that covers the
     * whole rendering returns that rendered string itself.
     * <p>
     * The text comes from {@link #toString()}. When this instance may cache
     * its rendering, the stored text is reused; otherwise the literal segments
     * and values are interpolated on every call.
     *
     * @param beginIndex first rendered index to keep, inclusive
     * @param endIndex rendered index at which the slice stops, exclusive
     * @return the requested slice, or the rendered string when the range
     *     covers the whole text
     * @throws StringIndexOutOfBoundsException if {@code beginIndex} is
     *     negative, {@code endIndex} is greater than the rendered length,
     *     or {@code beginIndex} is greater than {@code endIndex}
     */
    public String substring(int beginIndex, int endIndex) {
        return toString().substring(beginIndex, endIndex);
    }

    /**
     * Returns the rendered text followed by {@code str}. An empty
     * {@code str} returns the rendered string itself.
     * <p>
     * The text comes from {@link #toString()}. When this instance may cache
     * its rendering, the stored text is reused; otherwise the literal segments
     * and values are interpolated on every call.
     *
     * @param str text appended after the rendered text
     * @return the rendered text with {@code str} appended, or the rendered
     *     string when {@code str} is empty
     * @throws NullPointerException if {@code str} is null
     */
    public String concat(String str) {
        return toString().concat(str);
    }

    /**
     * Returns the rendered text with every {@code oldChar} replaced by
     * {@code newChar}. When {@code oldChar} does not occur, or the two
     * characters are equal, the rendered string is returned unchanged.
     * <p>
     * The text comes from {@link #toString()}. When this instance may cache
     * its rendering, the stored text is reused; otherwise the literal segments
     * and values are interpolated on every call.
     *
     * @param oldChar code unit to replace
     * @param newChar code unit written in its place
     * @return the rendered text with those replacements, or the rendered
     *     string when no code unit changes
     */
    public String replace(char oldChar, char newChar) {
        return toString().replace(oldChar, newChar);
    }

    /**
     * Reports whether {@code regex} matches the whole rendered text.
     * A pattern that matches only a substring yields {@code false}.
     * The pattern is compiled for this call.
     * <p>
     * The text comes from {@link #toString()}. When this instance may cache
     * its rendering, the stored text is reused; otherwise the literal segments
     * and values are interpolated on every call.
     *
     * @param regex regular expression matched against the entire rendering
     * @return {@code true} when the pattern matches the whole rendered text
     * @throws java.util.regex.PatternSyntaxException if {@code regex}
     *     cannot be compiled
     * @throws NullPointerException if {@code regex} is null
     */
    public boolean matches(String regex) {
        return toString().matches(regex);
    }

    /**
     * Reports whether {@code s} occurs in the rendered text.
     * An empty sequence occurs at the start.
     * <p>
     * The text comes from {@link #toString()}. When this instance may cache
     * its rendering, the stored text is reused; otherwise the literal segments
     * and values are interpolated on every call.
     *
     * @param s sequence to search for
     * @return {@code true} when {@code s} occurs in the rendered text
     * @throws NullPointerException if {@code s} is null
     */
    public boolean contains(CharSequence s) {
        return toString().contains(s);
    }

    /**
     * Returns the rendered text with the first substring matched by
     * {@code regex} replaced by {@code replacement}. A dollar sign or
     * backslash in {@code replacement} is a matcher substitution, not
     * a literal character. A pattern that matches nothing returns the
     * rendered string unchanged.
     * <p>
     * The text comes from {@link #toString()}. When this instance may cache
     * its rendering, the stored text is reused; otherwise the literal segments
     * and values are interpolated on every call.
     *
     * @param regex regular expression whose first match is replaced
     * @param replacement text written over that match, with matcher
     *     substitution of dollar signs and backslashes
     * @return the rendered text after that one replacement
     * @throws java.util.regex.PatternSyntaxException if {@code regex}
     *     cannot be compiled
     * @throws NullPointerException if {@code regex} or {@code replacement}
     *     is null
     */
    public String replaceFirst(String regex, String replacement) {
        return toString().replaceFirst(regex, replacement);
    }

    /**
     * Returns the rendered text with every substring matched by
     * {@code regex} replaced by {@code replacement}. A dollar sign or
     * backslash in {@code replacement} is a matcher substitution, not
     * a literal character. A pattern that matches nothing returns the
     * rendered string unchanged.
     * <p>
     * The text comes from {@link #toString()}. When this instance may cache
     * its rendering, the stored text is reused; otherwise the literal segments
     * and values are interpolated on every call.
     *
     * @param regex regular expression whose matches are replaced
     * @param replacement text written over each match, with matcher
     *     substitution of dollar signs and backslashes
     * @return the rendered text after every replacement
     * @throws java.util.regex.PatternSyntaxException if {@code regex}
     *     cannot be compiled
     * @throws NullPointerException if {@code regex} or {@code replacement}
     *     is null
     */
    public String replaceAll(String regex, String replacement) {
        return toString().replaceAll(regex, replacement);
    }

    /**
     * Returns the rendered text with every occurrence of the literal
     * sequence {@code target} replaced by {@code replacement}. The
     * search is not a regular expression, so a dollar sign in
     * {@code replacement} stays a dollar sign. An empty {@code target}
     * is written between every code unit, including both ends. A
     * {@code target} that does not occur returns the rendered string
     * unchanged.
     * <p>
     * The text comes from {@link #toString()}. When this instance may cache
     * its rendering, the stored text is reused; otherwise the literal segments
     * and values are interpolated on every call.
     *
     * @param target literal sequence to replace
     * @param replacement literal sequence written in its place
     * @return the rendered text after those literal replacements
     * @throws NullPointerException if {@code target} or {@code replacement}
     *     is null
     */
    public String replace(CharSequence target, CharSequence replacement) {
        return toString().replace(target, replacement);
    }

    /**
     * Splits the rendered text around matches of {@code regex}. A
     * positive {@code limit} applies the pattern at most
     * {@code limit - 1} times and leaves the unmatched tail in the last
     * element. A limit of zero applies the pattern as many times as
     * possible and drops trailing empty strings. A negative limit
     * applies the pattern as many times as possible and keeps trailing
     * empty strings. When the pattern matches nothing, the result is a
     * one-element array holding the rendered text.
     * <p>
     * The text comes from {@link #toString()}. When this instance may cache
     * its rendering, the stored text is reused; otherwise the literal segments
     * and values are interpolated on every call.
     *
     * @param regex regular expression that separates the rendered text
     * @param limit maximum number of applications of the pattern, with
     *     the zero and negative cases above
     * @return the segments produced by that split
     * @throws java.util.regex.PatternSyntaxException if {@code regex}
     *     cannot be compiled
     * @throws NullPointerException if {@code regex} is null
     */
    public String[] split(String regex, int limit) {
        return toString().split(regex, limit);
    }

    /**
     * Splits the rendered text around matches of {@code regex} and
     * drops trailing empty strings. The split uses a limit of zero.
     * When the pattern matches nothing, the result is a one-element
     * array holding the rendered text.
     * <p>
     * The text comes from {@link #toString()}. When this instance may cache
     * its rendering, the stored text is reused; otherwise the literal segments
     * and values are interpolated on every call.
     *
     * @param regex regular expression that separates the rendered text
     * @return the segments, without trailing empty strings
     * @throws java.util.regex.PatternSyntaxException if {@code regex}
     *     cannot be compiled
     * @throws NullPointerException if {@code regex} is null
     */
    public String[] split(String regex) {
        return toString().split(regex);
    }

    /**
     * Returns the rendered text converted to lower case with the
     * case-mapping rules of {@code locale}. Those rules can change the
     * length of the text.
     * <p>
     * The text comes from {@link #toString()}. When this instance may cache
     * its rendering, the stored text is reused; otherwise the literal segments
     * and values are interpolated on every call.
     *
     * @param locale locale whose case mappings are applied
     * @return the lower-case form of the rendered text
     * @throws NullPointerException if {@code locale} is null
     */
    public String toLowerCase(Locale locale) {
        return toString().toLowerCase(locale);
    }

    /**
     * Returns the rendered text converted to lower case with the
     * case-mapping rules of {@link Locale#getDefault()} at the time of
     * the call. Those rules can change the length of the text.
     * <p>
     * The text comes from {@link #toString()}. When this instance may cache
     * its rendering, the stored text is reused; otherwise the literal segments
     * and values are interpolated on every call.
     *
     * @return the lower-case form of the rendered text
     */
    public String toLowerCase() {
        return toString().toLowerCase();
    }

    /**
     * Returns the rendered text converted to upper case with the
     * case-mapping rules of {@code locale}. Those rules can change the
     * length of the text.
     * <p>
     * The text comes from {@link #toString()}. When this instance may cache
     * its rendering, the stored text is reused; otherwise the literal segments
     * and values are interpolated on every call.
     *
     * @param locale locale whose case mappings are applied
     * @return the upper-case form of the rendered text
     * @throws NullPointerException if {@code locale} is null
     */
    public String toUpperCase(Locale locale) {
        return toString().toUpperCase(locale);
    }

    /**
     * Returns the rendered text converted to upper case with the
     * case-mapping rules of {@link Locale#getDefault()} at the time of
     * the call. Those rules can change the length of the text.
     * <p>
     * The text comes from {@link #toString()}. When this instance may cache
     * its rendering, the stored text is reused; otherwise the literal segments
     * and values are interpolated on every call.
     *
     * @return the upper-case form of the rendered text
     */
    public String toUpperCase() {
        return toString().toUpperCase();
    }

    /**
     * Copies the UTF-16 code units of the rendered text into a new
     * array. Later writes to that array do not change this GString or
     * the rendered string.
     * <p>
     * The text comes from {@link #toString()}. When this instance may cache
     * its rendering, the stored text is reused; otherwise the literal segments
     * and values are interpolated on every call.
     *
     * @return a new array holding the rendered code units
     */
    public char[] toCharArray() {
        return toString().toCharArray();
    }

    /**
     * Returns the pooled instance of the rendered text. The rendering
     * is installed in the pool when no equal string is already there,
     * and an equal string that was already pooled is the object
     * returned.
     * <p>
     * The text comes from {@link #toString()}. When this instance may cache
     * its rendering, the stored text is reused; otherwise the literal segments
     * and values are interpolated on every call.
     *
     * @return the pooled string equal to the rendered text
     */
    public String intern() {
        return toString().intern();
    }

    @Override
    public String toString() {
        if (null != cachedStringLiteral) {
            return cachedStringLiteral;
        }
        String str = super.toString();
        if (cacheable) {
            cachedStringLiteral = str;
        }
        return str;
    }

    private static boolean checkValuesStringConstant(Object[] values) {
        for (Object value : values) {
            if (null == value) continue;
            if (!(ImmutableTypes.builtinOrMarkedImmutableClass(value.getClass())
                    || toStringMarkedPure(value.getClass())
                    || (value instanceof GStringImpl && ((GStringImpl) value).cacheable))) {
                return false;
            }
        }

        return true;
    }

    private static boolean toStringMarkedPure(Class<?> clazz) {
        Method toStringMethod;
        try {
            toStringMethod = clazz.getMethod("toString");
        } catch (NoSuchMethodException | SecurityException e) {
            return false;
        }
        return toStringMethod.isAnnotationPresent(Pure.class);
    }
}
