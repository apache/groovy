package org.codehaus.groovy.grails.scaffolding;

import groovy.lang.Closure;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import groovy.lang.MissingMethodException;
import groovy.lang.MissingPropertyException;
import groovy.lang.ProxyMetaClass;

import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.groovy.grails.commons.DefaultGrailsApplication;
import org.codehaus.groovy.grails.commons.GrailsApplication;
import org.codehaus.groovy.grails.commons.metaclass.PropertyAccessProxyMetaClass;
import org.codehaus.groovy.grails.commons.spring.SpringConfig;
import org.codehaus.groovy.grails.orm.hibernate.cfg.DefaultGrailsDomainConfiguration;
import org.codehaus.groovy.grails.web.metaclass.ControllerDynamicMethods;
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsControllerHelper;
import org.codehaus.groovy.grails.web.servlet.mvc.SimpleGrailsControllerHelper;
import org.hibernate.SessionFactory;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;
import org.springframework.web.servlet.ModelAndView;
import org.springmodules.beans.factory.drivers.xml.XmlApplicationContextDriver;

public class ControllerScaffoldingTests extends
AbstractDependencyInjectionSpringContextTests {

	private GenericApplicationContext localContext;
	private ConfigurableApplicationContext appCtx; 
	private GrailsApplication grailsApplication;
	private SessionFactory sessionFactory;
	private Class domainClass;
	private Class controllerClass;

	protected void onSetUp() throws Exception {
		GroovyClassLoader cl = new GroovyClassLoader();
		Thread.currentThread().setContextClassLoader(cl);
		
		
		Class tmpClass = cl.parseClass( "class ScaffoldController {\n" +
				"@Property boolean scaffold = true" +
		"}" );
		
		
		Class tmpClass2 = cl.parseClass( "class Scaffold {\n" +
		"@Property Long id\n" +
		"@Property Long version\n" +
		"@Property String name\n" +
		"}" );		
		
		this.controllerClass = tmpClass;
		this.domainClass = tmpClass2;
		
		//grailsApplication = new DefaultGrailsApplication(,cl);
		this.localContext = new GenericApplicationContext(super.applicationContext);
		
		
		
		ConstructorArgumentValues args = new ConstructorArgumentValues();
		args.addGenericArgumentValue(new Class[]{ controllerClass, domainClass});
		args.addGenericArgumentValue(cl);
		MutablePropertyValues propValues = new MutablePropertyValues();
		
		BeanDefinition grailsApplicationBean = new RootBeanDefinition(DefaultGrailsApplication.class,args,propValues);
		
		localContext.registerBeanDefinition( "grailsApplication", grailsApplicationBean );
		this.localContext.refresh();
		
		/*BeanDefinition applicationEventMulticaster = new RootBeanDefinition(SimpleApplicationEventMulticaster.class);
		context.registerBeanDefinition( "applicationEventMulticaster ", applicationEventMulticaster);*/
		
		this.grailsApplication = (GrailsApplication)localContext.getBean("grailsApplication");
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
		Thread.currentThread().setContextClassLoader(cl);		
		this.sessionFactory = config.buildSessionFactory();
		assertNotNull(this.sessionFactory);
		
		
		
		SpringConfig springConfig = new SpringConfig(grailsApplication);
		this.appCtx = (ConfigurableApplicationContext) 
		new XmlApplicationContextDriver().getApplicationContext(
				springConfig.getBeanReferences(), this.localContext);
		
		assertNotNull(appCtx);
		
		GroovyObject domainObject = (GroovyObject)domainClass.newInstance();
		domainObject.setProperty("name", "fred");
		domainObject.invokeMethod("save", new Object[0]);
		
		GroovyObject domainObject2 = (GroovyObject)domainClass.newInstance();
		domainObject2.setProperty("name", "wilma");
		domainObject2.invokeMethod("save", new Object[0]);		
			
		super.onSetUp();
	}	
	
	public void testScaffoldList() throws Exception {

		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		request.setRequestURI("/scaffold/list");
		
		GroovyObject go = configureDynamicGO(controllerClass, grailsApplication,request,response);
		
		// first test redirection within the same controller		
		try {
			Closure closure = (Closure)go.getProperty("list");
			Object returnValue = closure.call();
			assertNotNull(returnValue);
			assertTrue(returnValue instanceof ModelAndView);
			ModelAndView mv = (ModelAndView)returnValue;
			assertEquals("/scaffold/list",mv.getViewName());
			assertTrue(mv.getModel().containsKey("scaffoldList"));
		}
		catch(MissingMethodException mme) {
			fail("Missing method exception should not have been thrown!");
		}	
		catch(MissingPropertyException mpex) {
			fail("Missing property exception should not have been thrown!");
		}	
	}	
	
	public void testScaffoldDelete() throws Exception {

		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		request.setRequestURI("/scaffold/delete");
		request.addParameter("id", "1");
		
		GroovyObject go = configureDynamicGO(controllerClass, grailsApplication,request,response);
		
		// first test redirection within the same controller		
		try {
			Closure closure = (Closure)go.getProperty("delete");
			Object returnValue = closure.call();
			assertNotNull(returnValue);
			assertTrue(returnValue instanceof ModelAndView);
			ModelAndView mv = (ModelAndView)returnValue;
			// should delegate to list 
			assertEquals("/scaffold/list",mv.getViewName());
			assertTrue(mv.getModel().containsKey("scaffoldList"));
			assertNull(mv.getModel().get("scaffold"));
		}
		catch(MissingMethodException mme) {
			fail("Missing method exception should not have been thrown!");
		}	
		catch(MissingPropertyException mpex) {
			fail("Missing property exception should not have been thrown!");
		}	
	}		
	
	public void testScaffoldSave() throws Exception {

		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		request.setRequestURI("/scaffold/save");
		request.addParameter("name", "dino");
		
		GroovyObject go = configureDynamicGO(controllerClass, grailsApplication,request,response);
		
		// first test redirection within the same controller		
		try {
			Closure closure = (Closure)go.getProperty("save");
			Object returnValue = closure.call();
			assertNotNull(returnValue);
			assertTrue(returnValue instanceof ModelAndView);
			ModelAndView mv = (ModelAndView)returnValue;
			// should end up at the show view
			assertEquals("/scaffold/show",mv.getViewName());
			// and contain the appropriate model
			assertTrue(mv.getModel().containsKey("scaffold"));
			GroovyObject domainObject = (GroovyObject)mv.getModel().get("scaffold");
			assertNotNull(domainObject);
			assertEquals("dino", domainObject.getProperty("name"));
		}
		catch(MissingMethodException mme) {
			fail("Missing method exception should not have been thrown!");
		}	
		catch(MissingPropertyException mpex) {
			fail("Missing property exception should not have been thrown!");
		}	
	}	
	
	public void testScaffoldUpdate() throws Exception {

		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		request.setRequestURI("/scaffold/update");
		request.addParameter("id", "1");
		request.addParameter("name", "fredjnr");
		
		GroovyObject go = configureDynamicGO(controllerClass, grailsApplication,request,response);
		
		// first test redirection within the same controller		
		try {
			Closure closure = (Closure)go.getProperty("update");
			Object returnValue = closure.call();
			assertNotNull(returnValue);
			assertTrue(returnValue instanceof ModelAndView);
			ModelAndView mv = (ModelAndView)returnValue;
			// should end up at the show view
			assertEquals("/scaffold/show",mv.getViewName());
			// and contain the appropriate model
			assertTrue(mv.getModel().containsKey("scaffold"));
			GroovyObject domainObject = (GroovyObject)mv.getModel().get("scaffold");
			assertNotNull(domainObject);
			assertEquals("fredjnr", domainObject.getProperty("name"));
		}
		catch(MissingMethodException mme) {
			fail("Missing method exception should not have been thrown!");
		}	
		catch(MissingPropertyException mpex) {
			fail("Missing property exception should not have been thrown!");
		}	
	}		
	
	public void testScaffoldShow() throws Exception {

		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		request.setRequestURI("/scaffold/show");
		request.addParameter("id", "1");
		
		GroovyObject go = configureDynamicGO(controllerClass, grailsApplication,request,response);
		
		// first test redirection within the same controller		
		try {
			Closure closure = (Closure)go.getProperty("show");
			Object returnValue = closure.call();
			assertNotNull(returnValue);
			assertTrue(returnValue instanceof ModelAndView);
			ModelAndView mv = (ModelAndView)returnValue;
			assertEquals("/scaffold/show",mv.getViewName());
			assertTrue(mv.getModel().containsKey("scaffold"));
			
			//	now try a rubish id
			request = new MockHttpServletRequest();
			request.setRequestURI("/scaffold/show");
			request.addParameter("id", "13423");			
			go = configureDynamicGO(controllerClass, grailsApplication,request,response);
			
			closure = (Closure)go.getProperty("show");
			returnValue = closure.call();
			assertNotNull(returnValue);
			assertTrue(returnValue instanceof ModelAndView);
			mv = (ModelAndView)returnValue;
			assertEquals("/scaffold/show",mv.getViewName());
			assertTrue(mv.getModel().containsKey("scaffold"));
			
			// now try a different action name that uses the same class
			// to implement scaffolding
			request = new MockHttpServletRequest();
			request.setRequestURI("/scaffold/edit");
			request.addParameter("id", "1");			
			go = configureDynamicGO(controllerClass, grailsApplication,request,response);
			
			closure = (Closure)go.getProperty("edit");
			returnValue = closure.call();
			assertNotNull(returnValue);
			assertTrue(returnValue instanceof ModelAndView);
			mv = (ModelAndView)returnValue;
			assertEquals("/scaffold/edit",mv.getViewName());
			assertTrue(mv.getModel().containsKey("scaffold"));			
			
		}
		catch(MissingMethodException mme) {
			fail("Missing method exception should not have been thrown!");
		}	
		catch(MissingPropertyException mpex) {
			fail("Missing property exception should not have been thrown!");
		}							
	}		
	
	

	private GroovyObject configureDynamicGO(Class groovyClass,GrailsApplication application, HttpServletRequest request, HttpServletResponse response)
	throws Exception {
	ProxyMetaClass pmc = PropertyAccessProxyMetaClass.getInstance(groovyClass);
	// proof of concept to try out proxy meta class
	
	BeanDefinition bd = new RootBeanDefinition(groovyClass,false);
	localContext.registerBeanDefinition( groovyClass.getName(), bd );
			
	
	GrailsControllerHelper helper = new SimpleGrailsControllerHelper(application,this.appCtx);
	GroovyObject go = (GroovyObject)groovyClass.newInstance();
	pmc.setInterceptor( new ControllerDynamicMethods(go,helper,request,response) );
	
	
	go.setMetaClass( pmc );
	return go;
}

	protected String[] getConfigLocations() {
		return new String[] { "org/codehaus/groovy/grails/scaffolding/grails-scaffolding-tests.xml" };
	}	
	
	
}
