class EntryController {
	@Property boolean scaffold = true
	
	@Property createComment = {
		def e = Entry.get( this.params["entryId"] )
		
		def c = new Comment()
		c.properties = this.params		
		e.comments.add(c)		
		e.save()
		
		redirect(action:this.show,params:[ "id": this.params["entryId"] ] )
	}
}