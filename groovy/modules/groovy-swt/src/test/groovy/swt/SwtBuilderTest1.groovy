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
			onEvent( type:"Paint" )
			onEvent( type:"DefaultSelection" )
			onEvent( type:"Dispose" )
			onEvent( type:"FocusIn" )
			onEvent( type:"FocusOut" )
			onEvent( type:"Hide" )
			onEvent( type:"Show" )
			onEvent( type:"KeyDown" )
			onEvent( type:"KeyUp" )
			onEvent( type:"MouseDown" )
			onEvent( type:"MouseUp" )
			onEvent( type:"MouseDoubleClick" )
			onEvent( type:"MouseMove" )
			onEvent( type:"Resize" )
			onEvent( type:"Move" )
			onEvent( type:"Close" )
			onEvent( type:"Activate" )
			onEvent( type:"Iconify" )
			onEvent( type:"Deiconify" )
			onEvent( type:"Deactivate" )
			onEvent( type:"Expand" )
			onEvent( type:"Collapse" )
			onEvent( type:"Modify" )
			onEvent( type:"Verify" )
			onEvent( type:"Help" )
			onEvent( type:"Arm" )
			onEvent( type:"MouseExit" )
			onEvent( type:"MouseEnter" )
			onEvent( type:"MouseHover" )
			onEvent( type:"Traverse" )
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
