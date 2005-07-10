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
package org.codehaus.groovy.grails.commons;

import grails.pageflow.Flow;

import java.lang.Class;
import java.lang.String;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.codehaus.groovy.grails.exceptions.RequiredPropertyMissingException;

/**
 *
 * 
 * @author Steven Devijver
 * @since Jul 10, 2005
 */
public class DefaultGrailsPageFlowClass extends AbstractInjectableGrailsClass
		implements GrailsPageFlowClass {

	public static final String PAGE_FLOW = "PageFlow";
	public static final String FLOW = "flow";
	private static final String SLASH = "/";
	
	private String uri = null;
	
	public DefaultGrailsPageFlowClass(Class clazz) {
		super(clazz, PAGE_FLOW);
		
		if (getPropertyValue(FLOW, Flow.class) == null) {
			throw new RequiredPropertyMissingException("On class [" + clazz.getName() + "] required property [" + FLOW + "] of type [" + Flow.class.getName() + "] is not present!");
		}
		
		this.uri = (StringUtils.isNotBlank(getPackageName()) ? SLASH + getPackageName().replace('.', '/') : "") + SLASH + WordUtils.uncapitalize(getName());
	}

	public String getFlowId() {
		return getName() + PAGE_FLOW;
	}

	public String getUri() {
		return this.uri;
	}
}
