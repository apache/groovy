/*
 * Copyright 2004-2005 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 
package org.codehaus.groovy.grails.web.pageflow;

import groovy.lang.Closure;
import groovy.lang.ParameterArray;

import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

/**
 * <p>Delegate that turns a closure into a Spring validator.
 * 
 * <p>The closure needs to accept two parameters: target and and {@link org.springframework.validation.Errors} instance.
 * 
 * @author Steven Devijver
 * @since Aug 6, 2005
 */
public class ClosureValidator implements Validator {

	private Closure closure = null;
	
	public ClosureValidator(Closure closure) {
		super();
		this.closure = closure;
	}

	public boolean supports(Class arg0) {
		return true;
	}

	public void validate(Object target, Errors errors) {
		this.closure.call(new ParameterArray(new Object[] { target, errors }));
	}

}
