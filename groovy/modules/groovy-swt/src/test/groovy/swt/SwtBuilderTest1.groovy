package groovy.swt

import groovy.swt.SwtBuilder

class SwtBuilderTest1 {
    property swt    
    void run() {
        swt = new SwtBuilder()
        swt.shell(){        	
       		button()
        	canvas()
        	caret()
	        combo()
        	composite()
        	scrolledComposite()
        	coolBar() {
	        	coolItem()
	        }
        	decorations()
        	group()
        	label()
        	list()
        	menu() {
				menuSeparator()
				menuItem()
			}
			messageBox()
			progressBar()
			sash()
			scale()
			slider()
			tabFolder() {
				tabItem()
			}
			table() {
				tableColumn()
				tableItem()
			}
			text()
			toolBar(){
				toolItem()
			}
			tracker()
			tray() {
				trayItem()
			}
			tree(){
				treeItem()
			}
			cTabFolder() {
				cTabItem()
			}
			tableTree() {
				tableTreeItem()
			}
			fillLayout()
			gridLayout()
			rowLayout()
			gridData()
			rowData()
			colorDialog()
			directoryDialog()
			fileDialog()
			fontDialog()
			onEvent( type:"Selection" )
			image( src:"src/test/groovy/swt/groovy-logo.png" )
			browser() {
				locationListener()
				progressListener()
				statusTextListener()
			}	
			form(){
				formButton()
				formComposite()
				formCompositeSeparator()
				formExpandableComposite()
				formText()
				formHyperlink(){
					hyperlinkListener()
				}
				formImageHyperlink()
				formLabel()
				formPageBook()
				formSection(){
					expansionListener()
				}
				formSeparator()
				formTable()
				formFormattedText()
				formTree()
				tableWrapLayout()
				tableWrapData()
			}
			scrolledForm()
        }
	}
}
