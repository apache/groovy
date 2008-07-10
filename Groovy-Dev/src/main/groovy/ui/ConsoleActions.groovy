/*
 * Copyright 2003-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package groovy.ui

import java.awt.event.InputEvent
import java.awt.event.KeyEvent
import javax.swing.KeyStroke

newFileAction = action(
    name: 'New File',
    closure: controller.&fileNewFile,
    mnemonic: 'N',
    accelerator: shortcut('N'),
    smallIcon: imageIcon(resource:"icons/page.png", class:this),
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
    smallIcon: imageIcon(resource:"icons/folder_page.png", class:this),
    shortDescription: 'Open Groovy Script'
)

saveAction = action(
    name: 'Save',
    closure: controller.&fileSave,
    mnemonic: 'S',
    accelerator: shortcut('S'),
    smallIcon: imageIcon(resource:"icons/disk.png", class:this),
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
// accellerator is debatable in usability circles
// at the very least a confirm dialog should dhow up
//accelerator: shortcut('Q')
)

undoAction = action(
    name: 'Undo',
    closure: controller.&undo,
    mnemonic: 'U',
    accelerator: shortcut('Z'),
    smallIcon: imageIcon(resource:"icons/arrow_undo.png", class:this),
    shortDescription: 'Undo'
)

redoAction = action(
    name: 'Redo',
    closure: controller.&redo,
    mnemonic: 'R',
    accelerator: shortcut('shift Z'), // is control-shift-Z or control-Y more common?
    smallIcon: imageIcon(resource:"icons/arrow_redo.png", class:this),
    shortDescription: 'Redo'
)

findAction = action(
    name: 'Find...',
    closure: controller.&find,
    mnemonic: 'F',
    accelerator: shortcut('F'),
    smallIcon: imageIcon(resource:"icons/find.png", class:this),
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
    smallIcon: imageIcon(resource:"icons/text_replace.png", class:this),
    shortDescription: 'Replace'
)

cutAction = action(
    name: 'Cut',
    closure: controller.&cut,
    mnemonic: 'T',
    accelerator: shortcut('X'),
    smallIcon: imageIcon(resource:"icons/cut.png", class:this),
    shortDescription: 'Cut'
)

copyAction = action(
    name: 'Copy',
    closure: controller.&copy,
    mnemonic: 'C',
    accelerator: shortcut('C'),
    smallIcon: imageIcon(resource:"icons/page_copy.png", class:this),
    shortDescription: 'Copy'
)

pasteAction = action(
    name: 'Paste',
    closure: controller.&paste,
    mnemonic: 'P',
    accelerator: shortcut('V'),
    smallIcon: imageIcon(resource:"icons/page_paste.png", class:this),
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
    smallIcon: imageIcon(resource:"icons/book_previous.png", class:this),
    shortDescription: 'Previous Groovy Script',
    enabled: false // controller will enable as needed
)

historyNextAction = action(
    name: 'Next',
    closure: controller.&historyNext,
    mnemonic: 'N',
    accelerator: shortcut(KeyEvent.VK_PERIOD),
    smallIcon: imageIcon(resource:"icons/book_next.png", class:this),
    shortDescription: 'Next Groovy Script',
    enabled: false // controller will enable as needed
)

clearOutputAction = action(
    name: 'Clear Output',
    closure: controller.&clearOutput,
    mnemonic: 'O',
    accelerator: shortcut('W')
)

runAction = action(
    name: 'Run',
    closure: controller.&runScript,
    mnemonic: 'R',
    keyStroke: shortcut('ENTER'),
    accelerator: shortcut('R'),
    smallIcon: imageIcon(resource:"icons/script_go.png", class:this),
    shortDescription: 'Execute Groovy Script'
)

runSelectionAction = action(
    name: 'Run Selection',
    closure: controller.&runSelectedScript,
    mnemonic: 'E',
    keyStroke: shortcut('shift ENTER'),
    accelerator: shortcut('shift R')
)

addClasspathJar = action(
    name: 'Add Jar to ClassPath',
    closure: controller.&addClasspathJar,
    mnemonic: 'J',
)

addClasspathDir = action(
    name: 'Add Directory to ClassPath',
    closure: controller.&addClasspathDir,
    mnemonic: 'D',
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

captureStdOutAction = action(
    name: 'Capture Standard Output',
    closure: controller.&captureStdOut,
    mnemonic: 'C'
)

fullStackTracesAction = action(
    name: 'Show Full Stack Traces',
    closure: controller.&fullStackTraces,
    mnemonic: 'F'
)

showToolbarAction = action(
    name: 'Show Toolbar',
    closure: controller.&showToolbar,
    mnemonic: 'T'
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

aboutAction = action(
    name: 'About',
    closure: controller.&showAbout,
    mnemonic: 'A'
)

interruptAction = action(
    name: 'Interrupt',
    closure: controller.&confirmRunInterrupt
)
