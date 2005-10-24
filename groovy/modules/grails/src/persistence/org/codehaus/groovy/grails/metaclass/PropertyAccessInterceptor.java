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
package org.codehaus.groovy.grails.metaclass;

import groovy.lang.Interceptor;
/**
 * <p>An interface that extends the Groovy interceptor and adds the ability to intercept
 * property getters/setters
 * 
 * @author Graeme Rocher
 * @since Oct 24, 2005
 */
public interface PropertyAccessInterceptor extends Interceptor {
	
	/**
	 * Intercepts a getXXX call and returns a result. The result is replaced by the 
	 * real value if doGet() return false
	 * 
	 * @param object The target object
	 * @param property The property to get
	 * @return
	 */
	public Object beforeGet(Object object, String property);
	
	public void beforeSet(Object object, String property, Object newValue);
	
	public boolean doSet();
	
	/**
	 * @return True if the real property getter should be called
	 */
	public boolean doGet();

}
