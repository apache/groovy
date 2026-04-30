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

/**
 * Base implementation of the {@link GroovyDoc} contract.
 */
public class SimpleGroovyDoc implements GroovyDoc/*, GroovyTokenTypes*/ {
    /**
     * Token type used for ordinary classes.
     */
    public static final int CLASS_DEF = 13;
    /**
     * Token type used for traits.
     */
    public static final int TRAIT_DEF = 15;
    /**
     * Token type used for interfaces.
     */
    public static final int INTERFACE_DEF = 14;

    /**
     * Token type used for records.
     */
    public static final int RECORD_DEF = 16;
    /**
     * Token type used for annotation types.
     */
    public static final int ANNOTATION_DEF = 64;
    /**
     * Token type used for enums.
     */
    public static final int ENUM_DEF = 61;
    private static final Pattern TAG2_PATTERN = Pattern.compile("(?s)([a-z]+)\\s+(.*)");
    private static final Pattern TAG3_PATTERN = Pattern.compile("(?s)([a-z]+)\\s+(\\S*)\\s+(.*)");
    private static final Pattern RAW_COMMENT_PATTERN = Pattern.compile("(?s).*?\\*\\s*@");
    private static final Pattern TRIMMED_COMMENT_PATTERN = Pattern.compile("(?m)^\\s*\\*\\s*([^*]*)$");
    private static final GroovyTag[] EMPTY_GROOVYTAG_ARRAY = new GroovyTag[0];
    private final String name;
    private String commentText = null;
    private String rawCommentText = "";
    private String firstSentenceCommentText = null;
    private int definitionType;
    private boolean deprecated;
    private boolean isScript;
    // GROOVY-11542 stage 1: marks a comment whose body is Markdown (/// runs
    // per JEP 467). The flag is set during AST visit; templates / renderers
    // inspect it to decide whether to route the body through a Markdown
    // renderer (stage 2) instead of the traditional Javadoc/HTML handling.
    private boolean markdown;
    private GroovyTag[] tags;

    /**
     * Creates a documented element with the supplied name.
     *
     * @param name the element name
     */
    public SimpleGroovyDoc(String name) {
        this.name = name;
        definitionType = CLASS_DEF;
    }

    /** {@inheritDoc} */
    @Override
    public String name() {
        return name;
    }

    /**
     * Returns a debug-friendly representation of this documented element.
     *
     * @return the string form of this element
     */
    @Override
    public String toString() {
        return getClass() + "(" + name + ")";
    }

    /**
     * Stores the rendered comment text for this element.
     *
     * @param commentText the rendered comment text
     */
    protected void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    /**
     * Stores the first-sentence summary for this element.
     *
     * @param firstSentenceCommentText the summary text
     */
    protected void setFirstSentenceCommentText(String firstSentenceCommentText) {
        this.firstSentenceCommentText = firstSentenceCommentText;
    }

    /** {@inheritDoc} */
    @Override
    public String commentText() {
        return commentText;
    }

    /** {@inheritDoc} */
    @Override
    public String firstSentenceCommentText() {
        return firstSentenceCommentText;
    }

    /** {@inheritDoc} */
    @Override
    public String getRawCommentText() {
        return rawCommentText;
    }

    /** {@inheritDoc} */
    @Override
    public void setRawCommentText(String rawCommentText) {
        this.rawCommentText = rawCommentText;
        calculateTags(rawCommentText);
    }

    /**
     * Indicates whether this documentation comment should be rendered as Markdown.
     *
     * @return {@code true} if Markdown rendering is enabled
     */
    public boolean isMarkdown() {
        return markdown;
    }

    /**
     * Sets whether this documentation comment should be rendered as Markdown.
     *
     * @param markdown {@code true} to enable Markdown rendering
     */
    public void setMarkdown(boolean markdown) {
        this.markdown = markdown;
    }

    /**
     * Marks this documented element as a script or ordinary class.
     *
     * @param script {@code true} if this element represents a script
     */
    public void setScript(boolean script) {
        isScript = script;
    }

