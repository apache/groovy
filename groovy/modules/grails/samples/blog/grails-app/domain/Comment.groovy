class Comment { 
	@Property Long id; 
	@Property Long version; 
	
	@Property Entry entry
	
	@Property String authorName
	@Property String authorEmail
	@Property String authorBlogURL
	@Property String body
	
	@Property constraints = {
		authorEmail(email:true)
		authorBlogURL(url:true)
		body(blank:false)
	}
}	
