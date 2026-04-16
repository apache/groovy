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

import javax.swing.text.StyleConstants
import javax.swing.text.StyleContext
import java.awt.Color
import java.util.prefs.Preferences

/**
 * Manages theme state (light/dark/system) for GroovyConsole.
 */
class ThemeManager {

    enum ThemeMode {
        LIGHT, DARK, SYSTEM
    }

    private static final Preferences prefs = Preferences.userNodeForPackage(Console)

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
        if (mode == ThemeMode.DARK || (mode == ThemeMode.SYSTEM && isSystemDarkMode())) {
            FlatDarkLaf.setup()
        } else {
            FlatLightLaf.setup()
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
        isDark() ? new Color(30, 30, 30) : Color.WHITE
    }

    static Color getOutputBackground() {
        isDark() ? new Color(43, 43, 43) : new Color(255, 255, 218)
    }

    static Map getStyles(String fontFamily) {
        isDark() ? getDarkStyles(fontFamily) : getLightStyles(fontFamily)
    }

    private static Map getLightStyles(String fontFamily) {
        [
            // output window styles
            regular: [
                (StyleConstants.FontFamily): fontFamily,
                (StyleConstants.Foreground): Color.BLACK
            ],
            prompt: [
                (StyleConstants.Foreground): new Color(0, 128, 0)
            ],
            command: [
                (StyleConstants.Foreground): Color.BLUE
            ],
            stacktrace: [
                (StyleConstants.Foreground): Color.RED.darker()
            ],
            hyperlink: [
                (StyleConstants.Foreground): Color.BLUE,
                (StyleConstants.Underline): true
            ],
            output: [
                (StyleConstants.Foreground): Color.BLACK
            ],
            result: [
                (StyleConstants.Foreground): Color.BLUE,
                (StyleConstants.Background): Color.YELLOW
            ],

            // syntax highlighting styles
            (StyleContext.DEFAULT_STYLE): [
                (StyleConstants.FontFamily): fontFamily
            ],
            (GroovyFilter.COMMENT): [
                (StyleConstants.Foreground): Color.LIGHT_GRAY.darker().darker(),
                (StyleConstants.Italic): true
            ],
            (GroovyFilter.QUOTES): [
                (StyleConstants.Foreground): Color.MAGENTA.darker().darker()
            ],
            (GroovyFilter.SINGLE_QUOTES): [
                (StyleConstants.Foreground): Color.GREEN.darker().darker()
            ],
            (GroovyFilter.SLASHY_QUOTES): [
                (StyleConstants.Foreground): Color.ORANGE.darker()
            ],
            (GroovyFilter.DIGIT): [
                (StyleConstants.Foreground): Color.RED.darker()
            ],
            (GroovyFilter.ANNOTATION): [
                (StyleConstants.Foreground): new Color(128, 128, 0)
            ],
            (GroovyFilter.OPERATION): [
                (StyleConstants.Bold): true
            ],
            (GroovyFilter.IDENT): [:],
            (GroovyFilter.RESERVED_WORD): [
                (StyleConstants.Bold): true,
                (StyleConstants.Foreground): Color.BLUE.darker().darker()
            ]
        ]
    }

    private static Map getDarkStyles(String fontFamily) {
        [
            // output window styles
            regular: [
                (StyleConstants.FontFamily): fontFamily,
                (StyleConstants.Foreground): new Color(204, 204, 204)
            ],
            prompt: [
                (StyleConstants.Foreground): new Color(106, 180, 101)
            ],
            command: [
                (StyleConstants.Foreground): new Color(104, 151, 187)
            ],
            stacktrace: [
                (StyleConstants.Foreground): new Color(204, 102, 102)
            ],
            hyperlink: [
                (StyleConstants.Foreground): new Color(104, 151, 187),
                (StyleConstants.Underline): true
            ],
            output: [
                (StyleConstants.Foreground): new Color(204, 204, 204)
            ],
            result: [
                (StyleConstants.Foreground): new Color(169, 183, 198),
                (StyleConstants.Background): new Color(50, 50, 80)
            ],

            // syntax highlighting styles
            (StyleContext.DEFAULT_STYLE): [
                (StyleConstants.FontFamily): fontFamily,
                (StyleConstants.Foreground): new Color(204, 204, 204)
            ],
            (GroovyFilter.COMMENT): [
                (StyleConstants.Foreground): new Color(160, 160, 160),
                (StyleConstants.Italic): true
            ],
            (GroovyFilter.QUOTES): [
                (StyleConstants.Foreground): new Color(220, 175, 240)
            ],
            (GroovyFilter.SINGLE_QUOTES): [
                (StyleConstants.Foreground): new Color(160, 225, 155)
            ],
            (GroovyFilter.SLASHY_QUOTES): [
                (StyleConstants.Foreground): new Color(235, 190, 130)
            ],
            (GroovyFilter.DIGIT): [
                (StyleConstants.Foreground): new Color(220, 150, 150)
            ],
            (GroovyFilter.ANNOTATION): [
                (StyleConstants.Foreground): new Color(210, 210, 130)
            ],
            (GroovyFilter.OPERATION): [
                (StyleConstants.Bold): true,
                (StyleConstants.Foreground): new Color(204, 204, 204)
            ],
            (GroovyFilter.IDENT): [
                (StyleConstants.Foreground): new Color(204, 204, 204)
            ],
            (GroovyFilter.RESERVED_WORD): [
                (StyleConstants.Bold): true,
                (StyleConstants.Foreground): new Color(180, 210, 240)
            ]
        ]
    }

    private static boolean isSystemDarkMode() {
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
