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
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsControllerHelper;
import org.codehaus.groovy.grails.web.servlet.GrailsHttpServletRequest;
import org.codehaus.groovy.grails.commons.metaclass.GenericDynamicProperty;
import org.codehaus.groovy.grails.commons.metaclass.GroovyDynamicMethodsInterceptor;

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

    public TagLibDynamicMethods(GroovyObject taglib, GroovyObject controller, HttpServletRequest request, HttpServletResponse response) throws IntrospectionException {
        super(taglib);

        addDynamicProperty(new GenericDynamicProperty(OUT_PROPERTY, Writer.class,false));

        // add dynamic properties
        addDynamicProperty(new GetParamsDynamicProperty(request,response));
        addDynamicProperty(new GetSessionDynamicProperty(request,response));
        addDynamicProperty(new GenericDynamicProperty(REQUEST_PROPERTY, HttpServletRequest.class,new GrailsHttpServletRequest( request,controller),true) );
        addDynamicProperty(new GenericDynamicProperty(RESPONSE_PROPERTY, HttpServletResponse.class,response,true) );
        
    }
}
