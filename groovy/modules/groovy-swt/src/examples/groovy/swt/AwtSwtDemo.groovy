package groovy.swt.examples

import groovy.swt.SwtBuilder
import groovy.swing.SwingBuilder


class AwtSwtDemo {
    property swt
        
    void run() {
        swt = new SwtBuilder()
   		swing = new SwingBuilder()        
   		
        shell = swt.shell ( text:'The AwtSwt Demo' ) {
         	fillLayout()
         	
         	swing.current = awtFrame()
			swing.tree()
        }
        
		shell.open()
	
		while(! shell.isDisposed()) { 
			if (! shell.display.readAndDispatch()) {
				shell.display.sleep();
			}
		}
			
		shell.display.dispose()	
	}
}
