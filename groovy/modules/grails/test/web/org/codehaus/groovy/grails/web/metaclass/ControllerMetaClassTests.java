package org.codehaus.groovy.grails.web.metaclass;

import java.util.Map;

import org.codehaus.groovy.grails.metaclass.PropertyAccessProxyMetaClass;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import groovy.lang.MissingMethodException;
import groovy.lang.MissingPropertyException;
import groovy.lang.ProxyMetaClass;
import groovy.lang.TracingInterceptor;
import junit.framework.TestCase;

public class ControllerMetaClassTests extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testMetaClassProxy()
		throws Exception {
		
		GroovyClassLoader gcl = new GroovyClassLoader();
		
		Class groovyClass = gcl.parseClass( "class TestClass {\n" +
						"def testMethod() {\n" +
						"}\n" +
						"}" );
		
		ProxyMetaClass pmc = ProxyMetaClass.getInstance(groovyClass);
		// proof of concept to try out proxy meta class
		pmc.setInterceptor( new TracingInterceptor() {
			public boolean doInvoke() {
				return false;
			}
		});
		
		GroovyObject go = (GroovyObject)groovyClass.newInstance();
		
		go.setMetaClass( pmc );
		try {
			// invoke real method
			go.invokeMethod("testMethod", new Object[]{});
			// invoke fake method
			go.invokeMethod("fakeMethod", new Object[]{});		
		}
		catch(MissingMethodException mme) {
			fail("Missing method exception should not have been thrown!");
		}
	}
	
	public void testParamsDynamicProperty() throws Exception {
		
		GroovyClassLoader gcl = new GroovyClassLoader();
		MockHttpServletRequest request = new MockHttpServletRequest();
		MockHttpServletResponse response = new MockHttpServletResponse();
		
		request.addParameter("testParam", "testValue");
		
		Class groovyClass = gcl.parseClass( "class TestController {\n" +
						"def testMethod() {\n" +
						"}\n" +
						"}" );
		
		ProxyMetaClass pmc = PropertyAccessProxyMetaClass.getInstance(groovyClass);
		// proof of concept to try out proxy meta class
		pmc.setInterceptor( new ControllerDynamicMethodsInterceptor(groovyClass,request,response) );
		
		GroovyObject go = (GroovyObject)groovyClass.newInstance();
		
		go.setMetaClass( pmc );
		try {
			Object params = go.getProperty( "params" );			
			assertNotNull(params);
			assertTrue(params instanceof Map);
			
			Map paramsMap = (Map)params;
			assertTrue(paramsMap.containsKey("testParam"));
			assertEquals("testValue",paramsMap.get("testParam"));
		}
		catch(MissingMethodException mme) {
			fail("Missing method exception should not have been thrown!");
		}	
		catch(MissingPropertyException mpex) {
			fail("Missing property exception should not have been thrown!");
		}
	}
}
