package groovy.jface

import groovy.jface.JFaceBuilder

class JFaceBuilderTest1 {
    property jface    
    void run() {
        jface = new JFaceBuilder()
               
        jface.applicationWindow() {
        	// Viewers
        	tableViewer() {
        		doubleClickListener()
       			selectionChangedListener()
        	}
        	
        	tableTreeViewer()
        	treeViewer()
        	checkboxTreeViewer()	

        	// ContributionManager 
        	menuManager( text:"menuManager" )
        	
        	// Action tags
        	action()

        	// ContributionItem 
        	separator()

        	// Wizard 
        	wizardDialog(){
        		wizardPage( title:"title" )
        	}

        	// Preferences
        	preferenceDialog(  ) {
        		preferencePage( filename:"src/test/groovy/jface/test.properties", title:"myprefs" ) {
	        		booleanFieldEditor( propertyName:"prop", title:"none" )
    	    		colorFieldEditor( propertyName:"prop", title:"none" )
   					directoryFieldEditor( propertyName:"prop", title:"none" )
	        		fileFieldEditor( propertyName:"prop", title:"none" )
	        		fontFieldEditor( propertyName:"prop", title:"none" )
    	    		integerFieldEditor( propertyName:"prop", title:"none" )
    	    		stringFieldEditor( propertyName:"prop", title:"none" )
    	    	}
    	    }

        	image( src:"src/test/groovy/swt/groovy-logo.png" )
        }
	}
}


