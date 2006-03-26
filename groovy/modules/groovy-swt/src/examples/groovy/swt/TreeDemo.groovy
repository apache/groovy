package groovy.swt.examples

import groovy.swt.SwtBuilder

class SwtDemo {
    @Property swt
        
    void run() {
        swt = new SwtBuilder()
        
        def shell = swt.shell ( text:'The Swt Demo #1', location:[100,100], size:[700,600] ) {
         	gridLayout(numColumns:3) 
 
			tree( toolTipText:"This is a tree!", style:"multi" ) {
			
				gridData( style:"fill_both" )
			
				treeItem( text:"A" ) {
					treeItem( text:"A/A" )
					treeItem( text:"A/B" )
					treeItem( text:"A/C" )
				}
				
				treeItem( text:"B" ) {
					treeItem( text:"B/A" )
					treeItem( text:"B/B" )
					treeItem( text:"B/C" )
				}
						
				menu( style:"pop_up" ) {
					menuItem( text:"do something!" )
					menuItem( text:"do something else" )
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
