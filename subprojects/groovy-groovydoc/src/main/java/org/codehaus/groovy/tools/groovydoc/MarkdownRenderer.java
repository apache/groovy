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

import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.Code;
import org.commonmark.node.FencedCodeBlock;
import org.commonmark.node.IndentedCodeBlock;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

/**
 * GROOVY-11542: support for JEP 467 Markdown documentation comments
 * (triple-slash {@code ///} runs, captured by {@link
 * org.apache.groovy.antlr.GroovydocVisitor}).
 *
 * <p>Two concerns are kept here:
 * <ul>
 *   <li>Splitting a raw comment body into its Markdown body (the prose) and
 *       its Javadoc block-tag trail ({@code @param}, {@code @return}, …).
 *       Block-tag detection matches Javadoc's JEP 467 rule: a line is a
 *       block tag when its first non-blank character is {@code @}, it is
 *       not inside a fenced code block ({@code ``` }/{@code ~~~}), and the
 *       {@code @} is followed by a tag word (an ASCII letter).</li>
 *   <li>Rendering the Markdown body to HTML. Headings are shifted down by
 *       two levels ({@code #} → {@code <h3>}, {@code ##} → {@code <h4>}, …)
 *       so they slot under the class/member page's own {@code <h1>} and
 *       {@code <h2>} structure, matching Javadoc's layout.</li>
 * </ul>
 *
 * <p>The renderer is stateless and thread-safe; a single {@link Parser} +
 * {@link HtmlRenderer} pair is cached per class.
 */
public final class MarkdownRenderer {

    private static final Parser PARSER = Parser.builder().build();
    private static final HtmlRenderer RENDERER = HtmlRenderer.builder().build();
    // Shift class-level headings down by two so `#` becomes `<h3>` (leaving
    // `<h1>`/`<h2>` for the page title and section banners).
    private static final int HEADING_SHIFT = 2;
    // Unicode control characters used to mask `{` and `}` inside Markdown
    // code spans / code blocks so that the downstream TagRenderer pass does
    // not expand inline tags (e.g. {@link}) that are meant to be literal
    // code samples. We substitute them back for the real braces after the
    // TagRenderer pass; see {@link #unmaskBracesInCode(String)}.
    static final char LBRACE_MASK = '\u0001';
    static final char RBRACE_MASK = '\u0002';

    private MarkdownRenderer() {}

