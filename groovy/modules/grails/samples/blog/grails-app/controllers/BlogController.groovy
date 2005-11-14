class BlogController {
	
	@Property String defaultAction = "list"
	
	@Property Closure list = {}
	
	@Property Closure show = {		
		println "Executing show"
		if(this.params.containsKey("name")) {
			println "Querying for name: ${this.params['name']}"
			
			def results = User.findByLogin( this.params["name"] )
				
			println "Found results: ${results}"
			
			println "User: " + User.findAll()
			
			if(results.size() > 0) {
				def firstOwner = results[0]
				println "Checking errors"
				if(!firstOwner.validate()) {
					println "Errors"
					println firstOwner.errors
				}
								
				Blog blog = firstOwner.blog	
					
				return [ "blog": blog ];
			}
			else {
				return [:]
			}		
		}
	}
	
}

