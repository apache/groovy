package groovy.ui.view

static handler = false
if (!handler) {
    try {
        handler = build("""
package groovy.ui

import com.apple.mrj.*

class ConsoleMacOsSupport implements MRJQuitHandler, MRJAboutHandler {

	def quitHandler
	def aboutHandler

	public void handleAbout() {
		aboutHandler()
	}

	public void handleQuit() {
		quitHandler()
	}

}

def handler = new ConsoleMacOsSupport(quitHandler:controller.&exit, aboutHandler:controller.&showAbout)
MRJApplicationUtils.registerAboutHandler(handler)
MRJApplicationUtils.registerQuitHandler(handler)

return handler
""", new GroovyClassLoader(this.class.classLoader))
    } catch (Exception se) {
        // usually an AccessControlException, sometimes applets and JNLP won't let
        // you access MRJ classes.
        // However, in any exceptional case back out and use the BasicMenuBar
        se.printStackTrace()
        build(BasicMenuBar)
        return
    }
}

menuBar {
    menu(text: 'File', mnemonic: 'F') {
        menuItem(newFileAction, icon:null)
        menuItem(newWindowAction, icon:null)
        menuItem(openAction, icon:null)
        separator()
        menuItem(saveAction, icon:null)
        menuItem(saveAsAction, icon:null)
        separator()
        menuItem(printAction, icon:null)
    }

    menu(text: 'Edit', mnemonic: 'E') {
        menuItem(undoAction, icon:null)
        menuItem(redoAction, icon:null)
        separator()
        menuItem(cutAction, icon:null)
        menuItem(copyAction, icon:null)
        menuItem(pasteAction, icon:null)
        separator()
        menuItem(findAction, icon:null)
        menuItem(findNextAction, icon:null)
        menuItem(findPreviousAction, icon:null)
        menuItem(replaceAction, icon:null)
        separator()
        menuItem(selectAllAction, icon:null)
    }

    menu(text: 'View', mnemonic: 'V') {
        menuItem(clearOutputAction, icon:null)
        separator()
        menuItem(largerFontAction, icon:null)
        menuItem(smallerFontAction, icon:null)
        separator()
        checkBoxMenuItem(captureStdOutAction, selected: controller.captureStdOut)
        checkBoxMenuItem(fullStackTracesAction, selected: controller.fullStackTraces)
        checkBoxMenuItem(showScriptInOutputAction, selected: controller.showScriptInOutput)
        checkBoxMenuItem(visualizeScriptResultsAction, selected: controller.visualizeScriptResults)
        checkBoxMenuItem(showToolbarAction, selected: controller.showToolbar)
    }

    menu(text: 'History', mnemonic: 'I') {
        menuItem(historyPrevAction, icon:null)
        menuItem(historyNextAction, icon:null)
    }

    menu(text: 'Script', mnemonic: 'S') {
        menuItem(runAction, icon:null)
        menuItem(runSelectionAction, icon:null)
        separator()
        menuItem(addClasspathJar)
        menuItem(addClasspathDir)
        menuItem(clearClassloader)
        separator()
        menuItem(inspectLastAction, icon:null)
        menuItem(inspectVariablesAction, icon:null)
    }
}

