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
import org.codehaus.groovy.groovydoc.GroovyFieldDoc;
import org.codehaus.groovy.groovydoc.GroovyMemberDoc;
import org.codehaus.groovy.groovydoc.GroovyMethodDoc;
import org.codehaus.groovy.groovydoc.GroovyParameter;
import org.codehaus.groovy.groovydoc.GroovyRootDoc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Single-pass tokenizer and renderer for Javadoc-style inline tags and
 * block tags in groovydoc text. Replaces the earlier chain of regex-based
 * passes in {@link SimpleGroovyClassDoc#replaceTags} and provides a single
 * extension point for new tags.
 *
 * <p>Supported inline tags: {@code {@link ...}}, {@code {@see ...}},
 * {@code {@code ...}}, {@code {@literal ...}} (class-level only),
 * {@code {@interface ...}} (swallowed), {@code {@value #FIELD}} /
 * {@code {@value pkg.Class#FIELD}} / bare {@code {@value}} on a field's
 * own comment (GROOVY-6016),
 * {@code {@inheritDoc}} (GROOVY-3782 — inline form, full parent comment
 * substituted), inline {@code {@snippet}} (GROOVY-11938 stage 1 — renders
 * as {@code <pre><code>} with language/id/class attributes and
 * HTML-escaped body).
 *
 * <p>Supported block tags: {@code @see}, {@code @param}, {@code @return},
 * {@code @throws} / {@code @exception}, {@code @since}, {@code @author},
 * {@code @version}, {@code @default}, plus synthesised {@code typeparam}
 * (from {@code @param <T>}). Unknown block tags fall through to a generic
 * {@code <DL>} rendering.
 *
 * <h2>Known limitations / pending work</h2>
 * <ul>
 *   <li>{@code {@snippet}} external form ({@code file="..."} /
 *       {@code region="..."} reading from a package's
 *       {@code snippet-files/} directory) not yet implemented — stage 2
 *       of GROOVY-11938.</li>
 *   <li>{@code {@snippet}} markup comments ({@code // @highlight},
 *       {@code @replace}, {@code @link}, {@code @start}/{@code @end})
 *       not yet implemented — stage 3 of GROOVY-11938.</li>
 *   <li>Client-side syntax highlighting assets (Prism.js / highlight.js)
 *       not yet wired in — stage 4 of GROOVY-11938.</li>
 *   <li>{@code {@inheritDoc}} inherits the full parent comment rather than
 *       position-aware substitution inside specific block tags (e.g.
 *       {@code @param x {@inheritDoc}} inheriting just the parent's
 *       {@code @param x}). Reasonable initial implementation.</li>
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
        // GROOVY-11945: three "note" tags standardised by Javadoc's stdlib
        // doclet since JEP 172. Recognised so the display heading is right
        // (not the raw lowercase tag name) and so the bodies are collated
        // with the rest of the block tags rather than emitted inline at
        // source position.
        COLLATED_TAGS.put("apiNote", "API Note");
        COLLATED_TAGS.put("implSpec", "Implementation Requirements");
        COLLATED_TAGS.put("implNote", "Implementation Note");
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
        return render(text, links, relPath, rootDoc, classDoc, null, CLASS_LEVEL);
    }

    static String render(String text,
                         List<LinkArgument> links,
                         String relPath,
                         GroovyRootDoc rootDoc,
                         SimpleGroovyClassDoc classDoc,
                         GroovyMemberDoc memberDoc) {
        return render(text, links, relPath, rootDoc, classDoc, memberDoc, CLASS_LEVEL);
    }

    static String render(String text,
                         List<LinkArgument> links,
                         String relPath,
                         GroovyRootDoc rootDoc,
                         SimpleGroovyClassDoc classDoc,
                         Config cfg) {
        return render(text, links, relPath, rootDoc, classDoc, null, cfg);
    }

    static String render(String text,
                         List<LinkArgument> links,
                         String relPath,
                         GroovyRootDoc rootDoc,
                         SimpleGroovyClassDoc classDoc,
                         GroovyMemberDoc memberDoc,
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
                int consumed = renderInlineTagAt(text, i, out, links, relPath, rootDoc, classDoc, memberDoc, cfg);
                if (consumed > 0) { i += consumed; continue; }
            }
            if (c == '@' && isBlockTagBoundary(text, i)) {
                int consumed = renderBlockTagAt(text, i, out, collated, links, relPath, rootDoc, classDoc, memberDoc, cfg);
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
                                         GroovyMemberDoc memberDoc,
                                         Config cfg) {
        int nameStart = start + 2; // skip '{@'
        int nameEnd = readTagName(text, nameStart);
        if (nameEnd == nameStart) return 0;
        String name = text.substring(nameStart, nameEnd);
        if (!isKnownInlineTag(name, cfg)) return 0;
        // GROOVY-11938: {@snippet} has JEP 413 body-parsing rules — attributes
        // before an optional `:`, then a brace-balanced body that may contain
        // source code with its own `{`/`}`. Needs its own parser.
        if ("snippet".equals(name)) {
            return renderSnippetAt(text, start, nameEnd, out, links, relPath, rootDoc, classDoc);
        }
        int bodyStart = skipWhitespace(text, nameEnd);
        // Match the previous regex's laxness: body extends to the first '}'.
        int close = text.indexOf('}', bodyStart);
        if (close < 0) return 0;
        String body = text.substring(bodyStart, close);
        renderInlineTag(name, body, out, links, relPath, rootDoc, classDoc, memberDoc, cfg);
        return close + 1 - start;
    }

    /**
     * GROOVY-11938: render a {@code {@snippet}} tag per JEP 413.
     *
     * <p>Stage 1 supports the inline form:
     * <pre>
     *   {@snippet [attr=value ...] :
     *       ... code body (may contain matched braces) ...
     *   }
     * </pre>
     * Emitted as {@code <pre><code class="language-xxx">...escaped body...</code></pre>}
     * with HTML-safe angle-bracket encoding of the body so code is preserved
     * verbatim. Attributes recognised so far: {@code lang} (→ language class),
     * {@code id}, {@code class} (attribute merged with language).
     *
     * <p>Deferred stages: external form with {@code file=...}/{@code region=...}
     * reading from a package's {@code snippet-files/} directory; markup comments
     * {@code // @highlight}, {@code // @replace}, {@code // @link},
     * {@code // @start}/{@code // @end} for annotated regions; client-side
     * syntax highlighting assets. See the ticket for the full scope.
     *
     * @return characters consumed starting from {@code start} (the {@code '{'})
     *         or 0 if the tag is malformed.
     */
    private static int renderSnippetAt(String text, int start, int nameEnd, StringBuilder out,
                                       List<LinkArgument> links, String relPath,
                                       GroovyRootDoc rootDoc, SimpleGroovyClassDoc classDoc) {
        int n = text.length();
        int i = skipWhitespace(text, nameEnd);

        Map<String, String> attrs = new LinkedHashMap<>();
        while (i < n) {
            char c = text.charAt(i);
            if (c == ':' || c == '}') break;
            if (Character.isWhitespace(c)) { i++; continue; }
            // Read attribute name.
            int nameS = i;
            while (i < n) {
                char ch = text.charAt(i);
                if (ch == '=' || ch == ':' || ch == '}' || Character.isWhitespace(ch)) break;
                i++;
            }
            String attrName = text.substring(nameS, i);
            i = skipWhitespace(text, i);
            String attrValue = "";
            if (i < n && text.charAt(i) == '=') {
                i++;
                i = skipWhitespace(text, i);
                if (i >= n) return 0;
                char q = text.charAt(i);
                // GROOVY-11938 stage 5: a Markdown body is HTML-escaped by
                // CommonMark before TagRenderer runs, so `"` appears as
                // `&quot;` (and `'` as `&apos;` / `&#39;`). Recognise those
                // entity forms as quote delimiters in addition to literal.
                String entityEnd = matchQuoteEntity(text, i);
                if (entityEnd != null) {
                    int close = text.indexOf(entityEnd, i + entityEnd.length());
                    if (close < 0) return 0;
                    attrValue = text.substring(i + entityEnd.length(), close);
                    i = close + entityEnd.length();
                } else if (q == '"' || q == '\'') {
                    int close = text.indexOf(q, i + 1);
                    if (close < 0) return 0;
                    attrValue = text.substring(i + 1, close);
                    i = close + 1;
                } else {
                    int valS = i;
                    while (i < n) {
                        char ch = text.charAt(i);
                        if (ch == ':' || ch == '}' || Character.isWhitespace(ch)) break;
                        i++;
                    }
                    attrValue = text.substring(valS, i);
                }
            }
            if (!attrName.isEmpty()) attrs.put(attrName, attrValue);
        }
        if (i >= n) return 0;

        String body;
        int endPos;
        if (text.charAt(i) == ':') {
            int bodyStart = i + 1;
            // Brace-balanced body — consume until the matching close of the
            // outer {@snippet ...}.
            int depth = 1;
            int j = bodyStart;
            while (j < n && depth > 0) {
                char c = text.charAt(j);
                if (c == '{') depth++;
                else if (c == '}') {
                    depth--;
                    if (depth == 0) break;
                }
                j++;
            }
            if (depth != 0) return 0; // unterminated
            body = text.substring(bodyStart, j);
            endPos = j + 1; // past the final '}'
        } else {
            // External form: {@snippet file="..." [region="..."]}
            // Read the referenced file from the current package's
            // snippet-files/ directory (already copied into output by
            // GROOVY-5986's resource-copy pass).
            String fileName = attrs.get("file");
            if (fileName == null || fileName.isEmpty()) return 0;
            String loaded = loadSnippetFile(fileName, rootDoc, classDoc);
            if (loaded == null) return 0;
            String region = attrs.get("region");
            if (region != null && !region.isEmpty()) {
                String extracted = extractRegion(loaded, region);
                if (extracted == null) return 0;
                loaded = extracted;
            }
            body = loaded;
            endPos = text.charAt(i) == '}' ? i + 1 : 0;
            if (endPos == 0) return 0;
        }

        String langClass = attrs.getOrDefault("lang", "");
        String extraClass = attrs.getOrDefault("class", "");
        String combined = (langClass.isEmpty() ? "" : "language-" + langClass)
                + (extraClass.isEmpty() ? "" : (langClass.isEmpty() ? extraClass : " " + extraClass));
        String dedented = dedent(body);
        // GROOVY-11938 stage 3: process JEP 413 markup comments (// @highlight,
        // // @replace, // @link) inside the snippet body. Markup-comment lines
        // are stripped from the rendered output; their directives annotate the
        // target line with bold/italic/highlight wrappers, inline replacements,
        // or hyperlinks.
        String processed = processSnippetMarkup(dedented, links, relPath, rootDoc, classDoc);
        out.append("<pre><code");
        if (!combined.isEmpty()) out.append(" class=\"").append(combined).append('"');
        String id = attrs.get("id");
        if (id != null && !id.isEmpty()) out.append(" id=\"").append(id).append('"');
        out.append('>').append(processed).append("</code></pre>");
        return endPos - start;
    }

    /**
     * GROOVY-11938 stage 2: look up a snippet file in the current package's
     * {@code snippet-files/} directory under any configured sourcepath and
     * return its contents, or {@code null} if not found.
     */
    private static String loadSnippetFile(String fileName, GroovyRootDoc rootDoc, SimpleGroovyClassDoc classDoc) {
        if (classDoc == null || !(rootDoc instanceof SimpleGroovyRootDoc)) return null;
        String[] sourcepaths = ((SimpleGroovyRootDoc) rootDoc).getSourcepaths();
        if (sourcepaths == null || sourcepaths.length == 0) return null;
        // Package path is fullPathName minus the class simple name.
        String full = classDoc.getFullPathName();
        if (full == null) return null;
        int lastSlash = full.lastIndexOf('/');
        String pkgPath = lastSlash >= 0 ? full.substring(0, lastSlash) : "";
        for (String sourcepath : sourcepaths) {
            Path path = Paths.get(sourcepath, pkgPath, "snippet-files", fileName);
            if (Files.isRegularFile(path)) {
                try {
                    return Files.readString(path);
                } catch (IOException e) {
                    return null;
                }
            }
        }
        return null;
    }

    /**
     * GROOVY-11938 stage 2: extract a named region from a snippet file.
     * Regions are bounded by {@code // @start region=NAME} and
     * {@code // @end [region=NAME]} markers. The markers themselves are
     * stripped from the returned slice. Returns {@code null} if the region
     * isn't found.
     */
    private static String extractRegion(String source, String regionName) {
        String[] lines = source.split("\n", -1);
        int startIdx = -1;
        int endIdx = -1;
        Pattern startPat = Pattern.compile(
                "//\\s*@start\\b[^\\n]*\\bregion\\s*=\\s*(['\"]?)" + Pattern.quote(regionName) + "\\1");
        Pattern endPat = Pattern.compile(
                "//\\s*@end\\b(?:[^\\n]*\\bregion\\s*=\\s*(['\"]?)" + Pattern.quote(regionName) + "\\1)?");
        for (int k = 0; k < lines.length; k++) {
            if (startIdx < 0 && startPat.matcher(lines[k]).find()) {
                startIdx = k + 1;
            } else if (startIdx >= 0 && endPat.matcher(lines[k]).find()) {
                endIdx = k;
                break;
            }
        }
        if (startIdx < 0 || endIdx < 0) return null;
        StringBuilder sb = new StringBuilder();
        for (int k = startIdx; k < endIdx; k++) {
            if (k > startIdx) sb.append('\n');
            sb.append(lines[k]);
        }
        return sb.toString();
    }

    /**
     * GROOVY-11938 stage 3: JEP 413 markup-comment processor. Walks the snippet
     * body line-by-line looking for {@code // @highlight}, {@code // @replace},
     * and {@code // @link} directives. Directive comments are stripped from
     * the output. Each directive targets the same line if written after code,
     * or the next non-directive line if written standalone.
     *
     * <p>Directive forms accepted:
     * <ul>
     *   <li>{@code @highlight [substring="..."|regex="..."] [type="bold|italic|highlighted"]}
     *       — wraps matching text in {@code <b>}/{@code <i>}/{@code <mark>}.
     *       Default type is {@code bold}.</li>
     *   <li>{@code @replace [substring="..."|regex="..."] replacement="..."}
     *       — substitutes matching text.</li>
     *   <li>{@code @link [substring="..."|regex="..."] target="Class[#member]"}
     *       — resolves {@code target} via the existing {@code {@link}} machinery
     *       and wraps matching text in an {@code <a href="...">} anchor.</li>
     * </ul>
     *
     * <p>Out of scope in this pass: region-scoped variants
     * ({@code region="name"}), which are a natural follow-up layered on top of
     * the existing external-file region machinery.
     */
    private static String processSnippetMarkup(String body, List<LinkArgument> links, String relPath,
                                               GroovyRootDoc rootDoc, SimpleGroovyClassDoc classDoc) {
        String[] lines = body.split("\n", -1);
        List<Directive> pending = new ArrayList<>();
        // GROOVY-11938 stage 3 region support: directives that carry a
        // `region="name"` attribute activate at their own line and remain in
        // effect until the matching `// @end` marker (or end of snippet).
        List<Directive> regionActive = new ArrayList<>();
        StringBuilder out = new StringBuilder();
        boolean first = true;
        for (String line : lines) {
            // Region markers `// @start region="name"` / `// @end [region="name"]`
            // are stripped from the output regardless of any directive state.
            if (isRegionStart(line) != null) continue;
            String endRegion = isRegionEnd(line);
            if (endRegion != null) {
                deactivateRegion(regionActive, endRegion);
                continue;
            }
            int commentIdx = findMarkupCommentStart(line);
            Directive parsed = null;
            if (commentIdx >= 0) {
                String commentText = line.substring(commentIdx + 2).trim();
                parsed = parseDirective(commentText);
            }
            if (parsed != null) {
                String region = parsed.attrs.get("region");
                String before = line.substring(0, commentIdx);
                if (region != null && !region.isEmpty()) {
                    // Region-scoped directive — activate, drop this line.
                    regionActive.add(parsed);
                    continue;
                }
                if (before.trim().isEmpty()) {
                    // Standalone directive — queue for next code line, drop this line.
                    pending.add(parsed);
                    continue;
                } else {
                    // EOL directive — apply to the code portion only (trailing
                    // whitespace trimmed off), drop the directive comment.
                    String code = before.replaceFirst("\\s+$", "");
                    List<Directive> effective = new ArrayList<>(regionActive);
                    effective.add(parsed);
                    String rendered = applyDirectives(code, effective, links, relPath, rootDoc, classDoc);
                    if (!first) out.append('\n');
                    out.append(rendered);
                    first = false;
                    continue;
                }
            }
            // Normal line — apply active region directives plus any queued
            // standalone directives (latter only consumed once).
            List<Directive> effective = new ArrayList<>(regionActive);
            effective.addAll(pending);
            String rendered = applyDirectives(line, effective, links, relPath, rootDoc, classDoc);
            pending.clear();
            if (!first) out.append('\n');
            out.append(rendered);
            first = false;
        }
        return out.toString();
    }

    /** Return the region name if the line is a `// @start region="name"` marker, else {@code null}. */
    private static String isRegionStart(String line) {
        Matcher m = Pattern.compile("^\\s*//\\s*@start\\b([^\\n]*)").matcher(line);
        if (!m.find()) return null;
        Matcher r = Pattern.compile("region\\s*=\\s*(['\"]?)([^'\"\\s]+)\\1").matcher(m.group(1));
        return r.find() ? r.group(2) : "";
    }

    /** Return the region name if the line is a `// @end [region="name"]` marker, empty string for an unnamed `// @end`, or {@code null} otherwise. */
    private static String isRegionEnd(String line) {
        Matcher m = Pattern.compile("^\\s*//\\s*@end\\b([^\\n]*)").matcher(line);
        if (!m.find()) return null;
        Matcher r = Pattern.compile("region\\s*=\\s*(['\"]?)([^'\"\\s]+)\\1").matcher(m.group(1));
        return r.find() ? r.group(2) : "";
    }

    /** Remove directives from {@code regionActive} that match the given end-region name. An unnamed {@code // @end} deactivates every region-scoped directive. */
    private static void deactivateRegion(List<Directive> regionActive, String endRegion) {
        if (endRegion.isEmpty()) { regionActive.clear(); return; }
        regionActive.removeIf(d -> endRegion.equals(d.attrs.get("region")));
    }

    /** Scan a line for the start of a recognised markup comment ({@code //} + optional space + {@code @highlight}/{@code @replace}/{@code @link}); returns the offset of the {@code //} or {@code -1}. */
    private static int findMarkupCommentStart(String line) {
        int i = 0, n = line.length();
        while (i <= n - 2) {
            if (line.charAt(i) == '/' && line.charAt(i + 1) == '/') {
                int j = i + 2;
                while (j < n && (line.charAt(j) == ' ' || line.charAt(j) == '\t')) j++;
                if (j < n && line.charAt(j) == '@') {
                    int k = j + 1;
                    while (k < n && Character.isLetterOrDigit(line.charAt(k))) k++;
                    String name = line.substring(j + 1, k);
                    if ("highlight".equals(name) || "replace".equals(name) || "link".equals(name)) {
                        return i;
                    }
                }
            }
            i++;
        }
        return -1;
    }

    /** Parsed markup directive with name + attributes. */
    private static final class Directive {
        final String name;
        final Map<String, String> attrs;
        Directive(String name, Map<String, String> attrs) { this.name = name; this.attrs = attrs; }
    }

    /** Parse a string of the form {@code @name attr1="v1" attr2=v2} into a {@link Directive}. Returns {@code null} on unrecognised names or malformed input. */
    private static Directive parseDirective(String s) {
        if (s == null || !s.startsWith("@")) return null;
        int i = 1, n = s.length();
        while (i < n && Character.isLetterOrDigit(s.charAt(i))) i++;
        String name = s.substring(1, i);
        if (!"highlight".equals(name) && !"replace".equals(name) && !"link".equals(name)) return null;
        Map<String, String> attrs = new LinkedHashMap<>();
        while (i < n) {
            while (i < n && Character.isWhitespace(s.charAt(i))) i++;
            if (i >= n) break;
            int nameS = i;
            while (i < n && (Character.isLetterOrDigit(s.charAt(i)) || s.charAt(i) == '_')) i++;
            String attrName = s.substring(nameS, i);
            if (attrName.isEmpty()) break;
            while (i < n && Character.isWhitespace(s.charAt(i))) i++;
            String val = "";
            if (i < n && s.charAt(i) == '=') {
                i++;
                while (i < n && Character.isWhitespace(s.charAt(i))) i++;
                if (i < n && (s.charAt(i) == '"' || s.charAt(i) == '\'')) {
                    char q = s.charAt(i);
                    int close = s.indexOf(q, i + 1);
                    if (close < 0) return null;
                    val = s.substring(i + 1, close);
                    i = close + 1;
                } else {
                    int valS = i;
                    while (i < n && !Character.isWhitespace(s.charAt(i))) i++;
                    val = s.substring(valS, i);
                }
            }
            attrs.put(attrName, val);
        }
        return new Directive(name, attrs);
    }

    /** Apply a list of directives to a single line, HTML-escaping untouched content. */
    private static String applyDirectives(String line, List<Directive> directives,
                                          List<LinkArgument> links, String relPath,
                                          GroovyRootDoc rootDoc, SimpleGroovyClassDoc classDoc) {
        String escaped = SimpleGroovyClassDoc.encodeAngleBrackets(line);
        if (directives.isEmpty()) return escaped;
        for (Directive d : directives) {
            escaped = applyDirective(escaped, d, links, relPath, rootDoc, classDoc);
        }
        return escaped;
    }

    /** Apply one directive to an already HTML-escaped line. */
    private static String applyDirective(String escaped, Directive d,
                                         List<LinkArgument> links, String relPath,
                                         GroovyRootDoc rootDoc, SimpleGroovyClassDoc classDoc) {
        Pattern pat = buildMatchPattern(d);
        if (pat == null) return escaped; // no match clause — nothing to do
        Matcher m = pat.matcher(escaped);
        StringBuilder sb = new StringBuilder();
        int last = 0;
        while (m.find()) {
            sb.append(escaped, last, m.start());
            String match = m.group();
            String wrapped = wrapForDirective(match, d, links, relPath, rootDoc, classDoc);
            sb.append(wrapped);
            last = m.end();
        }
        sb.append(escaped, last, escaped.length());
        return sb.toString();
    }

    /**
     * Build a regex matching the directive's {@code substring} or {@code regex}
     * attribute, run against the already-escaped line text. Returns {@code null}
     * when neither attribute is present.
     */
    private static Pattern buildMatchPattern(Directive d) {
        String substring = d.attrs.get("substring");
        String regex = d.attrs.get("regex");
        if (substring != null && !substring.isEmpty()) {
            // Escape the user's literal substring with the same angle-bracket
            // encoder applied to the line, then quote for regex.
            String escapedSub = SimpleGroovyClassDoc.encodeAngleBrackets(substring);
            return Pattern.compile(Pattern.quote(escapedSub));
        }
        if (regex != null && !regex.isEmpty()) {
            try {
                return Pattern.compile(regex);
            } catch (PatternSyntaxException e) {
                return null;
            }
        }
        return null;
    }

    /** Produce the HTML wrapping that a directive applies to one match. */
    private static String wrapForDirective(String match, Directive d,
                                           List<LinkArgument> links, String relPath,
                                           GroovyRootDoc rootDoc, SimpleGroovyClassDoc classDoc) {
        switch (d.name) {
            case "highlight": {
                String type = d.attrs.getOrDefault("type", "bold");
                switch (type) {
                    case "italic":      return "<i>" + match + "</i>";
                    case "highlighted": return "<mark>" + match + "</mark>";
                    case "bold":
                    default:            return "<b>" + match + "</b>";
                }
            }
            case "replace": {
                String replacement = d.attrs.getOrDefault("replacement", "");
                return SimpleGroovyClassDoc.encodeAngleBrackets(replacement);
            }
            case "link": {
                String target = d.attrs.getOrDefault("target", "");
                if (target.isEmpty()) return match;
                String url = SimpleGroovyClassDoc.getDocUrl(target, false, links, relPath, rootDoc, classDoc);
                // getDocUrl returns HTML like `<a href='...' title='...'>text</a>` —
                // pull just the href and wrap the match in our own anchor so the
                // visible text remains the code's own identifier.
                Matcher hrefM = Pattern.compile("href='([^']+)'").matcher(url);
                if (hrefM.find()) {
                    return "<a href=\"" + hrefM.group(1) + "\">" + match + "</a>";
                }
                return match;
            }
            default:
                return match;
        }
    }

    /**
     * Strip leading blank lines from a snippet body and remove the common
     * indentation (smallest non-empty leading-whitespace prefix) — matches
     * JEP 413's convention so indentation in the source doesn't leak into
     * the rendered snippet.
     */
    private static String dedent(String body) {
        // Drop the first line if it's only whitespace (typical when the body
        // starts right after the `:` with a newline).
        int firstNl = body.indexOf('\n');
        if (firstNl >= 0) {
            boolean firstLineBlank = true;
            for (int k = 0; k < firstNl; k++) {
                if (!Character.isWhitespace(body.charAt(k))) { firstLineBlank = false; break; }
            }
            if (firstLineBlank) body = body.substring(firstNl + 1);
        }
        // Strip trailing whitespace on the final line (body text before the
        // closing `}` often has one) so `</code></pre>` lands cleanly.
        int end = body.length();
        while (end > 0 && (body.charAt(end - 1) == ' ' || body.charAt(end - 1) == '\t')) end--;
        body = body.substring(0, end);
        // Compute common leading indentation across non-blank lines.
        String[] lines = body.split("\n", -1);
        int common = Integer.MAX_VALUE;
        for (String line : lines) {
            if (line.isEmpty()) continue;
            int lead = 0;
            while (lead < line.length() && (line.charAt(lead) == ' ' || line.charAt(lead) == '\t')) lead++;
            if (lead == line.length()) continue; // all-whitespace line
            if (lead < common) common = lead;
        }
        if (common == Integer.MAX_VALUE || common == 0) return body;
        StringBuilder sb = new StringBuilder(body.length());
        for (int li = 0; li < lines.length; li++) {
            if (li > 0) sb.append('\n');
            String line = lines[li];
            if (line.length() >= common) sb.append(line, common, line.length());
            else sb.append(line);
        }
        return sb.toString();
    }

    private static boolean isKnownInlineTag(String name, Config cfg) {
        switch (name) {
            case "link":
            case "see":
            case "code":
            case "interface":
            case "value":
            case "inheritDoc":
            case "snippet":
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
                                        GroovyMemberDoc memberDoc,
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
                // GROOVY-6016: resolve {@value #FIELD} or {@value Class#FIELD}.
                // Bare {@value} resolves to the enclosing field's own value
                // when the comment is attached to a field (Javadoc semantics).
                String resolved;
                if (body.isEmpty() && memberDoc instanceof GroovyFieldDoc) {
                    resolved = ((GroovyFieldDoc) memberDoc).constantValueExpression();
                } else {
                    resolved = resolveValueTag(body, rootDoc, classDoc);
                }
                if (resolved != null) {
                    out.append(SimpleGroovyClassDoc.encodeAngleBrackets(resolved));
                    return;
                }
                break; // unresolved — emit verbatim
            }
            case "inheritDoc": {
                // GROOVY-3782: only meaningful on a method; pull the parent
                // method's already-rendered comment text into this position.
                String inherited = resolveInheritDoc(memberDoc, classDoc, new HashSet<>());
                if (inherited != null) {
                    out.append(inherited);
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
    /**
     * GROOVY-3782: resolve {@code {@inheritDoc}} to the parent method's
     * already-rendered comment text. Walks the superclass chain, then
     * declared interfaces, looking for a method with the same name and
     * matching parameter type names. Returns {@code null} if the current
     * member isn't a method, no parent method is found, or the parent
     * method has no doc.
     *
     * <p>Recursion safety: the {@code visited} set tracks methods we've
     * already expanded on this chain to prevent infinite loops (e.g. two
     * interfaces with {@code {@inheritDoc}} pointing at each other).</p>
     */
    private static String resolveInheritDoc(GroovyMemberDoc memberDoc,
                                            SimpleGroovyClassDoc classDoc,
                                            Set<GroovyMethodDoc> visited) {
        if (!(memberDoc instanceof GroovyMethodDoc)) return null;
        GroovyMethodDoc thisMethod = (GroovyMethodDoc) memberDoc;
        if (!visited.add(thisMethod)) return null; // cycle
        if (classDoc == null) return null;

        GroovyMethodDoc parent = findInheritedMethod(thisMethod, classDoc, new HashSet<>());
        if (parent == null) return null;
        // Calling commentText() on the parent runs it through the full
        // replaceTags pipeline — so any {@inheritDoc} nested in the parent
        // resolves recursively via its own ancestry, and our `visited` set
        // blocks cycles.
        String rendered = parent.commentText();
        return rendered == null ? "" : rendered;
    }

    private static GroovyMethodDoc findInheritedMethod(GroovyMethodDoc thisMethod,
                                                       GroovyClassDoc startingClass,
                                                       Set<GroovyClassDoc> seen) {
        // Javadoc's search order: superclass chain first, then interfaces.
        for (GroovyClassDoc sup = startingClass.superclass(); sup != null; sup = sup.superclass()) {
            if (!seen.add(sup)) break;
            GroovyMethodDoc match = findMatchingMethod(sup, thisMethod);
            if (match != null) return match;
        }
        for (GroovyClassDoc iface : startingClass.interfaces()) {
            if (!seen.add(iface)) continue;
            GroovyMethodDoc match = findMatchingMethod(iface, thisMethod);
            if (match != null) return match;
            GroovyMethodDoc deeper = findInheritedMethod(thisMethod, iface, seen);
            if (deeper != null) return deeper;
        }
        return null;
    }

    private static GroovyMethodDoc findMatchingMethod(GroovyClassDoc cls, GroovyMethodDoc target) {
        String targetName = target.name();
        GroovyParameter[] targetParams = target.parameters();
        for (GroovyMethodDoc m : cls.methods()) {
            if (!targetName.equals(m.name())) continue;
            GroovyParameter[] params = m.parameters();
            if (params.length != targetParams.length) continue;
            boolean allMatch = true;
            for (int i = 0; i < params.length; i++) {
                String a = params[i].typeName();
                String b = targetParams[i].typeName();
                if (a == null ? b != null : !a.equals(b)) { allMatch = false; break; }
            }
            if (allMatch) return m;
        }
        return null;
    }

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
        for (GroovyFieldDoc f : target.fields()) {
            if (fieldName.equals(f.name())) return f.constantValueExpression();
        }
        for (GroovyFieldDoc f : target.enumConstants()) {
            if (fieldName.equals(f.name())) return f.constantValueExpression();
        }
        for (GroovyFieldDoc f : target.properties()) {
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
                                        GroovyMemberDoc memberDoc,
                                        Config cfg) {
        int nameStart = start + 1; // skip '@'
        int nameEnd = readTagName(text, nameStart);
        if (nameEnd == nameStart) return 0;
        int n = text.length();
        if (nameEnd >= n || !Character.isWhitespace(text.charAt(nameEnd))) return 0;
        int bodyStart = skipWhitespace(text, nameEnd);
        int bodyEnd = findBlockTagEnd(text, bodyStart);
        String name = text.substring(nameStart, nameEnd);
        int trimmedEnd = bodyEnd;
        while (trimmedEnd > bodyStart && Character.isWhitespace(text.charAt(trimmedEnd - 1))) {
            trimmedEnd--;
        }
        String rawBody = text.substring(bodyStart, trimmedEnd);
        handleBlockTag(name, rawBody, out, collated, links, relPath, rootDoc, classDoc, memberDoc, cfg);
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
                                       GroovyMemberDoc memberDoc,
                                       Config cfg) {
        if ("interface".equals(name)) return; // swallow, matches prior behaviour

        // Block-tag bodies may embed inline tags like {@link ...}; render those
        // now so that collation sees fully-rendered HTML.
        String body = renderInline(rawBody, links, relPath, rootDoc, classDoc, memberDoc, cfg);

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
                                       GroovyMemberDoc memberDoc,
                                       Config cfg) {
        StringBuilder out = new StringBuilder(text.length());
        int i = 0;
        int n = text.length();
        while (i < n) {
            char c = text.charAt(i);
            if (c == '{' && i + 1 < n && text.charAt(i + 1) == '@') {
                int consumed = renderInlineTagAt(text, i, out, links, relPath, rootDoc, classDoc, memberDoc, cfg);
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
     * GROOVY-11938 stage 5: detect an HTML-entity-encoded quote delimiter at
     * position {@code i} in {@code text}. Returns the exact sequence
     * ({@code "&quot;"}, {@code "&apos;"}, {@code "&#34;"}, or
     * {@code "&#39;"}) when matched so the caller can find the matching
     * closing sequence; returns {@code null} otherwise.
     */
    private static String matchQuoteEntity(String text, int i) {
        if (text.startsWith("&quot;", i)) return "&quot;";
        if (text.startsWith("&apos;", i)) return "&apos;";
        if (text.startsWith("&#34;", i)) return "&#34;";
        if (text.startsWith("&#39;", i)) return "&#39;";
        return null;
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
