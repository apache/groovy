/**
 * 
 */
package org.codehaus.groovy.grails.domain;

import org.codehaus.groovy.grails.commons.GrailsApplication;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;

/**
 * @author graemer
 *
 */
public class GrailsDomainFactoryBean implements FactoryBean, InitializingBean {

	private Resource[] groovyFiles;
	private GrailsDomain grailsDomain;
	private GrailsApplication grailsApplication;
	
	public GrailsDomainFactoryBean() {
		super();		
	}
	
	/**
	 * @param groovyFiles The groovyFiles to set.
	 */
	public void setGroovyFiles(Resource[] groovyFiles) {
		this.groovyFiles = groovyFiles;
	}

	/**
	 * @param grailsApplication The grailsApplication to set.
	 */
	public void setGrailsApplication(GrailsApplication grailsApplication) {
		this.grailsApplication = grailsApplication;
	}

	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.FactoryBean#getObject()
	 */
	public Object getObject() throws Exception {
		return this.grailsDomain;
	}

	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.FactoryBean#getObjectType()
	 */
	public Class getObjectType() {
		return GrailsDomain.class;
	}

	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.FactoryBean#isSingleton()
	 */
	public boolean isSingleton() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	public void afterPropertiesSet() throws Exception {
		if (this.groovyFiles == null || groovyFiles.length == 0) {
			throw new IllegalStateException("Groovy files are not defined!");
		}
		if (this.grailsApplication == null) {
			throw new IllegalStateException("A GrailsDomain requires a GrailsApplication instance");
		}		
		this.grailsDomain = new DefaultGrailsDomain(this.grailsApplication,this.groovyFiles);
	}

}
