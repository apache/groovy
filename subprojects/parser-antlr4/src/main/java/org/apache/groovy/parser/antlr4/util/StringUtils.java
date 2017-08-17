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
        Pattern p = Pattern.compile("(\\\\*)\\\\u+([0-9abcdefABCDEF]{4})");
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

		return result.replace("\\\\", "\\");
    }

	public static final int NONE_SLASHY = 0;
	public static final int SLASHY = 1;
	public static final int DOLLAR_SLASHY = 2;

	public static String replaceEscapes(String text, int slashyType) {
		if (slashyType == SLASHY || slashyType == DOLLAR_SLASHY) {
			text = StringUtils.replaceHexEscapes(text);
			text = StringUtils.replaceLineEscape(text);

			if (slashyType == SLASHY) {
				text = text.replace("\\/", "/");
			}

			if (slashyType == DOLLAR_SLASHY) {
				text = text.replace("$$", "$");
				text = text.replace("$/", "/");
			}

		} else if (slashyType == NONE_SLASHY) {
			text = StringUtils.replaceEscapes(text);
		} else {
			throw new IllegalArgumentException("Invalid slashyType: " + slashyType);
		}

		return text;
	}

	private static String replaceEscapes(String text) {
		text = text.replace("\\$", "$");

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
        return text.replace("\r\n", "\n");
    }

	public static long countChar(String text, char c) {
		int length = text.length();
		int count = 0;
		for(int i = 0; i<length; ++i) {
			if (text.charAt(i) == c) count++;
		}
		return count; // return text.chars().filter(e -> c == e).count();
	}

	public static String trimQuotations(String text, int quotationLength) {
		int length = text.length();

		return length == quotationLength * 2 ? "" : text.substring(quotationLength, length - quotationLength);
	}
}