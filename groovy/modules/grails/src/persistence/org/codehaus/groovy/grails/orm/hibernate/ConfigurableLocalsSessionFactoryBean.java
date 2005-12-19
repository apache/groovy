
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

import java.beans.IntrospectionException;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.groovy.grails.commons.GrailsApplication;
import org.codehaus.groovy.grails.metaclass.DomainClassMethods;
import org.codehaus.groovy.grails.orm.hibernate.cfg.DefaultGrailsDomainConfiguration;
import org.hibernate.EntityMode;
import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.metadata.ClassMetadata;
import org.springframework.orm.hibernate3.LocalSessionFactoryBean;

/**
 * @author Graeme Rocher
 * @since 07-Jul-2005
 */
public class ConfigurableLocalsSessionFactoryBean extends
		LocalSessionFactoryBean {

	private static final Log LOG  = LogFactory.getLog(ConfigurableLocalsSessionFactoryBean.class);
	private ClassLoader classLoader = null;
	private GrailsApplication grailsApplication;	


	/* (non-Javadoc)
	 * @see org.springframework.orm.hibernate3.LocalSessionFactoryBean#newSessionFactory(org.hibernate.cfg.Configuration)
	 */
	protected SessionFactory newSessionFactory(Configuration config) throws HibernateException {
		SessionFactory sessionFactory = super.newSessionFactory(config);
		
		Collection classMetaData = sessionFactory.getAllClassMetadata().values();
		for (Iterator i = classMetaData.iterator(); i.hasNext();) {
			ClassMetadata cmd = (ClassMetadata) i.next();
			Class persistentClass = cmd.getMappedClass(EntityMode.POJO);
			if(this.grailsApplication.getGrailsDomainClass(persistentClass.getName()) == null) {
				LOG.info("[LocalsSessionFactoryBean] Registering dynamic methods on externally configured hibernate persistent class ["+persistentClass+"]");
				try {
					new DomainClassMethods(this.grailsApplication,persistentClass,sessionFactory,this.grailsApplication.getClassLoader());
				} catch (IntrospectionException e) {
					LOG.warn("[LocalsSessionFactoryBean] Introspection exception registering dynamic methods for ["+persistentClass+"]:" + e.getMessage(), e);
				}
			}
		}
		return sessionFactory;
	}

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
		DefaultGrailsDomainConfiguration config = new DefaultGrailsDomainConfiguration();
		config.setGrailsApplication(grailsApplication);
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

