package groovy.swt.guibuilder

import groovy.swt.guibuilder.ApplicationGuiBuilder

class ApplicationGuiDemo {
    @Property comp
    @Property builder
    @Property mainapp
    @Property guiBuilder
    @Property comp1
    
    def main( args ) {

        mainapp = guiBuilder.applicationWindow( title:"The ApplicationGuiDemo", size:[700,400] ) { 
            fillLayout( ) 
            
            menuManager( text:"Screens" ) {
                action( text:"Screen1", closure:{ 
                    guiBuilder.runScript( src:"Screen1.groovy", parent:mainapp, rebuild:true )
                })
                action( text:"Screen2", closure:{ 
                    guiBuilder.runScript( src:"Screen2.groovy", parent:mainapp, rebuild:true )
                })
            }
                
            toolBar( style:"horizontal" ){
                toolItem( text:"Blue" ) {
                    onEvent( type:"Selection", closure:{
                        guiBuilder.rebuild( parent:comp1, closure:{ 
                            guiBuilder.composite( it ) {
                                fillLayout()
                                label( text:"This is fresh new blue label ...", background:[0, 0, 255] )
                            }
                        })
                    })
                }               
                
                toolItem( text:"Red" ) {
                    onEvent( type:"Selection", closure:{
                        guiBuilder.rebuild( parent:comp1, closure:{ 
                            guiBuilder.composite( it ) {
                                fillLayout()
                                label( text:"This is fresh new red label ...", background:[255, 0, 0] )
                            }
                        })
                    })
                }               
                        
            }           
        
            composite() {
                
                fillLayout() 
                
                comp1 = composite() {
                    fillLayout()
                    label( text:"This is green label", background:[0, 255, 0]  )
                }
            }
            
        }
        
        mainapp.MenuBarManager.updateAll(true)
        mainapp.open()
    }
}
