class @controller.name@Controller {
	
	@Property String defaultAction = "list"
	
	@Property Closure list = { request,response ->
		return null;
	}
}

