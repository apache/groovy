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
package groovy.console.ui.view

import groovy.console.ui.Icons

import javax.swing.SwingConstants
import java.awt.BorderLayout

// Toolbar buttons override each action's smallIcon with a resizable toolbar-only
// icon — so View → Icon Size / Scale with Font affect toolbar icons only,
// not the matching menu items (which keep the fixed-size menu icons).
/** Toolbar shown above the editor and output area. */
toolbar = toolBar(rollover: true, visible: controller.showToolbar, constraints: BorderLayout.NORTH) {
    button(newFileAction,      text: null, icon: Icons.toolbar('note_add'))
    button(openAction,         text: null, icon: Icons.toolbar('folder_open'))
    button(saveAction,         text: null, icon: Icons.toolbar('save'))
    separator(orientation: SwingConstants.VERTICAL)
    button(undoAction,         text: null, icon: Icons.toolbar('undo'))
    button(redoAction,         text: null, icon: Icons.toolbar('redo'))
    separator(orientation: SwingConstants.VERTICAL)
    button(cutAction,          text: null, icon: Icons.toolbarSubtleBlue('content_cut'))
    button(copyAction,         text: null, icon: Icons.toolbarSubtleBlue('content_copy'))
    button(pasteAction,        text: null, icon: Icons.toolbarSubtleBlue('content_paste'))
    separator(orientation: SwingConstants.VERTICAL)
    button(findAction,         text: null, icon: Icons.toolbar('search'))
    button(replaceAction,      text: null, icon: Icons.toolbar('find_replace'))
    separator(orientation: SwingConstants.VERTICAL)
    button(historyPrevAction,  text: null, icon: Icons.toolbar('chevron_left'))
    button(historyNextAction,  text: null, icon: Icons.toolbar('chevron_right'))
    separator(orientation: SwingConstants.VERTICAL)
    button(runAction,          text: null, icon: Icons.toolbarGreen('play_arrow'))
    button(interruptAction,    text: null, icon: Icons.toolbarRed('stop_circle'))
    separator(orientation: SwingConstants.VERTICAL)
    button(clearOutputAction,  text: null, icon: Icons.toolbar('delete_sweep'))
    separator(orientation: SwingConstants.VERTICAL)
    button(smallerFontAction,  text: null, icon: Icons.toolbar('text_decrease'))
    button(largerFontAction,   text: null, icon: Icons.toolbar('text_increase'))
    separator(orientation: SwingConstants.VERTICAL)
    button(cycleThemeAction,   text: null, icon: Icons.toolbarViolet('refresh'))
}
