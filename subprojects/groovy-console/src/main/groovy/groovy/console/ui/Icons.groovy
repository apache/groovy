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

import com.formdev.flatlaf.extras.FlatSVGIcon

import javax.swing.Icon
import javax.swing.UIManager
import java.awt.Color
import java.awt.Component
import java.awt.Graphics
import java.util.function.Function

/**
 * Loads Material Symbols SVG icons and maps their fill color to the
 * active Look-and-Feel foreground. Selected usages go through {@link #green}
 * or {@link #red} to retain a colour cue from the earlier icon set.
 *
 * Two families of factory methods:
 *  - {@code load}/{@code green}/{@code red}          — fixed-size icons
 *    used in menus (and anywhere else that shouldn't scale).
 *  - {@code toolbar}/{@code toolbarGreen}/{@code toolbarRed}
 *                                                    — resizable icons
 *    wired to the toolbar; {@link #setSize(int)} resizes only these.
 *
 * Both families return {@link DynamicSVGIcon} proxies so the outer
 * reference held by Actions/components stays stable; {@link #refreshAll()}
 * forces every icon (toolbar and menu) to re-render — use after a LaF switch.
 */
class Icons {
    static final int SIZE_SMALL = 16
    static final int SIZE_NORMAL = 18
    static final int SIZE_LARGE = 28
    static final int MENU_SIZE = 18

    private static final String PATH = 'groovy/console/ui/icons/'
    private static int currentSize = SIZE_NORMAL

    private static final List<DynamicSVGIcon> allIcons = []
    private static final List<DynamicSVGIcon> toolbarIcons = []

    // ---- fixed-size icons (in-window menus, popups, AstBrowser/ObjectBrowser toolbars) ----

    static DynamicSVGIcon load(String name) {
        menuIcon(name, { Color c -> UIManager.getColor('Label.foreground') ?: c })
    }

    static DynamicSVGIcon green(String name)  { menuIcon(name, greenMapper({ ThemeManager.isDark() })) }
    static DynamicSVGIcon red(String name)    { menuIcon(name, redMapper({ ThemeManager.isDark() })) }
    static DynamicSVGIcon blue(String name)   { menuIcon(name, blueMapper({ ThemeManager.isDark() })) }
    static DynamicSVGIcon amber(String name)  { menuIcon(name, amberMapper({ ThemeManager.isDark() })) }
    static DynamicSVGIcon violet(String name) { menuIcon(name, violetMapper({ ThemeManager.isDark() })) }
    static DynamicSVGIcon teal(String name)   { menuIcon(name, tealMapper({ ThemeManager.isDark() })) }
    static DynamicSVGIcon javaBlue(String name) { menuIcon(name, javaBlueMapper({ ThemeManager.isDark() })) }
    static DynamicSVGIcon subtleBlue(String name) { menuIcon(name, subtleBlueMapper({ ThemeManager.isDark() })) }

    // ---- main menu-bar icons (tint follows the menu-bar background, which is OS-drawn on macOS) ----

    static DynamicSVGIcon menu(String name) {
        menuIcon(name, { Color c -> ThemeManager.menuIconForeground ?: c })
    }

    static DynamicSVGIcon menuGreen(String name)  { menuIcon(name, greenMapper({ ThemeManager.isMenuDark() })) }
    static DynamicSVGIcon menuRed(String name)    { menuIcon(name, redMapper({ ThemeManager.isMenuDark() })) }
    static DynamicSVGIcon menuBlue(String name)   { menuIcon(name, blueMapper({ ThemeManager.isMenuDark() })) }
    static DynamicSVGIcon menuAmber(String name)  { menuIcon(name, amberMapper({ ThemeManager.isMenuDark() })) }
    static DynamicSVGIcon menuViolet(String name) { menuIcon(name, violetMapper({ ThemeManager.isMenuDark() })) }
    static DynamicSVGIcon menuTeal(String name)   { menuIcon(name, tealMapper({ ThemeManager.isMenuDark() })) }
    static DynamicSVGIcon menuJavaBlue(String name) { menuIcon(name, javaBlueMapper({ ThemeManager.isMenuDark() })) }
    static DynamicSVGIcon menuSubtleBlue(String name) { menuIcon(name, subtleBlueMapper({ ThemeManager.isMenuDark() })) }

    // ---- toolbar / resizable ----

    static DynamicSVGIcon toolbar(String name) {
        toolbarIcon(name, { Color c -> UIManager.getColor('Label.foreground') ?: c })
    }

    static DynamicSVGIcon toolbarGreen(String name)  { toolbarIcon(name, greenMapper({ ThemeManager.isDark() })) }
    static DynamicSVGIcon toolbarRed(String name)    { toolbarIcon(name, redMapper({ ThemeManager.isDark() })) }
    static DynamicSVGIcon toolbarBlue(String name)   { toolbarIcon(name, blueMapper({ ThemeManager.isDark() })) }
    static DynamicSVGIcon toolbarAmber(String name)  { toolbarIcon(name, amberMapper({ ThemeManager.isDark() })) }
    static DynamicSVGIcon toolbarViolet(String name) { toolbarIcon(name, violetMapper({ ThemeManager.isDark() })) }
    static DynamicSVGIcon toolbarTeal(String name)   { toolbarIcon(name, tealMapper({ ThemeManager.isDark() })) }
    static DynamicSVGIcon toolbarJavaBlue(String name) { toolbarIcon(name, javaBlueMapper({ ThemeManager.isDark() })) }
    static DynamicSVGIcon toolbarSubtleBlue(String name) { toolbarIcon(name, subtleBlueMapper({ ThemeManager.isDark() })) }

