package groovy.swt.examples

import groovy.swt.guibuilder.ApplicationGuiBuilder

class ApplicationGuiDemo {
    property comp1
	property builder1
	property mainapp1
    
    run() {
    	
        builder1 = new ApplicationGuiBuilder("src/examples/groovy/swt/")

		mainapp1 = builder1.applicationWindow( title:"The ApplicationGuiDemo", size:[700,400] ) { 
			gridLayout( numColumns:2 ) 
			
			toolBar( style:"horizontal" ){
				toolItem( text:"Blue" ) {
					onEvent( type:"Selection", closure:{
						builder1.rebuild( parent:comp1, closure:{ 
							builder1.composite( it ) {
								fillLayout()
								label( text:"This is fresh new blue label ...", background:[0, 0, 255] )
							}
						})
					})
				}				
				
				toolItem( text:"Red" ) {
					onEvent( type:"Selection", closure:{
						builder1.rebuild( parent:comp1, closure:{ 
							builder1.composite( it ) {
								fillLayout()
								label( text:"This is fresh new red label ...", background:[255, 0, 0] )
							}
						})
					})
				}				
				
				toolItem( text:"Run script About.groovy" ){
					onEvent( type:"Selection", closure:{
						builder1.runScript( src:"About.groovy", parent:mainapp1 )
					})
				}				
			}			
		
			composite() {
				gridData( horizontalAlignment:"fill", horizontalSpan:2, grabExcessHorizontalSpace:true )
				gridLayout() 
				
				comp1 = composite() {
					fillLayout()
					label( text:"This is green label", background:[0, 255, 0]  )
				}
			}
			
		}
		
		mainapp1.getShell().pack()
		mainapp1.open()
	}
}
