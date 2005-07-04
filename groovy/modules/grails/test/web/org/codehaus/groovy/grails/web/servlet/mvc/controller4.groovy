class ParameterController {
	@Property String twoParametersView = "someView"
	@Property Map twoParametersTypedViews = [ "rss" : "someRssView" ]
	@Property Closure twoParameters = {
		request, response -> return [ "request" : request, "response" : response ]
	}
	@Property String defaultClosure = "twoParameters"
	
	@Property String oneParameterView = "someOtherView"
	@Property Map oneParameterTypedViews = [ "rss" : "someOtherRssView" ]
	@Property Closure oneParameter = {
		request -> return [ "request" : request ]
	}
}
