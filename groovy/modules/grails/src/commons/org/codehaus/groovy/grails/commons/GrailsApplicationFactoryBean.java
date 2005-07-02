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

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;

/**
 * <p>Factory bean that creates a Grails application object based on Groovy files.
 * 
 * @author Steven Devijver
 * @since Jul 2, 2005
 */
public class GrailsApplicationFactoryBean implements FactoryBean, InitializingBean {
	
	private Resource[] groovyFiles = null;
	private GrailsApplication grailsApplication = null;
	
	public GrailsApplicationFactoryBean() {
		super();		
	}

	public void setGroovyFiles(Resource[] groovyFiles) {
		this.groovyFiles = groovyFiles;
	}
	
	public void afterPropertiesSet() throws Exception {
		if (this.groovyFiles == null || groovyFiles.length == 0) {
			throw new IllegalStateException("Groovy files are not defined!");
		}
		this.grailsApplication = new DefaultGrailsApplication(this.groovyFiles);
	}
	
	public Object getObject() throws Exception {
		return this.grailsApplication;
	}

	public Class getObjectType() {
		return GrailsApplication.class;
	}

	public boolean isSingleton() {
		return true;
	}

}
