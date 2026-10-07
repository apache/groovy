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
package org.apache.groovy.lsp.internal.util;

import groovy.lang.groovydoc.Groovydoc;
import org.codehaus.groovy.ast.AnnotatedNode;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Turns a Groovydoc comment into compact Markdown for hover.
 * A whitelist tag is converted only outside a code span, and only when
 * it is void ({@code br}, {@code hr}, {@code img}), has attributes, or
 * has a matching close tag. Other angle brackets are escaped, so a type
 * argument such as {@code <U>} is not eaten as markup.
 */
public final class GroovydocMarkdown {

    private static final Set<String> HTML_NAMES = Set.of(
            "a", "b", "blockquote", "br", "caption", "cite", "code", "dd", "div", "dl", "dt",
            "em", "h1", "h2", "h3", "h4", "h5", "h6", "hr", "i", "img", "li", "ol", "p", "pre",
            "span", "strong", "sub", "sup", "table", "tbody", "td", "tfoot", "th", "thead", "tr",
            "tt", "u", "ul", "var");
    private static final Set<String> VOID_TAGS = Set.of("br", "hr", "img");

    private GroovydocMarkdown() {
    }

    /**
     * Markdown for {@code node}'s Groovydoc, or empty.
     *
     * @param node annotated AST node
     * @return markdown, never {@code null}
     */
    public static String of(final AnnotatedNode node) {
        if (node == null) {
            return "";
        }
        Groovydoc groovydoc = node.getGroovydoc();
        if (groovydoc == null || !groovydoc.isPresent()) {
            return "";
        }
        return format(groovydoc.getContent());
    }

    /**
     * Formats raw Groovydoc text (including {@code /**} delimiters).
     *
     * @param content comment text
     * @return markdown, never {@code null}
     */
    public static String format(final String content) {
        if (content == null || content.isBlank()) {
            return "";
        }
        String body = stripComment(content);
        body = inlineTags(body);
        body = htmlToMarkdown(body);
        body = blockTags(body);
        return body.strip();
    }

    private static String stripComment(final String content) {
        String text = content.strip();
        if (text.startsWith("/**")) {
            text = text.substring(3);
        }
        if (text.endsWith("*/")) {
            text = text.substring(0, text.length() - 2);
        }
        StringBuilder builder = new StringBuilder();
        for (String raw : text.split("\n", -1)) {
            String line = raw.strip();
            if (line.startsWith("*")) {
                line = line.substring(1);
                if (line.startsWith(" ")) {
                    line = line.substring(1);
                }
            }
            if (!builder.isEmpty()) {
                builder.append('\n');
            }
            builder.append(line);
        }
        return builder.toString();
    }

    private static String inlineTags(final String text) {
        String result = replaceTag(text, "{@code ", "`", "`");
        result = replaceTag(result, "{@literal ", "", "");
        result = replaceLink(result, "{@link ");
        result = replaceLink(result, "{@linkplain ");
        return result;
    }

    private static String replaceTag(final String text, final String open, final String before, final String after) {
        StringBuilder builder = new StringBuilder();
        int from = 0;
        while (from < text.length()) {
            int start = text.indexOf(open, from);
            int end = start < 0 ? -1 : text.indexOf('}', start + open.length());
            if (start < 0 || end < 0) {
                builder.append(text, from, text.length());
                break;
            }
            builder.append(text, from, start);
            builder.append(before).append(text, start + open.length(), end).append(after);
            from = end + 1;
        }
        return builder.toString();
    }

    private static String replaceLink(final String text, final String open) {
        StringBuilder builder = new StringBuilder();
        int from = 0;
        while (from < text.length()) {
            int start = text.indexOf(open, from);
            int end = start < 0 ? -1 : text.indexOf('}', start + open.length());
            if (start < 0 || end < 0) {
                builder.append(text, from, text.length());
                break;
            }
            builder.append(text, from, start);
            builder.append('`').append(linkLabel(text.substring(start + open.length(), end))).append('`');
            from = end + 1;
        }
        return builder.toString();
    }

    private static String linkLabel(final String body) {
        String trimmed = body.strip();
        int hash = trimmed.indexOf('#');
        if (hash >= 0) {
            String type = trimmed.substring(0, hash).strip();
            String member = trimmed.substring(hash + 1).strip();
            int space = member.indexOf(' ');
            if (space > 0) {
                member = member.substring(0, space);
            }
            int paren = member.indexOf('(');
            if (paren > 0) {
                member = member.substring(0, paren);
            }
            return type.isEmpty() ? member : type + "." + member;
        }
        int space = trimmed.indexOf(' ');
        String target = space < 0 ? trimmed : trimmed.substring(0, space);
        int lastDot = target.lastIndexOf('.');
        return lastDot < 0 ? target : target.substring(lastDot + 1);
    }

    /**
     * Type arguments share spellings with HTML ({@code <U>}, {@code <P>},
     * {@code <A, B>}). A tag counts only outside a code span, and only
     * when it is void, self-closing, attributed, or paired with a close tag.
     */
    private static String htmlToMarkdown(final String text) {
        boolean[] code = codeSpans(text);
        Map<Integer, Markup> markup = new HashMap<>();
        collectMarkup(text, code, markup);
        StringBuilder builder = new StringBuilder(text.length());
        int i = 0;
        while (i < text.length()) {
            Markup tag = markup.get(i);
            if (tag != null) {
                builder.append(tag.text);
                i = tag.end;
                continue;
            }
            char c = text.charAt(i);
            if (code[i]) {
                builder.append(c);
            } else if (c == '<') {
                builder.append("&lt;");
            } else if (c == '>') {
                builder.append("&gt;");
            } else {
                builder.append(c);
            }
            i++;
        }
        return builder.toString();
    }

