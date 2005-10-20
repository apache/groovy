class BlogEntry { 
	@Property Long id; 
	@Property Long version; 
	@Property relationships = [ "comments" : Comment.class ]
	
	@Property String title
	@Property Date date
	@Property String body
	@Property Set comments = new HashSet()
}	
