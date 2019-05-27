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

import javax.swing.KeyStroke
import java.awt.Toolkit
import java.awt.event.InputEvent
import java.awt.event.KeyEvent

newFileAction = action(
        name: 'New File',
        closure: controller.&fileNewFile,
        mnemonic: 'N',
        accelerator: shortcut('N'),
        smallIcon: imageIcon(resource: 'icons/page.png', class: this),
        shortDescription: 'New Groovy Script'
)

newWindowAction = action(
        name: 'New Window',
        closure: controller.&fileNewWindow,
        mnemonic: 'W',
        accelerator: shortcut('shift N')
)

openAction = action(
        name: 'Open',
        closure: controller.&fileOpen,
        mnemonic: 'O',
        accelerator: shortcut('O'),
        smallIcon: imageIcon(resource: 'icons/folder_page.png', class: this),
        shortDescription: 'Open Groovy Script'
)

saveAction = action(
        name: 'Save',
        closure: controller.&fileSave,
        mnemonic: 'S',
        accelerator: shortcut('S'),
        smallIcon: imageIcon(resource: 'icons/disk.png', class: this),
        shortDescription: 'Save Groovy Script',
        enabled: false // controller will enable as needed
)

saveAsAction = action(
        name: 'Save As...',
        closure: controller.&fileSaveAs,
        mnemonic: 'A',
)

printAction = action(
        name: 'Print...',
        closure: controller.&print,
        mnemonic: 'P',
        accelerator: shortcut('P')
)

exitAction = action(
        name: 'Exit',
        closure: controller.&exit,
        mnemonic: 'X'
// whether or not application exit should have an
// accelerator is debatable in usability circles
// at the very least a confirm dialog should dhow up
//accelerator: shortcut('Q')
)

undoAction = action(
        name: 'Undo',
        closure: controller.&undo,
        mnemonic: 'U',
        accelerator: shortcut('Z'),
        smallIcon: imageIcon(resource: 'icons/arrow_undo.png', class: this),
        shortDescription: 'Undo'
)

redoAction = action(
        name: 'Redo',
        closure: controller.&redo,
        mnemonic: 'R',
        accelerator: shortcut('shift Z'), // is control-shift-Z or control-Y more common?
        smallIcon: imageIcon(resource: 'icons/arrow_redo.png', class: this),
        shortDescription: 'Redo'
)

findAction = action(
        name: 'Find...',
        closure: controller.&find,
        mnemonic: 'F',
        accelerator: shortcut('F'),
        smallIcon: imageIcon(resource: 'icons/find.png', class: this),
        shortDescription: 'Find'
)

findNextAction = action(
        name: 'Find Next',
        closure: controller.&findNext,
        mnemonic: 'N',
        accelerator: KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0)
)

findPreviousAction = action(
        name: 'Find Previous',
        closure: controller.&findPrevious,
        mnemonic: 'V',
        accelerator: KeyStroke.getKeyStroke(KeyEvent.VK_F3, InputEvent.SHIFT_DOWN_MASK)
)

replaceAction = action(
        name: 'Replace...',
        closure: controller.&replace,
        mnemonic: 'E',
        accelerator: shortcut('H'),
        smallIcon: imageIcon(resource: 'icons/text_replace.png', class: this),
        shortDescription: 'Replace'
)

cutAction = action(
        name: 'Cut',
        closure: controller.&cut,
        mnemonic: 'T',
        accelerator: shortcut('X'),
        smallIcon: imageIcon(resource: 'icons/cut.png', class: this),
        shortDescription: 'Cut'
)

copyAction = action(
        name: 'Copy',
        closure: controller.&copy,
        mnemonic: 'C',
        accelerator: shortcut('C'),
        smallIcon: imageIcon(resource: 'icons/page_copy.png', class: this),
        shortDescription: 'Copy'
)

pasteAction = action(
        name: 'Paste',
        closure: controller.&paste,
        mnemonic: 'P',
        accelerator: shortcut('V'),
        smallIcon: imageIcon(resource: 'icons/page_paste.png', class: this),
        shortDescription: 'Paste'
)

selectAllAction = action(
        name: 'Select All',
        closure: controller.&selectAll,
        mnemonic: 'A',
        accelerator: shortcut('A')
)

historyPrevAction = action(
        name: 'Previous',
        closure: controller.&historyPrev,
        mnemonic: 'P',
        accelerator: shortcut(KeyEvent.VK_COMMA),
        smallIcon: imageIcon(resource: 'icons/book_previous.png', class: this),
        shortDescription: 'Previous Groovy Script',
        enabled: false // controller will enable as needed
)

historyNextAction = action(
        name: 'Next',
        closure: controller.&historyNext,
        mnemonic: 'N',
        accelerator: shortcut(KeyEvent.VK_PERIOD),
        smallIcon: imageIcon(resource: 'icons/book_next.png', class: this),
        shortDescription: 'Next Groovy Script',
        enabled: false // controller will enable as needed
)

clearOutputAction = action(
        name: 'Clear Output',
        closure: controller.&clearOutput,
        mnemonic: 'C',
        accelerator: shortcut('W'),
        smallIcon: imageIcon(resource: 'icons/clear.png', class: this),
        shortDescription: 'Clear Output Area'
)

runAction = action(
        name: 'Run',
        closure: controller.&runScript,
        mnemonic: 'R',
        keyStroke: shortcut('ENTER'),
        accelerator: shortcut('R'),
        smallIcon: imageIcon(resource: 'icons/script_go.png', class: this),
        shortDescription: 'Execute Groovy Script'
)

loopModeAction = action(
        name: 'Loop Mode',
        closure: controller.&loopMode,
        mnemonic: 'p',
        shortDescription: 'Run script continuously in a loop when run is envoked. Uncheck to stop loop'
)

