class User { 
	@Property Long id; 
	@Property Long version; 
	
	@Property String firstName
	@Property String lastName
	@Property String email
	@Property String login
	@Property String password
	@Property Blog blog	
	
	@Property constraints = {
		firstName(blank:false)
		lastName(blank:false)
		email(email:true, blank:false)
		login(length:5..15, blank:false)
		password(length:5..15, blank:false)
	}
}	
