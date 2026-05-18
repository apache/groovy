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
package org.apache.groovy.groovysh.jline

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import org.jline.builtins.ConfigurationPath
import org.jline.console.Printer
import org.jline.console.ScriptEngine
import org.jline.console.impl.DefaultPrinter

import java.nio.file.DirectoryStream
import java.nio.file.Files
import java.nio.file.Path
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * A {@link DefaultPrinter} that resolves nanorc highlight-style names
 * case-insensitively.
 *
 * JLine matches the {@code /prnt -s STYLE} value (and the
 * {@code valueStyle}/{@code valueStyleAll} options) against the
 * {@code syntax "<NAME>"} header of each nanorc grammar with a
 * case-sensitive {@code String.equals} (in
 * {@code org.jline.builtins.SyntaxHighlighter}), so {@code -s json}
 * would not match {@code syntax "JSON"}.
 *
 * Rather than blindly transforming the requested style (which would
 * break selecting a user's existing mixed-case grammars copied into
 * {@code ~/.groovy} per the groovysh docs), this resolves the requested
 * name <em>case-insensitively against the actually configured syntax
 * names</em> — the same {@code jnanorc} {@link DefaultPrinter} itself
 * uses (user-config first, via {@link ConfigurationPath}) plus the
 * grammars it {@code include}s. An exact match always wins; a unique
 * case-insensitive match is rewritten to the real name; an unknown or
 * an <em>ambiguous</em> name (where two grammars differ only by case)
 * is passed through untouched so JLine's own behaviour is preserved.
 * Discovery is failsafe per file and overall, the resolved set is
 * cached, and the cache is cleared on {@link #refresh()} so a
 * {@code /highlighter} switch or a nanorc edit takes effect without a
 * restart.
 */
@CompileStatic
class GroovyPrinter extends DefaultPrinter {

    private static final List<String> STYLE_KEYS = [Printer.STYLE, Printer.VALUE_STYLE, Printer.VALUE_STYLE_ALL]
    private static final Pattern SYNTAX_HEADER = Pattern.compile(/(?m)^\s*syntax\s+"([^"]+)"/)
    private static final Pattern INCLUDE_LINE = Pattern.compile(/(?m)^\s*include\s+(\S+)/)

    private final ConfigurationPath configPath
    private Names names // cached resolved syntax names; built lazily, cleared on refresh

    GroovyPrinter(ScriptEngine engine, ConfigurationPath configPath) {
        super(engine, configPath)
        this.configPath = configPath
    }

    @Override
    void println(Map<String, Object> options, Object object) {
        super.println(normalizeStyles(options), object)
    }

    @Override
    protected void highlightAndPrint(Map<String, Object> options, Throwable exception) {
        super.highlightAndPrint(normalizeStyles(options), exception)
    }

    /**
     * Clears the cached syntax-name set before delegating, so a
     * {@code /highlighter} theme switch or an edited/copied nanorc is
     * picked up on the next print rather than after a restart.
     */
    @Override
    boolean refresh() {
        synchronized (this) {
            names = null
        }
        return super.refresh()
    }

    /**
     * Returns a copy of {@code options} with style-name values resolved to the
     * actual configured syntax name (case-insensitively), or the original map
     * untouched when there is nothing to change (so an immutable or shared
     * caller map is not mutated). Failsafe: any problem returns {@code options}
     * unchanged.
     */
    private Map<String, Object> normalizeStyles(Map<String, Object> options) {
        if (options == null || options.isEmpty()) {
            return options
        }
        Names resolved
        try {
            resolved = syntaxNames()
        } catch (Exception ignored) {
            return options
        }
        if (resolved.actual.isEmpty()) {
            return options
        }
        Map<String, Object> copy = null
        for (String key : STYLE_KEYS) {
            Object value = options.get(key)
            if (value instanceof CharSequence && (value as CharSequence).length() > 0) {
                String requested = value.toString()
                String style = resolveStyle(requested, resolved)
                if (style != requested) {
                    if (copy == null) {
                        copy = new LinkedHashMap<String, Object>(options)
                    }
                    copy.put(key, style)
                }
            }
        }
        copy != null ? copy : options
    }

    /**
     * Pure resolution (package-scoped for testing). An exact match (any
     * configured spelling) is returned unchanged — so a user's exact
     * {@code -s json} still selects their {@code syntax "json"} even when a
     * {@code "JSON"} also exists. Otherwise a unique case-insensitive match is
     * returned; an unknown or ambiguous name is returned unchanged.
     *
     * @param requested the user-supplied style name
     * @param names the resolved configured syntax names
     * @return the name JLine should be given
     */
    @PackageScope
    static String resolveStyle(String requested, Names names) {
        if (requested == null || requested.isEmpty()) {
            return requested
        }
        if (names.actual.contains(requested)) {
            return requested // exact match always wins (also covers user-config names)
        }
        String unique = names.uniqueByLower.get(requested.toLowerCase(Locale.ROOT))
        unique != null ? unique : requested // unique CI hit, else (unknown/ambiguous) passthrough
    }

    /** Resolved configured syntax names; cached, cleared by {@link #refresh()}. */
    private synchronized Names syntaxNames() {
        if (names != null) {
            return names
        }
        Map<String, Set<String>> raw = new LinkedHashMap<>()
        Path jnanorc = configPath?.getConfig('jnanorc')
        if (jnanorc != null && Files.isReadable(jnanorc)) {
            collectSyntaxNames(jnanorc, raw)
        }
        names = buildNames(raw)
        return names
    }

    /**
     * Scans the jnanorc itself and every file it {@code include}s for
     * {@code syntax "NAME"} headers, accumulating each lower-cased name to the
     * set of actual spellings seen. Per-file and per-include failsafe.
     */
    @PackageScope
    static void collectSyntaxNames(Path jnanorc, Map<String, Set<String>> raw) {
        scanForSyntax(jnanorc, raw) // jnanorc may declare syntaxes directly
        String text
        try {
            text = Files.readString(jnanorc) // UTF-8 per repository .editorconfig
        } catch (Exception ignored) {
            return
        }
        Path dir = jnanorc.toAbsolutePath().parent
        Matcher m = INCLUDE_LINE.matcher(text)
        while (m.find()) {
            String glob = m.group(1)
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, glob)) {
                for (Path p : stream) {
                    scanForSyntax(p, raw) // per-file failsafe inside
                }
            } catch (Exception ignored) {
                // one bad include directive must not stop the others
            }
        }
    }

    /** Builds the resolved view: every spelling is exact-matchable; a lower-cased
     *  key resolves only when exactly one spelling was seen for it. */
    @PackageScope
    static Names buildNames(Map<String, Set<String>> raw) {
        Set<String> actual = new LinkedHashSet<>()
        Map<String, String> uniqueByLower = new LinkedHashMap<>()
        for (Map.Entry<String, Set<String>> e : raw.entrySet()) {
            actual.addAll(e.value)
            if (e.value.size() == 1) {
                uniqueByLower.put(e.key, e.value.iterator().next())
            }
            // size > 1: ambiguous (e.g. both "JSON" and "json") -> no CI rewrite
        }
        new Names(actual, uniqueByLower)
    }

    /** Convenience builder (testing/clarity) from a flat collection of names. */
    @PackageScope
    static Names buildNames(Collection<String> syntaxNames) {
        Map<String, Set<String>> raw = new LinkedHashMap<>()
        for (String n : syntaxNames) {
            raw.computeIfAbsent(n.toLowerCase(Locale.ROOT), { k -> new LinkedHashSet<String>() }).add(n)
        }
        buildNames(raw)
    }

    /**
     * Per-file failsafe: a single malformed/unreadable nanorc never breaks
     * discovery of the rest.
     */
    private static void scanForSyntax(Path file, Map<String, Set<String>> raw) {
        try {
            if (file == null || !Files.isReadable(file)) {
                return
            }
            String content = Files.readString(file) // UTF-8 per repository .editorconfig
            Matcher m = SYNTAX_HEADER.matcher(content)
            while (m.find()) {
                String name = m.group(1)
                raw.computeIfAbsent(name.toLowerCase(Locale.ROOT), { k -> new LinkedHashSet<String>() }).add(name)
            }
        } catch (Exception ignored) {
            // skip just this file
        }
    }

    /** Immutable resolved syntax-name view. */
    @PackageScope
    @CompileStatic
    static final class Names {
        /** Every configured spelling (for O(1) exact match). */
        final Set<String> actual
        /** Lower-cased name -&gt; actual spelling, only where unambiguous. */
        final Map<String, String> uniqueByLower

        Names(Set<String> actual, Map<String, String> uniqueByLower) {
            this.actual = actual
            this.uniqueByLower = uniqueByLower
        }
    }
}
