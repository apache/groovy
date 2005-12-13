package org.codehaus.groovy.grails.scaffolding;

import java.util.Map;

import org.springframework.validation.Errors;

public class DefaultScaffoldCallback implements ScaffoldCallback {

	private boolean invoked;
	private Errors errors;
	private Map model;
	/**
	 * @return Returns the errors.
	 */
	public Errors getErrors() {
		return errors;
	}
	/**
	 * @param errors The errors to set.
	 */
	public void setErrors(Errors errors) {
		this.errors = errors;
	}
	/**
	 * @return Returns the invoked.
	 */
	public boolean isInvoked() {
		return invoked;
	}
	/**
	 * @param invoked The invoked to set.
	 */
	public void setInvoked(boolean invoked) {
		this.invoked = invoked;
	}
	/**
	 * @return Returns the model.
	 */
	public Map getModel() {
		return model;
	}
	/**
	 * @param model The model to set.
	 */
	public void setModel(Map model) {
		this.model = model;
	}
	
	

}
