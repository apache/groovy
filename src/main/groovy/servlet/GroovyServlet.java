/*
 * Copyright 2003 (C) Sam Pullara. All Rights Reserved.
 * 
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided that the
 * following conditions are met: 1. Redistributions of source code must retain
 * copyright statements and notices. Redistributions must also contain a copy
 * of this document. 2. Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the distribution. 3.
 * The name "groovy" must not be used to endorse or promote products derived
 * from this Software without prior written permission of The Codehaus. For
 * written permission, please contact info@codehaus.org. 4. Products derived
 * from this Software may not be called "groovy" nor may "groovy" appear in
 * their names without prior written permission of The Codehaus. "groovy" is a
 * registered trademark of The Codehaus. 5. Due credit should be given to The
 * Codehaus - http://groovy.codehaus.org/
 * 
 * THIS SOFTWARE IS PROVIDED BY THE CODEHAUS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE CODEHAUS OR ITS CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
 * DAMAGE.
 *  
 */
package groovy.servlet;

import groovy.lang.Binding;
import groovy.util.GroovyScriptEngine;
import groovy.util.ResourceConnector;
import groovy.util.ResourceException;
import groovy.util.ScriptException;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This servlet should be registered to *.groovy in the web.xml.
 * 
 * <servlet><servlet-name>Groovy</servlet-name><servlet-class>
 * groovy.servlet.GroovyServlet</servlet-class></servlet>
 * 
 * <servlet-mapping><servlet-name>Groovy</servlet-name><url-pattern>
 * *.groovy</url-pattern></servlet-mapping>
 * 
 * @author Sam Pullara
 */
public class GroovyServlet extends HttpServlet implements ResourceConnector {

	private ServletContext sc;

	private static Map servletCache = Collections.synchronizedMap(new HashMap());
	private static ClassLoader parent;
	private static GroovyScriptEngine gse;
	
	public ServletContext getServletContext() {
		return sc;
	}
	
	private static class ServletCacheEntry {
		private Class servletScriptClass;
		private long lastModified;
		private Map dependencies = new HashMap();
	}

	public void init(ServletConfig config) {
		// Get the servlet context
		sc = config.getServletContext();
		sc.log("Groovy servlet initialized");

		// Ensure that we use the correct classloader so that we can find
		// classes in an application server.
		parent = Thread.currentThread().getContextClassLoader();
		if (parent == null)
			parent = GroovyServlet.class.getClassLoader();
		
		// Set up the scripting engine
		gse = new GroovyScriptEngine(this);
	}

	public URLConnection getResourceConnection(String name) throws ResourceException {
		try {
			URL url = sc.getResource("/" + name);
			if (url == null) {
				url = sc.getResource("/WEB-INF/groovy/" + name);
				if (url == null) {
					throw new ResourceException("Resource " + name + " not found");
				}
			}
			return url.openConnection();
		} catch (IOException ioe) {
			throw new ResourceException("Problem reading resource " + name);
		}
	}	
	
	public void service(ServletRequest request, ServletResponse response) throws ServletException, IOException {

		// Convert the generic servlet request and response to their Http
		// versions
		final HttpServletRequest httpRequest = (HttpServletRequest) request;
		final HttpServletResponse httpResponse = (HttpServletResponse) response;

		// Get the name of the Groovy script (intern the name so that we can
		// lock on it)
		int contextLength = httpRequest.getContextPath().length();
		String scriptFilename = httpRequest.getRequestURI().substring(contextLength).substring(1);

		// Set up the script context
		Binding binding = new Binding();
		binding.setVariable("request", httpRequest);
		binding.setVariable("response", httpResponse);
		binding.setVariable("application", sc);
		binding.setVariable("session", httpRequest.getSession(true));
		binding.setVariable("out", httpResponse.getWriter());

		// Form parameters. If there are multiple its passed as a list.
		for (Enumeration paramEnum = request.getParameterNames(); paramEnum.hasMoreElements();) {
			String key = (String) paramEnum.nextElement();
			if (binding.getVariable(key) == null) {
				String[] values = request.getParameterValues(key);
				if (values.length == 1) {
					binding.setVariable(key, values[0]);
				} else {
					binding.setVariable(key, values);
				}
			}
		}

		// Set it to HTML by default
		response.setContentType("text/html");
		
		// Run the script
		try {
			gse.run(scriptFilename, binding);
		} catch (ScriptException se) {
			httpResponse.sendError(500);
		} catch (ResourceException re) {
			httpResponse.sendError(404);
		}
	}

}
