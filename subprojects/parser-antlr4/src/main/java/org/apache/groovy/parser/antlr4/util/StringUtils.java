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

import groovy.lang.Closure;
import org.apache.groovy.util.Maps;
import org.codehaus.groovy.runtime.StringGroovyMethods;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * Utilities for handling strings
 *
 * @author  <a href="mailto:realbluesun@hotmail.com">Daniel.Sun</a>
 * Created on    2016/08/20
 */
public class StringUtils {
	public static String replaceHexEscapes(String text) {
		Pattern p = Pattern.compile("(\\\\*)\\\\u([0-9abcdefABCDEF]{4})");
		return StringGroovyMethods.replaceAll((CharSequence) text, p, new Closure<Void>(null, null) {
			Object doCall(String _0, String _1, String _2) {
				if (isLengthOdd(_1)) {
					return _0;
				}

				return _1 + new String(Character.toChars(Integer.parseInt(_2, 16)));
			}
		});
	}

	public static String replaceOctalEscapes(String text) {
		Pattern p = Pattern.compile("(\\\\*)\\\\([0-3]?[0-7]?[0-7])");
		return StringGroovyMethods.replaceAll((CharSequence) text, p, new Closure<Void>(null, null) {
			Object doCall(String _0, String _1, String _2) {
				if (isLengthOdd(_1)) {
					return _0;
				}

				return _1 + new String(Character.toChars(Integer.parseInt(_2, 8)));
			}
		});
	}

	private static final Map<Character, Character> STANDARD_ESCAPES = Maps.of(
			'b', '\b',
			't', '\t',
			'n', '\n',
			'f', '\f',
			'r', '\r'
	);

	public static String replaceStandardEscapes(String text) {
		Pattern p = Pattern.compile("(\\\\*)\\\\([btnfr\"'])");

		String result = StringGroovyMethods.replaceAll((CharSequence) text, p, new Closure<Void>(null, null) {
			Object doCall(String _0, String _1, String _2) {
				if (isLengthOdd(_1)) {
					return _0;
				}

				Character character = STANDARD_ESCAPES.get(_2.charAt(0));
				return _1 + (character != null ? character : _2);
			}
		});

		return replace(result,"\\\\", "\\");
	}

	public static final int NONE_SLASHY = 0;
	public static final int SLASHY = 1;
	public static final int DOLLAR_SLASHY = 2;

	public static String replaceEscapes(String text, int slashyType) {
		if (slashyType == SLASHY || slashyType == DOLLAR_SLASHY) {
			text = StringUtils.replaceHexEscapes(text);
			text = StringUtils.replaceLineEscape(text);

			if (slashyType == SLASHY) {
				text = replace(text,"\\/", "/");
			}

			if (slashyType == DOLLAR_SLASHY) {
				text = replace(text,"$$", "$");
				text = replace(text,"$/", "/");
			}

		} else if (slashyType == NONE_SLASHY) {
			text = StringUtils.replaceEscapes(text);
		} else {
			throw new IllegalArgumentException("Invalid slashyType: " + slashyType);
		}

		return text;
	}

	private static String replaceEscapes(String text) {
		text = replace(text,"\\$", "$");

		text = StringUtils.replaceLineEscape(text);

		return StringUtils.replaceStandardEscapes(replaceHexEscapes(replaceOctalEscapes(text)));
	}

	private static String replaceLineEscape(String text) {
		Pattern p = Pattern.compile("(\\\\*)\\\\\r?\n");
		text = StringGroovyMethods.replaceAll((CharSequence) text, p, new Closure<Void>(null, null) {
			Object doCall(String _0, String _1) {
				if (isLengthOdd(_1)) {
					return _0;
				}

				return _1;
			}
		});

		return text;
	}

