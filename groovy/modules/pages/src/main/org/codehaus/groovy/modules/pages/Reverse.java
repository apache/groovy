package org.codehaus.groovy.modules.pages;

/**
 * Created by IntelliJ IDEA.
 * Author: Troy Heninger
 * Date: Jan 16, 2004
 * Utility class to reverse a char sequence.
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
