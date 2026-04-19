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

    static DynamicSVGIcon green(String name) {
        menuIcon(name, greenMapper({ ThemeManager.isDark() }))
    }

    static DynamicSVGIcon red(String name) {
        menuIcon(name, redMapper({ ThemeManager.isDark() }))
    }

    // ---- main menu-bar icons (tint follows the menu-bar background, which is OS-drawn on macOS) ----

    static DynamicSVGIcon menu(String name) {
        menuIcon(name, { Color c -> ThemeManager.menuIconForeground ?: c })
    }

    static DynamicSVGIcon menuGreen(String name) {
        menuIcon(name, greenMapper({ ThemeManager.isMenuDark() }))
    }

    static DynamicSVGIcon menuRed(String name) {
        menuIcon(name, redMapper({ ThemeManager.isMenuDark() }))
    }

    // ---- toolbar / resizable ----

    static DynamicSVGIcon toolbar(String name) {
        toolbarIcon(name, { Color c -> UIManager.getColor('Label.foreground') ?: c })
    }

    static DynamicSVGIcon toolbarGreen(String name) {
        toolbarIcon(name, greenMapper({ ThemeManager.isDark() }))
    }

    static DynamicSVGIcon toolbarRed(String name) {
        toolbarIcon(name, redMapper({ ThemeManager.isDark() }))
    }

    private static Closure<Color> greenMapper(Closure<Boolean> darkCheck) {
        Color light = new Color(0x2E7D32)
        Color dark = new Color(0x81C784)
        return { Color c -> darkCheck() ? dark : light }
    }

    private static Closure<Color> redMapper(Closure<Boolean> darkCheck) {
        Color light = new Color(0xC62828)
        Color dark = new Color(0xEF5350)
        return { Color c -> darkCheck() ? dark : light }
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
        private int size

        DynamicSVGIcon(String path, int size, Closure<Color> colorMapper) {
            this.path = path
            this.colorMapper = colorMapper
            setSize(size)
        }

        void setSize(int newSize) {
            this.size = newSize
            this.delegate = new FlatSVGIcon(path, newSize, newSize)
            refreshColors()
        }

        void refreshColors() {
            // rebuild the FlatSVGIcon entirely — setColorFilter alone leaves the
            // internal raster cache in a state where some contexts (notably the
            // macOS screen menu bar) keep painting blank icons after a theme switch
            this.delegate = new FlatSVGIcon(path, size, size)
            delegate.setColorFilter(new FlatSVGIcon.ColorFilter(colorMapper as Function<Color, Color>))
        }

        @Override int getIconWidth() { size }
        @Override int getIconHeight() { size }
        @Override void paintIcon(Component c, Graphics g, int x, int y) { delegate.paintIcon(c, g, x, y) }
    }
}