	private static boolean isLengthOdd(String str) {
		return null != str && str.length() % 2 == 1;
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


	/**
	 * Copied from Apache commons-lang3-3.6
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
	 * @see #replace(String text, String searchString, String replacement, int max)
	 * @param text  text to search and replace in, may be null
	 * @param searchString  the String to search for, may be null
	 * @param replacement  the String to replace it with, may be null
	 * @return the text with any replacements processed,
	 *  {@code null} if null String input
	 */
	public static String replace(final String text, final String searchString, final String replacement) {
		return replace(text, searchString, replacement, -1);
	}

	/**
	 * Copied from Apache commons-lang3-3.6
	 *
	 * <p>Replaces a String with another String inside a larger String,
	 * for the first {@code max} values of the search String.</p>
	 *
	 * <p>A {@code null} reference passed to this method is a no-op.</p>
	 *
	 * <pre>
	 * StringUtils.replace(null, *, *, *)         = null
	 * StringUtils.replace("", *, *, *)           = ""
	 * StringUtils.replace("any", null, *, *)     = "any"
	 * StringUtils.replace("any", *, null, *)     = "any"
	 * StringUtils.replace("any", "", *, *)       = "any"
	 * StringUtils.replace("any", *, *, 0)        = "any"
	 * StringUtils.replace("abaa", "a", null, -1) = "abaa"
	 * StringUtils.replace("abaa", "a", "", -1)   = "b"
	 * StringUtils.replace("abaa", "a", "z", 0)   = "abaa"
	 * StringUtils.replace("abaa", "a", "z", 1)   = "zbaa"
	 * StringUtils.replace("abaa", "a", "z", 2)   = "zbza"
	 * StringUtils.replace("abaa", "a", "z", -1)  = "zbzz"
	 * </pre>
	 *
	 * @param text  text to search and replace in, may be null
	 * @param searchString  the String to search for, may be null
	 * @param replacement  the String to replace it with, may be null
	 * @param max  maximum number of values to replace, or {@code -1} if no maximum
	 * @return the text with any replacements processed,
	 *  {@code null} if null String input
	 */
	public static String replace(final String text, final String searchString, final String replacement, final int max) {
		return replace(text, searchString, replacement, max, false);
	}

	/**
	 * Copied from Apache commons-lang3-3.6
	 *
	 * <p>Replaces a String with another String inside a larger String,
	 * for the first {@code max} values of the search String,
	 * case sensitively/insensisitively based on {@code ignoreCase} value.</p>
	 *
	 * <p>A {@code null} reference passed to this method is a no-op.</p>
	 *
	 * <pre>
	 * StringUtils.replace(null, *, *, *, false)         = null
	 * StringUtils.replace("", *, *, *, false)           = ""
	 * StringUtils.replace("any", null, *, *, false)     = "any"
	 * StringUtils.replace("any", *, null, *, false)     = "any"
	 * StringUtils.replace("any", "", *, *, false)       = "any"
	 * StringUtils.replace("any", *, *, 0, false)        = "any"
	 * StringUtils.replace("abaa", "a", null, -1, false) = "abaa"
	 * StringUtils.replace("abaa", "a", "", -1, false)   = "b"
	 * StringUtils.replace("abaa", "a", "z", 0, false)   = "abaa"
	 * StringUtils.replace("abaa", "A", "z", 1, false)   = "abaa"
	 * StringUtils.replace("abaa", "A", "z", 1, true)   = "zbaa"
	 * StringUtils.replace("abAa", "a", "z", 2, true)   = "zbza"
	 * StringUtils.replace("abAa", "a", "z", -1, true)  = "zbzz"
	 * </pre>
	 *
	 * @param text  text to search and replace in, may be null
	 * @param searchString  the String to search for (case insensitive), may be null
	 * @param replacement  the String to replace it with, may be null
	 * @param max  maximum number of values to replace, or {@code -1} if no maximum
	 * @param ignoreCase if true replace is case insensitive, otherwise case sensitive
	 * @return the text with any replacements processed,
	 *  {@code null} if null String input
	 */
	private static String replace(final String text, String searchString, final String replacement, int max, final boolean ignoreCase) {
		if (isEmpty(text) || isEmpty(searchString) || replacement == null || max == 0) {
			return text;
		}
		String searchText = text;
		if (ignoreCase) {
			searchText = text.toLowerCase();
			searchString = searchString.toLowerCase();
		}
		int start = 0;
		int end = searchText.indexOf(searchString, start);
		if (end == INDEX_NOT_FOUND) {
			return text;
		}
		final int replLength = searchString.length();
		int increase = replacement.length() - replLength;
		increase = increase < 0 ? 0 : increase;
		increase *= max < 0 ? 16 : max > 64 ? 64 : max;
		final StringBuilder buf = new StringBuilder(text.length() + increase);
		while (end != INDEX_NOT_FOUND) {
			buf.append(text.substring(start, end)).append(replacement);
			start = end + replLength;
			if (--max == 0) {
				break;
			}
			end = searchText.indexOf(searchString, start);
		}
		buf.append(text.substring(start));
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
	 * @since 3.0 Changed signature from isEmpty(String) to isEmpty(CharSequence)
	 */
	public static boolean isEmpty(final CharSequence cs) {
		return cs == null || cs.length() == 0;
	}

	/**
	 * Copied from Apache commons-lang3-3.6
	 *
	 * Represents a failed index search.
	 */
	private static final int INDEX_NOT_FOUND = -1;
}