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
package groovy.console.ui

import com.formdev.flatlaf.FlatDarkLaf
import com.formdev.flatlaf.FlatLightLaf
import groovy.console.ui.text.GroovyFilter

import javax.swing.UIManager
import javax.swing.text.StyleConstants
import javax.swing.text.StyleContext
import java.awt.Color
import java.util.concurrent.CopyOnWriteArrayList
import java.util.prefs.Preferences

/**
 * Manages theme state (light/dark/system) for GroovyConsole.
 */
class ThemeManager {

    enum ThemeMode {
        LIGHT, DARK, SYSTEM
    }

    private static final Preferences prefs = Preferences.userNodeForPackage(Console)

    // cached result of the OS-appearance probe; isSystemDarkMode is on hot paths
    // (icon color filters), so we avoid shelling out per call
    private static volatile Boolean cachedSystemDark = null

    // theme-change listeners — invoked after the LaF is installed so auxiliary
    // frames (AstBrowser, ObjectBrowser) can retint their own text panes/icons
    private static final List<Runnable> themeChangeListeners = new CopyOnWriteArrayList<>()

    static void addThemeChangeListener(Runnable listener) {
        themeChangeListeners << listener
    }

    static void removeThemeChangeListener(Runnable listener) {
        themeChangeListeners.remove(listener)
    }

    static void notifyThemeChanged() {
        themeChangeListeners.each { it.run() }
    }

    static ThemeMode getCurrentMode() {
        try {
            ThemeMode.valueOf(prefs.get('theme', 'SYSTEM').toUpperCase())
        } catch (IllegalArgumentException ignored) {
            ThemeMode.SYSTEM
        }
    }

    static boolean isDark() {
        def mode = currentMode
        mode == ThemeMode.DARK || (mode == ThemeMode.SYSTEM && isSystemDarkMode())
    }

    static void applyTheme(ThemeMode mode) {
        prefs.put('theme', mode.name())
        refreshSystemDarkMode()
        if (mode == ThemeMode.DARK || (mode == ThemeMode.SYSTEM && isSystemDarkMode())) {
            FlatDarkLaf.setup()
        } else {
            FlatLightLaf.setup()
        }
    }

    /** Re-probe OS appearance, update the cache, and report whether it changed. */
    static boolean refreshSystemDarkMode() {
        def prior = cachedSystemDark
        cachedSystemDark = probeSystemDarkMode()
        prior == null || prior != cachedSystemDark
    }

    static boolean isSystemDarkMode() {
        if (cachedSystemDark == null) {
            cachedSystemDark = probeSystemDarkMode()
        }
        cachedSystemDark
    }

    /**
     * On macOS with {@code apple.laf.useScreenMenuBar=true} the menu bar is drawn
     * by the OS rather than FlatLaf, so menu-item icons need to track the OS
     * appearance instead of the app theme to stay legible.
     */
    static boolean isMenuDrawnByOS() {
        String os = System.getProperty('os.name', '').toLowerCase()
        os.contains('mac') && Boolean.parseBoolean(System.getProperty('apple.laf.useScreenMenuBar', 'false'))
    }

    /** True iff menu-item icons will paint against a dark background. */
    static boolean isMenuDark() {
        isMenuDrawnByOS() ? isSystemDarkMode() : isDark()
    }

    /** Foreground color for menu-item icons — OS-tinted on the mac screen menu bar, app-tinted otherwise. */
    static Color getMenuIconForeground() {
        if (isMenuDrawnByOS()) {
            isSystemDarkMode() ? new Color(204, 204, 204) : Color.BLACK
        } else {
            UIManager.getColor('Label.foreground')
        }
    }

    static ThemeMode cycleMode() {
        switch (currentMode) {
            case ThemeMode.LIGHT: return ThemeMode.DARK
            case ThemeMode.DARK: return ThemeMode.SYSTEM
            default: return ThemeMode.LIGHT
        }
    }

    static String getThemeLabel() {
        switch (currentMode) {
            case ThemeMode.LIGHT: return 'Light'
            case ThemeMode.DARK: return 'Dark'
            default: return 'System'
        }
    }

