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
package org.codehaus.groovy.grails.web.pageflow;

import groovy.lang.Closure;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.springframework.util.Assert;
import org.springframework.web.flow.RequestContext;
import org.springframework.web.flow.ViewDescriptor;
import org.springframework.web.flow.ViewDescriptorCreator;

/**
 * <p>All closures in the model associated with a view
 * state will be executed everytime a (@link org.springframework.web.flow.ViewDescriptor}
 * is created.
 * 
 * @author Steven Devijver
 * @since Jul 10, 2005
 */
public class ModelViewDescriptorCreator implements ViewDescriptorCreator {

	private Map model = null;
	private String viewName = null;
	
	public ModelViewDescriptorCreator(Map model) {
		super();
		this.model = model;
	}

	public ModelViewDescriptorCreator(String viewName, Map model) {
		this(model);
		Assert.notNull(viewName);
		this.viewName = viewName;
	}
	
	public ViewDescriptor createViewDescriptor(RequestContext requestContext) {
		Map tmpModel = new HashMap(requestContext.getModel());
		if (this.model != null) {
			for (Iterator iter = this.model.entrySet().iterator(); iter.hasNext();) {
				Map.Entry entry = (Map.Entry)iter.next();
				if (entry.getValue() != null && entry.getValue() instanceof Closure) {
					tmpModel.put(entry.getKey(), ((Closure)entry.getValue()).call(new Object[] { requestContext }));
				} else {
					tmpModel.put(entry.getKey(), entry.getValue());
				}
			}
		}
		return new ViewDescriptor(viewName, tmpModel);
	}

}
