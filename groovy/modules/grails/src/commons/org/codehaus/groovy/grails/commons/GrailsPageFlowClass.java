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

import java.lang.String;

/**
 * <p>Interface holding a page flow configuration.
 * 
 * @author Steven Devijver
 * @since Jul 10, 2005
 */
public interface GrailsPageFlowClass extends InjectableGrailsClass {

	/**
	 * <p>Returns the flow id.
	 * 
	 * @return the flow id
	 */
	public String getFlowId();
	
	/**
	 * <p>Returns the URI of this page flow.
	 * 
	 * @return the URI of this page flow.
	 */
	public String getUri();
	
	/**
	 * <p>Page flow is accessible through URI. If set to false the page flow
	 * is accessible as a sub flow or through the _flowId parameter.
	 * 
	 * @return the page flow is accessible through an URI.
	 */
	public boolean getAccessible();
}
