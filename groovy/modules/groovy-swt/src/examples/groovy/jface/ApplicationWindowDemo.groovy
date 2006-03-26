package groovy.jface.examples

import groovy.jface.JFaceBuilder

class ApplicationWindowDemo {
    @Property mainapp
        
    void run() {
        def swt = new JFaceBuilder()
        
	    mainapp = swt.applicationWindow() { 	
	         	
	         	menuManager( text:"File" ) {
	         		action ( text:"Very Nice", closure:{ println "Very Nice !!!" } )
	         		separator()
	         		action ( text:"Check me", checked:true, closure:{ println "I've been checked" } )
	         	}
	
	         	menuManager( text:"Edit" ) {
	         		action ( text:"Say Hi Statusbar", closure:{ mainapp.setStatus('Hello ...') } )
	         	}
	       
				fillLayout ( type:"vertical" )
	
				label( text:"A big red label", background:[204, 0, 0] ) 
				label( text:"I can barelly read this", foreground:[0,200,0] )  
				label( text:"It sure looks like the dutch flag", foreground:[0,0,150], background:[0, 0, 153] )
	 
		}
  
		mainapp.MenuBarManager.updateAll( true )
		mainapp.getShell().layout()
		mainapp.open()

	}
}
