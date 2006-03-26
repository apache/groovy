package groovy.swt.examples

import groovy.swt.SwtBuilder
import org.eclipse.swt.layout.FormAttachment
import org.eclipse.swt.graphics.Rectangle

/*
* java version: http://dev.eclipse.org/viewcvs/index.cgi/%7Echeckout%7E/platform-swt-home/snippits/snippet109.html
*/

class SwtDemo {
    @Property shell    
    @Property sashForm1    
    
    void run() {
        def builder = new SwtBuilder()
        shell = builder.shell ( text:'The SashForm Demo' ) {
        	fillLayout()
        
       		sashForm1 = sashForm( style:"horizontal" ) {
       			fillLayout()
       			
       			composite( style:"none" ) {
	       			fillLayout()
	       			label( text:"Label in pane 1" )
       			}
    
       			composite( style:"none" ) {
	       			fillLayout()
	       			button( text:"Button in pane2", style:"push" )
       			}
    
       			composite( style:"none" ) {
       				fillLayout()
	       			label( text:"Label in pane3" )
       			}
       		}
       		
       		sashForm1.weights = [30,40,30]

       	}	
	

		shell.open()
	
		while(! shell.isDisposed()) { 
			if (! shell.display.readAndDispatch()) {
				shell.display.sleep();
			}
		}
				
	}
}