    private void calculateTags(String rawCommentText) {
        String trimmed = RAW_COMMENT_PATTERN.matcher(rawCommentText).replaceFirst("@");
        if (trimmed.equals(rawCommentText)) return;
        String cleaned = TRIMMED_COMMENT_PATTERN.matcher(trimmed).replaceAll("$1").trim();
        String[] split = cleaned.split("(?m)^@", -1);
        List<GroovyTag> result = new ArrayList<>();
        for (String s : split) {
            String tagname = null;
            if (s.startsWith("param") || s.startsWith("throws") || s.startsWith("exception")) {
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

    /**
     * Extracts the first sentence from a raw documentation comment.
     *
     * @param raw the raw documentation comment
     * @return the calculated first sentence
     */
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

    /** {@inheritDoc} */
    @Override
    public boolean isClass() {
        return definitionType == CLASS_DEF && !isScript;
    }

    /**
     * Indicates whether this documented element represents a Groovy script.
     *
     * @return {@code true} if this element is a script
     */
    public boolean isScript() {
        return definitionType == CLASS_DEF && isScript;
    }

    /**
     * Indicates whether this documented element represents a trait.
     *
     * @return {@code true} if this element is a trait
     */
    public boolean isTrait() {
        return definitionType == TRAIT_DEF;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isInterface() {
        return definitionType == INTERFACE_DEF;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isAnnotationType() {
        return definitionType == ANNOTATION_DEF;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isEnum() {
        return definitionType == ENUM_DEF;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isRecord() {
        return definitionType == RECORD_DEF;
    }

    /**
     * Returns a human-readable description of this element's kind.
     *
     * @return the element kind description
     */
    public String getTypeDescription() {
        if (isInterface()) return "Interface";
        if (isRecord()) return "Record";
        if (isTrait()) return "Trait";
        if (isAnnotationType()) return "Annotation Type";
        if (isEnum()) return "Enum";
        if (isScript()) return "Script";
        return "Class";
    }

    /**
     * Returns the source-level keyword used to declare this element.
     *
     * @return the declaration keyword or descriptor
     */
    public String getTypeSourceDescription() {
        if (isInterface()) return "interface";
        if (isRecord()) return "record";
        if (isTrait()) return "trait";
        if (isAnnotationType()) return "@interface";
        if (isEnum()) return "enum";
        return "class";
    }

    /**
     * Sets the parsed token type for this element.
     *
     * @param t the token type
     */
    public void setTokenType(int t) {
        definitionType = t;
    }

    /**
     * Returns the parsed token type for this element.
     *
     * @return the token type
     */
    public int tokenType() {
        return definitionType;
    }

    // Methods from Comparable
    /** {@inheritDoc} */
    @Override
    public int compareTo(GroovyDoc that) {
        return name.compareTo((that).name());
    }

    // Methods from GroovyDoc

    //    public GroovyTag[] firstSentenceTags() {/*todo*/return null;}
    //    public GroovyTag[] inlineTags() {/*todo*/return null;}

    /** {@inheritDoc} */
    @Override
    public boolean isAnnotationTypeElement() {/*todo*/
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isConstructor() {/*todo*/
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isEnumConstant() {/*todo*/
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isDeprecated() {
        return deprecated;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isError() {/*todo*/
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isException() {/*todo*/
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isField() {/*todo*/
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isIncluded() {/*todo*/
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isMethod() {/*todo*/
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isOrdinaryClass() {/*todo*/
        return false;
    }
//    public GroovySourcePosition position() {/*todo*/return null;}
//    public GroovySeeTag[] seeTags() {/*todo*/return null;}

    /**
     * Returns the block tags parsed from the raw comment text.
     *
     * @return a defensive copy of the parsed tags, or {@code null} if tags have not been calculated
     */
    public GroovyTag[] tags() {
        return tags == null ? null : Arrays.copyOf(tags, tags.length);
    }

//    public GroovyTag[] tags(String arg0) {/*todo*/return null;}

    /**
     * Marks this documented element as deprecated or not deprecated.
     *
     * @param deprecated {@code true} if the element is deprecated
     */
    public void setDeprecated(boolean deprecated) {
        this.deprecated = deprecated;
    }
}
