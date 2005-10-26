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
package grails.util;

import org.codehaus.groovy.runtime.InvokerHelper;

import groovy.lang.MissingPropertyException;
import groovy.util.Proxy;
/** 
 * Extends the Groovy Proxy implementation and adds proxying of property getters/setters
 * 
* @author Graeme Rocher
* @since Oct 20, 2005
*/
public class ExtendProxy extends Proxy {

	public Object getProperty(String property) {
		try {			
			return super.getProperty(property);
		}
		catch(MissingPropertyException mpe) {
			return InvokerHelper.getMetaClass(getAdaptee()).getProperty(getAdaptee(),property);
		}
	}

	public void setProperty(String property, Object newValue) {
		try {			
			super.setProperty(property,newValue);
		}
		catch(MissingPropertyException mpe) {
			InvokerHelper.getMetaClass(getAdaptee()).setProperty(getAdaptee(),property,newValue);
		}
	}
	
	

}
