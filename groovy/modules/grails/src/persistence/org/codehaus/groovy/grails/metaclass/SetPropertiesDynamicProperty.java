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
import ognl.NoSuchPropertyException;
import ognl.Ognl;
import ognl.OgnlException;
import org.apache.commons.collections.BeanMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.groovy.grails.commons.metaclass.AbstractDynamicProperty;
import org.codehaus.groovy.grails.web.binding.GrailsDataBinder;
import org.codehaus.groovy.grails.web.metaclass.GrailsParameterMap;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.web.bind.ServletRequestDataBinder;

import javax.servlet.http.HttpServletRequest;
import java.text.DateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

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

	/**
	 * @return A org.apache.commons.collections.BeanMap instance
	 */
	public Object get(Object object) {				
		return new BeanMap(object);
	}

	/**
	 * Sets the property on the specified object with the specified value. The
	 * value is expected to be a Map containing OGNL expressions for the keys
	 * and objects for the values.
	 * 
	 * @param object The target object
	 * @param newValue The value to set
	 */
	public void set(Object object, Object newValue) {
		if(newValue == null)
			return;
		
		if(newValue instanceof GrailsParameterMap) {
			GrailsParameterMap parameterMap = (GrailsParameterMap)newValue;
			HttpServletRequest request = parameterMap.getRequest();
			ServletRequestDataBinder dataBinder = new GrailsDataBinder(object, object.getClass().getName());			
			dataBinder.registerCustomEditor( Date.class, new CustomDateEditor(DateFormat.getDateInstance( DateFormat.SHORT, request.getLocale() ),true) );
			dataBinder.bind(request);			
		}
		else if(newValue instanceof Map) {
			
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
				} catch (NoSuchPropertyException nspe) {
					if(LOG.isDebugEnabled())
						LOG.debug("Unable to set property '"+propertyName+"' to value '"+propertyValue+"' for object '"+object+"' property doesn't exist." + nspe.getMessage());					
				}catch (OgnlException e) {
					if(LOG.isDebugEnabled())
						LOG.debug("OGNL error attempt to set '"+propertyName+"' to value '"+propertyValue+"' for object '"+object+"':" + e.getMessage(),e);
				} 
			}
			
		}
		else {
			throw new MissingPropertyException(PROPERTY_NAME,object.getClass());
		}
	}

}
