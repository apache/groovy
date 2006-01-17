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
package org.codehaus.groovy.grails.web.errors;

import org.springframework.web.context.ServletContextAware;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *  Exception resolver that wraps any runtime exceptions with a GrailsWrappedException instance
 * 
 * @author Graeme Rocher
 * @since 22 Dec, 2005
 */
public class GrailsExceptionResolver  extends SimpleMappingExceptionResolver implements ServletContextAware {
    private ServletContext servletContext;

    /* (non-Javadoc)
      * @see org.springframework.web.servlet.handler.SimpleMappingExceptionResolver#resolveException(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object, java.lang.Exception)
      */
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        ModelAndView mv = super.resolveException(request, response, handler, ex);
        GrailsWrappedRuntimeException gwrex = new GrailsWrappedRuntimeException(servletContext,ex);
        mv.addObject("exception",gwrex);
        return mv;
    }


    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }
}
