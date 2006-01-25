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
package org.codehaus.groovy.grails.validation;

import org.springframework.validation.Errors;
/**
 * Interface that defines a validatable constraint 
 * 
 * @author Graeme Rocher
 * @since 10-Nov-2005
 */
public interface Constraint {
	/**
	 * Returns whether the constraint supports being applied against the specified type;
	 * 
	 * @param type The type to support
	 * @return True if the constraint can be applied against the specified type
	 */
	boolean supports(Class type);
	/**
	 * Validate this constraint against a property value
	 * 
	 * @param propertyValue The property value to validate
	 * @param errors The errors instance to record errors against
	 */
	void validate(Object propertyValue, Errors errors);
	
	/**
	 * The parameter which the constraint is validated against
	 * 
	 * @param parameter
	 */
	void setParameter(Object parameter);
	
	/**
	 * The class the constraint applies to
	 * 
	 * @param owningClass
	 */
	void setOwningClass(Class owningClass);
	
	/**
	 * The name of the property the constraint applies to
	 * 
	 * @param propertyName
	 */
	void setPropertyName(String propertyName);

    /**
     *
     * @return The name of the constraint
     */
    String getName();

    /**
     *
     * @return The property name of the constraint
     */
    String getPropertyName();

    
}
