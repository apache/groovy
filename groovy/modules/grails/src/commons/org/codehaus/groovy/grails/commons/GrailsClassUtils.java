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


import java.beans.PropertyDescriptor;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import groovy.lang.Closure;

/**
 * @author Graeme Rocher
 * @since 08-Jul-2005
 * 
 * Class containing utility methods for dealing with Grails class artifacts
 * 
 */
public class GrailsClassUtils {

	private static Map beanWrapperInstances = new HashMap();
	
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

	/**
	 * 
	 * Returns true if the specified property in the specified class is of the specified type
	 * 
	 * @param clazz The class which contains the property
	 * @param propertyName The property name
	 * @param type The type to check
	 * 
	 * @return A boolean value 
	 */
	public static boolean isPropertyOfType( Class clazz, String propertyName, Class type ) {
		try {			
			
			Class propType = getProperyType( clazz, propertyName );
			if(propType != null && propType.equals( type ))
				return true;
			else
				return false;
		}
		catch(Exception e) {
			return false;
		}
	}
	
	
	/**
	 * Returns the value of the specified property and type from an instance of the specified Grails class
	 *  
	 * @param clazz The name of the class which contains the property
	 * @param propertyName The property name
	 * @param propertyType The property type
	 * 
	 * @return The value of the property or null if none exists
	 */
	public static Object getPropertyValue(Class clazz, String propertyName, Class propertyType) {
		// validate
		if(clazz == null || StringUtils.isBlank(propertyName))
			return null;
		
		try {
			BeanWrapper wrapper = (BeanWrapper)beanWrapperInstances.get( clazz.getName() );
			if(wrapper == null) {
				wrapper = new BeanWrapperImpl(clazz.newInstance());
				beanWrapperInstances.put( clazz.getName(), wrapper );
			}
			return  wrapper.getPropertyValue( propertyName );					
			
		} catch (Exception e) {
			// if there are any errors in instantiating just return null
			return null;
		}		
	}
	
	/**
	 * Returns the type of the given property contained within the specified class
	 * 
	 * @param clazz The class which contains the property
	 * @param propertyName The name of the property
	 * 
	 * @return The property type or null if none exists
	 */
	public static Class getProperyType(Class clazz, String propertyName) {
		if(clazz == null || StringUtils.isBlank(propertyName))
			return null;
		
		try {
			BeanWrapper wrapper = (BeanWrapper)beanWrapperInstances.get( clazz.getName() );
			if(wrapper == null) {
				wrapper = new BeanWrapperImpl(clazz.newInstance());
				beanWrapperInstances.put( clazz.getName(), wrapper );
			}
			return wrapper.getPropertyType(propertyName);			
			
		} catch (Exception e) {
			// if there are any errors in instantiating just return null for the moment
			return null;
		}						
	}

	/**
	 * Retrieves all the properties of the given class for the given type
	 * 
	 * @param clazz The class to retrieve the properties from
	 * @param propertyType The type of the properties you wish to retrieve
	 * 
	 * @return An array of PropertyDescriptor instances
	 */
	public static PropertyDescriptor[] getPropertiesOfType(Class clazz, Class propertyType) {
		if(clazz == null || propertyType == null)
			return new PropertyDescriptor[0];
		
		Set properties = new HashSet();
		try {
			BeanWrapper wrapper = (BeanWrapper)beanWrapperInstances.get( clazz.getName() );
			if(wrapper == null) {
				wrapper = new BeanWrapperImpl(clazz.newInstance());
				beanWrapperInstances.put( clazz.getName(), wrapper );
			}
			PropertyDescriptor[] descriptors = wrapper.getPropertyDescriptors();
			
			for (int i = 0; i < descriptors.length; i++) {
				if(descriptors[i].getPropertyType().equals( propertyType )  ) {
					properties.add(descriptors[i]);
				}
			}
			
		} catch (Exception e) {
			// if there are any errors in instantiating just return null for the moment
			return new PropertyDescriptor[0];
		}				
		return (PropertyDescriptor[])properties.toArray( new PropertyDescriptor[ properties.size() ] );
	}
}