    static Color getInputBackground() {
        activeTheme.inputBackground
    }

    static Color getOutputBackground() {
        activeTheme.outputBackground
    }

    static Map getStyles(String fontFamily) {
        buildSwingStyles(activeTheme, fontFamily)
    }

    /**
     * Returns the attributes for a single theme style key as a plain map
     * (foreground/background Colors plus bold/italic/underline Booleans).
     * Consumed by SmartDocumentFilter so ANTLR-token-driven highlighting
     * picks up the same theme values as the regex-based GroovyFilter styles.
     * Returns an empty map if the theme doesn't define the key.
     */
    static Map getStyleAttrs(String key) {
        if (!key) return [:]
        activeTheme.styles[key.toLowerCase()] ?: [:]
    }

    // --- theme loading + parsing ---

    private static final Map<String, Object> themeCache = [:]

    static String getCustomLightPath() { prefs.get('themeCustomLightPath', null) }
    static String getCustomDarkPath()  { prefs.get('themeCustomDarkPath',  null) }

    static void setCustomLightPath(String path) {
        path ? prefs.put('themeCustomLightPath', path) : prefs.remove('themeCustomLightPath')
    }

    static void setCustomDarkPath(String path) {
        path ? prefs.put('themeCustomDarkPath', path) : prefs.remove('themeCustomDarkPath')
    }

    /** True iff the theme currently in use is loaded from a user-supplied file. */
    static boolean isUsingCustomTheme() {
        def file = customFileForCurrentMode()
        file?.exists() && file.canRead()
    }

    /** Clear the parsed-theme cache so next lookup re-reads from disk/classpath. */
    static void reloadThemes() {
        synchronized (themeCache) { themeCache.clear() }
    }

    private static File customFileForCurrentMode() {
        String path = isDark() ? customDarkPath : customLightPath
        path ? new File(path) : null
    }

    private static Map getActiveTheme() {
        def file = customFileForCurrentMode()
        if (file?.exists() && file.canRead()) {
            try {
                return themeCache.computeIfAbsent('custom:' + file.absolutePath) { key ->
                    file.withReader('UTF-8') { r -> parseTheme(r) }
                }
            } catch (Exception ignored) {
                // fall through to bundled on parse failure
            }
        }
        loadBundledTheme(isDark() ? 'dark' : 'light')
    }

    private static Map loadBundledTheme(String name) {
        themeCache.computeIfAbsent('bundled:' + name) { key ->
            String resourcePath = "groovy/console/ui/themes/${name}.theme"
            def resource = ThemeManager.classLoader.getResourceAsStream(resourcePath)
            if (!resource) {
                throw new IllegalStateException("Missing bundled theme resource: ${resourcePath}")
            }
            resource.withStream { stream ->
                parseTheme(new InputStreamReader(stream, 'UTF-8'))
            }
        }
    }

    /**
     * Parses a .theme file (java.util.Properties format with our value sub-syntax)
     * into a structured theme: { inputBackground, outputBackground, styles: name→attrs }.
     * Each attrs map may contain foreground/background Colors and bold/italic/underline flags.
     * Unknown keys are silently ignored so theme files stay forward-compatible.
     */
    static Map parseTheme(Reader reader) {
        def props = new Properties()
        props.load(reader)
        def result = [inputBackground: null, outputBackground: null, styles: [:]]
        props.stringPropertyNames().each { key ->
            def value = props.getProperty(key)?.trim() ?: ''
            switch (key.trim().toLowerCase()) {
                case 'input.background':
                    result.inputBackground = parseHexColor(value)
                    break
                case 'output.background':
                    result.outputBackground = parseHexColor(value)
                    break
                default:
                    result.styles[key.trim().toLowerCase()] = parseStyleValue(value)
            }
        }
        result
    }

