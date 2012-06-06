/*
 * Copyright 2003-2012 the original author or authors.
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

import groovy.ui.view.Defaults
import groovy.ui.view.GTKDefaults
import groovy.ui.view.MacOSXDefaults
import groovy.ui.view.WindowsDefaults
import java.awt.datatransfer.DataFlavor
import java.awt.dnd.*
import javax.swing.UIManager
import javax.swing.event.DocumentListener

switch (UIManager.getSystemLookAndFeelClassName()) {
    case 'com.sun.java.swing.plaf.windows.WindowsLookAndFeel':
    case 'com.sun.java.swing.plaf.windows.WindowsClassicLookAndFeel':
        build(WindowsDefaults)
        break

    case 'apple.laf.AquaLookAndFeel':
    case 'com.apple.laf.AquaLookAndFeel':
        build(MacOSXDefaults)
        break

    case 'com.sun.java.swing.plaf.gtk.GTKLookAndFeel':
        build(GTKDefaults)
        break

    default:
        build(Defaults)
        break
}

binding.rootContainerDelegate.delegate = this

consoleFrame = binding['rootContainerDelegate']()
container(consoleFrame) {

    binding.menuBarDelegate.delegate = delegate
    binding['menuBarDelegate'](menuBarClass)

    build(contentPaneClass)

    build(toolBarClass)

    build(statusBarClass)
}


controller.promptStyle = promptStyle
controller.commandStyle = commandStyle
controller.outputStyle = outputStyle
controller.stacktraceStyle = stacktraceStyle
controller.hyperlinkStyle = hyperlinkStyle
controller.resultStyle = resultStyle

// add the window close handler
if (consoleFrame instanceof java.awt.Window) {
    consoleFrame.windowClosing = controller.&exit
}

// link in references to the controller
controller.inputEditor = inputEditor
controller.inputArea = inputEditor.textEditor
controller.outputArea = outputArea
controller.outputWindow = outputWindow
controller.statusLabel = status
controller.frame = consoleFrame
controller.rowNumAndColNum = rowNumAndColNum
controller.toolbar = toolbar

// link actions
controller.saveAction = saveAction
controller.prevHistoryAction = historyPrevAction
controller.nextHistoryAction = historyNextAction
controller.fullStackTracesAction = fullStackTracesAction
controller.showToolbarAction = showToolbarAction
controller.detachedOutputAction = detachedOutputAction
controller.autoClearOutputAction = autoClearOutputAction
controller.threadInterruptAction = threadInterruptAction
controller.showOutputWindowAction = showOutputWindowAction
controller.hideOutputWindowAction1 = hideOutputWindowAction1
controller.hideOutputWindowAction2 = hideOutputWindowAction2
controller.hideOutputWindowAction3 = hideOutputWindowAction3
controller.hideOutputWindowAction4 = hideOutputWindowAction4
controller.interruptAction = interruptAction
controller.origDividerSize = origDividerSize
controller.splitPane = splitPane
controller.blank = blank
controller.scrollArea = scrollArea

// some more UI linkage
controller.outputArea.addComponentListener(controller)
controller.inputArea.addComponentListener(controller)
controller.outputArea.addHyperlinkListener(controller)
controller.outputArea.addHyperlinkListener(controller)
controller.outputArea.addFocusListener(controller)
controller.inputArea.addCaretListener(controller)
controller.inputArea.document.addDocumentListener({ controller.setDirty(true) } as DocumentListener)
controller.rootElement = inputArea.document.defaultRootElement


def dtListener =  [
    dragEnter:{DropTargetDragEvent evt ->
        if (evt.dropTargetContext.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
            evt.acceptDrag(DnDConstants.ACTION_COPY)
        } else {
            evt.rejectDrag()
        }
    },
    dragOver:{DropTargetDragEvent evt ->
        //dragEnter(evt)
    },
    dropActionChanged:{DropTargetDragEvent evt ->
        //dragEnter(evt)
    },
    dragExit:{DropTargetEvent evt  ->
    },
    drop:{DropTargetDropEvent evt  ->
        evt.acceptDrop DnDConstants.ACTION_COPY
        //println "Dropping! ${evt.transferable.getTransferData(DataFlavor.javaFileListFlavor)}"
        if (controller.askToSaveFile()) {
            controller.loadScriptFile(evt.transferable.getTransferData(DataFlavor.javaFileListFlavor)[0])
        }
    },
] as DropTargetListener

[consoleFrame, inputArea, outputArea].each {
    new DropTarget(it, DnDConstants.ACTION_COPY, dtListener)
}

// don't send any return value from the view, all items should be referenced via the bindings
return null
