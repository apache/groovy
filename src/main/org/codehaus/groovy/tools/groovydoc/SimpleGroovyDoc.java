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
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.groovy.groovydoc.*;
import org.codehaus.groovy.ant.Groovydoc;

public class SimpleGroovyDoc implements GroovyDoc {
	private String name;
	private String commentText;
	private String rawCommentText;
	private String firstSentenceCommentText;
    private List links;
    private int definitionType;

    private final int CLASS = 0;
    private final int INTERFACE = 1;

    public SimpleGroovyDoc(String name, List links) {
        this.name = name;
        this.links = links;
        this.setRawCommentText("");  // default to no comments (good for default constructors which will not have a reason to call this)
        definitionType = CLASS; // default this instance to a class, unless told otherwise
    }

    public SimpleGroovyDoc(String name) {
        this(name, new ArrayList());
    }

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
		this.commentText = replaceAllTags(this.commentText, "(?m)@([a-z]*)\\s*(.*)$",
                "<DL><DT><B>$1:</B></DT><DD>", "</DD></DL>");

		// hack to hide groovydoc tags in summaries
		this.firstSentenceCommentText = this.firstSentenceCommentText.replaceAll("(?m)@([a-z]*\\s*.*)$",""); // remove @return etc from summaries
        
	}

    // TODO: this should go away once we have tags
    public String replaceAllTags(String self, String regex, String s1, String s2) {
        Matcher matcher = Pattern.compile(regex).matcher(self);
        if (matcher.find()) {
            matcher.reset();
            StringBuffer sb = new StringBuffer();
            while (matcher.find()) {
                if (matcher.group(1).equals("see")) {
                    // TODO: escape $ signs?
                    matcher.appendReplacement(sb, s1 + getDocUrl(matcher.group(2)) + s2);
                } else {
                    matcher.appendReplacement(sb, s1 + "$2" + s2);
                }
            }
            matcher.appendTail(sb);
            return sb.toString();
        } else {
            return self;
        }
    }

    public String getDocUrl(String type) {
        if (type == null || type.indexOf('.') == -1)
            return type;

        final String[] target = type.split("#");
        String shortClassName = target[0].replaceAll(".*\\.", "");
        String packageName = type.substring(0, type.length()-shortClassName.length()-2);
        shortClassName += (target.length > 1 ? "#" + target[1].split("\\(")[0] : "");
        for (int i = 0; i < links.size(); i++) {
            Groovydoc.LinkArgument linkArgument = (Groovydoc.LinkArgument) links.get(i);
            final StringTokenizer tokenizer = new StringTokenizer(linkArgument.getPackages(), ", ");
            while (tokenizer.hasMoreTokens()) {
                final String token = tokenizer.nextToken();
                if (type.startsWith(token)) {
                    String apiBaseUrl = linkArgument.getHref();
                    if (!apiBaseUrl.endsWith("/")) { apiBaseUrl += "/"; }
                    String url = apiBaseUrl + target[0].replaceAll("\\.", "/") + ".html" + (target.length > 1 ? "#" + target[1] : "");
                    return "<a href='" + url + "' title='" + shortClassName + "'>" + shortClassName + "</a>";
                }
            }
        }
        return type;
    }

    public boolean isClass() {
        return definitionType == CLASS;
    }
    public boolean isInterface() {
        return definitionType == INTERFACE;
    }
    public void setAsInterfaceDefinition() {
        definitionType = INTERFACE;
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
	public boolean isConstructor() {/*todo*/return false;}
	public boolean isEnum() {/*todo*/return false;}
	public boolean isEnumConstant() {/*todo*/return false;}
	public boolean isError() {/*todo*/return false;}
	public boolean isException() {/*todo*/return false;}
	public boolean isField() {/*todo*/return false;}
	public boolean isIncluded() {/*todo*/return false;}
	public boolean isMethod() {/*todo*/return false;}
	public boolean isOrdinaryClass() {/*todo*/return false;}
//	public GroovySourcePosition position() {/*todo*/return null;}
//	public GroovySeeTag[] seeTags() {/*todo*/return null;}
//	public GroovyTag[] tags() {/*todo*/return null;}
//	public GroovyTag[] tags(String arg0) {/*todo*/return null;}

}
