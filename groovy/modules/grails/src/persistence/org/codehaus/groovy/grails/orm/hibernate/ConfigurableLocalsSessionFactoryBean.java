
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
package org.codehaus.groovy.grails.orm.hibernate;

import java.io.IOException;

import org.codehaus.groovy.grails.commons.GrailsApplication;
import org.codehaus.groovy.grails.orm.hibernate.cfg.GrailsDomainConfiguration;
import org.hibernate.HibernateException;
import org.hibernate.cfg.Configuration;
import org.springframework.orm.hibernate3.LocalSessionFactoryBean;

/**
 * @author Graeme Rocher
 * @since 07-Jul-2005
 */
public class ConfigurableLocalsSessionFactoryBean extends
		LocalSessionFactoryBean {

	private ClassLoader classLoader = null;
	private GrailsApplication grailsApplication;	

	/**
	 * 
	 */
	public ConfigurableLocalsSessionFactoryBean() {
		super();		
	}
	
	/**
	 * @return Returns the grailsApplication.
	 */
	public GrailsApplication getGrailsApplication() {
		return grailsApplication;
	}

	/**
	 * @param grailsApplication The grailsApplication to set.
	 */
	public void setGrailsApplication(GrailsApplication grailsApplication) {
		this.grailsApplication = grailsApplication;
	}
	
	/**
	 * Overrides default behaviour to allow for a configurable configuration class 
	 */
	protected Configuration newConfiguration() {
		Configuration config = super.newConfiguration();
		
		if(config instanceof GrailsDomainConfiguration) {
			((GrailsDomainConfiguration)config).setGrailsApplication(this.grailsApplication);
		}
		
		return config;
	}


	public void setClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}
	
	public void afterPropertiesSet() throws IllegalArgumentException,
			HibernateException, IOException {
		ClassLoader originalClassLoader = null;
		if (this.classLoader != null) {
			originalClassLoader = Thread.currentThread().getContextClassLoader();
			Thread.currentThread().setContextClassLoader(this.classLoader);
		}
		super.afterPropertiesSet();
		if (originalClassLoader != null) {
			Thread.currentThread().setContextClassLoader(originalClassLoader);
		}
	}
}

