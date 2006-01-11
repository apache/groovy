/*
 * Copyright 2004-2005 the original author or authors.
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
package org.codehaus.groovy.grails.web.metaclass;

import groovy.lang.Closure;
import groovy.lang.GroovyObject;
import groovy.lang.MissingPropertyException;
import org.codehaus.groovy.grails.commons.GrailsControllerClass;
import org.codehaus.groovy.grails.commons.metaclass.AbstractDynamicMethodInvocation;
import org.codehaus.groovy.grails.commons.metaclass.GenericDynamicProperty;
import org.codehaus.groovy.grails.commons.metaclass.GroovyDynamicMethodsInterceptor;
import org.codehaus.groovy.grails.scaffolding.GrailsScaffolder;
import org.codehaus.groovy.grails.web.servlet.GrailsHttpServletRequest;
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsControllerHelper;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.beans.IntrospectionException;
/**
 * Adds dynamic methods and properties for Grails Controllers
 * 
 * @author Graeme Rocher
 * @since Oct 24, 2005
 */
public class ControllerDynamicMethods extends
	GroovyDynamicMethodsInterceptor {

	public static final String REQUEST_PROPERTY = "request";
	public static final String RESPONSE_PROPERTY = "response";
	public static final String ERRORS_PROPERTY = "errors";
	public static final String HAS_ERRORS_METHOD = "hasErrors";
	public static final String MODEL_AND_VIEW_PROPERTY = "modelAndView";

    protected GrailsControllerClass controllerClass;
	protected GrailsScaffolder scaffolder;
	private boolean scaffolding;


    public ControllerDynamicMethods( GroovyObject controller,GrailsControllerHelper helper,HttpServletRequest request, HttpServletResponse response) throws IntrospectionException {
        super(controller);

        this.controllerClass = helper.getControllerClassByName(controller.getClass().getName());

        // add dynamic properties
        addDynamicProperty(new GetParamsDynamicProperty(request,response));
        addDynamicProperty(new GetSessionDynamicProperty(request,response));
        addDynamicProperty(new GenericDynamicProperty(REQUEST_PROPERTY, HttpServletRequest.class,new GrailsHttpServletRequest( request,controller),true) );
        addDynamicProperty(new GenericDynamicProperty(RESPONSE_PROPERTY, HttpServletResponse.class,response,true) );
        addDynamicProperty(new GenericDynamicProperty(ERRORS_PROPERTY, Errors.class, null, false));
        addDynamicProperty(new GenericDynamicProperty(MODEL_AND_VIEW_PROPERTY, ModelAndView.class,null,false));

        // add dynamic methods
        addDynamicMethodInvocation( new RedirectDynamicMethod(helper,request,response) );
        addDynamicMethodInvocation( new ChainDynamicMethod(helper, request, response ) );
        addDynamicMethodInvocation( new RenderDynamicMethod(helper,request,response));
        addDynamicMethodInvocation( new RicoDynamicMethod(request,response));
        addDynamicMethodInvocation( new BindDynamicMethod(request,response));

        // the hasErrors() dynamic method that checks of there are any errors in the controller
        addDynamicMethodInvocation( new AbstractDynamicMethodInvocation(HAS_ERRORS_METHOD) {
            public Object invoke(Object target, Object[] arguments) {
                GroovyObject controller = (GroovyObject)target;
                Errors errors = (Errors)controller.getProperty(ERRORS_PROPERTY);
                return new Boolean(errors.hasErrors());
            }
        });

        this.scaffolding = this.controllerClass.isScaffolding();

        // if the controller is scaffolding get the scaffolder, then loop through all the
        // support actions by the scaffolder and register dynamic properties for those that don't exist
        if(this.scaffolding) {
            this.scaffolder = helper.getScaffolderForController(controllerClass.getFullName());
            if(this.scaffolder == null) {
                throw new IllegalStateException("Scaffolder is null when controller scaffold property is set to 'true'");
            }
            String[] scaffoldActions = this.scaffolder.getSupportedActionNames();
            for (int i = 0; i < scaffoldActions.length; i++) {
                try {
                    controller.getProperty(scaffoldActions[i]);
                }
                catch(MissingPropertyException mpe) {
                    addDynamicProperty(new GenericDynamicProperty(	scaffoldActions[i],
                                                                    Closure.class,
                                                                    scaffolder.getAction(controller,scaffoldActions[i]),
                                                                    true));
                }
            }
        }
    }

}
