package org.codehaus.groovy.grails.scaffolding;

import java.util.Map;

import org.springframework.validation.Errors;

public interface ScaffoldCallback {

	boolean isInvoked();
	
	void setInvoked(boolean invoked);
	
	Errors getErrors();
	
	void setErrors(Errors errors);
	
	Map getModel();
	
	void setModel(Map model);
}
