package groovy.swt.guibuilder

class Screen1 extends Script {
        
    run() {     
		guiBuilder.composite {
			fillLayout()
			
			group( text:"This is Screen1.groovy" ) {
				gridLayout()
				button( text:"the hardest", background:[0, 255, 255] )
				button( text:"button", background:[0, 255, 255] )
				button( text:"to button", background:[0, 255, 255] )
			}
		}
	}
	
}
