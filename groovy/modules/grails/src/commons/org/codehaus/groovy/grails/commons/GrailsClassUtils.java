/* Copyright 2004-2005 the original author or authors.
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

import groovy.lang.Closure;

/**
 * @author Graeme Rocher
 * @since 08-Jul-2005
 */
public class GrailsClassUtils {

	/**
	 * Returns true of the specified Groovy class is a controller
	 * @param clazz
	 * @return
	 */
	public static boolean isControllerClass( Class clazz ) {
		return clazz.getName().endsWith(DefaultGrailsControllerClass.CONTROLLER)  && !Closure.class.isAssignableFrom(clazz);
	}
	
	/**
	 * <p>Returns true if the specified class is a page flow class type</p>
	 * 
	 * @param clazz
	 * @return
	 */
	public static boolean isPageFlowClass( Class clazz ) {
		return clazz.getName().endsWith(DefaultGrailsPageFlowClass.PAGE_FLOW)  && !Closure.class.isAssignableFrom(clazz);
	}	
	
	/**
	 * <p>Returns true if the specified class is a domain class. In Grails a domain class
	 * is any class that has "id" and "version" properties</p>
	 * 
	 * @param clazz The class to check
	 * @return A boolean value
	 */
	public static boolean isDomainClass( Class clazz ) {
		try {
			// make sure the identify and version field exist
			clazz.getDeclaredField( GrailsDomainClassProperty.IDENTITY );
			clazz.getDeclaredField( GrailsDomainClassProperty.VERSION );
			// and its not a closure
			if(Closure.class.isAssignableFrom(clazz)) {
				return false;
			}
			// passes all conditions return true
			return true;
		} catch (SecurityException e) {
			return false;
		} catch (NoSuchFieldException e) {
			return false;
		}
	}

}
