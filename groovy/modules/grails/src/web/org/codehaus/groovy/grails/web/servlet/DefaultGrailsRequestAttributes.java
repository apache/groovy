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
package org.codehaus.groovy.grails.web.servlet;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.validation.Errors;
import groovy.lang.GroovyObject;

import javax.servlet.ServletRequest;
import javax.servlet.ServletContext;

/**
 * @author Graeme Rocher
 * @since 17-Jan-2006
 */
public class DefaultGrailsRequestAttributes implements GrailsRequestAttributes {

    private ServletContext context;

    public DefaultGrailsRequestAttributes(ServletContext context) {
        this.context = context;
    }

    public WebApplicationContext getApplicationContext() {
        return (WebApplicationContext)this.context.getAttribute(APPLICATION_CONTEXT);
    }

    public GroovyObject getController(ServletRequest request) {
        return (GroovyObject)request.getAttribute(CONTROLLER);
    }

    public GroovyObject getTagLib(ServletRequest request) {
        return (GroovyObject)request.getAttribute(TAG_LIB);
    }

    public Errors getErrors(ServletRequest request) {
        return (Errors)request.getAttribute(ERRORS);
    }
}
