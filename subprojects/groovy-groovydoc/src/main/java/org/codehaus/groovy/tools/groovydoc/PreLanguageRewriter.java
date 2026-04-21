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

import org.codehaus.groovy.runtime.ResourceGroovyMethods;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Post-process pass that retrofits syntax-highlighting classes onto bare
 * {@code <pre>} blocks in generated HTML. Used by both the Ant Groovydoc
 * task and the {@code groovydoc} CLI so their outputs stay identical.
 *
 * <p>Bare {@code <pre>...</pre>} blocks — openings with no attributes at
 * all — are rewritten to {@code <pre class="language-xxx"><code>...</code></pre>}
 * so Prism's highlighter picks them up (Prism only walks {@code <code>}
 * descendants of language-classed elements). A {@code <pre>} whose body
 * already contains a {@code <code>} element (e.g. the canonical form
 * emitted by {@code {@snippet}}) only receives the class on its opening
 * tag, to avoid nested {@code <code><code>}. Any {@code <pre>} with an
 * existing attribute — {@code class}, {@code id}, or other — is left
 * untouched.
 *
 * @since 6.0.0
 */
public final class PreLanguageRewriter {

    private static final Pattern PRE_PATTERN = Pattern.compile("<pre\\s*>([\\s\\S]*?)</pre>");

    private PreLanguageRewriter() {}

    /**
     * Returns {@code html} with bare {@code <pre>} blocks rewritten per the
     * class contract. If {@code preLanguage} is {@code null} or empty, the
     * input is returned unchanged.
     */
    public static String rewriteTags(String html, String preLanguage) {
        if (preLanguage == null || preLanguage.isEmpty()) return html;
        Matcher m = PRE_PATTERN.matcher(html);
        StringBuilder sb = new StringBuilder();
        String langClass = "language-" + preLanguage;
        while (m.find()) {
            String body = m.group(1);
            String replacement;
            if (body.contains("<code")) {
                replacement = "<pre class=\"" + langClass + "\">" + body + "</pre>";
            } else {
                replacement = "<pre class=\"" + langClass + "\"><code>" + body + "</code></pre>";
            }
            m.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    /**
     * Walks {@code dir} recursively and applies {@link #rewriteTags} to
     * every {@code .html} file in place. A no-op when {@code preLanguage}
     * is {@code null} or empty.
     */
    public static void rewriteDirectory(File dir, String preLanguage) {
        if (preLanguage == null || preLanguage.isEmpty()) return;
        File[] entries = dir.listFiles();
        if (entries == null) return;
        for (File f : entries) {
            if (f.isDirectory()) {
                rewriteDirectory(f, preLanguage);
            } else if (f.getName().endsWith(".html")) {
                try {
                    String html = ResourceGroovyMethods.getText(f);
                    String rewritten = rewriteTags(html, preLanguage);
                    if (!rewritten.equals(html)) {
                        ResourceGroovyMethods.setText(f, rewritten);
                    }
                } catch (IOException ignore) {
                    // leave file untouched on IO error; surfacing this via
                    // logger would require a logger dependency here, and the
                    // caller walks again on retry if needed
                }
            }
        }
    }
}
