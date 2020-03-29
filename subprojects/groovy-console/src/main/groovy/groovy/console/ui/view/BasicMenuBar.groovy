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
	separator()
	menuItem(commentAction)
        menuItem(selectBlockAction)
        separator()
        menuItem(preferencesAction)
    }

    menu(text: 'View', mnemonic: 'V') {
        menuItem(clearOutputAction)
        separator()
        menuItem(largerFontAction)
        menuItem(smallerFontAction)
        checkBoxMenuItem(smartHighlighterAction, selected: controller.smartHighlighter)
        separator()
        checkBoxMenuItem(captureStdOutAction, selected: controller.captureStdOut)
        checkBoxMenuItem(captureStdErrAction, selected: controller.captureStdErr)
        checkBoxMenuItem(fullStackTracesAction, selected: controller.fullStackTraces)
        checkBoxMenuItem(showScriptInOutputAction, selected: controller.showScriptInOutput)
        checkBoxMenuItem(visualizeScriptResultsAction, selected: controller.visualizeScriptResults)
        checkBoxMenuItem(showToolbarAction, selected: controller.showToolbar)
        checkBoxMenuItem(detachedOutputAction, selected: controller.detachedOutput)
        checkBoxMenuItem(autoClearOutputAction, selected: controller.autoClearOutput)
        checkBoxMenuItem(orientationVerticalAction, selected: controller.orientationVertical)
    }

    menu(text: 'History', mnemonic: 'I') {
        menuItem(historyPrevAction)
        menuItem(historyNextAction)
    }

    menu(text: 'Script', mnemonic: 'S') {
        menuItem(runAction)
        menuItem(runJavaAction)
        checkBoxMenuItem(loopModeAction, selected: controller.loopMode)
        checkBoxMenuItem(saveOnRunAction, selected: controller.saveOnRun)
        menuItem(runSelectionAction)
        menuItem(runJavaSelectionAction)
        checkBoxMenuItem(threadInterruptAction, selected: controller.threadInterrupt)
        menuItem(interruptAction)
        menuItem(compileAction)
        menuItem(compileJavaAction)
        checkBoxMenuItem(indyAction, selected: controller.indy)
        separator()
        menuItem(addClasspathJar)
        menuItem(addClasspathDir)
        menuItem(listClasspath)
        menuItem(clearClassloader)
        separator()
        menuItem(inspectLastAction)
        menuItem(inspectVariablesAction)
        menuItem(inspectAstAction)
        menuItem(inspectCstAction)
        menuItem(inspectTokensAction)
    }

    menu(text: 'Help', mnemonic: 'H') {
        menuItem(aboutAction)
    }
}
