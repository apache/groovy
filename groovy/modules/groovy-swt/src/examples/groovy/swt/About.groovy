package com.dacelo.guidedsales.ui

class About extends Script {
        
    run( ) {       
		subapp = guiBuilder.shell( parent ) {
		
			gridLayout()
			
			group( text:"Groovy Swt", background:[255, 255, 255] ) {
				gridLayout()
				
				label( text:"groove fun !" ,background:[255, 255, 255] )
				label( text:"Email: ckl@dacelo.nl", background:[255, 255, 255] )
			}
		}

		subapp.pack()
		subapp.open()
	}
	
}
