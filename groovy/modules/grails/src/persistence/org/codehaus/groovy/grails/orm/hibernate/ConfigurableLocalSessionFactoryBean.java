
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

import org.codehaus.groovy.grails.commons.GrailsApplication;
import org.codehaus.groovy.grails.orm.hibernate.cfg.DefaultGrailsDomainConfiguration;
import org.codehaus.groovy.grails.orm.hibernate.cfg.GrailsDomainConfiguration;
import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.springframework.orm.hibernate3.LocalSessionFactoryBean;

import java.io.IOException;

/**
 * A SessionFactory bean that allows the configuration class to be changed and customise for usage within Grails
 *
 * @author Graeme Rocher
 * @since 07-Jul-2005
 */
public class ConfigurableLocalSessionFactoryBean extends
		LocalSessionFactoryBean {

	
	private ClassLoader classLoader = null;
	private GrailsApplication grailsApplication;	

	/**
	 * 
	 */
	public ConfigurableLocalSessionFactoryBean() {
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
		DefaultGrailsDomainConfiguration config = new DefaultGrailsDomainConfiguration();
		config.setGrailsApplication(grailsApplication);
        // we set this to false as Spring might wrap the session factory in a transactional proxy
        // if configured as such
        config.setConfigureDynamicMethods(false);
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

        SessionFactory sf = (SessionFactory)getObject();
        if(sf != null) {
            Configuration c = getConfiguration();
            if(c instanceof GrailsDomainConfiguration) {
                GrailsDomainConfiguration gc = (GrailsDomainConfiguration)c;
                gc.configureDynamicMethods(sf);
            }
        }
        if (originalClassLoader != null) {
			Thread.currentThread().setContextClassLoader(originalClassLoader);
		}
	}
}

