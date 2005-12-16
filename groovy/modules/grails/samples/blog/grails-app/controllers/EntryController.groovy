class EntryController {
	@Property boolean scaffold = true
	@Property defaultAction = "list"
	
	@Property createComment = {
		def e = Entry.get( this.params["entryId"] )
		
		def c = new Comment(entry:e)
		c.properties = this.params		
		c.save()
		
		redirect(action:this.show,params:[ "id": this.params["entryId"] ] )
	}
}