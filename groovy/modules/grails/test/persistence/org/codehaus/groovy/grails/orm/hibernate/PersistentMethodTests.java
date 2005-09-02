package org.codehaus.groovy.grails.orm.hibernate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;

import org.codehaus.groovy.grails.commons.GrailsApplication;
import org.codehaus.groovy.grails.commons.GrailsDomainClass;
import org.codehaus.groovy.grails.commons.spring.SpringConfig;
import org.codehaus.groovy.grails.orm.hibernate.metaclass.FindByPersistentMethod;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;
import org.springmodules.beans.factory.drivers.xml.XmlApplicationContextDriver;

public class PersistentMethodTests extends AbstractDependencyInjectionSpringContextTests {

	protected GrailsApplication grailsApplication = null;


	protected String[] getConfigLocations() {
		return new String[] { "org/codehaus/groovy/grails/orm/hibernate/grails-persistent-method-tests.xml" };
	}
	
	
	
	public void setGrailsApplication(GrailsApplication grailsApplication) {
		this.grailsApplication = grailsApplication;
	
	}



	public void testMethodSignatures() {
		
		FindByPersistentMethod findBy = new FindByPersistentMethod( null,new GroovyClassLoader());
		assertTrue(findBy.isMethodMatch("findByFirstName"));
		assertTrue(findBy.isMethodMatch("findByFirstNameAndLastName"));
		assertFalse(findBy.isMethodMatch("rubbish"));
	}
	
	
	public void testSavePersistentMethod() {
		// init spring config
		
		
		GrailsDomainClass domainClass = this.grailsApplication.getGrailsDomainClass("org.codehaus.groovy.grails.orm.hibernate.PersistentMethodTestClass");
		
		GroovyObject obj = domainClass.newInstance();
		obj.setProperty( "id", new Long(1) );
		obj.setProperty( "firstName", "fred" );
		obj.setProperty( "lastName", "flintstone" );
		
		obj.invokeMethod("save", null);
		
	}

	public void testFindByPersistentMethods() {
		GrailsDomainClass domainClass = this.grailsApplication.getGrailsDomainClass("org.codehaus.groovy.grails.orm.hibernate.PersistentMethodTestClass");
		
		GroovyObject obj = domainClass.newInstance();
		obj.setProperty( "id", new Long(1) );
		obj.setProperty( "firstName", "fred" );
		obj.setProperty( "lastName", "flintstone" );
		
		obj.invokeMethod("save", null);
		
		GroovyObject obj2 = domainClass.newInstance();
		obj2.setProperty( "id", new Long(2) );
		obj2.setProperty( "firstName", "wilma" );
		obj2.setProperty( "lastName", "flintstone" );
		
		obj2.invokeMethod("save", null);	
		
		Object returnValue = obj.getMetaClass().invokeStaticMethod(obj, "findByFirstName", new Object[] { "fred" });
		assertNotNull(returnValue);
		assertTrue(returnValue instanceof List);
		
		List returnList = (List)returnValue;
		assertEquals(1, returnList.size());
		
		returnValue = obj.getMetaClass().invokeStaticMethod(obj, "findByFirstNameAndLastName", new Object[] { "fred", "flintstone" });
		assertNotNull(returnValue);
		assertTrue(returnValue instanceof List);
		
		returnList = (List)returnValue;
		assertEquals(1, returnList.size());		
		
		Map queryMap = new HashMap();
		queryMap.put("firstName", "wilma");
		queryMap.put("lastName", "flintstone");
		returnValue = obj.getMetaClass().invokeStaticMethod(obj, "findWhere", new Object[] { queryMap });
		assertNotNull(returnValue);
		assertTrue(returnValue instanceof List);
		
		returnList = (List)returnValue;
		assertEquals(1, returnList.size());		
	}
	
	public void testGetPersistentMethod() {
		GrailsDomainClass domainClass = this.grailsApplication.getGrailsDomainClass("org.codehaus.groovy.grails.orm.hibernate.PersistentMethodTestClass");
		
		GroovyObject obj = domainClass.newInstance();
		obj.setProperty( "id", new Long(1) );
		obj.setProperty( "firstName", "fred" );
		obj.setProperty( "lastName", "flintstone" );
		
		obj.invokeMethod("save", null);
		
		GroovyObject obj2 = domainClass.newInstance();
		obj2.setProperty( "id", new Long(2) );
		obj2.setProperty( "firstName", "wilma" );
		obj2.setProperty( "lastName", "flintstone" );
		
		obj2.invokeMethod("save", null);	
		
		// get wilma
		Object returnValue = obj.getMetaClass().invokeStaticMethod(obj, "get", new Object[] { new Long(2) });
		assertNotNull(returnValue);
		assertEquals(returnValue.getClass(),domainClass.getClazz());
		GroovyObject groovyReturn = (GroovyObject)returnValue;
		
		assertEquals("wilma", groovyReturn.getProperty("firstName"));
	}
	

	public void testListPersistentMethods() {
		GrailsDomainClass domainClass = this.grailsApplication.getGrailsDomainClass("org.codehaus.groovy.grails.orm.hibernate.PersistentMethodTestClass");
		
		GroovyObject obj = domainClass.newInstance();
		obj.setProperty( "id", new Long(1) );
		obj.setProperty( "firstName", "fred" );
		obj.setProperty( "lastName", "flintstone" );
		
		obj.invokeMethod("save", null);
		
		GroovyObject obj2 = domainClass.newInstance();
		obj2.setProperty( "id", new Long(2) );
		obj2.setProperty( "firstName", "wilma" );
		obj2.setProperty( "lastName", "flintstone" );
		
		obj2.invokeMethod("save", null);	
		
		GroovyObject obj3 = domainClass.newInstance();
		obj3.setProperty( "id", new Long(3) );
		obj3.setProperty( "firstName", "dino" );
		obj3.setProperty( "lastName", "dinosaur" );
		
		obj3.invokeMethod("save", null);		
		
		// test plain list
		Object returnValue = obj.getMetaClass().invokeStaticMethod(obj,"list", null);
		assertNotNull(returnValue);
		assertTrue(returnValue instanceof List);
		
		List returnList = (List)returnValue;
		assertEquals(3, returnList.size());
		// test list with max value
		returnValue = obj.getMetaClass().invokeStaticMethod(obj,"list", new Object[]{ new Integer(1) });
		assertNotNull(returnValue);
		assertTrue(returnValue instanceof List);
		
		returnList = (List)returnValue;
		assertEquals(1, returnList.size());	
		
		// test list with order by
		returnValue = obj.getMetaClass().invokeStaticMethod(obj,"listOrderByFirstName", new Object[]{});
		assertNotNull(returnValue);
		assertTrue(returnValue instanceof List);
		
		returnList = (List)returnValue;
		obj = (GroovyObject)returnList.get(0);
		obj2 = (GroovyObject)returnList.get(1);
		
		assertEquals("dino", obj.getProperty("firstName"));
		assertEquals("fred", obj2.getProperty("firstName"));
		
	}
	protected void onSetUp() throws Exception {
		SpringConfig springConfig = new SpringConfig(grailsApplication);
		ConfigurableApplicationContext appCtx = (ConfigurableApplicationContext) 
		new XmlApplicationContextDriver().getApplicationContext(
				springConfig.getBeanReferences(), super.applicationContext);
		
		super.onSetUp();
	}
	
}