    private static boolean[] codeSpans(final String text) {
        boolean[] code = new boolean[text.length()];
        boolean inside = false;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '`') {
                inside = !inside;
            } else if (inside) {
                code[i] = true;
            }
        }
        return code;
    }

    private static void collectMarkup(final String text, final boolean[] code, final Map<Integer, Markup> markup) {
        int i = 0;
        while (i < text.length()) {
            if (code[i] || text.charAt(i) != '<') {
                i++;
                continue;
            }
            Tag tag = parseTag(text, i);
            if (tag == null || markup.containsKey(i)) {
                i++;
                continue;
            }
            int closeAt = tag.close || tag.selfClosing ? -1 : matchingClose(text, code, tag);
            if (closeAt >= 0) {
                Tag close = parseTag(text, closeAt);
                markup.put(i, new Markup(tag.end, htmlReplacement(tag)));
                markup.put(closeAt, new Markup(close.end, htmlReplacement(close)));
                if ("code".equals(tag.name)) {
                    for (int k = tag.end; k < closeAt; k++) {
                        code[k] = true;
                    }
                }
                i = tag.end;
            } else if (tag.selfClosing || tag.attributes || VOID_TAGS.contains(tag.name)) {
                markup.put(i, new Markup(tag.end, htmlReplacement(tag)));
                i = tag.end;
            } else {
                i++;
            }
        }
    }

    private static int matchingClose(final String text, final boolean[] code, final Tag open) {
        int i = open.end;
        while (i < text.length()) {
            if (!code[i] && text.charAt(i) == '<') {
                Tag tag = parseTag(text, i);
                if (tag != null && tag.close && tag.name.equals(open.name)) {
                    return i;
                }
                if (tag != null) {
                    i = tag.end;
                    continue;
                }
            }
            i++;
        }
        return -1;
    }

    /**
     * A whitelist tag at {@code start}. Returns {@code null} when the
     * following character is not {@code >}, {@code /}, or whitespace, so
     * {@code <A, B>} stays a type argument.
     */
    private static Tag parseTag(final String text, final int start) {
        if (start >= text.length() || text.charAt(start) != '<') {
            return null;
        }
        int i = start + 1;
        boolean close = false;
        if (i < text.length() && text.charAt(i) == '/') {
            close = true;
            i++;
        }
        int nameStart = i;
        while (i < text.length() && Character.isLetterOrDigit(text.charAt(i))) {
            i++;
        }
        if (i == nameStart || i >= text.length()) {
            return null;
        }
        String name = text.substring(nameStart, i).toLowerCase(Locale.ROOT);
        if (!HTML_NAMES.contains(name)) {
            return null;
        }
        char after = text.charAt(i);
        if (after != '>' && after != '/' && !Character.isWhitespace(after)) {
            return null;
        }
        boolean attributes = false;
        while (i < text.length()) {
            char c = text.charAt(i);
            if (c == '>') {
                return new Tag(name, i + 1, close, false, attributes);
            }
            if (c == '<') {
                return null;
            }
            if (c == '/' && i + 1 < text.length() && text.charAt(i + 1) == '>') {
                return new Tag(name, i + 2, close, true, attributes);
            }
            if (Character.isWhitespace(c)) {
                attributes = true;
            }
            i++;
        }
        return null;
    }

    private static String htmlReplacement(final Tag tag) {
        return switch (tag.name) {
            case "br" -> "\n";
            case "p" -> tag.close ? "" : "\n\n";
            case "b", "strong" -> "**";
            case "i", "em" -> "*";
            case "code" -> "`";
            default -> "";
        };
    }

    private record Tag(String name, int end, boolean close, boolean selfClosing, boolean attributes) {
    }

    private record Markup(int end, String text) {
    }

    private static String blockTags(final String text) {
        String normalized = text.replace(" @param ", "\n@param ")
                .replace(" @return", "\n@return")
                .replace(" @throws ", "\n@throws ")
                .replace(" @exception ", "\n@exception ")
                .replace(" @see ", "\n@see ");
        StringBuilder builder = new StringBuilder();
        for (String raw : normalized.split("\n", -1)) {
            String line = raw.strip();
            if (line.startsWith("@param ")) {
                line = formatNamedTag(line, "@param");
            } else if (line.startsWith("@return")) {
                line = "**@return**" + line.substring("@return".length());
            } else if (line.startsWith("@throws ")) {
                line = formatNamedTag(line, "@throws");
            } else if (line.startsWith("@exception ")) {
                line = formatNamedTag(line, "@exception");
            } else if (line.startsWith("@see ")) {
                line = "**@see** " + line.substring("@see ".length());
            }
            if (!builder.isEmpty()) {
                builder.append('\n');
            }
            builder.append(line);
        }
        return builder.toString();
    }

    private static String formatNamedTag(final String line, final String tag) {
        String rest = line.substring(tag.length()).strip();
        int space = rest.indexOf(' ');
        if (space < 0) {
            return "**" + tag + "** `" + rest + "`";
        }
        return "**" + tag + "** `" + rest.substring(0, space) + "` " + rest.substring(space + 1);
    }
}
