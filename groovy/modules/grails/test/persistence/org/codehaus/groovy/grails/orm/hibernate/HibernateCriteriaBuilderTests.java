package org.codehaus.groovy.grails.orm.hibernate;

import groovy.lang.Closure;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import groovy.lang.MissingMethodException;
import groovy.util.Proxy;

import org.apache.commons.lang.ArrayUtils;
import org.codehaus.groovy.grails.commons.GrailsApplication;
import org.codehaus.groovy.grails.commons.GrailsDomainClass;
import org.codehaus.groovy.grails.commons.spring.SpringConfig;
import org.codehaus.groovy.runtime.InvokerInvocationException;
import org.hibernate.SessionFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;
import org.springmodules.beans.factory.drivers.xml.XmlApplicationContextDriver;

public class HibernateCriteriaBuilderTests extends
		AbstractDependencyInjectionSpringContextTests {
	
	protected GrailsApplication grailsApplication = null;
    protected SessionFactory sessionFactory = null;

	protected String[] getConfigLocations() {
		return new String[] { "org/codehaus/groovy/grails/orm/hibernate/grails-persistent-method-tests.xml" };
	}


	public void setGrailsApplication(GrailsApplication grailsApplication) {
		this.grailsApplication = grailsApplication;
	}

	
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}


	private Proxy parse(String groovy,String testClassName) throws Exception {
	
		
		GroovyClassLoader cl = this.grailsApplication.getClassLoader();
		Class clazz = 
		 cl.parseClass( "package test;\n" +
		 				"import grails.orm.*;\n" +
		 				"import org.hibernate.*;\n" +
		 				"class "+testClassName+" {\n" +
		 					"@Property SessionFactory sf;\n" +
		 					"@Property Class tc;\n" + 
		 					"@Property Closure test = {\n" +
		 						"def hcb = new HibernateCriteriaBuilder(tc,sf);\n" +
		 						"return hcb" + groovy +";\n" +
		 					"}\n" +
		 				"}");
		GroovyObject go = (GroovyObject)clazz.newInstance();
		go.setProperty("sf", this.sessionFactory);
		
		Class tc = this.grailsApplication.getGrailsDomainClasses()[0].getClazz();
		go.setProperty("tc", tc);
		
		Closure closure = (Closure)go.getProperty("test");
		return (Proxy)closure.call();
		
		
	}
	
	public void testHibernateCriteriaBuilder()
		throws Exception {
		GrailsDomainClass domainClass = this.grailsApplication.getGrailsDomainClass("org.codehaus.groovy.grails.orm.hibernate.PersistentMethodTestClass");
		
		GroovyObject obj = domainClass.newInstance();
		//obj.setProperty( "id", new Long(1) );
		obj.setProperty( "firstName", "fred" );
		obj.setProperty( "lastName", "flintstone" );
		obj.setProperty( "age", new Integer(45));
		
		obj.invokeMethod("save", null);
		
		GroovyObject obj2 = domainClass.newInstance();
		//obj2.setProperty( "id", new Long(2) );
		obj2.setProperty( "firstName", "wilma" );
		obj2.setProperty( "lastName", "flintstone" );
		obj2.setProperty( "age", new Integer(42));
		obj2.invokeMethod("save", null);	
		
		GroovyObject obj3 = domainClass.newInstance();
		//obj3.setProperty( "id", new Long(3) );
		obj3.setProperty( "firstName", "dino" );
		obj3.setProperty( "lastName", "dinosaur" );
		obj3.setProperty( "age", new Integer(12));
		obj3.invokeMethod("save", null);
		
		
		Proxy p = null;
		p = parse(	"{ " +
					"and { " +
						"eq('firstName','fred');" +
						"eq('lastName', 'flintstone')" +
					"}" +
				"}", "Test1");
		System.out.println("Criteria output = ");
		System.out.println(ArrayUtils.toString(p.invokeMethod("toArray",null)));		
		p = parse(	"{\n" +
						"and {\n" +
							"eq(\"firstName\",\"Fred\");\n" +
							"and {\n" +
								"eq(\"age\", 42)\n" +
								"eq(\"lastName\", \"flintstone\")\n" +
						     "}\n" +
						"}\n" +
					"}", "Test2");
		System.out.println("Criteria output = ");
		System.out.println(ArrayUtils.toString(p.invokeMethod("toArray",null)));
		p = parse(	"{\n" +
						"eq(\"firstName\",\"Fred\")\n" +
						"order(\"firstName\")\n" +
						"maxResults(10)\n" +
					"}", "Test3");
		System.out.println("Criteria output = ");
		System.out.println(ArrayUtils.toString(p.invokeMethod("toArray",null)));		
		
		// now test out illegal arguments
		try {
			// and expression with only one argument
			p = parse(	"{\n" +
					"and {\n" +
						"eq(\"firstName\",\"Fred\");\n" +
						"and {\n" +
							"eq(\"age\", 42)\n" +
					     "}\n" +
					"}\n" +
				"}", "Test4");			
			
			fail("Should have thrown illegal argument exception");					
		}
		catch(InvokerInvocationException iie) {
			// success!
			assertEquals( IllegalArgumentException.class, iie.getCause().getClass() );
		}
		
		try {
			// rubbish argument
			p = parse(	"{\n" +
					"and {\n" +
						"eq(\"firstName\",\"Fred\");\n" +
						"not {\n" +
							"eq(\"age\", 42)\n" +
							"rubbish()\n" +
					     "}\n" +
					"}\n" +
				"}", "Test5");			
			
			fail("Should have thrown illegal argument exception");					
		}
		catch(InvokerInvocationException iie) {
			// success!
			assertEquals( MissingMethodException.class, iie.getCause().getClass() );
		}		
	}
	
	protected void onSetUp() throws Exception {
		SpringConfig springConfig = new SpringConfig(grailsApplication);
		ConfigurableApplicationContext appCtx = (ConfigurableApplicationContext) 
		new XmlApplicationContextDriver().getApplicationContext(
				springConfig.getBeanReferences(), super.applicationContext);
		
		assertNotNull(appCtx);
			
		super.onSetUp();
	}	
}
