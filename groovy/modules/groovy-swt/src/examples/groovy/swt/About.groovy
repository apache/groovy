package com.dacelo.guidedsales.ui

class About extends Script {
        
    run( ) {     
    	// println "--> parent: " + parent  
    	// println "--> guiBuilder: " + guiBuilder  
    	    	
		subapp = guiBuilder.shell( parent ) {
		
			gridLayout()
			
			group( text:"Groovy SWT", background:[255, 255, 255] ) {
				gridLayout()
				
				label( text:"groove fun !" ,background:[255, 255, 255] )
				label( text:"Email: ckl@dacelo.nl", background:[255, 255, 255] )
			}
		}

		subapp.pack()
		subapp.open()
	}
	
}
