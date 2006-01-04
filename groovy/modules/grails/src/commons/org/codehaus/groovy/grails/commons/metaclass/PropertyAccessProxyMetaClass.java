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
package org.codehaus.groovy.grails.commons.metaclass;

import java.beans.IntrospectionException;

import org.codehaus.groovy.runtime.InvokerHelper;

import groovy.lang.MetaClass;
import groovy.lang.MetaClassRegistry;
import groovy.lang.ProxyMetaClass;
/**
 * <p>Extends ProxyMetaClass and adds the ability to intercept calls to property getters/setters
 * 
 * @author Graeme Rocher
 * @since Oct 24, 2005
 */
public class PropertyAccessProxyMetaClass extends ProxyMetaClass {

    /**
     * convenience factory method for the most usual case.
     */
    public static ProxyMetaClass getInstance(Class theClass) throws IntrospectionException {
        MetaClassRegistry metaRegistry = InvokerHelper.getInstance().getMetaRegistry();
        MetaClass meta = metaRegistry.getMetaClass(theClass);
        return new PropertyAccessProxyMetaClass(metaRegistry, theClass, meta);
    }
    
	public PropertyAccessProxyMetaClass(MetaClassRegistry registry, Class theClass, MetaClass adaptee) throws IntrospectionException {
		super(registry, theClass, adaptee);
	}

	public Object getProperty(Object object, String property) {
        if (null == interceptor) {
            return super.getProperty(object, property);
        }
        if(interceptor instanceof PropertyAccessInterceptor) {
        	PropertyAccessInterceptor pae = (PropertyAccessInterceptor)interceptor;
	        Object result = pae.beforeGet(object,property);
	        if (pae.doGet()) {
	            result = super.getProperty(object, property);
	        }
	        return result;
        }
        return super.getProperty(object, property); 
	}

	public void setProperty(Object object, String property, Object newValue) {
        if (null == interceptor) {
            super.setProperty(object, property,newValue);
        }
        if(interceptor instanceof PropertyAccessInterceptor) {
        	PropertyAccessInterceptor pae = (PropertyAccessInterceptor)interceptor;
	        pae.beforeSet(object,property,newValue);
	        if (pae.doSet()) {
	        	super.setProperty(object, property,newValue);
	        }
        }
        else {
            super.setProperty(object, property, newValue);
        }
    }

	
}
