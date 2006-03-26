package groovy.jface.examples

import groovy.jface.JFaceBuilder

class PreferencesDemo {
	@Property pd

    void run() {
        def jface = new JFaceBuilder()
		def mainapp = jface.applicationWindow() { 	        
		
			pd = preferenceDialog() {
				
				preferencePage( title:"General settings", filename:"settings.props" ) { 
					booleanFieldEditor (propertyName:"var1", title:"It's boolean" )
					colorFieldEditor( propertyName:"var2", title:"MainColor" )
					directoryFieldEditor(propertyName:"var3", title:"Directory"	)
					fileFieldEditor( propertyName:"var4", title:"File" )
					fontFieldEditor( propertyName:"var5", title:"Font" )
					integerFieldEditor( propertyName:"var6", title:"Integer" )
					stringFieldEditor( propertyName:"var7", title:"String" )
				} 
								
				preferencePage( title:"Personal settings", filename:"settings.props" ) { 
					booleanFieldEditor( propertyName:"var8", title:"It's boolean" )
					colorFieldEditor( propertyName:"var2", title:"MainColor" )
					directoryFieldEditor( propertyName:"var9", title:"Directory" )
					fileFieldEditor( propertyName:"var10", title:"File" )
					fontFieldEditor( propertyName:"var11", title:"Font" )
					integerFieldEditor( propertyName:"var12", title:"Integer" )
					stringFieldEditor( propertyName:"var13", title:"String" )
				} 
			}
		}
		
	  	pd.open()
	}
  
}
