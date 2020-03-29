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
package org.codehaus.groovy.tools.groovydoc;

import org.codehaus.groovy.groovydoc.GroovyDoc;
import org.codehaus.groovy.groovydoc.GroovyTag;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SimpleGroovyDoc implements GroovyDoc/*, GroovyTokenTypes*/ {
    public static final int CLASS_DEF = 13;
    public static final int TRAIT_DEF = 15;
    public static final int INTERFACE_DEF = 14;
    public static final int ANNOTATION_DEF = 64;
    public static final int ENUM_DEF = 61;
    private static final Pattern TAG2_PATTERN = Pattern.compile("(?s)([a-z]+)\\s+(.*)");
    private static final Pattern TAG3_PATTERN = Pattern.compile("(?s)([a-z]+)\\s+(\\S*)\\s+(.*)");
    private static final Pattern RAW_COMMENT_PATTERN = Pattern.compile("\"(?s).*?\\\\*\\\\s*@\"");
    private static final Pattern TRIMMED_COMMENT_PATTERN = Pattern.compile("(?m)^\\s*\\*\\s*([^*]*)$");
    private static final GroovyTag[] EMPTY_GROOVYTAG_ARRAY = new GroovyTag[0];
    private final String name;
    private String commentText = null;
    private String rawCommentText = "";
    private String firstSentenceCommentText = null;
    private int definitionType;
    private boolean deprecated;
    private boolean isScript;
    private GroovyTag[] tags;

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
        return commentText;
    }

    public String firstSentenceCommentText() {
        return firstSentenceCommentText;
    }

    public String getRawCommentText() {
        return rawCommentText;
    }

    public void setRawCommentText(String rawCommentText) {
        this.rawCommentText = rawCommentText;
        calculateTags(rawCommentText);
    }

    public void setScript(boolean script) {
        isScript = script;
    }

    private void calculateTags(String rawCommentText) {
        String trimmed = RAW_COMMENT_PATTERN.matcher(rawCommentText).replaceFirst("@");
        if (trimmed.equals(rawCommentText)) return;
        String cleaned = TRIMMED_COMMENT_PATTERN.matcher(trimmed).replaceAll("$1").trim();
        String[] split = cleaned.split("(?m)^@");
        List<GroovyTag> result = new ArrayList<GroovyTag>();
        for (String s : split) {
            String tagname = null;
            if (s.startsWith("param") || s.startsWith("throws")) {
                Matcher m = TAG3_PATTERN.matcher(s);
                if (m.find()) {
                    tagname = m.group(1);
                    result.add(new SimpleGroovyTag(tagname, m.group(2), m.group(3)));
                }
            } else {
                Matcher m = TAG2_PATTERN.matcher(s);
                if (m.find()) {
                    tagname = m.group(1);
                    result.add(new SimpleGroovyTag(tagname, null, m.group(2)));
                }
            }
            if ("deprecated".equals(tagname)) {
                setDeprecated(true);
            }
        }
        tags = result.toArray(EMPTY_GROOVYTAG_ARRAY);
    }

    public static String calculateFirstSentence(String raw) {
        // remove all the * from beginning of lines
        String text = raw.replaceAll("(?m)^\\s*\\*", "").trim();
        // assume a <p> paragraph tag signifies end of sentence
        text = text.replaceFirst("(?ms)<p>.*", "").trim();
        // assume completely blank line signifies end of sentence
        text = text.replaceFirst("(?ms)\\n\\s*\\n.*", "").trim();
        // assume @tag signifies end of sentence
        text = text.replaceFirst("(?ms)\\n\\s*@(see|param|throws|return|author|since|exception|version|deprecated|todo)\\s.*", "").trim();
        // Comment Summary using first sentence (Locale sensitive)
        BreakIterator boundary = BreakIterator.getSentenceInstance(Locale.getDefault()); // todo - allow locale to be passed in
        boundary.setText(text);
        int start = boundary.first();
        int end = boundary.next();
        if (start > -1 && end > -1) {
            // need to abbreviate this comment for the summary
            text = text.substring(start, end);
        }
        return text;
    }

    public boolean isClass() {
        return definitionType == CLASS_DEF && !isScript;
    }

    public boolean isScript() {
        return definitionType == CLASS_DEF && isScript;
    }

    public boolean isTrait() {
        return definitionType == TRAIT_DEF;
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
        if (isTrait()) return "Trait";
        if (isAnnotationType()) return "Annotation Type";
        if (isEnum()) return "Enum";
        if (isScript()) return "Script";
        return "Class";
    }

    public String getTypeSourceDescription() {
        if (isInterface()) return "interface";
        if (isTrait()) return "trait";
        if (isAnnotationType()) return "@interface";
        if (isEnum()) return "enum";
        return "class";
    }

    public void setTokenType(int t) {
        definitionType = t;
    }

    public int tokenType() {
        return definitionType;
    }

    // Methods from Comparable
    public int compareTo(Object that) {
        if (that instanceof GroovyDoc) {
            return name.compareTo(((GroovyDoc) that).name());
        } else {
            throw new ClassCastException(String.format("Cannot compare object of type %s.", that.getClass()));
        }
    }

    // Methods from GroovyDoc

    //    public GroovyTag[] firstSentenceTags() {/*todo*/return null;}
    //    public GroovyTag[] inlineTags() {/*todo*/return null;}

    public boolean isAnnotationTypeElement() {/*todo*/
        return false;
    }

    public boolean isConstructor() {/*todo*/
        return false;
    }

    public boolean isEnumConstant() {/*todo*/
        return false;
    }

    public boolean isDeprecated() {
        return deprecated;
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
//    public GroovySourcePosition position() {/*todo*/return null;}
//    public GroovySeeTag[] seeTags() {/*todo*/return null;}

    public GroovyTag[] tags() {
        return tags == null ? null : Arrays.copyOf(tags, tags.length);
    }

//    public GroovyTag[] tags(String arg0) {/*todo*/return null;}

    public void setDeprecated(boolean deprecated) {
        this.deprecated = deprecated;
    }
}
