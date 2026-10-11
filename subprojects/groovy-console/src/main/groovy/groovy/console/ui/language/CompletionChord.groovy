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
package groovy.console.ui.language

import groovy.transform.CompileStatic

import javax.swing.KeyStroke
import java.awt.event.InputEvent
import java.awt.event.KeyEvent

/**
 * Matches a key event against the configured completion stroke.
 * <p>
 * An exact key-code and modifier match always wins, so a stored
 * {@code ctrl SPACE} or {@code alt shift SLASH} means exactly that.
 * The default {@code alt SLASH} also accepts an extra Shift when the
 * event's character is already {@code '/'}. That is Swedish Shift+7
 * when the platform reports {@code keyChar} {@code '/'}
 * ({@code VK_7} or {@code VK_SLASH}). A press that carries no character
 * does not match by itself: the editor waits for the following
 * {@code keyTyped} {@code '/'}. US Alt+Shift+/ types {@code '?'} and
 * does not complete. macOS Swedish Option+Shift+7 types {@code '\'},
 * so the default chord does not fire there; store {@code ctrl SPACE}
 * instead.
 */
@CompileStatic
final class CompletionChord {

    private static final int DOWN_MASKS = InputEvent.ALT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK
            | InputEvent.META_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK | InputEvent.ALT_GRAPH_DOWN_MASK
    private static final int SHIFT = InputEvent.SHIFT_DOWN_MASK

    private CompletionChord() {
    }

    /**
     * A press stroke. {@code typed /} and {@code released} strokes are rejected
     * so the preference cannot store a binding the key listener will not see.
     *
     * @param spec Swing key-stroke string, or {@code null}
     * @return the stroke, or {@code null} when it is missing or the wrong kind
     */
    static KeyStroke parse(String spec) {
        if (spec == null) {
            return null
        }
        KeyStroke stroke = KeyStroke.getKeyStroke(spec.trim())
        if (stroke == null || stroke.keyCode == KeyEvent.VK_UNDEFINED || stroke.onKeyRelease) {
            return null
        }
        return stroke
    }

    /**
     * @param event key pressed or released
     * @param stroke configured stroke, or {@code null}
     * @return whether {@code event} should open completion
     */
    static boolean matches(KeyEvent event, KeyStroke stroke) {
        if (!pressStroke(event, stroke)) {
            return false
        }
        int expected = downMasks(stroke.modifiers)
        int actual = event.modifiersEx & DOWN_MASKS
        if (event.keyCode == stroke.keyCode && actual == expected) {
            return true
        }
        if (!slashShift(stroke, expected, actual)) {
            return false
        }
        return event.keyChar == SLASH
    }

    /**
     * The press did not carry a character, but the non-Shift modifiers match
     * a slash stroke and the key is one a layout uses for {@code /}
     * ({@code VK_SLASH} or a digit). The following {@code keyTyped} decides.
     *
     * @param event key pressed
     * @param stroke configured stroke, or {@code null}
     * @return whether to wait for a typed {@code /}
     */
    static boolean awaitsTypedSlash(KeyEvent event, KeyStroke stroke) {
        if (!pressStroke(event, stroke) || event.ID != KeyEvent.KEY_PRESSED) {
            return false
        }
        if (event.keyChar != KeyEvent.CHAR_UNDEFINED) {
            return false
        }
        int expected = downMasks(stroke.modifiers)
        int actual = event.modifiersEx & DOWN_MASKS
        if (!slashShift(stroke, expected, actual)) {
            return false
        }
        int code = event.keyCode
        return code == KeyEvent.VK_SLASH || (code >= KeyEvent.VK_0 && code <= KeyEvent.VK_9)
    }

    private static boolean pressStroke(KeyEvent event, KeyStroke stroke) {
        return event != null && stroke != null
                && stroke.keyCode != KeyEvent.VK_UNDEFINED
                && !stroke.onKeyRelease
    }

    private static boolean slashShift(KeyStroke stroke, int expected, int actual) {
        if (stroke.keyCode != KeyEvent.VK_SLASH) {
            return false
        }
        if ((actual & ~SHIFT) != (expected & ~SHIFT)) {
            return false
        }
        return (expected & SHIFT) == 0 || (actual & SHIFT) != 0
    }

    private static final char SLASH = '/' as char

    private static int downMasks(int modifiers) {
        int down = modifiers & DOWN_MASKS
        if ((modifiers & InputEvent.ALT_MASK) != 0) {
            down |= InputEvent.ALT_DOWN_MASK
        }
        if ((modifiers & InputEvent.CTRL_MASK) != 0) {
            down |= InputEvent.CTRL_DOWN_MASK
        }
        if ((modifiers & InputEvent.META_MASK) != 0) {
            down |= InputEvent.META_DOWN_MASK
        }
        if ((modifiers & InputEvent.SHIFT_MASK) != 0) {
            down |= InputEvent.SHIFT_DOWN_MASK
        }
        if ((modifiers & InputEvent.ALT_GRAPH_MASK) != 0) {
            down |= InputEvent.ALT_GRAPH_DOWN_MASK
        }
        return down
    }
}
