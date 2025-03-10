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
package org.apache.groovy.parser.antlr4.util;

import org.apache.groovy.util.Maps;

import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utilities for handling strings
 *
 */
public class StringUtils {
    public static final int NONE_SLASHY = 0;
    public static final int SLASHY = 1;
    public static final int DOLLAR_SLASHY = 2;

    public static String replaceEscapes(String text, int slashyType) {
        if (slashyType == NONE_SLASHY) {
            text = replaceEscapes(text);
        } else if (slashyType == SLASHY || slashyType == DOLLAR_SLASHY) {
            text = replaceHexEscapes(text);
            text = replaceLineEscape(text);

            if (slashyType == SLASHY) {
                text = replace(text,"\\/", "/");
            } else if (slashyType == DOLLAR_SLASHY) {
                text = replace(text,"$/", "/");
                text = replace(text,"$$", "$");
            }
        } else  {
            throw new IllegalArgumentException("Invalid slashyType: " + slashyType);
        }

        return text;
    }

    public static String replaceHexEscapes(String text) {
		if (!text.contains(BACKSLASH)) return text;
		return replaceAll(text, HEX_ESCAPES_PATTERN, m -> {
            final String _0 = m.group(0), _1 = m.group(1), _2 = m.group(2);
            if (isLengthOdd(_1)) return _0;
            return _1 + new String(Character.toChars(Integer.parseInt(_2, 16)));
        });
	}

	public static String replaceOctalEscapes(String text) {
		if (!text.contains(BACKSLASH)) return text;
		return replaceAll(text, OCTAL_ESCAPES_PATTERN, m -> {
            final String _0 = m.group(0), _1 = m.group(1), _2 = m.group(2);
            if (isLengthOdd(_1)) return _0;
            return _1 + new String(Character.toChars(Integer.parseInt(_2, 8)));
        });
	}

    public static String replaceStandardEscapes(String text) {
		if (!text.contains(BACKSLASH)) return text;
		final String result = replaceAll(text, STANDARD_ESCAPES_PATTERN, m -> {
            final String _0 = m.group(0), _1 = m.group(1), _2 = m.group(2);
            if (isLengthOdd(_1)) return _0;
            final Character character = STANDARD_ESCAPES.get(_2.charAt(0));
            return _1 + (character != null ? character : _2);
        });
		return replace(result,"\\\\", "\\");
	}

	public static String removeCR(String text) {
		return replace(text,"\r\n", "\n");
	}

	public static long countChar(String text, char c) {
		return text.chars().filter(e -> c == e).count();
	}

	public static String trimQuotations(String text, int quotationLength) {
		int length = text.length();
		return length == quotationLength << 1 ? "" : text.substring(quotationLength, length - quotationLength);
	}

	public static boolean matches(String text, Pattern pattern) {
		return pattern.matcher(text).matches();
	}

	/**
	 * The modified implementation is based on StringUtils#replace(String text, String searchString, String replacement, int max), Apache commons-lang3-3.6
	 *
	 * <p>Replaces all occurrences of a String within another String.</p>
	 *
	 * <p>A {@code null} reference passed to this method is a no-op.</p>
	 *
	 * <pre>
	 * StringUtils.replace(null, *, *)        = null
	 * StringUtils.replace("", *, *)          = ""
	 * StringUtils.replace("any", null, *)    = "any"
	 * StringUtils.replace("any", *, null)    = "any"
	 * StringUtils.replace("any", "", *)      = "any"
	 * StringUtils.replace("aba", "a", null)  = "aba"
	 * StringUtils.replace("aba", "a", "")    = "b"
	 * StringUtils.replace("aba", "a", "z")   = "zbz"
	 * </pre>
	 *
	 * @param text  text to search and replace in, may be null
	 * @param searchString  the String to search for, may be null
	 * @param replacement  the String to replace it with, may be null
	 * @return the text with any replacements processed,
	 *  {@code null} if null String input
	 */
	public static String replace(final String text, String searchString, final String replacement) {
		if (isEmpty(text) || isEmpty(searchString) || replacement == null) return text;

		int start = 0;
		int end = text.indexOf(searchString, start);
		if (end == INDEX_NOT_FOUND) {
			return text;
		}
		final int replLength = searchString.length();
		int increase = replacement.length() - replLength;
		increase = (increase < 0 ? 0 : increase) * 16;
		final StringBuilder buf = new StringBuilder(text.length() + increase);
		while (end != INDEX_NOT_FOUND) {
			buf.append(text, start, end).append(replacement);
			start = end + replLength;
			end = text.indexOf(searchString, start);
		}
		buf.append(text, start, text.length());
		return buf.toString();
	}

	/**
	 * Copied from Apache commons-lang3-3.6
	 *
	 * <p>Checks if a CharSequence is empty ("") or null.</p>
	 *
	 * <pre>
	 * StringUtils.isEmpty(null)      = true
	 * StringUtils.isEmpty("")        = true
	 * StringUtils.isEmpty(" ")       = false
	 * StringUtils.isEmpty("bob")     = false
	 * StringUtils.isEmpty("  bob  ") = false
	 * </pre>
	 *
	 * <p>NOTE: This method changed in Lang version 2.0.
	 * It no longer trims the CharSequence.
	 * That functionality is available in isBlank().</p>
	 *
	 * @param cs  the CharSequence to check, may be null
	 * @return {@code true} if the CharSequence is empty or null
	 */
	public static boolean isEmpty(final CharSequence cs) {
		return cs == null || cs.length() == 0;
	}

    private static String replaceEscapes(String text) {
        if (!text.contains(BACKSLASH)) return text;
        text = replace(text,"\\$", "$");
        text = replaceLineEscape(text);
        return replaceStandardEscapes(replaceHexEscapes(replaceOctalEscapes(text)));
    }

    private static String replaceLineEscape(String text) {
        if (!text.contains(BACKSLASH)) return text;
        text = replaceAll(text, LINE_ESCAPE_PATTERN, m -> {
            final String _0 = m.group(0), _1 = m.group(1);
            if (isLengthOdd(_1)) return _0;
            return _1;
        });
        return text;
    }

    private static String replaceAll(final CharSequence input, final Pattern pattern, final Function<? super Matcher, String> replaceFunction) {
        final Matcher matcher = pattern.matcher(input);
        final StringBuilder result = new StringBuilder(input.length() + matcher.groupCount() * 8);
        while (matcher.find()) {
            final String replacement = replaceFunction.apply(matcher);
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private static boolean isLengthOdd(String str) {
        return null != str && str.length() % 2 == 1;
    }

    /**
	 * Copied from Apache commons-lang3-3.6
	 *
	 * Represents a failed index search.
	 */
	private static final int INDEX_NOT_FOUND = -1;
    private static final String BACKSLASH = "\\";
    private static final Pattern HEX_ESCAPES_PATTERN = Pattern.compile("(\\\\*)\\\\u([0-9abcdefABCDEF]{4})");
    private static final Pattern OCTAL_ESCAPES_PATTERN = Pattern.compile("(\\\\*)\\\\([0-3]?[0-7]?[0-7])");
    private static final Pattern STANDARD_ESCAPES_PATTERN = Pattern.compile("(\\\\*)\\\\([btnfrs\"'])");
    private static final Pattern LINE_ESCAPE_PATTERN = Pattern.compile("(\\\\*)\\\\\r?\n");
    private static final Map<Character, Character> STANDARD_ESCAPES = Maps.of(
        'b', '\b',
        't', '\t',
        'n', '\n',
        'f', '\f',
        'r', '\r',
        's', ' '
    );
}
