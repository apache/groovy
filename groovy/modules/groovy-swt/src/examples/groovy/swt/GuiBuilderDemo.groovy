package groovy.swt.examples

import groovy.swt.guibuilder.ApplicationGuiBuilder

class ApplicationGuiDemo extends Script {
    //property builder1
	// property mainapp
	//property comp2

    
    run() {
    	println "THIS DEMO IS UNDER CONSTRUCTION, EXPECT THE UNEXPECTED"
    	
        builder1 = new ApplicationGuiBuilder("src/test/groovy/swt/examples/")

		mainapp1 = builder1.applicationWindow( title:"The ApplicationGuiDemo", size:[700,400] ) { 
			gridLayout(numColumns:2) 
			
			toolBar( style:"horizontal" ){
				toolItem( text:"Blue" ) {
					onEvent( type:"Selection", closure:{
						builder1.rebuild( parent:comp1, closure:{ 
							builder1.composite( it ) {
								fillLayout()
								label( text:"12121212" )
							}
							comp1.pack()
						})
					})
				}				
				
				toolItem( text:"Red" ) {
					onEvent( type:"Selection", closure:{
						builder1.rebuild( parent:comp1, closure:{ 
							builder1.composite( it ) {
								fillLayout()
								label( text:"34343434" )
							}
							comp1.pack()
						})
					})
				}				
				
				toolItem( text:"Show About" ){
					onEvent( type:"Selection", closure:{
						// builder1.run( script:"About.groovy", parent:mainapp1 )
					})
				}				
			}			
		
			parent = composite() {
				gridData( horizontalAlignment:"fill", horizontalSpan:2, grabExcessHorizontalSpace:true )
				gridLayout(numColumns:2) 
				
				comp1 = composite( background:[100, 100, 100], foreground:[100, 255, 100] ) {
					fillLayout()
					button( text:"test" )
				}
				comp2 = composite( background:[0, 255, 0], foreground:[100, 255, 100] ) {
					fillLayout()				
					button( text:"test" )
				}
				comp3 = composite( background:[255, 50, 200] )
				comp4 = composite( background:[200, 70, 99] )
			}
		}
		
		mainapp1.getShell().pack()
		mainapp1.open()
	}
}
