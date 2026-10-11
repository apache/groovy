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

import groovy.console.ui.ThemeManager;
import groovy.lang.Tuple2;
import org.apache.groovy.lsp.GroovyLanguageClient;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.DocumentFilter;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Swing document filter that styles the editor from groovy-lsp semantic
 * tokens ({@code textDocument/semanticTokens}). The request does not
 * compile. Keywords, numbers,
 * comments, {@code @}, and comma or semicolon use the matching theme
 * style. Other legend names, including identifiers, use the identifier
 * style. A string takes its colour from the token that opened the
 * literal (single quote, double quote, or slashy). A line-split span
 * does not repeat that delimiter. The {@code $} that ends a GString
 * begin or part is left in the default style. Structural brackets,
 * including {@code ?[}, are {@code delimiter} tokens on that same
 * response. {@link #getLatestTokenList()} exposes them for brace
 * matching. The filter does not lex the buffer itself.
 *
 * @since 3.0.0
 */
public class SmartDocumentFilter extends DocumentFilter {
    private static final String MONOSPACED = "Monospaced";
    private static final String STYLE_COMMENT = "comment";
    private static final String STYLE_QUOTES = "quotes";
    private static final String STYLE_SINGLE_QUOTES = "single_quotes";
    private static final String STYLE_SLASHY_QUOTES = "slashy_quotes";
    private static final String STYLE_DIGIT = "digit";
    private static final String STYLE_ANNOTATION = "annotation";
    private static final String STYLE_OPERATION = "operation";
    private static final String STYLE_IDENT = "ident";
    private static final String STYLE_RESERVED = "reserved_word";
    private static final List<String> THEME_STYLE_KEYS = List.of(
            STYLE_COMMENT, STYLE_QUOTES, STYLE_SINGLE_QUOTES, STYLE_SLASHY_QUOTES,
            STYLE_DIGIT, STYLE_ANNOTATION, STYLE_OPERATION, STYLE_IDENT,
            STYLE_RESERVED);

    private static final int TYPE_KEYWORD = 8;
    private static final int TYPE_STRING = 9;
    private static final int TYPE_NUMBER = 10;
    private static final int TYPE_COMMENT = 11;
    private static final int TYPE_OPERATOR = 12;
    private static final int TYPE_DECORATOR = 13;
    private static final int TYPE_DELIMITER = 14;
    private static final int MOD_SLASHY = 8;
    private static final int MOD_SINGLE = 16;

    private final DefaultStyledDocument styledDocument;
    private final StyleContext styleContext;
    private final Style defaultStyle;
    private final GroovyLanguageClient language;

    /**
     * Creates a filter that styles the supplied document.
     *
     * @param styledDocument the document to tokenize and highlight
     */
    public SmartDocumentFilter(DefaultStyledDocument styledDocument) {
        this.styledDocument = styledDocument;

        this.styleContext = StyleContext.getDefaultStyleContext();
        this.defaultStyle = this.styleContext.getStyle(StyleContext.DEFAULT_STYLE);
        StyleConstants.setFontFamily(this.defaultStyle, MONOSPACED);

        initStyles();
        this.language = GroovyLanguageClient.open("groovy-buffer:highlight-" + System.identityHashCode(this), "", false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void insertString(DocumentFilter.FilterBypass fb, int offset,
                             String text, AttributeSet attrs) throws BadLocationException {
        text = replaceMetaCharacters(text);
        fb.insertString(offset, text, attrs);
        parseDocument();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void remove(DocumentFilter.FilterBypass fb, int offset, int length)
            throws BadLocationException {
        fb.remove(offset, length);
        parseDocument();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void replace(DocumentFilter.FilterBypass fb, int offset,
                        int length, String text, AttributeSet attrs)
            throws BadLocationException {
        if (text == null) {
            text = "";
        }
        text = replaceMetaCharacters(text);
        fb.replace(offset, length, text, attrs);
        parseDocument();
    }

    private String replaceMetaCharacters(String string) {
        return string.replace("\r\n", "\n");
    }

    private void parseDocument() throws BadLocationException {
        String text = styledDocument.getText(0, styledDocument.getLength());
        List<Integer> data = language.semanticTokens(text);
        int[] dirty = dirtyRange(text);
        try {
            this.latestTokenList = paintSemantic(text, data, dirty[0], dirty[1]);
            this.latest = true;
        } finally {
            this.setRenderRange(null);
        }
    }

    private int[] dirtyRange(String text) {
        int last = text.length() - 1;
        if (last < 0) {
            return new int[] {0, -1};
        }
        Tuple2<Integer, Integer> range = getRenderRange();
        if (range == null) {
            return new int[] {0, last};
        }
        int start = range.getV1();
        int stop = range.getV2();
        if (start < 0 || stop < 0) {
            return new int[] {0, last};
        }
        int from = Math.max(0, Math.min(start, stop));
        int to = Math.min(last, Math.max(start, stop));
        return new int[] {from, to};
    }

    private List<Delimiter> paintSemantic(String text, List<Integer> data, int dirtyStart, int dirtyStop) {
        List<Delimiter> delimiters = new ArrayList<>();
        if (!text.isEmpty() && dirtyStart <= dirtyStop) {
            styledDocument.setCharacterAttributes(dirtyStart, dirtyStop - dirtyStart + 1, defaultStyle, true);
        }
        int line = 0;
        int character = 0;
        for (int i = 0; i + 4 < data.size(); i += 5) {
            int deltaLine = data.get(i);
            line += deltaLine;
            character = deltaLine == 0 ? character + data.get(i + 1) : data.get(i + 1);
            int length = data.get(i + 2);
            int start = offsetOf(text, line, character);
            int end = Math.min(start + Math.max(length, 0), text.length());
            if (end <= start) {
                continue;
            }
            int type = data.get(i + 3);
            if (type == TYPE_DELIMITER) {
                delimiters.add(new Delimiter(start, end - start, text.substring(start, end)));
                continue;
            }
            if (end > dirtyStart && start <= dirtyStop) {
                Style style = styleFor(type, data.get(i + 4));
                styledDocument.setCharacterAttributes(start, end - start, style, true);
            }
        }
        return delimiters;
    }

    private Style styleFor(int type, int modifiers) {
        if (type == TYPE_KEYWORD) {
            return styleNamed(STYLE_RESERVED);
        }
        if (type == TYPE_STRING) {
            if ((modifiers & MOD_SLASHY) != 0) {
                return styleNamed(STYLE_SLASHY_QUOTES);
            }
            if ((modifiers & MOD_SINGLE) != 0) {
                return styleNamed(STYLE_SINGLE_QUOTES);
            }
            return styleNamed(STYLE_QUOTES);
        }
        if (type == TYPE_NUMBER) {
            return styleNamed(STYLE_DIGIT);
        }
        if (type == TYPE_COMMENT) {
            return styleNamed(STYLE_COMMENT);
        }
        if (type == TYPE_DECORATOR) {
            return styleNamed(STYLE_ANNOTATION);
        }
        if (type == TYPE_OPERATOR) {
            return styleNamed(STYLE_OPERATION);
        }
        return styleNamed(STYLE_IDENT);
    }

    private static int offsetOf(String text, int line, int character) {
        int currentLine = 0;
        int i = 0;
        while (i < text.length() && currentLine < line) {
            char c = text.charAt(i);
            if (c == '\n') {
                currentLine++;
            } else if (c == '\r') {
                if (i + 1 < text.length() && text.charAt(i + 1) == '\n') {
                    i++;
                }
                currentLine++;
            }
            i++;
        }
        return Math.min(i + Math.max(character, 0), text.length());
    }

    private Style styleNamed(String themeKey) {
        Style style = styleContext.getStyle(themeKey);
        return null == style ? defaultStyle : style;
    }

    private void initStyles() {
        for (String key : THEME_STYLE_KEYS) {
            if (styleContext.getStyle(key) == null) {
                styleContext.addStyle(key, defaultStyle);
            }
        }
        updateStyles();
    }

    /**
     * Forces a full re-parse and re-render of the document with current styles.
     * Clears the incremental-render cache first so every token is restyled,
     * not just the ones that differ from the last parse.
     *
     * @since 6.0.0
     */
    public void reparseDocument() {
        this.latestTokenList = List.of();
        try {
            parseDocument();
        } catch (BadLocationException e) {
            // ignore
        }
    }

    /**
     * Updates all SmartDocumentFilter styles with current theme colors.
     * Attribute values are sourced from ThemeManager so custom .theme files
     * feed both this semantic-token path and the regex-driven GroovyFilter
     * path from one place. Called at init time and on theme switch.
     *
     * @since 6.0.0
     */
    public static void updateStyles() {
        StyleContext sc = StyleContext.getDefaultStyleContext();
        applyThemeStyle(sc.getStyle(STYLE_COMMENT), STYLE_COMMENT);
        applyThemeStyle(sc.getStyle(STYLE_QUOTES), STYLE_QUOTES);
        applyThemeStyle(sc.getStyle(STYLE_SINGLE_QUOTES), STYLE_SINGLE_QUOTES);
        applyThemeStyle(sc.getStyle(STYLE_SLASHY_QUOTES), STYLE_SLASHY_QUOTES);
        applyThemeStyle(sc.getStyle(STYLE_DIGIT), STYLE_DIGIT);
        applyThemeStyle(sc.getStyle(STYLE_ANNOTATION), STYLE_ANNOTATION);
        applyThemeStyle(sc.getStyle(STYLE_OPERATION), STYLE_OPERATION);
        applyThemeStyle(sc.getStyle(STYLE_IDENT), STYLE_IDENT);
        applyThemeStyle(sc.getStyle(STYLE_RESERVED), STYLE_RESERVED);
    }

    @SuppressWarnings("rawtypes")
    static void applyThemeStyle(Style style, String themeKey) {
        if (style == null) {
            return;
        }
        style.removeAttribute(StyleConstants.Foreground);
        style.removeAttribute(StyleConstants.Background);
        style.removeAttribute(StyleConstants.Bold);
        style.removeAttribute(StyleConstants.Italic);
        style.removeAttribute(StyleConstants.Underline);
        Map attrs = ThemeManager.getStyleAttrs(themeKey);
        Object fg = attrs.get("foreground");
        if (fg instanceof Color) {
            StyleConstants.setForeground(style, (Color) fg);
        }
        Object bg = attrs.get("background");
        if (bg instanceof Color) {
            StyleConstants.setBackground(style, (Color) bg);
        }
        if (Boolean.TRUE.equals(attrs.get("bold"))) {
            StyleConstants.setBold(style, true);
        }
        if (Boolean.TRUE.equals(attrs.get("italic"))) {
            StyleConstants.setItalic(style, true);
        }
        if (Boolean.TRUE.equals(attrs.get("underline"))) {
            StyleConstants.setUnderline(style, true);
        }
    }

    private volatile boolean latest = false;
    private volatile List<Delimiter> latestTokenList = List.of();
    private volatile Tuple2<Integer, Integer> renderRange;

    /**
     * Indicates whether the cached token list reflects the current document text.
     *
     * @return {@code true} if the latest parse completed successfully
     */
    public boolean isLatest() {
        return latest;
    }

    /**
     * Structural brackets from the most recent semantic-token response.
     * Brackets inside strings and comments are absent.
     *
     * @return delimiter spans, never {@code null}
     */
    public List<Delimiter> getLatestTokenList() {
        return latestTokenList;
    }

    /**
     * Limits rendering to the supplied character range for the next parse.
     *
     * @param renderRange the inclusive start and stop offsets to re-render, or {@code null} for all text
     */
    public void setRenderRange(Tuple2<Integer, Integer> renderRange) {
        this.renderRange = renderRange;
    }

    /**
     * Returns the character range scheduled for selective re-rendering.
     *
     * @return the current render range, or {@code null} if the whole document will be rendered
     */
    public Tuple2<Integer, Integer> getRenderRange() {
        return renderRange;
    }

    /**
     * One structural bracket from {@code textDocument/semanticTokens}.
     */
    public static final class Delimiter {
        private final int offset;
        private final int length;
        private final String text;

        Delimiter(int offset, int length, String text) {
            this.offset = offset;
            this.length = length;
            this.text = text;
        }

        public int getOffset() {
            return offset;
        }

        public int getLength() {
            return length;
        }

        public String getText() {
            return text;
        }
    }
}
