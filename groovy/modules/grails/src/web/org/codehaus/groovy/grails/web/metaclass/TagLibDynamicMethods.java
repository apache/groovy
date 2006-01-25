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

import groovy.lang.GroovyObject;
import groovy.lang.ProxyMetaClass;
import groovy.lang.MissingMethodException;
import org.codehaus.groovy.grails.commons.metaclass.GenericDynamicProperty;
import org.codehaus.groovy.grails.commons.metaclass.GroovyDynamicMethodsInterceptor;
import org.codehaus.groovy.grails.commons.metaclass.AbstractDynamicMethodInvocation;
import org.codehaus.groovy.grails.web.taglib.exceptions.GrailsTagException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.beans.IntrospectionException;
import java.io.Writer;

/**
 * <p>Represents a controller class in Grails.
 *
 * @author Graeme Rocher
 * @since Jan 14, 20056
 */
public class TagLibDynamicMethods extends GroovyDynamicMethodsInterceptor {
    public static final String REQUEST_PROPERTY = "request";
    public static final String RESPONSE_PROPERTY = "response";
    public static final String OUT_PROPERTY = "out";
    public static final String GRAILS_ATTRIBUTES = "grailsAttributes";
    private static final String THROW_TAG_ERROR_METHOD = "throwTagError";

    public TagLibDynamicMethods(GroovyObject taglib, GroovyObject controller, HttpServletRequest request, HttpServletResponse response) throws IntrospectionException {
        super(taglib);

        ProxyMetaClass controllerMetaClass = (ProxyMetaClass)controller.getMetaClass();
        ControllerDynamicMethods controllerDynamicMethods = (ControllerDynamicMethods)controllerMetaClass.getInterceptor();


        addDynamicProperty(new GenericDynamicProperty(OUT_PROPERTY, Writer.class,false));

        // add dynamic properties (shared with controller)
        addDynamicProperty(controllerDynamicMethods.getDynamicProperty(GetParamsDynamicProperty.PROPERTY_NAME));
        addDynamicProperty(controllerDynamicMethods.getDynamicProperty(GetSessionDynamicProperty.PROPERTY_NAME));
        addDynamicProperty(controllerDynamicMethods.getDynamicProperty(ControllerDynamicMethods.REQUEST_PROPERTY));
        addDynamicProperty(controllerDynamicMethods.getDynamicProperty(ControllerDynamicMethods.RESPONSE_PROPERTY) );
        addDynamicProperty(controllerDynamicMethods.getDynamicProperty(ControllerDynamicMethods.SERVLET_CONTEXT) );
        addDynamicProperty(controllerDynamicMethods.getDynamicProperty(ControllerDynamicMethods.GRAILS_ATTRIBUTES) );

        addDynamicMethodInvocation(new AbstractDynamicMethodInvocation(THROW_TAG_ERROR_METHOD) {
            public Object invoke(Object target, Object[] arguments) {
                if(arguments.length == 0)
                    throw new MissingMethodException(THROW_TAG_ERROR_METHOD,target.getClass(),arguments);
                throw new GrailsTagException(arguments[0].toString());
            }
        });
    }
}
