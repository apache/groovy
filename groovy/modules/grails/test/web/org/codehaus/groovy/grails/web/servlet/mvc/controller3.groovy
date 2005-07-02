import org.springframework.web.servlet.ModelAndView;

class ReturnModelAndViewController {
	@Property Closure withView = {
		return new ModelAndView("someView");
	}
	
	@Property Closure withoutView = {
		return new ModelAndView();
	}
	
	@Property String viewConfiguredView = "someOtherView";
	@Property Closure viewConfigured = {
		return new ModelAndView();
	}
	
	@Property String defaultClosure = "withView";
}