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


import groovy.lang.GroovyObject;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.codehaus.groovy.grails.domain.GrailsDomain;
import org.codehaus.groovy.grails.domain.GrailsDomainClass;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

/**
 * @author Graeme Rocher
 * @since 08-Jul-2005
 */
public class GrailsHibernateConfigurationTests extends
		AbstractDependencyInjectionSpringContextTests {

	protected DataSource dataSource = null;
	protected GrailsDomain grailsDomain = null;
	protected SessionFactory sessionFactory = null;
	

	/* (non-Javadoc)
	 * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#getConfigLocations()
	 */
	protected String[] getConfigLocations() {
		return new String[] { "org/codehaus/groovy/grails/orm/hibernate/grails-hibernate-configuration-tests.xml" };
	}
	
	/**
	 * @param dataSource The dataSource to set.
	 */
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}
	
	/**
	 * @param grailsDomain The grailsDomain to set.
	 */
	public void setGrailsDomain(GrailsDomain grailsDomain) {
		this.grailsDomain = grailsDomain;
	}
	
	/**
	 * @param sessionFactory The sessionFactory to set.
	 */
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public void testDataSource() throws Exception {
		Connection connection = dataSource.getConnection();			
		connection.close();
	}

	public void testGrailsDomain() throws Exception {
		GrailsDomainClass[] domainClasses = grailsDomain.getGrailsDomainClasses();
		assertEquals(2,domainClasses.length);
	}
	
	public void testHibernateSave() throws Exception {		
		assertNotNull(this.sessionFactory);
		
		HibernateTemplate template = new HibernateTemplate(this.sessionFactory);
		GroovyObject obj = grailsDomain.getGrailsDomainClass("test1").newInstance();
		assertNotNull(obj);
		
		obj.setProperty("firstName", "Joe");
		obj.setProperty("id",new Long(1));
		//obj.setProperty("lastName", "Bloggs");
		template.save( obj );
	}
	
	public void testHibernateLoad() throws Exception {
		
		HibernateTemplate template = new HibernateTemplate(this.sessionFactory);
		
		GroovyObject obj = (GroovyObject)template.execute(new HibernateCallback() {

			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				// create query based on cid and list results
				List results = session.createQuery("from org.codehaus.groovy.grails.domain.Test1 where id = '1'").list();
				// if there are results return otherwise return null
				if(results.size() > 0) {
					return results.get(0);
				}
				return null;
			}
			
		});

		
		assertNotNull(obj);
		assertEquals("Joe",obj.getProperty("firstName"));
	}
}