    // Material "700" for light theme / "400" for dark — muted enough not to
    // shout next to grayscale neighbours, lifted in value for dark readability
    private static Closure<Color> greenMapper (Closure<Boolean> darkCheck) { hueMapper(darkCheck, 0x2E7D32, 0x81C784) }
    private static Closure<Color> redMapper   (Closure<Boolean> darkCheck) { hueMapper(darkCheck, 0xC62828, 0xEF5350) }
    private static Closure<Color> blueMapper  (Closure<Boolean> darkCheck) { hueMapper(darkCheck, 0x1565C0, 0x64B5F6) }
    private static Closure<Color> amberMapper (Closure<Boolean> darkCheck) { hueMapper(darkCheck, 0xEF6C00, 0xFFB74D) }
    private static Closure<Color> violetMapper(Closure<Boolean> darkCheck) { hueMapper(darkCheck, 0x7E57C2, 0xB39DDB) }
    private static Closure<Color> tealMapper  (Closure<Boolean> darkCheck) { hueMapper(darkCheck, 0x00838F, 0x4DD0E1) }
    // Official Java brand blue (#007396) for light; a lifted variant for dark
    private static Closure<Color> javaBlueMapper(Closure<Boolean> darkCheck) { hueMapper(darkCheck, 0x007396, 0x4DB6E3) }
    // Blue-leaning slate — reads as "almost the text colour" but with enough
    // blue to group related but unremarkable actions (cut/copy/paste).
    private static Closure<Color> subtleBlueMapper(Closure<Boolean> darkCheck) { hueMapper(darkCheck, 0x4A6A8A, 0xA0B5CC) }

    private static Closure<Color> hueMapper(Closure<Boolean> darkCheck, int light, int dark) {
        Color lightColor = new Color(light)
        Color darkColor = new Color(dark)
        return { Color c -> darkCheck() ? darkColor : lightColor }
    }

    private static DynamicSVGIcon menuIcon(String name, Closure<Color> mapper) {
        def icon = new DynamicSVGIcon(PATH + name + '.svg', MENU_SIZE, mapper)
        allIcons << icon
        icon
    }

    private static DynamicSVGIcon toolbarIcon(String name, Closure<Color> mapper) {
        def icon = new DynamicSVGIcon(PATH + name + '.svg', currentSize, mapper)
        allIcons << icon
        toolbarIcons << icon
        icon
    }

    /** Resize toolbar icons only — menu icons remain at {@link #MENU_SIZE}. */
    static void setSize(int size) {
        currentSize = size
        toolbarIcons.each { it.setSize(size) }
    }

    static int getCurrentSize() { currentSize }

    /** Re-run the colour filter so every icon adopts the new LaF foreground. */
    static void refreshAll() {
        allIcons.each { it.refreshColors() }
    }

    /**
     * Icon proxy whose inner FlatSVGIcon can be rebuilt at a new size
     * without changing the outer reference held by Swing components.
     */
    static class DynamicSVGIcon implements Icon {
        private final String path
        private final Closure<Color> colorMapper
        private FlatSVGIcon delegate
        private Icon disabledDelegate
        private int size

        DynamicSVGIcon(String path, int size, Closure<Color> colorMapper) {
            this.path = path
            this.colorMapper = colorMapper
            setSize(size)
        }

        void setSize(int newSize) {
            this.size = newSize
            rebuildDelegates()
        }

        void refreshColors() {
            // rebuild the FlatSVGIcon entirely — setColorFilter alone leaves the
            // internal raster cache in a state where some contexts (notably the
            // macOS screen menu bar) keep painting blank icons after a theme switch
            rebuildDelegates()
        }

        private void rebuildDelegates() {
            this.delegate = new FlatSVGIcon(path, size, size)
            delegate.setColorFilter(new FlatSVGIcon.ColorFilter(colorMapper as Function<Color, Color>))
            // FlatSVGIcon.getDisabledIcon() returns a grayscale variant; we
            // dispatch to it in paintIcon when the target component is disabled,
            // since Swing/FlatLaf can't derive one from a generic Icon wrapper
            this.disabledDelegate = delegate.getDisabledIcon()
        }

        @Override int getIconWidth() { size }
        @Override int getIconHeight() { size }
        @Override void paintIcon(Component c, Graphics g, int x, int y) {
            if (c != null && !c.isEnabled()) {
                disabledDelegate.paintIcon(c, g, x, y)
            } else {
                delegate.paintIcon(c, g, x, y)
            }
        }
    }
}
