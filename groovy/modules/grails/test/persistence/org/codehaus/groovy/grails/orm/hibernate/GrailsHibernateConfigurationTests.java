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


import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.codehaus.groovy.grails.commons.DefaultGrailsApplication;
import org.codehaus.groovy.grails.commons.GrailsApplication;
import org.codehaus.groovy.grails.commons.GrailsDomainClass;
import org.codehaus.groovy.grails.orm.hibernate.cfg.DefaultGrailsDomainConfiguration;
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
public class GrailsHibernateConfigurationTests extends AbstractDependencyInjectionSpringContextTests  {
	GroovyClassLoader cl = new GroovyClassLoader();
	
	
	protected GrailsApplication grailsApplication = null;
	protected SessionFactory sessionFactory = null;
	

	protected String[] getConfigLocations() {
		return new String[] { "org/codehaus/groovy/grails/orm/hibernate/grails-hibernate-configuration-tests.xml" };
	}
	
	
	protected void onSetUp() throws Exception {
		Thread.currentThread().setContextClassLoader(cl);
		
		cl.loadClass( "org.codehaus.groovy.grails.domain.RelationshipsTest" );
		cl.loadClass( "org.codehaus.groovy.grails.domain.Test1" );
		cl.loadClass( "org.codehaus.groovy.grails.domain.Test2" );
		
		Class[] loadedClasses = cl.getLoadedClasses();
		grailsApplication = new DefaultGrailsApplication(loadedClasses,cl);
		

		DefaultGrailsDomainConfiguration config = new DefaultGrailsDomainConfiguration();
		config.setGrailsApplication(this.grailsApplication);
		Properties props = new Properties();
		props.put("hibernate.connection.username","sa");
		props.put("hibernate.connection.password","");
		props.put("hibernate.connection.url","jdbc:hsqldb:mem:grailsDB");
		props.put("hibernate.connection.driver_class","org.hsqldb.jdbcDriver");
		props.put("hibernate.dialect","org.hibernate.dialect.HSQLDialect");
		props.put("hibernate.hbm2ddl.auto","create-drop");
		config.setProperties(props);
		//originalClassLoader = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(this.cl);		
		this.sessionFactory = config.buildSessionFactory();
		
		
	}	


	public void testGrailsDomain() throws Exception {
		GrailsDomainClass[] domainClasses = grailsApplication.getGrailsDomainClasses();
		assertEquals(6,domainClasses.length);
	}
	
	public void testHibernateSave() throws Exception {		
		assertNotNull(this.sessionFactory);
		
		HibernateTemplate template = new HibernateTemplate(this.sessionFactory);
		GroovyObject obj = grailsApplication.getGrailsDomainClass("org.codehaus.groovy.grails.domain.Test1").newInstance();
		assertNotNull(obj);
		
		obj.setProperty("firstName", "Joe");
		obj.setProperty("id",new Long(1));
		//obj.setProperty("lastName", "Bloggs");
		template.save( obj );
	}
	
	public void testHibernateLoad() throws Exception {
		
		
		HibernateTemplate template = new HibernateTemplate(this.sessionFactory);
		
		GroovyObject obj = grailsApplication.getGrailsDomainClass("org.codehaus.groovy.grails.domain.Test1").newInstance();
		assertNotNull(obj);
		
		obj.setProperty("firstName", "Joe");
		obj.setProperty("id",new Long(1));
		//obj.setProperty("lastName", "Bloggs");
		template.save( obj );
		
		obj = (GroovyObject)template.execute(new HibernateCallback() {

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
	
	
	public void testHibernateOneToOne() {

		HibernateTemplate template = new HibernateTemplate(this.sessionFactory);
		GroovyObject parent = grailsApplication.getGrailsDomainClass( "org.codehaus.groovy.grails.domain.RelationshipsTest" ).newInstance();
		GroovyObject child = grailsApplication.getGrailsDomainClass( "org.codehaus.groovy.grails.domain.OneToOneTest" ).newInstance();
		
		assertNotNull(child);
		
		parent.setProperty("one", child);
		
		template.save(parent);		
		// TODO Test loading the relationship back from hibernate
	}
	
	public void testHibernateOneToMany() {
		GroovyObject one2many = grailsApplication.getGrailsDomainClass( "org.codehaus.groovy.grails.domain.RelationshipsTest" ).newInstance();

		
		GroovyObject many2one = grailsApplication.getGrailsDomainClass( "org.codehaus.groovy.grails.domain.OneToManyTest2" ).newInstance();
		
		HibernateTemplate template = new HibernateTemplate(this.sessionFactory);
		
		
		assertNotNull(many2one);
									
		// create one-to-many relationship
		Set set = new HashSet();		
		one2many.setProperty("ones", set);		
		((Set)one2many.getProperty("ones")).add( many2one );
		
		// persist
		template.save(one2many);
		
		// TODO Test loading the relationship back from hibernate
		
	}
	
	public void testHibernateManyToOne() {
		GrailsDomainClass one2ManyDomain = grailsApplication.getGrailsDomainClass( "org.codehaus.groovy.grails.domain.RelationshipsTest" );
		GroovyObject one2many = one2ManyDomain.newInstance();

		
		GroovyObject many2one = grailsApplication.getGrailsDomainClass( "org.codehaus.groovy.grails.domain.OneToManyTest2" ).newInstance();
		
		HibernateTemplate template = new HibernateTemplate(this.sessionFactory);
		
		assertNotNull(many2one);
		Set set = new HashSet();
		one2many.setProperty("ones", set);
		template.save(one2many);
									
		// create many-to-one relationship
				
		many2one.setProperty("other", one2many);
		
		template.save(many2one);
		
		// now get it back and check it works as expected
		one2many = (GroovyObject) template.get(one2ManyDomain.getClazz(),new Long(1));
		assertNotNull(one2many);
		
		set = (Set)one2many.getProperty("ones");
		assertNotNull(set);
		assertEquals(1, set.size());
	}

}
