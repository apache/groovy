/*
 * Copyright 2004-2005 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.grails.web.pages;
/**
 * NOTE: Based on work done by on the GSP standalone project (https://gsp.dev.java.net/)
 *
 * Utility class to reverse a char sequence.
 *
 * @author Troy Heninger
 * Date: Jan 10, 2004
 *
 */
class Reverse implements CharSequence {
	private CharSequence text;
	private int start, end, anchor;

	Reverse(CharSequence text) {
		this(text, 0, text.length());
	}

	Reverse(CharSequence text, int start, int end) {
		this.text = text;
		this.start = start;
		this.end = end;
		anchor = end - 1;
	}
	public char charAt(int index) {
		return text.charAt(anchor - index);
	}

	public int length() {
		return end - start;
	}

	public CharSequence subSequence(int start, int end) {
		return new Reverse(text, anchor - end, anchor - start);
	}

	public String toString() {
		int len = length();
		StringBuffer buf = new StringBuffer(len);
		for (int ix = anchor; ix >= start; ix--) {
			buf.append(text.charAt(ix));
		}
		return buf.toString();
	}
} // Reverse
