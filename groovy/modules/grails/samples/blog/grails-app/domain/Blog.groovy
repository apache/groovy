class Blog { 
	@Property Long id; 
	@Property Long version;
	@Property relationships = [ "entries" : Entry.class ]
	
	@Property User owner
	@Property String name
	@Property Set entries = new HashSet()
	
	@Property constraints = {
		name(blank:false)
	}
}	
