
builder.composite( parent ) {

	gridLayout( numColumns:2 )
	
	gridData( style:"fill_both" )
	label( style:"none", text:obj.description ) {
		gridData( style:"fill_both" )
	}
        	
	text( style:"Border", text:obj.value ) 

}

