/*
 * Copyright 2003-2009 the original author or authors.
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

import org.codehaus.groovy.antlr.parser.GroovyTokenTypes;
import org.codehaus.groovy.groovydoc.GroovyDoc;

import java.text.BreakIterator;
import java.util.Locale;

public class SimpleGroovyDoc implements GroovyDoc, GroovyTokenTypes {
    private String name;
    private String commentText;
    private String rawCommentText = "";
    private String firstSentenceCommentText;
    private int definitionType;

    public SimpleGroovyDoc(String name) {
        this.name = name;
        definitionType = CLASS_DEF;
    }

    public String name() {
        return name;
    }

    public String toString() {
        return "" + getClass() + "(" + name + ")";
    }

    protected void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    protected void setFirstSentenceCommentText(String firstSentenceCommentText) {
        this.firstSentenceCommentText = firstSentenceCommentText;
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
        setFirstSentenceCommentText(calculateFirstSentence(rawCommentText));
    }

    private String calculateFirstSentence(String raw) {
        // remove all the * from beginning of lines
        String text = raw.replaceAll("(?m)^\\s*\\*", ""); // todo precompile regex
        // Comment Summary using first sentence (Locale sensitive)
        BreakIterator boundary = BreakIterator.getSentenceInstance(Locale.getDefault()); // todo - allow locale to be passed in
        boundary.setText(text);
        int start = boundary.first();
        int end = boundary.next();
        if (start > -1 && end > -1) {
            // need to abbreviate this comment for the summary
            text = text.substring(start, end);
        }
        // hide groovydoc tags in summaries
        return stripTags(text);
    }

    private String stripTags(String text) {
        return text.replaceAll("(?m)@([a-z]+\\s*.*)$", "");
    }

    public boolean isClass() {
        return definitionType == CLASS_DEF;
    }

    public boolean isInterface() {
        return definitionType == INTERFACE_DEF;
    }

    public boolean isAnnotationType() {
        return definitionType == ANNOTATION_DEF;
    }

    public boolean isEnum() {
        return definitionType == ENUM_DEF;
    }

    public String getTypeDescription() {
        if (isInterface()) return "Interface";
        if (isAnnotationType()) return "Annotation Type";
        if (isEnum()) return "Enum";
        return "Class";
    }

    public String getTypeSourceDescription() {
        if (isInterface()) return "interface";
        if (isAnnotationType()) return "@interface";
        if (isEnum()) return "enum";
        return "class";
    }

    public void setTokenType(int t) {
        definitionType = t;
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

    public boolean isAnnotationTypeElement() {/*todo*/
        return false;
    }

    public boolean isConstructor() {/*todo*/
        return false;
    }

    public boolean isEnumConstant() {/*todo*/
        return false;
    }

    public boolean isError() {/*todo*/
        return false;
    }

    public boolean isException() {/*todo*/
        return false;
    }

    public boolean isField() {/*todo*/
        return false;
    }

    public boolean isIncluded() {/*todo*/
        return false;
    }

    public boolean isMethod() {/*todo*/
        return false;
    }

    public boolean isOrdinaryClass() {/*todo*/
        return false;
    }
//	public GroovySourcePosition position() {/*todo*/return null;}
//	public GroovySeeTag[] seeTags() {/*todo*/return null;}
//	public GroovyTag[] tags() {/*todo*/return null;}
//	public GroovyTag[] tags(String arg0) {/*todo*/return null;}

}
