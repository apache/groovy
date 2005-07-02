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
package org.codehaus.groovy.grails.commons;



/**
 * 
 * 
 * @author Steven Devijver
 * @since Jul 2, 2005
 */
public abstract class AbstractInjectableGrailsClass extends AbstractGrailsClass
		implements InjectableGrailsClass {

	private static final String BY_NAME_PROPERTY = "byName";
	private static final String AVAILABLE_PROPERTY = "available";
	
	private boolean byName = false;
	private boolean byType = true;
	private boolean available = true;
	
	public AbstractInjectableGrailsClass(Class clazz, String trailingName) {
		super(clazz, trailingName);

		Object byNameValue = getPropertyValue(BY_NAME_PROPERTY, Boolean.class);
		this.byName = byNameValue != null && byNameValue.equals(Boolean.TRUE);
		this.byType = !byName;
		
		Object availableValue = getPropertyValue(AVAILABLE_PROPERTY, Boolean.class);
		this.available = availableValue == null || availableValue.equals(Boolean.TRUE);
	}

	public boolean byName() {
		return this.byName;
	}

	public boolean byType() {
		return this.byType;
	}
	
	public boolean getAvailable() {
		return this.available;
	}
}
