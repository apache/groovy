package org.codehaus.groovy.grails.web.errors;

import groovy.lang.GroovyRuntimeException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;

/**
 */
public class GrailsExceptionResolver extends SimpleMappingExceptionResolver {

	/* (non-Javadoc)
	 * @see org.springframework.web.servlet.handler.SimpleMappingExceptionResolver#resolveException(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object, java.lang.Exception)
	 */
	public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {

		ModelAndView mv = super.resolveException(request, response, handler, ex);
			
		if(ex instanceof GroovyRuntimeException) {
			GroovyRuntimeException gre = (GroovyRuntimeException)ex;
			GrailsWrappedRuntimeException gwrex = new GrailsWrappedRuntimeException(gre);
			mv.addObject("exception",gwrex);
		}
				
		return mv;
	}


}
