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

/**
 * Turns a Groovydoc comment into compact Markdown for hover.
 */
public final class GroovydocMarkdown {

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

    private static String htmlToMarkdown(final String text) {
        String result = text.replace("<br>", "\n").replace("<br/>", "\n").replace("<br />", "\n");
        result = result.replace("<p>", "\n\n").replace("</p>", "");
        result = result.replace("<b>", "**").replace("</b>", "**");
        result = result.replace("<strong>", "**").replace("</strong>", "**");
        result = result.replace("<i>", "*").replace("</i>", "*");
        result = result.replace("<em>", "*").replace("</em>", "*");
        result = result.replace("<code>", "`").replace("</code>", "`");
        StringBuilder builder = new StringBuilder();
        int i = 0;
        while (i < result.length()) {
            char c = result.charAt(i);
            if (c == '<') {
                int close = result.indexOf('>', i + 1);
                if (close < 0) {
                    builder.append(result, i, result.length());
                    break;
                }
                i = close + 1;
            } else {
                builder.append(c);
                i++;
            }
        }
        return builder.toString();
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
