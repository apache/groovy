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
package org.codehaus.groovy.grails.orm.hibernate.cfg;

import java.beans.IntrospectionException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.groovy.grails.commons.GrailsApplication;
import org.codehaus.groovy.grails.commons.GrailsDomainClass;
import org.codehaus.groovy.grails.metaclass.DomainClassMethods;
import org.hibernate.EntityMode;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.metadata.ClassMetadata;

/**
 * Creates runtime configuration mappings for the Grails domain classes
 * based on the work done in the Hibernate Annotations project
 * 
 * @author Graeme Rocher
 * @since 06-Jul-2005
 */
public class DefaultGrailsDomainConfiguration extends Configuration implements GrailsDomainConfiguration {

	private static final Log LOG  = LogFactory.getLog(DefaultGrailsDomainConfiguration.class);
	/**
	 * 
	 */
	private static final long serialVersionUID = -7115087342689305517L;
	private GrailsApplication grailsApplication;
	private Set domainClasses;
	private boolean configLocked;

	/**
	 * 
	 */
	public DefaultGrailsDomainConfiguration() {
		super();		
		this.domainClasses = new HashSet();
	}

	/* (non-Javadoc)
	 * @see org.codehaus.groovy.grails.orm.hibernate.cfg.GrailsDomainConfiguration#addDomainClass(org.codehaus.groovy.grails.commons.GrailsDomainClass)
	 */
	public GrailsDomainConfiguration addDomainClass( GrailsDomainClass domainClass ) {
		if(domainClass.getMappedBy().equalsIgnoreCase( GrailsDomainClass.GORM )) {
			this.domainClasses.add(domainClass);
		}
				
		return this;		
	}
	/* (non-Javadoc)
	 * @see org.codehaus.groovy.grails.orm.hibernate.cfg.GrailsDomainConfiguration#setGrailsApplication(org.codehaus.groovy.grails.commons.GrailsApplication)
	 */
	public void setGrailsApplication(GrailsApplication application) {
		this.grailsApplication = application;
		if(this.grailsApplication != null) {
			GrailsDomainClass[] existingDomainClasses = this.grailsApplication.getGrailsDomainClasses();
			for(int i = 0; i < existingDomainClasses.length;i++) {
				addDomainClass(existingDomainClasses[i]);
			}		
		}
	}
	
	
	/* (non-Javadoc)
	 * @see org.hibernate.cfg.Configuration#buildSessionFactory()
	 */
	public SessionFactory buildSessionFactory() throws HibernateException {

		SessionFactory sessionFactory =  super.buildSessionFactory();
		Collection classMetaData = sessionFactory.getAllClassMetadata().values();
		for (Iterator i = classMetaData.iterator(); i.hasNext();) {
			ClassMetadata cmd = (ClassMetadata) i.next();
			Class persistentClass = cmd.getMappedClass(EntityMode.POJO);
			LOG.info("[GrailsDomainConfiguration] Registering dynamic methods on externally configured hibernate persistent class ["+persistentClass+"]");
			try {
				new DomainClassMethods(this.grailsApplication,persistentClass,sessionFactory,this.grailsApplication.getClassLoader());
			} catch (IntrospectionException e) {
				LOG.warn("[GrailsDomainConfiguration] Introspection exception registering dynamic methods for ["+persistentClass+"]:" + e.getMessage(), e);
			}
		}	
		return sessionFactory;
	}

	/**
	 *  Overrides the default behaviour to including binding of Grails
	 *  domain classes
	 */
	protected void secondPassCompile() throws MappingException {
		if (configLocked) {
			return;
		}		
		// set the class loader to load Groovy classes
		if(this.grailsApplication != null)
			Thread.currentThread().setContextClassLoader( this.grailsApplication.getClassLoader() );
		// do Grails class configuration
		for(Iterator i = this.domainClasses.iterator();i.hasNext();) {
			GrailsDomainClass domainClass = (GrailsDomainClass)i.next();
			
			GrailsDomainBinder.bindClass(domainClass, super.createMappings());
		}
		
		// call super
		super.secondPassCompile();
		this.configLocked = true;
	}
	
}
