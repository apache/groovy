package groovy.swt.examples

import groovy.jface.JFaceBuilder

class ApplicationDemo {
    @Property  mainapp
    
    void run() {
        def jface = new JFaceBuilder()

        mainapp = jface.applicationWindow( title:"The forms demo", size:[700,800], location:[0,0] ) { 	
         	gridLayout ( numColumns:2 )
			form( text:"Hello, Eclipse Forms" ) {
				gridData( style:"fill_both" )
				tableWrapLayout()

				formSection( text:"section1", description:"description of section1", style:"description, twistie" ) {
					tableWrapData( style:"fill" )
					
					expansionListener( type:"expansionStateChanging", closure: { println "expansionStateChanging ... " + it } )
					expansionListener( type:"expansionStateChanged", closure: { println "expansionStateChanged ... " + it } )
					
					def htmlText = "<form>"
					htmlText += "<li>list item</li>"
					htmlText += "<p>this html code with an url: http://groovy.codehaus.org</p>"
					htmlText += "<li style=\"text\" value=\"1.\">list item 2</li>"
			        htmlText += "<li style=\"text\" value=\"2.\">list item 3</li>"
			        htmlText += "</form>"
					
					formFormattedText( text:htmlText, parseTags:true, expandURLs:true )					
					
					formButton ( text:"This is radiobutton1", style:"radio" ) 
					formButton ( text:"This is radiobutton2", style:"radio" ) 
					
					formButton ( text:"This is a ARROW button", style:"arrow" ) {
						onEvent(type:"Selection", closure:{ println "stop selecting me !!!" }) 
					}	
					formButton ( text:"This is a PUSH button", style:"push" ) {
	        			onEvent(type:"Selection", closure:{ println "stop pushing me !!!" }) 
	        		}
					formButton ( text:"This is a TOGGLE button", style:"TOGGLE" ) {
						onEvent(type:"Selection", closure:{ println it.event }) 
					}
					
				}
				
				formSection( text:"section2", description:"description of section2", style:"description, twistie" ) {
					tableWrapData( style:"fill" )
					formLabel( text:"This is a label in section 2" )
					formExpandableComposite( text:"formExpandableComposite" )
				}

				formSection( text:"section3", description:"description of section3", style:"description, twistie" ) {
					tableWrapData( style:"fill" )
					formLabel( text:"Below me is a tree" )
	       			formTree()
				}
				
				formSeparator( style:"separator, horizontal" ) {
					tableWrapData( style:"fill" )
				}
				
				formButton( text:"This is a formButton" )
				
       			formCompositeSeparator()
								
				formHyperlink( text:"this is a hyperlink" ) {
					hyperlinkListener( type:"hyperlinkUpdate", closure: { println "hyperlinkUpdate ... " + it } )
					hyperlinkListener( type:"linkEntered", closure: { println "linkEntered ... " + it } )
					hyperlinkListener( type:"linkExited", closure: { println "linkExited ... " + it } )
					hyperlinkListener( type:"linkActivated", closure: { println "linkActivated ... " + it } )
				}
       			
       			formLabel( text:"This is a formLabel, folowed by a formTable" )
       			formTable() {
	       			tableWrapData( style:"fill" )
       			}
     
     			
       			
       			// NOT FULLY IMPLEMENTED YET:
       			// formImageHyperlink( text:"formImageHyperlink" )
       			// formPageBook( text:"formPageBook" )
       			
			}
			
			form( text:"hello formScrolledForm" ) {
				gridData( style:"fill_both" )
				formLabel( text:"my parent is a scrolledForm" )
				formButton( text:"formButton" )
			}
		}
		
		mainapp.getShell().pack()
		mainapp.open()
	}
}
