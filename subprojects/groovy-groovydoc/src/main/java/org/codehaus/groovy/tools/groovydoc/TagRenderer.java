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

import org.codehaus.groovy.groovydoc.GroovyClassDoc;
import org.codehaus.groovy.groovydoc.GroovyRootDoc;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Single-pass tokenizer and renderer for Javadoc-style inline tags and
 * block tags in groovydoc text. Replaces the earlier chain of regex-based
 * passes in {@link SimpleGroovyClassDoc#replaceTags} and provides a single
 * extension point for new tags.
 *
 * <p>Supported inline tags: {@code {@link ...}}, {@code {@see ...}},
 * {@code {@code ...}}, {@code {@literal ...}} (class-level only),
 * {@code {@interface ...}} (swallowed), {@code {@value #FIELD}} /
 * {@code {@value pkg.Class#FIELD}} (GROOVY-6016).
 *
 * <p>Supported block tags: {@code @see}, {@code @param}, {@code @return},
 * {@code @throws} / {@code @exception}, {@code @since}, {@code @author},
 * {@code @version}, {@code @default}, plus synthesised {@code typeparam}
 * (from {@code @param <T>}). Unknown block tags fall through to a generic
 * {@code <DL>} rendering.
 *
 * <h2>Known limitations / pending work</h2>
 * <ul>
 *   <li>Bare {@code {@value}} (no body, referencing the current field's own
 *       constant value) is not yet supported. It requires threading the
 *       current member through {@code TagRenderer}; the tag currently falls
 *       through to verbatim emission. {@code {@value #FIELD}} and
 *       {@code {@value Class#FIELD}} both work.</li>
 *   <li>{@code {@inheritDoc}} — GROOVY-3782, pending.</li>
 *   <li>{@code {@snippet}} — GROOVY-11938, pending.</li>
 *   <li>JEP 467 Markdown reference links — GROOVY-11542, pending.</li>
 * </ul>
 *
 * <p>Output is byte-for-byte compatible with the previous multi-pass
 * implementation for inputs the old code handled; see the GROOVY-11939
 * commit notes for the small set of semantic improvements introduced by
 * the refactor (e.g. {@code {@linkplain X}} passed through verbatim
 * instead of broken as a bogus block tag).
 */
final class TagRenderer {

    /** Block-tag names that get merged under a single display heading. */
    static final Map<String, String> COLLATED_TAGS = new LinkedHashMap<>();
    static {
        COLLATED_TAGS.put("see", "See Also");
        COLLATED_TAGS.put("param", "Parameters");
        COLLATED_TAGS.put("throw", "Throws");
        COLLATED_TAGS.put("exception", "Throws");
        COLLATED_TAGS.put("return", "Returns");
        COLLATED_TAGS.put("since", "Since");
        COLLATED_TAGS.put("author", "Authors");
        COLLATED_TAGS.put("version", "Version");
        COLLATED_TAGS.put("default", "Default");
        // typeparam is synthesised from `@param <T> desc`
        COLLATED_TAGS.put("typeparam", "Type Parameters");
    }

    private static final String BLOCK_PRE_KEY = "<DL><DT><B>";
    private static final String BLOCK_POST_KEY = ":</B></DT><DD>";
    private static final String BLOCK_VALUE_SEP = "</DD><DD>";
    private static final String BLOCK_POST_VALUES = "</DD></DL>";

    /**
     * Rendering profile — class-level docs vs package-level docs historically
     * produced slightly different HTML for the same tags. Captured here so one
     * tokenizer implementation can drive both.
     */
    static final class Config {
        final String codeOpen;
        final String codeClose;
        final boolean literalEnabled;
        final boolean collateBlockTags;

        Config(String codeOpen, String codeClose, boolean literalEnabled, boolean collateBlockTags) {
            this.codeOpen = codeOpen;
            this.codeClose = codeClose;
            this.literalEnabled = literalEnabled;
            this.collateBlockTags = collateBlockTags;
        }
    }

    /** Config used when rendering tags in a class-level doc comment. */
    static final Config CLASS_LEVEL = new Config("<CODE>", "</CODE>", true, true);

    /**
     * Config used when rendering tags in a package-info doc comment. Historical
     * quirks: {@code {@code}} wraps with {@code <TT>} (a deprecated HTML tag)
     * rather than {@code <CODE>}; {@code {@literal}} is not recognised; block
     * tags are emitted inline rather than collated. Preserved for byte-for-byte
     * parity with the previous {@code GroovyRootDocBuilder.replaceTags}
     * implementation.
     */
    static final Config PACKAGE_LEVEL = new Config("<TT>", "</TT>", false, false);

    private TagRenderer() {}

    /**
     * Render inline and block tags in {@code text}, emitting an HTML fragment.
     * Equivalent to the previous {@code replaceTags} + {@code replaceAllTags}
     * + {@code replaceAllTagsCollated} pipeline, collapsed into one pass.
     */
    static String render(String text,
                         List<LinkArgument> links,
                         String relPath,
                         GroovyRootDoc rootDoc,
                         SimpleGroovyClassDoc classDoc) {
        return render(text, links, relPath, rootDoc, classDoc, CLASS_LEVEL);
    }

    static String render(String text,
                         List<LinkArgument> links,
                         String relPath,
                         GroovyRootDoc rootDoc,
                         SimpleGroovyClassDoc classDoc,
                         Config cfg) {
        StringBuilder out = new StringBuilder(text.length() + 64);
        // Whitespace preceding a block tag must be dropped (matches the
        // prior regex's leading `\s*` before `@`). Buffer whitespace runs
        // so we can discard them if followed by a block tag, or flush them
        // to output otherwise.
        StringBuilder pendingWs = new StringBuilder();
        Map<String, List<String>> collated = new LinkedHashMap<>();
        boolean anyBlockTag = false;
        int i = 0;
        int n = text.length();
        while (i < n) {
            char c = text.charAt(i);
            if (c == '{' && i + 1 < n && text.charAt(i + 1) == '@') {
                out.append(pendingWs);
                pendingWs.setLength(0);
                int consumed = renderInlineTagAt(text, i, out, links, relPath, rootDoc, classDoc, cfg);
                if (consumed > 0) { i += consumed; continue; }
            }
            if (c == '@' && isBlockTagBoundary(text, i)) {
                int consumed = renderBlockTagAt(text, i, out, collated, links, relPath, rootDoc, classDoc, cfg);
                if (consumed > 0) {
                    pendingWs.setLength(0); // discard ws preceding the tag
                    anyBlockTag = true;
                    i += consumed;
                    continue;
                }
            }
            if (Character.isWhitespace(c)) {
                pendingWs.append(c);
            } else {
                out.append(pendingWs);
                pendingWs.setLength(0);
                out.append(c);
            }
            i++;
        }
        out.append(pendingWs);
        // Package-level rendering always tacked on a trailing space (the old
        // `self + " @endMarker"` + substring(0, len-10) pipeline produced it
        // even when no block tags matched). Preserve for byte-for-byte parity.
        boolean emitTrailingSpace = anyBlockTag || !cfg.collateBlockTags;
        appendCollatedOrTrailingSpace(out, collated, emitTrailingSpace);
        return out.toString();
    }

    // -------- inline tags --------

    /**
     * Try to parse and render an inline tag starting at {@code start}.
     * Returns the number of characters consumed, or 0 if the position does
     * not actually begin a well-formed inline tag (in which case the caller
     * should emit the literal '{' and advance one character).
     */
    private static int renderInlineTagAt(String text, int start, StringBuilder out,
                                         List<LinkArgument> links, String relPath,
                                         GroovyRootDoc rootDoc, SimpleGroovyClassDoc classDoc,
                                         Config cfg) {
        int nameStart = start + 2; // skip '{@'
        int nameEnd = readTagName(text, nameStart);
        if (nameEnd == nameStart) return 0;
        String name = text.substring(nameStart, nameEnd);
        // Historical behaviour: only the known inline tags are recognised.
        // Anything else (e.g. Javadoc `{@linkplain}` or `{@inheritDoc}`) flows
        // through as plain text so that later passes / subsequent tickets
        // can decide how to handle it. Skipping unknown tags here preserves
        // byte-for-byte parity with the old LINK/LITERAL/CODE regex chain.
        if (!isKnownInlineTag(name, cfg)) return 0;
        int bodyStart = skipWhitespace(text, nameEnd);
        // Match the previous regex's laxness: body extends to the first '}'.
        int close = text.indexOf('}', bodyStart);
        if (close < 0) return 0;
        String body = text.substring(bodyStart, close);
        renderInlineTag(name, body, out, links, relPath, rootDoc, classDoc, cfg);
        return close + 1 - start;
    }

    private static boolean isKnownInlineTag(String name, Config cfg) {
        switch (name) {
            case "link":
            case "see":
            case "code":
            case "interface":
            case "value":
                return true;
            case "literal":
                return cfg.literalEnabled;
            default:
                return false;
        }
    }

    private static void renderInlineTag(String name, String body, StringBuilder out,
                                        List<LinkArgument> links, String relPath,
                                        GroovyRootDoc rootDoc, SimpleGroovyClassDoc classDoc,
                                        Config cfg) {
        switch (name) {
            case "interface":
                // Historical behaviour: swallow `{@interface ...}` so Javadoc-style
                // annotation declarations in comments don't pollute output.
                return;
            case "link":
            case "see":
                out.append(SimpleGroovyClassDoc.getDocUrl(body, false, links, relPath, rootDoc, classDoc));
                return;
            case "literal":
                if (cfg.literalEnabled) {
                    out.append(SimpleGroovyClassDoc.encodeAngleBrackets(body));
                    return;
                }
                break; // fall through to emit verbatim
            case "code":
                out.append(cfg.codeOpen).append(SimpleGroovyClassDoc.encodeAngleBrackets(body)).append(cfg.codeClose);
                return;
            case "value": {
                // GROOVY-6016: resolve {@value #FIELD} or {@value Class#FIELD}
                // to the field's source-form constant value. Bare {@value}
                // (current field) is not supported here because we don't
                // thread the current member through TagRenderer — fall back
                // to emitting the tag verbatim in that case.
                String resolved = resolveValueTag(body, rootDoc, classDoc);
                if (resolved != null) {
                    out.append(SimpleGroovyClassDoc.encodeAngleBrackets(resolved));
                    return;
                }
                break; // unresolved — emit verbatim
            }
        }
        // Unknown or unresolved inline tag — emit body verbatim (matches prior regex behaviour).
        out.append('{').append('@').append(name);
        if (!body.isEmpty()) out.append(' ').append(body);
        out.append('}');
    }

    /**
     * Resolve a {@code {@value}} tag body to the target field's source-form
     * constant value, or {@code null} if the reference couldn't be resolved.
     *
     * <p>Body forms:
     * <ul>
     *   <li>{@code #FIELD} — field {@code FIELD} in {@code classDoc}</li>
     *   <li>{@code Class#FIELD} / {@code pkg.Class#FIELD} — field in another class</li>
     *   <li>empty — not supported here (needs current-member context); returns {@code null}</li>
     * </ul>
     */
    private static String resolveValueTag(String body, GroovyRootDoc rootDoc, SimpleGroovyClassDoc classDoc) {
        if (body == null || body.isEmpty()) return null;
        String className;
        String fieldName;
        int hash = body.indexOf('#');
        if (hash < 0) {
            // Bare name — treat as same-class field ref (lenient).
            className = null;
            fieldName = body.trim();
        } else {
            className = body.substring(0, hash).trim();
            fieldName = body.substring(hash + 1).trim();
        }
        if (fieldName.isEmpty()) return null;
        SimpleGroovyClassDoc target = classDoc;
        if (className != null && !className.isEmpty() && classDoc != null && rootDoc != null) {
            String slashed = className.replace('.', '/');
            GroovyClassDoc resolved = rootDoc.classNamed(classDoc, slashed);
            if (resolved instanceof SimpleGroovyClassDoc) target = (SimpleGroovyClassDoc) resolved;
            else return null;
        }
        if (target == null) return null;
        for (org.codehaus.groovy.groovydoc.GroovyFieldDoc f : target.fields()) {
            if (fieldName.equals(f.name())) return f.constantValueExpression();
        }
        for (org.codehaus.groovy.groovydoc.GroovyFieldDoc f : target.enumConstants()) {
            if (fieldName.equals(f.name())) return f.constantValueExpression();
        }
        for (org.codehaus.groovy.groovydoc.GroovyFieldDoc f : target.properties()) {
            if (fieldName.equals(f.name())) return f.constantValueExpression();
        }
        return null;
    }

    // -------- block tags --------

    /**
     * Try to parse and render a block tag starting at {@code start}. A block
     * tag starts at {@code '@'} that sits at the start of the text or is
     * preceded by whitespace.
     */
    private static int renderBlockTagAt(String text, int start, StringBuilder out,
                                        Map<String, List<String>> collated,
                                        List<LinkArgument> links, String relPath,
                                        GroovyRootDoc rootDoc, SimpleGroovyClassDoc classDoc,
                                        Config cfg) {
        int nameStart = start + 1; // skip '@'
        int nameEnd = readTagName(text, nameStart);
        if (nameEnd == nameStart) return 0;
        // Require at least one whitespace after the tag name (mirrors the old
        // regex's `\s+` between name and body). Rejects things like
        // `@CompileStatic(TypeCheckingMode.SKIP)` mid-sentence — those should
        // stay as literal text, not be parsed as a block tag.
        int n = text.length();
        if (nameEnd >= n || !Character.isWhitespace(text.charAt(nameEnd))) return 0;
        int bodyStart = skipWhitespace(text, nameEnd);
        int bodyEnd = findBlockTagEnd(text, bodyStart);
        String name = text.substring(nameStart, nameEnd);
        // Trim trailing whitespace from the body, matching the prior regex's
        // non-greedy `(.*?)(?=\s+@)` / " @endMarker" behaviour.
        int trimmedEnd = bodyEnd;
        while (trimmedEnd > bodyStart && Character.isWhitespace(text.charAt(trimmedEnd - 1))) {
            trimmedEnd--;
        }
        String rawBody = text.substring(bodyStart, trimmedEnd);
        handleBlockTag(name, rawBody, out, collated, links, relPath, rootDoc, classDoc, cfg);
        // Advance only past the trimmed body — trailing whitespace flows back
        // through the main loop as plain text so that any whitespace trailing
        // the overall input (for the last block tag) is preserved verbatim
        // before the collated block is appended. This reproduces the old
        // " @endMarker" + substring(0, length-10) artifact exactly.
        return trimmedEnd - start;
    }

    private static void handleBlockTag(String name, String rawBody, StringBuilder out,
                                       Map<String, List<String>> collated,
                                       List<LinkArgument> links, String relPath,
                                       GroovyRootDoc rootDoc, SimpleGroovyClassDoc classDoc,
                                       Config cfg) {
        if ("interface".equals(name)) return; // swallow, matches prior behaviour

        // Block-tag bodies may embed inline tags like {@link ...}; render those
        // now so that collation sees fully-rendered HTML.
        String body = renderInline(rawBody, links, relPath, rootDoc, classDoc, cfg);

        if (cfg.collateBlockTags) {
            if ("see".equals(name) || "link".equals(name)) {
                body = SimpleGroovyClassDoc.getDocUrl(body, false, links, relPath, rootDoc, classDoc);
            } else if ("param".equals(name)) {
                int space = body.indexOf(' ');
                if (space >= 0) {
                    String paramName = body.substring(0, space);
                    String paramDesc = body.substring(space);
                    if (paramName.startsWith("<") && paramName.endsWith(">")) {
                        paramName = paramName.substring(1, paramName.length() - 1);
                        name = "typeparam";
                    }
                    body = "<code>" + paramName + "</code> - " + paramDesc;
                }
            }
            String displayName = COLLATED_TAGS.get(name);
            if (displayName != null) {
                collated.computeIfAbsent(displayName, k -> new ArrayList<>()).add(body);
                return;
            }
        }
        // Non-collated (or unknown) block tag — emit inline.
        out.append(BLOCK_PRE_KEY).append(name).append(BLOCK_POST_KEY)
           .append(body).append(BLOCK_POST_VALUES);
    }

    /**
     * Render inline tags only (no block-tag recognition). Used for block-tag
     * bodies which may themselves embed {@code {@link}} / {@code {@code}} /
     * {@code {@literal}}.
     */
    private static String renderInline(String text,
                                       List<LinkArgument> links, String relPath,
                                       GroovyRootDoc rootDoc, SimpleGroovyClassDoc classDoc,
                                       Config cfg) {
        StringBuilder out = new StringBuilder(text.length());
        int i = 0;
        int n = text.length();
        while (i < n) {
            char c = text.charAt(i);
            if (c == '{' && i + 1 < n && text.charAt(i + 1) == '@') {
                int consumed = renderInlineTagAt(text, i, out, links, relPath, rootDoc, classDoc, cfg);
                if (consumed > 0) { i += consumed; continue; }
            }
            out.append(c);
            i++;
        }
        return out.toString();
    }

    private static void appendCollatedOrTrailingSpace(StringBuilder out,
                                                      Map<String, List<String>> collated,
                                                      boolean emitTrailingSpace) {
        if (!emitTrailingSpace) return;
        // Historical quirk: the previous implementation appended " @endMarker"
        // to its input then stripped only 10 of those 11 chars after regex
        // replacement, leaving a single trailing space whenever any block tag
        // was present. Preserved here for byte-identical output.
        out.append(' ');
        for (Map.Entry<String, List<String>> e : collated.entrySet()) {
            out.append(BLOCK_PRE_KEY).append(e.getKey()).append(BLOCK_POST_KEY);
            List<String> values = e.getValue();
            for (int j = 0; j < values.size(); j++) {
                if (j > 0) out.append(BLOCK_VALUE_SEP);
                out.append(values.get(j));
            }
            out.append(BLOCK_POST_VALUES);
        }
    }

    // -------- scanning helpers --------

    /** Matches {@code [a-zA-Z.]+} (same character class used by the prior TAG_REGEX). */
    private static int readTagName(String text, int start) {
        int i = start;
        int n = text.length();
        while (i < n) {
            char c = text.charAt(i);
            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '.') {
                i++;
            } else {
                break;
            }
        }
        return i;
    }

    private static int skipWhitespace(String text, int start) {
        int i = start;
        int n = text.length();
        while (i < n && Character.isWhitespace(text.charAt(i))) i++;
        return i;
    }

    /**
     * A '@' begins a block tag only if it's at text start or preceded by
     * whitespace. The old TAG_REGEX had a laxer lookbehind ({@code \s*} —
     * matches zero whitespace, so things like {@code {@linkplain ...}} got
     * picked up as block tags even though the {@code '@'} was preceded by
     * {@code '{'}). The stricter check here is a semantic improvement: a
     * stray {@code '@'} sitting in the middle of a word or after punctuation
     * should not be treated as a tag.
     */
    private static boolean isBlockTagBoundary(String text, int i) {
        return i == 0 || Character.isWhitespace(text.charAt(i - 1));
    }

    /**
     * Find the end of a block-tag body starting at {@code bodyStart}. The
     * body extends up to (but not including) the whitespace preceding the
     * next block tag (a {@code '@'} preceded by whitespace and followed by
     * a valid tag name), or the end of the text.
     *
     * <p>Mirrors the lookahead {@code (?=\s+@)} in the former TAG_REGEX, with
     * the additional guard that an {@code '@'} sitting <em>inside</em> an
     * inline tag (e.g. {@code {@code @NotNull}}) does not terminate the block
     * tag. The previous implementation got away without this because it
     * replaced all inline tags before running the block-tag regex — any
     * {@code @whatever} inside {@code {@code ...}} was already wrapped in
     * HTML and the regex's {@code \s+@} lookahead no longer matched.</p>
     */
    private static int findBlockTagEnd(String text, int bodyStart) {
        int n = text.length();
        int i = bodyStart;
        while (i < n) {
            char c = text.charAt(i);
            if (c == '{' && i + 1 < n && text.charAt(i + 1) == '@') {
                int skipTo = skipInlineTag(text, i);
                if (skipTo > i) { i = skipTo; continue; }
            }
            if (Character.isWhitespace(c)) {
                int ws = i;
                int afterWs = i;
                while (afterWs < n && Character.isWhitespace(text.charAt(afterWs))) afterWs++;
                // Only terminate the block-tag body at a well-formed next tag
                // (`@<name>`). The old TAG_REGEX's lookahead was `\s+@` with
                // no name check, so things like `@-files` inside a body would
                // spuriously break it off — corrected here for semantic
                // equivalence rather than strict byte equivalence.
                if (afterWs < n && text.charAt(afterWs) == '@') {
                    int nameEnd = readTagName(text, afterWs + 1);
                    if (nameEnd > afterWs + 1) {
                        return ws;
                    }
                }
                i = afterWs;
            } else {
                i++;
            }
        }
        return n;
    }

    /** Position past the closing '}' of an inline tag starting at {@code start},
     *  or {@code start} itself if the tag is malformed (no closing '}' or no name). */
    private static int skipInlineTag(String text, int start) {
        int nameStart = start + 2; // past '{@'
        int nameEnd = readTagName(text, nameStart);
        if (nameEnd == nameStart) return start;
        int close = text.indexOf('}', nameEnd);
        if (close < 0) return start;
        return close + 1;
    }
}
