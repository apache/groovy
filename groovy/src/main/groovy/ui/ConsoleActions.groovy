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

action(id: 'newFileAction',
    name: 'New File',
    closure: controller.&fileNewFile,
    mnemonic: 'N',
    accelerator: shortcut('N'),
    smallIcon: imageIcon(resource:"icons/page.png", class:this),
    shortDescription: 'New Groovy Script'
)

action(id: 'newWindowAction',
    name: 'New Window',
    closure: controller.&fileNewWindow,
    mnemonic: 'W',
    accelerator: shortcut('shift N')
)

action(id: 'openAction',
    name: 'Open',
    closure: controller.&fileOpen,
    mnemonic: 'O',
    accelerator: shortcut('O'),
    smallIcon: imageIcon(resource:"icons/folder_page.png", class:this),
    shortDescription: 'Open Groovy Script'
)

action(id: 'saveAction',
    name: 'Save',
    closure: controller.&fileSave,
    mnemonic: 'S',
    accelerator: shortcut('S'),
    smallIcon: imageIcon(resource:"icons/disk.png", class:this),
    shortDescription: 'Save Groovy Script',
    enabled: false // controller will enable as needed
)

action(id: 'saveAsAction',
    name: 'Save As...',
    closure: controller.&fileSaveAs,
    mnemonic: 'A',
)

action(id: 'printAction',
    name: 'Print...',
    closure: controller.&print,
    mnemonic: 'P',
    accelerator: shortcut('P')
)

action(id: 'exitAction',
    name: 'Exit',
    closure: controller.&exit,
    mnemonic: 'X'
// whether or not application exit should have an
// accellerator is debatable in usability circles
// at the very least a confirm dialog should dhow up
//accelerator: shortcut('Q')
)

action(id: 'undoAction',
    name: 'Undo',
    closure: controller.&undo,
    mnemonic: 'U',
    accelerator: shortcut('Z'),
    smallIcon: imageIcon(resource:"icons/arrow_undo.png", class:this),
    shortDescription: 'Undo'
)

action(id: 'redoAction',
    name: 'Redo',
    closure: controller.&redo,
    mnemonic: 'R',
    accelerator: shortcut('shift Z'), // is control-shift-Z or control-Y more common?
    smallIcon: imageIcon(resource:"icons/arrow_redo.png", class:this),
    shortDescription: 'Redo'
)

action(id: 'findAction',
    name: 'Find...',
    closure: controller.&find,
    mnemonic: 'F',
    accelerator: shortcut('F'),
    smallIcon: imageIcon(resource:"icons/find.png", class:this),
    shortDescription: 'Find'
)

action(id: 'findNextAction',
    name: 'Find Next',
    closure: controller.&findNext,
    mnemonic: 'N',
    accelerator: KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0)
)

action(id: 'findPreviousAction',
    name: 'Find Previous',
    closure: controller.&findPrevious,
    mnemonic: 'V',
    accelerator: KeyStroke.getKeyStroke(KeyEvent.VK_F3, InputEvent.SHIFT_DOWN_MASK)
)

action(id: 'replaceAction',
    name: 'Replace...',
    closure: controller.&replace,
    mnemonic: 'E',
    accelerator: shortcut('H'),
    smallIcon: imageIcon(resource:"icons/text_replace.png", class:this),
    shortDescription: 'Replace'
)

action(id: 'cutAction',
    name: 'Cut',
    closure: controller.&cut,
    mnemonic: 'T',
    accelerator: shortcut('X'),
    smallIcon: imageIcon(resource:"icons/cut.png", class:this),
    shortDescription: 'Cut'
)

action(id: 'copyAction',
    name: 'Copy',
    closure: controller.&copy,
    mnemonic: 'C',
    accelerator: shortcut('C'),
    smallIcon: imageIcon(resource:"icons/page_copy.png", class:this),
    shortDescription: 'Copy'
)

action(id: 'pasteAction',
    name: 'Paste',
    closure: controller.&paste,
    mnemonic: 'P',
    accelerator: shortcut('V'),
    smallIcon: imageIcon(resource:"icons/page_paste.png", class:this),
    shortDescription: 'Paste'
)

action(id: 'selectAllAction',
    name: 'Select All',
    closure: controller.&selectAll,
    mnemonic: 'A',
    accelerator: shortcut('A')
)

action(id: 'historyPrevAction',
    name: 'Previous',
    closure: controller.&historyPrev,
    mnemonic: 'P',
    accelerator: shortcut(KeyEvent.VK_COMMA),
    smallIcon: imageIcon(resource:"icons/book_previous.png", class:this),
    shortDescription: 'Previous Groovy Script',
    enabled: false // controller will enable as needed
)

action(id: 'historyNextAction',
    name: 'Next',
    closure: controller.&historyNext,
    mnemonic: 'N',
    accelerator: shortcut(KeyEvent.VK_PERIOD),
    smallIcon: imageIcon(resource:"icons/book_next.png", class:this),
    shortDescription: 'Next Groovy Script',
    enabled: false // controller will enable as needed
)

action(id: 'clearOutputAction',
    name: 'Clear Output',
    closure: controller.&clearOutput,
    mnemonic: 'O',
    accelerator: shortcut('W')
)

action(id: 'runAction',
    name: 'Run',
    closure: controller.&runScript,
    mnemonic: 'R',
    keyStroke: shortcut('ENTER'),
    accelerator: shortcut('R'),
    smallIcon: imageIcon(resource:"icons/script_go.png", class:this),
    shortDescription: 'Execute Groovy Script'
)

action(id: 'runSelectionAction',
    name: 'Run Selection',
    closure: controller.&runSelectedScript,
    mnemonic: 'E',
    keyStroke: shortcut('shift ENTER'),
    accelerator: shortcut('shift R')
)

action(id: 'inspectLastAction',
    name: 'Inspect Last',
    closure: controller.&inspectLast,
    mnemonic: 'I',
    accelerator: shortcut('I')
)

action(id: 'inspectVariablesAction',
    name: 'Inspect Variables',
    closure: controller.&inspectVariables,
    mnemonic: 'V',
    accelerator: shortcut('J')
)

action(id: 'captureStdOutAction',
    name: 'Capture Standard Output',
    closure: controller.&captureStdOut,
    mnemonic: 'C'
)

action(id: 'fullStackTracesAction',
    name: 'Show Full Stack Traces',
    closure: controller.&fullStackTraces,
    mnemonic: 'F'
)

action(id:'showToolbarAction',
    name: 'Show Toolbar',
    closure: controller.&showToolbar,
    mnemonic: 'T'
)

action(id: 'largerFontAction',
    name: 'Larger Font',
    closure: controller.&largerFont,
    mnemonic: 'L',
    accelerator: shortcut('shift L')
)

action(id: 'smallerFontAction',
    name: 'Smaller Font',
    closure: controller.&smallerFont,
    mnemonic: 'S',
    accelerator: shortcut('shift S')
)

action(id: 'aboutAction',
    name: 'About',
    closure: controller.&showAbout,
    mnemonic: 'A'
)

action(id: 'interruptAction',
    name: 'Interrupt',
    closure: controller.&confirmRunInterrupt
)
