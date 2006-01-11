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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * NOTE: Based on work done by on the GSP standalone project (https://gsp.dev.java.net/)
 *
 * Utility class to strip HTML from around tags that specify it.
 *
 * @author Troy Heninger
 * Date: Jan 10, 2004
 *
 */
class Strip {
	private static Pattern stripTag = Pattern.compile("\\^([a-zA-Z]+)%\\{([^}]|\\}[^%])*\\}%");
	private static Pattern anyTag = Pattern.compile("((\\^[a-zA-Z])?%\\{([^}]|\\}[^%])*\\}%|[$@]\\{[^}]*\\})");

	private StringBuffer text;

	Strip(CharSequence text) {
		this.text = new StringBuffer(text.toString());
	} // Scan()

	void strip(int index) {
		Matcher match = stripTag.matcher(text);
		if (match.find(index)) {
			strip(match.end());
			String tag = match.group(1);
			int start = match.start() + 1 + tag.length(); // begin after '^tag'; at the '%{'
			int end = match.end();
			Pattern patAfter = Pattern.compile("</" + tag + "(>|[^>a-zA-Z][^>]*>)\\s*", Pattern.CASE_INSENSITIVE);
			Matcher matchAfter = patAfter.matcher(text);
			if (matchAfter.find(end)) {
				int end2 = matchAfter.end();
				Matcher matchAny = anyTag.matcher(text.subSequence(0, end2));
				if (matchAny.find(end)) end2 = matchAny.start();
				Pattern nextTagPat = Pattern.compile("<" + tag + "(\\s|>)", Pattern.CASE_INSENSITIVE);
				Matcher matchNext = nextTagPat.matcher(text.subSequence(0, end2));
				if (matchNext.find(end)) end2 = matchNext.start();
					// System.out.println("Stripping " + text.subSequence(end, end2));
				text.delete(end, end2);
			}
			Pattern patBefore = Pattern.compile(new Reverse("*s\\<" + tag).toString(),
					Pattern.CASE_INSENSITIVE);
			Matcher matchBefore = patBefore.matcher(new Reverse(text, 0, start));
			if (matchBefore.find()) {
				int start2 = start - matchBefore.end();
				Matcher matchAny = anyTag.matcher(text.subSequence(0, start));
				if (matchAny.find(start2)) start2 = matchAny.end();
					// System.out.println("Stripping " + text.subSequence(start2, start));
				text.delete(start2, start);
			}
		}
	} // strip()

	public String toString() {
		return text.toString();
	}

} // Strip
