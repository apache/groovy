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
package org.codehaus.groovy.grails.validation.metaclass;

import groovy.lang.Closure;


import org.codehaus.groovy.grails.commons.metaclass.AbstractDynamicProperty;
import org.codehaus.groovy.grails.validation.ConstrainedPropertyBuilder;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
/**
 * This is a dynamic property that instead of returning the closure sets a new proxy meta class for the scope 
 * of the call and invokes the closure itself which builds up a list of ConstrainedProperty instances
 * 
 * @author Graeme Rocher
 * @since 07-Nov-2005
 */
public class ConstraintsDynamicProperty extends AbstractDynamicProperty {
	public static final String PROPERTY_NAME = "constraints";
	
	public ConstraintsDynamicProperty() {
		super(PROPERTY_NAME);
	}

	public Object get(Object object) {
		BeanWrapper bean = new BeanWrapperImpl(object);
		Closure c = (Closure)bean.getPropertyValue(PROPERTY_NAME);
		ConstrainedPropertyBuilder delegate = new ConstrainedPropertyBuilder(object);
		c.setDelegate(delegate);
		c.call();
		return delegate.getConstrainedProperties();
	}

	public void set(Object object, Object newValue) {
		throw new UnsupportedOperationException("Cannot set read-only property ["+PROPERTY_NAME+"]");
	}

}