    private static Map parseStyleValue(String raw) {
        def attrs = [:]
        if (!raw) return attrs
        // split off "<fg> on <bg>"
        int onIdx = raw.toLowerCase().indexOf(' on ')
        String bg = null
        if (onIdx >= 0) {
            bg = raw.substring(onIdx + 4).trim()
            raw = raw.substring(0, onIdx).trim()
        }
        for (String part : raw.split(',')) {
            part = part.trim()
            if (!part) continue
            if (part.startsWith('#')) {
                attrs.foreground = parseHexColor(part)
            } else {
                switch (part.toLowerCase()) {
                    case 'bold':      attrs.bold = true;       break
                    case 'italic':    attrs.italic = true;     break
                    case 'underline': attrs.underline = true;  break
                }
            }
        }
        if (bg) attrs.background = parseHexColor(bg)
        attrs
    }

    private static Color parseHexColor(String hex) {
        hex = hex.trim()
        if (hex.startsWith('#')) hex = hex.substring(1)
        new Color(Integer.parseInt(hex, 16))
    }

    /**
     * Converts a parsed theme into the Swing-shaped style map consumed by the
     * output area (per-document styles keyed by String) and by the input area's
     * syntax highlighter (global StyleContext styles keyed by GroovyFilter
     * constants / StyleContext.DEFAULT_STYLE).
     */
    private static Map buildSwingStyles(Map theme, String fontFamily) {
        def result = [:]
        theme.styles.each { String name, Map attrs ->
            def key = resolveStyleKey(name)
            if (key == null) return
            def styleAttrs = [:]
            if (attrs.foreground) styleAttrs[StyleConstants.Foreground] = attrs.foreground
            if (attrs.background) styleAttrs[StyleConstants.Background] = attrs.background
            if (attrs.bold)       styleAttrs[StyleConstants.Bold]       = true
            if (attrs.italic)     styleAttrs[StyleConstants.Italic]     = true
            if (attrs.underline)  styleAttrs[StyleConstants.Underline]  = true
            result[key] = styleAttrs
        }
        // ensure regular + default carry the user-configured monospaced family
        result.computeIfAbsent('regular') { [:] }[StyleConstants.FontFamily] = fontFamily
        result.computeIfAbsent(StyleContext.DEFAULT_STYLE) { [:] }[StyleConstants.FontFamily] = fontFamily
        result
    }

    private static Object resolveStyleKey(String name) {
        switch (name) {
            // output window styles — literal String keys (per-document)
            case 'regular': case 'prompt': case 'command': case 'stacktrace':
            case 'hyperlink': case 'output': case 'result':
                return name
            // syntax-highlighting styles — global StyleContext keys
            case 'default':       return StyleContext.DEFAULT_STYLE
            case 'comment':       return GroovyFilter.COMMENT
            case 'quotes':        return GroovyFilter.QUOTES
            case 'single_quotes': return GroovyFilter.SINGLE_QUOTES
            case 'slashy_quotes': return GroovyFilter.SLASHY_QUOTES
            case 'digit':         return GroovyFilter.DIGIT
            case 'annotation':    return GroovyFilter.ANNOTATION
            case 'operation':     return GroovyFilter.OPERATION
            case 'ident':         return GroovyFilter.IDENT
            case 'reserved_word': return GroovyFilter.RESERVED_WORD
            default:              return null
        }
    }

    private static boolean probeSystemDarkMode() {
        String os = System.getProperty('os.name', '').toLowerCase()
        if (os.contains('mac')) {
            try {
                Process p = ['defaults', 'read', '-g', 'AppleInterfaceStyle'].execute()
                if (!p.waitFor(2, java.util.concurrent.TimeUnit.SECONDS)) {
                    p.destroyForcibly()
                    return false
                }
                return p.text.trim().equalsIgnoreCase('Dark')
            } catch (Exception ignored) {
                return false
            }
        } else if (os.contains('windows')) {
            try {
                Process p = ['reg', 'query',
                    'HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize',
                    '/v', 'AppsUseLightTheme'].execute()
                if (!p.waitFor(2, java.util.concurrent.TimeUnit.SECONDS)) {
                    p.destroyForcibly()
                    return false
                }
                return p.text.contains('0x0')
            } catch (Exception ignored) {
                return false
            }
        }
        // Linux: no reliable universal detection, default to light
        return false
    }
}
