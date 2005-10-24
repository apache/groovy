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

import groovy.lang.MissingPropertyException;

import java.util.Iterator;
import java.util.Map;

import ognl.Ognl;
import ognl.OgnlException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

/**
 * A dynamic property that uses a Map of OGNL expressions to sets properties on the target object
 * 
 * @author Graeme Rocher
 * @since Oct 24, 2005
 */
public class SetPropertiesDynamicProperty extends AbstractDynamicProperty {

	private static final Log LOG = LogFactory.getLog( SetPropertiesDynamicProperty.class );
	
	private static final String PROPERTY_NAME = "properties";
	

	public SetPropertiesDynamicProperty() {
		super(PROPERTY_NAME);
	}

	public Object get(Object object) {
		BeanWrapper bean = new BeanWrapperImpl(object);
		return bean.getPropertyDescriptors();
	}

	public void set(Object object, Object newValue) {
		if(newValue == null)
			return;
		
		if(newValue instanceof Map) {
			
			Map propertyMap = (Map)newValue;
			
			for (Iterator i = propertyMap.keySet().iterator(); i.hasNext();) {
				String propertyName = (String) i.next();
				Object propertyValue = propertyMap.get(propertyName);				
				// if null skip
				if(propertyValue == null)
					continue;
				
				if(LOG.isDebugEnabled())
					LOG.debug("Attempting to set property '"+propertyName+"' to value '"+propertyValue+"' on instance '"+object+"'");
				
				try {
					Ognl.setValue(propertyName,object,propertyValue);
				} catch (OgnlException e) {
					if(LOG.isDebugEnabled())
						LOG.debug("OGNL error attempt to set '"+propertyName+"' to value '"+propertyValue+"' for object '"+object+"'");
				}				
			}
			
		}
		else {
			throw new MissingPropertyException(PROPERTY_NAME,object.getClass());
		}
	}

}
