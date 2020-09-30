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
package groovy.console.ui.text;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.DocumentFilter;
import javax.swing.text.Position;
import javax.swing.text.Segment;
import javax.swing.text.Style;
import javax.swing.text.StyleContext;
import java.io.Serializable;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StructuredSyntaxDocumentFilter extends DocumentFilter {
    
    public static final String TAB_REPLACEMENT = "    ";
    
    private static final MLComparator ML_COMPARATOR = new MLComparator();

    /**
     * The root of the lexical parsing tree.
     */
    protected LexerNode lexer = new LexerNode(true);
    
    // The styled document the filter parses
    protected DefaultStyledDocument styledDocument; 
    
    // the document buffer and segment
    private Segment segment = new Segment();
    private CharBuffer buffer;
    
    /**
     * The position tree of multi-line comments.
     */ 
    protected SortedSet mlTextRunSet = new TreeSet(ML_COMPARATOR);
    
    // Ensures not adding any regexp with capturing groups
    private static void checkRegexp(String regexp) {
        String checking = regexp.replaceAll("\\\\\\(", "X").replaceAll("\\(\\?", "X");
        int checked = checking.indexOf('(');
        if (checked > -1) {
            StringBuilder msg = new StringBuilder("Only non-capturing groups allowed:\r\n" +
                    regexp + "\r\n");
            for (int i = 0; i < checked; i++) {
                msg.append(" ");
            }
            msg.append("^");
            throw new IllegalArgumentException(msg.toString());
        }
    }
    
    /**
     * Creates a new instance of StructuredSyntaxDocumentFilter
     * @param document the styled document to parse
     */
    public StructuredSyntaxDocumentFilter(DefaultStyledDocument document) {
        this.styledDocument = document;
    }
    
    private int calcBeginParse(int offset) {
        MultiLineRun mlr = getMultiLineRun(offset);
        if (mlr != null) {
            // means we're in middle of mlr, so start at beginning of mlr
            offset = mlr.start();
        }
        else {
            // otherwise, earliest position in line not part of mlr
            offset = styledDocument.getParagraphElement(offset).getStartOffset();
            mlr = getMultiLineRun(offset);
            offset = mlr == null ? offset : mlr.end() + 1;
        }
        
        return offset;
    }
    
    private int calcEndParse(int offset) {
        MultiLineRun mlr = getMultiLineRun(offset);
        if (mlr != null) {
            // means we're in middle of mlr, so end is at end of mlr
            offset = mlr.end();
        }
        else {
            // otherwise, latest position in line not part of mlr
            offset = styledDocument.getParagraphElement(offset).getEndOffset();
            mlr = getMultiLineRun(offset);
            offset = mlr == null ? offset : mlr.end();
        }
        
        return offset;
    }
    
    /**
     * Create a new LexerNode for adding to root.
     *
     * @return a new LexerNode
     */
    public LexerNode createLexerNode() {
        return new LexerNode(false);
    }
    
    // given an offset, return the mlr it resides in
    private MultiLineRun getMultiLineRun(int offset) {
        MultiLineRun ml = null;
        if (offset > 0) {
            Integer os = offset;

            SortedSet set = mlTextRunSet.headSet(os);
            if (!set.isEmpty()) {
                ml = (MultiLineRun)set.last();
                ml = ml.end() >= offset ? ml : null;
            }
        }

        return ml;
    }
    
    /**
     * Get the root node for lexing the document.   Children can be added such
     * that matching patterns can be further parsed if required.
     *
     * @return the root lexing node.  
     */
    public LexerNode getRootNode() {
        return lexer;
    }
    
    /**
     * Insert a string into the document, and then parse it if the parser has been
     * set.
     *
     * @param fb
     * @param offset
     * @param text
     * @param attrs
     * @throws BadLocationException
     */    
    @Override
    public void insertString(DocumentFilter.FilterBypass fb, int offset,
                             String text, AttributeSet attrs)
        throws BadLocationException {
        // remove problem meta characters returns
        text = replaceMetaCharacters(text);
        
        fb.insertString(offset, text, attrs);
        
        // start on the string that was inserted
        parseDocument(offset, text.length());
    }
    
    /**
     * Parse the Document to update the character styles given an initial start
     * position.  Called by the filter after it has updated the text. 
     *
     * @param offset
     * @param length
     * @throws BadLocationException
     */
    protected void parseDocument(int offset, int length) throws BadLocationException {
        // initialize the segment with the complete document so the segment doesn't
        // have an underlying gap in the buffer
        styledDocument.getText(0, styledDocument.getLength(), segment);
        
        buffer = CharBuffer.wrap(segment.array).asReadOnlyBuffer();
        
        // initialize the lexer if necessary
        if (!lexer.isInitialized()) {
            // prime the parser and reparse whole document
            lexer.initialize();
            offset = 0;
            length = styledDocument.getLength();
        }
        else {
            int end = offset + length;
            offset = calcBeginParse(offset);
            length = calcEndParse(end) - offset;
            
            // clean the tree by ensuring multi line styles are reset in area
            // of parsing
            SortedSet set = mlTextRunSet.subSet(offset,
                    offset + length);
            if (set != null) {
                set.clear();
            }
        }
        
        // parse the document
        lexer.parse(buffer, offset, length);
    }

    /**
     * Remove a string from the document, and then parse it if the parser has been
     * set.
     *
     * @param fb
     * @param offset
     * @param length
     * @throws BadLocationException
     */    
    @Override
    public void remove(DocumentFilter.FilterBypass fb, int offset, int length)
        throws BadLocationException {
        // FRICKIN' HACK!!!!! For some reason, deleting a string at offset 0
        // does not get done properly, so first replace and remove after parsing
        if (offset == 0 && length != fb.getDocument().getLength()) {
            fb.replace(0, length, "\n", lexer.defaultStyle);
            
            // start on either side of the removed text
            parseDocument(offset, 2);
            fb.remove(offset, 1);
        }
        else {
            fb.remove(offset, length);
            
            // start on either side of the removed text
            if (offset + 1 < fb.getDocument().getLength()) {
                parseDocument(offset, 1);
            }
            else if (offset - 1 > 0) {
                parseDocument(offset - 1, 1);
            }
            else {
                // empty text
                mlTextRunSet.clear();
            }
        }

    }

    /**
     * Replace a string in the document, and then parse it if the parser has been
     * set.
     *
     * @param fb
     * @param offset
     * @param length
     * @param text
     * @param attrs
     * @throws BadLocationException
     */    
    @Override
    public void replace(DocumentFilter.FilterBypass fb, int offset,
                        int length, String text, AttributeSet attrs)
        throws BadLocationException
    {
        // text might be null and indicates no replacement text
        if (text == null) text = "";
        
        // remove problem meta characters returns
        text = replaceMetaCharacters(text);
        
        fb.replace(offset, length, text, attrs);
        
        // start on the text that was replaced
        parseDocument(offset, text.length());
    }
    
    // tabs with spaces (I hate tabs)
    private String replaceMetaCharacters(String string) {
        // just in case remove carriage returns
        string = string.replace("\\t", TAB_REPLACEMENT);
        return string;
    }
    
    public final class LexerNode {
        
        private Style defaultStyle;
    
        private Map styleMap = new LinkedHashMap();
        private Map children = new HashMap();

        private Matcher matcher;
        private List groupList = new ArrayList();
        
        private boolean initialized;
        
        private CharBuffer lastBuffer;

        /*
         * Creates a new instance of LexerNode 
         */
        LexerNode(boolean isParent) {
            StyleContext sc = StyleContext.getDefaultStyleContext();
            defaultStyle = sc.getStyle(StyleContext.DEFAULT_STYLE);
        }
    
        private String buildRegexp(String[] regexps) {
            StringBuilder regexp = new StringBuilder();

            for (String s : regexps) {
                regexp.append("|").append(s);
            }

            // ensure leading '|' is removed
            return regexp.substring(1);
        }
        
        public Style getDefaultStyle() {
            return defaultStyle;
        }

        private void initialize() {
            matcher = null;
            groupList.clear();
            groupList.add(null);
            
            Iterator iter = styleMap.keySet().iterator();
            StringBuilder regexp = new StringBuilder();
            while (iter.hasNext()) {
                String nextRegexp = (String)iter.next();
                regexp.append("|(").append(nextRegexp).append(")");
                // have to compile regexp first so that it will match
                groupList.add(Pattern.compile(nextRegexp).pattern());
            }
            if (!regexp.toString().isEmpty()) {
                matcher = Pattern.compile(regexp.substring(1)).matcher("");
                
                iter = children.values().iterator();
                while (iter.hasNext()) {
                    ((LexerNode)iter.next()).initialize();
                }
            }
            initialized = true;
        }
        
        /**
         * @return true if initialised
         */        
        public boolean isInitialized() {
            return initialized;
        }

        /**
         * @param buffer
         * @param offset
         * @param length
         * @throws BadLocationException
         */        
        public void parse(CharBuffer buffer, int offset, int length)
            throws BadLocationException {
            // get the index of where we can start to look for an exit:
            // i.e. after the end of the length of the segment, when we find 
            // that text in question already is set properly, we can stop
            // parsing
            int checkPoint = offset + length;
            
            // reset the matcher and start parsing string
            if (lastBuffer != buffer) {
                matcher.reset(buffer);
                lastBuffer = buffer;
            }
            
            // the start and end indices of a match in the Matcher looking
            int matchEnd = offset;
            Style style = null;
            while (matchEnd < checkPoint && matcher.find(offset)) {
                // when we get something other than -1, we know which regexp
                // matched; the 0 group is the complete expression of the 
                // matcher, which would always return a hit based on the above
                // while condition
                int groupNum = 0;
                while ((offset = matcher.start(++groupNum)) == -1){
                }
                
                // if the matching offset is not the same as the end of the 
                // previous match, we have extra text not matched, so set to 
                // the default style of this lexer node
                if (offset != matchEnd) {
                    offset = offset > checkPoint ? checkPoint : offset; 
                    styledDocument.setCharacterAttributes(matchEnd,
                                                          offset - matchEnd,
                                                          defaultStyle,
                                                          true);
                    if (offset >= checkPoint) {
                        return;
                    }
                }

                // track the end of the matching string 
                matchEnd = matcher.end(groupNum);

                // retrieve the proper style from groupNum of the groupList and
                // styleMap, then set the attributes of the matching string
                style = (Style)styleMap.get((String)groupList.get(groupNum));
                styledDocument.setCharacterAttributes(offset,
                                                      matchEnd - offset,
                                                      style, true);

                // if the match was multiline, which we'll know if they span
                // multiple paragraph elements, the mark it (this list was cleaned
                // above in parseDocument())
                if (styledDocument.getParagraphElement(offset).getStartOffset() !=
                    styledDocument.getParagraphElement(matchEnd).getStartOffset()) {
                    // mark a ml run
                    MultiLineRun mlr = new MultiLineRun(offset, matchEnd);
                    mlTextRunSet.add(mlr);
                }
                
                // parse the child regexps, if any, within a matched block
                LexerNode node = (LexerNode)children.get(groupList.get(groupNum));
                if (node != null) {
                    node.parse(buffer, offset, matchEnd - offset);
                }
                
                // set the offset to start where we left off
                offset = matchEnd;
            }
            if (matchEnd < checkPoint) {
                // if we finished before hitting the end of the checkpoint from
                // no mroe matches, then set ensure the text is reset to the
                // defaultStyle
                styledDocument.setCharacterAttributes(matchEnd,
                                                      checkPoint - matchEnd,
                                                      defaultStyle,
                                                      true);
            }
        }

        /**
         *
         * @param regexp
         * @param node
         */        
        public void putChild(String regexp, LexerNode node) {
            node.defaultStyle = (Style)styleMap.get(regexp);
            
            // have to compile regexp first so that it will match
            children.put(Pattern.compile(regexp).pattern(), node);
            initialized = false;
        }

        /**
         * @param regexps
         * @param node
         */        
        public void putChild(String[] regexps, LexerNode node) {
            putChild(buildRegexp(regexps), node);
        }

        /**
         * @param regexp
         * @param style
         */        
        public void putStyle(String regexp, Style style) {
            checkRegexp(regexp);
            styleMap.put(regexp, style);
            initialized = false;
        }

        /**
         * @param regexps
         * @param style
         */        
        public void putStyle(String regexps[], Style style) {
            putStyle(buildRegexp(regexps), style);
        }

        /**
         * @param regexp
         */        
        public void removeChild(String regexp) {
            children.remove(regexp);
        }

        /**
         * @param regexp
         */        
        public void removeStyle(String regexp) {
            styleMap.remove(regexp);
            children.remove(regexp);
        }

        /**
         * @param regexps
         */        
        public void removeStyle(String regexps[]) {
            removeStyle(buildRegexp(regexps));
        }
        
        public void setDefaultStyle(Style style) {
            defaultStyle = style;
        }
    }
    
    protected class MultiLineRun {
        
        private Position start;
        private Position end;
        private int delimeterSize;
        
        public MultiLineRun(int start, int end) throws BadLocationException {
            this(start, end, 2);
        }
        
        public MultiLineRun(int start, int end, int delimeterSize) throws BadLocationException {
            if (start > end) {
                String msg = "Start offset is after end: ";
                throw new BadLocationException(msg, start);
            }
            if (delimeterSize < 1) {
                String msg = "Delimiters be at least size 1: " + 
                              delimeterSize;
                throw new IllegalArgumentException(msg);
            }
            this.start = styledDocument.createPosition(start);
            this.end = styledDocument.createPosition(end);
            this.delimeterSize = delimeterSize;
        }
        
        public int getDelimeterSize() {
            return delimeterSize;
        }
        
        public int end() {
            return end.getOffset();
        }
        
        public int length() {
            return end.getOffset() - start.getOffset();
        }
        
        public int start() {
            return start.getOffset();
        }
        
        public String toString() {
            return start.toString() + " " + end.toString();
        }
        
    }

    private static class MLComparator implements Comparator, Serializable {

        private static final long serialVersionUID = -4210196728719411217L;

        @Override
        public int compare(Object obj, Object obj1) {
            return valueOf(obj) - valueOf(obj1);
        }
        
        private int valueOf(Object obj) {
            return obj instanceof Integer ?
                    (Integer) obj :
                    (obj instanceof MultiLineRun) ?
                        ((MultiLineRun)obj).start() :
                        ((Position)obj).getOffset();
        }
    }
}
