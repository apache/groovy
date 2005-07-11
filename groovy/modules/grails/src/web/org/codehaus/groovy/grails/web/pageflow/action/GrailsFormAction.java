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
package org.codehaus.groovy.grails.web.pageflow.action;

import org.springframework.webflow.action.FormAction;

/**
 * <p>Adds some convenience property setters to {@link org.springframework.webflow.action.FormAction}.
 * 
 * @author Steven Devijver
 * @since Jul 11, 2005
 * @see org.springframework.webflow.action.FormAction
 */
public class GrailsFormAction extends FormAction {

	public GrailsFormAction() {
		super();
	}

	/**
	 * <p>Convencience property for {@link FormAction#setFormObjectClass(java.lang.Class)}.
	 * 
	 * @param clazz the form object class
	 */
	public void setClass(Class clazz) {
		setFormObjectClass(clazz);
	}
	
	/**
	 * <p>Convenience property for {@link FormAction#setFormObjectName(java.lang.String)}.
	 * 
	 * @param name the form object name in the scope
	 */
	public void setName(String name) {
		setFormObjectName(name);
	}
	
	/**
	 * <p>Convenience property for {@link FormAction#setFormObjectScopeAsString(java.lang.String)}.
	 * 
	 * @param scope the form object scope (<code>request</code> or <code>scope</code>)
	 */
	public void setScope(String scope) {
		setFormObjectScopeAsString(scope);
	}
}
