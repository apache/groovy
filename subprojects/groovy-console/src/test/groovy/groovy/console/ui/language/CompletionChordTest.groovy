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

import org.junit.jupiter.api.Test

import javax.swing.JTextPane
import javax.swing.KeyStroke
import java.awt.event.InputEvent
import java.awt.event.KeyEvent

final class CompletionChordTest {

    private final JTextPane source = new JTextPane()

    @Test
    void defaultAltSlashMatchesAndRejectsOtherChords() {
        KeyStroke altSlash = KeyStroke.getKeyStroke('alt SLASH')
        assert CompletionChord.matches(pressed(InputEvent.ALT_DOWN_MASK, KeyEvent.VK_SLASH, '/' as char), altSlash)
        assert !CompletionChord.matches(pressed(InputEvent.CTRL_DOWN_MASK, KeyEvent.VK_SPACE, ' ' as char), altSlash)
        assert !CompletionChord.matches(pressed(InputEvent.ALT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK,
                KeyEvent.VK_SLASH, '/' as char), altSlash)
        assert !CompletionChord.matches(null, altSlash)
        assert !CompletionChord.matches(pressed(InputEvent.ALT_DOWN_MASK, KeyEvent.VK_SLASH, '/' as char), null)
    }

    @Test
    void shiftThatProducesSlashIsAccepted() {
        KeyStroke altSlash = KeyStroke.getKeyStroke('alt SLASH')
        // Swedish layouts report '/' as Shift plus the slash key, or as Shift+7.
        assert CompletionChord.matches(pressed(InputEvent.ALT_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK,
                KeyEvent.VK_SLASH, '/' as char), altSlash)
        assert CompletionChord.matches(pressed(InputEvent.ALT_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK,
                KeyEvent.VK_7, '/' as char), altSlash)
        assert !CompletionChord.matches(pressed(InputEvent.ALT_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK,
                KeyEvent.VK_SLASH, KeyEvent.CHAR_UNDEFINED), altSlash)
        assert !CompletionChord.matches(pressed(InputEvent.ALT_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK,
                KeyEvent.VK_7, KeyEvent.CHAR_UNDEFINED), altSlash)
        assert CompletionChord.awaitsTypedSlash(pressed(InputEvent.ALT_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK,
                KeyEvent.VK_7, KeyEvent.CHAR_UNDEFINED), altSlash)
        assert !CompletionChord.awaitsTypedSlash(pressed(InputEvent.ALT_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK,
                KeyEvent.VK_A, KeyEvent.CHAR_UNDEFINED), altSlash)
        // US Alt+Shift+/ types '?'.
        assert !CompletionChord.matches(pressed(InputEvent.ALT_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK,
                KeyEvent.VK_SLASH, '?' as char), altSlash)
        assert !CompletionChord.matches(pressed(InputEvent.ALT_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK,
                KeyEvent.VK_A, 'A' as char), altSlash)
    }

    @Test
    void configuredStrokeIsExact() {
        KeyStroke ctrlSpace = KeyStroke.getKeyStroke('ctrl SPACE')
        assert CompletionChord.matches(pressed(InputEvent.CTRL_DOWN_MASK, KeyEvent.VK_SPACE, ' ' as char), ctrlSpace)
        assert !CompletionChord.matches(pressed(InputEvent.ALT_DOWN_MASK, KeyEvent.VK_SLASH, '/' as char), ctrlSpace)
        assert !CompletionChord.matches(pressed(InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK,
                KeyEvent.VK_SPACE, ' ' as char), ctrlSpace)

        KeyStroke shifted = KeyStroke.getKeyStroke('alt shift SLASH')
        assert CompletionChord.matches(pressed(InputEvent.ALT_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK,
                KeyEvent.VK_SLASH, '/' as char), shifted)
        assert !CompletionChord.matches(pressed(InputEvent.ALT_DOWN_MASK, KeyEvent.VK_SLASH, '/' as char), shifted)

        assert CompletionChord.parse('typed /') == null
        assert CompletionChord.parse('alt released SLASH') == null
        assert CompletionChord.parse('not a stroke') == null
        assert CompletionChord.parse('ctrl SPACE') != null

        KeyStroke altA = KeyStroke.getKeyStroke('alt A')
        assert CompletionChord.matches(pressed(InputEvent.ALT_DOWN_MASK, KeyEvent.VK_A, 'a' as char), altA)
        assert !CompletionChord.matches(pressed(InputEvent.ALT_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK,
                KeyEvent.VK_A, 'A' as char), altA)
    }

    @Test
    void legacyModifierMasksAreNormalized() {
        KeyStroke legacy = KeyStroke.getKeyStroke(KeyEvent.VK_SLASH, InputEvent.ALT_MASK)
        assert CompletionChord.matches(pressed(InputEvent.ALT_DOWN_MASK, KeyEvent.VK_SLASH, '/' as char), legacy)
        KeyStroke meta = KeyStroke.getKeyStroke(KeyEvent.VK_SLASH, InputEvent.META_MASK | InputEvent.ALT_GRAPH_MASK)
        assert CompletionChord.matches(pressed(InputEvent.META_DOWN_MASK | InputEvent.ALT_GRAPH_DOWN_MASK,
                KeyEvent.VK_SLASH, '/' as char), meta)
        KeyStroke ctrl = KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, InputEvent.CTRL_MASK)
        assert CompletionChord.matches(pressed(InputEvent.CTRL_DOWN_MASK, KeyEvent.VK_SPACE, ' ' as char), ctrl)
    }

    private KeyEvent pressed(int modifiers, int keyCode, char keyChar) {
        new KeyEvent(source, KeyEvent.KEY_PRESSED, 0, modifiers, keyCode, keyChar)
    }
}