runSelectionAction = action(
        name: 'Run Selection',
        closure: controller.&runSelectedScript,
        mnemonic: 'E',
        keyStroke: shortcut('shift ENTER'),
        accelerator: shortcut('shift R')
)

addClasspathJar = action(
        name: 'Add Jar(s) to ClassPath',
        closure: controller.&addClasspathJar,
        mnemonic: 'J',
)

addClasspathDir = action(
        name: 'Add Directory to ClassPath',
        closure: controller.&addClasspathDir,
        mnemonic: 'D',
)

listClasspath = action(
        name: 'List Classpath',
        closure: controller.&listClasspath
)

clearClassloader = action(
        name: 'Clear Script Context',
        closure: controller.&clearContext,
        mnemonic: 'C',
)

inspectLastAction = action(
        name: 'Inspect Last',
        closure: controller.&inspectLast,
        mnemonic: 'I',
        accelerator: shortcut('I')
)

inspectVariablesAction = action(
        name: 'Inspect Variables',
        closure: controller.&inspectVariables,
        mnemonic: 'V',
        accelerator: shortcut('J')
)

inspectAstAction = action(
        name: 'Inspect Ast',
        closure: controller.&inspectAst,
        mnemonic: 'A',
        accelerator: shortcut('T'),
)

inspectTokensAction = action(
        name: 'Inspect Tokens',
        closure: controller.&inspectTokens,
        mnemonic: 'T',
        accelerator: shortcut('K'),
)

captureStdOutAction = action(
        name: 'Capture Standard Output',
        closure: controller.&captureStdOut,
        mnemonic: 'O'
)

captureStdErrAction = action(
        name: 'Capture Standard Error Output',
        closure: controller.&captureStdErr,
        mnemonic: 'E'
)

fullStackTracesAction = action(
        name: 'Show Full Stack Traces',
        closure: controller.&fullStackTraces,
        mnemonic: 'F'
)

showScriptInOutputAction = action(
        name: 'Show Script in Output',
        closure: controller.&showScriptInOutput,
        mnemonic: 'R'
)

visualizeScriptResultsAction = action(
        name: 'Visualize Script Results',
        closure: controller.&visualizeScriptResults,
        mnemonic: 'V'
)

showToolbarAction = action(
        name: 'Show Toolbar',
        closure: controller.&showToolbar,
        mnemonic: 'T'
)

detachedOutputAction = action(
        name: 'Detached Output',
        closure: controller.&detachedOutput,
        mnemonic: 'D'
)

orientationVerticalAction = action(
        name: 'Vertical Orientation',
        closure: controller.&orientationVertical,
        mnemonic: 'n'
)

showOutputWindowAction = action(
        closure: controller.&showOutputWindow,
        keyStroke: shortcut('shift O'),
)

hideOutputWindowAction1 = action(
        closure: controller.&hideOutputWindow,
        keyStroke: 'SPACE',
)

hideOutputWindowAction2 = action(
        closure: controller.&hideOutputWindow,
        keyStroke: 'ENTER',
)

hideOutputWindowAction3 = action(
        closure: controller.&hideOutputWindow,
        keyStroke: 'ESCAPE',
)

hideOutputWindowAction4 = action(
        closure: controller.&hideAndClearOutputWindow,
        keyStroke: shortcut('W'),
)

autoClearOutputAction = action(
        name: 'Auto Clear Output On Run',
        closure: controller.&autoClearOutput,
        mnemonic: 'A'
)

saveOnRunAction = action(
        name: 'Auto Save on Runs',
        closure: controller.&saveOnRun,
        mnemonic: 'A'
)

largerFontAction = action(
        name: 'Larger Font',
        closure: controller.&largerFont,
        mnemonic: 'L',
        accelerator: shortcut('shift L')
)

smallerFontAction = action(
        name: 'Smaller Font',
        closure: controller.&smallerFont,
        mnemonic: 'S',
        accelerator: shortcut('shift S')
)

smartHighlighterAction = action(
        name: 'Enable Smart Highlighter',
        closure: controller.&smartHighlighter
)

aboutAction = action(
        name: 'About',
        closure: controller.&showAbout,
        mnemonic: 'A'
)

threadInterruptAction = action(
        name: 'Allow Interruption',
        closure: controller.&threadInterruption,
        mnemonic: 'O'
)

interruptAction = action(
        name: 'Interrupt',
        closure: controller.&doInterrupt,
        mnemonic: 'T',
        smallIcon: imageIcon(resource: 'icons/cross.png', class: this),
        shortDescription: 'Interrupt Running Script',
        enabled: false // controller will enable as needed
)

compileAction = action(
        name: 'Compile',
        closure: controller.&compileScript,
        mnemonic: 'L',
        accelerator: shortcut('L'),
        shortDescription: 'Compile Groovy Script'
)

commentAction = action(
        name: 'Comment',
        closure: controller.&comment,
        mnemonic: 'C',
        // Ctrl or Command + /
        accelerator: KeyStroke.getKeyStroke(KeyEvent.VK_SLASH, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
        shortDescription: 'Comment/Uncomment Selected Script'
)

selectBlockAction = action(
        name: 'Select Block',
        closure: controller.&selectBlock,
        mnemonic: 'B',
        accelerator: shortcut('B'),
        shortDescription: 'Selects current Word, Line or Block in Script'
)

indyAction = action(
        name: 'Enable Indy Compilation',
        closure: controller.&indy,
        mnemonic: 'I',
        shortDescription: 'Enable InvokeDynamic (Indy) compilation for scripts'
)

preferencesAction = action(
        name: 'Preferences',
        closure: controller.&preferences,
        mnemonic: 'S',
        shortDescription: 'Preference Settings'
)