package groovy.swt.guibuilder

class Screen2 extends Script {
        
    def run() {
		guiBuilder.composite {
			fillLayout( type:"vertical")
			group( text:"This is Screen2.groovy", background:[255, 255, 255] ) {
				fillLayout( type:"vertical")
		 		label( text:"A big red label", background:[204, 0, 0] ) 
				label( text:"I can barelly read this", foreground:[0,200,0] )  
				label( text:"It sure looks like the dutch flag", foreground:[0,0,150], background:[0, 0, 153] )
			}
		}
    }
	
}
