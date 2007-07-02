/*
 * Copyright 2003-2007 the original author or authors.
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
 */
package org.codehaus.groovy.tools.groovydoc;

import java.text.BreakIterator;
import java.util.Locale;

import org.codehaus.groovy.groovydoc.*;

public class SimpleGroovyDoc implements GroovyDoc {
	public SimpleGroovyDoc(String name) {
		this.name = name;
	}
	private String name;
	private String commentText;
	private String rawCommentText;
	private String firstSentenceCommentText;
	public String name() {
		return name;
	}

	public String toString() {
		return "" + getClass() + "(" + name + ")";
	}
	public String commentText() {
		return commentText; // derived from rawCommentText
	}
	public String getRawCommentText() {
		return rawCommentText;
	}
	public String firstSentenceCommentText() {
		return firstSentenceCommentText; // derived from rawCommentText
	}

	public void setRawCommentText(String rawCommentText) {
		this.rawCommentText = rawCommentText;
		
		// remove all the * from beginning of lines
		this.commentText = rawCommentText.replaceAll("(?m)^\\s*\\*", ""); // todo precompile regex Patterns

		// Comment Summary using first sentence (Locale sensitive)
		BreakIterator boundary = BreakIterator.getSentenceInstance(Locale.getDefault()); // todo - allow locale to be passed in
        boundary.setText(commentText);
        int start = boundary.first();
        int end = boundary.next();
        if (start > -1 && end > -1) {
        	// need to abbreviate this comment for the summary
        	this.firstSentenceCommentText = commentText.substring(start,end);
        } else {
        	this.firstSentenceCommentText = commentText;
        }
		// hack to reformat groovydoc tags into html (todo: tags)
		this.commentText = this.commentText.replaceAll("(?m)@([a-z]*)\\s*(.*)$","<DL><DT><B>$1:</B></DT><DD>$2</DD></DL>");			// note: use of $ here is a reference to a subsequence (as defined in Matcher.appendReplacement())

		// hack to hide groovydoc tags in summaries
		this.firstSentenceCommentText = this.firstSentenceCommentText.replaceAll("(?m)@([a-z]*\\s*.*)$",""); // remove @return etc from summaries
        
	}

	
	
	
	// Methods from Comparable
	public int compareTo(Object that) {
		if (that instanceof SimpleGroovyDoc) {
			return name.compareTo(((SimpleGroovyDoc) that).name);
		} else {
			throw new ClassCastException();
		}
	}

	// Methods from GroovyDoc
//	public GroovyTag[] firstSentenceTags() {/*todo*/return null;}
//	public GroovyTag[] inlineTags() {/*todo*/return null;}
	public boolean isAnnotationType() {/*todo*/return false;}
	public boolean isAnnotationTypeElement() {/*todo*/return false;}
	public boolean isClass() {/*todo*/return false;}
	public boolean isConstructor() {/*todo*/return false;}
	public boolean isEnum() {/*todo*/return false;}
	public boolean isEnumConstant() {/*todo*/return false;}
	public boolean isError() {/*todo*/return false;}
	public boolean isException() {/*todo*/return false;}
	public boolean isField() {/*todo*/return false;}
	public boolean isIncluded() {/*todo*/return false;}
	public boolean isInterface() {/*todo*/return false;}
	public boolean isMethod() {/*todo*/return false;}
	public boolean isOrdinaryClass() {/*todo*/return false;}
//	public GroovySourcePosition position() {/*todo*/return null;}
//	public GroovySeeTag[] seeTags() {/*todo*/return null;}
//	public GroovyTag[] tags() {/*todo*/return null;}
//	public GroovyTag[] tags(String arg0) {/*todo*/return null;}

}
