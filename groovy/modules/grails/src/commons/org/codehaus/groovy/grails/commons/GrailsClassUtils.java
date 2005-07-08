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
	public static boolean isController( GrailsApplication application, Class clazz ) {
		GrailsControllerClass controllers[] =  application.getControllers();
		
		for (int i = 0; i < controllers.length; i++) {
			if(controllers[i].getClazz().equals( clazz )) {
				return true;
			}
		}
		return false;
	}
	
	

}