    /**
     * Split a raw comment body (already stripped of {@code ///} prefixes)
     * into its Markdown body and block-tag trail. Returns a two-element
     * array {@code [body, tags]}; either may be empty.
     */
    public static String[] splitBodyAndTags(String raw) {
        if (raw == null || raw.isEmpty()) return new String[] {"", ""};
        String[] lines = raw.split("\n", -1);
        int split = -1;
        boolean inFence = false;
        String fenceMarker = null;
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            String trimmed = line.trim();
            // Track fenced code blocks so `@Override` inside a code sample
            // isn't mistaken for a block tag.
            if (inFence) {
                if (trimmed.startsWith(fenceMarker)) {
                    inFence = false;
                    fenceMarker = null;
                }
                continue;
            }
            if (trimmed.startsWith("```")) {
                inFence = true; fenceMarker = "```"; continue;
            }
            if (trimmed.startsWith("~~~")) {
                inFence = true; fenceMarker = "~~~"; continue;
            }
            // Block tag start: first non-blank is '@', followed by an ASCII
            // letter (so email-like `@mention` is tolerated only when it
            // actually leads with a letter tag word, matching Javadoc).
            if (trimmed.length() >= 2 && trimmed.charAt(0) == '@'
                    && Character.isLetter(trimmed.charAt(1))) {
                split = i;
                break;
            }
        }
        if (split < 0) return new String[] {raw, ""};
        StringBuilder body = new StringBuilder();
        for (int i = 0; i < split; i++) {
            if (i > 0) body.append('\n');
            body.append(lines[i]);
        }
        StringBuilder tags = new StringBuilder();
        for (int i = split; i < lines.length; i++) {
            if (i > split) tags.append('\n');
            tags.append(lines[i]);
        }
        return new String[] {body.toString(), tags.toString()};
    }

    /**
     * Render a Markdown body to HTML and shift heading levels down so that
     * JEP 467 top-level headings ({@code #}) become {@code <h3>}, fitting
     * under the page's existing {@code <h1>}/{@code <h2>} structure.
     */
    public static String render(String markdownBody) {
        if (markdownBody == null || markdownBody.isEmpty()) return "";
        Node document = PARSER.parse(markdownBody);
        // Mask `{` / `}` inside code spans and code blocks so inline tags
        // like {@link Foo} appearing in a code sample are treated as literal
        // text by the subsequent TagRenderer pass. The masks are translated
        // back to real braces via unmaskBracesInCode() after TagRenderer runs.
        document.accept(new CodeBraceMasker());
        String html = RENDERER.render(document);
        return shiftHeadings(html);
    }

    /**
     * Translate the {@code \u0001} / {@code \u0002} placeholders left inside
     * code spans / code blocks by {@link #render(String)} back to literal
     * {@code &#123;} / {@code &#125;} numeric HTML entities, so browsers
     * display them as {@code {} and {@code }} while keeping the content
     * distinguishable from tag syntax. Called after {@code TagRenderer} has
     * run so inline-tag expansion has already skipped over them.
     */
    public static String unmaskBracesInCode(String html) {
        if (html == null || html.isEmpty()) return html;
        if (html.indexOf(LBRACE_MASK) < 0 && html.indexOf(RBRACE_MASK) < 0) return html;
        StringBuilder sb = new StringBuilder(html.length());
        for (int i = 0, n = html.length(); i < n; i++) {
            char c = html.charAt(i);
            if (c == LBRACE_MASK) sb.append("&#123;");
            else if (c == RBRACE_MASK) sb.append("&#125;");
            else sb.append(c);
        }
        return sb.toString();
    }

    /**
     * CommonMark AST visitor that replaces every occurrence of {@code '{'}
     * and {@code '}'} in the literal text of {@link Code}, {@link
     * FencedCodeBlock}, and {@link IndentedCodeBlock} nodes with the
     * placeholder characters defined by {@link MarkdownRenderer}. The
     * placeholders are immune to TagRenderer's {@code '{@'} trigger so
     * inline-tag-looking text inside code samples is preserved verbatim.
     */
    private static final class CodeBraceMasker extends AbstractVisitor {
        @Override
        public void visit(Code code) {
            code.setLiteral(mask(code.getLiteral()));
        }
        @Override
        public void visit(FencedCodeBlock block) {
            block.setLiteral(mask(block.getLiteral()));
        }
        @Override
        public void visit(IndentedCodeBlock block) {
            block.setLiteral(mask(block.getLiteral()));
        }
        private static String mask(String text) {
            if (text == null || text.isEmpty()) return text;
            if (text.indexOf('{') < 0 && text.indexOf('}') < 0) return text;
            return text.replace('{', LBRACE_MASK).replace('}', RBRACE_MASK);
        }
    }

    private static String shiftHeadings(String html) {
        if (html.indexOf("<h") < 0) return html;
        StringBuilder out = new StringBuilder(html.length());
        int i = 0, n = html.length();
        while (i < n) {
            char c = html.charAt(i);
            if (c == '<') {
                // Look for <hN> or </hN> where 1 <= N <= 6.
                boolean closing = i + 1 < n && html.charAt(i + 1) == '/';
                int tagStart = closing ? i + 2 : i + 1;
                if (tagStart + 1 < n && html.charAt(tagStart) == 'h'
                        && Character.isDigit(html.charAt(tagStart + 1))) {
                    int level = html.charAt(tagStart + 1) - '0';
                    int endAngle = html.indexOf('>', tagStart + 2);
                    if (level >= 1 && level <= 6 && endAngle > 0) {
                        int newLevel = Math.min(6, level + HEADING_SHIFT);
                        out.append('<');
                        if (closing) out.append('/');
                        out.append('h').append(newLevel);
                        out.append(html, tagStart + 2, endAngle + 1);
                        i = endAngle + 1;
                        continue;
                    }
                }
            }
            out.append(c);
            i++;
        }
        return out.toString();
    }
}
