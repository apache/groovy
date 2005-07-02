class ParameterController {
	@Property String twoParametersView = "someView"
	@Property Closure twoParameters = {
		request, response -> return [ "request" : request, "response" : response ]
	}
	@Property String defaultClosure = "twoParameters"
	
	@Property String oneParameterView = "someOtherView"
	@Property Closure oneParameter = {
		request -> return [ "request" : request ]
	}
}