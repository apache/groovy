package groovy.ui.view

menuBar {
    menu(text: 'File', mnemonic: 'F') {
        menuItem(newFileAction)
        menuItem(newWindowAction)
        menuItem(openAction)
        separator()
        menuItem(saveAction)
        menuItem(saveAsAction)
        separator()
        menuItem(printAction)
        separator()
        menuItem(exitAction)
    }

    menu(text: 'Edit', mnemonic: 'E') {
        menuItem(undoAction)
        menuItem(redoAction)
        separator()
        menuItem(cutAction)
        menuItem(copyAction)
        menuItem(pasteAction)
        separator()
        menuItem(findAction)
        menuItem(findNextAction)
        menuItem(findPreviousAction)
        menuItem(replaceAction)
        separator()
        menuItem(selectAllAction)
    }

    menu(text: 'View', mnemonic: 'V') {
        menuItem(clearOutputAction)
        separator()
        menuItem(largerFontAction)
        menuItem(smallerFontAction)
        separator()
        checkBoxMenuItem(captureStdOutAction, selected: controller.captureStdOut)
        checkBoxMenuItem(fullStackTracesAction, selected: controller.fullStackTraces)
        checkBoxMenuItem(showScriptInOutputAction, selected: controller.showScriptInOutput)
        checkBoxMenuItem(visualizeScriptResultsAction, selected: controller.visualizeScriptResults)
        checkBoxMenuItem(showToolbarAction, selected: controller.showToolbar)
    }

    menu(text: 'History', mnemonic: 'I') {
        menuItem(historyPrevAction)
        menuItem(historyNextAction)
    }

    menu(text: 'Script', mnemonic: 'S') {
        menuItem(runAction)
        menuItem(runSelectionAction)
        separator()
        menuItem(addClasspathJar)
        menuItem(addClasspathDir)
        menuItem(clearClassloader)
        separator()
        menuItem(inspectLastAction)
        menuItem(inspectVariablesAction)
    }

    menu(text: 'Help', mnemonic: 'H') {
        menuItem(aboutAction)
    }
}
