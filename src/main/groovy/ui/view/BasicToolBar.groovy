package groovy.ui.view

import javax.swing.SwingConstants
import java.awt.BorderLayout

toolbar = toolBar(rollover:true, visible:controller.showToolbar, constraints:BorderLayout.NORTH) {
    button(newFileAction, text:null)
    button(openAction, text:null)
    button(saveAction, text:null)
    separator(orientation:SwingConstants.VERTICAL)
    button(undoAction, text:null)
    button(redoAction, text:null)
    separator(orientation:SwingConstants.VERTICAL)
    button(cutAction, text:null)
    button(copyAction, text:null)
    button(pasteAction, text:null)
    separator(orientation:SwingConstants.VERTICAL)
    button(findAction, text:null)
    button(replaceAction, text:null)
    separator(orientation:SwingConstants.VERTICAL)
    button(historyPrevAction, text:null)
    button(historyNextAction, text:null)
    separator(orientation:SwingConstants.VERTICAL)
    button(runAction, text:null)
}
