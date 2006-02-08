package org.codehaus.groovy.grails.web.metaclass;

import groovy.lang.*;
import junit.framework.TestCase;
import org.codehaus.groovy.grails.commons.DefaultGrailsApplication;
import org.codehaus.groovy.grails.commons.GrailsApplication;
import org.codehaus.groovy.grails.commons.metaclass.PropertyAccessProxyMetaClass;
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsControllerHelper;
import org.codehaus.groovy.grails.web.servlet.mvc.SimpleGrailsControllerHelper;
import org.codehaus.groovy.grails.web.servlet.GrailsApplicationAttributes;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public class ControllerMetaClassTests extends TestCase {


    private GenericApplicationContext context;

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
                         "@Property list = {\n" +
                         "}\n" +
                         "}" );


         GrailsApplication application = new DefaultGrailsApplication(new Class[] { groovyClass },gcl);
         registerBeanDefinition(groovyClass,application, request,response);
         GrailsControllerHelper helper1 = new SimpleGrailsControllerHelper(application,context,new MockServletContext());
         try {
             helper1.handleURI("/test/list",request,response);
             GroovyObject go = (GroovyObject)request.getAttribute(GrailsApplicationAttributes.CONTROLLER);
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

    public void testRedirectDynamicMethod() throws Exception {

        GroovyClassLoader gcl = new GroovyClassLoader();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();


        request.addParameter("testParam", "testValue");

        Class groovyClass = gcl.parseClass( "class TestController {\n" +
                        "@Property next = {\n" +
                            "return ['success':this.params['testParam2']]" +
                        "}\n" +
                        "@Property list = {\n" +
                            "redirect(action:next,params:['testParam2':'testValue2'])\n" +
                        "}\n" +
                        "}" );
        Class secondController = gcl.parseClass( "class SecondController {\n" +
                "@Property list = {\n" +
                    "return redirect(action:'test/list',params:['testParam2':'testValue2'])\n" +
                "}\n" +
                "}" );

        GrailsApplication application = new DefaultGrailsApplication(new Class[] { groovyClass,secondController },gcl);
        registerBeanDefinition(groovyClass, application,request,response);
        registerBeanDefinition(secondController, application,request,response);

        GrailsControllerHelper helper1 = new SimpleGrailsControllerHelper(application,context,new MockServletContext());
        GrailsControllerHelper helper2 = new SimpleGrailsControllerHelper(application,context,new MockServletContext());

        // first test redirection within the same controller
        try {
            helper1.handleURI("/test/list",request,response);
        }
        catch(MissingMethodException mme) {
            fail("Missing method exception should not have been thrown!");
        }
        catch(MissingPropertyException mpex) {
            fail("Missing property exception should not have been thrown!");
        }
        // now redirection to another controller
        try {
            request = new MockHttpServletRequest();
            response = new MockHttpServletResponse();
            helper2.handleURI("/second/list",request,response);
        }
        catch(MissingMethodException mme) {
            fail("Missing method exception should not have been thrown!");
        }
        catch(MissingPropertyException mpex) {
            fail("Missing property exception should not have been thrown!");
        }
    }

    /*public void testChainDynamicMethod() throws Exception {

         GroovyClassLoader gcl = new GroovyClassLoader();
         MockHttpServletRequest request = new MockHttpServletRequest();
         MockHttpServletResponse response = new MockHttpServletResponse();
		
         request.addParameter("testParam", "testValue");
		
         Class groovyClass = gcl.parseClass( "class TestController {\n" +
                         "@Property next = {\n" +
                             "chain(action:this.next2,model:['mymodel2':'myvalue2'],params:['testParam2':this.params['testParam2']])\n" +
                         "}\n" +
                         "@Property next2 = {\n" +
                             "return ['success':this.params['testParam2']]" +
                         "}\n" +
                         "@Property list = {\n" +
                             "chain(action:this.next,model:['mymodel':'myvalue'],params:['testParam2':'testValue2'])\n" +
                         "}\n" +
                         "}" );
         Class secondController = gcl.parseClass( "class SecondController {\n" +
                 "@Property list = {\n" +
                     "return chain(action:'test/list',model:['mymodel':'myvalue'],params:['testParam2':'testValue2'])\n" +
                 "}\n" +
                 "}" );
		
         GrailsApplication application = new DefaultGrailsApplication(new Class[] { groovyClass,secondController },gcl);
         GroovyObject go = registerBeanDefinition(groovyClass, application,request,response);
         GroovyObject go2 = registerBeanDefinition(secondController, application,request,response);
		
         // first test redirection within the same controller
         try {
             Closure closure = (Closure)go.getProperty("list");
             Object returnValue = closure.call();
             assertNotNull(returnValue);
             assertTrue(returnValue instanceof ModelAndView);
             Map model = ((ModelAndView)returnValue).getModel();
			
             assertEquals("myvalue", model.get("mymodel"));
             assertEquals("myvalue2", model.get("mymodel2"));
             assertEquals("testValue2", model.get("success"));
         }
         catch(MissingMethodException mme) {
             fail("Missing method exception should not have been thrown!");
         }
         catch(MissingPropertyException mpex) {
             fail("Missing property exception should not have been thrown!");
         }
         // now redirection to another controller
         try {
             Closure closure = (Closure)go2.getProperty("list");
             Object returnValue = closure.call();
             assertNotNull(returnValue);
             assertTrue(returnValue instanceof ModelAndView);
             Map model = ((ModelAndView)returnValue).getModel();
			
             assertEquals("myvalue", model.get("mymodel"));
             assertEquals("testValue2", model.get("success"));
         }
         catch(MissingMethodException mme) {
             fail("Missing method exception should not have been thrown!");
         }
         catch(MissingPropertyException mpex) {
             fail("Missing property exception should not have been thrown!");
         }
     } */

    private void registerBeanDefinition(Class groovyClass,GrailsApplication application, HttpServletRequest request, HttpServletResponse response)
        throws Exception {
        ProxyMetaClass pmc = PropertyAccessProxyMetaClass.getInstance(groovyClass);
        // proof of concept to try out proxy meta class
        if(context == null)
            this.context = new GenericApplicationContext();

        BeanDefinition bd = new RootBeanDefinition(groovyClass,false);
        context.registerBeanDefinition( groovyClass.getName(), bd );
    }
}
