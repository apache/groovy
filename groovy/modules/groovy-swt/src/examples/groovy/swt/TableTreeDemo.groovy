package groovy.swt.examples

import groovy.swt.SwtBuilder

class TableTreeDemo {
    @Property swt
        
    void run() {
        swt = new SwtBuilder()
        
        def shell = swt.shell ( text:'The TableTree Test1', location:[100,100], size:[700,600] ) {
         	gridLayout(numColumns:3) 
         	
         	tableTree( toolTipText:"This is a table tree!", style:"multi, full_selection" ) {  
			
				gridData( style:"fill_both" ) 
				
				tableTreeItem ( text:"root1" )  {
						tableTreeItem ( text:"child 1-1" )  
						tableTreeItem ( text:"child 1-2" )  								
						tableTreeItem ( text:"child 1-3" )  								
				}

				tableTreeItem ( text:"root2" )  {
						tableTreeItem ( text:"child 2-1" )  
						tableTreeItem ( text:"child 2-2" )  								
						tableTreeItem ( text:"child 2-3" )  								
				}
				
			}
		}
        
		shell.open()
	
		while(! shell.isDisposed()) { 
			if (! shell.display.readAndDispatch()) {
				shell.display.sleep();
			}
		}
			
	}
}
